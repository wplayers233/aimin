import request from '@/utils/request'

/**
 * 获取药品列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getDrugList() {
  return request({
    url: '/drug/list',
    method: 'get'
  })
}

export function getDrugPage(data) {
  return request({
    url: '/drug/page',
    method: 'post',
    data
  })
}

/**
 * 添加药品
 * @param {Object} data - 药品数据
 * @returns {Promise}
 */
export function addDrug(data) {
  return request({
    url: '/drug',
    method: 'post',
    data
  })
}

/**
 * 更新药品
 * @param {Object} data - 药品数据
 * @returns {Promise}
 */
export function updateDrug(data) {
  return request({
    url: `/drug`,
    method: 'put',
    data
  })
}

/**
 * 删除药品
 * @param {Number} drugId - 药品ID
 * @returns {Promise}
 */
export function deleteDrug(drugId) {
  return request({
    url: `/drug/${drugId}`,
    method: 'delete'
  })
}

/**
 * 获取药品详情
 * @param {Number} drugId - 药品ID
 * @returns {Promise}
 */
export function getDrugById(drugId) {
  return request({
    url: `/drug/${drugId}`,
    method: 'get'
  })
} 