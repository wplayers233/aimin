import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './styles/index.scss'

import pinia from '@/store'
import router from '@/router/index'
// 导入自定义指令
import directives from '@/directives'

// 创建应用实例
const app = createApp(App)

// 全局挂载和注册 element-plus 的所有 icon
app.config.globalProperties.$icons = []

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.config.globalProperties.$icons.push(key)
  app.component(key, component)
}

app.use(pinia)
// 在 Pinia 初始化后再使用 store
import { loadRoutesFromStore } from '@/utils/dynamicRoutes'
// 从store中加载动态路由,避免页面刷新后路由丢失
loadRoutesFromStore(router)
app.use(ElementPlus)
app.use(router)
// 注册自定义指令
app.use(directives)



// 最后挂载应用
app.mount('#app')

