<template>
  <div class="knowledge-page">
    <header class="page-head">
      <div>
        <h2>知识文档</h2>
        <p>上传医学资料后，Agent 会在回答时检索其中片段并标注来源。</p>
      </div>
      <div class="head-stats">
        <span class="aimin-chip">{{ docs.length }} 篇文档</span>
        <span class="aimin-chip">{{ totalChunks }} 个切片</span>
      </div>
    </header>

    <div class="upload-card">
      <h3>知识文档入库</h3>
      <p class="hint">支持 PDF / MD / TXT · 自动按章节切片 + 向量化（bge-small-zh 本地模型）</p>
      <el-upload
        drag
        :auto-upload="false"
        :show-file-list="false"
        accept=".pdf,.md,.txt"
        :on-change="handleFile"
      >
        <el-icon :size="40" class="upload-icon"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到这里，或 <em>点击选择</em></div>
      </el-upload>
      <el-alert v-if="uploading" title="正在入库（切片 + 向量化）…" type="info" :closable="false" class="upload-tip" />
      <el-alert v-else-if="lastResult" :title="lastResult" :type="lastOk ? 'success' : 'error'" :closable="true" class="upload-tip" />
    </div>

    <div class="list-card">
      <div class="card-head">
        <h3>已入库文档</h3>
        <el-button text type="primary" size="small" @click="refresh">刷新</el-button>
      </div>
      <el-table :data="docs" style="width: 100%" empty-text="暂无文档，先上传一份吧">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="chunks" label="切片数" width="100" />
        <el-table-column label="大小" width="110">
          <template #default="{ row }">{{ (row.bytes / 1024).toFixed(1) }} KB</template>
        </el-table-column>
        <el-table-column label="入库时间" width="170">
          <template #default="{ row }">{{ new Date(row.ingestedAt).toLocaleString() }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadDoc, listDocs } from '@/api/agent'

const docs = ref([])
const uploading = ref(false)
const lastResult = ref('')
const lastOk = ref(false)

const totalChunks = computed(() =>
  docs.value.reduce((sum, d) => sum + (Number(d?.chunks) || 0), 0)
)

async function refresh() {
  try {
    const res = await listDocs()
    docs.value = res?.data || res || []
    if (Array.isArray(docs.value) && docs.value.length && typeof docs.value[0] === 'string') docs.value = []
  } catch (e) {
    /* 服务未启动时静默 */
  }
}

async function handleFile(file) {
  uploading.value = true
  lastResult.value = ''
  try {
    const res = await uploadDoc(file.raw)
    const body = res?.data ? res : { code: res?.code ?? 200, data: res, msg: res?.msg }
    if ((body.code ?? 200) === 200) {
      lastOk.value = true
      lastResult.value = `「${body.data.fileName}」入库成功，共 ${body.data.chunks} 个切片`
      ElMessage.success(lastResult.value)
    } else {
      lastOk.value = false
      lastResult.value = body.msg || '入库失败'
      ElMessage.error(lastResult.value)
    }
    await refresh()
  } catch (e) {
    lastOk.value = false
    lastResult.value = '入库失败：' + e.message
    ElMessage.error(lastResult.value)
  } finally {
    uploading.value = false
  }
}

onMounted(refresh)
</script>

<style lang="scss" scoped>
.knowledge-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 2px 2px 0;

  h2 {
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 0.2px;
  }

  p {
    margin-top: 5px;
    font-size: 12.5px;
    color: var(--aimin-text-secondary, #6e6e73);
  }
}

.head-stats {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.upload-card,
.list-card {
  background: var(--aimin-card, #fff);
  border: 1px solid var(--aimin-border, rgba(15, 23, 42, 0.07));
  border-radius: var(--aimin-radius-lg, 18px);
  padding: 20px 22px;
  box-shadow: var(--aimin-shadow-card, 0 4px 24px rgba(0, 0, 0, 0.06));

  h3 {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 700;
  }
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  h3 {
    margin: 0;
  }
}

.hint {
  font-size: 12px;
  color: var(--aimin-text-secondary, #6e6e73);
  margin-bottom: 14px;
}

.upload-tip {
  margin-top: 12px;
}

.upload-card :deep(.el-upload-dragger) {
  border-radius: 14px;
  border-color: rgba(10, 132, 255, 0.28);
  background: linear-gradient(180deg, rgba(10, 132, 255, 0.04), rgba(10, 132, 255, 0.01));
  transition: border-color 0.2s ease, background 0.2s ease;

  &:hover {
    border-color: var(--aimin-primary, #0a84ff);
    background: rgba(10, 132, 255, 0.06);
  }

  .upload-icon {
    color: var(--aimin-primary, #0a84ff);
    margin-bottom: 10px;
  }

  .el-icon--upload {
    color: var(--aimin-primary, #0a84ff);
    font-size: 44px;
    margin-bottom: 10px;
  }

  .el-upload__text {
    color: var(--aimin-text-secondary, #6e6e73);
    font-size: 13px;

    em {
      color: var(--aimin-primary, #0a84ff);
      font-style: normal;
      font-weight: 600;
    }
  }
}

.list-card {
  flex: 1;
  overflow: auto;
}
</style>
