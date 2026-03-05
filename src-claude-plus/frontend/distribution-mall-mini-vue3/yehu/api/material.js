/**
 * 物料相关API
 */
import sheep from '@/sheep';
const http = sheep.$api;

/**
 * 获取特定产品的补货价格
 * 
 * @param {number} productId - 产品ID
 */
export function getMaterialRestockPrice(productId) {
  return http.get(`/material/restock-price/price/${productId}`);
}

/**
 * 创建补货订单
 * 
 * @param {Object} data - 订单数据
 * @param {number} data.productId - 产品ID
 * @param {number} data.restockQuantity - 补货数量
 */
export function createMaterialRestockOrder(data) {
  return http.post('/mb/order/create-order', data);
}

/**
 * 获取补货订单分页列表
 * 
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} [params.productId] - 商品ID，可选
 * @param {string} [params.paymentId] - 支付单ID，可选
 */
export function getMaterialRestockOrderPage(params) {
  return http.get('/mb/order/order-page', { params });
} 