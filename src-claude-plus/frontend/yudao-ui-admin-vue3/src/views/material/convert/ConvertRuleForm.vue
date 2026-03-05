<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="源物料ID" prop="sourceMaterialId">
            <el-input-number
              v-model="formData.sourceMaterialId"
              placeholder="请输入源物料ID"
              :min="1"
              controls-position="right"
              style="width: 100%"
              @change="handleSourceMaterialChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标物料ID" prop="targetMaterialId">
            <el-input-number
              v-model="formData.targetMaterialId"
              placeholder="请输入目标物料ID"
              :min="1"
              controls-position="right"
              style="width: 100%"
              @change="handleTargetMaterialChange"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="源物料名称">
            <span>{{ formData.sourceMaterialName || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标物料名称">
            <span>{{ formData.targetMaterialName || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-divider content-position="left">转化比例</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="源物料数量" prop="sourceQuantity">
            <el-input-number
              v-model="formData.sourceQuantity"
              placeholder="源物料数量"
              :min="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标物料数量" prop="targetQuantity">
            <el-input-number
              v-model="formData.targetQuantity"
              placeholder="目标物料数量"
              :min="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="转化费用(分)" prop="convertFee">
        <el-input-number
          v-model="formData.convertFee"
          placeholder="转化费用，单位：分"
          :min="0"
          controls-position="right"
          style="width: 100%"
        />
        <div class="text-gray-500 text-sm mt-1">
          输入费用单位为分，如1元请输入100
        </div>
      </el-form-item>
      
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="规则描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          placeholder="请输入规则描述"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import * as ConvertRuleApi from '@/api/material/convertRule'
import * as MaterialApi from '@/api/material/definition'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  sourceMaterialId: undefined,
  sourceMaterialName: '',
  targetMaterialId: undefined,
  targetMaterialName: '',
  sourceQuantity: 1,
  targetQuantity: 1,
  convertFee: 0,
  status: 1,
  description: undefined
})

const formRules = reactive({
  sourceMaterialId: [{ required: true, message: '源物料ID不能为空', trigger: 'blur' }],
  targetMaterialId: [{ required: true, message: '目标物料ID不能为空', trigger: 'blur' }],
  sourceQuantity: [{ required: true, message: '源物料数量不能为空', trigger: 'blur' }],
  targetQuantity: [{ required: true, message: '目标物料数量不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})

const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ConvertRuleApi.getConvertRule(id)
    } finally {
      formLoading.value = false
    }
  }
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  
  // 验证源物料和目标物料不能相同
  if (formData.value.sourceMaterialId === formData.value.targetMaterialId) {
    message.error('源物料和目标物料不能相同')
    return
  }
  
  formLoading.value = true
  try {
    const data = formData.value as unknown as ConvertRuleApi.ConvertRuleVO
    if (formType.value === 'create') {
      await ConvertRuleApi.createConvertRule(data)
      message.success(t('common.createSuccess'))
    } else {
      await ConvertRuleApi.updateConvertRule(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 源物料变更时获取物料信息 */
const handleSourceMaterialChange = async () => {
  if (formData.value.sourceMaterialId) {
    try {
      const material = await MaterialApi.getDefinition(formData.value.sourceMaterialId)
      formData.value.sourceMaterialName = material.name
    } catch (e) {
      console.log('获取源物料信息失败:', e)
      formData.value.sourceMaterialName = ''
    }
  } else {
    formData.value.sourceMaterialName = ''
  }
}

/** 目标物料变更时获取物料信息 */
const handleTargetMaterialChange = async () => {
  if (formData.value.targetMaterialId) {
    try {
      const material = await MaterialApi.getDefinition(formData.value.targetMaterialId)
      formData.value.targetMaterialName = material.name
    } catch (e) {
      console.log('获取目标物料信息失败:', e)
      formData.value.targetMaterialName = ''
    }
  } else {
    formData.value.targetMaterialName = ''
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    sourceMaterialId: undefined,
    sourceMaterialName: '',
    targetMaterialId: undefined,
    targetMaterialName: '',
    sourceQuantity: 1,
    targetQuantity: 1,
    convertFee: 0,
    status: 1,
    description: undefined
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>