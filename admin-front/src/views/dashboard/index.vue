<template>
  <div class="dashboard">
    <!-- 欢迎区 -->
    <section class="hero">
      <div class="hero-copy">
        <span class="hero-badge">
          <el-icon><FirstAidKit /></el-icon>
          Aimin 医疗诊断 Agent
        </span>
        <h1>{{ greeting }}，{{ displayName }}</h1>
        <p>
          基于医学知识库与药品库的检索增强问答：回答带可追溯证据，高风险用药问题会先请你确认，
          所有内容仅用于辅助参考。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/agent')">
            <el-icon><ChatDotRound /></el-icon>
            开始一次问诊
          </el-button>
          <el-button size="large" class="ghost-btn" @click="router.push('/knowledge')">
            <el-icon><FolderOpened /></el-icon>
            管理知识库
          </el-button>
        </div>
      </div>

      <div class="hero-art" aria-hidden="true">
        <span class="ring ring-1"></span>
        <span class="ring ring-2"></span>
        <span class="cross">
          <span class="cross-v"></span>
          <span class="cross-h"></span>
        </span>
        <span class="pulse">
          <svg viewBox="0 0 220 40" preserveAspectRatio="none">
            <path
              d="M0 24 H56 l10 -16 l12 30 l10 -22 l9 8 H220"
              fill="none"
              stroke="rgba(255,255,255,.75)"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </span>
      </div>
    </section>

    <!-- 数据快照 -->
    <section class="stat-row">
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon><Document /></el-icon></div>
        <div class="stat-body">
          <span class="stat-value">{{ docsLoaded ? docCount : '—' }}</span>
          <span class="stat-label">已入库文档</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon teal"><el-icon><Files /></el-icon></div>
        <div class="stat-body">
          <span class="stat-value">{{ docsLoaded ? chunkCount : '—' }}</span>
          <span class="stat-label">知识切片</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon amber"><el-icon><WarnTriangleFilled /></el-icon></div>
        <div class="stat-body">
          <span class="stat-value">两阶段</span>
          <span class="stat-label">高风险问题确认</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><el-icon><CircleCheckFilled /></el-icon></div>
        <div class="stat-body">
          <span class="stat-value">5 条</span>
          <span class="stat-label">检索证据上限</span>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="entry-grid">
      <button class="entry-card" type="button" @click="router.push('/agent')">
        <div class="entry-icon"><el-icon :size="22"><ChatDotRound /></el-icon></div>
        <div class="entry-text">
          <h3>智能问诊</h3>
          <p>描述症状、药品或诊疗问题，获得带证据引用的回答</p>
        </div>
        <el-icon class="entry-arrow"><ArrowRight /></el-icon>
      </button>

      <button class="entry-card" type="button" @click="router.push('/knowledge')">
        <div class="entry-icon teal"><el-icon :size="22"><FolderOpened /></el-icon></div>
        <div class="entry-text">
          <h3>知识库</h3>
          <p>上传 PDF / Markdown / TXT，自动切片并写入向量库</p>
        </div>
        <el-icon class="entry-arrow"><ArrowRight /></el-icon>
      </button>
    </section>

    <!-- 底部说明 -->
    <section class="tips-card">
      <div class="tips-head">
        <h3>使用小贴士</h3>
        <span class="aimin-chip">安全策略已启用</span>
      </div>
      <ul class="tips-list">
        <li>
          <span class="tip-index">1</span>
          <div>
            <strong>证据优先</strong>
            <p>回答中的 <sup>[1]</sup> 角标对应检索到的药品或文档片段，可在证据卡中回溯原文。</p>
          </div>
        </li>
        <li>
          <span class="tip-index">2</span>
          <div>
            <strong>风险确认</strong>
            <p>涉及剂量、处方、停药、联合用药等问题时，系统会先中断生成，确认后才继续，避免直接给出处方级建议。</p>
          </div>
        </li>
        <li>
          <span class="tip-index">3</span>
          <div>
            <strong>不替代医生</strong>
            <p>工作台输出仅供医学信息参考，不能替代执业医师的诊断与处方，紧急情况请立即就医。</p>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { listDocs } from '@/api/agent'

const router = useRouter()
const userStore = useUserStore()

const docs = ref([])
const docsLoaded = ref(false)

const displayName = computed(() => userStore.nickname || '管理员')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const docCount = computed(() => docs.value.length)
const chunkCount = computed(() =>
  docs.value.reduce((sum, d) => sum + (Number(d?.chunks) || 0), 0)
)

onMounted(async () => {
  try {
    const res = await listDocs()
    const list = res?.data ?? res
    if (Array.isArray(list)) docs.value = list
  } catch (e) {
    // 后端未启动时保持占位符，不打扰用户
  } finally {
    docsLoaded.value = true
  }
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

// ---------------- Hero ----------------
.hero {
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 34px 38px;
  border-radius: var(--aimin-radius-xl);
  background: var(--aimin-gradient-hero);
  box-shadow: 0 24px 50px -26px rgba(10, 100, 220, 0.65);
  color: #fff;
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 620px;

  h1 {
    margin: 14px 0 10px;
    font-size: 30px;
    font-weight: 700;
    letter-spacing: 0.3px;
  }

  p {
    font-size: 13.5px;
    line-height: 1.75;
    color: rgba(255, 255, 255, 0.82);
  }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.4px;
  backdrop-filter: blur(6px);
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;

  :deep(.el-button--primary) {
    background: #fff;
    border-color: #fff;
    color: #0a6ce0;
    box-shadow: 0 10px 24px -12px rgba(0, 0, 0, 0.55);
    font-weight: 600;

    &:hover {
      background: #f2f8ff;
      border-color: #f2f8ff;
      color: #0a6ce0;
    }
  }
}

.ghost-btn {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: #fff;
  backdrop-filter: blur(6px);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.5);
    color: #fff;
  }
}

.hero-art {
  position: relative;
  width: 280px;
  flex-shrink: 0;
  align-self: center;

  .ring {
    position: absolute;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.28);
  }

  .ring-1 {
    width: 190px;
    height: 190px;
    right: 18px;
    top: -58px;
  }

  .ring-2 {
    width: 120px;
    height: 120px;
    right: 52px;
    top: -12px;
    border-style: dashed;
    border-color: rgba(255, 255, 255, 0.22);
  }

  .cross {
    position: absolute;
    right: 68px;
    top: 12px;
    width: 62px;
    height: 62px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.16);
    border: 1px solid rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(4px);

    .cross-v,
    .cross-h {
      position: absolute;
      left: 50%;
      top: 50%;
      background: #fff;
      border-radius: 3px;
      transform: translate(-50%, -50%);
    }

    .cross-v {
      width: 10px;
      height: 30px;
    }

    .cross-h {
      width: 30px;
      height: 10px;
    }
  }

  .pulse {
    position: absolute;
    right: 0;
    bottom: -6px;
    width: 230px;
    opacity: 0.9;
  }
}

// ---------------- 数据卡 ----------------
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--aimin-card);
  border: 1px solid var(--aimin-border);
  border-radius: var(--aimin-radius-lg);
  box-shadow: var(--aimin-shadow-card);
  transition: box-shadow 0.2s ease, transform 0.2s var(--aimin-ease);

  &:hover {
    box-shadow: var(--aimin-shadow-hover);
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;

  &.blue {
    background: rgba(10, 132, 255, 0.1);
    color: #0a84ff;
  }
  &.teal {
    background: rgba(46, 156, 184, 0.12);
    color: #2e9cb8;
  }
  &.amber {
    background: rgba(247, 144, 9, 0.12);
    color: #e58200;
  }
  &.green {
    background: rgba(18, 183, 106, 0.12);
    color: #12b76a;
  }
}

.stat-body {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.stat-label {
  margin-top: 3px;
  font-size: 12px;
  color: var(--aimin-text-tertiary);
}

// ---------------- 快捷入口 ----------------
.entry-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.entry-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  text-align: left;
  border: 1px solid var(--aimin-border);
  border-radius: var(--aimin-radius-lg);
  background: var(--aimin-card);
  box-shadow: var(--aimin-shadow-card);
  cursor: pointer;
  transition: box-shadow 0.22s ease, transform 0.22s var(--aimin-ease),
    border-color 0.22s ease;

  &:hover {
    box-shadow: var(--aimin-shadow-hover);
    border-color: rgba(10, 132, 255, 0.28);
    transform: translateY(-2px);

    .entry-arrow {
      color: var(--aimin-accent);
      transform: translateX(3px);
    }
  }
}

.entry-icon {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 132, 255, 0.1);
  color: var(--aimin-accent);

  &.teal {
    background: rgba(46, 156, 184, 0.12);
    color: #2e9cb8;
  }
}

.entry-text {
  flex: 1;
  min-width: 0;

  h3 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 4px;
  }

  p {
    font-size: 12.5px;
    color: var(--aimin-text-secondary);
    line-height: 1.6;
  }
}

.entry-arrow {
  color: var(--aimin-text-tertiary);
  transition: color 0.2s ease, transform 0.2s var(--aimin-ease);
}

// ---------------- 小贴士 ----------------
.tips-card {
  padding: 22px 24px 8px;
  background: var(--aimin-card);
  border: 1px solid var(--aimin-border);
  border-radius: var(--aimin-radius-lg);
  box-shadow: var(--aimin-shadow-card);
}

.tips-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  h3 {
    font-size: 15px;
    font-weight: 700;
  }
}

.tips-list {
  list-style: none;
  display: flex;
  flex-direction: column;

  li {
    display: flex;
    gap: 12px;
    padding: 13px 0;
    border-top: 1px solid var(--aimin-border);

    strong {
      display: block;
      font-size: 13px;
      margin-bottom: 3px;
    }

    p {
      font-size: 12.5px;
      line-height: 1.65;
      color: var(--aimin-text-secondary);
    }

    sup {
      color: var(--aimin-accent);
      font-weight: 700;
    }
  }
}

.tip-index {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  margin-top: 1px;
  border-radius: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(10, 132, 255, 0.1);
  color: var(--aimin-accent);
  font-size: 11px;
  font-weight: 700;
}

@media (max-width: 1180px) {
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-art {
    display: none;
  }
}

@media (max-width: 760px) {
  .entry-grid {
    grid-template-columns: 1fr;
  }
}
</style>
