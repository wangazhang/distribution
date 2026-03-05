<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑文件夹' : '新建文件夹'"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="文件夹名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入文件夹名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="父文件夹" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="folderTreeForSelect"
          :props="treeProps"
          placeholder="请选择父文件夹"
          clearable
          check-strictly
          :disabled="isEdit"
        />
      </el-form-item>

      <el-form-item label="权限类型" prop="permissionType">
        <el-radio-group v-model="formData.permissionType">
          <el-radio :value="1">
            <Icon icon="ep:folder-opened" />
            共享文件夹
          </el-radio>
          <el-radio :value="2">
            <Icon icon="ep:lock" />
            隐私文件夹
          </el-radio>
        </el-radio-group>
        <div class="permission-tip">
          <Icon icon="ep:info-filled" />
          <span v-if="formData.permissionType === 1">
            共享文件夹：所有有权限的用户都可以查看和使用
          </span>
          <span v-else>
            隐私文件夹：仅创建者和管理员可以查看和使用
          </span>
        </div>
      </el-form-item>

      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="请输入排序号"
          style="width: 200px"
        />
        <div class="form-tip">数字越小排序越靠前</div>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注信息"
          maxlength="500"
          show-word-limit
        />
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
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as ImageFolderApi from '@/api/infra/imageFolder'

// Props
interface Props {
  visible: boolean
  folderData: any
  isEdit: boolean
}

const props = withDefaults(defineProps<Props>(), {
  folderData: () => ({}),
  isEdit: false
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

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  parentId: null as number | null,
  permissionType: 1,
  sortOrder: 0,
  remark: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '文件夹名称不能为空', trigger: 'blur' },
    { min: 1, max: 100, message: '文件夹名称长度为 1 到 100 个字符', trigger: 'blur' }
  ],
  permissionType: [
    { required: true, message: '权限类型不能为空', trigger: 'change' }
  ]
}

const folderTreeForSelect = ref([])

const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 加载文件夹树
const loadFolderTree = async () => {
  try {
    const data = await ImageFolderApi.getImageFolderTree()
    const validData = Array.isArray(data) ? data : []
    folderTreeForSelect.value = [
      {
        id: 0,
        name: '根目录',
        children: validData
      }
    ]
  } catch (error) {
    console.error('加载文件夹树失败:', error)
    // 设置默认数据
    folderTreeForSelect.value = [
      {
        id: 0,
        name: '根目录',
        children: []
      }
    ]
  }
}

// 监听folderData变化
watch(() => props.folderData, (newData) => {
  if (newData) {
    Object.assign(formData, newData)
    if (!formData.permissionType) {
      formData.permissionType = 1
    }
  }
}, { immediate: true, deep: true })

// 监听dialogVisible变化
watch(dialogVisible, async (visible) => {
  if (visible) {
    // 加载文件夹树数据
    await loadFolderTree()
    // 重置表单验证
    try {
      await nextTick()
      formRef.value?.clearValidate()
    } catch (error) {
      console.warn('重置表单验证失败:', error)
    }
  }
})

// 方法
const handleClose = () => {
  dialogVisible.value = false
  // 重置表单
  Object.assign(formData, {
    id: undefined,
    name: '',
    parentId: null,
    permissionType: 1,
    sortOrder: 0,
    remark: ''
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    if (props.isEdit) {
      await ImageFolderApi.updateImageFolder(formData)
      ElMessage.success('更新成功')
    } else {
      await ImageFolderApi.createImageFolder(formData)
      ElMessage.success('创建成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.permission-tip {
  display: flex;
  align-items: center;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.permission-tip .el-icon {
  margin-right: 4px;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-radio) {
  margin-right: 16px;
  margin-bottom: 8px;
}

:deep(.el-radio__label) {
  display: flex;
  align-items: center;
}

:deep(.el-radio__label .el-icon) {
  margin-right: 4px;
}
</style>