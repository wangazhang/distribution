<template>
  <div class="image-grid" v-loading="loading">
    <div v-if="!loading && images.length === 0" class="empty-state">
      <el-empty description="暂无图片" />
    </div>

    <div v-else class="grid-container">
      <div
        v-for="image in images"
        :key="image.id"
        class="image-item"
        :class="{ 'selected': isSelected(image) }"
      >
        <!-- 选择框 -->
        <div class="checkbox-container" @click.stop>
          <el-checkbox
            :model-value="isSelected(image)"
            @change="(val) => handleSelectionChange(image, val)"
          />
        </div>

        <!-- 图片容器 -->
        <div class="image-container" @click="handleImagePreview(image)">
          <el-image
            :src="image.url"
            :alt="image.name"
            fit="cover"
            class="image"
            :preview-src-list="[]"
            :initial-index="0"
            hide-on-click-modal
            :lazy="true"
          >
            <template #error>
              <div class="image-error">
                <Icon icon="ep:picture" />
                <span>加载失败</span>
              </div>
            </template>
          </el-image>

          <!-- 悬浮操作按钮 -->
          <div class="image-actions" @click.stop>
            <el-button
              text
              size="small"
              @click="handleImagePreview(image)"
              title="预览"
            >
              <Icon icon="ep:view" />
            </el-button>
            <el-button
              text
              size="small"
              @click="handleEdit(image)"
              title="编辑"
              v-if="hasPermission('infra:image-resource:update')"
            >
              <Icon icon="ep:edit" />
            </el-button>
            <el-button
              text
              size="small"
              @click="handleCopyLink(image)"
              title="复制链接"
            >
              <Icon icon="ep:document-copy" />
            </el-button>
            <el-button
              text
              size="small"
              @click="handleDownload(image)"
              title="下载"
            >
              <Icon icon="ep:download" />
            </el-button>
            <el-button
              text
              size="small"
              @click="handleDelete(image)"
              title="删除"
              v-if="hasPermission('infra:image-resource:delete')"
            >
              <Icon icon="ep:delete" />
            </el-button>
          </div>
        </div>

        <!-- 图片信息 -->
        <div class="image-info">
          <div class="image-name" :title="image.name">
            {{ image.name }}
          </div>
          <div class="image-meta">
            <span class="image-size">{{ image.width }}×{{ image.height }}</span>
            <span class="image-format">{{ image.format?.toUpperCase() }}</span>
            <span class="image-filesize">{{ image.fileSizeDisplay }}</span>
          </div>
          <div class="image-stats">
            <span class="stat-item">
              <Icon icon="ep:view" />
              {{ image.viewCount || 0 }}
            </span>
            <span class="stat-item">
              <Icon icon="ep:download" />
              {{ image.downloadCount || 0 }}
            </span>
          </div>
          <div v-if="image.tags" class="image-tags">
            <el-tag
              v-for="tag in image.tags.split(',')"
              :key="tag"
              size="small"
              type="info"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/store/modules/user'

// Props
interface Props {
  images: any[]
  loading: boolean
  selectedImages: any[]
}

const props = withDefaults(defineProps<Props>(), {
  images: () => [],
  loading: false,
  selectedImages: () => []
})

// Emits
const emit = defineEmits<{
  'image-preview': [image: any]
  'image-update': [image: any]
  'image-delete': [image: any]
  'selection-change': [selection: any[]]
}>()

// 计算属性
const userStore = useUserStore()
const hasPermission = (permission: string) => {
  // 确保getPermissions是数组且包含includes方法
  if (!userStore?.getPermissions || !Array.isArray(userStore.getPermissions)) {
    return false
  }
  return userStore.getPermissions.includes(permission)
}

const { copy } = useClipboard()

// 方法
const isSelected = (image: any) => {
  return props.selectedImages.some(selected => selected.id === image.id)
}

const handleSelectionChange = (image: any, checked: boolean) => {
  let newSelection = [...props.selectedImages]

  if (checked) {
    if (!isSelected(image)) {
      newSelection.push(image)
    }
  } else {
    newSelection = newSelection.filter(item => item.id !== image.id)
  }

  emit('selection-change', newSelection)
}

const handleImagePreview = (image: any) => {
  emit('image-preview', image)
}

const handleEdit = (image: any) => {
  emit('image-update', image)
}

const handleCopyLink = async (image: any) => {
  try {
    await copy(image.url)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const handleDownload = (image: any) => {
  const link = document.createElement('a')
  link.href = image.url
  link.download = image.originalName || image.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleDelete = (image: any) => {
  emit('image-delete', image)
}
</script>

<style scoped>


/* 响应式调整 */
@media (width <= 768px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .image-container {
    height: 150px;
  }
}

@media (width <= 480px) {
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

.image-grid {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.image-item {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.image-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgb(0 0 0 / 10%);
}

.image-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgb(64 158 255 / 20%);
}

.checkbox-container {
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

.image-item:hover .checkbox-container,
.image-item.selected .checkbox-container {
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

.image-item:hover .image-actions {
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
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
}

.image-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #606266;
}

.image-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
