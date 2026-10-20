import request from '@/utils/request'

/**
 * 获取子权限列表
 * @param pid 父权限id
 * @returns {*}
 */
export function getChildrenByPid(pid) {
    return request({
        url: `/system/permission/parent/${pid}`,
        method: 'get',
    })
}

/**
 * 修改菜单
 * @param data
 * @returns {*}
 */
export function update(data) {
    return request({
        url: '/system/permission',
        method: 'put',
        data
    })
}

/**
 * 添加菜单
 * @param data
 * @returns {*}
 */
export function add(data) {
    return request({
        url: '/system/permission',
        method: 'post',
        data
    })
}

/**
 *
 * @param roleId
 * @returns {*}
 */
export function getPermissionTreeWithRole(roleId) {
    return request({
        url: `/system/permission/tree/${roleId}`,
        method: 'get',
    })
}



/**
 * 删除菜单
 * @param {Number} id 菜单ID
 * @returns {Promise}
 */
export function del(id ) {
    return request({
        url: `/system/permission/${id}`,
        method: 'delete'
    })
}


export function getMenuTree() {
    return request({
        url: '/system/permission/menu/tree',
        method: 'get'
    })
}

