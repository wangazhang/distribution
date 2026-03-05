import request from '@/config/axios'

export interface MaterialRestockPriceVO {
  id?: number
  productId: number
  productName?: string
  levelId: number
  levelName?: string
  restockPrice: number
  status: number
  createTime?: Date
  updateTime?: Date
}

export interface MaterialRestockPricePageReqVO {
  pageNo: number
  pageSize: number
  productId?: number
  levelId?: number
  status?: number
  createTime?: [Date, Date]
}

export const getMaterialRestockPricePage = async (params: MaterialRestockPricePageReqVO) => {
  return await request.get<PageResult<MaterialRestockPriceVO>>({
    url: '/mb/material-restock-price/page',
    params
  })
}

export const getMaterialRestockPrice = async (id: number) => {
  return await request.get<MaterialRestockPriceVO>({
    url: `/mb/material-restock-price/get`,
    params: { id }
  })
}

export const createMaterialRestockPrice = async (data: MaterialRestockPriceVO) => {
  return await request.post({
    url: '/mb/material-restock-price/create',
    data
  })
}

export const updateMaterialRestockPrice = async (data: MaterialRestockPriceVO) => {
  return await request.put({
    url: '/mb/material-restock-price/update',
    data
  })
}

export const deleteMaterialRestockPrice = async (id: number) => {
  return await request.delete({
    url: '/mb/material-restock-price/delete',
    params: { id }
  })
}

export const exportMaterialRestockPrice = async (params: MaterialRestockPricePageReqVO) => {
  return await request.download({
    url: '/mb/material-restock-price/export-excel',
    params
  })
}
