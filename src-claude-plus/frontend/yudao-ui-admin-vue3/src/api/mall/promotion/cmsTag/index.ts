import request from '@/config/axios'

// CMS 标签 VO
export interface CmsTagVO {
  id: number
  name: string
  description: string
  color: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 获得CMS标签分页
export const getCmsTagPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-tag/page`, params })
}

// 查询CMS标签详情
export const getCmsTag = async (id: number) => {
  return await request.get({ url: `/promotion/cms-tag/get?id=` + id })
}

// 新增CMS标签
export const createCmsTag = async (data: CmsTagVO) => {
  return await request.post({ url: `/promotion/cms-tag/create`, data })
}

// 修改CMS标签
export const updateCmsTag = async (data: CmsTagVO) => {
  return await request.put({ url: `/promotion/cms-tag/update`, data })
}

// 删除CMS标签
export const deleteCmsTag = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-tag/delete?id=` + id })
}

// 获得CMS标签列��
export const getCmsTagList = async (ids?: number[]) => {
  return await request.get({
    url: `/promotion/cms-tag/list`,
    params: ids ? { ids: ids.join(',') } : {}
  })
}

// 获得用户端CMS标签列表
export const getAppCmsTagList = async () => {
  return await request.get({ url: `/promotion/cms-tag/app/list` })
}

// 根据分类ID获得CMS标签列表
export const getCmsTagListByCategoryId = async (categoryId: number) => {
  return await request.get({ url: `/promotion/cms-tag/list-by-category?categoryId=` + categoryId })
}