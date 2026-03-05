import request from '@/config/axios'

export interface PayeaseSubMerchantPageReqVO {
  signedName?: string
  mobile?: string
  status?: number
  pageNo: number
  pageSize: number
}

export interface PayeaseSubMerchantRespVO {
  id: number
  signedName: string
  signedShortName: string
  mobile: string
  status: number
  subMerchantId?: string
  rejectReason?: string
  bankAccountNo: string
  bankName: string
  createTime: string
}

export interface PayeaseSubMerchantUpdateReqVO {
  id: number
  mobile?: string
  email?: string
  bankAccountNo?: string
  bankBranchName?: string
  bankCardFrontUrl?: string
  extra?: string
}

export const PayeaseSubMerchantApi = {
  getPage: (params: PayeaseSubMerchantPageReqVO) =>
    request.get({ url: '/pay/payease-sub-merchant/page', params }),
  get: (id: number) =>
    request.get({ url: '/pay/payease-sub-merchant/get', params: { id } }),
  update: (data: PayeaseSubMerchantUpdateReqVO) =>
    request.put({ url: '/pay/payease-sub-merchant/update', data }),
  submit: (id: number) =>
    request.post({ url: '/pay/payease-sub-merchant/submit', params: { id } })
}
