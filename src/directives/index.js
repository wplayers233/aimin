import permission from './permission/hasPermission'

// 注册所有自定义指令
export default {
  install(app) {
    // 注册v-permission指令
    app.directive('permission', permission)
    
    // 这里可以注册更多的自定义指令
  }
} 