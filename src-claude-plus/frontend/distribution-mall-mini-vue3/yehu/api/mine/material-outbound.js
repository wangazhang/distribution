// 物料出库相关API - 已迁移至新的物料管理接口

import request from '@/sheep/request';

/**
 * 创建物料出库申请
 * 注意：后端在接收到请求后应该再次校验每个物料的库存数量，
 * 确保每个物料的出库数量不超过用户当前的库存量，
 * 这是一个关键的业务规则，前后端都需要进行验证。
 * 
 * @param {Object} data - 出库申请数据
 * @param {number} data.addressId - 收货地址ID
 * @param {string} data.remark - 备注信息
 * @param {Array} data.items - 出库物料明细
 * @param {number} data.items[].materialId - 物料ID
 * @param {number} data.items[].quantity - 出库数量
 */
export const createMaterialOutbound = (data) => {
  return request({
    url: '/material/outbound/create',
    method: 'POST',
    data,
    custom: {
      showSuccess: true,
      successMsg: '出库申请提交成功'
    }
  });
};

/**
 * 获取物料出库单分页列表
 * 
 * @param {Object} params - 查询参数
 * @param {number} params.pageNo - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} [params.status] - 状态，可选
 */
export const getMaterialOutboundPage = (params) => {
  return request({
    url: '/material/outbound/page',
    method: 'GET',
    params
  });
};

/**
 * 获取物料出库单详情
 * 
 * @param {number} id - 出库单ID
 */
export const getMaterialOutboundDetail = (id) => {
  return request({
    url: `/material/outbound/get?id=${id}`,
    method: 'GET'
  });
};

/**
 * 取消物料出库申请
 *
 * @param {Object} data - 取消数据
 * @param {number} data.id - 出库单ID
 * @param {string} data.cancelReason - 取消原因
 */
export const cancelMaterialOutbound = (data) => {
  return request({
    url: '/material/outbound/cancel',
    method: 'PUT',
    params: {
      id: data.id,
      reason: data.cancelReason
    },
    custom: {
      showSuccess: true,
      successMsg: '出库申请已取消'
    }
  });
};

/**
 * 确认物料出库单已收货
 *
 * @param {number} id - 出库单ID
 */
export const confirmMaterialOutbound = (id) => {
  return request({
    url: `/material/outbound/complete`,
    method: 'PUT',
    params: { id },
    custom: {
      showSuccess: true,
      successMsg: '已确认收货'
    }
  });
};

/**
 * 获取用户可出库的物料列表
 */
export const getOutboundMaterialList = () => {
  return request({
    url: '/material/balance/outbound-materials',
    method: 'GET'
  });
};

/**
 * 过滤原料物料（编号90000），获取真正可以出库的物料列表
 * 
 * @param {Array} materialList - 物料列表
 * @returns {Array} - 过滤后的物料列表
 */
export const filterOutboundMaterials = (materialList) => {
  if (!materialList || !Array.isArray(materialList)) {
    return [];
  }
  
  // 过滤掉原料物料（编号90000）
  return materialList.filter(item => item.id !== 90000);
};

/**
 * 获取可出库物料列表（后端已过滤原料物料）
 * 直接调用后端新增的接口，已过滤掉原料物料（编号90000）
 */
export const getFilteredOutboundMaterials = () => {
  return request({
    url: '/material/balance/outbound-materials',
    method: 'GET'
  });
}; 
