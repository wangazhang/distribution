import request from '@/config/axios'

export interface MaterialOutboundAddressVO {
  id?: number
  outboundId?: number
  recipientName?: string
  recipientPhone?: string
  recipientAddress?: string
  province?: string
  city?: string
  area?: string
  detailAddress?: string
  zipCode?: string
  createTime?: Date
}

export interface MaterialOutboundAddressPageReqVO extends PageParam {
  outboundId?: number
  recipientName?: string
  recipientPhone?: string
  createTime?: Date[]
}

export interface MaterialOutboundAddressExportReqVO {
  outboundId?: number
  recipientName?: string
  recipientPhone?: string
  createTime?: Date[]
}

// 获得物料出库地址分页
export const getMaterialOutboundAddressPage = (params: MaterialOutboundAddressPageReqVO) => {
  return request.get({ url: `/material/outbound-address/page`, params })
}

// 获得物料出库地址
export const getMaterialOutboundAddress = (id: number) => {
  return request.get({ url: `/material/outbound-address/get?id=` + id })
}

// 新增物料出库地址
export const createMaterialOutboundAddress = (data: MaterialOutboundAddressVO) => {
  return request.post({ url: `/material/outbound-address/create`, data })
}

// 修改物料出库地址
export const updateMaterialOutboundAddress = (data: MaterialOutboundAddressVO) => {
  return request.put({ url: `/material/outbound-address/update`, data })
}

// 删除物料出库地址
export const deleteMaterialOutboundAddress = (id: number) => {
  return request.delete({ url: `/material/outbound-address/delete?id=` + id })
}

// 导出物料出库地址 Excel
export const exportMaterialOutboundAddressExcel = (params: MaterialOutboundAddressExportReqVO) => {
  return request.download({ url: `/material/outbound-address/export-excel`, params })
}

// 兼容旧API名称的别名
export const exportMaterialOutboundAddress = exportMaterialOutboundAddressExcel

// 物料出库地址 API 对象
export const MaterialOutboundAddressApi = {
  getMaterialOutboundAddressPage,
  getMaterialOutboundAddress,
  createMaterialOutboundAddress,
  updateMaterialOutboundAddress,
  deleteMaterialOutboundAddress,
  exportMaterialOutboundAddressExcel,
  exportMaterialOutboundAddress // 兼容旧名称
}