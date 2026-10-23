<template>
  <div class="atlas-login">
    <div class="art">
      <div class="wash" :style="{ backgroundImage: `url(${heroImg})` }"></div>
      <span class="fig">FIG. 01 — VASCULAR ATLAS</span>
      <svg class="ecg" viewBox="0 0 600 60" preserveAspectRatio="none">
        <path
          d="M0 34 L120 34 L138 34 L146 18 L154 46 L162 34 L230 34 L246 34 L254 24 L262 40 L270 34 L360 34 L378 34 L386 12 L394 50 L402 34 L470 34 L486 34 L494 26 L502 38 L510 34 L600 34"
        />
      </svg>
      <div class="caption">
        <p>医学判断的每一步，都应当有据可查。</p>
        <span>CLINICAL ATLAS · 2026</span>
      </div>
    </div>

    <div class="panel">
      <div class="brand">
        <span class="mark">艾</span>
        <div>
          <strong>aimin</strong>
          <em>MEDICAL AGENT WORKBENCH</em>
        </div>
      </div>

      <h1>临床辅助<br />诊断工作台</h1>
      <p class="hint">基于知识库的可追溯问答 · 风险内容二次确认</p>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent>
        <div class="field">
          <label>ACCOUNT</label>
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名" />
          </el-form-item>
        </div>

        <div class="field">
          <label>PASSWORD</label>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
          </el-form-item>
        </div>

        <div class="field">
          <label>VERIFY</label>
          <el-form-item prop="captchaAnswer">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captchaAnswer"
                placeholder="请输入计算结果"
                maxlength="8"
                @keyup.enter="handleLogin"
              />
              <button
                type="button"
                class="cap"
                :disabled="captchaLoading"
                title="点击刷新验证码"
                @click="loadCaptcha"
              >
                {{ captchaLoading ? '加载中…' : captcha.question }}
              </button>
            </div>
          </el-form-item>
        </div>

        <div class="actions">
          <el-button
            class="enter"
            :loading="appStore.loading"
            @click="handleLogin"
          >
            进入工作台
            <svg viewBox="0 0 24 24"><path d="M5 12h14M13 6l6 6-6 6" /></svg>
          </el-button>
          <span class="foot">v1.0 · 演示环境</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { h } from 'vue'

import { useUserStore, useRouteStore, useAppStore } from '@/store'
import { convertToRoutes, addDynamicRoutes } from '@/utils/dynamicRoutes'
import { getCaptcha } from '@/api/auth'
import heroImg from '@/assets/watercolor/processed/hero.webp'

const appStore = useAppStore()
const userStore = useUserStore()
const routeStore = useRouteStore()
const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)
const captchaLoading = ref(false)
const captcha = reactive({ captchaId: '', question: '' })

const loginForm = reactive({
  username: '',
  password: '',
  captchaAnswer: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaAnswer: [{ required: true, message: '请输入计算结果', trigger: 'blur' }]
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captcha.captchaId = res.data?.captchaId || ''
    captcha.question = res.data?.question || '点击刷新'
    loginForm.captchaAnswer = ''
  } catch (e) {
    captcha.captchaId = ''
    captcha.question = '点击重试'
  } finally {
    captchaLoading.value = false
  }
}

onMounted(loadCaptcha)

const handleLogin = async () => {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!captcha.captchaId) {
    ElMessage.error('请先获取验证码')
    await loadCaptcha()
    return
  }

  try {
    const loginState = await userStore.accountLogin({
      username: loginForm.username,
      password: loginForm.password,
      captchaId: captcha.captchaId,
      captchaAnswer: loginForm.captchaAnswer
    })
    routeStore.clearAll()
    if (!loginState) {
      ElMessage.error('用户名、密码或验证码错误')
      await loadCaptcha()
      return
    }

    const routesState = await routeStore.getUserRouters()
    if (!routesState) {
      ElMessage.error('获取菜单失败，请检查角色权限配置')
      return
    }
    const routers = convertToRoutes(routeStore.dynamicRoutes)
    addDynamicRoutes(routers, router)
    ElMessage.success('登录成功')
    await router.push(route.query.redirect || '/')
  } catch (err) {
    try {
      ElNotification({
        title: '登录失败',
        type: 'error',
        message: h('p', null, [h('span', null, err?.message || '请稍后重试')])
      })
    } catch (_) {
      ElMessage.error('登录失败，请稍后重试')
    }
    await loadCaptcha()
  }
}
</script>

<style scoped lang="scss">
.atlas-login {
  position: relative;
  z-index: 1;
  height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  background: var(--paper);
}

.art {
  position: relative;
  overflow: hidden;
  border-right: 1px solid var(--line);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    background: linear-gradient(90deg, rgba(236, 226, 207, 0.3) 0%, transparent 16%, transparent 76%, var(--paper) 100%),
      linear-gradient(0deg, rgba(236, 226, 207, 0.94) 0%, rgba(236, 226, 207, 0.5) 20%, transparent 38%);
  }

  .wash {
    position: absolute;
    inset: -4%;
    background-size: contain;
    background-position: 50% 46%;
    background-repeat: no-repeat;
    opacity: 1;
    filter: saturate(1.02) contrast(1.02);
  }

  .fig {
    position: absolute;
    left: 52px;
    top: 42px;
    z-index: 2;
    font-family: var(--mono);
    font-size: 10px;
    letter-spacing: 0.2em;
    color: var(--ink-3);
  }

  .ecg {
    position: absolute;
    left: 4%;
    right: 4%;
    top: 74%;
    height: 44px;
    opacity: 0.3;

    path {
      fill: none;
      stroke: var(--ochre);
      stroke-width: 1.1;
      stroke-linecap: round;
      stroke-dasharray: 900;
      stroke-dashoffset: 900;
      animation: draw 3.4s var(--ease) 0.5s forwards;
    }
  }

  @keyframes draw {
    to {
      stroke-dashoffset: 0;
    }
  }

  .caption {
    position: absolute;
    left: 52px;
    right: 52px;
    bottom: 46px;
    z-index: 2;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 18px;
    border-top: 1px solid var(--line);
    padding-top: 16px;

    p {
      font-family: var(--serif);
      font-size: 19px;
      line-height: 1.6;
      max-width: 24ch;
    }

    span {
      font-family: var(--mono);
      font-size: 10px;
      letter-spacing: 0.16em;
      color: var(--ink-3);
      white-space: nowrap;
    }
  }
}

.panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 9% 0 8%;
  overflow-y: auto;

  .brand {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 58px;

    .mark {
      width: 34px;
      height: 34px;
      border: 1px solid var(--ink);
      border-radius: 50%;
      display: grid;
      place-items: center;
      font-family: var(--serif);
      font-size: 15px;
    }

    strong {
      display: block;
      font-family: var(--serif);
      font-size: 19px;
      font-weight: 500;
      line-height: 1.2;
    }

    em {
      display: block;
      font-style: normal;
      font-family: var(--mono);
      font-size: 8.5px;
      letter-spacing: 0.22em;
      color: var(--ink-3);
      margin-top: 3px;
    }
  }

  h1 {
    font-family: var(--serif);
    font-size: 34px;
    font-weight: 400;
    line-height: 1.35;
    letter-spacing: 0.01em;
  }

  .hint {
    margin: 10px 0 36px;
    color: var(--ink-2);
    font-size: 13px;
  }

  .field {
    position: relative;
    margin-bottom: 14px;

    label {
      display: block;
      font-family: var(--mono);
      font-size: 10px;
      letter-spacing: 0.18em;
      color: var(--ink-3);
      margin-bottom: 2px;
    }

    :deep(.el-form-item) {
      margin-bottom: 0;
    }

    :deep(.el-input__wrapper) {
      box-shadow: none;
      border-bottom: 1px solid var(--line);
      border-radius: 0;
      padding-left: 0;
      padding-right: 0;
      background: transparent;
      transition: border-color 0.3s;

      &:hover,
      &.is-focus {
        box-shadow: none;
        border-bottom-color: var(--ink);
      }
    }

    :deep(.el-input__inner) {
      font-size: 15px;
      height: 38px;
    }
  }

  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
    align-items: center;

    :deep(.el-input) {
      flex: 1;
    }

    .cap {
      flex: 0 0 auto;
      font-family: var(--mono);
      font-size: 12px;
      color: var(--ochre);
      letter-spacing: 0.06em;
      border-bottom: 1px dashed rgba(180, 85, 45, 0.4);
      padding-bottom: 2px;
      transition: opacity 0.2s;

      &:hover:not(:disabled) {
        opacity: 0.75;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 30px;

    .enter {
      height: 42px;
      padding: 0 22px;
      background: var(--ink);
      border-color: var(--ink);
      color: var(--paper);
      font-size: 13px;
      letter-spacing: 0.06em;
      border-radius: 2px;
      display: inline-flex;
      align-items: center;
      gap: 10px;
      transition: background 0.3s, transform 0.35s var(--ease);

      svg {
        width: 14px;
        height: 14px;
        stroke: currentColor;
        fill: none;
        stroke-width: 1.5;
        position: static;
        margin: 0;
      }

      &:hover {
        background: var(--indigo);
        border-color: var(--indigo);
        transform: translateX(3px);
      }
    }

    .foot {
      font-family: var(--mono);
      font-size: 10px;
      color: var(--ink-3);
      letter-spacing: 0.1em;
    }
  }
}

@media (max-width: 900px) {
  .atlas-login {
    grid-template-columns: 1fr;
  }
  .art {
    display: none;
  }
}
</style>
