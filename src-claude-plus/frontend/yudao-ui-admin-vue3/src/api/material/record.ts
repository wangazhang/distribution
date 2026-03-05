import request from '@/config/axios'

// 物料变更记录 VO
export interface MaterialRecordVO {
  id: number // 记录ID
  userId: number // 用户ID
  materialId: number // 物料ID
  actionType: number // 操作类型（0-使用 1-获得）
  amount: number // 操作数量
  sourceDesc: string // 来源描述
  beforeAmount: number // 变更前数量
  afterAmount: number // 变更后数量
  createTime: string // 创建时间
  actionDate?: string // 操作日期
  materialName?: string // 物料名称
  materialImage?: string // 物料图片
  actionTypeDesc?: string // 操作类型描述
  sourceTypeDesc?: string // 来源类型描述
}

// 物料变更记录 API
export const MaterialRecordApi = {
  // 获取物料变更记录分页
  getMaterialRecordPage: async (params: any) => {
    return await request.get({ url: `/material/record/page`, params })
  }
}