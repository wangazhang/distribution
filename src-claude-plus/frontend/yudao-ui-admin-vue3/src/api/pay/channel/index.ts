import request from '@/config/axios'

export interface ChannelVO {
  id: number
  code: string
  config: string
  status: number
  remark: string
  feeRate: number
  appId: number
  type: number
  createTime: Date
}

// 查询列表支付渠道
export const getChannelPage = (params: PageParam) => {
  return request.get({ url: '/pay/channel/page', params })
}

// 查询详情支付渠道
export const getChannel = (appId: string, code: string) => {
  const params = {
    appId: appId,
    code: code
  }
  return request.get({ url: '/pay/channel/get', params: params })
}

// 新增支付渠道
export const createChannel = (data: ChannelVO) => {
  return request.post({ url: '/pay/channel/create', data })
}

// 修改支付渠道
export const updateChannel = (data: ChannelVO) => {
  return request.put({ url: '/pay/channel/update', data })
}

// 删除支付渠道
export const deleteChannel = (id: number) => {
  return request.delete({ url: '/pay/channel/delete?id=' + id })
}

// 导出支付渠道
export const exportChannel = (params) => {
  return request.download({ url: '/pay/channel/export-excel', params })
}

// 查询指定应用的渠道列表（可按类型筛选）
export const getChannelSimpleList = (appId: number, type?: number) => {
  return request.get({
    url: '/pay/channel/list',
    params: {
      appId,
      type
    }
  })
}
