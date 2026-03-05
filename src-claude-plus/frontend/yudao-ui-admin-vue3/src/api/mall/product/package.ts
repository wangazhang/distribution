import request from '@/config/axios'

// -------------------- Types --------------------
export interface PackageItemVO {
  itemType: number // 1: 商品 2: 权益 3: 自定义
  itemId: number // 商品：SPU ID；权益：数值编码（1=OPEN_BROKERAGE, 2=SET_LEVEL）
  itemQuantity: number
  extJson?: Record<string, any>
}

export interface PackageCommissionVO {
  level: number // 1/2
  commissionType: number // 1固定金额(分) 2比例(%)
  commissionValue: number // 金额分 或 百分比
  baseType?: number // 1订单项实付 2自定义基准
  baseAmount?: number // 分
}

export interface PackageSaveReqVO {
  id?: number
  name: string
  spuId: number
  status: number // 0草稿 1启用 2禁用
  remark?: string
  items: PackageItemVO[]
  commissions?: PackageCommissionVO[]
}

export interface PackageRespVO extends PackageSaveReqVO {
  id: number
}

// -------------------- API --------------------

// 创建套包
export const createPackage = (data: PackageSaveReqVO) => {
  return request.post({ url: '/product/package/create', data })
}

// 更新套包
export const updatePackage = (data: PackageSaveReqVO) => {
  return request.put({ url: '/product/package/update', data })
}

// 更新套包状态
export const updatePackageStatus = (id: number, status: number) => {
  return request.put({ url: '/product/package/update-status', params: { id, status } })
}

// 删除套包
export const deletePackage = (id: number) => {
  return request.delete({ url: '/product/package/delete', params: { id } })
}

// 获得套包
export const getPackage = (id: number) => {
  return request.get({ url: '/product/package/get', params: { id } })
}

// 获得套包分页
export const getPackagePage = (params: PageParam & { name?: string; spuId?: number; status?: number }) => {
  return request.get({ url: '/product/package/page', params })
}

// 根据 SPU 获得启用中的套包
export const getEnabledPackageBySpu = (spuId: number) => {
  return request.get({ url: '/product/package/get-by-spu', params: { spuId } })
}

