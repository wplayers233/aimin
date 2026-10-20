import request from '@/utils/request'

/**
 * 创建新用户
 * @param {Object} data - 用户数据
 * @returns {Promise} - 返回创建结果
 */
export function addAdmin(data) {
    return request({
        url: '/system/admin',
        method: 'post',
        data
    })
}

export function deleteAdmin(ids) {
    return request({
        url: '/system/admin',
        method: 'delete',
        data: ids
    })
}


/**
 * 更新用户信息
 * @param {Object} data - 用户数据
 * @returns {Promise}
 */
export function edit(data) {
    return request({
        url: '/system/admin',
        method: 'put',
        data
    })
}

/**
 * 用户模糊搜索
 * @param {Object} data - 搜索参数
 * @returns {Promise} - 返回搜索结果
 */
export function searchAdmin(data) {
  return request({
    url: '/system/admin/search',
    method: 'post',
    data
  })
}

export function getDepartmentTree() {
  return request({
    url: '/system/dept/tree',
    method: 'get'
  })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getCurrentAdminInfo() {
    return request({
        url: '/system/admin',
        method: 'get'
    })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getAdminInfoById(id) {
    return request({
        url: `/system/admin/${id}`,
        method: 'get'
    })
}




