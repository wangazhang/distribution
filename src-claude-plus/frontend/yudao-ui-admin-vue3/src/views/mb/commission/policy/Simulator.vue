<template>
  <ContentWrap>
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      class="grid gap-x-8 gap-y-2 md:grid-cols-2"
    >
      <el-form-item label="策略" prop="policyId">
        <el-select v-model="formData.policyId" placeholder="请选择策略" clearable filterable>
          <el-option
            v-for="item in policyOptions"
            :key="item.id"
            :label="`${item.displayName} (${item.policyCode})`"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务类型">
        <el-input v-model="selectedPolicyBizType" disabled placeholder="自动匹配" />
      </el-form-item>
      <el-form-item label="下单人等级" prop="selfLevel">
        <el-input-number v-model="formData.selfLevel" :min="0" />
      </el-form-item>
      <el-form-item label="直属上级等级">
        <el-input-number v-model="formData.parentLevel" :min="0" />
      </el-form-item>
      <el-form-item label="上上级等级">
        <el-input-number v-model="formData.grandParentLevel" :min="0" />
      </el-form-item>
      <el-form-item label="团队/分公司等级">
        <el-input-number v-model="formData.teamLevel" :min="0" />
      </el-form-item>
      <el-form-item label="单价(分)" prop="unitPrice">
        <el-input-number v-model="formData.unitPrice" :min="0" />
      </el-form-item>
      <el-form-item label="数量" prop="quantity">
        <el-input-number v-model="formData.quantity" :min="1" />
      </el-form-item>
      <el-form-item label="总价(分)">
        <el-input-number v-model="formData.totalPrice" :min="0" placeholder="可自动计算" />
      </el-form-item>
      <el-form-item label="商品ID">
        <el-input-number v-model="formData.productId" :min="0" />
      </el-form-item>
      <el-form-item label="套包ID">
        <el-input-number v-model="formData.packageId" :min="0" />
      </el-form-item>
    </el-form>
    <div class="mt-4 flex justify-end gap-3">
      <el-button @click="resetForm"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      <el-button type="primary" @click="handleSimulate" :loading="loading">
        <Icon icon="ep:cpu" class="mr-5px" /> 执行模拟
      </el-button>
    </div>
  </ContentWrap>

  <ContentWrap v-if="result">
    <div class="mb-3 flex justify-between items-center">
      <div>
        <div class="text-lg font-medium">模拟结果</div>
        <div class="text-13px text-gray-500">策略：{{ result.policyName }}（{{ result.policyCode }}）</div>
      </div>
      <div class="text-right">
        <div class="text-sm text-gray-500">订单金额：{{ amountFormatter(result.orderTotalPrice) }}</div>
        <div class="text-base font-semibold">总佣金：{{ amountFormatter(result.totalCommission) }}</div>
      </div>
    </div>
    <el-table :data="result.rules" border stripe>
      <el-table-column label="规则名称" prop="displayName" min-width="160" />
      <el-table-column label="流水类型" prop="bizCode" width="140" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE" :value="scope.row.bizCode" />
        </template>
      </el-table-column>
      <el-table-column label="出资流水类型" prop="fundBizCode" width="140" align="center">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE" :value="scope.row.fundBizCode" />
        </template>
      </el-table-column>
      <el-table-column label="匹配层级" prop="targetHierarchy" width="100" align="center" />
      <el-table-column label="匹配等级" prop="targetLevel" width="110" align="center" />
      <el-table-column label="金额来源" prop="amountSource" width="130" align="center" />
      <el-table-column label="金额模式" prop="amountMode" width="120" align="center" />
      <el-table-column label="金额参数" prop="amountValue" width="140" align="center">
        <template #default="scope">
          <span>{{ ruleAmountValueFormatter(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="资金账户" prop="fundAccount" width="130" align="center" />
      <el-table-column label="优先级" prop="priority" width="100" align="center" />
      <el-table-column label="分佣金额" prop="amount" width="130" align="center">
        <template #default="scope">
          <span>{{ amountFormatter(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="物料奖励" min-width="220">
        <template #default="{ row }">
          <span>{{ materialRewardFormatter(row.materials) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type {
  CommissionPolicySimpleRespVO,
  CommissionSimulationReqVO,
  CommissionSimulationRespVO,
  CommissionSimulationRuleRespVO
} from '@/api/mb/commission/policy'
import * as PolicyApi from '@/api/mb/commission/policy'
import { DICT_TYPE } from '@/utils/dict'
import { fenToYuan } from '@/utils'

const router = useRouter()
const message = useMessage()

defineOptions({ name: 'MbCommissionSimulator' })

const loading = ref(false)
const formRef = ref<FormInstance>()
const policyOptions = ref<CommissionPolicySimpleRespVO[]>([])
const selectedPolicyBizType = ref('')

const formData = reactive<CommissionSimulationReqVO>({
  policyId: undefined,
  selfLevel: undefined,
  parentLevel: undefined,
  grandParentLevel: undefined,
  teamLevel: undefined,
  unitPrice: 0,
  quantity: 1,
  totalPrice: undefined,
  productId: undefined,
  packageId: undefined
})

const formRules: FormRules = {
  policyId: [{ required: true, message: '请选择策略', trigger: 'change' }],
  selfLevel: [{ required: true, message: '请输入下单人等级', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const result = ref<CommissionSimulationRespVO | null>(null)

const loadPolicies = async () => {
  policyOptions.value = await PolicyApi.getOnlineCommissionPolicyList()
}

watch(
  () => formData.policyId,
  (id) => {
    if (!id) {
      selectedPolicyBizType.value = ''
      return
    }
    const policy = policyOptions.value.find((item) => item.id === id)
    selectedPolicyBizType.value = policy?.bizType || ''
  }
)

const handleSimulate = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (!formData.totalPrice && formData.unitPrice && formData.quantity) {
      formData.totalPrice = formData.unitPrice * formData.quantity
    }
    result.value = await PolicyApi.simulateCommissionPolicy(formData)
    if (!result.value) {
      message.warning('未命中任何规则')
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  result.value = null
  selectedPolicyBizType.value = ''
}

const ruleAmountValueFormatter = (rule: CommissionSimulationRuleRespVO) => {
  if (rule.amountMode === 'PERCENTAGE') {
    return `${rule.amountValue ?? 0}%`
  }
  if (rule.amountValue === undefined || rule.amountValue === null) {
    return '--'
  }
  return `${fenToYuan(rule.amountValue)} 元`
}

const amountFormatter = (value?: number) => {
  if (value === undefined || value === null) {
    return '--'
  }
  return `${fenToYuan(value)} 元`
}

const materialRewardFormatter = (materials?: CommissionSimulationRuleRespVO['materials']) => {
  if (!materials || materials.length === 0) {
    return '--'
  }
  return materials
    .map((item) => `${item.materialName || `物料-${item.materialId}`} x${item.quantity ?? 0}`)
    .join('，')
}

onMounted(async () => {
  await loadPolicies()
  if (router.currentRoute.value.query?.policyId) {
    formData.policyId = Number(router.currentRoute.value.query.policyId)
    formData.quantity = 1
  }
})
</script>
