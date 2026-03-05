import request from '@/config/axios'

// 物料转化规则 VO
export interface MaterialConvertRuleVO {
  id: number // 规则ID
  name: string // 规则名称
  sourceMaterialType: number // 源物料类型
  targetMaterialType: number // 目标物料类型
  ratio: number // 转化比例
  status: number // 状态：0-禁用，1-启用
  description?: string // 规则描述
  createTime?: Date // 创建时间
  updateTime?: Date // 更新时间
}

// 物料转化规则查询参数
export interface MaterialConvertRulePageReqVO {
  name?: string // 规则名称
  sourceMaterialType?: number // 源物料类型
  targetMaterialType?: number // 目标物料类型
  status?: number // 状态
  pageNo?: number // 页码
  pageSize?: number // 每页数量
}

// 物料转化规则 API - 待实现
export const ConvertRuleApi = {
  // 查询物料转化规则分页 - 待后端实现
  getConvertRulePage: async (params: MaterialConvertRulePageReqVO) => {
    console.warn('Material convert rule API not implemented yet')
    // 临时返回空数据，待后端实现实际API
    return { list: [], total: 0 }
  },

  // 查询物料转化规则详情 - 待后端实现
  getConvertRule: async (id: number) => {
    console.warn('Material convert rule detail API not implemented yet')
    return null
  },

  // 新增物料转化规则 - 待后端实现
  createConvertRule: async (data: MaterialConvertRuleVO) => {
    console.warn('Material convert rule create API not implemented yet')
    return null
  },

  // 修改物料转化规则 - 待后端实现
  updateConvertRule: async (data: MaterialConvertRuleVO) => {
    console.warn('Material convert rule update API not implemented yet')
    return null
  },

  // 删除物料转化规则 - 待后端实现
  deleteConvertRule: async (id: number) => {
    console.warn('Material convert rule delete API not implemented yet')
    return null
  }
}

// 导出类型
export type { MaterialConvertRuleVO, MaterialConvertRulePageReqVO }

// 导出单独函数供 * as ConvertRuleApi 导入方式使用
export const getConvertRule = ConvertRuleApi.getConvertRule
export const getConvertRulePage = ConvertRuleApi.getConvertRulePage
export const createConvertRule = ConvertRuleApi.createConvertRule
export const updateConvertRule = ConvertRuleApi.updateConvertRule
export const deleteConvertRule = ConvertRuleApi.deleteConvertRule