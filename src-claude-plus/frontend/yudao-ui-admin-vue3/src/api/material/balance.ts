import request from '@/config/axios'

// 物料余额 VO
export interface MaterialBalanceVO {
  id: number // 余额记录ID
  userId: number // 用户ID
  materialId: number // 物料ID
  balance: number // 物料余额
  materialName?: string // 物料名称
  materialImage?: string // 物料图片
  nickname?: string // 用户昵称
  updateTime?: string // 更新时间
  createTime?: string // 创建时间
}

// 用户物料库存 VO
export interface UserMaterialStockVO {
  materialId: number // 物料ID
  materialName: string // 物料名称
  materialImage: string // 物料图片
  stock: number // 库存数量
  unit: string // 单位
}

// 物料余额 API
export const MaterialBalanceApi = {
  // 获得用户物料余额分页
  getUserBalancePage: async (params: any) => {
    return await request.get({ url: `/material/balance/page`, params })
  },

  // 获得用户物料余额详情
  getUserBalance: async (params: any) => {
    return await request.get({ url: `/material/balance/get`, params })
  },

  // 批量查询用户物料库存
  batchQueryUserStock: async (data: any) => {
    return await request.post({ url: `/material/balance/batch-query`, data })
  },

  // 获取可出库的物料列表
  getOutboundMaterials: async () => {
    return await request.get({ url: `/material/balance/outbound-materials` })
  },

  // 获取指定用户的所有物料余额
  getUserMaterials: async (userId: number) => {
    return await request.get({ url: `/material/balance/user-materials`, params: { userId } })
  }
}

// 导出单独的函数供 * as BalanceApi 导入方式使用
export const getBalancePage = MaterialBalanceApi.getUserBalancePage
export const getUserBalancePage = MaterialBalanceApi.getUserBalancePage
export const getUserBalance = MaterialBalanceApi.getUserBalance
export const batchQueryUserStock = MaterialBalanceApi.batchQueryUserStock
export const getOutboundMaterials = MaterialBalanceApi.getOutboundMaterials

// 为直接导入 { BalanceApi } 提供支持
export const BalanceApi = MaterialBalanceApi
