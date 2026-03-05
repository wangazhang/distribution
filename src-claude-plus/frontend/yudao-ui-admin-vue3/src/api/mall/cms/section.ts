import request from '@/config/axios'

// CMS板块 VO
export interface CmsSectionVO {
  id?: number
  name: string
  type: string // article-文章类, dynamic-动态类, course-课程类, custom-自定义
  layoutStyle: string
  coverDisplayType?: string
  config?: any
  sort?: number
  status?: number
}

// 查询板块分页
export const getCmsSectionPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-section/page`, params })
}

// 查询板块详情
export const getCmsSection = async (id: number) => {
  return await request.get({ url: `/promotion/cms-section/get?id=` + id })
}

// 查询板块列表
export const getCmsSectionList = async (ids?: number[]) => {
  return await request.get({ 
    url: `/promotion/cms-section/list`, 
    params: ids ? { ids: ids.join(',') } : {} 
  })
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
