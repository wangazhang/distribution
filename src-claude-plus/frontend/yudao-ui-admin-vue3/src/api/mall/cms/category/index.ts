import request from '@/config/axios'

// CMS分类 VO
export interface CmsCategoryVO {
  id?: number
  sectionId: number
  name: string
  sort?: number
  iconUrl?: string
  description?: string
  articleCount?: number
  createTime?: Date
}

// 查询分类分页
export const getCmsCategoryPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-category/page`, params })
}

// 查询分类详情
export const getCmsCategory = async (id: number) => {
  return await request.get({ url: `/promotion/cms-category/get?id=` + id })
}

// 根据板块ID获取分类列表
export const getCmsCategoryBySectionId = async (sectionId: number) => {
  return await request.get({ url: `/promotion/cms-category/list-by-section`, params: { sectionId } })
}

// 获取分类简单列表（用于下拉选择）
export const getCmsCategorySimpleList = async (sectionId?: number) => {
  if (sectionId) {
    return await request.get({ url: `/promotion/cms-category/list-by-section`, params: { sectionId } })
  }
  return await request.get({ url: `/promotion/cms-category/list` })
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

// 更新分类排序
export const updateCmsCategorySort = async (data: { id: number; sort: number }[]) => {
  return await request.put({ url: `/promotion/cms-category/update-sort`, data })
}

// 获取分类下的文章数量
export const getCmsCategoryArticleCount = async (categoryId: number) => {
  return await request.get({ url: `/promotion/cms-category/article-count`, params: { categoryId } })
}