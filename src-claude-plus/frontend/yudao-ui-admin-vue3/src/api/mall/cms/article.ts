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

// 发布文章
export const publishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/publish/` + id })
}

// 下架文章
export const unpublishCmsArticle = async (id: number) => {
  return await request.put({ url: `/promotion/cms-article/unpublish/` + id })
}
