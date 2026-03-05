<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择图库照片"
    width="90%"
    :before-close="handleClose"
    class="image-gallery-selector"
  >
    <div class="selector-content">
      <!-- 左侧文件夹树 -->
      <div class="folder-tree-container">
        <div class="folder-header">
          <h3>图片文件夹</h3>
        </div>
        <el-tree
          ref="folderTreeRef"
          :data="folderTree"
          :props="treeProps"
          :highlight-current="true"
          node-key="id"
          @node-click="handleFolderSelect"
          class="folder-tree"
        >
          <template #default="{ node }">
            <span class="folder-node">
              <Icon icon="ep:folder" />
              <span>{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
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
                @click="handleBreadcrumbClick(folder)">
                {{ folder?.name || '根目录' }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="search-filter">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索图片名称"
              clearable
              @input="handleSearch"
              style="width: 300px"
            >
              <template #prefix>
                <Icon icon="ep:search" />
              </template>
            </el-input>
          </div>
        </div>

        <!-- 图片展示区域 -->
        <div class="image-display-area" v-loading="loading">
          <div v-if="!loading && imageList.length === 0" class="empty-state">
            <el-empty description="暂无图片" />
          </div>

          <div v-else class="image-grid">
            <div
              v-for="image in filteredImageList"
              :key="image.id"
              class="image-item"
              :class="{ 'selected': isSelected(image) }"
              @click="handleImageClick(image)"
            >
              <div class="image-container">
                <img
                  :src="image.url"
                  :alt="image.name"
                  class="image"
                  @error="handleImageError"
                />
                <div class="image-overlay">
                  <div class="image-info">
                    <div class="image-name">{{ image.name }}</div>
                    <div class="image-meta">
                      <span>{{ image.width }}×{{ image.height }}</span>
                      <span>{{ image.format?.toUpperCase() }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="selected-mask" v-if="isSelected(image)">
                <Icon icon="ep:check" />
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="queryParams.pageNo"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[20, 50, 100]"
              :total="total"
              background
              layout="total, sizes, prev, pager, next"
              @size-change="loadImageList"
              @current-change="loadImageList"
            />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="selected-info">
          已选择 {{ selectedImages.length }} 张图片
        </div>
        <div class="dialog-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            @click="handleConfirm"
            :disabled="selectedImages.length === 0"
          >
            确定插入 ({{ selectedImages.length }})
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as ImageFolderApi from '@/api/infra/imageFolder'
import * as ImageResourceApi from '@/api/infra/imageResource'

interface ImageResource {
  id: number
  name: string
  url: string
  width: number
  height: number
  format: string
  folderId: number
}

interface Props {
  modelValue: boolean
  multiple?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  multiple: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'confirm': [images: ImageResource[]]
}>()

// 响应式数据
const loading = ref(false)
const folderTree = ref<any[]>([])
const imageList = ref<ImageResource[]>([])
const total = ref(0)
const selectedImages = ref<ImageResource[]>([])
const currentFolderId = ref<number | null>(null)
const searchKeyword = ref('')

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  name: '',
  folderId: null as number | null
})

// 面包屑
const breadcrumbList = ref<any[]>([{ id: 0, name: '根目录' }])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const filteredImageList = computed(() => {
  if (!searchKeyword.value) {
    return imageList.value
  }
  return imageList.value.filter(img =>
    img.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 初始化
onMounted(() => {
  if (dialogVisible.value) {
    loadFolderTree()
    loadImageList()
  }
})

// 监听对话框显示
watch(dialogVisible, (visible) => {
  if (visible) {
    loadFolderTree()
    loadImageList()
  }
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

// 加载图片列表
const loadImageList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      name: searchKeyword.value || undefined
    }
    const data = await ImageResourceApi.getImageResourcePage(params)

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

// 文件夹选择
const handleFolderSelect = (folder: any) => {
  currentFolderId.value = folder.id
  queryParams.folderId = folder.id === 0 ? null : folder.id
  queryParams.pageNo = 1
  updateBreadcrumb(folder)
  loadImageList()
}

// 面包屑点击
const handleBreadcrumbClick = (folder: any) => {
  handleFolderSelect(folder)
}

// 更新面包屑
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

// 搜索
const handleSearch = () => {
  queryParams.pageNo = 1
  loadImageList()
}

// 图片点击
const handleImageClick = (image: ImageResource) => {
  if (props.multiple) {
    const index = selectedImages.value.findIndex(img => img.id === image.id)
    if (index > -1) {
      selectedImages.value.splice(index, 1)
    } else {
      selectedImages.value.push(image)
    }
  } else {
    selectedImages.value = [image]
  }
}

// 选择状态检查
const isSelected = (image: ImageResource) => {
  return selectedImages.value.some(selected => selected.id === image.id)
}

// 图片加载错误处理
const handleImageError = (event: any) => {
  event.target.style.display = 'none'
}

// 关闭对话框
const handleClose = () => {
  selectedImages.value = []
  searchKeyword.value = ''
  dialogVisible.value = false
}

// 确认选择
const handleConfirm = () => {
  if (selectedImages.value.length === 0) {
    ElMessage.warning('请至少选择一张图片')
    return
  }

  emit('confirm', selectedImages.value)
  handleClose()
}
</script>

<style scoped>
.image-gallery-selector {
  .selector-content {
    display: flex;
    height: 600px;
    gap: 16px;
  }

  .folder-tree-container {
    display: flex;
    width: 250px;
    border-right: 1px solid #e4e7ed;
    flex-direction: column;
  }

  .folder-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
  }

  .folder-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }

  .folder-tree {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }

  .folder-node {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .image-content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
  }

  .breadcrumb {
    flex: 1;
  }

  .breadcrumb :deep(.el-breadcrumb__item) {
    cursor: pointer;
  }

  .search-filter {
    flex-shrink: 0;
  }

  .image-display-area {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
    margin-bottom: 16px;
  }

  .image-item {
    position: relative;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    transition: all 0.2s;
    aspect-ratio: 1;
  }

  .image-item:hover {
    border-color: #409eff;
    transform: scale(1.02);
  }

  .image-item.selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgb(64 158 255 / 20%);
  }

  .image-container {
    position: relative;
    width: 100%;
    height: 100%;
  }

  .image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-overlay {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    padding: 8px;
    color: white;
    background: linear-gradient(transparent, rgb(0 0 0 / 70%));
    transform: translateY(100%);
    transition: transform 0.2s;
  }

  .image-item:hover .image-overlay {
    transform: translateY(0);
  }

  .image-info {
    font-size: 12px;
  }

  .image-name {
    margin-bottom: 4px;
    overflow: hidden;
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .image-meta {
    display: flex;
    gap: 8px;
    opacity: 0.8;
  }

  .selected-mask {
    position: absolute;
    top: 8px;
    right: 8px;
    display: flex;
    width: 24px;
    height: 24px;
    font-size: 14px;
    color: white;
    background: #409eff;
    border-radius: 50%;
    align-items: center;
    justify-content: center;
  }

  .pagination {
    display: flex;
    justify-content: center;
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .selected-info {
    font-size: 14px;
    color: #666;
  }

  .dialog-actions {
    display: flex;
    gap: 8px;
  }
}
</style>