<template>
    <!-- 搜索区域 -->
  <div style="border: 1px dashed #ccc; padding: 10px; margin-bottom: 20px;">
    <el-form :model="searchForm" inline>
      <el-form-item label="药品名称">
        <el-input v-model="searchForm.name" placeholder="请输入药品名称" clearable style="width: 240px;" />
      </el-form-item>
      <el-form-item label="药品分类">
        <el-tree-select v-model="searchForm.categoryId" :data="categoryOptions" :props="{ label: 'name', value: 'categoryId', children: 'children' }" placeholder="请选择分类" check-strictly clearable :render-after-expand="false" style="width: 240px;"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetSearch">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </div>
  <!-- 表格工具栏 -->
  <div style="display: flex; gap: 10px; margin-bottom: 20px;">
    <el-button type="primary" @click="handleAdd" v-permission="['drug:info:add']">
      <el-icon><Plus /></el-icon>
      新增药品
    </el-button>
    <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete" v-permission="['drug:info:delete']">
      <el-icon><Delete /></el-icon>
      批量删除
    </el-button>
  </div>
  <!-- 药品列表表格 -->
    <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border stripe style="width: 100%; height:65vh;"
    >
      <el-table-column type="selection" width="55" fixed />
      <el-table-column prop="drugId" label="ID" width="80" fixed />
      <el-table-column prop="name" label="药品名称" min-width="160" fixed />
      <el-table-column prop="genericName" label="通用名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" min-width="120" />
      <el-table-column prop="brand" label="品牌" min-width="120" />
      <el-table-column prop="manufacturer" label="生产厂家" min-width="180" show-overflow-tooltip />
      <el-table-column prop="dosageForm" label="剂型" min-width="100" />
      <el-table-column prop="strength" label="规格" min-width="120" />
      <el-table-column prop="price" label="价格" min-width="100">
        <template #default="scope">
          ¥{{ scope.row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="库存" min-width="100" />
      <el-table-column prop="prescription" label="处方药" min-width="90">
        <template #default="scope">
          <el-tag :type="scope.row.prescription === 1 ? 'danger' : 'success'">
            {{ scope.row.prescription === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="scope">
          <div class="operation-buttons">
            <el-button type="primary" size="small" @click="handleView(scope.row)" v-permission="['drug:info:view']"
            >
              查看
            </el-button>
            <el-button type="warning" size="small" @click="handleEdit(scope.row)" v-permission="['drug:info:edit']"
            >
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)" v-permission="['drug:info:delete']"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无药品数据" />
      </template>
    </el-table>
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange"
      />
    </div>
  <!-- 药品表单对话框 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false"
  >
    <el-form ref="drugFormRef" :model="drugForm" :rules="formRules" label-width="120px"
    >
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="药品名称" prop="name">
                <el-input v-model="drugForm.name" placeholder="请输入药品名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="通用名" prop="genericName">
                <el-input v-model="drugForm.genericName" placeholder="请输入通用名" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="药品分类" prop="categoryId">
                <el-tree-select v-model="drugForm.categoryId" :data="categoryOptions" :props="{ label: 'name', value: 'categoryId', children: 'children' }" placeholder="请选择分类" check-strictly clearable :render-after-expand="false" style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品牌" prop="brand">
                <el-input v-model="drugForm.brand" placeholder="请输入品牌名称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="剂型" prop="dosageForm">
                <el-input v-model="drugForm.dosageForm" placeholder="请输入剂型" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="规格" prop="strength">
                <el-input v-model="drugForm.strength" placeholder="请输入规格" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="生产厂家" prop="manufacturer">
                <el-input v-model="drugForm.manufacturer" placeholder="请输入生产厂家" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="是否处方药" prop="prescription">
                <el-radio-group v-model="drugForm.prescription">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="价格与库存">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="售价" prop="price">
                <el-input-number v-model="drugForm.price" :precision="2" :min="0" :step="0.01" style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="折扣价" prop="discountPrice">
                <el-input-number v-model="drugForm.discountPrice" :precision="2" :min="0" :step="0.01" style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="库存数量" prop="stockQuantity">
                <el-input-number v-model="drugForm.stockQuantity" :min="0" :step="1" style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="条形码" prop="barCode">
                <el-input v-model="drugForm.barCode" placeholder="请输入条形码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="SKU编号" prop="sku">
                <el-input v-model="drugForm.sku" placeholder="请输入SKU编号" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="储存信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="保质期" prop="shelfLife">
                <el-input-number v-model="drugForm.shelfLife" :min="0" :step="1" style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保质期单位" prop="shelfLifeUnit">
                <el-select v-model="drugForm.shelfLifeUnit" placeholder="请选择单位" style="width: 100%">
                  <el-option :value="1" label="天" />
                  <el-option :value="2" label="月" />
                  <el-option :value="3" label="年" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="储存说明" prop="storageInstructions">
            <el-input v-model="drugForm.storageInstructions" type="textarea" placeholder="请输入储存说明" :rows="3"
            />
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="其他信息">
          <el-form-item label="药品描述" prop="description">
            <el-input v-model="drugForm.description" type="textarea" placeholder="请输入药品描述" :rows="5"
            />
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :disabled="viewMode">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import {getDrugList, addDrug, updateDrug, deleteDrug, getDrugById, getDrugPage} from '@/api/drugs/drug'
import { getCategoryTree } from '@/api/drugs/category'
import { formatDateTime } from '@/utils/dateUtils'
import { useAppStore } from "@/store"

const appStore = useAppStore()
const loading = ref(false)
const tableData = ref([])
const categoryOptions = ref([])
const selectedRows = ref([])
const viewMode = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: null,
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add', 'edit', 'view'
const dialogTitle = computed(() => {
  const titleMap = {
    'add': '新增药品',
    'edit': '编辑药品',
    'view': '查看药品详情'
  }
  return titleMap[dialogType.value]
})
const drugFormRef = ref(null)

// 表单数据
const drugForm = reactive({
  drugId: null,
  name: '',
  genericName: '',
  categoryId: null,
  brand: '',
  manufacturer: '',
  description: '',
  dosageForm: '',
  strength: '',
  price: 0,
  discountPrice: null,
  stockQuantity: 0,
  sku: '',
  barCode: '',
  prescription: 0,
  shelfLife: null,
  shelfLifeUnit: null,
  storageInstructions: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入药品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择药品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' }
  ],
  stockQuantity: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
}

// 初始化数据
onMounted(() => {
  fetchDrugList()
  fetchCategoryOptions()
})

// 获取药品列表
const fetchDrugList = async () => {
  loading.value = true
  try {
    const params = {
      content: searchForm.name,
      categoryId: searchForm.categoryId,
      pageSize: pagination.pageSize,
      currentPage: pagination.currentPage
    }
    
    const res = await getDrugPage(params);
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

// 获取分类选项
const fetchCategoryOptions = async () => {
  const res = await getCategoryTree()
  categoryOptions.value = res.data || []
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchDrugList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = null
  })
  pagination.currentPage = 1
  fetchDrugList()
}

// 处理表格选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchDrugList()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchDrugList()
}

// 处理新增药品
const handleAdd = () => {
  dialogType.value = 'add'
  viewMode.value = false
  resetForm()
  dialogVisible.value = true
}

// 处理查看药品详情
const handleView = async (row) => {
  dialogType.value = 'view'
  viewMode.value = true
  resetForm()
  
  // 获取药品详情
  const res = await getDrugById(row.drugId)
  
  // 填充表单
  Object.assign(drugForm, res.data)
  
  dialogVisible.value = true
}

// 处理编辑药品
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  viewMode.value = false
  resetForm()
  
  // 获取药品详情
  const res = await getDrugById(row.drugId)
  
  // 填充表单
  Object.assign(drugForm, res.data)
  
  dialogVisible.value = true
}

// 处理删除药品
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除药品"${row.name}"吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await deleteDrug(row.drugId)
    ElMessage.success('删除成功')
    fetchDrugList()
  })
}

// 处理批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一项')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 项吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    const drugIds = selectedRows.value.map(item => item.drugId)
    // 这里需要后端提供批量删除接口，暂时使用循环删除
    for (const drugId of drugIds) {
      await deleteDrug(drugId)
    }
    ElMessage.success('批量删除成功')
    fetchDrugList()
  })
}

// 重置表单
const resetForm = () => {
  if (drugFormRef.value) {
    drugFormRef.value.resetFields()
  }
  
  Object.assign(drugForm, {
    drugId: null,
    name: '',
    genericName: '',
    categoryId: null,
    brand: '',
    manufacturer: '',
    description: '',
    dosageForm: '',
    strength: '',
    price: 0,
    discountPrice: null,
    stockQuantity: 0,
    sku: '',
    barCode: '',
    prescription: 0,
    shelfLife: null,
    shelfLifeUnit: null,
    storageInstructions: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (viewMode.value) return
  if (!drugFormRef.value) return

  await drugFormRef.value.validate()
  
  if (dialogType.value === 'add') {
    await addDrug(drugForm)
    ElMessage.success('添加成功')
  } else {
    await updateDrug(drugForm)
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  fetchDrugList()
}
</script>

<style scoped>
.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.operation-buttons {
  display: flex;
  gap: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 