import request from '@/config/axios'

export interface SettleAccountPageReqVO {
  signedName?: string
  mobile?: string
  status?: number
  pageNo: number
  pageSize: number
}

export interface SettleAccountRespVO {
  id: number
  userId: number
  signedName: string
  mobile: string
  status: number
  subMerchantId?: string
  requestId?: string
  rejectReason?: string
  bankAccountNo: string
  bankName: string
  bankBranchName?: string
  provinceCode?: string
  cityCode?: string
  areaCode?: string
  address?: string
  receiverAddress?: string
  email?: string
  idCardFrontUrl?: string
  idCardBackUrl?: string
  bankCardFrontUrl?: string
  createTime: string
}

export interface SettleAccountUpdateReqVO {
  id: number
  mobile?: string
  email?: string
  bankAccountNo?: string
  bankBranchName?: string
  bankCardFrontUrl?: string
  address?: string
  provinceCode?: string
  cityCode?: string
  areaCode?: string
  extra?: string
}

export const SettleAccountApi = {
  getPage: (params: SettleAccountPageReqVO) =>
    request.get({ url: '/pay/settle-account/page', params }),
  get: (id: number) =>
    request.get({ url: '/pay/settle-account/get', params: { id } }),
  update: (data: SettleAccountUpdateReqVO) =>
    request.put({ url: '/pay/settle-account/update', data }),
  submit: (id: number) =>
    request.post({ url: '/pay/settle-account/submit', params: { id } }),
  sync: (id: number) =>
    request.post({ url: '/pay/settle-account/sync', params: { id } })
}
