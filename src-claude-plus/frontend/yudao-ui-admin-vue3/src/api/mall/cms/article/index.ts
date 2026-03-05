import request from '@/config/axios'

// CMS文章 VO
export interface CmsArticleVO {
  id?: number
  sectionId: number
  categoryId: number
  title: string
  subtitle?: string
  coverImages?: string[]
  content: string
  contentType?: string // richtext-富文本, markdown-Markdown
  tagIds?: number[]
  authorId?: number
  authorType?: string
  authorName?: string
  authorAvatar?: string
  isHot?: number
  isOfficial?: number
  auditStatus?: string
  auditRemark?: string
  publishStatus?: number
  publishTime?: Date
  permissionType?: string // public-公开, member-会员专享, level-指定等级
  requiredLevel?: number
  viewCount?: number
  likeCount?: number
  collectCount?: number
  shareCount?: number
  downloadCount?: number
}

// 审核请求 VO
export interface CmsArticleAuditVO {
  id: number
  auditStatus: string // approved-通过, rejected-拒绝
  auditRemark?: string
}

// 查询文章分页
export const getCmsArticlePage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-article/page`, params })
}

// 查询文章详情
export const getCmsArticle = async (id: number) => {
  return await request.get({ url: `/promotion/cms-article/get?id=` + id })
}

// 获取用户端文章分页列表
export const getAppArticlePage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-article/page`, params })
}

// 获取用户端文章详情
export const getAppArticleDetail = async (id: number) => {
  return await request.get({ url: `/promotion/cms-article/get`, params: { id } })
}

// 获取热门文章列表
export const getHotArticleList = async (sectionId?: number, limit?: number) => {
  return await request.get({ url: `/promotion/cms-article/hot-list`, params: { sectionId, limit } })
}

// 获取推荐文章列表
export const getRecommendArticleList = async (sectionId?: number, limit?: number) => {
  return await request.get({ url: `/promotion/cms-article/recommend-list`, params: { sectionId, limit } })
}

// 新增文章
export const createCmsArticle = async (data: CmsArticleVO) => {
  return await request.post({ url: `/promotion/cms-article/create`, data })
}

// 修改文章
export const updateCmsArticle = async (data: CmsArticleVO) => {
  return await request.put({ url: `/promotion/cms-article/update`, data })
}

// 删除文章
export const deleteCmsArticle = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-article/delete?id=` + id })
}

// 审核文章
export const auditCmsArticle = async (data: CmsArticleAuditVO) => {
  return await request.put({ url: `/promotion/cms-article/audit`, data })
}

// 批量审核文章
export const batchAuditCmsArticle = async (data: CmsArticleAuditVO[]) => {
  return await request.put({ url: `/promotion/cms-article/batch-audit`, data })
}

// 发布文章
export const publishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/publish/` + id })
}

// 下架文章
export const unpublishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/unpublish/` + id })
}

// 批量发布文章
export const batchPublishCmsArticle = async (ids: number[]) => {
  return await request.put({ url: `/promotion/cms-article/batch-publish`, data: { ids } })
}

// 批量下架文章
export const batchUnpublishCmsArticle = async (ids: number[]) => {
  return await request.put({ url: `/promotion/cms-article/batch-unpublish`, data: { ids } })
}

// 记录浏览
export const viewArticle = async (id: number) => {
  return await request.post({ url: `/promotion/cms-article/view/${id}` })
}

// 记录分享
export const shareArticle = async (id: number) => {
  return await request.post({ url: `/promotion/cms-article/share/${id}` })
}

// 用户发布文章（UGC）
export const publishArticle = async (data: any) => {
  return await request.post({ url: `/promotion/cms-article/publish`, data })
}