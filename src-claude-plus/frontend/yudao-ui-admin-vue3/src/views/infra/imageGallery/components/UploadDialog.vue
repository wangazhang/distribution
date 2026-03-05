<template>
  <el-dialog
    v-model="dialogVisible"
    title="上传图片"
    width="800px"
    @close="handleClose"
  >
    <div class="upload-dialog">
      <!-- 上传区域 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-progress="handleUploadProgress"
          accept="image/*"
          multiple
          drag
        >
          <div class="upload-area">
            <Icon icon="ep:upload-filled" class="upload-icon" />
            <div class="upload-text">
              <p>点击或拖拽文件到此处上传</p>
              <p class="upload-tip">支持 JPG、PNG、GIF、WEBP 格式，单个文件不超过 10MB</p>
            </div>
          </div>
        </el-upload>

        <!-- 上传设置 -->
        <div class="upload-settings">
          <el-form :model="uploadSettings" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="目标文件夹">
                  <el-tree-select
                    v-model="uploadSettings.folderId"
                    :data="folderTreeForSelect"
                    :props="treeProps"
                    placeholder="请选择文件夹"
                    clearable
                    check-strictly
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="图片名称前缀">
                  <el-input
                    v-model="uploadSettings.namePrefix"
                    placeholder="可选，将作为图片名称前缀"
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="通用标签">
                  <el-input
                    v-model="uploadSettings.commonTags"
                    placeholder="多个标签用逗号分隔，将应用到所有上传的图片"
                    maxlength="200"
                    show-word-limit
                  />
                  <div class="form-tip">示例：商品,主图,新品</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!-- 上传队列 -->
        <div class="upload-queue" v-if="uploadQueue.length > 0">
          <h4>上传队列</h4>
          <div class="queue-list">
            <div
              v-for="(item, index) in uploadQueue"
              :key="index"
              class="queue-item"
              :class="{ 'uploading': item.status === 'uploading', 'success': item.status === 'success', 'error': item.status === 'error' }"
            >
              <div class="item-preview">
                <el-image
                  :src="item.url"
                  :alt="item.name"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                />
              </div>

              <div class="item-info">
                <div class="item-name" :title="item.name">{{ item.name }}</div>
                <div class="item-meta">
                  <span>{{ formatFileSize(item.size) }}</span>
                  <span>{{ item.format?.toUpperCase() }}</span>
                </div>
                <div class="item-progress" v-if="item.status === 'uploading'">
                  <el-progress
                    :percentage="item.percentage"
                    :stroke-width="6"
                    :show-text="false"
                  />
                  <span class="progress-text">{{ item.percentage }}%</span>
                </div>
              </div>

              <div class="item-status">
                <Icon
                  v-if="item.status === 'success'"
                  icon="ep:check"
                  class="status-icon success"
                />
                <Icon
                  v-else-if="item.status === 'error'"
                  icon="ep:close"
                  class="status-icon error"
                />
                <Icon
                  v-else-if="item.status === 'uploading'"
                  icon="ep:loading"
                  class="status-icon uploading"
                />
              </div>

              <div class="item-actions">
                <el-button
                  v-if="item.status === 'error'"
                  text
                  size="small"
                  @click="retryUpload(item, index)"
                >
                  重试
                </el-button>
                <el-button
                  text
                  size="small"
                  @click="removeFromQueue(index)"
                  :disabled="item.status === 'uploading'"
                >
                  移除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 上传统计 -->
      <div class="upload-stats" v-if="uploadStats.total > 0">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总计" :value="uploadStats.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="成功" :value="uploadStats.success" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="失败" :value="uploadStats.failed" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="上传中" :value="uploadStats.uploading" />
          </el-col>
        </el-row>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        @click="handleUpload"
        :disabled="uploadQueue.length === 0"
        :loading="isUploading"
      >
        {{ isUploading ? '上传中...' : '开始上传' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccessToken, getTenantId } from '@/utils/auth'
import * as ImageFolderApi from '@/api/infra/imageFolder'
import * as ImageResourceApi from '@/api/infra/imageResource'

// Props
interface Props {
  visible: boolean
  currentFolderId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  currentFolderId: null
})

// Emits
interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const uploadRef = ref()
const folderTreeForSelect = ref([])
const uploadQueue = ref([])
const isUploading = ref(false)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 使用系统标准的文件上传接口
const uploadUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/infra/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getAccessToken(),
  'tenant-id': getTenantId()
}))

const uploadSettings = reactive({
  folderId: null as number | null,
  namePrefix: '',
  commonTags: ''
})

const uploadData = computed(() => ({
  folderId: uploadSettings.folderId || props.currentFolderId
}))

// 上传统计
const uploadStats = computed(() => {
  const stats = {
    total: uploadQueue.value.length,
    success: 0,
    failed: 0,
    uploading: 0
  }

  uploadQueue.value.forEach(item => {
    stats[item.status]++
  })

  return stats
})

// 监听dialogVisible变化
watch(dialogVisible, async (visible) => {
  if (visible) {
    // 初始化上传设置
    uploadSettings.folderId = props.currentFolderId
    uploadSettings.namePrefix = ''
    uploadSettings.commonTags = ''
    uploadQueue.value = []

    // 加载文件夹树
    await loadFolderTree()
  }
})

// 方法
const loadFolderTree = async () => {
  try {
    const data = await ImageFolderApi.getImageFolderTree()
    folderTreeForSelect.value = [
      {
        id: 0,
        name: '根目录',
        children: data
      }
    ]
  } catch (error) {
    console.error('加载文件夹树失败:', error)
  }
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB!')
    return false
  }
  return true
}

const handleFileChange = (file: any, fileList: any[]) => {
  // 添加到上传队列
  const queueItem = {
    file: file.raw,
    name: uploadSettings.namePrefix
      ? `${uploadSettings.namePrefix}_${file.name}`
      : file.name,
    size: file.size,
    format: file.name.split('.').pop()?.toLowerCase(),
    url: URL.createObjectURL(file.raw),
    status: 'pending',
    percentage: 0,
    response: null,
    error: null
  }

  uploadQueue.value.push(queueItem)
}

const handleFileRemove = (file: any, fileList: any[]) => {
  // 从上传队列中移除
  const index = uploadQueue.value.findIndex(item => item.name === file.name)
  if (index !== -1) {
    URL.revokeObjectURL(uploadQueue.value[index].url)
    uploadQueue.value.splice(index, 1)
  }
}

const handleUploadProgress = (event: any, file: any, fileList: any[]) => {
  // 更新上传进度
  const index = uploadQueue.value.findIndex(item => item.name === file.name)
  if (index !== -1) {
    uploadQueue.value[index].percentage = event.percent
    uploadQueue.value[index].status = 'uploading'
  }
}

const handleUploadSuccess = (response: any, file: any, fileList: any[]) => {
  // 更新上传状态
  const index = uploadQueue.value.findIndex(item => item.name === file.name)
  if (index !== -1) {
    if (response.code === 200) {
      uploadQueue.value[index].status = 'success'
      uploadQueue.value[index].response = response.data
      ElMessage.success(`${file.name} 上传成功`)
    } else {
      uploadQueue.value[index].status = 'error'
      uploadQueue.value[index].error = response.msg || '上传失败'
      ElMessage.error(`${file.name} 上传失败: ${response.msg || '未知错误'}`)
    }
  }

  // 检查是否全部上传完成
  checkUploadComplete()
}

const handleUploadError = (error: any, file: any, fileList: any[]) => {
  const index = uploadQueue.value.findIndex(item => item.name === file.name)
  if (index !== -1) {
    uploadQueue.value[index].status = 'error'
    uploadQueue.value[index].error = '网络错误'
    ElMessage.error(`${file.name} 上传失败: 网络错误`)
  }

  checkUploadComplete()
}

const handleUpload = async () => {
  if (uploadQueue.value.length === 0) {
    ElMessage.warning('请先选择要上传的图片')
    return
  }

  isUploading.value = true

  try {
    // 逐个上传文件
    for (let i = 0; i < uploadQueue.value.length; i++) {
      const item = uploadQueue.value[i]

      if (item.status === 'pending' || item.status === 'error') {
        await uploadSingleFile(item, i)
      }
    }
  } catch (error) {
    console.error('批量上传失败:', error)
  } finally {
    isUploading.value = false
  }
}

const uploadSingleFile = async (item: any, index: number) => {
  try {
    // 使用新的上传流程：先上传文件，再创建图片资源记录
    await ImageResourceApi.uploadImageResource(
      {
        name: item.name,
        folderId: uploadSettings.folderId,
        tags: uploadSettings.commonTags
      },
      item.file
    )
  } catch (error) {
    throw error
  }
}

const retryUpload = async (item: any, index: number) => {
  item.status = 'pending'
  item.percentage = 0
  item.error = null
  await uploadSingleFile(item, index)
}

const removeFromQueue = (index: number) => {
  URL.revokeObjectURL(uploadQueue.value[index].url)
  uploadQueue.value.splice(index, 1)
}

const checkUploadComplete = () => {
  const allCompleted = uploadQueue.value.every(
    item => item.status === 'success' || item.status === 'error'
  )

  if (allCompleted && !isUploading.value) {
    const successCount = uploadQueue.value.filter(item => item.status === 'success').length
    const failedCount = uploadQueue.value.filter(item => item.status === 'error').length

    if (failedCount === 0) {
      ElMessage.success(`成功上传 ${successCount} 张图片`)
    } else {
      ElMessage.warning(`上传完成，成功 ${successCount} 张，失败 ${failedCount} 张`)
    }

    // 延迟关闭对话框，让用户看到结果
    setTimeout(() => {
      emit('success')
      handleClose()
    }, 2000)
  }
}

const handleClose = () => {
  dialogVisible.value = false

  // 清理URL对象
  uploadQueue.value.forEach(item => {
    URL.revokeObjectURL(item.url)
  })
  uploadQueue.value = []
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.upload-dialog {
  max-height: 80vh;
  overflow-y: auto;
}

.upload-section {
  margin-bottom: 24px;
}

.upload-area {
  padding: 40px;
  text-align: center;
  background: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-icon {
  margin-bottom: 16px;
  font-size: 48px;
  color: #c0c4cc;
}

.upload-text {
  color: #606266;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.upload-settings {
  padding: 20px;
  margin-top: 24px;
  background: #f8f9fa;
  border-radius: 8px;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.upload-queue {
  margin-top: 24px;
}

.upload-queue h4 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
}

.queue-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.queue-item.uploading {
  background: #ecf5ff;
  border-color: #409eff;
}

.queue-item.success {
  background: #f0f9ff;
  border-color: #67c23a;
}

.queue-item.error {
  background: #fef0f0;
  border-color: #f56c6c;
}

.item-preview {
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  margin-bottom: 4px;
  overflow: hidden;
  font-weight: 500;
  color: #303133;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
  gap: 12px;
}

.item-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  min-width: 35px;
  font-size: 12px;
  color: #606266;
}

.item-status {
  flex-shrink: 0;
}

.status-icon {
  font-size: 18px;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.error {
  color: #f56c6c;
}

.status-icon.uploading {
  color: #409eff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }

  to { transform: rotate(360deg); }
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.upload-stats {
  padding: 16px;
  margin-top: 24px;
  background: #f8f9fa;
  border-radius: 8px;
}
</style>
