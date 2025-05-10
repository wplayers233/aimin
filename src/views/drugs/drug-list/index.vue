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
            <el-button type="primary" link size="small" @click="handleView(scope.row)" v-permission="['drug:info:view']"
            >
              查看
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(scope.row)" v-permission="['drug:info:edit']"
            >
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row)" v-permission="['drug:info:delete']"
            >
              删除
            </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无药品数据" />
      </template>
    </el-table>
    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
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

        <el-tab-pane label="图片管理">
          <div class="image-upload-container">
            <el-form-item label="药品图片">
              <el-upload
                class="drug-image-upload"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemoveFile"
                :on-change="handleFileChange"
                :auto-upload="false"
                :file-list="uploadFileList"
                multiple
              >
                <template #trigger>
                  <el-button type="primary">选择图片</el-button>
                </template>
              </el-upload>
              <el-dialog v-model="previewVisible" width="100%">
                <img w-full :src="previewUrl" alt="预览图片" style="max-width: 100%; max-height: 80vh; object-fit: contain;" />
              </el-dialog>
            </el-form-item>
            
            <div style="margin-left: 120px; margin-bottom: 20px;">
              <div v-if="uploadFileList.length > 0" class="selected-files-info">
                <p>已选择 {{ uploadFileList.length }} 张图片</p>
                <div class="selected-files-list">
                  <div 
                    v-for="(file, index) in uploadFileList" 
                    :key="index" 
                    class="selected-file-item"
                  >
                    <img :src="getFileUrl(file)" class="selected-file-img" style="width: 60px; height: 60px; object-fit: cover;" />
                    <div class="selected-file-info">
                      <span class="selected-file-name">{{ file.name }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <el-button type="success" @click="submitUpload" :disabled="uploadFileList.length === 0 || viewMode" :loading="uploadLoading">
                上传图片
              </el-button>
            </div>
          </div>

          <div v-if="imageList.length > 0" class="image-list-container">
            <el-divider content-position="left">已上传图片</el-divider>
            <el-table :data="imageList" border style="width: 100%">
              <el-table-column label="主图" width="80">
                <template #default="scope">
                  <el-radio
                    v-model="drugForm.mainImgPath"
                    :label="scope.row.path"
                    :disabled="viewMode"
                    @change="selectMainImage(scope.row.path)"
                  ></el-radio>
                </template>
              </el-table-column>
              <el-table-column prop="originalName" label="图片名称" min-width="120" />
              <el-table-column label="预览图" width="120">
                <template #default="scope">
                  <el-image
                    style="width: 80px; height: 80px; object-fit: cover;"
                    :src="scope.row.externalPath"
                    fit="cover"
                    :preview-src-list="[scope.row.path]"
                    :initial-index="0"
                    preview-teleported
                  />
                </template>
              </el-table-column>
              <el-table-column label="排序" width="150">
                <template #default="scope">
                  <el-input-number
                    v-model="scope.row.sort"
                    :min="1"
                    :max="999"
                    size="small"
                    @change="(val) => handleUpdateSort(scope.row, val)"
                    :disabled="viewMode"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="danger" link size="small" @click="handleRemove(scope.row)" :disabled="viewMode">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :disabled="viewMode || uploadLoading">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import {
  getDrugList, 
  addDrug, 
  updateDrug, 
  deleteDrug, 
  getDrugById,
  getDrugPage,
  getDrugImages,
  deleteDrugImage,
  batchUploadDrugImages
} from '@/api/drugs/drug'
import { getCategoryTree } from '@/api/drugs/category'
import { formatDateTime } from '@/utils/dateUtils'
import { useAppStore } from "@/store"
import { useUserStore } from "@/store"

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
  storageInstructions: '',
  mainImgPath: null, // 仅在前端使用，不传递到后端
  imgList: [] // 存储完整图片信息(path, sort, isMain)
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

// 图片上传相关
const imageList = ref([])
const uploadFileList = ref([])
const mainImageIndex = ref(-1) // 不再用于上传，仅用于已上传图片列表
const previewUrl = ref('')
const previewVisible = ref(false)
const uploadLoading = ref(false)


// 初始化数据
onMounted(() => {
  fetchDrugList()
  fetchCategoryOptions()
})

// 获取药品列表
const fetchDrugList = async () => {
  loading.value = true
    const params = {
      content: searchForm.name || '',
      categoryId: searchForm.categoryId || null,
      pageSize: pagination.pageSize,
      currentPage: pagination.currentPage
    }
    
    const res = await getDrugPage(params);
    if (res && res.data) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
    }

    loading.value = false
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

// 获取药品图片列表
const fetchDrugImages = async (drugId) => {
  if (!drugId) return
  
  const res = await getDrugImages(drugId)
  imageList.value = res.data || []
    
  // 对图片列表按排序号排序
  imageList.value.sort((a, b) => a.sort - b.sort)
  
  // 设置主图
  const mainImage = imageList.value.find(img => img.isMain === true)
  if (mainImage) {
    drugForm.mainImgPath = mainImage.path
  } else {
    // 如果没有主图，将主图ID设置为null，保持单选按钮不选中状态
    drugForm.mainImgPath = null
  }
  
  // 同步更新drugForm.imgList
  drugForm.imgList = imageList.value.map(img => ({
    path: img.path,
    sort: img.sort,
    isMain: img.isMain
  }))
}

// 文件选择变化处理
const handleFileChange = (uploadFile, uploadFiles) => {
  try {
    // 检查文件类型和大小
    if (!uploadFile.raw) {
      console.error('文件对象无效')
      uploadFiles.splice(uploadFiles.indexOf(uploadFile), 1)
      return
    }
    
    const isImage = uploadFile.raw.type && uploadFile.raw.type.startsWith('image/')
    const isLt10M = uploadFile.raw.size && uploadFile.raw.size / 1024 / 1024 < 10
    
    if (!isImage) {
      ElMessage.error('只能上传图片文件!')
      uploadFiles.splice(uploadFiles.indexOf(uploadFile), 1)
      return
    }
    
    if (!isLt10M) {
      ElMessage.error('图片大小不能超过 10MB!')
      uploadFiles.splice(uploadFiles.indexOf(uploadFile), 1)
      return
    }
    
    uploadFileList.value = uploadFiles
    
    // 如果是第一张图片，默认设为主图
    if (uploadFileList.value.length === 1 && mainImageIndex.value === -1) {
      mainImageIndex.value = 0
      uploadFileList.value[0].isMain = true
    }
  } catch (error) {
    console.error('处理文件变化时出错:', error)
    ElMessage.error('处理文件时出错，请重试')
  }
}

// 手动上传图片
const submitUpload = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的图片')
    return
  }
  
  try {
    uploadLoading.value = true
    
    const formData = new FormData()
    
    // 添加所有文件到formData
    uploadFileList.value.forEach((file) => {
      formData.append('files', file.raw)
    })
    
    // 批量上传图片
    const res = await batchUploadDrugImages(formData)
    
    if (res.code === 200) {
      ElMessage.success('图片上传成功')
      
      // 更新显示列表，用于界面展示
      if (res.data && Array.isArray(res.data)) {
        res.data.forEach(img => {
          // 添加到图片显示列表
          imageList.value.push({
            id: img.id,
            originalName: img.originalName,
            path: img.path,
            externalPath: img.externalPath,
            isMain: img.isMain,
            sort: img.sort
          })
          
          // 同时记录图片ID，用于保存药品时一起提交
          if (img.path && !drugForm.imgList.some(i => i.path === img.path)) {
            drugForm.imgList.push({
              path: img.path,
              sort: img.sort,
              isMain: img.isMain
            })
          }
        })
      }
      
      // 清空上传列表
      uploadFileList.value = []
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传图片失败，请重试')
  } finally {
    uploadLoading.value = false
  }
}

// 选择主图
const selectMainImage = (path) => {
  // 设置主图ID
  drugForm.mainImgPath = path;
  
  // 更新图片列表中的主图标记
  imageList.value.forEach(item => {
    item.isMain = item.path === path;
  });
}

// 移除选择的文件
const handleRemoveFile = (uploadFile, uploadFiles) => {
  uploadFileList.value = uploadFiles
}

// 更新排序
const handleUpdateSort = (row, value) => {
  row.sort = value
  // 不立即保存排序，等待用户点击确定按钮统一保存
}

// 删除图片
const handleRemove = (row) => {
  ElMessageBox.confirm(
    '确定要删除该图片吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await deleteDrugImage(row.id)
    
    // 从列表中移除
    imageList.value = imageList.value.filter(item => item.id !== row.id)
    
    // 从药品的imgList中也移除
    drugForm.imgList = drugForm.imgList.filter(i => i.path !== row.path)
    
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 图片预览
const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url || (file.response && file.response.data && file.response.data.url)
  previewVisible.value = true
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
  
  // 获取药品图片
  await fetchDrugImages(row.drugId);
  
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
  
  // 获取药品图片
  await fetchDrugImages(row.drugId);
  
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
    const list = [];
    list.push(row.drugId);
    await deleteDrug(list);
    ElMessage.success('删除成功')
    await fetchDrugList()
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
    await deleteDrug(drugIds)

    ElMessage.success('批量删除成功')
    await fetchDrugList();
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
    storageInstructions: '',
    imgList: [], // 重置imageDetails
    mainImgPath: null // 重置主图ID
  })
  
  // 清空图片列表
  imageList.value = []
  mainImageIndex.value = -1
}

// 提交表单
const submitForm = async () => {
  if (viewMode.value) return
  if (!drugFormRef.value) return

  await drugFormRef.value.validate()
  
  // 将图片详细信息添加到表单数据中
  drugForm.imgList = imageList.value.map(img => ({
    path: img.path,
    sort: img.sort,
    isMain: img.path === drugForm.mainImgPath
  }));

  if (dialogType.value === 'add') {
    // 新增药品
    await addDrug(drugForm)
    ElMessage.success('添加成功')
  } else {
    // 更新药品
    await updateDrug(drugForm)
    ElMessage.success('更新成功')
  }
  
  dialogVisible.value = false
  await fetchDrugList();
}

// 获取文件URL，安全处理
const getFileUrl = (file) => {
  if (file.url) {
    // 如果文件已有URL（如从服务器获取的）
    return file.url
  } else if (file.raw && typeof URL !== 'undefined') {
    // 如果是新上传的文件且浏览器支持URL.createObjectURL
    try {
      return URL.createObjectURL(file.raw)
    } catch (error) {
      console.error('创建对象URL失败:', error)
      return ''
    }
  } else {
    // 返回默认图片
  }
}
</script>