import request from '@/config/axios'

// CMS板块 VO
export interface CmsSectionVO {
  id?: number
  name: string
  type?: string // article-文章类, dynamic-动态类, course-课程类, custom-自定义
  description?: string
  layoutStyle?: string
  detailButtonConfig?: Record<string, any>
  cardButtonConfig?: Record<string, any>
  userContentProduction?: boolean
  iconUrl?: string
  articleCount?: number
  createTime?: Date
}

// 查询板块分页
export const getCmsSectionPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-section/page`, params })
}

// 查询板块详情
export const getCmsSection = async (id: number) => {
  return await request.get({ url: `/promotion/cms-section/get?id=` + id })
}

// 获取板块简单列表（用于下拉选择）
export const getCmsSectionSimpleList = async () => {
  return await request.get({ url: `/promotion/cms-section/list` })
}

// 新增板块
export const createCmsSection = async (data: CmsSectionVO) => {
  return await request.post({ url: `/promotion/cms-section/create`, data })
}

// 修改板块
export const updateCmsSection = async (data: CmsSectionVO) => {
  return await request.put({ url: `/promotion/cms-section/update`, data })
}

// 删除板块
export const deleteCmsSection = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-section/delete?id=` + id })
}

// 获取板块下的文章数量
export const getCmsSectionArticleCount = async (sectionId: number) => {
  return await request.get({ url: `/promotion/cms-section/article-count`, params: { sectionId } })
}