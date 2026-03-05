import request from '@/config/axios'

// 图片文件夹 VO
export interface ImageFolderVO {
  id: number
  name: string
  parentId: number
  level: number
  path: string
  permissionType: number
  permissionTypeName: string
  sortOrder: number
  remark: string
  creator: string
  createTime: Date
  updateTime: Date
  childrenCount: number
  imageCount: number
}

// 图片文件夹创建请求 VO
export interface ImageFolderCreateReqVO {
  name: string
  parentId?: number
  permissionType: number
  sortOrder?: number
  remark?: string
}

// 图片文件夹更新请求 VO
export interface ImageFolderUpdateReqVO {
  id: number
  name: string
  permissionType: number
  parentId?: number
  sortOrder?: number
  remark?: string
}

// 图片文件夹移动请求 VO
export interface ImageFolderMoveReqVO {
  id: number
  targetParentId?: number
}

// 查询图片文件夹分页
export const getImageFolderPage = async (params: any) => {
  return await request.get({ url: '/infra/image-folder/page', params })
}

// 查询图片文件夹详情
export const getImageFolder = async (id: number) => {
  return await request.get({ url: '/infra/image-folder/get?id=' + id })
}

// 新增图片文件夹
export const createImageFolder = async (data: ImageFolderCreateReqVO) => {
  return await request.post({ url: '/infra/image-folder/create', data })
}

// 修改图片文件夹
export const updateImageFolder = async (data: ImageFolderUpdateReqVO) => {
  return await request.put({ url: '/infra/image-folder/update', data })
}

// 删除图片文件夹
export const deleteImageFolder = async (id: number) => {
  return await request.delete({ url: '/infra/image-folder/delete?id=' + id })
}

// 获得图片文件夹树
export const getImageFolderTree = async (params?: { permissionType?: number }) => {
  return await request.get({ url: '/infra/image-folder/tree', params })
}

// 移动图片文件夹
export const moveImageFolder = async (data: ImageFolderMoveReqVO) => {
  return await request.post({ url: '/infra/image-folder/move', data })
}

// 获得子文件夹列表
export const getChildrenFolderList = async (parentId?: number) => {
  return await request.get({ url: '/infra/image-folder/children', params: { parentId } })
}