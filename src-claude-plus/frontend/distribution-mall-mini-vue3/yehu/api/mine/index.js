// 个人中心页API服务

import request from '@/sheep/request';
import mockData from './mine.json';
import UserApi from '@/sheep/api/member/user';
import PayWalletApi from '@/sheep/api/pay/wallet';
import OrderApi from '@/sheep/api/trade/order';
import BrokerageApi from '@/sheep/api/trade/brokerage';

// 判断是否使用mock数据
const useMock = false;

// 获取用户信息
export const getUserInfo = () => {
  if (useMock) {
    return Promise.resolve({ data: mockData.userInfo });
  }
  return UserApi.getUserInfo();
};

// 获取用户信息  
export const getPayWalletInfo = () => {
  if (useMock) {
    return Promise.resolve({ data: mockData.userInfo });
  }
  return PayWalletApi.getPayWallet();
};
// 获取用户统计数据
export const getUserStats = () => {
  if (useMock) {
    return Promise.resolve(mockData.userStats);
  }
  // 使用OrderApi获取订单统计数据
  return OrderApi.getOrderCount();
};

// 获取我的订单列表
export const getOrderList = (params) => {
  if (useMock) {
    return Promise.resolve(mockData.orderList);
  }
  return OrderApi.getOrderPage(params);
};

// 获取收益数据
export const getEarningsData = () => {
  if (useMock) {
    return Promise.resolve({
      totalEarnings: 0,
      todayEarnings: 0,
      monthEarnings: 0,
    });
  }
  return BrokerageApi.getBrokerageUserSummary();
};
// 获取分销信息
export const getBrokerageUser = () => {
  if (useMock) {
    return Promise.resolve({
      totalEarnings: 0,
      todayEarnings: 0,
      monthEarnings: 0,
    });
  }
  return BrokerageApi.getBrokerageUser();
};

// 获取物料列表 - 已迁移至新接口
export const getMaterialList = (params) => {
  if (useMock) {
    return Promise.resolve({data:mockData.materialList});
  }
  return request({
    url: '/material/balance/page',
    method: 'GET',
    params,
  }).then(response => {
    console.log('原始API响应:', response);
    return response;
  });
};

// 获取物料余额 - 已迁移至新接口
export const getMaterialBalance = () => {
  if (useMock) {
    return Promise.resolve(mockData.materialList.list);
  }
  return request({
    url: '/material/balance/get',
    custom: {
      showLoading: false,
    },
  });
};

// 获取物料变更记录
export const getMaterialRecords = (params) => {
  if (useMock) {
    // 从mock数据中获取记录列表
    const recordsData = mockData.materialRecords.list;

    // 如果请求指定了物料ID, 则过滤记录
    const filteredRecords = params.materialId
      ? recordsData.filter((record) => record.material_id == params.materialId)
      : recordsData;

    return Promise.resolve({
      list: filteredRecords,
      total: filteredRecords.length,
      pageSize: params.pageSize || 10,
      pageNo: params.pageNo || 1,
    });
  }
  return request({
    url: '/material/record/page',
    method: 'GET',
    params,
    custom: {
      showLoading: false,
    },
  });
};

// 更新用户信息
export const updateUserInfo = (data) => {
  if (useMock) {
    return Promise.resolve({ success: true });
  }
  return UserApi.updateUser(data);
};
