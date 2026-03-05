import request from '@/config/axios'

export interface CommissionRuleMaterialVO {
  id?: number
  materialId: number
  materialName?: string
  materialCode?: string
  materialImage?: string
  materialUnit?: string
  quantity: number
}

export interface CommissionRuleConditionVO {
  id?: number
  conditionType: string
  operator: string
  value: Record<string, any>
  extra?: Record<string, any>
  priority?: number
}

export interface CommissionRuleActionVO {
  id?: number
  actionType: string
  amountMode?: string
  amountValue?: number
  capValue?: number | null
  payload?: Record<string, any>
  priority: number
}

export interface CommissionRuleEffectScopeVO {
  targetHierarchy?: string
  targetLevel?: string
  amountSource?: string
  fundAccount?: string
  inviteOrder?: {
    operator?: string
    minIndex?: number
    maxIndex?: number
    indexes?: number[]
  }
}

export interface CommissionRuleVO {
  id?: number
  policyId: number
  ruleCode: string
  displayName: string
  priority: number
  status?: string
  versionNo?: number
  remark?: string
  createTime?: string
  effectScope?: CommissionRuleEffectScopeVO
  conditions: CommissionRuleConditionVO[]
  actions: CommissionRuleActionVO[]
  materials?: CommissionRuleMaterialVO[]
}

export const getCommissionRuleList = (policyId: number) => {
  return request.get({ url: '/mb/commission-rule/list', params: { policyId } })
}

export const createCommissionRule = (data: CommissionRuleVO) => {
  return request.post({ url: '/mb/commission-rule/create', data })
}

export const updateCommissionRule = (data: CommissionRuleVO) => {
  return request.put({ url: '/mb/commission-rule/update', data })
}

export const enableCommissionRule = (id: number) => {
  return request.post({ url: '/mb/commission-rule/enable', params: { id } })
}

export const disableCommissionRule = (id: number) => {
  return request.post({ url: '/mb/commission-rule/disable', params: { id } })
}

export const deleteCommissionRule = (id: number) => {
  return request.delete({ url: '/mb/commission-rule/delete', params: { id } })
}

export const cloneCommissionRule = (id: number) => {
  return request.post({ url: '/mb/commission-rule/clone', params: { id } })
}
