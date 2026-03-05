import request from '@/config/axios'

// CMS 板块 VO
export interface CmsSectionVO {
  id?: number
  name?: string
  type?: string
  description?: string
  icon?: string
  layoutStyle?: string
  coverDisplayType?: string
  config?: Record<string, any>
  sort?: number
  status?: number
  requireAudit?: boolean
  autoAuditEnabled?: boolean
  autoAuditDelayMinutes?: number
  createTime?: string
  updateTime?: string
}

// 获得CMS板块分页
export const getCmsSectionPage = async (params: any) => {
  return await request.get({ url: `/promotion/cms-section/page`, params })
}

// 查询CMS板块详情
export const getCmsSection = async (id: number) => {
  return await request.get({ url: `/promotion/cms-section/get?id=` + id })
}

// 新增CMS板块
export const createCmsSection = async (data: CmsSectionVO) => {
  return await request.post({ url: `/promotion/cms-section/create`, data })
}

// 修改CMS板块
export const updateCmsSection = async (data: CmsSectionVO) => {
  return await request.put({ url: `/promotion/cms-section/update`, data })
}

// 删除CMS板块
export const deleteCmsSection = async (id: number) => {
  return await request.delete({ url: `/promotion/cms-section/delete?id=` + id })
}

// 获得CMS板块列表
export const getCmsSectionList = async (ids?: number[]) => {
  return await request.get({
    url: `/promotion/cms-section/list`,
    params: ids ? { ids: ids.join(',') } : {}
  })
}

// 获得用户端CMS板块列表
export const getAppCmsSectionList = async () => {
  return await request.get({ url: `/promotion/cms-section/app/list` })
}
