<template>
  <div class="app-container">
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" type="card">
      <!-- 轮播图管理 -->
      <el-tab-pane label="轮播图管理" name="banner">
        <div class="banner-header">
          <el-button type="primary" @click="handleAddBanner">
            <el-icon><Plus /></el-icon> 新增轮播图
          </el-button>
        </div>
        
        <!-- 轮播图列表 -->
        <el-table v-loading="tableLoading" :data="bannerList" border style="width: 100%; margin-top: 15px">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="图片预览" width="150">
            <template #default="scope">
              <el-image
                style="width: 120px; height: 60px; object-fit: cover;"
                :src="scope.row.imageUrl"
                fit="cover"
                :preview-src-list="[scope.row.imageUrl]"
                preview-teleported
              />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="180" />
          <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
          <el-table-column prop="link" label="链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '显示' : '隐藏' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button type="primary" link @click="handleEditBanner(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" link @click="handleDeleteBanner(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
      
      <!-- 其他功能选项卡 -->
      <el-tab-pane label="功能2" name="feature2">
        <div>功能2内容，待开发</div>
      </el-tab-pane>
      
      <el-tab-pane label="功能3" name="feature3">
        <div>功能3内容，待开发</div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 轮播图表单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'" 
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="bannerFormRef" 
        :model="bannerForm" 
        :rules="bannerRules" 
        label-width="80px"
      >
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="banner-upload"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :file-list="uploadFileList"
            :limit="1"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div v-if="bannerForm.imageUrl" class="image-preview">
            <el-image 
              style="width: 200px; height: 100px" 
              :src="bannerForm.imageUrl" 
              fit="cover"
            />
          </div>
          <el-button 
            v-if="uploadFileList.length > 0" 
            type="primary" 
            @click="uploadImage" 
            :loading="uploadLoading"
          >
            上传图片
          </el-button>
        </el-form-item>
        
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="说明" prop="description">
          <el-input v-model="bannerForm.description" type="textarea" placeholder="请输入说明" :rows="3" />
        </el-form-item>
        
        <el-form-item label="链接" prop="link">
          <el-input v-model="bannerForm.link" placeholder="请输入链接地址" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="bannerForm.sort" :min="1" :max="999" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="bannerForm.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBannerForm" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 当前活跃选项卡
const activeTab = ref('banner')

// 表格数据
const bannerList = ref([])
const tableLoading = ref(false)

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const bannerFormRef = ref(null)
const submitLoading = ref(false)

// 文件上传相关
const uploadFileList = ref([])
const uploadLoading = ref(false)

// 轮播图表单
const bannerForm = reactive({
  id: null,
  imageUrl: '',
  title: '',
  description: '',
  link: '',
  sort: 1,
  status: 1
})

// 表单验证规则
const bannerRules = {
  imageUrl: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  link: [
    { required: false, message: '请输入链接地址', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 初始化数据
onMounted(() => {
  fetchBannerList()
})

// 获取轮播图列表
const fetchBannerList = async () => {
  tableLoading.value = true
  try {
    // 这里需要替换为实际的API调用
    // const res = await getBannerList({
    //   page: pagination.currentPage,
    //   pageSize: pagination.pageSize
    // })
    
    // 模拟数据，实际开发中需要替换为API调用
    setTimeout(() => {
      bannerList.value = [
        {
          id: 1,
          imageUrl: 'https://placeholder.pics/svg/300x150/DEDEDE/555555/Banner%201',
          title: '促销活动',
          description: '夏季药品促销活动',
          link: '/promotion/summer',
          sort: 1,
          status: 1
        },
        {
          id: 2,
          imageUrl: 'https://placeholder.pics/svg/300x150/DEDEDE/555555/Banner%202',
          title: '新品上线',
          description: '新品药品上线通知',
          link: '/new-products',
          sort: 2,
          status: 1
        },
        {
          id: 3,
          imageUrl: 'https://placeholder.pics/svg/300x150/DEDEDE/555555/Banner%203',
          title: '健康知识',
          description: '健康知识专栏推广',
          link: '/health-tips',
          sort: 3,
          status: 0
        }
      ]
      pagination.total = 3
      tableLoading.value = false
    }, 500)
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
    ElMessage.error('获取轮播图列表失败')
    tableLoading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchBannerList()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchBannerList()
}

// 重置表单
const resetForm = () => {
  if (bannerFormRef.value) {
    bannerFormRef.value.resetFields()
  }
  
  Object.assign(bannerForm, {
    id: null,
    imageUrl: '',
    title: '',
    description: '',
    link: '',
    sort: 1,
    status: 1
  })
  
  uploadFileList.value = []
}

// 新增轮播图
const handleAddBanner = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑轮播图
const handleEditBanner = (row) => {
  dialogType.value = 'edit'
  resetForm()
  
  // 填充表单数据
  Object.assign(bannerForm, { ...row })
  
  // 如果有图片，设置显示
  if (row.imageUrl) {
    uploadFileList.value = [
      {
        name: '当前图片',
        url: row.imageUrl
      }
    ]
  }
  
  dialogVisible.value = true
}

// 处理图片上传
const handleImageChange = (file) => {
  // 检查文件类型和大小
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    uploadFileList.value = []
    return
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    uploadFileList.value = []
    return
  }
  
  // 预览本地图片
  const reader = new FileReader()
  reader.onload = (e) => {
    bannerForm.imageUrl = e.target.result // 临时显示，实际需要上传后获取URL
  }
  reader.readAsDataURL(file.raw)
}

// 上传图片
const uploadImage = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请选择要上传的图片')
    return
  }
  
  uploadLoading.value = true
  
  try {
    // 这里需要替换为实际的上传API
    // const formData = new FormData()
    // formData.append('file', uploadFileList.value[0].raw)
    // const res = await uploadBannerImage(formData)
    // bannerForm.imageUrl = res.data.url
    
    // 模拟上传成功
    setTimeout(() => {
      bannerForm.imageUrl = URL.createObjectURL(uploadFileList.value[0].raw)
      ElMessage.success('图片上传成功')
      uploadLoading.value = false
    }, 1000)
  } catch (error) {
    console.error('上传图片失败:', error)
    ElMessage.error('上传图片失败')
    uploadLoading.value = false
  }
}

// 提交表单
const submitBannerForm = async () => {
  if (!bannerFormRef.value) return
  
  await bannerFormRef.value.validate()
  
  submitLoading.value = true
  
  try {
    if (dialogType.value === 'add') {
      // 新增轮播图
      // const res = await addBanner(bannerForm)
      
      // 模拟添加成功
      setTimeout(() => {
        ElMessage.success('添加轮播图成功')
        dialogVisible.value = false
        fetchBannerList()
        submitLoading.value = false
      }, 500)
    } else {
      // 更新轮播图
      // const res = await updateBanner(bannerForm)
      
      // 模拟更新成功
      setTimeout(() => {
        ElMessage.success('更新轮播图成功')
        dialogVisible.value = false
        fetchBannerList()
        submitLoading.value = false
      }, 500)
    }
  } catch (error) {
    console.error('保存轮播图失败:', error)
    ElMessage.error('保存轮播图失败')
    submitLoading.value = false
  }
}

// 删除轮播图
const handleDeleteBanner = (row) => {
  ElMessageBox.confirm(
    `确定要删除标题为"${row.title}"的轮播图吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用删除API
      // await deleteBanner(row.id)
      
      // 模拟删除成功
      setTimeout(() => {
        ElMessage.success('删除成功')
        fetchBannerList()
      }, 500)
    } catch (error) {
      console.error('删除轮播图失败:', error)
      ElMessage.error('删除轮播图失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.app-container {
  padding: 1px;
}

.banner-header {
  margin-bottom: 20px;
}

.banner-upload .el-upload {
  width: 148px;
  height: 148px;
  line-height: 148px;
}

.image-preview {
  margin-top: 10px;
  margin-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 添加一些基本间距 */
.el-form-item {
  margin-bottom: 22px;
}
</style>