<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="物料" prop="materialId">
        <el-select
          v-model="formData.materialId"
          placeholder="请选择物料"
          clearable
          class="w-full"
          @change="handleMaterialChange"
        >
          <el-option
            v-for="item in materialOptions"
            :key="item.id"
            :label="`${item.name}(${item.code})`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="物料编码" prop="materialCode">
        <el-input v-model="formData.materialCode" disabled />
      </el-form-item>
      <el-form-item label="物料名称" prop="materialName">
        <el-input v-model="formData.materialName" disabled />
      </el-form-item>
      <el-form-item label="规格型号" prop="specification">
        <el-input v-model="formData.specification" disabled />
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number v-model="formData.quantity" :min="1" class="w-full" />
      </el-form-item>
      <el-form-item label="单位" prop="unit">
        <el-input v-model="formData.unit" disabled />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm" :disabled="formLoading">确定</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { MaterialOutboundItemApi, MaterialOutboundItemVO } from '@/api/material/outbound-item'
import * as MaterialApi from '@/api/material/index'

defineOptions({ name: 'MaterialOutboundItemForm' })

// 扩展物料出库明细VO，添加前端需要的额外字段
interface ExtendedMaterialOutboundItemVO extends MaterialOutboundItemVO {
  materialCode?: string
  materialName?: string
  specification?: string
  remark?: string
}

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 接收父组件传递的出库单ID
const props = defineProps({
  outboundId: {
    type: Number,
    required: true
  }
})

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref<ExtendedMaterialOutboundItemVO>({
  id: 0,
  outboundId: 0,
  materialId: 0,
  quantity: 1,
  unit: '',
  materialCode: '',
  materialName: '',
  specification: '',
  remark: ''
})
const formRules = reactive({
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

// 物料选项
const materialOptions = ref<any[]>([])

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增出库物料' : '编辑出库物料'
  formType.value = type
  resetForm()

  // 设置出库单ID
  formData.value.outboundId = props.outboundId

  // 加载物料选项
  await loadMaterialOptions()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const response = await MaterialOutboundItemApi.getMaterialOutboundItem(id)
      // 复制API返回的字段
      formData.value.id = response.id
      formData.value.outboundId = response.outboundId
      formData.value.materialId = response.materialId
      formData.value.quantity = response.quantity
      formData.value.unit = response.unit

      // 加载物料详情
      if (response.materialId) {
        await loadMaterialDetail(response.materialId)
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 提交请求
  formLoading.value = true
  try {
    // 只提交API需要的字段
    const data: MaterialOutboundItemVO = {
      id: formData.value.id,
      outboundId: formData.value.outboundId,
      materialId: formData.value.materialId,
      quantity: formData.value.quantity,
      unit: formData.value.unit
    }

    if (formType.value === 'create') {
      await MaterialOutboundItemApi.createMaterialOutboundItem(data)
      message.success(t('common.createSuccess'))
    } else {
      await MaterialOutboundItemApi.updateMaterialOutboundItem(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: 0,
    outboundId: props.outboundId,
    materialId: 0,
    quantity: 1,
    unit: '',
    materialCode: '',
    materialName: '',
    specification: '',
    remark: ''
  }
  formRef.value?.resetFields()
}

/** 加载物料选项 */
const loadMaterialOptions = async () => {
  try {
    // 这里假设有一个获取物料列表的API
    const response = await MaterialApi.getMaterialList()
    materialOptions.value = response || []
  } catch (error) {
    console.error('加载物料选项失败', error)
    materialOptions.value = []
  }
}

/** 加载物料详情 */
const loadMaterialDetail = async (materialId: number) => {
  try {
    const material = await MaterialApi.getMaterial(materialId)
    if (material) {
      formData.value.materialCode = material.code
      formData.value.materialName = material.name
      formData.value.specification = material.specification
      formData.value.unit = material.unit
    }
  } catch (error) {
    console.error('加载物料详情失败', error)
  }
}

/** 物料选择变更 */
const handleMaterialChange = async (materialId: number) => {
  if (!materialId) {
    formData.value.materialCode = ''
    formData.value.materialName = ''
    formData.value.specification = ''
    formData.value.unit = ''
    return
  }

  await loadMaterialDetail(materialId)
}
</script>
