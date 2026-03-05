<template>
  <el-dialog :title="dialogTitle" v-model="dialogVisible" width="640px" append-to-body>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="内部编码" prop="ruleCode">
        <el-input v-model="formData.ruleCode" placeholder="请输入内部编码" />
      </el-form-item>
      <el-form-item label="展示名称" prop="displayName">
        <el-input v-model="formData.displayName" placeholder="请输入展示名称" />
      </el-form-item>
      <el-form-item label="流水类型" prop="bizCode">
        <el-select v-model="formData.bizCode" placeholder="请选择流水类型" class="!w-240px">
          <el-option
            v-for="item in incomeBizCodeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="受益层级" prop="targetHierarchy">
        <el-select v-model="formData.targetHierarchy" placeholder="请选择层级" class="!w-240px">
          <el-option v-for="item in hierarchyOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标等级" prop="targetLevel">
        <el-select
          v-model="formData.targetLevel"
          placeholder="请选择目标等级"
          filterable
          class="!w-240px"
        >
          <el-option
            v-for="item in levelOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="邀请顺序" prop="inviteOrderType">
        <div class="flex flex-col gap-2">
          <el-radio-group v-model="formData.inviteOrderType">
            <el-radio label="ALWAYS">不限</el-radio>
            <el-radio label="EQ">指定第 N 位</el-radio>
            <el-radio label="BETWEEN">区间</el-radio>
          </el-radio-group>
          <div v-if="formData.inviteOrderType === 'EQ'" class="flex items-center gap-2">
            <span>第</span>
            <el-input-number v-model="formData.inviteOrderValue" :min="1" />
            <span>位被邀请者</span>
          </div>
          <div v-else-if="formData.inviteOrderType === 'BETWEEN'" class="flex items-center gap-2">
            <span>第</span>
            <el-input-number v-model="formData.inviteOrderStart" :min="1" />
            <span>至</span>
            <el-input-number v-model="formData.inviteOrderEnd" :min="formData.inviteOrderStart || 1" />
            <span>位</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="金额来源" prop="amountSource">
        <el-select v-model="formData.amountSource" placeholder="请选择金额来源" class="!w-240px">
          <el-option v-for="item in amountSourceOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额模式" prop="amountMode">
        <el-select v-model="formData.amountMode" placeholder="请选择金额模式" class="!w-240px">
          <el-option v-for="item in amountModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额数值" prop="amountValue">
        <el-input-number
          v-model="formData.amountValue"
          :min="0"
          :precision="amountValuePrecision"
          :step="amountValueStep"
          controls-position="right"
        />
        <span class="ml-10px text-12px text-gray-500">{{ amountValueHint }}</span>
      </el-form-item>
      <el-form-item label="资金账户" prop="fundAccount">
        <el-select v-model="formData.fundAccount" placeholder="请选择资金承担主体" class="!w-240px">
          <el-option v-for="item in hierarchyOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="出资流水类型" prop="fundBizCode">
        <el-select
          v-model="formData.fundBizCode"
          placeholder="请选择出资流水类型"
          class="!w-240px"
          :disabled="costBizCodeOptions.length === 0"
        >
          <el-option
            v-for="item in costBizCodeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <div v-if="costBizCodeOptions.length === 0" class="ml-10px text-12px text-gray-500">
          请先在字典“佣金业务类型”中新增出资向流水（名称包含“出资”）。
        </div>
      </el-form-item>

      <el-form-item label="物料奖励" prop="materials">
        <!-- 物料奖励配置区域：展示已选物料并可维护数量 -->
        <div class="material-reward w-full">
          <el-table
            v-if="formData.materials.length"
            :data="formData.materials"
            border
            class="material-reward__table mb-2"
          >
            <el-table-column label="物料" min-width="200">
              <template #default="{ row }">
                <div class="flex flex-col">
                  <span class="font-medium">
                    {{ row.materialName || `物料-${row.materialId}` }}
                  </span>
                  <span class="text-12px text-gray-500">ID：{{ row.materialId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="编码" prop="materialCode" width="160" />
            <el-table-column label="单位" prop="materialUnit" width="100" align="center">
              <template #default="{ row }">
                <span>{{ row.materialUnit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="140" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  controls-position="right"
                  @change="handleMaterialQuantityChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeMaterialRow($index)">
                  <Icon icon="ep:delete" />
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="尚未配置物料奖励" />
          <el-button type="primary" plain @click="openMaterialDialog">
            <Icon icon="ep:plus" class="mr-5px" /> 添加物料
          </el-button>
        </div>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-input-number v-model="formData.priority" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="规则状态" prop="status">
        <el-switch v-model="statusSwitch" active-text="启用" inactive-text="停用" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="materialDialogVisible" title="选择物料" width="720px" append-to-body>
    <div class="material-dialog__toolbar mb-3 flex gap-2">
      <el-input
        v-model="materialQuery.keyword"
        placeholder="输入物料名称或编码"
        clearable
        class="!w-260px"
        @keyup.enter="loadMaterialDialogList"
      />
      <el-button @click="loadMaterialDialogList"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
      <el-button @click="resetMaterialQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
    </div>
    <el-table
      v-loading="materialDialogLoading"
      :data="materialDialogList"
      height="360px"
      @selection-change="handleMaterialSelectionChange"
      row-key="id"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="90" align="center" />
      <el-table-column prop="name" label="物料名称" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="baseUnit" label="单位" width="100" align="center" />
    </el-table>
    <div class="material-dialog__pagination mt-3 flex justify-end">
      <el-pagination
        v-model:current-page="materialDialogPagination.pageNo"
        v-model:page-size="materialDialogPagination.pageSize"
        :total="materialDialogPagination.total"
        layout="prev, pager, next, jumper, ->, total"
        background
        @current-change="handleMaterialPageChange"
        @size-change="handleMaterialPageSizeChange"
      />
    </div>
    <template #footer>
      <el-button @click="materialDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirmMaterialDialog">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import * as RuleApi from '@/api/mb/commission/rule'
import type { CommissionRuleMaterialVO } from '@/api/mb/commission/rule'
import * as MemberLevelApi from '@/api/member/level'
import { DefinitionApi } from '@/api/material/definition'
import type { MaterialDefinitionVO } from '@/api/material/definition'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { fenToYuan, yuanToFen } from '@/utils'

const props = defineProps<{ policyId: number }>()
const emits = defineEmits<{ (e: 'success'): void }>()
const message = useMessage()

const hierarchyOptions = [
  { label: '自己', value: 'SELF' },
  { label: '直属上级', value: 'PARENT' },
  { label: '上上级', value: 'GRANDPARENT' },
  { label: '团队/分公司', value: 'TEAM' },
  { label: '平台', value: 'PLATFORM' },
  { label: '总部', value: 'HQ' }
]
const amountSourceOptions = [
  { label: '订单金额', value: 'ORDER_AMOUNT' },
  { label: '单价', value: 'UNIT_PRICE' },
  { label: '数量', value: 'QUANTITY' }
]
const amountModeOptions = [
  { label: '百分比', value: 'PERCENTAGE' },
  { label: '固定金额', value: 'FIXED' }
]

const levelOptions = ref<{ label: string; value: string }[]>([
  { label: '不限 (ALL)', value: 'ALL' }
])

const bizCodeOptions = computed(() => getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE))
const isCostOption = (option: { label: string }) =>
  option.label.includes('出资') || option.label.includes('扣减') || option.label.includes('成本')
const costBizCodeOptions = computed(() => bizCodeOptions.value.filter((item) => isCostOption(item)))
const incomeBizCodeOptions = computed(() => {
  const income = bizCodeOptions.value.filter((item) => !isCostOption(item))
  return income.length ? income : bizCodeOptions.value
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isUpdate = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
// 物料选择弹窗相关状态
const materialDialogVisible = ref(false)
const materialDialogLoading = ref(false)
const materialDialogList = ref<MaterialDefinitionVO[]>([])
const materialDialogSelection = ref<MaterialDefinitionVO[]>([])
const materialDialogPagination = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const materialQuery = reactive({ keyword: '' })

// 规则表单数据，materials 数组保存物料奖励配置
type RuleFormState = {
  id?: number
  policyId: number
  ruleCode: string
  displayName: string
  bizCode?: number
  fundBizCode?: number | null
  targetHierarchy: string
  targetLevel: string
  amountSource: string
  amountMode: string
  amountValue: number
  fundAccount: string
  priority: number
  status: 'ONLINE' | 'OFFLINE'
  remark?: string
  materials: CommissionRuleMaterialVO[]
  inviteOrderType: 'ALWAYS' | 'EQ' | 'BETWEEN'
  inviteOrderValue?: number | null
  inviteOrderStart?: number | null
  inviteOrderEnd?: number | null
}

const formData = reactive<RuleFormState>({
  policyId: 0,
  ruleCode: '',
  displayName: '',
  bizCode: incomeBizCodeOptions.value.length ? incomeBizCodeOptions.value[0].value : undefined,
  fundBizCode: costBizCodeOptions.value.length ? costBizCodeOptions.value[0].value : null,
  targetHierarchy: 'PARENT',
  targetLevel: 'ALL',
  amountSource: 'ORDER_AMOUNT',
  amountMode: 'PERCENTAGE',
  amountValue: 0,
  fundAccount: 'HQ',
  priority: 1,
  status: 'ONLINE',
  remark: undefined,
  materials: [],
  inviteOrderType: 'ALWAYS',
  inviteOrderValue: null,
  inviteOrderStart: null,
  inviteOrderEnd: null
})

const amountValuePrecision = computed(() => (formData.amountMode === 'PERCENTAGE' ? 4 : 2))
const amountValueStep = computed(() => (formData.amountMode === 'PERCENTAGE' ? 0.0001 : 0.01))
const amountValueHint = computed(() =>
  formData.amountMode === 'PERCENTAGE' ? '百分比使用 0-100' : '固定金额按元填写'
)

const statusSwitch = computed({
  get: () => formData.status !== 'OFFLINE',
  set: (value: boolean) => {
    formData.status = value ? 'ONLINE' : 'OFFLINE'
  }
})

watch(
  incomeBizCodeOptions,
  (options) => {
    if ((!formData.bizCode || !options.find((item) => item.value === formData.bizCode)) && options.length) {
      formData.bizCode = options[0].value
    }
  },
  { immediate: true }
)

watch(
  costBizCodeOptions,
  (options) => {
    if (
      (formData.fundBizCode === undefined ||
        formData.fundBizCode === null ||
        !options.some((item) => item.value === formData.fundBizCode)) &&
      options.length
    ) {
      formData.fundBizCode = options[0].value
    }
  },
  { immediate: true }
)

const validateMaterials = () => {
  formRef.value?.validateField('materials').catch(() => undefined)
}

const removeMaterialRow = (index: number) => {
  if (!formData.materials) {
    formData.materials = []
    return
  }
  formData.materials.splice(index, 1)
  validateMaterials()
}

const handleMaterialQuantityChange = (row: CommissionRuleMaterialVO) => {
  if (!row.quantity || row.quantity < 1) {
    row.quantity = 1
  }
  validateMaterials()
}

const openMaterialDialog = () => {
  materialDialogVisible.value = true
  materialDialogSelection.value = []
  materialDialogPagination.pageNo = 1
  loadMaterialDialogList()
}

// 根据条件类型查找规则配置，便于编辑时回显
const findCondition = (record: RuleApi.CommissionRuleVO | undefined, type: string) =>
  record?.conditions?.find((item) => item.conditionType === type)

// 兼容 value/value.values 两种表达方式，抽出第一个字符串
const readFirstStringValue = (value?: Record<string, any>, fallback?: string) => {
  if (!value) {
    return fallback
  }
  const values = value.values as string[] | undefined
  if (Array.isArray(values) && values.length) {
    return values[0]
  }
  if (typeof value.value === 'string') {
    return value.value
  }
  return fallback
}

// 邀请顺序条件 -> 表单字段的映射
const applyInviteCondition = (condition?: RuleApi.CommissionRuleConditionVO) => {
  if (!condition || condition.operator === 'ALWAYS') {
    formData.inviteOrderType = 'ALWAYS'
    formData.inviteOrderValue = null
    formData.inviteOrderStart = null
    formData.inviteOrderEnd = null
    return
  }
  if (condition.operator === 'EQ' || condition.operator === 'IN') {
    formData.inviteOrderType = 'EQ'
    const indexes = (condition.value?.indexes as number[] | undefined) ?? []
    formData.inviteOrderValue = indexes.length ? indexes[0] : null
    return
  }
  formData.inviteOrderType = 'BETWEEN'
  formData.inviteOrderStart = (condition.value?.min as number | undefined) ?? null
  formData.inviteOrderEnd = (condition.value?.max as number | undefined) ?? null
}

// 佣金动作携带了金额、流水类型、资金账户等信息，这里用于回显
const applyActionFields = (record: RuleApi.CommissionRuleVO) => {
  const action = record.actions?.find((item) => item.actionType === 'COMMISSION')
  const payload = action?.payload ?? {}
  formData.amountMode = action?.amountMode || 'PERCENTAGE'
  const rawAmount = Number(action?.amountValue ?? 0)
  formData.amountValue =
    formData.amountMode === 'FIXED' ? toDisplayFixedAmount(rawAmount) : rawAmount
  formData.amountSource =
    (payload.amountSource as string) || record.effectScope?.amountSource || 'ORDER_AMOUNT'
  formData.fundAccount =
    (payload.fundAccount as string) || record.effectScope?.fundAccount || 'HQ'
  formData.bizCode =
    (payload.bizCode as number | undefined) ??
    (incomeBizCodeOptions.value.length ? incomeBizCodeOptions.value[0].value : undefined)
  formData.fundBizCode =
    (payload.fundBizCode as number | undefined) ??
    (costBizCodeOptions.value.length ? costBizCodeOptions.value[0].value : null)
}

const loadMaterialDialogList = async () => {
  materialDialogLoading.value = true
  try {
    const res = await DefinitionApi.getDefinitionPage({
      pageNo: materialDialogPagination.pageNo,
      pageSize: materialDialogPagination.pageSize,
      name: materialQuery.keyword || undefined,
      status: 1
    })
    materialDialogList.value = res.list || []
    materialDialogPagination.total = res.total || 0
  } catch (error) {
    console.error('加载物料列表失败', error)
    materialDialogList.value = []
    materialDialogPagination.total = 0
  } finally {
    materialDialogLoading.value = false
  }
}

const resetMaterialQuery = () => {
  materialQuery.keyword = ''
  materialDialogPagination.pageNo = 1
  loadMaterialDialogList()
}

const handleMaterialSelectionChange = (rows: MaterialDefinitionVO[]) => {
  materialDialogSelection.value = rows
}

const handleMaterialPageChange = (page: number) => {
  materialDialogPagination.pageNo = page
  loadMaterialDialogList()
}

const handleMaterialPageSizeChange = (size: number) => {
  materialDialogPagination.pageSize = size
  materialDialogPagination.pageNo = 1
  loadMaterialDialogList()
}

const confirmMaterialDialog = () => {
  addMaterialRows(materialDialogSelection.value)
  materialDialogVisible.value = false
}

const addMaterialRows = (rows: MaterialDefinitionVO[]) => {
  if (!rows || rows.length === 0) {
    return
  }
  if (!formData.materials) {
    formData.materials = []
  }
  const existIds = new Set(formData.materials.map((item) => item.materialId))
  rows.forEach((row) => {
    if (!row || !row.id || existIds.has(row.id)) {
      return
    }
    formData.materials.push({
      materialId: row.id,
      materialName: row.name || row.code || `物料-${row.id}`,
      materialCode: row.code,
      materialImage: row.image,
      materialUnit: row.baseUnit,
      quantity: 1
    })
    existIds.add(row.id)
  })
  validateMaterials()
}

const formRules: FormRules = {
  ruleCode: [{ required: true, message: '内部编码不能为空', trigger: 'blur' }],
  displayName: [{ required: true, message: '展示名称不能为空', trigger: 'blur' }],
  bizCode: [{ required: true, message: '请选择流水类型', trigger: 'change' }],
  fundBizCode: [{ required: true, message: '请选择出资流水类型', trigger: 'change' }],
  targetHierarchy: [{ required: true, message: '请选择层级', trigger: 'change' }],
  targetLevel: [{ required: true, message: '请选择目标等级', trigger: 'change' }],
  amountSource: [{ required: true, message: '请选择金额来源', trigger: 'change' }],
  amountMode: [{ required: true, message: '请选择金额模式', trigger: 'change' }],
  amountValue: [{ required: true, message: '请输入金额数值', trigger: 'blur' }],
  fundAccount: [{ required: true, message: '请选择资金账户', trigger: 'change' }],
  priority: [{ required: true, message: '优先级不能为空', trigger: 'blur' }],
  materials: [
    {
      validator: (_, value, callback) => {
        const list = Array.isArray(value) ? (value as CommissionRuleMaterialVO[]) : []
        const invalid = list.some((item) => !item.materialId || !item.quantity || item.quantity <= 0)
        if (invalid) {
          callback(new Error('请完善物料奖励数量'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const toDisplayFixedAmount = (value?: number | null) => {
  const amount = Number(fenToYuan(value ?? 0))
  return Number.isNaN(amount) ? 0 : amount
}

// 生成与后端匹配的条件列表（层级、等级、邀请顺序），保持优先级顺序
const buildConditions = (): RuleApi.CommissionRuleConditionVO[] => {
  const conditions: RuleApi.CommissionRuleConditionVO[] = [
    {
      conditionType: 'TARGET_HIERARCHY',
      operator: 'EQ',
      value: { values: [formData.targetHierarchy] },
      priority: 10
    },
    {
      conditionType: 'TARGET_LEVEL',
      operator: formData.targetLevel === 'ALL' ? 'ALWAYS' : 'EQ',
      value:
        formData.targetLevel === 'ALL'
          ? { always: true }
          : { values: [formData.targetLevel] },
      priority: 20
    }
  ]
  if (formData.inviteOrderType === 'EQ' && formData.inviteOrderValue) {
    conditions.push({
      conditionType: 'INVITE_ORDER',
      operator: 'EQ',
      value: { indexes: [formData.inviteOrderValue] },
      priority: 30
    })
  } else if (formData.inviteOrderType === 'BETWEEN') {
    conditions.push({
      conditionType: 'INVITE_ORDER',
      operator: 'BETWEEN',
      value: {
        min: formData.inviteOrderStart ?? undefined,
        max: formData.inviteOrderEnd ?? undefined
      },
      priority: 30
    })
  } else {
    conditions.push({
      conditionType: 'INVITE_ORDER',
      operator: 'ALWAYS',
      value: { always: true },
      priority: 30
    })
  }
  return conditions
}

const normalizeAmountValue = () => {
  if (formData.amountMode === 'FIXED') {
    return yuanToFen(formData.amountValue ?? 0)
  }
  return Number(formData.amountValue ?? 0)
}

// 这里只维护佣金类动作，payload 携带业务类型等后续扩展字段
const buildActions = (): RuleApi.CommissionRuleActionVO[] => {
  return [
    {
      actionType: 'COMMISSION',
      amountMode: formData.amountMode,
      amountValue: normalizeAmountValue(),
      payload: {
        amountSource: formData.amountSource,
        fundAccount: formData.fundAccount,
        bizCode: formData.bizCode,
        fundBizCode: formData.fundBizCode
      },
      priority: 1
    }
  ]
}

// 物料奖励直接透传关键字段，避免提交冗余属性
const serializeMaterials = (): RuleApi.CommissionRuleMaterialVO[] => {
  return (formData.materials || []).map((item) => ({
    materialId: item.materialId,
    materialName: item.materialName,
    materialCode: item.materialCode,
    materialImage: item.materialImage,
    materialUnit: item.materialUnit,
    quantity: item.quantity ?? 1
  }))
}

const buildSubmitData = (): RuleApi.CommissionRuleVO => ({
  id: formData.id,
  policyId: formData.policyId,
  ruleCode: formData.ruleCode,
  displayName: formData.displayName,
  priority: formData.priority,
  status: formData.status,
  remark: formData.remark,
  conditions: buildConditions(),
  actions: buildActions(),
  materials: serializeMaterials()
})

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: undefined,
    policyId: props.policyId,
    ruleCode: '',
    displayName: '',
    bizCode: incomeBizCodeOptions.value.length ? incomeBizCodeOptions.value[0].value : 0,
    fundBizCode: costBizCodeOptions.value.length ? costBizCodeOptions.value[0].value : null,
    targetHierarchy: 'PARENT',
    targetLevel: 'ALL',
    amountSource: 'ORDER_AMOUNT',
    amountMode: 'PERCENTAGE',
    amountValue: 0,
    fundAccount: 'HQ',
    priority: 1,
    status: 'ONLINE',
    remark: undefined,
    materials: [],
    inviteOrderType: 'ALWAYS',
    inviteOrderValue: null,
    inviteOrderStart: null,
    inviteOrderEnd: null
  })
}

const open = (type: 'create' | 'update', record?: RuleApi.CommissionRuleVO) => {
  if (!props.policyId) {
    message.error('缺少策略信息，无法创建规则')
    return
  }
  resetForm()
  dialogVisible.value = true
  isUpdate.value = type === 'update'
  dialogTitle.value = type === 'create' ? '新增规则' : '编辑规则'
  formData.policyId = props.policyId
  if (type === 'update' && record) {
    formData.id = record.id
    formData.policyId = record.policyId
    formData.ruleCode = record.ruleCode
    formData.displayName = record.displayName
    formData.priority = record.priority
    formData.status = (record.status as RuleFormState['status']) || 'ONLINE'
    const hierarchyCond = findCondition(record, 'TARGET_HIERARCHY')
    const levelCond = findCondition(record, 'TARGET_LEVEL')
    formData.targetHierarchy =
      readFirstStringValue(hierarchyCond?.value, record.effectScope?.targetHierarchy || 'PARENT') || 'PARENT'
    formData.targetLevel =
      levelCond?.operator === 'ALWAYS'
        ? 'ALL'
        : readFirstStringValue(levelCond?.value, record.effectScope?.targetLevel || 'ALL') || 'ALL'
    applyInviteCondition(findCondition(record, 'INVITE_ORDER'))
    applyActionFields(record)
    formData.materials =
      record.materials?.map((item) => ({
        id: item.id,
        materialId: item.materialId,
        materialName: item.materialName,
        materialCode: item.materialCode,
        materialImage: item.materialImage,
        materialUnit: item.materialUnit,
        quantity: item.quantity
      })) ?? []
  }
}

defineExpose({ open })

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = buildSubmitData()
    if (isUpdate.value) {
      await RuleApi.updateCommissionRule(payload)
    } else {
      await RuleApi.createCommissionRule(payload)
    }
    message.success(isUpdate.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    emits('success')
  } finally {
    submitLoading.value = false
  }
}

const loadLevelOptions = async () => {
  try {
    const res = await MemberLevelApi.getLevelList({ status: 0 })
    levelOptions.value = [
      { label: '不限 (ALL)', value: 'ALL' },
      ...res.map((item: any) => ({
        label: `${item.name}${item.level !== undefined ? `（等级 ${item.level}）` : ''}`,
        value: item.level !== undefined ? String(item.level) : String(item.id)
      }))
    ]
  } catch (error) {
    console.error('加载会员等级失败', error)
  }
}

onMounted(() => {
  loadLevelOptions()
})
</script>
