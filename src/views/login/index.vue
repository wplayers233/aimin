<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img :src="logoSrc" alt="Logo" class="logo" />
        <h2 class="title">ElementAdmin</h2>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
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
        <el-form-item>
          <Captcha/>
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="appStore.loading"
            type="primary"
            class="login-button"
            id="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, onBeforeUnmount, h} from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

import { useUserStore, useRouteStore } from '@/store'
import { convertToRoutes, addDynamicRoutes } from '@/utils/dynamicRoutes'
import { useAppStore } from '@/store'
const appStore = useAppStore()


const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)

// 使用与布局组件相同的 logo
const logoSrc = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iIzQwOUVGRiIgLz48dGV4dCB4PSI1MCIgeT0iNTAiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIzNSIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiPkVBPC90ZXh0Pjwvc3ZnPg==';

const loginForm = reactive({
  username: '',
  password: ''
})

const captcha = ref(null)
const captchaButton = ref(null)

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
}
onMounted(() => {
  captchaButton.value = document.getElementById('captcha-button')

  window.initAliyunCaptcha({
    SceneId: '3xb2a2t8', // 场景ID。根据步骤二新建验证场景后，您可以在验证码场景列表，获取该场景的场景ID
    prefix: '113p8i', // 身份标。开通阿里云验证码2.0后，您可以在控制台概览页面的实例基本信息卡片区域，获取身份标
    mode: 'popup', // 验证码模式。popup表示要集成的验证码模式为弹出式。无需修改
    element: '#captcha-element', // 页面上预留的渲染验证码的元素，与原代码中预留的页面元素保持一致。
    button: '#login-btn', // 触发验证码弹窗的元素。button表示单击登录按钮后，触发captchaVerifyCallback函数。您可以根据实际使用的元素修改element的值
    captchaVerifyCallback: captchaVerifyCallback, // 业务请求(带验证码校验)回调函数，无需修改
    onBizResultCallback: onBizResultCallback, // 业务请求结果回调函数，无需修改
    getInstance: getInstance, // 绑定验证码实例函数，无需修改
    slideStyle: {
      width: 360,
      height: 40
    }, // 滑块验证码样式，支持自定义宽度和高度，单位为px。其中，width最小值为320 px
    language: 'cn' // 验证码语言类型，支持简体中文（cn）、繁体中文（tw）、英文（en）
  })
})

onBeforeUnmount(() => {
  captchaButton.value = null
  // 必须删除相关元素，否则再次mount多次调用 initAliyunCaptcha 会导致多次回调 captchaVerifyCallback
  document.getElementById('aliyunCaptcha-mask')?.remove()
  document.getElementById('aliyunCaptcha-window-popup')?.remove()
})

const getInstance = (instance) => {
  captcha.value = instance
}

const captchaVerifyCallback = async (captchaVerifyParam) => {

  await handleLogin(captchaVerifyParam);
  return {
    captchaResult: useUserStore().captchaResult,
    bizResult: true
  }
}

// 验证通过后调用
const onBizResultCallback = () => {

}

const handleLogin = async (captchaVerifyParam) => {
  const valid = await loginFormRef.value.validate();
    if (!valid) return
    const userStore = useUserStore();
    const routeStore = useRouteStore();

    const loginReq = {
      ...loginForm,
      captchaVerifyParam
    }
    const loginState = await userStore.accountLogin(loginReq);
    routeStore.clearAll();
    if (!loginState) return;
    const routesState = await routeStore.getUserRouters();
    if (loginState && routesState) {
      try {
        console.log(routeStore.dynamicRoutes);
        const routers = convertToRoutes(routeStore.dynamicRoutes);
        console.log('routers', routers);
        addDynamicRoutes(routers, router);
      }catch(err) {
        ElNotification({
          title: '‼️错误',
          type: 'error',
          message: h('p', null, [
            h('span', null, '路由加载出错，联系管理员')
          ]),
        })
      }
      ElMessage.success('登录成功')
      // 如果有重定向参数，则跳转到对应页面，否则跳转到dashboard
      const redirectPath = route.query.redirect || '/'
      // 确保路由已经添加完成再跳转
      setTimeout(() => {
        router.push(redirectPath)
      }, 300);
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
  background-color: #f5f7f9;
  
  .login-card {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .login-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 30px;
      
      .logo {
        width: 80px;
        height: 80px;
        margin-bottom: 16px;
      }
      
      .title {
        font-size: 24px;
        color: #303133;
        margin: 0;
      }
    }
    
    .login-form {
      .login-button {
        width: 100%;
      }
    }
  }
}
</style> 