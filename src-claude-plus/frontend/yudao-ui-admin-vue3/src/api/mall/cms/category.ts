import request from '@/config/axios'

// CMS分类 VO
export interface CmsCategoryVO {
  id?: number
  sectionId: number
  name: string
  icon?: string
  sort?: number
  status?: number
}

// 查询分类分页
export const getCmsCategoryPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-category/page`, params })
}

// 查询分类详情
export const getCmsCategory = async (id: number) => {
  return await request.get({ url: `/promotion/cms-category/get?id=` + id })
}

// 查询分类列表
export const getCmsCategoryList = async (ids?: number[]) => {
  return await request.get({
    url: `/promotion/cms-category/list`,
    params: ids ? { ids: ids.join(',') } : {}
  })
}

// 根据板块ID查询分类列表
export const getCmsCategoryListBySectionId = async (sectionId: number) => {
  return await request.get({ url: `/promotion/cms-category/list-by-section?sectionId=` + sectionId })
}

// 新增分类
export const createCmsCategory = async (data: CmsCategoryVO) => {
  return await request.post({ url: `/promotion/cms-category/create`, data })
}

// 修改分类
export const updateCmsCategory = async (data: CmsCategoryVO) => {
  return await request.put({ url: `/promotion/cms-category/update`, data })
}

// 删除分类
export const deleteCmsCategory = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-category/delete?id=` + id })
}
