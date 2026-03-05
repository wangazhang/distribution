import request from '@/config/axios'

// 佣金统计数据接口
export interface BrokerageStatisticsVO {
  totalIncome: number      // 总收入
  totalExpense: number     // 总支出
  totalCount: number       // 总笔数
  pendingCount: number     // 待结算笔数
  pendingAmount: number    // 待结算金额
  settledCount: number     // 已结算笔数
  settledAmount: number    // 已结算金额
}

export interface BrokerageRecordBizDetailRespVO {
  recordId?: number
  userId?: number
  bizType?: number
  bizCategory?: number
  bizId?: string
  title?: string
  description?: string
  price?: number
  totalPrice?: number
  status?: number
  createTime?: string
  mallOrder?: MallOrderBizDetail
  mbOrder?: MbOrderBizDetail
}

export interface MallOrderBizDetail {
  orderId?: number
  orderNo?: string
  orderStatus?: number
  orderStatusName?: string
  refundStatus?: number
  refundStatusName?: string
  orderPayPrice?: number
  payFinished?: boolean
  payChannelCode?: string
  payTime?: string
  deliveryType?: number
  deliveryTypeName?: string
  receiver?: MallOrderReceiver
  item?: MallOrderItem
}

export interface MallOrderReceiver {
  name?: string
  mobile?: string
  areaName?: string
  detailAddress?: string
}

export interface MallOrderItem {
  itemId?: number
  spuId?: number
  skuId?: number
  spuName?: string
  propertiesText?: string
  picUrl?: string
  count?: number
  unitPrice?: number
  payPrice?: number
  afterSaleStatus?: number
  afterSaleStatusName?: string
}

export interface MbOrderBizDetail {
  orderId?: number
  orderNo?: string
  status?: string
  statusName?: string
  bizType?: string
  bizTypeName?: string
  productId?: number
  productName?: string
  productImage?: string
  quantity?: number
  unitPrice?: number
  totalPrice?: number
  payPrice?: number
  payChannelCode?: string
  payStatus?: number
  payStatusName?: string
  paymentId?: string
  paySuccessTime?: string
  agentUserId?: number
  agentUserNickname?: string
  agentUserMobile?: string
  canRetryVirtualDelivery?: boolean
  canReceive?: boolean
  createTime?: string
  updateTime?: string
  deliveryTime?: string
  receiveTime?: string
}

// 查询佣金记录列表
export const getBrokerageRecordPage = async (params: any) => {
  return await request.get({ url: `/trade/brokerage-record/page`, params })
}

// 查询佣金记录详情
export const getBrokerageRecord = async (id: number) => {
  return await request.get({ url: `/trade/brokerage-record/get?id=` + id })
}

// 查询佣金记录业务详情
export const getBrokerageRecordBizDetail = async (id: number) => {
  return await request.get<BrokerageRecordBizDetailRespVO>({
    url: `/trade/brokerage-record/biz-detail`,
    params: { id }
  })
}

// 查询用户佣金统计
export const getBrokerageStatistics = async (userId: number) => {
  return await request.get({ url: `/trade/brokerage-record/statistics?userId=` + userId })
}
