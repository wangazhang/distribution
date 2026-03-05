// MB订单相关API

import request from '@/sheep/request';

/**
 * 获取MB订单分页列表
 *
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} [params.productId] - 商品ID，可选
 * @param {string} [params.bizType] - 业务类型，可选
 * @param {string} [params.status] - 订单状态，可选
 * @param {string} [params.paymentId] - 支付单ID，可选
 * @param {Array} [params.createTime] - 创建时间范围，可选
 */
export const getMbOrderPage = (params) => {
  return request({
    url: '/mb/order/order-page',
    method: 'GET',
    params,
  });
};

/**
 * 获取订单状态列表
 */
export const getMbOrderStatusList = () => {
  return request({
    url: '/mb/order/status-list',
    method: 'GET',
  });
};

/**
 * 创建MB订单
 * 
 * @param {Object} data - 订单数据
 * @param {number} data.productId - 产品ID
 * @param {number} data.quantity - 数量
 * @param {string} data.bizType - 业务类型
 */
export const createMbOrder = (data) => {
  // 确保数据类型正确
  const orderData = {
    productId: Number(data.productId),
    quantity: Number(data.quantity),
    bizType: data.bizType,
  };
  
  return request({
    url: '/mb/order/create-order',
    method: 'POST',
    data: orderData,
    custom: {
      showError: false // 禁用默认错误提示，让调用方自行处理
    }
  });
};

/**
 * 获取MB订单详情
 *
 * @param {number} orderId - 订单ID
 */
export const getMbOrderDetail = (orderId) => {
  return request({
    url: `/mb/order/get?id=${orderId}`,
    method: 'GET',
    custom: {
      showError: false
    }
  });
};

/**
 * 确认收货
 *
 * @param {number} orderId - 订单ID
 */
export const receiveMbOrder = (orderId) => {
  return request({
    url: `/mb/order/receive?id=${orderId}`,
    method: 'PUT',
    custom: {
      showError: false
    }
  });
};

/**
 * 补偿校验渠道收货状态
 *
 * @param {number} orderId - 订单ID
 */
export const checkMbOrderChannelConfirm = (orderId) => {
  return request({
    url: '/mb/order/check-channel-confirm',
    method: 'POST',
    data: { orderId },
    custom: {
      showError: false
    }
  });
};

/**
 * 申请退款
 * 
 * @param {Object} data - 退款申请数据
 * @param {number} data.orderId - 订单ID
 * @param {string} [data.reason] - 退款原因
 */
export const applyRefund = (data) => {
  return request({
    url: '/mb/order/apply-refund',
    method: 'POST',
    data,
    custom: {
      showError: false
    }
  });
};

/**
 * 获取退款详情
 * 
 * @param {number} orderId - 订单ID
 */
export const getRefundDetail = (orderId) => {
  return request({
    url: `/mb/order/refund-detail?orderId=${orderId}`,
    method: 'GET',
    custom: {
      showError: false
    }
  });
};

/**
 * 取消订单
 * 
 * @param {number} orderId - 订单ID
 */
export const cancelMbOrder = (orderId) => {
  return request({
    url: `/mb/order/cancel?id=${orderId}`,
    method: 'POST',
    custom: {
      showError: false
    }
  });
};

/**
 * 订单状态枚举 - 统一使用数字状态
 */
export const ORDER_STATUS = {
  PENDING: 1,        // 待处理
  PROCESSING: 2,     // 处理中
  DELIVERED: 3,      // 待确认收货
  COMPLETED: 4,      // 已完成
  FAILED: 5,         // 处理失败
  REFUNDING: 6,      // 退款中
  REFUNDED: 7,       // 已退款
  REFUND_FAILED: 8,  // 退款失败
};

/**
 * 订单状态名称映射
 */
export const ORDER_STATUS_NAME = {
  [ORDER_STATUS.PENDING]: '待处理',
  [ORDER_STATUS.PROCESSING]: '处理中',
  [ORDER_STATUS.DELIVERED]: '待确认收货',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.FAILED]: '处理失败',
  [ORDER_STATUS.REFUNDING]: '退款中',
  [ORDER_STATUS.REFUNDED]: '已退款',
  [ORDER_STATUS.REFUND_FAILED]: '退款失败',
};

/**
 * 订单状态颜色映射
 */
export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: '#ff9500',        // 待处理 - 橙色
  [ORDER_STATUS.PROCESSING]: '#007aff',     // 处理中 - 蓝色
  [ORDER_STATUS.DELIVERED]: '#34c759',      // 待收货 - 绿色
  [ORDER_STATUS.COMPLETED]: '#34c759',      // 已完成 - 绿色
  [ORDER_STATUS.FAILED]: '#ff3b30',         // 处理失败 - 红色
  [ORDER_STATUS.REFUNDING]: '#ff9500',      // 退款中 - 橙色
  [ORDER_STATUS.REFUNDED]: '#8e8e93',       // 已退款 - 灰色
  [ORDER_STATUS.REFUND_FAILED]: '#ff3b30',  // 退款失败 - 红色
};

/**
 * 业务类型枚举
 */
export const BIZ_TYPE = {
  RESTOCK: 'restock',                    // 补货
  MATERIAL_CONVERT: 'collagenConvert',   // 物料转化
};

// 兼容旧命名
export const BIZ_TYPE_LEGACY = {
  RESTOCK: BIZ_TYPE.RESTOCK,
  COLLAGEN_CONVERT: BIZ_TYPE.MATERIAL_CONVERT,
};

/**
 * 业务类型名称映射
 */
export const BIZ_TYPE_NAME = {
  [BIZ_TYPE.RESTOCK]: '物料补货',
  [BIZ_TYPE.MATERIAL_CONVERT]: '物料转化',
};

/**
 * MB订单状态格式化函数
 * @param {Object} order - 订单对象
 * @returns {string} 状态名称
 */
export function formatMbOrderStatus(order) {
  return ORDER_STATUS_NAME[order.status] || '未知状态';
}

/**
 * MB订单状态颜色格式化函数
 * @param {Object} order - 订单对象
 * @returns {string} 颜色CSS类名
 */
export function formatMbOrderColor(order) {
  const colorMap = {
    [ORDER_STATUS.PENDING]: 'warning-color',      // 待处理 - 橙色
    [ORDER_STATUS.PROCESSING]: 'info-color',      // 处理中 - 蓝色
    [ORDER_STATUS.DELIVERED]: 'success-color',    // 待收货 - 绿色
    [ORDER_STATUS.COMPLETED]: 'success-color',    // 已完成 - 绿色
    [ORDER_STATUS.FAILED]: 'danger-color',        // 处理失败 - 红色
    [ORDER_STATUS.REFUNDING]: 'warning-color',    // 退款中 - 橙色
    [ORDER_STATUS.REFUNDED]: 'info-color',        // 已退款 - 灰色
    [ORDER_STATUS.REFUND_FAILED]: 'danger-color', // 退款失败 - 红色
  };
  return colorMap[order.status] || 'danger-color';
}
