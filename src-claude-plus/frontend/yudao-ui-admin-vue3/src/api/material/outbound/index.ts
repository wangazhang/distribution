import request from '@/config/axios'

// 物料出库 VO
export interface MaterialOutboundVO {
  id: number // 出库单ID
  outboundNo: string // 出库单号
  userId: number // 用户ID
  addressId: number // 收货地址ID
  receiverName: string // 收货人姓名
  receiverMobile: string // 收货人手机号
  receiverProvince: string // 收货人省份
  receiverCity: string // 收货人城市
  receiverDistrict: string // 收货人区县
  receiverDetailAddress: string // 收货人详细地址
  status: number // 状态：0-待审核，1-已审核待发货，2-已发货，3-已完成，4-已取消
  remark: string // 备注
  logisticsCode: string // 物流单号
  logisticsCompany: string // 物流公司
  approveTime: Date // 审核时间
  approveUserId: number // 审核人ID
  shipTime: Date // 发货时间
  completeTime: Date // 完成时间
  cancelTime: Date // 取消时间
  cancelReason: string // 取消原因
  createTime: Date // 创建时间
  createBy: string // 创建人/申请人
}

// 物料出库 API
export const OutboundApi = {
  // 查询物料出库分页
  getMaterialOutboundPage: async (params: any) => {
    return await request.get({ url: `/material/outbound/page`, params })
  },

  // 查询物料出库详情
  getMaterialOutbound: async (id: number) => {
    return await request.get({ url: `/material/outbound/get?id=` + id })
  },

  // 新增物料出库
  createMaterialOutbound: async (data: MaterialOutboundVO) => {
    return await request.post({ url: `/material/outbound/create`, data })
  },

  // 修改物料出库
  updateMaterialOutbound: async (data: MaterialOutboundVO) => {
    return await request.put({ url: `/material/outbound/update`, data })
  },

  // 删除物料出库
  deleteMaterialOutbound: async (id: number) => {
    return await request.delete({ url: `/material/outbound/delete?id=` + id })
  },

  // 导出物料出库 Excel
  exportMaterialOutbound: async (params) => {
    return await request.download({ url: `/material/outbound/export-excel`, params })
  },
  
  // 导出待发货物料出库单模板
  exportShippingTemplate: async () => {
    try {
      const data = await request.download({ url: `/material/outbound/export-shipping-template` })
      // 处理下载
      if (data) {
        const url = window.URL.createObjectURL(new Blob([data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', '物料出库待发货模板.xls')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      }
      return data
    } catch (error) {
      console.error('导出模板失败', error)
      throw error
    }
  },
  
  // 导入发货数据
  importShipping: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.upload({
      url: `/material/outbound/import-shipping`,
      data: formData
    })
    return res
  },
  
  // 审核出库单
  approveMaterialOutbound: async (data: { id: number, approved: boolean, reason?: string }) => {
    return await request.put({
      url: `/material/outbound/approve`,
      params: {
        id: data.id,
        approve: data.approved,
        reason: data.reason
      }
    })
  },
  
  // 批量审核通过出库单
  batchApproveMaterialOutbound: async (data: { ids: number[] }) => {
    return await request.post({ url: `/material/outbound/batch-approve`, data })
  },
  
  // 发货出库单
  shipMaterialOutbound: async (data: { id: number, logisticsCompany: string, logisticsCode: string }) => {
    return await request.put({
      url: `/material/outbound/ship`,
      params: {
        id: data.id,
        logisticsCompany: data.logisticsCompany,
        logisticsCode: data.logisticsCode
      }
    })
  },

  // 完成出库单
  completeMaterialOutbound: async (id: number) => {
    return await request.put({ url: `/material/outbound/complete?id=` + id })
  },

  // 取消出库单
  cancelMaterialOutbound: async (data: { id: number, reason: string }) => {
    return await request.put({ url: `/material/outbound/cancel`, data })
  }
}

// 导出类型
export type { MaterialOutboundVO }