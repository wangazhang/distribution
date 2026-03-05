<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="物料编码" prop="code">
            <el-input
              v-model="formData.code"
              placeholder="留空则自动生成 (MER格式)"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="物料类型" prop="type">
            <el-select
              v-model="formData.type"
              placeholder="请选择物料类型"
              style="width: 100%"
              @change="handleTypeChange"
            >
              <el-option label="半成品" :value="1" />
              <el-option label="成品" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 选择关联商品（半成品/成品均可选） -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="关联商品" prop="spuId">
            <div class="spu-select-container">
              <el-input
                v-model="selectedProductName"
                placeholder="请选择关联商品"
                readonly
                style="width: calc(100% - 100px)"
              >
                <template #suffix>
                  <el-button type="primary" link @click="openSpuSelect" size="small">
                    选择商品
                  </el-button>
                </template>
              </el-input>
              <el-button
                v-if="formData.spuId"
                type="danger"
                link
                size="small"
                @click="clearSelectedProduct"
                class="clear-btn"
              >
                清除
              </el-button>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 成品且选择了SPU，显示关联模式选择 -->
      <el-row :gutter="20" v-if="formData.type === 2 && formData.spuId">
        <el-col :span="24">
          <el-form-item label="关联模式" prop="linkMode">
            <el-radio-group v-model="formData.linkMode" @change="handleLinkModeChange">
              <el-radio :label="1">
                映射模式
                <el-tooltip
                  content="名称、图片、描述从SPU实时获取，不单独存储"
                  placement="top"
                >
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </el-radio>
              <el-radio :label="2">
                快照模式
                <el-tooltip content="从SPU复制信息并存储，可独立修改" placement="top">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="物料名称" prop="name">
            <el-input
              v-model="formData.name"
              placeholder="请输入物料名称"
              :disabled="isMappingMode"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="基础单位" prop="baseUnit">
            <el-select
              v-model="formData.baseUnit"
              placeholder="请选择基础单位"
              style="width: 100%"
              clearable
              filterable
              allow-create
            >
              <el-option label="个" value="个" />
              <el-option label="次" value="次" />
              <el-option label="克" value="克" />
              <el-option label="千克" value="千克" />
              <el-option label="毫升" value="毫升" />
              <el-option label="升" value="升" />
              <el-option label="盒" value="盒" />
              <el-option label="瓶" value="瓶" />
              <el-option label="支" value="支" />
              <el-option label="袋" value="袋" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="物料图片" prop="image">
        <UploadImg v-model="formData.image" :limit="1" :disabled="isMappingMode" />
      </el-form-item>

      <el-form-item label="物料描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          placeholder="请输入物料描述"
          :rows="3"
          :disabled="isMappingMode"
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="支持出库">
            <el-switch v-model="formData.supportOutbound" active-text="是" inactive-text="否" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="支持转化">
            <el-switch v-model="formData.supportConvert" active-text="是" inactive-text="否" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="showConvertConfig" :gutter="20">
        <el-col :span="12">
          <el-form-item label="转化目标" prop="convertedSpuId">
            <div class="spu-select-container">
              <el-input
                v-model="selectedConvertProductName"
                placeholder="请选择转化目标物料"
                readonly
                style="width: calc(100% - 100px)"
              >
                <template #suffix>
                  <el-button type="primary" link size="small" @click="openConvertSpuSelect">
                    选择商品
                  </el-button>
                </template>
              </el-input>
              <el-button
                v-if="formData.convertedSpuId"
                type="danger"
                link
                size="small"
                class="clear-btn"
                @click="clearConvertTarget"
              >
                清除
              </el-button>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="转化单价" prop="convertPrice">
            <el-input v-model="formData.convertPrice" placeholder="请输入转化单价" class="!w-200px">
              <template #append>元</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
  <SpuSelect ref="spuSelectRef" :selectable-only="true" @confirm="onSpuSelected" />
  <SpuSelect ref="convertSpuSelectRef" :selectable-only="true" @confirm="onConvertSpuSelected" />
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch, nextTick } from 'vue'
import * as DefinitionApi from '@/api/material/definition'
import { SpuSelect } from '@/views/mall/promotion/components'
import { QuestionFilled } from '@element-plus/icons-vue'
import { fenToYuan, yuanToFen } from '@/utils'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: null,
  code: undefined,
  spuId: null,
  image: null,
  description: null,
  type: 1,
  linkMode: null,
  baseUnit: undefined,
  status: 1,
  supportOutbound: true,
  supportConvert: true,
  convertedSpuId: null,
  convertPrice: ''
})

// SPU预览数据（映射模式使用）
const spuPreviewData = ref<{
  name?: string
  image?: string
  description?: string
} | null>(null)

// 商品选择相关
const spuSelectRef = ref()
const selectedProductName = ref('')
const convertSpuSelectRef = ref()
const selectedConvertProductName = ref('')

// 是否为映射模式
const isMappingMode = computed(() => {
  return formData.value.type === 2 && formData.value.linkMode === 1
})

const showConvertConfig = computed(() => formData.value.supportConvert && formData.value.type === 1)

// 表单验证规则
const formRules = reactive({
  name: [
    {
      required: true, // 改为始终必填(无论映射还是快照,都需要提交给服务端验证)
      message: '物料名称不能为空',
      trigger: 'blur'
    }
  ],
  code: [{ required: false }], // 物料编码改为非必填(后端自动生成)
  type: [{ required: true, message: '物料类型不能为空', trigger: 'change' }],
  baseUnit: [{ required: true, message: '基础单位不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  spuId: [
    {
      required: computed(() => formData.value.type === 2),
      message: '请选择关联商品',
      trigger: 'change'
    }
  ],
  linkMode: [
    {
      required: computed(() => formData.value.type === 2 && formData.value.spuId),
      message: '请选择关联模式',
      trigger: 'change'
    }
  ],
  convertedSpuId: [
    {
      required: computed(() => showConvertConfig.value),
      message: '请选择转化目标',
      trigger: 'change'
    }
  ],
  convertPrice: [
    {
      required: computed(() => showConvertConfig.value),
      message: '请输入转化单价',
      trigger: 'blur'
    },
    {
      validator: (_: any, value: any, callback: any) => {
        if (!showConvertConfig.value) {
          callback()
          return
        }
        if (value === '' || value === null || value === undefined) {
          callback(new Error('请输入转化单价'))
          return
        }
        const num = Number(value)
        if (Number.isNaN(num) || num < 0) {
          callback(new Error('转化单价需为非负数字'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
})
const formRef = ref() // 表单 Ref

/** 打开商品选择弹窗 */
const openSpuSelect = () => {
  spuSelectRef.value?.open()
}

const openConvertSpuSelect = () => {
  convertSpuSelectRef.value?.open(
    formData.value.convertedSpuId ? { spuId: formData.value.convertedSpuId } : undefined
  )
}

/** 商品选择确认回调 */
const onSpuSelected = async (spuId: number) => {
  formData.value.spuId = spuId

  // 默认选择映射模式
  if (formData.value.type === 2 && !formData.value.linkMode) {
    formData.value.linkMode = 1
  }

  // 调用后端接口获取SPU信息
  try {
    const spuInfo = await DefinitionApi.getSpuInfo(spuId)
    if (spuInfo) {
      // 保存预览数据(用于模式切换)
      spuPreviewData.value = {
        name: spuInfo.name,
        image: spuInfo.image,
        description: spuInfo.description
      }
      selectedProductName.value = spuInfo.name

      // 🔥 关键: 映射模式和快照模式都填充表单
      // 前端必须有值才能通过后端@NotBlank验证
      // 后端Service层根据linkMode判断是否设为null
      formData.value.name = spuInfo.name
      formData.value.image = spuInfo.image
      formData.value.description = spuInfo.description
    }
  } catch (error) {
    console.error('获取SPU信息失败:', error)
    selectedProductName.value = `商品ID: ${spuId}`
  }
}

const onConvertSpuSelected = async (spuId: number) => {
  formData.value.convertedSpuId = spuId
  try {
    const spuInfo = await DefinitionApi.getSpuInfo(spuId)
    selectedConvertProductName.value = spuInfo?.name ?? `商品ID: ${spuId}`
  } catch (error) {
    console.error('获取转化目标信息失败:', error)
    selectedConvertProductName.value = `商品ID: ${spuId}`
  }
  // 自动触发校验
  await nextTick()
  formRef.value?.clearValidate(['convertedSpuId'])
}

/** 清除选择的商品 */
const clearSelectedProduct = () => {
  formData.value.spuId = null
  formData.value.linkMode = null
  selectedProductName.value = ''
  spuPreviewData.value = null
  formData.value.name = null
  formData.value.image = null
  formData.value.description = null
}

const clearConvertTarget = () => {
  formData.value.convertedSpuId = null
  formData.value.convertPrice = ''
  selectedConvertProductName.value = ''
  formRef.value?.clearValidate(['convertedSpuId', 'convertPrice'])
}

/** 处理物料类型切换 */
const handleTypeChange = (type: number) => {
  if (type !== 2) {
    // 非成品无需关联模式
    formData.value.linkMode = null
  }
  if (type !== 1 && formData.value.supportConvert) {
    formData.value.supportConvert = false
  }
}

/** 处理关联模式切换 */
const handleLinkModeChange = (mode: number) => {
  if (!spuPreviewData.value) return

  // 无论映射还是快照,都填充表单(用于提交验证)
  // 后端会根据linkMode判断是否设为null
  formData.value.name = spuPreviewData.value.name || null
  formData.value.image = spuPreviewData.value.image || null
  formData.value.description = spuPreviewData.value.description || null
}

watch(
  () => formData.value.supportConvert,
  (enabled) => {
    if (!enabled) {
      clearConvertTarget()
    }
  }
)

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await DefinitionApi.getDefinition(id)
      formData.value = {
        ...(data as any),
        convertedSpuId: data.convertedSpuId ?? null,
        convertPrice: data.convertPrice != null ? fenToYuan(data.convertPrice) : ''
      }

      // 如果有关联的SPU，获取商品名称和预览信息
      if (formData.value.spuId) {
        try {
          const spuInfo = await DefinitionApi.getSpuInfo(formData.value.spuId)
          if (spuInfo) {
            selectedProductName.value = spuInfo.name
            spuPreviewData.value = {
              name: spuInfo.name,
              image: spuInfo.image,
              description: spuInfo.description
            }
          }
        } catch (error) {
          console.error('获取SPU信息失败:', error)
          selectedProductName.value = `商品ID: ${formData.value.spuId}`
        }
      }

      if (formData.value.convertedSpuId) {
        try {
          const targetSpuInfo = await DefinitionApi.getSpuInfo(formData.value.convertedSpuId)
          selectedConvertProductName.value = targetSpuInfo?.name ?? `商品ID: ${formData.value.convertedSpuId}`
        } catch (error) {
          console.error('获取转化目标信息失败:', error)
          selectedConvertProductName.value = `商品ID: ${formData.value.convertedSpuId}`
        }
      }
      if (formData.value.type !== 1) {
        formData.value.supportConvert = false
        clearConvertTarget()
      } else if (!formData.value.supportConvert) {
        clearConvertTarget()
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  // 校验表单
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 提交请求
  // 注意: 映射模式也会提交name/image/description的值
  // 后端Service层会根据linkMode判断是否设为null
  formLoading.value = true
  try {
    const submitData = { ...(formData.value as any) } as DefinitionApi.MaterialDefinitionVO
    if (showConvertConfig.value) {
      submitData.convertedSpuId = formData.value.convertedSpuId ?? null
      submitData.convertPrice = yuanToFen(formData.value.convertPrice || 0)
    } else {
      submitData.convertedSpuId = null
      submitData.convertPrice = null
    }
    if (formType.value === 'create') {
      await DefinitionApi.createDefinition(submitData)
      message.success(t('common.createSuccess'))
    } else {
      await DefinitionApi.updateDefinition(submitData)
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
    id: undefined,
    name: null,
    code: undefined,
    spuId: null,
    image: null,
    description: null,
    type: 1,
    linkMode: null,
    baseUnit: undefined,
    status: 1,
    supportOutbound: true,
    supportConvert: true,
    convertedSpuId: null,
    convertPrice: ''
  }
  selectedProductName.value = ''
  spuPreviewData.value = null
  clearConvertTarget()
  formRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.spu-select-container {
  display: flex;
  align-items: center;
  gap: 8px;

  .clear-btn {
    flex-shrink: 0;
  }
}

:deep(.el-input-group__append) {
  padding: 0;
}

:deep(.el-input-group__append .el-button) {
  margin: 0;
  border: none;
}
</style>
