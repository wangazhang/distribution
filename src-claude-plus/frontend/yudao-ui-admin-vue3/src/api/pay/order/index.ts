import request from '@/config/axios'

export interface OrderVO {
  id: number
  merchantId: number
  appId: number
  channelId: number
  channelCode: string
  merchantOrderId: string
  subject: string
  body: string
  notifyUrl: string
  notifyStatus: number
  amount: number
  channelFeeRate: number
  channelFeeAmount: number
  status: number
  userIp: string
  expireTime: Date
  successTime: Date
  notifyTime: Date
  successExtensionId: number
  refundStatus: number
  refundTimes: number
  refundAmount: number
  channelUserId: string
  channelOrderNo: string
  createTime: Date
}

export interface OrderPageReqVO extends PageParam {
  // 支付 ID 精准筛选，避免与支付 NO 概念混淆
  id?: number
  merchantId?: number
  appId?: number
  channelId?: number
  channelCode?: string
  merchantOrderId?: string
  // 支付 NO 是 pay_no
  no?: string
  subject?: string
  body?: string
  notifyUrl?: string
  notifyStatus?: number
  amount?: number
  channelFeeRate?: number
  channelFeeAmount?: number
  status?: number
  expireTime?: Date[]
  successTime?: Date[]
  notifyTime?: Date[]
  successExtensionId?: number
  refundStatus?: number
  refundTimes?: number
  channelUserId?: string
  channelOrderNo?: string
  createTime?: Date[]
}

export interface OrderExportReqVO {
  // 支付 ID 精准筛选，避免与支付 NO 概念混淆
  id?: number
  merchantId?: number
  appId?: number
  channelId?: number
  channelCode?: string
  merchantOrderId?: string
  // 支付 NO 是 pay_no
  no?: string
  subject?: string
  body?: string
  notifyUrl?: string
  notifyStatus?: number
  amount?: number
  channelFeeRate?: number
  channelFeeAmount?: number
  status?: number
  expireTime?: Date[]
  successTime?: Date[]
  notifyTime?: Date[]
  successExtensionId?: number
  refundStatus?: number
  refundTimes?: number
  channelUserId?: string
  channelOrderNo?: string
  createTime?: Date[]
}

export interface ChannelOrderRespVO {
  status?: number
  statusName?: string
  deliveryStatus?: string
  deliveryStatusName?: string
  outTradeNo?: string
  channelOrderNo?: string
  channelTransactionNo?: string
  channelUserId?: string
  successTime?: string
  rawData?: Record<string, any> | null
}

// 查询列表支付订单
export const getOrderPage = async (params: OrderPageReqVO) => {
  return await request.get({ url: '/pay/order/page', params })
}

// 查询详情支付订单
export const getOrder = async (id: number, sync?: boolean) => {
  return await request.get({
    url: '/pay/order/get',
    params: {
      id,
      sync
    }
  })
}

// 获得支付订单的明细
export const getOrderDetail = async (id: number) => {
  return await request.get({ url: '/pay/order/get-detail?id=' + id })
}

// 提交支付订单
export const submitOrder = async (data: any) => {
  return await request.post({ url: '/pay/order/submit', data })
}

// 查询渠道侧订单
export const queryChannelOrder = async (id: number | string) => {
  const pathId = typeof id === 'string' ? id : String(id)
  return await request.get<ChannelOrderRespVO>({ url: `/pay/order/${pathId}/channel-order` })
}

// 导出支付订单
export const exportOrder = async (params: OrderExportReqVO) => {
  return await request.download({ url: '/pay/order/export-excel', params })
}
