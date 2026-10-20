import request from '@/utils/request'

export function getRoleList() {
    return request({
        url: '/system/role',
        method: 'get'
    })
}

/**
 * 创建新角色
 * @param {Object} data - 角色数据
 * @returns {Promise}
 */
export function createRole(data) {
    return request({
        url: '/system/role',
        method: 'post',
        data
    })
}

/**
 * 更新角色
 * @param {Object} data - 角色数据
 * @returns {Promise}
 */
export function updateRole(data) {
    return request({
        url: `/system/role`,
        method: 'put',
        data
    })
}

/**
 * 删除角色
 * @param {Number} roleId - 角色ID
 * @returns {Promise}
 */
export function deleteRole(roleId) {
    return request({
        url: `/system/role/${roleId}`,
        method: 'delete'
    })
}

/**
 * 分配角色权限
 * @param {Number} roleId - 角色ID
 * @param {Array<Number>} permissionIds - 权限ID数组
 * @returns {Promise}
 */
export function assignPermissions(roleId, permissionIds) {
    return request({
        url: `/system/role/${roleId}/permissions`,
        method: 'put',
        data: permissionIds
    })
}