<template>
    <!-- 搜索区域和工具栏 -->
  <div style="margin-bottom: 15px;">
    <el-button type="primary" @click="handleAdd" v-permission="['drug:category:add']">
      <el-icon><Plus /></el-icon>新增分类
    </el-button>
  </div>

  <!-- 分类树形表格 -->
  <el-table
    v-loading="loading"
    :data="categoryList"
    row-key="categoryId"
    border
  
    :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    style="width: 100%; height: calc(100vh - 180px);"
  >
    <el-table-column prop="name" label="分类名称" min-width="180" />
    <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
    <el-table-column prop="parentId" label="上级分类" min-width="120">
      <template #default="scope">
        {{ getParentCategoryName(scope.row.parentId) }}
      </template>
    </el-table-column>
    <el-table-column prop="createAt" label="创建时间" min-width="180">
      <template #default="scope">
        {{ formatDateTime(scope.row.createAt) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="220" fixed="right">
      <template #default="scope">
        <div style="display: flex; gap: 5px;">
          <el-button type="primary" size="small" @click="handleAddChild(scope.row)" v-permission="['drug:category:add']">
            添加子分类
          </el-button>
          <el-button type="warning" size="small" @click="handleEdit(scope.row)" v-permission="['drug:category:edit']">
            修改
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)" v-permission="['drug:category:delete']">
            删除
          </el-button>
        </div>
      </template>
    </el-table-column>
    <template #empty>
      <el-empty description="暂无分类数据" />
    </template>
  </el-table>

    <!-- 分类编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增分类' : '编辑分类'" width="500px" :close-on-click-modal="false">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="formRules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类" prop="parentId" v-if="dialogType === 'add'">
          <el-tree-select v-model="categoryForm.parentId" :data="categoryOptions" :props="{ label: 'name', value: 'categoryId', children: 'children' }" placeholder="请选择上级分类" check-strictly clearable :render-after-expand="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" placeholder="请输入分类描述" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryList, getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/drugs/category'
import { formatDateTime } from '@/utils/dateUtils'
import { useAppStore } from "@/store"

const appStore = useAppStore()
const loading = ref(false)
const categoryList = ref([])
const categoryOptions = ref([])

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const categoryFormRef = ref(null)

// 表单数据
const categoryForm = reactive({
  categoryId: null,
  name: '',
  parentId: null,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    categoryList.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 获取分类选项（用于选择上级分类）
const fetchCategoryOptions = async () => {
  const res = await getCategoryTree()
  // 构建包含"顶级分类"选项的树
  categoryOptions.value = [
    { 
      categoryId: 0, 
      name: '顶级分类', 
      children: res.data || [] 
    }
  ]
}

// 获取父分类名称
const getParentCategoryName = (parentId) => {
  if (!parentId) return '顶级分类'
  
  const findParent = (categories, id) => {
    for (const category of categories) {
      if (category.categoryId === id) {
        return category.name
      }
      if (category.children && category.children.length) {
        const result = findParent(category.children, id)
        if (result) return result
      }
    }
    return null
  }
  
  return findParent(categoryList.value, parentId) || '未知分类'
}

// 初始化
onMounted(() => {
  fetchCategoryList()
  fetchCategoryOptions()
})

// 处理新增分类
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 处理新增子分类
const handleAddChild = (row) => {
  dialogType.value = 'add'
  resetForm()
  categoryForm.parentId = row.categoryId
  dialogVisible.value = true
}

// 处理编辑分类
const handleEdit = (row) => {
  dialogType.value = 'edit'
  resetForm()
  Object.assign(categoryForm, {
    categoryId: row.categoryId,
    name: row.name,
    parentId: row.parentId,
    description: row.description
  })
  dialogVisible.value = true
}

// 处理删除分类
const handleDelete = (row) => {
  // 检查是否有子分类
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下有子分类，不能直接删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除分类"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await deleteCategory(row.categoryId)
    ElMessage.success('删除成功')
    fetchCategoryList()
    fetchCategoryOptions()
  })
}

// 重置表单
const resetForm = () => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  Object.assign(categoryForm, {
    categoryId: null,
    name: '',
    parentId: null,
    description: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return

  await categoryFormRef.value.validate()
  
  if (dialogType.value === 'add') {
    await addCategory(categoryForm)
    ElMessage.success('添加成功')
  } else {
    await updateCategory(categoryForm)
    ElMessage.success('修改成功')
  }
  
  dialogVisible.value = false
  fetchCategoryList()
  fetchCategoryOptions()
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 