// 物料相关API - 已迁移至新的物料管理接口

import request from '@/sheep/request';

/**
 * 获取特定产品的补货价格
 * 
 * @param {number} productId - 产品ID
 */
export const getMaterialRestockPrice = (productId) => {
  return request({
    url: `/material/restock-price/price/${Number(productId)}`,
    method: 'GET',
    custom: {
      showError: false // 禁用默认错误提示，让调用方自行处理
    }
  });
};

/**
 * 创建补货订单
 * 
 * @param {Object} data - 订单数据
 * @param {number} data.productId - 产品ID
 * @param {number} data.restockQuantity - 补货数量
 * @param {boolean} [data.validateStock] - 是否验证库存
 */
export const createMaterialRestockOrder = (data) => {
  // 确保数据类型正确
  const orderData = {
    productId: Number(data.productId), // 确保产品ID是数字类型
    quantity: Number(data.quantity), // 确保数量是数字类型
    bizType: data.bizType,
  };
  
  if (typeof data.validateStock !== 'undefined') {
    orderData.validateStock = Boolean(data.validateStock);
  }
  
  return request({
    url: '/mb/order/create-order',
    method: 'POST',
    data: orderData,
    // 确保正确处理响应
    custom: {
      showError: false // 禁用默认错误提示，让调用方自行处理
    }
  });
};

/**
 * 获取补货订单分页列表
 * 
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} [params.productId] - 商品ID，可选
 * @param {string} [params.paymentId] - 支付单ID，可选
 */
export const getMaterialRestockOrderPage = (params) => {
  return request({
    url: '/mb/order/order-page',
    method: 'GET',
    params,
  });
};

/**
 * 批量查询用户物料库存
 *
 * @param {Array<number>} materialIds - 物料ID数组（等同于商品ID）
 * @returns {Promise} - 返回包含库存信息的Promise
 */
export const batchQueryUserMaterialStock = (materialIds) => {
  return request({
    url: '/material/balance/batch-query',
    method: 'POST',
    data: {
      materialIds: materialIds.map(id => Number(id)) // 确保所有ID都是数字类型
    },
    custom: {
      showError: false // 禁用默认错误提示，让调用方自行处理
    }
  });
}; 
