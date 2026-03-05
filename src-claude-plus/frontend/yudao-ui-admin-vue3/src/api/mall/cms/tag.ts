import request from '@/config/axios'

// CMS标签 VO
export interface CmsTagVO {
  id?: number
  name: string
  sort?: number
  status?: number
}

// 查询标签分页
export const getCmsTagPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-tag/page`, params })
}

// 查询标签详情
export const getCmsTag = async (id: number) => {
  return await request.get({ url: `/promotion/cms-tag/get?id=` + id })
}

// 查询标签列表
export const getCmsTagList = async () => {
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
