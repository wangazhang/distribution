<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量移动图片"
    width="500px"
    @close="handleClose"
  >
    <div class="dialog-content">
      <div class="move-info">
        <Icon icon="ep:info-filled" />
        <span>已选择 <strong>{{ selectedIds.length }}</strong> 张图片</span>
      </div>

      <el-form :model="formData" label-width="100px">
        <el-form-item label="目标文件夹" prop="targetFolderId" :rules="formRules.targetFolderId">
          <el-tree-select
            v-model="formData.targetFolderId"
            :data="folderTreeForSelect"
            :props="treeProps"
            placeholder="请选择目标文件夹"
            clearable
            check-strictly
            style="width: 100%"
          />
          <div class="form-tip">
            选择要移动到的目标文件夹，不选择则移动到根目录
          </div>
        </el-form-item>
      </el-form>

      <!-- 图片预览 -->
      <div class="image-preview">
        <h4>即将移动的图片：</h4>
        <div class="preview-grid">
          <div
            v-for="image in previewImages"
            :key="image.id"
            class="preview-item"
          >
            <el-image
              :src="image.url"
              :alt="image.name"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
            />
            <div class="preview-name" :title="image.name">
              {{ image.name }}
            </div>
          </div>
          <div v-if="selectedIds.length > previewImages.length" class="preview-more">
            +{{ selectedIds.length - previewImages.length }}
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定移动
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as ImageResourceApi from '@/api/infra/imageResource'
import * as ImageFolderApi from '@/api/infra/imageFolder'

// Props
interface Props {
  visible: boolean
  selectedIds: number[]
}

const props = withDefaults(defineProps<Props>(), {
  selectedIds: () => []
})

// Emits
interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const folderTreeForSelect = ref([])
const previewImages = ref([])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = reactive({
  targetFolderId: null as number | null
})

const formRules = {
  targetFolderId: [
    { required: true, message: '请选择目标文件夹', trigger: 'change' }
  ]
}

const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 监听dialogVisible变化
watch(dialogVisible, async (visible) => {
  if (visible) {
    await loadFolderTree()
    await loadPreviewImages()
    // 重置表单
    formData.targetFolderId = null
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

const loadPreviewImages = async () => {
  try {
    // 获取选中图片的详细信息
    const imagePromises = props.selectedIds.slice(0, 6).map(id =>
      ImageResourceApi.getImageResource(id)
    )

    const results = await Promise.all(imagePromises)
    previewImages.value = results.map(result => result.data)
  } catch (error) {
    console.error('加载图片预览失败:', error)
    previewImages.value = []
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  try {
    if (!formData.targetFolderId) {
      formData.targetFolderId = 0
    }

    loading.value = true

    await ImageResourceApi.batchMoveImageResource({
      ids: props.selectedIds,
      targetFolderId: formData.targetFolderId
    })

    ElMessage.success('移动成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('批量移动失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-content {
  padding: 20px 0;
}

.move-info {
  display: flex;
  padding: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #1e40af;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  align-items: center;
  gap: 8px;
}

.move-info .el-icon {
  font-size: 16px;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.image-preview {
  margin-top: 24px;
}

.image-preview h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
}

.preview-item {
  text-align: center;
}

.preview-name {
  margin-top: 4px;
  overflow: hidden;
  font-size: 12px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-more {
  display: flex;
  height: 60px;
  font-size: 14px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 4px;
  align-items: center;
  justify-content: center;
}
</style>