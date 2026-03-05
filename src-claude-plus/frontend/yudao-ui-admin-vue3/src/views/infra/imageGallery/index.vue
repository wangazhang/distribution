<template>
  <div class="app-container">
    <div class="image-gallery">
      <!-- 左侧文件夹树 -->
      <div class="folder-tree-container">
        <div class="folder-header">
          <h3>图片文件夹</h3>
          <el-button
            type="primary"
            size="small"
            @click="handleCreateFolder"
            v-hasPermi="['infra:image-folder:create']">
            <Icon icon="ep:plus" />
            新建文件夹
          </el-button>
        </div>

        <FolderTree
          :folder-tree="folderTree"
          :current-folder-id="currentFolderId"
          @folder-select="handleFolderSelect"
          @folder-create="handleCreateFolder"
          @folder-update="handleUpdateFolder"
          @folder-delete="handleDeleteFolder"
          @folder-move="handleMoveFolder"
        />
      </div>

      <!-- 右侧图片区域 -->
      <div class="image-content-container">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item
                v-for="folder in breadcrumbList"
                :key="folder?.id || 0"
                :to="{ query: { folderId: folder?.id === 0 ? undefined : folder?.id } }">
                {{ folder?.name || '根目录' }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="actions">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept="image/*"
              v-hasPermi="['infra:image-resource:upload']"
            >
              <el-button type="primary">
                <Icon icon="ep:upload" />
                上传图片
              </el-button>
            </el-upload>

            <el-button
              @click="handleBatchMove"
              :disabled="!selectedImages.length"
              v-hasPermi="['infra:image-resource:update']">
              <Icon icon="ep:folder" />
              批量移动
            </el-button>

            <el-button
              @click="handleBatchDelete"
              :disabled="!selectedImages.length"
              type="danger"
              v-hasPermi="['infra:image-resource:delete']">
              <Icon icon="ep:delete" />
              批量删除
            </el-button>
          </div>
        </div>

        <!-- 搜索和筛选 -->
        <div class="search-filter">
          <el-form :model="queryParams" ref="queryForm" inline>
            <el-form-item label="图片名称">
              <el-input
                v-model="queryParams.name"
                placeholder="请输入图片名称"
                clearable
                @keyup.enter="handleQuery"
                style="width: 200px"
              />
            </el-form-item>

            <el-form-item label="图片格式">
              <el-select
                v-model="queryParams.format"
                placeholder="请选择格式"
                clearable
                style="width: 120px"
              >
                <el-option label="JPG" value="jpg" />
                <el-option label="PNG" value="png" />
                <el-option label="GIF" value="gif" />
                <el-option label="WEBP" value="webp" />
              </el-select>
            </el-form-item>

            <el-form-item label="标签">
              <el-input
                v-model="queryParams.tags"
                placeholder="请输入标签"
                clearable
                @keyup.enter="handleQuery"
                style="width: 200px"
              />
            </el-form-item>

            <el-form-item>
              <el-button @click="handleQuery">
                <Icon icon="ep:search" />
                搜索
              </el-button>
              <el-button @click="resetQuery">
                <Icon icon="ep:refresh" />
                重置
              </el-button>
            </el-form-item>
          </el-form>

          <div class="view-modes">
            <el-button-group>
              <el-button
                :type="viewMode === 'grid' ? 'primary' : 'default'"
                @click="viewMode = 'grid'">
                <Icon icon="ep:grid" />
                网格视图
              </el-button>
              <el-button
                :type="viewMode === 'list' ? 'primary' : 'default'"
                @click="viewMode = 'list'">
                <Icon icon="ep:list" />
                列表视图
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 图片展示区域 -->
        <div class="image-display-area">
          <div v-loading="loading" style="min-height: 400px;">
            <div v-if="!loading && imageList.length === 0" class="empty-state">
              <el-empty description="暂无图片" />
            </div>

            <div v-else-if="viewMode === 'grid'" class="simple-image-grid">
              <div
                v-for="image in imageList"
                :key="image.id"
                class="image-card"
                :class="{ 'selected': isSelected(image) }"
              >
                <!-- 选择框 -->
                <div class="image-checkbox">
                  <el-checkbox
                    :model-value="isSelected(image)"
                    @change="(val) => handleSelectionChange(image, val)"
                  />
                </div>

                <!-- 图片 -->
                <div class="image-container" @click="handleImagePreview(image)">
                  <img
                    :src="image.url"
                    :alt="image.name"
                    class="image"
                    @error="$event.target.style.display='none'"
                  />
                  <div class="image-error" v-show="false">
                    <Icon icon="ep:picture" />
                    <span>加载失败</span>
                  </div>
                </div>

                <!-- 操作按钮 -->
                <div class="image-actions">
                  <el-button text size="small" @click="handleImagePreview(image)">
                    <Icon icon="ep:view" />
                  </el-button>
                  <el-button text size="small" @click="handleUpdateImage(image)" v-if="hasPermission('infra:image-resource:update')">
                    <Icon icon="ep:edit" />
                  </el-button>
                  <el-button text size="small" @click="handleCopyLink(image)">
                    <Icon icon="ep:document-copy" />
                  </el-button>
                  <el-button text size="small" @click="handleDeleteImage(image)" v-if="hasPermission('infra:image-resource:delete')">
                    <Icon icon="ep:delete" />
                  </el-button>
                </div>

                <!-- 图片信息 -->
                <div class="image-info">
                  <div class="image-name" :title="image.name">{{ image.name }}</div>
                  <div class="image-meta">
                    <span>{{ image.width }}×{{ image.height }}</span>
                    <span>{{ image.format?.toUpperCase() }}</span>
                    <span>{{ image.fileSizeDisplay }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 列表视图 -->
            <el-table
              v-else
              :data="imageList"
              @selection-change="handleSelectionChange"
              style="width: 100%"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="图片" width="100">
                <template #default="{ row }">
                  <img
                    :src="row.url"
                    :alt="row.name"
                    style="width: 60px; height: 60px; cursor: pointer; border-radius: 4px; object-fit: cover;"
                    @click="handleImagePreview(row)"
                    @error="$event.target.style.display='none'"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="name" label="图片名称" min-width="150" />
              <el-table-column label="尺寸" width="100">
                <template #default="{ row }">
                  <span>{{ row.width }}×{{ row.height }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="format" label="格式" width="80">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.format?.toUpperCase() }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button text size="small" @click="handleImagePreview(row)">预览</el-button>
                  <el-button text size="small" @click="handleCopyLink(row)">复制</el-button>
                  <el-button text size="small" @click="handleDeleteImage(row)" v-if="hasPermission('infra:image-resource:delete')">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="queryParams.pageNo"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleQuery"
              @current-change="handleQuery"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 文件夹表单对话框 -->
    <FolderForm
      v-model:visible="folderFormVisible"
      :folder-data="folderFormData"
      :is-edit="isEditFolder"
      @success="handleFolderFormSuccess"
    />

    <!-- 图片更新对话框 -->
    <ImageForm
      v-model:visible="imageFormVisible"
      :image-data="imageFormData"
      @success="handleImageFormSuccess"
    />

    <!-- 批量移动对话框 -->
    <BatchMoveDialog
      v-model:visible="batchMoveVisible"
      :selected-ids="selectedImages.map(img => img.id)"
      @success="handleBatchMoveSuccess"
    />

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="图片预览"
      width="80%"
      :before-close="handlePreviewClose"
    >
      <div class="image-preview-container">
        <img
          v-if="previewImageUrl"
          :src="previewImageUrl"
          :alt="previewImageInfo?.name || ''"
          class="preview-image"
          @error="handlePreviewError"
        />
        <div v-else class="preview-error">
          <Icon icon="ep:picture" />
          <span>图片加载失败</span>
        </div>
      </div>
      <template #footer>
        <div class="preview-info">
          <div><strong>文件名：</strong>{{ previewImageInfo?.name || '' }}</div>
          <div><strong>尺寸：</strong>{{ previewImageInfo?.width || 0 }}×{{ previewImageInfo?.height || 0 }}</div>
          <div><strong>格式：</strong>{{ previewImageInfo?.format?.toUpperCase() || '' }}</div>
          <div><strong>大小：</strong>{{ previewImageInfo?.fileSizeDisplay || '' }}</div>
        </div>
        <el-button @click="handlePreviewClose">关闭</el-button>
        <el-button type="primary" @click="handleDownloadImage">下载</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

// API
import * as ImageFolderApi from '@/api/infra/imageFolder'
import * as ImageResourceApi from '@/api/infra/imageResource'

// 组件
import FolderTree from './components/FolderTree.vue'
import ImageGrid from './components/ImageGrid.vue'
import ImageList from './components/ImageList.vue'
import FolderForm from './components/FolderForm.vue'
import ImageForm from './components/ImageForm.vue'
import BatchMoveDialog from './components/BatchMoveDialog.vue'

// 路由
const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const folderTree = ref<any[]>([])
const imageList = ref<any[]>([])
const total = ref(0)
const selectedImages = ref<any[]>([])
const currentFolderId = ref<number | null>(null)

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  name: '',
  format: '',
  tags: '',
  folderId: null as number | null
})

// 面包屑 - 简化版本
const breadcrumbList = ref<any[]>([{ id: 0, name: '根目录' }])

// 视图模式
const viewMode = ref<'grid' | 'list'>('grid')

// 对话框状态
const folderFormVisible = ref(false)
const imageFormVisible = ref(false)
const batchMoveVisible = ref(false)
const previewVisible = ref(false)
const isEditFolder = ref(false)

// 表单数据
const folderFormData = ref({})
const imageFormData = ref({})
const previewImageUrl = ref('')
const previewImageInfo = ref({})

// 初始化
onMounted(() => {
  // 从路由参数获取文件夹ID
  const folderId = route.query.folderId
  if (folderId) {
    currentFolderId.value = Number(folderId)
    queryParams.folderId = Number(folderId)
  }

  loadFolderTree()
  loadImageList()
})

// 加载文件夹树
const loadFolderTree = async () => {
  try {
    const data = await ImageFolderApi.getImageFolderTree()
    folderTree.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载文件夹树失败:', error)
    folderTree.value = []
  }
}

// 加载图片列表 - 简化版本
const loadImageList = async () => {
  loading.value = true
  try {
    const data = await ImageResourceApi.getImageResourcePage(queryParams)

    // 直接处理数据
    imageList.value = data?.list || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('加载图片列表失败:', error)
    ElMessage.error('加载图片列表失败')
    imageList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 权限检查
const hasPermission = (permission: string) => {
  return true // 临时简化，可以后续恢复权限检查
}

// 复制链接
const handleCopyLink = async (image: any) => {
  try {
    await navigator.clipboard.writeText(image.url)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 选择状态检查
const isSelected = (image: any) => {
  return selectedImages.value.some(selected => selected.id === image.id)
}

// 选择变化 - 合并两个函数
const handleSelectionChange = (imageOrSelection: any, checked?: boolean) => {
  // 如果第一个参数是数组（表格的选择变化）
  if (Array.isArray(imageOrSelection)) {
    selectedImages.value = imageOrSelection
    return
  }

  // 如果是单个图片的选择变化（网格视图）
  const image = imageOrSelection
  if (checked !== undefined) {
    let newSelection = [...selectedImages.value]

    if (checked) {
      if (!isSelected(image)) {
        newSelection.push(image)
      }
    } else {
      newSelection = newSelection.filter(item => item.id !== image.id)
    }

    selectedImages.value = newSelection
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNo = 1
  loadImageList()
}

// 重置搜索
const resetQuery = () => {
  queryParams.name = ''
  queryParams.format = ''
  queryParams.tags = ''
  queryParams.pageNo = 1
  loadImageList()
}

// 文件夹选择
const handleFolderSelect = (folder: any) => {
  currentFolderId.value = folder.id
  queryParams.folderId = folder.id === 0 ? null : folder.id
  queryParams.pageNo = 1

  // 更新面包屑
  updateBreadcrumb(folder)

  // 加载图片
  loadImageList()

  // 更新路由参数
  router.push({
    query: {
      ...route.query,
      folderId: folder.id === 0 ? undefined : folder.id
    }
  })
}

// 更新面包屑 - 简化版本
const updateBreadcrumb = (folder: any) => {
  if (!folder || folder.id === 0) {
    breadcrumbList.value = [{ id: 0, name: '根目录' }]
  } else {
    breadcrumbList.value = [
      { id: 0, name: '根目录' },
      { id: folder.id, name: folder.name }
    ]
  }
}

// 创建文件夹
const handleCreateFolder = () => {
  folderFormData.value = {
    parentId: currentFolderId.value,
    permissionType: 1
  }
  isEditFolder.value = false
  folderFormVisible.value = true
}

// 更新文件夹
const handleUpdateFolder = (folder: any) => {
  folderFormData.value = { ...folder }
  isEditFolder.value = true
  folderFormVisible.value = true
}

// 删除文件夹
const handleDeleteFolder = async (folder: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件夹"${folder.name}"吗？`, '提示', {
      type: 'warning'
    })

    await ImageFolderApi.deleteImageFolder(folder.id)
    ElMessage.success('删除成功')
    loadFolderTree()
    loadImageList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文件夹失败:', error)
    }
  }
}

// 移动文件夹
const handleMoveFolder = async (folder: any, targetParentId: number) => {
  try {
    await ImageFolderApi.moveImageFolder({
      id: folder.id,
      targetParentId
    })
    ElMessage.success('移动成功')
    loadFolderTree()
  } catch (error) {
    console.error('移动文件夹失败:', error)
  }
}

// 图片选择 - 移除未使用的方法

// 图片预览
const handleImagePreview = (image: any) => {
  previewImageUrl.value = image.url
  previewImageInfo.value = { ...image }
  previewVisible.value = true

  // 增加查看次数
  try {
    ImageResourceApi.incrementViewCount(image.id)
  } catch (error) {
    console.error('增加查看次数失败:', error)
  }
}

// 预览关闭
const handlePreviewClose = () => {
  previewVisible.value = false
  previewImageUrl.value = ''
  previewImageInfo.value = {}
}

// 预览错误处理
const handlePreviewError = (event: any) => {
  console.error('图片预览加载失败:', event)
  event.target.style.display = 'none'
}

// 下载图片
const handleDownloadImage = () => {
  if (previewImageUrl.value && previewImageInfo.value) {
    const link = document.createElement('a')
    link.href = previewImageUrl.value
    link.download = previewImageInfo.value.originalName || previewImageInfo.value.name || 'image'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // 增加下载次数
    try {
      ImageResourceApi.incrementDownloadCount(previewImageInfo.value.id)
    } catch (error) {
      console.error('增加下载次数失败:', error)
    }
  }
}

// 更新图片
const handleUpdateImage = (image: any) => {
  imageFormData.value = { ...image }
  imageFormVisible.value = true
}

// 删除图片
const handleDeleteImage = async (image: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除图片"${image.name}"吗？`, '提示', {
      type: 'warning'
    })

    await ImageResourceApi.deleteImageResource(image.id)
    ElMessage.success('删除成功')
    loadImageList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图片失败:', error)
    }
  }
}

// 批量移动
const handleBatchMove = () => {
  batchMoveVisible.value = true
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedImages.value.length}张图片吗？`, '提示', {
      type: 'warning'
    })

    const deletePromises = selectedImages.value.map(img =>
      ImageResourceApi.deleteImageResource(img.id)
    )

    await Promise.all(deletePromises)
    ElMessage.success('批量删除成功')
    selectedImages.value = []
    loadImageList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 处理文件选择
const handleFileChange = async (file: any) => {
  const rawFile = file.raw

  // 校验文件
  const isImage = rawFile.type.startsWith('image/')
  const isLt10M = rawFile.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB!')
    return
  }

  try {
    // 调用我们的上传API
    const result = await ImageResourceApi.uploadImageResource(
      {
        name: rawFile.name,
        folderId: currentFolderId.value,
        tags: ''
      },
      rawFile
    )

    if (result.code === 200) {
      ElMessage.success('上传成功')
      loadImageList()
    } else {
      ElMessage.error('上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  }
}


// 文件夹表单成功
const handleFolderFormSuccess = () => {
  folderFormVisible.value = false
  loadFolderTree()
}

// 图片表单成功
const handleImageFormSuccess = () => {
  imageFormVisible.value = false
  loadImageList()
}

// 批量移动成功
const handleBatchMoveSuccess = () => {
  batchMoveVisible.value = false
  selectedImages.value = []
  loadImageList()
}
</script>

<style scoped>
.image-gallery {
  display: flex;
  height: calc(100vh - 120px);
  gap: 16px;
}

.folder-tree-container {
  display: flex;
  width: 300px;
  border-right: 1px solid #e4e7ed;
  flex-direction: column;
}

.folder-header {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  justify-content: space-between;
  align-items: center;
}

.folder-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.image-content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.toolbar {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  flex: 1;
}

.actions {
  display: flex;
  gap: 8px;
}

.search-filter {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  justify-content: space-between;
  align-items: center;
}

.image-display-area {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.pagination {
  display: flex;
  margin-top: 16px;
  justify-content: center;
}

.view-modes {
  display: flex;
  align-items: center;
}

.simple-image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.image-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.image-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgb(0 0 0 / 10%);
}

.image-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgb(64 158 255 / 20%);
}

.image-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
  padding: 2px;
  background: rgb(255 255 255 / 90%);
  border-radius: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-card:hover .image-checkbox,
.image-card.selected .image-checkbox {
  opacity: 1;
}

.image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  background: #f5f7fa;
}

.image-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  padding: 4px;
  background: rgb(0 0 0 / 60%);
  border-radius: 4px;
  opacity: 0;
  transition: opacity 0.2s;
  gap: 4px;
}

.image-card:hover .image-actions {
  opacity: 1;
}

.image-actions .el-button {
  padding: 4px;
  color: #fff;
}

.image-info {
  padding: 12px;
}

.image-name {
  margin-bottom: 4px;
  overflow: hidden;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.image-meta span {
  padding: 2px 4px;
  background: #f5f7fa;
  border-radius: 2px;
}

/* 图片预览样式 */
.image-preview-container {
  display: flex;
  max-height: 60vh;
  min-height: 400px;
  overflow: auto;
  justify-content: center;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.preview-error {
  display: flex;
  padding: 40px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.preview-error .icon {
  margin-bottom: 16px;
  font-size: 48px;
}

.preview-info {
  display: grid;
  padding: 16px;
  margin-bottom: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.preview-info div {
  font-size: 14px;
  color: #606266;
}
</style>