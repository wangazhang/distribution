import request from '@/config/axios'

// CMS 文章 VO
export interface CmsArticleVO {
  id: number
  sectionId: number
  categoryId: number
  title: string
  summary: string
  content: string
  author: string
  authorType: number
  authorId: number
  picUrl: string
  publishTime: string
  auditStatus: string
  publishStatus: number
  viewCount: number
  likeCount: number
  collectCount: number
  shareCount: number
  isHot: number
  isOfficial: number
  sort: number
  status: number
  createTime: string
  updateTime: string
}

// 获得CMS文章分页
export const getCmsArticlePage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-article/page`, params })
}

// 查询CMS文章详情
export const getCmsArticle = async (id: number) => {
  return await request.get({ url: `/promotion/cms-article/get?id=` + id })
}

// 新增CMS文章
export const createCmsArticle = async (data: CmsArticleVO) => {
  return await request.post({ url: `/promotion/cms-article/create`, data })
}

// 修改CMS文章
export const updateCmsArticle = async (data: CmsArticleVO) => {
  return await request.put({ url: `/promotion/cms-article/update`, data })
}

// 删除CMS文章
export const deleteCmsArticle = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-article/delete?id=` + id })
}

// 批量删除CMS文章
export const batchDeleteCmsArticle = async (ids: number[]) => {
  return await request.post({ url: `/promotion/cms-article/batch-delete`, data: ids })
}

// 审核CMS文章
export const auditCmsArticle = async (data: { id: number; auditStatus: string; auditRemark?: string }) => {
  return await request.put({ url: `/promotion/cms-article/audit`, data })
}

// 批量审核CMS文章
export const batchAuditCmsArticle = async (data: { ids: number[]; auditStatus: string; auditRemark?: string }) => {
  return await request.post({ url: `/promotion/cms-article/batch-audit`, data })
}

// 发布CMS文章
export const publishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/publish/` + id })
}

// 下架CMS文章
export const unpublishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/unpublish/` + id })
}

// 批量发布CMS文章
export const batchPublishCmsArticle = async (ids: number[]) => {
  return await request.post({ url: `/promotion/cms-article/batch-publish`, data: ids })
}

// 批量下架CMS文章
export const batchUnpublishCmsArticle = async (ids: number[]) => {
  return await request.post({ url: `/promotion/cms-article/batch-unpublish`, data: ids })
}

// 增加浏览数
export const incrementViewCount = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/increment-view-count/` + id })
}

// 增加分享数
export const incrementShareCount = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/increment-share-count/` + id })
}

// 获得已发布文章分页(用户端)
export const getAppArticlePage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-article/page`, params })
}

// 获得用户端文章详情
export const getAppArticleDetail = async (id: number) => {
  return await request.get({ url: `/promotion/cms-article/app/detail?id=` + id })
}

// 获取热门文章列表
export const getHotArticleList = async (sectionId?: number, limit?: number) => {
  const params: any = {}
  if (sectionId) params.sectionId = sectionId
  if (limit) params.limit = limit
  return await request.get({ url: `/promotion/cms-article/hot-list`, params })
}

// 获取推荐文章列表
export const getRecommendArticleList = async (sectionId?: number, limit?: number) => {
  const params: any = {}
  if (sectionId) params.sectionId = sectionId
  if (limit) params.limit = limit
  return await request.get({ url: `/promotion/cms-article/recommend-list`, params })
}