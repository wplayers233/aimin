<template>
  <div class="library">
    <span class="eyebrow">Knowledge Base</span>
    <h2 class="serif-title">知识库</h2>
    <p class="lede">
      入库后自动切分、向量化并建立词法索引；同一份文档的切片共享页码与章节信息，便于引用回溯。
    </p>

    <div class="lib-grid">
      <el-upload
        drag
        class="drop"
        :auto-upload="false"
        :show-file-list="false"
        accept=".pdf,.md,.txt"
        :on-change="handleFile"
      >
        <img class="still" :src="stillImg" alt="" />
        <div class="drop-inner">
          <b>拖拽文档到此处</b>
          <span>支持 PDF / Markdown / TXT，单文件不超过 50MB</span>
          <em>SELECT OR DROP</em>
        </div>
      </el-upload>

      <div class="stats">
        <div class="stat-line"><span>文档总数</span><b>{{ docs.length }}</b></div>
        <div class="stat-line"><span>切片总数</span><b>{{ totalChunks }}</b></div>
        <div class="stat-line"><span>向量维度</span><b>512</b></div>
        <div class="stat-line"><span>词法索引</span><b>BM25 · bigram</b></div>
        <div class="stat-line"><span>最近入库</span><b>{{ lastIngest }}</b></div>
        <div v-if="uploading" class="note">正在入库（切片 + 向量化）…</div>
        <div v-else-if="lastResult" class="note" :class="{ err: !lastOk }">{{ lastResult }}</div>
      </div>
    </div>

    <div class="rule"></div>

    <div class="panel-head">
      <h3>全部文档</h3>
      <button class="refresh" @click="refresh">刷新</button>
    </div>

    <table class="lib-table">
      <thead>
        <tr>
          <th>名称</th>
          <th>切片</th>
          <th>大小</th>
          <th>入库</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in docs" :key="d.docId || d.fileName">
          <td>{{ d.fileName }}</td>
          <td class="num">{{ d.chunks }}</td>
          <td class="num">{{ (Number(d.bytes || 0) / 1024).toFixed(1) }} KB</td>
          <td class="num">{{ formatTime(d.ingestedAt) }}</td>
        </tr>
        <tr v-if="!docs.length">
          <td colspan="4" class="empty">暂无文档，先上传一份</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadDoc, listDocs } from '@/api/agent'
import stillImg from '@/assets/watercolor/processed/still.webp'

const docs = ref([])
const uploading = ref(false)
const lastResult = ref('')
const lastOk = ref(false)

const totalChunks = computed(() =>
  docs.value.reduce((sum, d) => sum + (Number(d?.chunks) || 0), 0)
)

const lastIngest = computed(() => {
  const ts = docs.value[0]?.ingestedAt
  return ts ? formatTime(ts) : '—'
})

function formatTime(ts) {
  const n = Number(ts)
  if (!n) return '—'
  const d = new Date(n)
  const p = (v) => String(v).padStart(2, '0')
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

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

<style scoped lang="scss">
.library {
  display: flex;
  flex-direction: column;

  .lede {
    margin-top: 6px;
    color: var(--ink-2);
    font-size: 13.5px;
    max-width: 760px;
  }
}

.lib-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 34px;
  margin-top: 26px;
}

.drop {
  position: relative;

  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    height: 196px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    position: relative;
    overflow: hidden;
  }

  .still {
    position: absolute;
    right: -24px;
    bottom: -40px;
    width: 190px;
    opacity: 0.55;
    mix-blend-mode: multiply;
    pointer-events: none;
  }

  .drop-inner {
    position: relative;
    text-align: center;

    b {
      display: block;
      font-family: var(--serif);
      font-size: 16px;
      font-weight: 400;
      margin-bottom: 4px;
    }

    span {
      font-size: 12px;
      color: var(--ink-3);
    }

    em {
      display: block;
      margin-top: 12px;
      font-style: normal;
      font-family: var(--mono);
      font-size: 10px;
      letter-spacing: 0.14em;
      color: var(--ochre);
    }
  }
}

.stats {
  .stat-line {
    display: flex;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid var(--line-2);
    font-size: 12.5px;

    span {
      color: var(--ink-2);
    }

    b {
      font-family: var(--mono);
      font-weight: 400;
      color: var(--ink);
    }
  }

  .note {
    margin-top: 14px;
    font-size: 12px;
    color: var(--sage);

    &.err {
      color: var(--ochre);
    }
  }
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;

  h3 {
    font-family: var(--serif);
    font-size: 17px;
    font-weight: 400;
  }

  .refresh {
    font-family: var(--mono);
    font-size: 10.5px;
    letter-spacing: 0.12em;
    color: var(--ink-3);
    border-bottom: 1px solid transparent;
    transition: color 0.2s, border-color 0.2s;

    &:hover {
      color: var(--ink);
      border-bottom-color: var(--ink);
    }
  }
}

.lib-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12.5px;

  th {
    text-align: left;
    font-family: var(--mono);
    font-weight: 400;
    font-size: 10px;
    letter-spacing: 0.14em;
    color: var(--ink-3);
    padding: 0 12px 8px 0;
    border-bottom: 1px solid var(--line);
  }

  td {
    padding: 11px 12px 11px 0;
    border-bottom: 1px solid var(--line-2);
    color: var(--ink-2);

    &:first-child {
      color: var(--ink);
    }
  }

  .empty {
    text-align: center;
    color: var(--ink-3);
    padding: 26px 0;
  }
}

@media (max-width: 1000px) {
  .lib-grid {
    grid-template-columns: 1fr;
  }
}
</style>
