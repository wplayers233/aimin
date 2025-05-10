<template>
  <el-container class="user-container" direction="vertical">
    <!-- 工具栏 -->
    <el-header height="auto" style="padding: 0; margin-bottom: 10px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入关键字搜索"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="18" style="display: flex; justify-content: flex-end;">
          <el-tooltip
            content="请先选择左侧部门，再添加用户"
            :disabled="!!searchForm.deptId"
            placement="top"
          >
            <el-button type="primary" v-permission="['system:user:add']" @click="handleAdd" :disabled="!searchForm.deptId">新增用户</el-button>
          </el-tooltip>
          <el-button type="danger" v-permission="['system:user:delete']" :disabled="!selectedRows.length" @click="handleBatchDelete" style="margin-left: 10px;">批量删除</el-button>
          <el-button type="success" v-permission="['system:user:export']" @click="handleExport" style="margin-left: 10px;">导出数据</el-button>
        </el-col>
      </el-row>
    </el-header>

    <!-- 主体区域 - 左侧树形结构 + 右侧表格 -->
    <el-container style="flex: 1; overflow: hidden;">
      <!-- 左侧公司架构树 -->
      <el-aside width="250px" style="margin-right: 10px;">
        <el-card shadow="hover" style="height: 100%;">
          <template #header>
            <div class="card-header">
              <span>公司架构</span>
              <el-button type="primary" size="small" @click="refreshDepartmentTree">刷新</el-button>
            </div>
          </template>
          <el-tree
            ref="departmentTreeRef"
            :data="departmentTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleDepartmentSelect"
          />
        </el-card>
      </el-aside>

      <!-- 右侧表格区域 -->
      <el-main style="padding: 0; overflow: hidden;">
        <el-card shadow="hover" style="height: 100%;">
          <el-table
            v-loading="appStore.loading"
            :data="tableData"
            :height="tableHeight"
            v-permission="['system:user:list']"
            @selection-change="handleSelectionChange"
          >
            <template #empty>
              <el-empty description="暂无用户数据" />
            </template>
            <el-table-column type="selection" width="55" fixed />
            <el-table-column prop="id" label="用户ID" min-width="80" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
            <el-table-column prop="gender" label="性别" min-width="80">
              <template #default="scope">
                <el-tag :type="scope.row.gender === true ? 'primary' : 'danger'">
                  {{ scope.row.gender === true ? '男' : '女' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" min-width="120" />
            <el-table-column prop="status" label="状态" min-width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status === true ? 'success' : 'info'">
                  {{ scope.row.status === true ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="department" label="所属部门" min-width="120">
              <template #default="scope">
                {{ scope.row.department?.name || '--' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" min-width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.createAt) }}

              </template>
            </el-table-column>
            <el-table-column prop="lastLoginTime" label="最后登录时间" min-width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.lastLoginTime) }}
              </template>
            </el-table-column>
            <el-table-column v-permission="['system:user:edit','system:user:info']" label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button
                    v-permission="['system:user:info']"
                    size="small"
                    type="primary"
                    link
                    @click="handleDetail(row)"
                >
                  详情
                </el-button>
                <el-button
                    v-permission="['system:user:edit']"
                    size="small"
                    type="warning"
                    link
                    @click="handleEdit(row)"
                >
                  修改
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页区域 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 新增用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增用户"
      width="500px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="userForm.departmentName" disabled />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio :value="true">男</el-radio>
            <el-radio :value="false">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="appStore.loading" @click="submitForm">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="用户详情"
      size="30%"
      :destroy-on-close="true"
    >
      <div v-if="detailUser" class="user-detail-container">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ detailUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detailUser.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <el-tag :type="detailUser.gender === true ? 'primary' : 'danger'">
              {{ detailUser.gender === true ? '男' : '女' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailUser.status === true ? 'success' : 'info'">
              {{ detailUser.status === true ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ detailUser.departmentStr || detailUser.department?.name || '--' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(detailUser.createAt) }}</el-descriptions-item>
          <el-descriptions-item label="最后登录时间">{{ formatDateTime(detailUser.lastLoginTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- 编辑用户抽屉 -->
    <el-drawer
      v-model="editDrawerVisible"
      title="编辑用户信息"
      size="40%"
      :destroy-on-close="true"
    >
      <el-form
        v-if="editUserForm"
        ref="editUserFormRef"
        :model="editUserForm"
        :rules="editRules"
        label-width="100px"
        class="edit-user-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editUserForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editUserForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editUserForm.gender">
            <el-radio :value="true">男</el-radio>
            <el-radio :value="false">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editUserForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="editUserForm.status"
            :active-value="true"
            :inactive-value="false"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="editUserForm.deptId"
            :data="departmentTree"
            node-key="id"
            :props="{
              label: 'name',
              children: 'children',
              value: 'id'
            }"
            placeholder="请选择部门"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select
            v-model="editUserForm.roleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="editUserForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="flex: auto;">
          <el-button @click="closeEditDrawer">取消</el-button>
          <el-button type="primary" :loading="appStore.loading" @click="submitEditForm">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { searchAdmin, addAdmin, deleteAdmin, getDepartmentTree, edit, getAdminInfoById } from '@/api/system/admin.js'
import { getRoleList } from '@/api/system/role.js'
import { formatDateTime } from '@/utils/dateUtils.js'
import { useAppStore } from '@/store'
const appStore = useAppStore();

// 搜索表单
const searchForm = reactive({
  keyword: '',
  deptId: null // 添加部门ID字段
})

// 表格数据
const tableData = ref([])
const selectedRows = ref([])

// 部门树数据
const departmentTree = ref([])
const departmentTreeRef = ref(null)
const currentDepartment = ref(null)

// 角色列表数据
const roleOptions = ref([])

// 计算表格高度 - 动态计算可用高度
const tableHeight = computed(() => {
  // 视口高度减去其他区域的高度（搜索区、工具栏、分页区、边距等）
  return 'calc(100vh - 220px)';
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 新增用户表单
const userForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  gender: true, // 默认为男性
  password: '',
  confirmPassword: '',
  deptId: null,
  departmentName: ''
})

// 新增用户表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (userForm.confirmPassword !== '') {
      userFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== userForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (value === '') {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入有效的手机号'))
  } else {
    callback()
  }
}

const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 新增用户对话框
const dialogVisible = ref(false)

// 用户详情抽屉
const detailDrawerVisible = ref(false)
const detailUser = ref(null)

// 编辑用户抽屉
const editDrawerVisible = ref(false)
const editUserForm = ref(null)
const editUserFormRef = ref(null)

// 表单验证规则
const editRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  roleIds: [
    { type: 'array', required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 初始化数据
onMounted(() => {
  fetchDepartmentTree()
  fetchAdminData()
  fetchRoleOptions()
})

// 获取部门树数据
const fetchDepartmentTree = async () => {
  // 这里应该调用获取部门树的API
  const { data } = await getDepartmentTree()
  departmentTree.value = data || []
  
}

// 刷新部门树
const refreshDepartmentTree = () => {
  fetchDepartmentTree()
  ElMessage.success('部门树已刷新')
}

// 处理部门选择
const handleDepartmentSelect = (department) => {
  currentDepartment.value = department
  searchForm.deptId = department.id
  pagination.currentPage = 1 // 重置页码
  fetchAdminData() // 重新获取用户数据
}

// 获取用户数据
const fetchAdminData = async () => {

  // 调用API函数，添加部门ID参数
  const resp = await searchAdmin({
    content: searchForm.keyword,
    deptId: searchForm.deptId,
    currentPage: pagination.currentPage,
    pageSize: pagination.pageSize
  })
  
  // 处理响应数据
  tableData.value = resp.data.list || []
  pagination.total = resp.data.total || 0

}

// 获取角色选项
const fetchRoleOptions = async () => {
  const res = await getRoleList({ content: null })
  roleOptions.value = res.data || []
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  // 保留当前选中的部门ID
  fetchAdminData()
}

// 处理表格选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchAdminData()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchAdminData()
}

// 处理新增用户
const handleAdd = () => {
  // 确保已选择部门
  if (!searchForm.deptId) {
    ElMessage.warning('请先选择部门，再添加用户')
    return
  }
  
  // 设置当前选中的部门ID
  userForm.deptId = searchForm.deptId
  userForm.departmentName = currentDepartment.value ? currentDepartment.value.name : ''
  
  // 打开对话框
  dialogVisible.value = true
}

// 处理编辑用户
const handleEdit = async (row) => {

  const resp = await getAdminInfoById(row.id);
  const { data } = resp;
  try {
    // 获取用户的角色
    const userRoles = data.roles || []
    
    // 创建编辑用户表单数据
    editUserForm.value = {
      id: row.id,
      username: row.username,
      nickname: row.nickname,
      gender: row.gender,
      phone: row.phone,
      status: row.status,
      deptId: row.department?.id || null,
      departmentName: row.department?.name || '',
      roleIds: Array.isArray(userRoles) ? userRoles.map(role => role.id) : [],
      description: row.description || ''
    }
    
    // 打开编辑抽屉
    editDrawerVisible.value = true
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 处理查看用户详情
const handleDetail = (row) => {
  detailUser.value = row
  detailDrawerVisible.value = true
}

// 处理批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 条记录吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    // 提取选中行的ID
    const ids = selectedRows.value.map(row => row.id)
    

    // 调用删除API
    await deleteAdmin(ids)
      
    // 请求成功，显示成功消息
    ElMessage.success(`已删除 ${selectedRows.value.length} 条记录`)
    await fetchAdminData()
  }).catch(() => {
    // 取消删除操作，不做任何处理
  })
}

// 处理导出数据
const handleExport = () => {
  ElMessage.success('点击了导出数据按钮')
  // 这里应该调用导出数据的API或功能
}

// 处理新增用户表单提交
const userFormRef = ref(null)
const submitForm = async () => {
  if (!userFormRef.value) return
  
  try {
    // 表单验证
    await userFormRef.value.validate()
    

    // 构建提交数据
    const submitData = {
      username: userForm.username,
      nickname: userForm.nickname,
      phone: userForm.phone,
      gender: userForm.gender,
      password: userForm.password,
      deptId: userForm.deptId
    }
    
    // 调用API
    const res = await addAdmin(submitData)
    
    // 请求成功，显示成功消息
    ElMessage.success('用户创建成功')
    dialogVisible.value = false
    resetForm()
    fetchAdminData()
  } catch (error) {
    // 错误信息已在request.js中处理
    console.error('创建用户失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  
  // 保存当前部门信息
  const deptId = userForm.deptId
  const departmentName = userForm.departmentName
  
  // 手动重置表单数据
  Object.keys(userForm).forEach(key => {
    userForm[key] = key === 'gender' ? true : ''
  })
  
  // 恢复部门信息
  userForm.deptId = deptId
  userForm.departmentName = departmentName
}

// 监听对话框关闭事件，重置表单
const handleDialogClosed = () => {
  resetForm()
}

// 格式化时间


// 提交编辑表单
const submitEditForm = async () => {
  if (!editUserFormRef.value) return
    // 表单验证
  editUserFormRef.value.validate().then(async (valid) => {
    if (valid) {
      try {
        // 构建提交数据
        const submitData = {
          aid: editUserForm.value.id,
          username: editUserForm.value.username,
          nickname: editUserForm.value.nickname,
          gender: editUserForm.value.gender,
          phone: editUserForm.value.phone,
          status: editUserForm.value.status,
          deptId: editUserForm.value.deptId,
          roleIds: editUserForm.value.roleIds || [],
          description: editUserForm.value.description || ''
        }

        console.log("submitData",  submitData)
        // 调用API
        await edit(submitData)
  
        // 请求成功，显示成功消息
        ElMessage.success('更新用户信息成功')
        editDrawerVisible.value = false
        await fetchAdminData();
      } catch (error) {
        console.error('编辑用户失败:', error)
      }
    }
  })
}

// 关闭编辑抽屉
const closeEditDrawer = () => {
  editDrawerVisible.value = false
  editUserForm.value = null
  if (editUserFormRef.value) {
    editUserFormRef.value.resetFields()
  }
}

// 处理部门变化
const handleDeptChange = () => {
  // 这里应该处理部门变化后的逻辑
}

// 处理角色变化
const handleRoleChange = () => {
  // 这里应该处理角色变化后的逻辑
}
</script>

<style scoped>
.user-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.user-detail-container {
  padding: 20px;
}

:deep(.el-descriptions__label) {
  width: 100px;
  padding-right: 20px;
}

:deep(.el-descriptions__content) {
  padding-right: 20px;
}

.edit-user-form {
  padding: 20px;
}

:deep(.el-drawer__body) {
  padding: 0;
  overflow-y: auto;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-drawer__footer) {
  padding: 10px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
}
</style> 