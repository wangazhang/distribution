import request from '@/config/axios'

// 物料转化 VO
export interface MaterialConvertVO {
  id: number // 转化ID
  userId: number // 用户ID
  sourceMaterialId: number // 源物料ID
  targetMaterialId: number // 目标物料ID
  status: number // 状态
  createTime?: Date // 创建时间
}

// 物料转化查询参数
export interface MaterialConvertPageReqVO {
  id?: number // 转化ID
  userId?: number // 用户ID
  sourceMaterialId?: number // 源物料ID
  targetMaterialId?: number // 目标物料ID
  status?: number // 状态
  pageNo?: number // 页码
  pageSize?: number // 每页数量
}

// 物料转化 API - 待实现
export const ConvertApi = {
  // 查询物料转化分页 - 待后端实现
  getConvertPage: async (params: MaterialConvertPageReqVO) => {
    console.warn('Material convert API not implemented yet')
    // 临时返回空数据，待后端实现实际API
    return { list: [], total: 0 }
  },

  // 查询物料转化详情 - 待后端实现
  getConvert: async (id: number) => {
    console.warn('Material convert detail API not implemented yet')
    return null
  },

  // 新增物料转化 - 待后端实现
  createConvert: async (data: MaterialConvertVO) => {
    console.warn('Material convert create API not implemented yet')
    return null
  },

  // 修改物料转化 - 待后端实现
  updateConvert: async (data: MaterialConvertVO) => {
    console.warn('Material convert update API not implemented yet')
    return null
  },

  // 删除物料转化 - 待后端实现
  deleteConvert: async (id: number) => {
    console.warn('Material convert delete API not implemented yet')
    return null
  },

  // 取消物料转化 - 待后端实现
  cancelConvert: async (id: number) => {
    console.warn('Material convert cancel API not implemented yet')
    return null
  }
}

// 导出类型
export type { MaterialConvertVO, MaterialConvertPageReqVO }

// 导出单独函数供 * as ConvertApi 导入方式使用
export const getConvert = ConvertApi.getConvert
export const getConvertPage = ConvertApi.getConvertPage
export const createConvert = ConvertApi.createConvert
export const updateConvert = ConvertApi.updateConvert
export const deleteConvert = ConvertApi.deleteConvert
export const cancelConvert = ConvertApi.cancelConvert