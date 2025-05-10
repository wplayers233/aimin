import request from '@/utils/request'

/**
 * 获取药品列表（不分页）
 * @param {Object} params 查询参数
 * @returns {Promise} Promise对象
 */
export function getDrugList(params) {
  return request({
    url: '/drug',
    method: 'get',
    params
  })
}

/**
 * 分页获取药品列表
 * @param {Object} data 分页和查询参数
 * @returns {Promise} Promise对象
 */
export function getDrugPage(data) {
  return request({
    url: '/drug/page',
    method: 'post',
    data
  })
}

/**
 * 添加药品
 * @param {Object} data 药品信息
 * @returns {Promise} Promise对象
 */
export function addDrug(data) {
  return request({
    url: '/drug',
    method: 'post',
    data
  })
}

/**
 * 更新药品信息
 * @param {Object} data 药品信息
 * @returns {Promise} Promise对象
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
 * @returns {Promise} Promise对象
 */
export function deleteDrug(data) {
  return request({
    url: `/drug/batchDelete`,
    method: 'post',
    data
  })
}

/**
 * 获取药品详情
 * @param {number} drugId 药品ID
 * @returns {Promise} Promise对象
 */
export function getDrugById(drugId) {
  return request({
    url: `/drug/${drugId}`,
    method: 'get'
  })
}

/**
 * 获取药品图片列表
 * @param {number} drugId 药品ID
 * @returns {Promise} Promise对象
 */
export function getDrugImages(drugId) {
  return request({
    url: `/drug/img/${drugId}`,
    method: 'get'
  })
}


/**
 * 删除药品图片
 * @param {number} imageId 图片ID
 * @returns {Promise} Promise对象
 */
export function deleteDrugImage(imageId) {
  return request({
    url: `/drug/img/${imageId}`,
    method: 'delete'
  })
}

/**
 * 批量上传药品图片
 * @param {FormData} formData 表单数据
 * @returns {Promise} Promise对象
 */
export function batchUploadDrugImages(formData) {
  return request({
    url: '/drug/img/batchUpload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 