import request from '@/config/axios'

export interface MaterialVO {
  id?: number
  name?: string
  code?: string
  description?: string
  unit?: string
  status?: number
  createTime?: Date
}

export interface MaterialPageReqVO extends PageParam {
  name?: string
  code?: string
  status?: number
  createTime?: Date[]
}

export interface MaterialExportReqVO {
  name?: string
  code?: string
  status?: number
  createTime?: Date[]
}

// 创建物料
export const createMaterial = (data: MaterialVO) => {
  return request.post({ url: '/material/material/create', data })
}

// 更新物料
export const updateMaterial = (data: MaterialVO) => {
  return request.put({ url: '/material/material/update', data })
}

// 删除物料
export const deleteMaterial = (id: number) => {
  return request.delete({ url: '/material/material/delete', data: id })
}

// 获得物料
export const getMaterial = (id: number) => {
  return request.get<MaterialVO>({ url: '/material/material/get?id=' + id })
}

// 获得物料分页
export const getMaterialPage = (params: MaterialPageReqVO) => {
  return request.get<PageResult<MaterialVO>>({
    url: '/material/material/page',
    params
  })
}

// 导出物料 Excel
export const exportMaterialExcel = (params: MaterialExportReqVO) => {
  return request.download({
    url: '/material/material/export-excel',
    params
  })
}

// 获得物料精简信息列表
export const getMaterialSimpleList = () => {
  return request.get<MaterialVO[]>({
    url: '/material/material/list'
  })
}

// 为组件提供别名函数
export const getMaterialList = getMaterialSimpleList