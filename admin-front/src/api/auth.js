import request from '@/utils/request'

/**
 * 获取登录验证码（本地算术验证码，一次性）
 * @returns {Promise<{data: {captchaId: string, question: string}}>}
 */
export function getCaptcha() {
  return request({
    url: '/public/auth/captcha',
    method: 'get'
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.captchaId - 验证码 ID
 * @param {string} data.captchaAnswer - 验证码计算结果
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/public/auth/token',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/public/auth/logout',
    method: 'get'
  })
}

