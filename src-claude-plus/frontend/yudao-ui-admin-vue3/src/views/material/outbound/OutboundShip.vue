<template>
  <Dialog :title="'发货'" v-model="dialogVisible" width="600px">
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
      <el-form-item label="收货人：">
        <span>{{ outboundData.receiverName || '-' }} ({{ outboundData.receiverMobile || '-' }})</span>
      </el-form-item>
      <el-form-item label="收货地址：">
        <span>{{ getFullAddress() }}</span>
      </el-form-item>
      
      <el-divider content-position="left">物流信息</el-divider>
      <el-form-item label="物流公司" prop="logisticsCompany">
        <el-select
          v-model="formData.logisticsCompany"
          placeholder="请选择物流公司"
          style="width: 100%"
          filterable
          allow-create
        >
          <el-option
            v-for="item in expressList"
            :key="item.id || item.name"
            :label="item.name"
            :value="item.name"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="快递单号" prop="logisticsCode">
        <el-input 
          v-model="formData.logisticsCode" 
          placeholder="请输入快递单号"
        />
      </el-form-item>
      <el-form-item label="发货备注" prop="shipRemark">
        <el-input
          v-model="formData.shipRemark"
          type="textarea"
          placeholder="请输入发货备注（可选）"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确认发货</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { OutboundApi } from '@/api/material/outbound/index'
import { getSimpleDeliveryExpressList } from '@/api/mall/trade/delivery/express'

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const outboundData = ref({})
const expressList = ref([])
const formData = ref({
  id: undefined,
  logisticsCompany: undefined,
  logisticsCode: undefined,
  shipRemark: undefined
})

const formRules = reactive({
  logisticsCompany: [{ required: true, message: '物流公司不能为空', trigger: 'change' }],
  logisticsCode: [{ required: true, message: '快递单号不能为空', trigger: 'blur' }]
})

const formRef = ref()

/** 加载物流公司列表 */
const loadExpressList = async () => {
  try {
    const data = await getSimpleDeliveryExpressList()
    expressList.value = data || []
  } catch (error) {
    console.error('加载物流公司失败', error)
    // 失败时使用默认列表作为备用
    expressList.value = [
      { name: '顺丰速运' },
      { name: '中通快递' },
      { name: '圆通速递' },
      { name: '申通快递' },
      { name: '韵达速递' },
      { name: '百世汇通' },
      { name: '天天快递' },
      { name: '德邦快递' },
      { name: '京东物流' },
      { name: '邮政EMS' }
    ]
  }
}

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

  formLoading.value = true
  try {
    await OutboundApi.shipMaterialOutbound({
      id: formData.value.id!,
      logisticsCompany: formData.value.logisticsCompany!,
      logisticsCode: formData.value.logisticsCode!
    })
    message.success('发货成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const getFullAddress = () => {
  const data = outboundData.value
  if (!data.receiverProvince) return '-'
  
  const parts = [
    data.receiverProvince,
    data.receiverCity,
    data.receiverDistrict,
    data.receiverDetailAddress
  ].filter(Boolean)
  
  return parts.join('')
}

const resetForm = () => {
  formData.value = {
    id: formData.value.id,
    logisticsCompany: undefined,
    logisticsCode: undefined,
    shipRemark: undefined
  }
  formRef.value?.resetFields()
}

// 组件挂载时加载物流公司列表
onMounted(() => {
  loadExpressList()
})

defineExpose({ open })
</script>