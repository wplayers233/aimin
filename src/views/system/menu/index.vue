<template>
  <div class="menu-container">
    <div class="menu-header">
      <h2>菜单管理</h2>
      <el-button type="primary" v-permission="['system:menu:add']" @click="handleAdd">新增</el-button>
    </div>
    
    <!-- 菜单树形表格 -->
    <el-table
      ref="tableRef"
      class="menu-table"
      height="calc(100vh - 150px)"
      v-loading="appStore.loading"
      :data="menuList"
      row-key="id"
      border
      lazy
      :load="loadChildren"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      v-permission="['system:menu:list']"
    >
      <!-- 菜单标题 -->
      <el-table-column prop="title" label="菜单标题" min-width="180">
        <template #default="scope">
          <el-icon>
            <component :is="scope.row.icon" />
          </el-icon>
          <span style="margin-left: 5px;">{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      
      <!-- 菜单类型 -->
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag
            size="small"
            :type="getMenuTypeTag(scope.row.permType)"
          >
            {{ scope.row.permType }}
          </el-tag>
        </template>
      </el-table-column>
      
      <!-- 状态 -->
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <template v-if="scope.row && scope.row.status !== undefined">
            <el-icon 
              :color="getStatusValue(scope.row.status) ? '#67C23A' : '#F56C6C'" 
              :size="18"
              class="status-icon"
            >
              <component :is="getStatusValue(scope.row.status) ? 'CircleCheck' : 'CircleClose'" />
            </el-icon>
            <span class="status-text" :class="{ 'status-enabled': getStatusValue(scope.row.status), 'status-disabled': !getStatusValue(scope.row.status) }">
              {{ scope.row.status }}
            </span>
          </template>
          <template v-else>-</template>
        </template>
      </el-table-column>
      
      <!-- 路由地址 -->
      <el-table-column prop="path" label="路由地址" min-width="150" />
      
      <!-- 组件名称 -->
      <el-table-column prop="component" label="组件名称" min-width="180" />
      
      <!-- 权限标识 -->
      <el-table-column prop="permKey" label="权限标识" min-width="150">
        <template #default="scope">
          {{ scope.row.permKey || '-' }}
        </template>
      </el-table-column>
      
      <!-- 隐藏 -->
      <el-table-column prop="hidden" label="隐藏" width="80">
        <template #default="scope">
          <template v-if="scope.row && scope.row.hidden !== undefined">
            <el-icon 
              :color="getHiddenValue(scope.row.hidden) ? '#E6A23C' : '#67C23A'" 
              :size="18"
              class="status-icon"
            >
              <component :is="getHiddenValue(scope.row.hidden) ? 'Hide' : 'View'" />
            </el-icon>
            <span class="status-text" :class="{ 'status-hidden': getHiddenValue(scope.row.hidden), 'status-visible': !getHiddenValue(scope.row.hidden) }">
              {{ getHiddenValue(scope.row.hidden) ? '是' : '否' }}
            </span>
          </template>
          <template v-else>-</template>
        </template>
      </el-table-column>
      
      <!-- 创建时间 -->
      <el-table-column
        label="创建时间"
        min-width="180"
      >
        <template #default="scope">
          {{ formatDateTime(scope.row.createAt) }}
        </template>
      </el-table-column>
      
      <!-- 操作 -->
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <div class="operation-buttons">
            <el-button 
              type="warning" 
              size="small"
              link
              v-permission="['system:menu:edit']"
              @click="handleEdit(scope.row)"
            >
              修改
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              link
              v-permission="['system:menu:delete']"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
            <el-button 
              type="primary"
              link
              size="small"
              v-permission="['system:menu:add']"
              @click="handleAddChild(scope.row)"
              v-if="scope.row.permType !== '按钮'"
            >
              新增
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 菜单编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增菜单' : '编辑菜单'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="menuFormRef"
        :model="menuForm"
        :rules="menuRules"
        label-width="100px"
      >
        <!-- 菜单类型选择 -->
        <el-form-item label="菜单类型" prop="permType">
          <el-radio-group v-model="menuForm.permType" :disabled="dialogType === 'edit'">
            <el-radio value="目录">目录</el-radio>
            <el-radio value="菜单">菜单</el-radio>
            <el-radio value="按钮">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 上级菜单 -->
        <el-form-item label="上级菜单" prop="parentId" v-if="!(dialogType === 'edit' && menuForm.parentId === 0)">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="menuTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择上级菜单"
            clearable
            check-strictly
            node-key="id"
            :default-expanded-keys="[1]"
          />
        </el-form-item>

        <!-- 菜单标题 -->
        <el-form-item label="菜单标题" prop="title">
          <el-input v-model="menuForm.title" placeholder="请输入菜单标题" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 菜单图标 -->
        <el-form-item label="菜单图标" prop="icon" v-if="menuForm.permType !== '按钮'">
          <IconSelect v-model="menuForm.icon"/>
        </el-form-item>

        <!-- 路由地址 -->
        <el-form-item label="路由地址" prop="path" v-if="menuForm.permType !== '按钮'">
          <el-input v-model="menuForm.path" placeholder="请输入路由地址" />
        </el-form-item>

        <!-- 重定向 -->
        <el-form-item label="重定向" prop="redirect" v-if="menuForm.permType === '目录'">
          <el-input v-model="menuForm.redirect" placeholder="请输入重定向地址" />
        </el-form-item>

        <!-- 组件名称 -->
        <el-form-item label="组件名称" prop="component" v-if="menuForm.permType !== '按钮'">
          <el-input v-model="menuForm.component" placeholder="请输入组件名称" />
        </el-form-item>

        <!-- 权限标识 -->
        <el-form-item label="权限标识" prop="permKey">
          <el-input v-model="menuForm.permKey" placeholder="请输入权限标识" maxlength="100" show-word-limit />
        </el-form-item>

        <!-- 是否隐藏 -->
        <el-form-item label="是否隐藏" prop="hidden" v-if="menuForm.permType !== '按钮'">
          <el-switch v-model="menuForm.hidden" />
        </el-form-item>

        <!-- 菜单排序 -->
        <el-form-item label="菜单排序" prop="sort">
          <el-input-number v-model="menuForm.sort" :min="0" :max="999" />
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="menuForm.status"
              active-value="启用"
              inactive-value="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChildrenByPid, update, getMenuTree, del, add } from "@/api/system/menu.js";
import { useAppStore } from "@/store";
const appStore = useAppStore();
import { formatDateTime } from '@/utils/dateUtils.js'
import IconSelect from "@/components/IconSelect/index.vue"

// 菜单列表
const menuList = ref([])
const menuTree = ref([])



// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const menuFormRef = ref(null)
const tableRef = ref(null)

// 表单数据
const menuForm = reactive({
  permType: '目录',
  parentId: null,
  title: '',
  icon: '',
  path: '',
  redirect: '',
  component: '',
  permKey: '',
  hidden: false,
  sort: 0,
  status: '启用',
  children: []
})

// 表单验证规则
const menuRules = {
  permType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入菜单标题', trigger: 'blur' },
    { max: 50, message: '标题长度不能超过50个字符', trigger: 'blur' }
  ],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }],
  component: [{ required: true, message: '请输入组件名称', trigger: 'blur' }],
  permKey: [
    { required: true, message: '请输入权限标识', trigger: 'blur' },
    { max: 100, message: '权限标识长度不能超过100个字符', trigger: 'blur' }
  ],
  sort: [{ required: true, message: '请输入菜单排序', trigger: 'blur' }]
}

// 初始化数据
onMounted(async () => {
  // 模拟加载数据
  await fetchMenuData();
  await fetchMenuTree();
})

const fetchMenuTree = async () => {
  const resp = await getMenuTree()
  // 确保树形数据也按sort字段排序
  menuTree.value = sortMenuData(resp.data)
}

// 获取菜单数据
const fetchMenuData = async (pid) => {
  if (!pid) {
    pid = 0;
  }
  // 模拟异步请求
  const resp = await getChildrenByPid(pid);
  // 对数据按照sort字段进行正序排序
  menuList.value = sortMenuData(resp.data);
}

// 按sort字段对菜单数据进行排序(正序)
const sortMenuData = (data) => {
  if (!data || !Array.isArray(data)) return [];
  
  return [...data].sort((a, b) => {
    // 首先按sort字段排序(正序)
    return (a.sort || 0) - (b.sort || 0);
  });
}

const loadMap = new Map()
const loadChildren = async (row, treeNode,resolve) => {
  loadMap.set(row.id, { row, treeNode, resolve })

  const resp = await getChildrenByPid(row.id);
  // 对加载的子节点数据按照sort字段进行排序
  return resolve(sortMenuData(resp.data));
}
/**
 * 
 * @param child
 * @returns {Promise<void>}
 */
const syncData  = async (child) => {
  const { row, treeNode, resolve } = loadMap.get(child.parentId)
  if (tableRef.value.store.states.lazyTreeNodeMap.value[child.parentId].length > 1) {
    //说明该节点下有多个子节点
    tableRef.value.store.states.lazyTreeNodeMap[child.parentId] = []
  } else {
    //说明该节点只有一个节点
    tableRef.value.store.states.lazyTreeNodeMap.value[child.parentId] = []
  }

  await fetchMenuData()
  await loadChildren(row, treeNode, resolve);
}

// 获取菜单类型标签样式
const getMenuTypeTag = (type) => {
  if(!type){
    return ;
  }
  switch (type) {
    case '目录':
      return 'info'
    case '菜单':
      return 'primary'
    case '按钮':
      return 'success'
    default:
      return 'info'
  }
}

// 处理添加子菜单
const handleAddChild = (parentMenu) => {
  console.log('parentMenu', parentMenu)
  dialogType.value = 'add'
  dialogVisible.value = true
  // 重置表单
  Object.assign(menuForm, {
    permType: '目录',
    parentId: parentMenu.id,
    title: '',
    icon: '',
    path: '',
    redirect: '',
    component: '',
    permKey: '',
    hidden: false,
    sort: 0,
    status: '启用'
  })
}

// 处理状态值在表格显示时的转换
const getStatusValue = (status) => {
  // 如果是字符串类型
  if (typeof status === 'string') {
    return status === '启用';
  }
}

// 处理编辑菜单
const handleEdit = (menu) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  
  // 填充表单数据
  Object.assign(menuForm, {
    id: menu.id, // 确保ID被正确传递
    permType: menu.permType || '目录',
    parentId: menu.parentId,
    title: menu.title,
    icon: menu.icon || '',
    path: menu.path || '',
    redirect: menu.redirect || '',
    component: menu.component || '',
    permKey: menu.permKey || '',
    hidden: !!menu.hidden,
    sort: menu.sort || 0,
    // 处理不同类型的状态值
    status: menu.status
  })
}

// 处理删除菜单
const handleDelete = (menu) => {
  ElMessageBox.confirm(
    `确定要删除菜单 "${menu.title}" 吗？${menu.children && menu.children.length ? '这将同时删除所有子菜单！' : ''}`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    // 调用后端删除接口
    await del(menu.id)
    
    // 刷新数据
    if (!menu.parentId || menu.parentId === 0) {
      // 如果是顶级菜单,刷新整个菜单列表
      await fetchMenuData()
      await fetchMenuTree()
    } else {
      // 如果是子菜单,刷新父菜单的子节点
      await syncData(menu)
    }
    
    ElMessage.success(`已删除菜单: ${menu.title}`)
  }).catch(() => {
    // 用户取消删除
  })
}

// 处理表单提交
const handleSubmit = async () => {
  if (!menuFormRef.value) return
  
  await menuFormRef.value.validate( async (valid) => {
    if (valid) {
      // 准备提交数据
      const submitData = { ...menuForm }
      
      // 根据是否存在id调用不同的API
      if (dialogType.value === 'edit') {
        // 编辑模式下调用更新API
        await update(submitData)
      } else {
        // 新增模式下调用保存API
        await add(submitData)
      }
      
      // 如果是添加顶级菜单(parentId为null或0),直接刷新菜单列表
      if (dialogType.value === 'add' && (!submitData.parentId || submitData.parentId === 0)) {
        await fetchMenuData()
        await fetchMenuTree()
      } else if (submitData.parentId) {
        // 如果有父菜单ID,则同步该父菜单下的数据
        await syncData(submitData)
      }
      
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
      dialogVisible.value = false
    }
  })
}

// 处理隐藏值在表格显示时的转换
const getHiddenValue = (hidden) => {
  // 如果hidden为null或undefined，返回false
  if (hidden === null || hidden === undefined) {
    return false;
  }
  
  // 如果是数值，则1表示隐藏，0表示显示
  if (typeof hidden === 'number') {
    return hidden === 1;
  }
  // 如果是布尔值，则直接返回
  return !!hidden;
}

// 处理添加菜单
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  // 重置表单
  Object.assign(menuForm, {
    permType: '目录',
    parentId: null,
    title: '',
    icon: '',
    path: '',
    redirect: '',
    component: '',
    permKey: '',
    hidden: false,
    sort: 0,
    status: '启用'
  })
}

</script>

<style scoped>
.menu-container {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

/* 状态图标和文本样式 */
.status-icon {
  vertical-align: middle;
  margin-right: 4px;
}

.status-text {
  font-size: 13px;
  vertical-align: middle;
  color: #000000;
}

.status-enabled {
  color: #000000;
}

.status-disabled {
  color: #000000;
}

.status-hidden {
  color: #000000;
}

.status-visible {
  color: #000000;
}

/* 操作按钮容器 */
.operation-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

/* 操作按钮样式 */
.operation-buttons .el-button {
  min-width: 52px;
  margin-left: 0 !important;
  padding: 5px 8px;
  height: 28px;
  font-size: 12px;
}

/* 表格样式覆盖 */
:deep(.el-table .cell) {
  padding: 5px 8px;
}

/* 确保卡片内容区域可以滚动 */
:deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow-y: auto;
  padding: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 