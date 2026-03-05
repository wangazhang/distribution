
import request from '@/config/axios'
import * as FileApi from '@/api/infra/file'

// 图片资源 VO
export interface ImageResourceVO {
  id: number
  fileId: number
  name: string
  originalName: string
  folderId: number
  folderName: string
  fileSize: number
  fileSizeDisplay: string
  width: number
  height: number
  format: string
  url: string
  tags: string
  downloadCount: number
  viewCount: number
  creator: string
  createTime: Date
  updateTime: Date
}

// 图片资源分页查询参数
export interface ImageResourcePageReqVO {
  pageNo?: number
  pageSize?: number
  name?: string
  originalName?: string
  folderId?: number
  format?: string
  tags?: string
  creator?: string
  fileSize?: [number, number]
  createTime?: Date[]
}

// 图片资源创建请求 VO（上传）
export interface ImageResourceUploadReqVO {
  name?: string
  folderId?: number
  tags?: string
}

// 图片资源更新请求 VO
export interface ImageResourceUpdateReqVO {
  id: number
  name: string
  folderId?: number
  tags?: string
}

// 图片资源批量移动请求 VO
export interface ImageResourceBatchMoveReqVO {
  ids: number[]
  targetFolderId: number
}

// 查询图片资源分页
export const getImageResourcePage = async (params: ImageResourcePageReqVO) => {
  return await request.get({ url: '/infra/image-resource/page', params })
}

// 查询图片资源详情
export const getImageResource = async (id: number) => {
  return await request.get({ url: '/infra/image-resource/get?id=' + id })
}

// 上传图片资源（新的流程：先上传文件，再创建图片资源记录）
export const uploadImageResource = async (data: ImageResourceUploadReqVO, file: File) => {
  try {
    // 第一步：使用系统的文件上传接口
    const uploadResult = await FileApi.updateFile({ file: file })

    if (uploadResult.code === 0 && uploadResult.data) {
      // 第二步：文件上传成功，创建图片资源记录
      const imageResourceData = {
        name: data.name || file.name,
        originalName: file.name,
        folderId: data.folderId,
        tags: data.tags,
        url: uploadResult.data,  // 文件上传接口直接返回URL字符串
        fileSize: file.size,
        format: file.type.split('/')[1]
      }

      // 调用后端接口创建图片资源记录
      const createResult = await createImageResource(imageResourceData)

      return {
        code: 200,
        data: {
          id: createResult.data,
          ...imageResourceData
        }
      }
    } else {
      throw new Error(uploadResult.msg || '文件上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    throw error
  }
}

// 创建图片资源记录（用于已上传的文件）
export const createImageResource = async (data: {
  name: string
  originalName: string
  folderId?: number
  tags?: string
  url: string
  fileSize: number
  format: string
}) => {
  return await request.post({ url: '/infra/image-resource/create', data })
}

// 修改图片资源
export const updateImageResource = async (data: ImageResourceUpdateReqVO) => {
  return await request.put({ url: '/infra/image-resource/update', data })
}

// 删除图片资源
export const deleteImageResource = async (id: number) => {
  return await request.delete({ url: '/infra/image-resource/delete?id=' + id })
}

// 获得图片选择列表
export const getImageSelectList = async (params?: { folderId?: number; keyword?: string }) => {
  return await request.get({ url: '/infra/image-resource/select-list', params })
}

// 批量移动图片资源
export const batchMoveImageResource = async (data: ImageResourceBatchMoveReqVO) => {
  return await request.post({ url: '/infra/image-resource/batch-move', data })
}

// 增加图片查看次数
export const incrementViewCount = async (id: number) => {
  return await request.post({ url: '/infra/image-resource/increment-view-count?id=' + id })
}

// 增加图片下载次数
export const incrementDownloadCount = async (id: number) => {
  return await request.post({ url: '/infra/image-resource/increment-download-count?id=' + id })
}

// 根据文件夹ID获取图片列表
export const getImageResourceListByFolderId = async (folderId: number) => {
  return await request.get({ url: '/infra/image-resource/list-by-folder?folderId=' + folderId })
}