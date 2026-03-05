<template>
  <Dialog v-model="dialogVisible" title="导入打款结果" width="460">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :auto-upload="false"
      :disabled="formLoading"
      :limit="1"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      accept=".xlsx, .xls"
      drag
    >
      <Icon icon="ep:upload" />
      <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <template #tip>
        <div class="el-upload__tip text-center">
          仅允许导入 xls、xlsx 格式文件，请在“待打款提现单导出”后填好打款状态再导入。
        </div>
      </template>
    </el-upload>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as BrokerageWithdrawApi from '@/api/mall/trade/brokerage/withdraw'

defineOptions({ name: 'BrokerageWithdrawFinanceImportForm' })

const message = useMessage()
const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const formLoading = ref(false)
const uploadRef = ref()
const fileList = ref([])

const open = () => {
  dialogVisible.value = true
  formLoading.value = false
  fileList.value = []
  resetForm()
}
defineExpose({ open })

const submitForm = async () => {
  if (fileList.value.length === 0) {
    message.error('请先上传文件')
    return
  }
  const rawFile = fileList.value[0]?.raw as File | undefined
  if (!rawFile) {
    message.error('文件读取失败，请重新选择')
    return
  }
  formLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', rawFile)
    const response = await BrokerageWithdrawApi.importFinanceResult(formData)
    const { code, data, msg } = response as any
    if (code !== 0) {
      message.error(msg || '导入失败，请重试')
      return
    }
    const resData = data || {}
    let tips = `导入成功：${resData.successCount || 0} 条；失败：${resData.failureCount || 0} 条。`
    if (resData.failureReasons && resData.failureReasons.length) {
      tips += '<br/>失败详情：'
      resData.failureReasons.forEach((item: string) => {
        tips += `<br/>${item}`
      })
    }
    message.alert(tips)
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('导入打款结果失败', error)
    message.error('导入失败，请重试')
  } finally {
    formLoading.value = false
  }
  resetForm()
}

const resetForm = async () => {
  await nextTick()
  uploadRef.value?.clearFiles()
}

const handleExceed = () => {
  message.error('最多只能上传一个文件！')
}

const handleRemove = () => {
  fileList.value = []
}
</script>
