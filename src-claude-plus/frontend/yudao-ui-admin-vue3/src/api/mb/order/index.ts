import request from '@/config/axios'

export interface MbOrderPageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
  orderNo?: string
  bizType?: string
  status?: string
  agentUserId?: number
  agentUserName?: string
  paymentId?: string
  productName?: string
  createTime?: string[]
}

export interface MbOrderSummaryVO {
  id: number
  orderNo: string
  productId: number
  productName?: string
  productImage?: string
  bizType: string
  bizTypeName?: string
  agentUserId: number
  agentUserNickname?: string
  agentUserMobile?: string
  quantity: number
  unitPrice: number
  unitPriceDisplay?: string
  totalPrice: number
  totalPriceDisplay?: string
  paymentId?: string
  status: string
  statusName?: string
  refundable?: boolean
  createTime: string
  updateTime: string
}

export interface MbOrderDetailVO extends MbOrderSummaryVO {
  payChannelCode?: string
  payStatus?: number
  payStatusName?: string
  payPrice?: number
  payPriceDisplay?: string
  paySuccessTime?: string
  canRetryVirtualDelivery?: boolean
}

export const MbOrderApi = {
  getOrderPage: async (params: MbOrderPageReqVO) => {
    return await request.get<PageResult<MbOrderSummaryVO>>({ url: '/mb/order/page', params })
  },
  getOrder: async (id: number) => {
    return await request.get<MbOrderDetailVO>({ url: '/mb/order/get', params: { id } })
  },
  updateStatus: async (data: { id: number; status: string }) => {
    return await request.put({ url: '/mb/order/status', data })
  },
  retryVirtualDelivery: async (id: number) => {
    return await request.post({ url: `/mb/order/${id}/virtual-delivery/retry` })
  },
  refundOrder: async (data: { id: number; reason?: string; password: string }) => {
    return await request.post({ url: '/mb/order/refund', data })
  },
  getRefundConfig: async () => {
    return await request.get<boolean>({ url: '/mb/order/refund/config' })
  },
  exportOrder: async (params: MbOrderPageReqVO) => {
    return await request.download({ url: '/mb/order/export-excel', params })
  }
}

export type { MbOrderSummaryVO as MbOrderRespVO }
