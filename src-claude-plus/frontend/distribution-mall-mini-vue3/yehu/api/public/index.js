// 个人中心页API服务

import request from '@/sheep/request'
import mockData from './public.json'

// 判断是否使用mock数据
const useMock = true

// 获取用户信息
export const getImageContentList = (data) => {
  console.log('使用getImageContentList');
  if (useMock) {
    return Promise.resolve({ data:mockData[data.type]} || [] )
  }
  return request.post('/XXX/XXXX',data)
}
