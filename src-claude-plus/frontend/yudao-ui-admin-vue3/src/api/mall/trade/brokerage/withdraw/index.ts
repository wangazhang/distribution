import request from '@/config/axios'

export interface BrokerageWithdrawVO {
  id: number
  userId: number
  price: number
  feePrice: number
  totalPrice: number
  type: number
  name: string
  accountNo: string
  bankName: string
  bankAddress: string
  accountQrCodeUrl: string
  status: number
  auditReason: string
  auditTime: Date
  remark: string
  channelPayEnabled?: boolean
  channelPayLocked?: boolean
}

export interface BrokerageWithdrawConfirmPayVO {
  id: number
  remark?: string
}

export interface BrokerageWithdrawChannelTransferStageVO {
  stage: string
  status: string
  statusName: string
  channelTransferNo: string
  requestId: string
  errorCode: string
  errorMessage: string
  successTime: string
}

export interface BrokerageWithdrawChannelTransferDetailVO {
  id: number
  status: number
  statusName: string
  price: number
  channelTransferNo: string
  channelErrorCode: string
  channelErrorMsg: string
  channelExtras?: Record<string, string>
  channelNotifyData?: string
  successTime: string
  createTime: string
  updateTime: string
  transferStage?: BrokerageWithdrawChannelTransferStageVO
  withdrawStage?: BrokerageWithdrawChannelTransferStageVO
}

export interface BrokerageWithdrawChannelTransferRespVO {
  withdrawId: number
  withdrawStatus: number
  withdrawType: number
  withdrawPrice: number
  channelPayEnabled: boolean
  channelRetryPasswordRequired: boolean
  transfer?: BrokerageWithdrawChannelTransferDetailVO
}

// 查询佣金提现列表
export const getBrokerageWithdrawPage = async (params: any) => {
  return await request.get({ url: `/trade/brokerage-withdraw/page`, params })
}

// 查询佣金提现详情
export const getBrokerageWithdraw = async (id: number) => {
  return await request.get({ url: `/trade/brokerage-withdraw/get?id=` + id })
}

// 佣金提现 - 通过申请
export const approveBrokerageWithdraw = async (id: number) => {
  return await request.put({ url: `/trade/brokerage-withdraw/approve?id=` + id })
}

// 审核佣金提现 - 驳回申请
export const rejectBrokerageWithdraw = async (data: BrokerageWithdrawVO) => {
  return await request.put({ url: `/trade/brokerage-withdraw/reject`, data })
}

// 导出佣金提现
export const exportBrokerageWithdraw = (params: any) => {
  return request.download({ url: `/trade/brokerage-withdraw/export-excel`, params })
}

// 导出待打款提现
export const exportFinanceBrokerageWithdraw = (params: any) => {
  return request.download({ url: `/trade/brokerage-withdraw/export-finance-excel`, params })
}

// 财务确认打款
export const confirmFinancePay = async (data: BrokerageWithdrawConfirmPayVO) => {
  return await request.put({ url: `/trade/brokerage-withdraw/confirm-pay`, data })
}

// 渠道打款
export const channelPay = async (id: number) => {
  return await request.put({ url: `/trade/brokerage-withdraw/channel-pay?id=` + id })
}

// 渠道打款详情
export const getChannelTransferDetail = async (id: number) => {
  return await request.get<BrokerageWithdrawChannelTransferRespVO>({
    url: `/trade/brokerage-withdraw/channel-transfer`,
    params: { id }
  })
}

// 渠道打款手动同步
export const syncChannelTransfer = async (id: number) => {
  return await request.post<BrokerageWithdrawChannelTransferRespVO>({
    url: `/trade/brokerage-withdraw/channel-transfer/sync?id=` + id
  })
}

// 渠道打款重新发起
export const retryChannelPay = async (data: { id: number; password?: string }) => {
  return await request.post<BrokerageWithdrawChannelTransferRespVO>({
    url: `/trade/brokerage-withdraw/channel-pay/retry`,
    data
  })
}

// 导入财务打款结果
export const importFinanceResult = async (formData: FormData) => {
  return await request.upload({
    url: '/trade/brokerage-withdraw/import-finance-result',
    data: formData
  })
}
