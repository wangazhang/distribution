<template>
  <el-dialog
    v-model="dialogVisible"
    title="编辑图片信息"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="图片名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入图片名称"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="所在文件夹" prop="folderId">
        <el-tree-select
          v-model="formData.folderId"
          :data="folderTreeForSelect"
          :props="treeProps"
          placeholder="请选择文件夹"
          clearable
          check-strictly
        />
      </el-form-item>

      <el-form-item label="图片标签" prop="tags">
        <el-input
          v-model="formData.tags"
          type="textarea"
          :rows="3"
          placeholder="请输入图片标签，多个标签用逗号分隔"
          maxlength="500"
          show-word-limit
        />
        <div class="form-tip">多个标签请用逗号分隔，如：商品,主图,新品</div>
      </el-form-item>

      <!-- 图片信息展示 -->
      <el-form-item label="图片信息">
        <div class="image-info-display">
          <el-image
            :src="formData.url"
            :alt="formData.name"
            fit="cover"
            style="width: 100px; height: 100px; border-radius: 4px;"
          />
          <div class="image-details">
            <p><strong>原始文件名：</strong>{{ formData.originalName }}</p>
            <p><strong>图片尺寸：</strong>{{ formData.width }}×{{ formData.height }}</p>
            <p><strong>文件大小：</strong>{{ formData.fileSizeDisplay }}</p>
            <p><strong>图片格式：</strong>{{ formData.format?.toUpperCase() }}</p>
            <p><strong>上传者：</strong>{{ formData.creator }}</p>
            <p><strong>上传时间：</strong>{{ formatDateTime(formData.createTime) }}</p>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as ImageResourceApi from '@/api/infra/imageResource'
import * as ImageFolderApi from '@/api/infra/imageFolder'
import { formatDate } from '@/utils/formatTime'

// Props
interface Props {
  visible: boolean
  imageData: any
}

const props = withDefaults(defineProps<Props>(), {
  imageData: () => ({}),
})

// Emits
interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const formRef = ref<FormInstance>()
const loading = ref(false)
const folderTreeForSelect = ref([])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  folderId: null as number | null,
  tags: '',
  url: '',
  originalName: '',
  width: 0,
  height: 0,
  fileSizeDisplay: '',
  format: '',
  creator: '',
  createTime: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '图片名称不能为空', trigger: 'blur' },
    { min: 1, max: 255, message: '图片名称长度为 1 到 255 个字符', trigger: 'blur' }
  ]
}

const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 监听imageData变化
watch(() => props.imageData, (newData) => {
  if (newData) {
    Object.assign(formData, newData)
  }
}, { immediate: true, deep: true })

// 监听dialogVisible变化
watch(dialogVisible, async (visible) => {
  if (visible) {
    // 重置表单验证
    formRef.value?.clearValidate()
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

const handleClose = () => {
  dialogVisible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await ImageResourceApi.updateImageResource(formData)
    ElMessage.success('更新成功')

    emit('success')
    handleClose()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateTime: string | Date) => {
  return formatDate(dateTime)
}
</script>

<style scoped>
.image-info-display {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.image-details {
  flex: 1;
}

.image-details p {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.4;
  color: #606266;
}

.image-details strong {
  color: #303133;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>