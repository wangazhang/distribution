<template>
  <Dialog v-model="dialogVisible" title="批量导入发货数据">
    <el-form>
      <el-form-item label="操作步骤" label-width="80px">
        <div class="steps-container">
          <div class="step">
            <div class="step-number">1</div>
            <div class="step-content">
              <div class="step-title">下载导入模板</div>
              <div class="step-desc">点击下载待发货订单模板，并填写物流公司和物流单号</div>
              <el-button type="primary" @click="handleDownloadTemplate" :loading="downloadLoading">
                <Icon icon="ep:download" class="mr-5px" /> 下载模板
              </el-button>
              <div class="import-tips mt-10px">
                <div class="tip-title">注意事项：</div>
                <ul class="tip-list">
                  <li>下载模板后请勿修改表头和出库单ID字段</li>
                  <li>必须填写物流公司和物流单号字段</li>
                  <li>只有状态为"已审核待发货"的订单才能被导入</li>
                  <li>表格中可能包含多余的空行，请删除后再导入</li>
                  <li><strong>WPS用户注意</strong>：保存时请选择Excel 97-2003格式(.xls)，避免格式兼容问题</li>
                  <li>如果导入失败，请尝试将表格另存为Excel 97-2003格式(.xls)后再导入</li>
                </ul>
              </div>
            </div>
          </div>
          <div class="step">
            <div class="step-number">2</div>
            <div class="step-content">
              <div class="step-title">选择Excel文件上传</div>
              <div class="step-desc">请上传填写好的Excel文件（请勿修改表头和出库单ID字段）</div>
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :limit="1"
                :on-exceed="handleExceed"
                :on-change="handleFileChange"
                accept=".xls,.xlsx"
              >
                <template #trigger>
                  <el-button type="primary">
                    <Icon icon="ep:upload-filled" class="mr-5px" /> 选择文件
                  </el-button>
                </template>
                <template #tip>
                  <div class="upload-tip">
                    提示：仅允许上传Excel文件(.xls/.xlsx)
                  </div>
                </template>
              </el-upload>
            </div>
          </div>
          <div class="step">
            <div class="step-number">3</div>
            <div class="step-content">
              <div class="step-title">开始导入</div>
              <div class="step-desc">点击导入按钮，将Excel中的发货数据导入系统</div>
              <el-button type="success" @click="handleImport" :loading="importLoading" :disabled="!fileList.length">
                <Icon icon="ep:upload" class="mr-5px" /> 开始导入
              </el-button>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>
    
    <!-- 导入结果显示 -->
    <div v-if="importResult" class="result-container">
      <el-divider>导入结果</el-divider>
      <el-alert
        :type="importResult.failureCount > 0 ? 'warning' : 'success'"
        :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failureCount} 条`"
        :closable="false"
        show-icon
      />
      <div v-if="importResult.failureCount > 0" class="mt-10px">
        <div class="font-bold mb-5px">失败原因：</div>
        <el-scrollbar height="150px">
          <ul class="error-list">
            <li v-for="(error, index) in importResult.errors" :key="index">{{ error }}</li>
          </ul>
        </el-scrollbar>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { MaterialOutboundApi } from '@/api/material/outbound'
import { UploadInstance, UploadUserFile } from 'element-plus'
import download from '@/utils/download'

const message = useMessage() // 消息弹窗

const dialogVisible = ref(false)
const downloadLoading = ref(false)
const importLoading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadRef = ref<UploadInstance>()
const importResult = ref<{
  successCount: number
  failureCount: number
  errors: string[]
} | null>(null)

/** 打开对话框 */
const open = () => {
  dialogVisible.value = true
  resetForm()
}
defineExpose({ open })

/** 下载模板 */
const handleDownloadTemplate = async () => {
  downloadLoading.value = true
  try {
    await MaterialOutboundApi.exportShippingTemplate()
    message.success('下载成功')
  } catch (error: any) {
    console.error('下载模板错误', error)
    message.error('下载模板失败：' + (error.message || '未知错误'))
  } finally {
    downloadLoading.value = false
  }
}

/** 处理文件超出限制 */
const handleExceed = () => {
  message.warning('只能上传一个文件')
}

/** 处理文件变更 */
const handleFileChange = (file: UploadUserFile) => {
  // 检查文件格式
  const isExcel = file.name.endsWith('.xls') || file.name.endsWith('.xlsx')
  if (!isExcel) {
    message.error('只能上传Excel文件(.xls/.xlsx)')
    return
  }

  fileList.value = [file]
  // 清空之前的导入结果
  importResult.value = null
}

/** 导入数据 */
const handleImport = async () => {
  if (fileList.value.length === 0) {
    message.warning('请先选择要上传的文件')
    return
  }
  
  // 检查文件大小
  const file = fileList.value[0].raw as File
  if (!file) {
    message.error('文件获取失败，请重新选择文件')
    return
  }
  
  if (file.size === 0) {
    message.error('文件内容为空，请检查后重新上传')
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    message.warning('文件过大，请确保小于10MB')
  }
  
  importLoading.value = true
  try {
    console.log('开始上传文件:', file.name, '大小:', file.size, '类型:', file.type)
    const res = await MaterialOutboundApi.importShipping(file)
    console.log('导入响应:', res)
    
    // 处理后端返回的数据
    const data = res.data || res
    
    if (data && (typeof data.successCount === 'number' || data.successCount !== undefined) &&
        (typeof data.failureCount === 'number' || data.failureCount !== undefined)) {
      // 确保导入结果是数字
      importResult.value = {
        successCount: Number(data.successCount) || 0,
        failureCount: Number(data.failureCount) || 0,
        errors: Array.isArray(data.errors) ? data.errors : []
      }
      
      // 如果全部成功，显示成功提示
      if (importResult.value.failureCount === 0 && importResult.value.successCount > 0) {
        message.success(`导入成功，共导入 ${importResult.value.successCount} 条数据`)
        // 关闭对话框
        setTimeout(() => {
          dialogVisible.value = false
          // 触发事件通知父组件刷新列表
          emit('success')
        }, 1500)
      } else if (importResult.value.successCount === 0 && importResult.value.failureCount === 0) {
        message.warning('导入完成，但没有任何数据被处理')
      }
    } else {
      // 数据格式不正确，显示错误
      console.error('导入结果格式不正确', data)
      message.error('导入失败：服务器返回数据格式不正确')
    }
  } catch (error: any) {
    console.error('导入失败', error)
    message.error(error.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

const emit = defineEmits(['success'])

/** 重置表单 */
const resetForm = () => {
  fileList.value = []
  importResult.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}
</script>

<style scoped>
.steps-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step {
  display: flex;
  gap: 16px;
}

.step-number {
  display: flex;
  width: 32px;
  height: 32px;
  font-weight: bold;
  color: white;
  background-color: var(--el-color-primary);
  border-radius: 50%;
  justify-content: center;
  align-items: center;
}

.step-content {
  flex: 1;
}

.step-title {
  margin-bottom: 8px;
  font-weight: bold;
}

.step-desc {
  margin-bottom: 10px;
  color: var(--el-text-color-secondary);
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.result-container {
  margin-top: 20px;
}

.error-list {
  padding-left: 20px;
  margin: 0;
  color: var(--el-color-danger);
}

.import-tips {
  margin-top: 10px;
}

.tip-title {
  margin-bottom: 8px;
  font-weight: bold;
}

.tip-list {
  padding-left: 20px;
  margin: 0;
  color: var(--el-text-color-secondary);
}
</style> 