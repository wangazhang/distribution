<template>
  <Dialog :title="'取消出库'" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="出库单号：">
        <span>{{ outboundData.outboundNo || '-' }}</span>
      </el-form-item>
      <el-form-item label="申请用户：">
        <span>{{ outboundData.userNickname || '-' }} (ID: {{ outboundData.userId || '-' }})</span>
      </el-form-item>
      <el-form-item label="当前状态：">
        <el-tag v-if="outboundData.status === 0" type="warning">待审核</el-tag>
        <el-tag v-else-if="outboundData.status === 1" type="primary">已审核</el-tag>
        <el-tag v-else-if="outboundData.status === 2" type="info">已发货</el-tag>
        <el-tag v-else-if="outboundData.status === 3" type="success">已完成</el-tag>
        <el-tag v-else-if="outboundData.status === 4" type="danger">已取消</el-tag>
        <el-tag v-else-if="outboundData.status === 5" type="danger">审核拒绝</el-tag>
      </el-form-item>
      
      <el-divider content-position="left">取消信息</el-divider>
      <el-form-item label="取消原因" prop="cancelReason">
        <el-input
          v-model="formData.cancelReason"
          type="textarea"
          placeholder="请输入取消原因"
          :rows="4"
        />
      </el-form-item>
      
      <el-alert
        title="温馨提示"
        type="warning"
        show-icon
        :closable="false"
      >
        <template #default>
          <div>取消出库申请后：</div>
          <div>1. 该出库申请将不能再被处理</div>
          <div>2. 如果已扣减物料余额，将会自动回退</div>
          <div>3. 此操作不可撤销，请谨慎操作</div>
        </template>
      </el-alert>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="danger" @click="submitForm">确认取消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OutboundApi } from '@/api/material/outbound/index'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const outboundData = ref({})
const formData = ref({
  id: undefined,
  cancelReason: undefined
})

const formRules = reactive({
  cancelReason: [{ required: true, message: '取消原因不能为空', trigger: 'blur' }]
})

const formRef = ref()

const open = async (id: number) => {
  dialogVisible.value = true
  formData.value.id = id
  resetForm()

  formLoading.value = true
  try {
    outboundData.value = await OutboundApi.getMaterialOutbound(id)
  } finally {
    formLoading.value = false
  }
}

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return

  try {
    await message.confirm('确认取消该出库申请吗？此操作不可撤销！')

    formLoading.value = true
    await OutboundApi.cancelMaterialOutbound({
      id: formData.value.id,
      cancelReason: formData.value.cancelReason
    })
    message.success('取消成功')
    dialogVisible.value = false
    emit('success')
  } catch (e) {
    console.log(e)
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: formData.value.id,
    cancelReason: undefined
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>