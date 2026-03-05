<template>
  <ContentWrap>
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      class="-mb-15px"
      label-width="80px"
    >
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="输入名称/编码"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select
          v-model="queryParams.bizType"
          placeholder="请选择业务类型"
          clearable
          class="!w-200px"
        >
          <el-option
            v-for="item in bizTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-200px">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="openPolicyForm('create')" v-hasPermi="['mb:commission-policy:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增策略
        </el-button>
        <el-button type="success" plain @click="handleSimulator">
          <Icon icon="ep:cpu" class="mr-5px" /> 分佣模拟
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      ref="policyTableRef"
      v-loading="loading"
      :data="list"
      stripe
      highlight-current-row
      @row-click="handlePolicyRowClick"
      @current-change="handlePolicyCurrentChange"
      :row-key="(row) => row.id"
    >
      <el-table-column label="策略名称" prop="displayName" min-width="160" />
      <el-table-column label="策略编码" prop="policyCode" min-width="160" />
      <el-table-column label="业务类型" prop="bizType" min-width="140" />
      <el-table-column label="版本" prop="versionNo" width="80" align="center" />
      <el-table-column label="状态" prop="status" width="110" align="center">
        <template #default="scope">
          <el-tag
            :type="scope.row.status === 'ONLINE' ? 'success' : scope.row.status === 'DRAFT' ? 'info' : 'warning'"
          >
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" min-width="180" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="220" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click.stop="openPolicyForm('update', scope.row.id)"
            v-hasPermi="['mb:commission-policy:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click.stop="handlePublish(scope.row)"
            v-if="scope.row.status !== 'ONLINE'"
            v-hasPermi="['mb:commission-policy:publish']"
          >
            发布
          </el-button>
          <el-button
            link
            type="warning"
            @click.stop="handleOffline(scope.row)"
            v-if="scope.row.status === 'ONLINE'"
            v-hasPermi="['mb:commission-policy:offline']"
          >
            下线
          </el-button>
          <el-button
            link
            type="info"
            @click.stop="handleClone(scope.row.id)"
            v-hasPermi="['mb:commission-policy:clone']"
          >
            克隆
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <ContentWrap>
    <div class="mb-15px flex items-center justify-between">
      <div>
        <span class="text-base font-medium">规则列表</span>
        <span v-if="selectedPolicyName" class="ml-8 text-13px text-gray-500">
          当前策略：{{ selectedPolicyName }}
        </span>
        <span v-else class="ml-8 text-13px text-gray-500">请选择策略查看规则</span>
      </div>
      <div class="flex gap-2">
        <el-button
          type="primary"
          :disabled="!selectedPolicyId"
          @click="openRuleForm('create')"
          v-hasPermi="['mb:commission-rule:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增规则
        </el-button>
        <el-button @click="refreshRuleList" :disabled="!selectedPolicyId">
          <Icon icon="ep:refresh" class="mr-5px" /> 刷新
        </el-button>
      </div>
    </div>
    <el-table v-loading="ruleLoading" :data="rulePageList" stripe :show-overflow-tooltip="true">
      <el-table-column label="内部编码" prop="ruleCode" min-width="200" />
      <el-table-column label="展示名称" prop="displayName" min-width="200" />
      <el-table-column label="匹配层级" prop="targetHierarchyLabel" width="130" align="center" />
      <el-table-column label="匹配等级" prop="targetLevelLabel" width="150" align="center" />
      <el-table-column label="邀请顺序" prop="inviteOrderLabel" width="100" align="center" />
      <el-table-column label="金额来源" prop="amountSourceLabel" width="100" align="center" />
      <el-table-column label="金额模式" prop="amountModeLabel" width="100" align="center" />
      <el-table-column label="金额参数" prop="amountValueDisplay" width="120" align="center" />
      <el-table-column label="物料奖励" min-width="220" :show-overflow-tooltip="false">
        <template #default="{ row }">
          <el-tooltip
            v-if="row.materialRewards.length"
            placement="top"
            popper-class="material-reward-tooltip"
          >
            <template #content>
              <div class="material-reward-tooltip__content">
                <span v-for="item in row.materialRewards" :key="item">{{ item }}</span>
              </div>
            </template>
            <div class="material-reward-cell">
              <span v-for="item in row.materialRewards" :key="item">{{ item }}</span>
            </div>
          </el-tooltip>
          <span v-else>--</span>
        </template>
      </el-table-column>
      <el-table-column label="出资账户" prop="fundAccountLabel" width="100" align="center" />
      <el-table-column label="流水类型" width="200" align="center">
        <template #default="{ row }">
          <span>{{ row.bizCodeLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出资流水类型" width="200" align="center">
        <template #default="{ row }">
          <span>{{ row.fundBizCodeLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column label="优先级" prop="priority" width="100" align="center" />
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ONLINE' ? 'success' : 'info'">
            {{ row.status === 'ONLINE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="230" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openRuleForm('update', scope.row)"
            v-hasPermi="['mb:commission-rule:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleEnableRule(scope.row)"
            v-if="scope.row.status !== 'ONLINE'"
            v-hasPermi="['mb:commission-rule:enable']"
          >
            启用
          </el-button>
          <el-button
            link
            type="warning"
            @click="handleDisableRule(scope.row)"
            v-if="scope.row.status === 'ONLINE'"
            v-hasPermi="['mb:commission-rule:disable']"
          >
            停用
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDeleteRule(scope.row)"
            v-hasPermi="['mb:commission-rule:delete']"
          >
            删除
          </el-button>
          <el-button
            link
            type="info"
            @click="handleCloneRule(scope.row)"
            v-hasPermi="['mb:commission-rule:clone']"
          >
            克隆
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="ruleList.length"
      v-model:page="rulePagination.pageNo"
      v-model:limit="rulePagination.pageSize"
      @pagination="handleRulePageChange"
    />
  </ContentWrap>

  <PolicyForm ref="policyFormRef" @success="getList" />
  <RuleForm ref="ruleFormRef" :policy-id="selectedPolicyId || 0" @success="refreshRuleList" />
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PolicyForm from './PolicyForm.vue'
import RuleForm from '@/views/mb/commission/rule/RuleForm.vue'
import * as PolicyApi from '@/api/mb/commission/policy'
import * as RuleApi from '@/api/mb/commission/rule'
import * as MemberLevelApi from '@/api/member/level'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { fenToYuan } from '@/utils'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'MbCommissionPolicy' })

const router = useRouter()
const message = useMessage()
const { t } = useI18n()

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'ONLINE' },
  { label: '已下线', value: 'OFFLINE' }
]

const bizTypeOptions = [
  { label: '补货订单（restock）', value: 'restock' },
  { label: '商城商品订单（mall_goods_order）', value: 'mall_goods_order' },
  { label: '套包订单（package_order）', value: 'package_order' }
]

const loading = ref(true)
const list = ref<PolicyApi.CommissionPolicyVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const policyTableRef = ref()
const queryParams = reactive<PolicyApi.CommissionPolicyPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined,
  status: undefined,
  bizType: undefined
})

const policyFormRef = ref()
const ruleFormRef = ref()

const selectedPolicyId = ref<number | null>(null)
const selectedPolicyName = ref('')
const ruleLoading = ref(false)
const ruleList = ref<RuleApi.CommissionRuleVO[]>([])
const rulePagination = reactive({ pageNo: 1, pageSize: 10 })

const hierarchyMap: Record<string, string> = {
  SELF: '自己',
  PARENT: '直属上级',
  GRANDPARENT: '上上级',
  TEAM: '团队/分公司',
  PLATFORM: '平台',
  HQ: '总部'
}

const amountSourceMap: Record<string, string> = {
  ORDER_AMOUNT: '成交金额',
  UNIT_PRICE: '商品单价',
  QUANTITY: '数量'
}

const amountModeMap: Record<string, string> = {
  PERCENTAGE: '百分比',
  FIXED: '固定金额'
}

// 根据 effectScope.inviteOrder 的运算符生成可读文案
const renderInviteOrder = (invite?: {
  operator?: string
  minIndex?: number | null
  maxIndex?: number | null
  indexes?: number[] | null
}) => {
  if (!invite || !invite.operator || invite.operator === 'ALWAYS') {
    return '不限'
  }
  if (invite.operator === 'EQ' || invite.operator === 'IN') {
    const value = invite.indexes && invite.indexes.length ? invite.indexes[0] : undefined
    return value ? `第 ${value} 位` : '指定序号'
  }
  if (invite.operator === 'BETWEEN') {
    const min = invite.minIndex ?? 1
    const max = invite.maxIndex
    return max ? `第 ${min}-${max} 位` : `第 ${min} 位起`
  }
  if (invite.operator === 'GE') {
    return `第 ${invite.minIndex ?? 1} 位及以后`
  }
  if (invite.operator === 'LE') {
    return `第 ${invite.maxIndex ?? 1} 位及以前`
  }
  return '不限'
}

const bizCodeOptions = computed(() => getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE))

const renderHierarchy = (value?: string) => hierarchyMap[value || ''] || value || '--'
const renderAmountSource = (value?: string) => amountSourceMap[value || ''] || value || '--'
const renderAmountMode = (value?: string) => amountModeMap[value || ''] || value || '--'
const renderBizCode = (value?: number) => {
  if (value === undefined || value === null) {
    return '--'
  }
  const option = bizCodeOptions.value.find((item) => item.value === value)
  return option ? option.label : `编码 ${value}`
}

const levelLabelMap = ref<Record<string, string>>({ ALL: '不限 (ALL)' })

const formatDateTime = (value?: string | number | Date) => {
  return value ? formatDate(value, 'YYYY-MM-DD HH:mm:ss') : '--'
}

const loadLevelLabels = async () => {
  try {
    const list = await MemberLevelApi.getLevelList({ status: 0 })
    const map: Record<string, string> = { ALL: '不限 (ALL)' }
    list.forEach((item: any) => {
      const key = String(item.level ?? item.value ?? item.id)
      map[key] = `${item.name}${item.level !== undefined ? `（等级 ${item.level}）` : ''}`
    })
    levelLabelMap.value = map
  } catch (err) {
    console.error('加载会员等级失败', err)
  }
}

const renderLevel = (value?: string) => {
  if (!value) {
    return '--'
  }
  return levelLabelMap.value[value] || value
}

const extractCommissionAction = (rule: RuleApi.CommissionRuleVO) =>
  rule.actions?.find((action) => action.actionType === 'COMMISSION')

const renderAmountValue = (action?: RuleApi.CommissionRuleActionVO) => {
  if (!action) {
    return '--'
  }
  if (action.amountMode === 'PERCENTAGE') {
    return `${action.amountValue ?? 0}%`
  }
  if (action.amountValue === undefined || action.amountValue === null) {
    return '--'
  }
  return `${fenToYuan(action.amountValue)} 元`
}

const rulePageList = computed(() => {
  const start = (rulePagination.pageNo - 1) * rulePagination.pageSize
  const end = start + rulePagination.pageSize
  return ruleList.value.slice(start, end).map((item) => {
    const action = extractCommissionAction(item)
    const payload = action?.payload ?? {}
    const scope = item.effectScope
    return {
      ...item,
      targetHierarchyLabel: renderHierarchy(scope?.targetHierarchy),
      fundAccountLabel: renderHierarchy(scope?.fundAccount || (payload.fundAccount as string)),
      amountSourceLabel: renderAmountSource(
        (payload.amountSource as string) || scope?.amountSource
      ),
      amountModeLabel: renderAmountMode(action?.amountMode),
      targetLevelLabel: renderLevel(scope?.targetLevel),
      amountValueDisplay: renderAmountValue(action),
      bizCodeLabel: renderBizCode(payload.bizCode as number | undefined),
      fundBizCodeLabel: renderBizCode(payload.fundBizCode as number | undefined),
      inviteOrderLabel: renderInviteOrder(scope?.inviteOrder),
      materialRewards:
        item.materials && item.materials.length
          ? item.materials.map(
              (material) => `${material.materialName || `物料-${material.materialId}`} ×${material.quantity ?? 0}`
            )
          : []
    }
  })
})

const getList = async () => {
  loading.value = true
  try {
    const res = await PolicyApi.getCommissionPolicyPage(queryParams)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
  await nextTick(() => {
    if (list.value.length === 0) {
      selectedPolicyId.value = null
      selectedPolicyName.value = ''
      ruleList.value = []
      return
    }
    const current = list.value.find((item) => item.id === selectedPolicyId.value) || list.value[0]
    policyTableRef.value?.setCurrentRow(current)
    handlePolicySelection(current)
  })
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const statusLabel = (value?: string) => {
  const item = statusOptions.find((option) => option.value === value)
  return item ? item.label : value || '--'
}

const handleSimulator = () => {
  router.push('/mb/commission/policy/simulator')
}

const openPolicyForm = (type: 'create' | 'update', id?: number) => {
  policyFormRef.value.open(type, id)
}

const handlePublish = async (row: PolicyApi.CommissionPolicyVO) => {
  await message.confirm(`确定发布【${row.displayName}】吗？`)
  await PolicyApi.publishCommissionPolicy(row.id!)
  message.success(t('common.updateSuccess'))
  await getList()
}

const handleOffline = async (row: PolicyApi.CommissionPolicyVO) => {
  await message.confirm(`确定下线【${row.displayName}】吗？`)
  await PolicyApi.offlineCommissionPolicy(row.id!)
  message.success(t('common.updateSuccess'))
  await getList()
}

const handleClone = async (id: number) => {
  await message.confirm('确定克隆该策略吗？')
  await PolicyApi.cloneCommissionPolicy(id)
  message.success(t('common.saveSuccess'))
  await getList()
}

const handlePolicySelection = (row?: PolicyApi.CommissionPolicyVO) => {
  if (!row) {
    selectedPolicyId.value = null
    selectedPolicyName.value = ''
    ruleList.value = []
    return
  }
  if (selectedPolicyId.value === row.id) {
    return
  }
  selectedPolicyId.value = row.id
  selectedPolicyName.value = row.displayName
  rulePagination.pageNo = 1
  loadRuleList()
}

const handlePolicyRowClick = (row: PolicyApi.CommissionPolicyVO) => {
  handlePolicySelection(row)
}

const handlePolicyCurrentChange = (row?: PolicyApi.CommissionPolicyVO) => {
  handlePolicySelection(row)
}

const loadRuleList = async () => {
  if (!selectedPolicyId.value) {
    ruleList.value = []
    return
  }
  ruleLoading.value = true
  try {
    ruleList.value = await RuleApi.getCommissionRuleList(selectedPolicyId.value)
  } finally {
    ruleLoading.value = false
  }
}

const refreshRuleList = async () => {
  await loadRuleList()
}

const handleRulePageChange = () => {
  // 前端分页，computed 自动更新
}

const openRuleForm = (type: 'create' | 'update', record?: RuleApi.CommissionRuleVO) => {
  if (!selectedPolicyId.value) {
    message.warning('请先选择策略')
    return
  }
  ruleFormRef.value.open(type, record)
}

const handleEnableRule = async (record: RuleApi.CommissionRuleVO) => {
  await message.confirm(`确认启用规则【${record.displayName}】吗？`)
  await RuleApi.enableCommissionRule(record.id!)
  message.success('启用成功')
  await loadRuleList()
}

const handleDisableRule = async (record: RuleApi.CommissionRuleVO) => {
  await message.confirm(`确认停用规则【${record.displayName}】吗？`)
  await RuleApi.disableCommissionRule(record.id!)
  message.success('停用成功')
  await loadRuleList()
}

const handleDeleteRule = async (record: RuleApi.CommissionRuleVO) => {
  await message.delConfirm()
  await RuleApi.deleteCommissionRule(record.id!)
  message.success('删除成功')
  await loadRuleList()
}

const handleCloneRule = async (record: RuleApi.CommissionRuleVO) => {
  await message.confirm(`确认克隆规则【${record.displayName}】吗？`)
  await RuleApi.cloneCommissionRule(record.id!)
  message.success('克隆成功')
  await loadRuleList()
}

onMounted(async () => {
  await loadLevelLabels()
  await getList()
})
</script>

<style scoped>
.material-reward-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 20px;

  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.material-reward-tooltip__content {
  display: flex;
  flex-direction: column;
  gap: 4px;

  span {
    display: block;
    max-width: none;
    white-space: nowrap;
  }
}
</style>
