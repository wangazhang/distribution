import request from '@/config/axios'

export interface MaterialOutboundVO {
  id?: number
  outboundCode?: string
  userId?: number
  userName?: string
  outboundType?: number
  status?: number
  auditUserId?: number
  auditUser?: string
  auditTime?: Date
  shippingDate?: Date
  shippingMethod?: string
  trackingNumber?: string
  remarks?: string
  createTime?: Date
}

export interface MaterialOutboundPageReqVO extends PageParam {
  outboundCode?: string
  userId?: number
  outboundType?: number
  status?: number
  auditUserId?: number
  shippingDate?: Date[]
  createTime?: Date[]
}

export interface MaterialOutboundExportReqVO {
  outboundCode?: string
  userId?: number
  outboundType?: number
  status?: number
  auditUserId?: number
  shippingDate?: Date[]
  createTime?: Date[]
}

// 获得物料出库分页
export const getMaterialOutboundPage = (params: MaterialOutboundPageReqVO) => {
  return request.get({ url: `/material/outbound/page`, params })
}

// 获得物料出库
export const getMaterialOutbound = (id: number) => {
  return request.get({ url: `/material/outbound/get?id=` + id })
}

// 新增物料出库
export const createMaterialOutbound = (data: MaterialOutboundVO) => {
  return request.post({ url: `/material/outbound/create`, data })
}

// 修改物料出库
export const updateMaterialOutbound = (data: MaterialOutboundVO) => {
  return request.put({ url: `/material/outbound/update`, data })
}

// 删除物料出库
export const deleteMaterialOutbound = (id: number) => {
  return request.delete({ url: `/material/outbound/delete?id=` + id })
}

// 导出物料出库 Excel
export const exportMaterialOutboundExcel = (params: MaterialOutboundExportReqVO) => {
  return request.download({ url: `/material/outbound/export-excel`, params })
}

// 下载发货模板
export const downloadShippingTemplate = async () => {
  try {
    const data = await request.download({ url: `/material/outbound/export-shipping-template` })

    // 检查返回的数据大小,如果太小可能是空模板
    if (data && data.size < 1024) {
      console.warn('下载的模板文件较小,可能没有待发货数据')
    }

    const blob = new Blob([data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '物料出库发货模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载发货模板失败:', error)
    throw error
  }
}

// 导入发货信息
export const importShippingInfo = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.upload({
    url: `/material/outbound/import-shipping`,
    data: formData
  })
}

// 审批物料出库
export const approveMaterialOutbound = (data: any) => {
  return request.post({ url: `/material/outbound/approve`, data })
}

// 批量审批物料出库
export const batchApproveMaterialOutbound = (data: any) => {
  return request.post({ url: `/material/outbound/batch-approve`, data })
}

// 发货物料出库
export const shipMaterialOutbound = (data: any) => {
  return request.post({ url: `/material/outbound/ship`, data })
}

// 物料出库 API 对象
export const MaterialOutboundApi = {
  getMaterialOutboundPage,
  getMaterialOutbound,
  createMaterialOutbound,
  updateMaterialOutbound,
  deleteMaterialOutbound,
  exportMaterialOutboundExcel,
  downloadShippingTemplate,
  exportShippingTemplate: downloadShippingTemplate, // 导出发货模板别名
  importShippingInfo,
  importShipping: importShippingInfo, // 导入发货信息别名
  approveMaterialOutbound,
  batchApproveMaterialOutbound,
  shipMaterialOutbound
}

// 为组件提供简化的函数名别名
export const getOutbound = getMaterialOutbound
export const getOutboundPage = getMaterialOutboundPage
export const createOutbound = createMaterialOutbound
export const updateOutbound = updateMaterialOutbound
export const deleteOutbound = deleteMaterialOutbound
export const exportOutbound = exportMaterialOutboundExcel
export const approveOutbound = approveMaterialOutbound
export const batchApproveOutbound = batchApproveMaterialOutbound
export const shipOutbound = shipMaterialOutbound
export const completeOutbound = shipMaterialOutbound // 完成出库等同于发货

// 取消出库 - 待后端实现
export const cancelOutbound = async (data: any) => {
  console.warn('Cancel outbound API not implemented yet')
  return null
}