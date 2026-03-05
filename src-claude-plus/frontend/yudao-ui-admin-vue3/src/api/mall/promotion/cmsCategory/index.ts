import request from '@/config/axios'

// CMS 分类 VO
export interface CmsCategoryVO {
  id: number
  sectionId: number
  name: string
  description: string
  icon: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 获得CMS分类分页
export const getCmsCategoryPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-category/page`, params })
}

// 查询CMS分类详情
export const getCmsCategory = async (id: number) => {
  return await request.get({ url: `/promotion/cms-category/get?id=` + id })
}

// 新增CMS分类
export const createCmsCategory = async (data: CmsCategoryVO) => {
  return await request.post({ url: `/promotion/cms-category/create`, data })
}

// 修改CMS分类
export const updateCmsCategory = async (data: CmsCategoryVO) => {
  return await request.put({ url: `/promotion/cms-category/update`, data })
}

// 删除CMS分类
export const deleteCmsCategory = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-category/delete?id=` + id })
}

// 获得CMS分类列表
export const getCmsCategoryList = async (ids?: number[]) => {
  return await request.get({
    url: `/promotion/cms-category/list`,
    params: ids ? { ids: ids.join(',') } : {}
  })
}

// 获得用户端CMS分类列表
export const getAppCmsCategoryList = async (sectionId?: number) => {
  return await request.get({
    url: `/promotion/cms-category/app/list`,
    params: sectionId ? { sectionId } : {}
  })
}