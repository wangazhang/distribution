import request from '@/config/axios'

// CMS标签 VO
export interface CmsTagVO {
  id?: number
  name: string
  description?: string
  articleCount?: number
  createTime?: Date
}

// 分类标签关联
export interface CmsCategoryTagVO {
  id?: number
  categoryId: number
  tagId: number
}

// 查询标签分页
export const getCmsTagPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-tag/page`, params })
}

// 查询标签详情
export const getCmsTag = async (id: number) => {
  return await request.get({ url: `/promotion/cms-tag/get?id=` + id })
}

// 根据分类ID获取标签列表
export const getCmsTagByCategoryId = async (categoryId: number) => {
  return await request.get({ url: `/promotion/cms-tag/list-by-category`, params: { categoryId } })
}

// 获取标签简单列表（用于下拉选择）
export const getCmsTagSimpleList = async (categoryId?: number) => {
  if (categoryId) {
    return await request.get({ url: `/promotion/cms-tag/list-by-category`, params: { categoryId } })
  }
  return await request.get({ url: `/promotion/cms-tag/list` })
}

// 新增标签
export const createCmsTag = async (data: CmsTagVO) => {
  return await request.post({ url: `/promotion/cms-tag/create`, data })
}

// 修改标签
export const updateCmsTag = async (data: CmsTagVO) => {
  return await request.put({ url: `/promotion/cms-tag/update`, data })
}

// 删除标签
export const deleteCmsTag = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-tag/delete?id=` + id })
}

// 批量关联分类和标签
export const batchLinkCategoryTags = async (data: { categoryId: number; tagIds: number[] }[]) => {
  return await request.post({ url: `/promotion/cms-tag/batch-link-category`, data })
}

// 获取标签下的文章数量
export const getCmsTagArticleCount = async (tagId: number) => {
  return await request.get({ url: `/promotion/cms-tag/article-count`, params: { tagId } })
}