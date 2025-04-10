<template>
  <el-container class="role-container" direction="vertical">
    <!-- 主体区域 - 左侧角色列表 + 右侧权限信息 -->
    <el-container style="flex: 1; overflow: hidden;">
      <!-- 左侧角色列表 -->
      <el-aside width="300px" style="margin-right: 10px;">
        <el-card shadow="hover" style="height: 100%; display: flex; flex-direction: column; overflow: hidden;">
          <template #header>
            <div class="card-header">
              <div class="header-actions">
                <el-input
                    v-model="searchKeyword"
                    placeholder="搜索角色名称或编码"
                    clearable
                    prefix-icon="Search"
                    size="small"
                    style="width: 180px; margin-right: 10px;"
                />
                <el-button
                    type="primary"
                    size="small"
                    v-permission="['system:role:add']"
                    @click="handleAddRole"
                >
                  <el-icon>
                    <Plus/>
                  </el-icon>
                  新增
                </el-button>
              </div>
            </div>
          </template>
          <div v-loading="appStore.getLoading" class="role-list-container">
            <div
                v-for="role in filteredRoleList"
                :key="role.id"
                class="role-item"
                :class="{ 'role-item-active': currentRole && currentRole.id === role.id }"
                @click="handleRoleSelect(role)"
            >
              <div class="role-info">
                <div class="role-name">{{ role.roleName }}</div>
                <div class="role-code">{{ role.roleCode }}</div>
              </div>
              <div class="role-actions">
                <el-dropdown trigger="hover" @command="(command) => handleCommand(command, role)">
                  <span class="el-dropdown-link">
                    <el-icon><MoreFilled/></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <div v-permission="['system:role:edit']">
                        <el-dropdown-item command="edit">修改</el-dropdown-item>
                      </div>
                      <div v-permission="['system:role:delete']">
                        <el-dropdown-item command="delete" divided>删除
                        </el-dropdown-item>
                      </div>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </el-card>
      </el-aside>

      <!-- 右侧权限信息 -->
      <el-main style="padding: 0; overflow: hidden;">
        <el-card shadow="hover" style="height: 100%;">
          <template #header>
            <div class="card-header">
              <span>{{ currentRole ? `${currentRole.roleName} 的权限` : '请选择角色' }}</span>
              <div v-if="currentRole">
                <el-button type="primary" v-permission="['system:role:updatePermission']" size="small"
                           @click="handleSavePermissions">保存权限
                </el-button>
              </div>
            </div>
          </template>

          <!-- 权限树形表格 -->
          <el-table
              v-permission="['system:role:list']"
              v-loading="appStore.loading"
              :data="permissionTree"
              row-key="id"
              border
              height="100%"
              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          >
            <el-table-column prop="name" label="权限名称" min-width="220">
              <template #default="scope">
                <span>{{ scope.row.name }}</span>
                <el-tag
                    size="small"
                    :type="getPermissionTypeTag(scope.row.type)"
                    class="permission-type-tag"
                >
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="permKey" label="权限标识" min-width="150"/>
            <el-table-column label="操作" min-width="120">
              <template #default="scope">
                <el-checkbox
                    v-model="scope.row.checked"
                    @change="(val) => handlePermissionCheck(scope.row, val)"
                >
                  分配
                </el-checkbox>
              </template>
            </el-table-column>
            <template #empty>
              <el-empty description="暂无权限数据" :image-size="120"/>
            </template>
          </el-table>
        </el-card>
      </el-main>
    </el-container>

    <!-- 角色对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
        width="500px"
        :close-on-click-modal="false"
        @closed="handleDialogClosed"
    >
      <el-form
          ref="roleFormRef"
          :model="roleForm"
          :rules="roleFormRules"
          label-width="100px"
          status-icon
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"/>
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码"/>
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input v-model="roleForm.roleDesc" type="textarea" placeholder="请输入角色描述"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitRoleForm">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {MoreFilled, Plus} from '@element-plus/icons-vue'
import {getRoleList, createRole, updateRole, assignPermissions, deleteRole} from '@/api/system/role'
import { getPermissionTreeWithRole } from '@/api/system/menu'
import {useAppStore} from '@/store'

const appStore = useAppStore()

// 角色列表数据
const roleList = ref([])
const currentRole = ref(null)

// 权限列表数据
const permissionTree = ref([])
const permissionList = ref([])

// 角色表单
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const submitLoading = ref(false)
const roleFormRef = ref(null)

const roleForm = reactive({
  id: '',
  roleName: '',
  roleCode: '',
  roleDesc: ''
})

const roleFormRules = {
  roleName: [
    {required: true, message: '请输入角色名称', trigger: 'blur'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
  ],
  roleCode: [
    {required: true, message: '请输入角色编码', trigger: 'blur'},
    {min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur'}
  ]
}

// 获取角色列表
const fetchRoleList = async () => {
  const res = await getRoleList()
  roleList.value = res.data
}

// 初始化数据
onMounted(async () => {
  // 获取角色列表
  await fetchRoleList()
  // 默认不选中任何角色
  currentRole.value = null
})

// 处理角色选择
const handleRoleSelect = async(role) => {
  if (!role) return
  
  currentRole.value = role
  
  // 直接从后端获取已包含checked属性的权限树
  const res = await getPermissionTreeWithRole(role.id)
  permissionTree.value = res.data || []
}

// 获取权限类型标签样式
const getPermissionTypeTag = (type) => {
  switch (type) {
    case '菜单':
      return 'primary'
    case '按钮':
      return 'success'
    default:
      return 'info'
  }
}

// 处理权限选择
const handlePermissionCheck = (permission, checked) => {
  // 更新当前节点的选中状态
  permission.checked = checked

  // 如果有子节点，同步更新子节点的选中状态
  if (permission.children && permission.children.length > 0) {
    updateChildrenChecked(permission.children, checked)
  }

  // 更新父节点的选中状态
  updateParentChecked(permissionTree.value)
}

// 更新子节点的选中状态
const updateChildrenChecked = (children, checked) => {
  children.forEach(child => {
    child.checked = checked
    if (child.children && child.children.length > 0) {
      updateChildrenChecked(child.children, checked)
    }
  })
}

// 更新父节点的选中状态
const updateParentChecked = (permissions, parent = null) => {
  if (!parent) {
    permissions.forEach(permission => {
      if (permission.children && permission.children.length > 0) {
        const allChecked = permission.children.every(child => child.checked)
        const someChecked = permission.children.some(child => child.checked)
        permission.checked = allChecked
        permission.indeterminate = someChecked && !allChecked
        updateParentChecked(permission.children, permission)
      }
    })
  }
}

// 保存权限
const handleSavePermissions = async () => {
  if (!currentRole.value) {
    ElMessage.warning('请先选择角色')
    return
  }

  // 收集选中的权限ID
  const selectedPermissions = collectSelectedPermissions(permissionTree.value)
  console.log('选中的权限ID:', selectedPermissions)

  // 调用权限分配API
  await assignPermissions(currentRole.value.id, selectedPermissions)
  ElMessage.success('权限分配成功')
}

// 收集选中的权限ID
const collectSelectedPermissions = (permissions) => {
  const result = []

  const collect = (items) => {
    items.forEach(item => {
      if (item.checked) {
        result.push(item.id)
      }

      if (item.children && item.children.length > 0) {
        collect(item.children)
      }
    })
  }

  collect(permissions)
  return result
}

// 处理新增角色
const handleAddRole = () => {
  dialogType.value = 'add'
  resetRoleForm()
  dialogVisible.value = true
}

// 处理编辑角色
const handleEditRole = (role) => {
  dialogType.value = 'edit'
  resetRoleForm()

  // 填充表单数据
  roleForm.id = role.id
  roleForm.roleName = role.roleName
  roleForm.roleCode = role.roleCode
  roleForm.roleDesc = role.roleDesc || ''

  dialogVisible.value = true
}

// 提交角色表单
const submitRoleForm = async () => {
  if (!roleFormRef.value) return

  // 表单验证
  await roleFormRef.value.validate()

  // 开始提交
  submitLoading.value = true

  try {
    // 构建提交数据
    const submitData = {
      roleName: roleForm.roleName,
      roleCode: roleForm.roleCode,
      roleDesc: roleForm.roleDesc
    }

    // 根据操作类型调用不同的API
    if (dialogType.value === 'add') {
      // 创建新角色
      await createRole(submitData)
      ElMessage.success('角色创建成功')
    } else {
      // 更新角色 - 添加ID
      submitData.id = roleForm.id
      await updateRole(submitData)
      ElMessage.success('角色更新成功')
    }

    // 关闭对话框
    dialogVisible.value = false

    // 刷新角色列表
    fetchRoleList()
  } finally {
    submitLoading.value = false
  }
}

// 重置角色表单
const resetRoleForm = () => {
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }

  // 手动重置表单数据
  Object.keys(roleForm).forEach(key => {
    roleForm[key] = ''
  })
}

// 监听对话框关闭事件，重置表单
const handleDialogClosed = () => {
  resetRoleForm()
}

// 处理下拉菜单命令
const handleCommand = (command, role) => {
  // 阻止事件冒泡，防止触发行点击事件
  event.stopPropagation()

  if (command === 'edit') {
    handleEditRole(role)
  } else if (command === 'delete') {
    handleDeleteRole(role)
  }
}

// 处理删除角色
const handleDeleteRole = (role) => {
  ElMessageBox.confirm(
      `确定要删除角色 "${role.roleName}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    // 调用API删除角色
    await deleteRole(role.id)

    // 删除成功
    ElMessage.success(`角色 "${role.roleName}" 已删除`)

    // 刷新角色列表
    await fetchRoleList()

    // 如果删除的是当前选中的角色，清空当前选中
    if (currentRole.value && currentRole.value.id === role.id) {
      currentRole.value = null
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 搜索关键字
const searchKeyword = ref('')

// 过滤后的角色列表
const filteredRoleList = computed(() => {
  if (!searchKeyword.value) {
    return roleList.value
  }

  const keyword = searchKeyword.value.toLowerCase()
  return roleList.value.filter(role => {
    return (
        (role.roleName && role.roleName.toLowerCase().includes(keyword)) ||
        (role.roleCode && role.roleCode.toLowerCase().includes(keyword))
    )
  })
})
</script>

<style scoped>
.role-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.role-list-container {
  height: 100%;
  overflow-y: auto;
}

.role-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.role-item:hover {
  background-color: #f5f7fa;
}

.role-item-active {
  background-color: #ecf5ff;
}

.role-info {
  flex: 1;
  overflow: hidden;
}

.role-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-code {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-actions {
  margin-left: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #909399;
  display: flex;
  align-items: center;
  font-size: 18px;
}

.el-dropdown-link:hover {
  color: #409eff;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #909399;
  font-size: 14px;
}

/* 添加样式确保卡片内容区域可以滚动 */
:deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow-y: auto;
  padding: 0;
}

/* 权限类型标签样式 */
.permission-type-tag {
  margin-left: 8px;
  font-size: 11px;
  height: 20px;
  line-height: 18px;
  padding: 0 5px;
  border-radius: 2px;
  vertical-align: middle;
}
</style> 