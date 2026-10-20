import request from '@/utils/request'

export function getRouters() {
    return request({
        url: '/auth/router/list',
        method: 'get'
    })
}