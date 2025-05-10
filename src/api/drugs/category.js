import request from '@/utils/request'

/**
 * 获取药品分类列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getCategoryList(params) {
  return request({
    url: '/drug/category',
    method: 'get',
    params
  })
}

/**
 * 获取药品分类树形结构
 * @returns {Promise}
 */
export function getCategoryTree() {
  return request({
    url: '/drug/category/tree',
    method: 'get'
  })
}

/**
 * 添加药品分类
 * @param {Object} data - 分类数据
 * @returns {Promise}
 */
export function addCategory(data) {
  return request({
    url: '/drug/category',
    method: 'post',
    data
  })
}

/**
 * 更新药品分类
 * @param {Object} data - 分类数据
 * @returns {Promise}
 */
export function updateCategory(data) {
  return request({
    url: `/drug/category/${data.categoryId}`,
    method: 'put',
    data
  })
}

/**
 * 删除药品分类
 * @param {Number} categoryId - 分类ID
 * @returns {Promise}
 */
export function deleteCategory(categoryId) {
  return request({
    url: `/drug/category/${categoryId}`,
    method: 'delete'
  })
} 