// index页API服务

import request from '@/sheep/request'
import mockData from './mock'

// 判断是否使用mock数据
const useMock = false

// 获取首页数据
export const getIndexData = () => {
  console.log('useMock:', useMock);
  if (useMock) {
    return Promise.resolve(mockData.indexData)
  }
  return  request({
    url: '/index/data',
    method: 'GET'
  });
}

// 获取通知消息
export const getNotifications = () => {
  if (useMock) {
    return Promise.resolve(mockData.notifications)
  }
  return  request({
    url: '/notifications',
    method: 'GET'
  });
}

// 获取应用配置
export const getAppConfig = () => {
  if (useMock) {
    return Promise.resolve(mockData.appConfig)
  }
  return  request({
    url: '/app/config',
    method: 'GET'
  });
}

// 检查更新
export const checkUpdate = () => {
  if (useMock) {
    return Promise.resolve(mockData.updateInfo)
  }
  return  request({
    url: '/app/checkUpdate',
    method: 'GET'
  });
}

// 上报用户行为
export const reportUserAction = (data) => {
  if (useMock) {
    return Promise.resolve({ success: true })
  }
  return  request({
    url: '/user/action',
    method: 'GET',
    data:data
  });
}

// 导出首页API
export default {
  /**
   * 获取首页数据
   * 请替换为真实的API实现
   */
  getIndexData: () => {
    return Promise.resolve({});
  }
};