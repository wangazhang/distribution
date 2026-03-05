import request from '@/config/axios'

export interface BrokerageUserVO {
  id: number
  nickname: string
  avatar: string
  bindUserId?: number
  bindUserNickname?: string
  brokerageUserCount: number
  brokerageOrderCount: number
  brokerageOrderPrice: number
  withdrawPrice: number
  withdrawCount: number
  price: number
  frozenPrice: number
  brokerageEnabled: boolean
  levelName?: string
  brokerageTime?: Date
  bindUserTime?: Date
  createTime?: Date
}

export interface BrokerageUserPageReqVO extends PageParam {
  bindUserId?: number
  userId?: number
  brokerageEnabled?: boolean
  createTime?: Date[]
}

export interface BrokerageUserUpdateBindUserReqVO {
  id: number
  bindUserId: number
}

export interface BrokerageUserClearBindUserReqVO {
  id: number
}

export interface BrokerageUserUpdateEnabledReqVO {
  id: number
  enabled: boolean
}

// 团队关系图谱数据结构
export interface TeamGraphNode {
  id: string
  nickname: string
  avatar: string
  levelName: string
  totalCommission: number
  childCount: number
  createTime: string
  children?: TeamGraphNode[]
}

export interface TeamGraphVO {
  rootNode: TeamGraphNode
  totalCount: number
  levelCount: number
  totalCommission: number
}

// 获得分销用户分页
export const getBrokerageUserPage = async (params: BrokerageUserPageReqVO) => {
  return await request.get({ url: `/trade/brokerage-user/page`, params })
}

// 修改推广员绑定
export const updateBindUser = async (data: BrokerageUserUpdateBindUserReqVO) => {
  return await request.put({ url: `/trade/brokerage-user/update-bind-user`, data })
}

// 清除推广员绑定
export const clearBindUser = async (data: BrokerageUserClearBindUserReqVO) => {
  return await request.delete({ url: `/trade/brokerage-user/clear-bind-user`, data })
}

// 修改推广资格
export const updateBrokerageEnabled = async (data: BrokerageUserUpdateEnabledReqVO) => {
  return await request.put({ url: `/trade/brokerage-user/update-brokerage-enabled`, data })
}

// 获得分销用户团队关系图谱
export const getTeamGraph = async (userId: number) => {
  return await request.get({ url: `/trade/brokerage-user/team-graph/${userId}` })
}

// 获得分销用户的推广人分页
export const getBrokerageUserChildPage = async (params: any) => {
  return await request.get({ url: `/trade/brokerage-user/child-page`, params })
}

// 获得分销用户的推广订单分页
export const getBrokerageOrderPage = async (params: any) => {
  return await request.get({ url: `/trade/brokerage-record/page`, params })
}

// 创建分销用户
export const createBrokerageUser = async (data: BrokerageUserVO) => {
  return await request.post({ url: `/trade/brokerage-user/create`, data })
}

// 获得分销用户详情
export const getBrokerageUser = async (id: number) => {
  return await request.get<BrokerageUserVO>({ url: `/trade/brokerage-user/get?id=` + id })
}