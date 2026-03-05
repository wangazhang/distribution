import request from '@/config/axios'

// 物料定义 VO
export interface MaterialDefinitionVO {
  id: number // 物料ID
  name: string | null // 物料名称（映射模式为null）
  code: string // 物料编码
  spuId: number | null // 关联SPU ID
  spuName?: string | null // 关联SPU名称
  image?: string | null // 物料图片（映射模式为null）
  description?: string | null // 物料描述（映射模式为null）
  baseUnit: string // 基本单位
  type: number // 物料类型：1-半成品 2-成品
  linkMode?: number | null // SPU关联模式：1-映射 2-快照（仅成品）
  autoSync?: boolean | null // 是否自动同步（已废弃）
  spuSnapshot?: string | null // SPU信息快照
  convertStatus?: number | null // 转化状态
  convertedSpuId?: number | null // 转化后的SPU ID
  convertedSpuName?: string | null // 转化目标SPU名称
  convertPrice?: number | null // 转化单价（分）
  supportOutbound: boolean // 是否支持出库
  supportConvert: boolean // 是否支持转化
  attrs?: Record<string, any> // 扩展属性
  status: number // 状态：0-禁用，1-启用
  createTime?: Date // 创建时间
  updateTime?: Date // 更新时间
}

// 物料定义查询参数
export interface MaterialDefinitionPageReqVO {
  name?: string // 物料名称
  code?: string // 物料编码
  type?: number // 物料类型
  status?: number // 状态
  pageNo?: number // 页码
  pageSize?: number // 每页数量
}

// 物料定义 API
export const DefinitionApi = {
  // 创建物料定义
  createDefinition: async (data: MaterialDefinitionVO) => {
    return await request.post({ url: '/material/definition/create', data })
  },

  // 更新物料定义
  updateDefinition: async (data: MaterialDefinitionVO) => {
    return await request.put({ url: '/material/definition/update', data })
  },

  // 删除物料定义
  deleteDefinition: async (id: number) => {
    return await request.delete({ url: '/material/definition/delete?id=' + id })
  },

  // 查询物料定义详情
  getDefinition: async (id: number) => {
    return await request.get({ url: '/material/definition/get?id=' + id })
  },

  // 查询物料定义分页
  getDefinitionPage: async (params: MaterialDefinitionPageReqVO) => {
    return await request.get({ url: '/material/definition/page', params })
  },

  // 导出物料定义 Excel
  exportDefinition: async (params) => {
    return await request.download({ url: '/material/definition/export-excel', params })
  },

  // 根据SPU ID获取商品信息（用于自动填充）
  getSpuInfo: async (spuId: number) => {
    return await request.get({ url: '/material/definition/get-spu-info', params: { spuId } })
  },

  // 更新物料的SPU快照（仅快照模式）
  updateSpuSnapshot: async (id: number) => {
    return await request.post({ url: '/material/definition/update-snapshot', params: { id } })
  }
}

// 导出类型
export type { MaterialDefinitionVO, MaterialDefinitionPageReqVO }

// 导出单独函数供 * as DefinitionApi 导入方式使用
export const getDefinition = DefinitionApi.getDefinition
export const createDefinition = DefinitionApi.createDefinition
export const updateDefinition = DefinitionApi.updateDefinition
export const deleteDefinition = DefinitionApi.deleteDefinition
export const getDefinitionPage = DefinitionApi.getDefinitionPage
export const exportDefinition = DefinitionApi.exportDefinition
export const getSpuInfo = DefinitionApi.getSpuInfo
export const updateSpuSnapshot = DefinitionApi.updateSpuSnapshot
