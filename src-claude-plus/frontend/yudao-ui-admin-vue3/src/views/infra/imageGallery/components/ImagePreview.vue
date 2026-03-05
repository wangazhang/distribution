<template>
  <el-dialog
    v-model="dialogVisible"
    title="图片预览"
    width="80%"
    :show-close="true"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="preview-container" v-if="imageUrl">
      <div class="image-container">
        <el-image
          :src="imageUrl"
          :alt="imageInfo.name"
          fit="contain"
          style="max-width: 100%; max-height: 500px;"
          :preview-src-list="[imageUrl]"
          :initial-index="0"
          hide-on-click-modal
        />
      </div>

      <div class="image-info-panel" v-if="imageInfo.name">
        <h3>{{ imageInfo.name }}</h3>

        <div class="info-grid">
          <div class="info-item">
            <label>原始文件名：</label>
            <span>{{ imageInfo.originalName || '-' }}</span>
          </div>

          <div class="info-item">
            <label>所在文件夹：</label>
            <span>{{ imageInfo.folderName || '根目录' }}</span>
          </div>

          <div class="info-item">
            <label>图片尺寸：</label>
            <span>{{ imageInfo.width }}×{{ imageInfo.height }}</span>
          </div>

          <div class="info-item">
            <label>文件大小：</label>
            <span>{{ imageInfo.fileSizeDisplay }}</span>
          </div>

          <div class="info-item">
            <label>图片格式：</label>
            <el-tag size="small" type="info">{{ imageInfo.format?.toUpperCase() }}</el-tag>
          </div>

          <div class="info-item">
            <label>创建者：</label>
            <span>{{ imageInfo.creator || '-' }}</span>
          </div>

          <div class="info-item">
            <label>创建时间：</label>
            <span>{{ formatDateTime(imageInfo.createTime) }}</span>
          </div>

          <div class="info-item">
            <label>查看次数：</label>
            <span>{{ imageInfo.viewCount || 0 }}</span>
          </div>

          <div class="info-item">
            <label>下载次数：</label>
            <span>{{ imageInfo.downloadCount || 0 }}</span>
          </div>

          <div class="info-item full-width" v-if="imageInfo.tags">
            <label>标签：</label>
            <div class="tags-container">
              <el-tag
                v-for="tag in imageInfo.tags.split(',')"
                :key="tag"
                size="small"
                type="info"
                effect="plain"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="info-item full-width" v-if="imageInfo.remark">
            <label>备注：</label>
            <span>{{ imageInfo.remark }}</span>
          </div>
        </div>

        <div class="action-buttons">
          <el-button @click="handleCopyLink" :loading="copying">
            <Icon icon="ep:document-copy" />
            复制链接
          </el-button>
          <el-button @click="handleDownload">
            <Icon icon="ep:download" />
            下载
          </el-button>
          <el-button type="primary" @click="handleEdit" v-if="hasPermission('infra:image-resource:update')">
            <Icon icon="ep:edit" />
            编辑信息
          </el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/store/modules/user'
import { formatDate } from '@/utils/formatTime'

// Props
interface Props {
  visible: boolean
  imageUrl: string
  imageInfo: any
}

const props = withDefaults(defineProps<Props>(), {
  imageUrl: '',
  imageInfo: () => ({})
})

// Emits
interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'edit', image: any): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const copying = ref(false)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 计算属性
const userStore = useUserStore()
const hasPermission = (permission: string) => {
  return userStore.getPermissions.includes(permission)
}

const { copy } = useClipboard()

// 方法
const handleClose = () => {
  dialogVisible.value = false
}

const handleCopyLink = async () => {
  try {
    copying.value = true
    await copy(props.imageUrl)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  } finally {
    copying.value = false
  }
}

const handleDownload = () => {
  const link = document.createElement('a')
  link.href = props.imageUrl
  link.download = props.imageInfo.originalName || props.imageInfo.name || 'image'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  ElMessage.success('开始下载')
}

const handleEdit = () => {
  emit('edit', props.imageInfo)
  // 可以选择关闭预览对话框
  // dialogVisible.value = false
}

const formatDateTime = (dateTime: string | Date) => {
  return formatDate(dateTime)
}
</script>

<style scoped>


/* 响应式调整 */
@media (width <= 768px) {
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}

.preview-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-height: 80vh;
  overflow-y: auto;
}

.image-container {
  display: flex;
  min-height: 300px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  justify-content: center;
  align-items: center;
}

.image-info-panel {
  padding: 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.image-info-panel h3 {
  padding-bottom: 12px;
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  margin-bottom: 2px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.info-item span {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0;
}

.action-buttons {
  display: flex;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  gap: 12px;
  justify-content: flex-end;
}
</style>