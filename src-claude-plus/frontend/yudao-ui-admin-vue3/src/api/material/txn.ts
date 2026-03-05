import request from '@/config/axios'

// 物料流水 VO
export interface MaterialTxnVO {
  id: number // 流水ID
  materialId: number // 物料ID
  materialName?: string // 物料名称（可选）
  materialCode?: string // 物料编码（可选）
  userId: number // 用户ID
  nickname?: string // 用户昵称（可选）
  type: number // 流水类型
  amount: number // 数量
  balance: number // 操作后余额
  remark?: string // 备注
  createTime?: Date // 创建时间
  direction?: number // 方向（-1减少，1增加）
  quantity?: number // 数量
  balanceAfter?: number // 操作后余额
  bizType?: string // 业务类型
  bizKey?: string // 业务唯一标识
  reason?: string // 变更原因
}

// 物料流水查询参数
export interface MaterialTxnPageReqVO {
  userId?: number // 用户ID
  materialId?: number // 物料ID
  type?: number // 流水类型
  direction?: number // 方向（-1减少，1增加）
  pageNo?: number // 页码
  pageSize?: number // 每页数量
}

// 物料流水 API
export const TxnApi = {
  // 查询物料流水分页
  getTxnPage: async (params: MaterialTxnPageReqVO) => {
    return await request.get({ url: '/material/txn/page', params })
  },

  // 查询物料流水详情
  getTxn: async (id: number) => {
    return await request.get({ url: '/material/txn/get?id=' + id })
  },

  // 新增物料流水
  createTxn: async (data: MaterialTxnVO) => {
    return await request.post({ url: '/material/txn/create', data })
  },

  // 修改物料流水
  updateTxn: async (data: MaterialTxnVO) => {
    return await request.put({ url: '/material/txn/update', data })
  },

  // 删除物料流水
  deleteTxn: async (id: number) => {
    return await request.delete({ url: '/material/txn/delete?id=' + id })
  },

  // 导出物料流水 Excel
  exportTxn: async (params) => {
    return await request.download({ url: '/material/txn/export-excel', params })
  }
}

// 导出类型
export type { MaterialTxnVO, MaterialTxnPageReqVO }

// 导出单独函数供 * as TxnApi 导入方式使用
export const getTxnPage = TxnApi.getTxnPage
export const getTxn = TxnApi.getTxn
export const createTxn = TxnApi.createTxn
export const updateTxn = TxnApi.updateTxn
export const deleteTxn = TxnApi.deleteTxn
export const exportTxn = TxnApi.exportTxn