import request from '@/config/axios'

export interface MaterialOutboundItemVO {
  id?: number
  outboundId?: number
  materialId?: number
  materialName?: string
  materialCode?: string
  materialImage?: string
  requestQuantity?: number
  actualQuantity?: number
  price?: number
  totalPrice?: number
  remarks?: string
  createTime?: Date
}

export interface MaterialOutboundItemPageReqVO extends PageParam {
  outboundId?: number
  materialId?: number
  createTime?: Date[]
}

export interface MaterialOutboundItemExportReqVO {
  outboundId?: number
  materialId?: number
  createTime?: Date[]
}

// 创建物料出库明细
export const createMaterialOutboundItem = (data: MaterialOutboundItemVO) => {
  return request.post({ url: '/material/outbound-item/create', data })
}

// 更新物料出库明细
export const updateMaterialOutboundItem = (data: MaterialOutboundItemVO) => {
  return request.put({ url: '/material/outbound-item/update', data })
}

// 删除物料出库明细
export const deleteMaterialOutboundItem = (id: number) => {
  return request.delete({ url: '/material/outbound-item/delete', data: id })
}

// 获得物料出库明细
export const getMaterialOutboundItem = (id: number) => {
  return request.get<MaterialOutboundItemVO>({ url: '/material/outbound-item/get?id=' + id })
}

// 获得物料出库明细分页
export const getMaterialOutboundItemPage = (params: MaterialOutboundItemPageReqVO) => {
  return request.get<PageResult<MaterialOutboundItemVO>>({
    url: '/material/outbound-item/page',
    params
  })
}

// 导出物料出库明细 Excel
export const exportMaterialOutboundItemExcel = (params: MaterialOutboundItemExportReqVO) => {
  return request.download({
    url: '/material/outbound-item/export-excel',
    params
  })
}

// 根据出库单ID获取出库明细列表
export const getMaterialOutboundItemListByOutboundId = (outboundId: number) => {
  return request.get<MaterialOutboundItemVO[]>({
    url: '/material/outbound-item/list-by-outbound-id',
    params: { outboundId }
  })
}

// 兼容旧API名称的别名
export const exportMaterialOutboundItem = exportMaterialOutboundItemExcel

// 物料出库明细 API 对象
export const MaterialOutboundItemApi = {
  createMaterialOutboundItem,
  updateMaterialOutboundItem,
  deleteMaterialOutboundItem,
  getMaterialOutboundItem,
  getMaterialOutboundItemPage,
  exportMaterialOutboundItemExcel,
  exportMaterialOutboundItem, // 兼容旧名称
  getMaterialOutboundItemListByOutboundId
}