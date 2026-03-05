import request from '@/config/axios'

export interface CommissionPolicyVO {
  id?: number
  policyCode: string
  displayName: string
  status?: string
  versionNo: number
  bizType: string
  applicableLevel?: string | null
  applicableProductIds?: number[] | null
  applicablePackageIds?: number[] | null
  remark?: string
  createTime?: string
}

export interface CommissionPolicyPageReqVO {
  pageNo: number
  pageSize: number
  keyword?: string
  status?: string
  bizType?: string
}

export interface CommissionSimulationReqVO {
  policyId?: number
  policyCode?: string
  selfLevel?: number
  parentLevel?: number
  grandParentLevel?: number
  teamLevel?: number
  productId?: number
  packageId?: number
  agentUserId?: number
  parentUserId?: number
  grandParentUserId?: number
  teamUserId?: number
  hqUserId?: number
  unitPrice?: number
  quantity?: number
  totalPrice?: number
}

export interface CommissionSimulationRuleRespVO {
  ruleId: number
  ruleCode: string
  displayName: string
  bizCode: number
  fundBizCode?: number
  targetHierarchy?: string
  targetLevel?: string
  amountSource?: string
  amountMode?: string
  amountValue?: number
  fundAccount?: string
  priority: number
  userId: number
  amount: number
  materials?: CommissionSimulationRuleMaterialRespVO[]
}

export interface CommissionSimulationRuleMaterialRespVO {
  materialId: number
  materialName?: string
  materialCode?: string
  materialImage?: string
  materialUnit?: string
  quantity: number
}

export interface CommissionSimulationRespVO {
  policyId: number
  policyCode: string
  policyName: string
  versionNo: number
  orderTotalPrice: number
  unitPrice: number
  quantity: number
  totalCommission: number
  rules: CommissionSimulationRuleRespVO[]
}

export const getCommissionPolicyPage = (params: CommissionPolicyPageReqVO) => {
  return request.get({ url: '/mb/commission-policy/page', params })
}

export const getCommissionPolicy = (id: number) => {
  return request.get({ url: '/mb/commission-policy/get', params: { id } })
}

export const createCommissionPolicy = (data: CommissionPolicyVO) => {
  return request.post({ url: '/mb/commission-policy/create', data })
}

export const updateCommissionPolicy = (data: CommissionPolicyVO) => {
  return request.put({ url: '/mb/commission-policy/update', data })
}

export const publishCommissionPolicy = (id: number) => {
  return request.post({ url: '/mb/commission-policy/publish', params: { id } })
}

export const offlineCommissionPolicy = (id: number) => {
  return request.post({ url: '/mb/commission-policy/offline', params: { id } })
}

export const cloneCommissionPolicy = (id: number) => {
  return request.post({ url: '/mb/commission-policy/clone', params: { id } })
}

export const getOnlineCommissionPolicyList = () => {
  return request.get({ url: '/mb/commission-policy/list-online' })
}

export const simulateCommissionPolicy = (data: CommissionSimulationReqVO) => {
  return request.post({ url: '/mb/commission-policy/simulate', data })
}
