import axios from 'axios'
import {ElMessage, ElMessageBox, ElNotification} from 'element-plus'
import router from '@/router'
import {useUserStore, useAppStore} from '@/store'
import {h} from "vue";



// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000 // 请求超时时间
})

// 处理请求拦截
service.interceptors.request.use(
  config => {
    const appStore = useAppStore();
    appStore.setLoading(true);
    
    // 从store中获取token，如果有token则添加到请求头中
    const userStore = useUserStore();
    if (userStore.getIsLogin && userStore.getTokenValue && userStore.getTokenName) {
      config.headers[userStore.getTokenName] = userStore.getTokenValue;
    }
    return config
  },
  error => {
    // 处理请求错误
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
      // 请求完成后停止loading
      const appStore = useAppStore();
      appStore.setLoading(false);

      // 处理HTTP状态码错误

      handleHttpStatusError(response)
        // 处理业务状态码错误
      return handleBusinessCodeError(response.data)
    },
    error => {
      // 请求完成后停止loading
      const appStore = useAppStore();
      appStore.setLoading(false);

      // 处理网络错误
      return handleNetworkError(error)
    }
)


/**
 * 处理HTTP状态码错误
 * @param {Object} response - 响应对象
 * @returns {Object|Promise} - 成功时返回响应,失败时返回Promise.reject
 */
const handleHttpStatusError = (response) => {
  if (response.status !== 200) {
    ElMessage({
      message: '网络异常，稍后重试',
      type: 'error',
      duration: 5000
    })
    return Promise.reject(new Error('网络异常，稍后重试'))
  }
  return response
}

/**
 * 处理401未授权错误
 * @returns {Promise} - 返回Promise.reject
 */
const handle401Error = () => {
  ElMessageBox.confirm(
    '登录状态已失效，请重新登录',
    '系统提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 清除token并重定向到登录页
    useUserStore().clearLoginState();
    router.push('/login')
  })
  return Promise.reject(new Error('登录状态已失效'))
}

/**
 * 处理业务状态码错误
 * @param {Object} res - 响应数据
 * @returns {Object|Promise} - 成功时返回响应数据,失败时返回Promise.reject
 */
const handleBusinessCodeError = (res) => {
  const message = res?.msg;
  // 业务状态码处理
  switch (res.code) {
    // 成功
    case 200:
      return res
    // 未登录或token过期
    case 401:
      return handle401Error()
      
    // 权限不足
    case 403:
      ElMessage({
        message: message || '权限不足，无法访问',
        type: 'error',
        duration: 5000
      })
      return Promise.reject(new Error('权限不足'))
      
    // 资源不存在
    case 404:
      ElMessage({
        message: '请求的资源不存在',
        type: 'error',
        duration: 5000
      })
      return Promise.reject(new Error('资源不存在'))
      
    // 服务器错误
    case 500:
      // 只在响应拦截器中显示错误信息，不在错误处理函数中重复显示
      ElMessage({
        message: message || '服务有误，稍后重试',
        type: 'error',
        duration: 5000
      })
      return Promise.reject(new Error(message))
      
    // 其他业务错误
    default:
      ElMessage({
        message: message || `未知错误(${res.code})`,
        type: 'error',
        duration: 5000
      })
      return Promise.reject(new Error(message || '未知错误'))
  }
}

/**
 * 处理网络错误
 * @param {Error} error - 错误对象
 * @returns {Promise} - 返回Promise.reject
 */
const handleNetworkError = (error) => {
  // 如果错误已经在业务代码处理中显示过消息，则不再显示
  if (error._showedMessage) {
    return Promise.reject(error)
  }
  
  // 网络错误或请求被中断的处理
  let message = '网络异常'
  
  if (error.response) {
    // 服务器返回了错误状态码
    message = getHttpStatusErrorMessage(error.response.status)
  } else if (error.request) {
    // 请求发出但没收到响应
    message = '服务器无响应'
  } else if (error.message.includes('timeout')) {
    // 请求超时
    message = '请求超时，请稍后再试'
  }

  ElNotification({
    title: '‼️错误',
    type: 'error',
    message: h('p', null, [
      h('span', null, message)
    ]),
  })
  return Promise.reject(error)
}

/**
 * 根据HTTP状态码获取错误消息
 * @param {Number} status - HTTP状态码
 * @returns {String} - 错误消息
 */
const getHttpStatusErrorMessage = (status) => {
  switch (status) {
    case 400: return '请求错误(400)'
    case 401: return '未授权，请重新登录(401)'
    case 403: return '拒绝访问(403)'
    case 404: return '请求的资源不存在(404)'
    case 408: return '请求超时(408)'
    case 500: return '服务器内部错误(500)'
    case 501: return '服务未实现(501)'
    case 502: return '网关错误(502)'
    case 503: return '服务不可用(503)'
    case 504: return '网关超时(504)'
    default: return `未知错误(${status})`
  }
}



export default service 