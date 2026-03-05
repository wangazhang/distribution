<template>
  <div class="image-list" v-loading="loading">
    <div v-if="!loading && images.length === 0" class="empty-state">
      <el-empty description="暂无图片" />
    </div>

    <el-table
      v-else
      :data="images"
      @selection-change="handleSelectionChange"
      style="width: 100%"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.url"
            :alt="row.name"
            fit="cover"
            style="width: 60px; height: 60px; cursor: pointer; border-radius: 4px;"
            @click="handleImagePreview(row)"
            :preview-src-list="[]"
            :lazy="true"
          >
            <template #error>
              <div class="list-image-error">
                <Icon icon="ep:picture" />
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="图片名称" min-width="150">
        <template #default="{ row }">
          <div class="image-name-cell">
            <span class="name" :title="row.name">{{ row.name }}</span>
            <div class="original-name" v-if="row.originalName !== row.name">
              {{ row.originalName }}
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="folderName" label="所在文件夹" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.folderName" size="small" type="info">
            {{ row.folderName }}
          </el-tag>
          <span v-else class="text-placeholder">根目录</span>
        </template>
      </el-table-column>

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

      <el-table-column prop="fileSizeDisplay" label="大小" width="80" />

      <el-table-column label="统计" width="120">
        <template #default="{ row }">
          <div class="stats-cell">
            <span class="stat-item">
              <Icon icon="ep:view" />
              {{ row.viewCount || 0 }}
            </span>
            <span class="stat-item">
              <Icon icon="ep:download" />
              {{ row.downloadCount || 0 }}
            </span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="tags" label="标签" min-width="150">
        <template #default="{ row }">
          <div v-if="row.tags" class="tags-cell">
            <el-tag
              v-for="tag in row.tags.split(',')"
              :key="tag"
              size="small"
              type="info"
              effect="plain"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
          <span v-else class="text-placeholder">-</span>
        </template>
      </el-table-column>

      <el-table-column prop="creator" label="创建者" width="100" />

      <el-table-column prop="createTime" label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            text
            size="small"
            @click="handleImagePreview(row)"
          >
            <Icon icon="ep:view" />
            预览
          </el-button>
          <el-button
            text
            size="small"
            @click="handleCopyLink(row)"
          >
            <Icon icon="ep:document-copy" />
            复制
          </el-button>
          <el-button
            text
            size="small"
            @click="handleEdit(row)"
            v-if="hasPermission('infra:image-resource:update')"
          >
            <Icon icon="ep:edit" />
            编辑
          </el-button>
          <el-button
            text
            size="small"
            @click="handleDownload(row)"
          >
            <Icon icon="ep:download" />
            下载
          </el-button>
          <el-button
            text
            size="small"
            @click="handleDelete(row)"
            v-if="hasPermission('infra:image-resource:delete')"
          >
            <Icon icon="ep:delete" />
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/store/modules/user'
import { formatDate } from '@/utils/formatTime'

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
const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
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

const formatDateTime = (dateTime: string | Date) => {
  return formatDate(dateTime)
}
</script>

<style scoped>


/* 响应式调整 */
@media (width <= 768px) {
  :deep(.el-table) {
    font-size: 12px;
  }

  .stats-cell {
    gap: 1px;
  }

  .stat-item {
    font-size: 11px;
  }
}

.image-list {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.list-image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  background: #f5f7fa;
  border-radius: 4px;
}

.image-name-cell {
  display: flex;
  flex-direction: column;
}

.name {
  overflow: hidden;
  font-weight: 500;
  color: #303133;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.original-name {
  overflow: hidden;
  font-size: 12px;
  color: #909399;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #606266;
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-item {
  margin: 0;
}

.text-placeholder {
  font-style: italic;
  color: #c0c4cc;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>
