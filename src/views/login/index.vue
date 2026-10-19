<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <BrandLogo :size="58" :show-text="false" />
        <h2 class="title">Aimin 医疗诊断工作台</h2>
        <p class="subtitle">医学知识库 · 智能问诊 · 证据可溯源</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="captchaAnswer">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaAnswer"
              placeholder="计算结果"
              prefix-icon="Key"
              maxlength="8"
              @keyup.enter="handleLogin"
            />
            <button
              type="button"
              class="captcha-question"
              :disabled="captchaLoading"
              title="点击刷新验证码"
              @click="loadCaptcha"
            >
              {{ captchaLoading ? '加载中…' : captcha.question }}
            </button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            :loading="appStore.loading"
            type="primary"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
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
import BrandLogo from '@/components/BrandLogo.vue'

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
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captchaAnswer: [
    { required: true, message: '请输入计算结果', trigger: 'blur' }
  ]
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captcha.captchaId = res.data?.captchaId || ''
    captcha.question = res.data?.question || '点击刷新'
    loginForm.captchaAnswer = ''
  } catch (e) {
    // 请求拦截器已提示具体错误，这里只保证界面可重试
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
      // 验证码一次性使用，失败后必须换一张
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

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  // 柔光渐变背景（苹果发布会风）
  background:
    radial-gradient(ellipse 60% 50% at 15% 20%, rgba(10, 132, 255, 0.14), transparent 65%),
    radial-gradient(ellipse 55% 45% at 85% 80%, rgba(94, 92, 230, 0.12), transparent 65%),
    radial-gradient(ellipse 40% 35% at 80% 10%, rgba(48, 209, 88, 0.08), transparent 60%),
    linear-gradient(180deg, #fafbff 0%, #f0f2f7 100%);

  .login-card {
    width: 400px;
    padding: 44px 40px 36px;
    // 毛玻璃卡片
    background: rgba(255, 255, 255, 0.68);
    backdrop-filter: blur(24px) saturate(1.6);
    -webkit-backdrop-filter: blur(24px) saturate(1.6);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: 24px;
    box-shadow:
      0 8px 40px rgba(0, 0, 0, 0.08),
      0 1px 0 rgba(255, 255, 255, 0.8) inset;
    animation: card-in 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;

    @keyframes card-in {
      from {
        opacity: 0;
        transform: translateY(16px) scale(0.98);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    .login-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 32px;

      .title {
        margin-top: 18px;
        font-size: 22px;
        font-weight: 700;
        letter-spacing: 0.4px;
        color: var(--aimin-text, #1d1d1f);
      }

      .subtitle {
        margin-top: 6px;
        font-size: 12.5px;
        color: var(--aimin-text-tertiary, #a1a1a6);
        letter-spacing: 0.6px;
      }
    }

    .login-form {
      .captcha-row {
        display: flex;
        gap: 10px;
        width: 100%;

        .el-input {
          flex: 1;
        }

        .captcha-question {
          flex: 0 0 116px;
          height: 32px;
          padding: 0 8px;
          border: 1px solid var(--el-border-color);
          border-radius: 8px;
          background: rgba(255, 255, 255, 0.7);
          color: var(--el-text-color-regular);
          font-size: 14px;
          letter-spacing: 1px;
          cursor: pointer;
          transition: all 0.15s ease;

          &:hover:not(:disabled) {
            border-color: var(--el-color-primary);
            color: var(--el-color-primary);
          }

          &:disabled {
            cursor: not-allowed;
            opacity: 0.6;
          }
        }
      }

      .login-button {
        width: 100%;
        height: 42px;
        font-size: 15px;
        font-weight: 600;
        border-radius: 12px;
      }
    }
  }
}
</style>
