<template>
  <doc-alert title="【交易】分销返佣" url="https://doc.example.com/mall/trade-brokerage/" />

  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="用户昵称" prop="userId">
        <el-select
          v-model="queryParams.userId"
          placeholder="请输入用户昵称搜索"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="searchUsers"
          :loading="userLoading"
          @change="handleUserChange"
          class="!w-240px"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          >
            <div class="flex items-center">
              <el-avatar :src="user.avatar" :size="20" class="mr-2" />
              <span>{{ user.nickname }}</span>
              <span class="text-gray-400 ml-2">(ID: {{ user.value }})</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="提现类型" prop="type">
        <el-select
          v-model="queryParams.type"
          placeholder="请选择提现类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_WITHDRAW_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="账号" prop="accountNo">
        <el-input
          v-model="queryParams.accountNo"
          placeholder="请输入账号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="提现银行" prop="bankName">
        <el-select
          v-model="queryParams.bankName"
          placeholder="请选择提现银行"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.BROKERAGE_BANK_NAME)"
            :key="dict.label"
            :label="dict.label"
            :value="dict.label"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_WITHDRAW_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="申请时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="success"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['trade:brokerage-withdraw:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="primary"
          plain
          :loading="financeExportLoading"
          @click="handleExportFinance"
          v-hasPermi="['trade:brokerage-withdraw:export']"
        >
          <Icon icon="ep:list" class="mr-5px" /> 待打款导出
        </el-button>
        <el-button
          type="warning"
          plain
          @click="openImportDialog"
          v-hasPermi="['trade:brokerage-withdraw:import']"
        >
          <Icon icon="ep:upload" class="mr-5px" /> 导入打款结果
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="编号" align="left" prop="id" min-width="60px" />
      <el-table-column label="用户信息" align="left" min-width="120px">
        <template #default="scope">
          <div>编号：{{ scope.row.userId }}</div>
          <div>昵称：{{ scope.row.userNickname }}</div>
        </template>
      </el-table-column>
      <el-table-column label="提现金额" align="left" prop="price" min-width="80px">
        <template #default="scope">
          <div>金　额：￥{{ fenToYuan(scope.row.price) }}</div>
          <div>手续费：￥{{ fenToYuan(scope.row.feePrice) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="提现方式" align="left" prop="type" min-width="120px">
        <template #default="scope">
          <div v-if="scope.row.type === BrokerageWithdrawTypeEnum.WALLET.type"> 余额 </div>
          <div v-else>
            {{ getDictLabel(DICT_TYPE.BROKERAGE_WITHDRAW_TYPE, scope.row.type) }}
            <span v-if="scope.row.accountNo">账号：{{ scope.row.accountNo }}</span>
          </div>
          <div v-if="scope.row.channelPayEnabled" class="text-xs text-[#13ce66] mt-1">
            渠道打款已开通
          </div>
          <template v-if="scope.row.type === BrokerageWithdrawTypeEnum.BANK.type">
            <div>真实姓名：{{ scope.row.name }}</div>
            <div>
              银行名称：{{ scope.row.bankName || '无' }}
            </div>
            <div>开户地址：{{ scope.row.bankAddress }}</div>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="收款码" align="left" prop="accountQrCodeUrl" min-width="70px">
        <template #default="scope">
          <el-image
            v-if="scope.row.accountQrCodeUrl"
            :src="scope.row.accountQrCodeUrl"
            class="h-40px w-40px"
            :preview-src-list="[scope.row.accountQrCodeUrl]"
            preview-teleported
          />
          <span v-else>无</span>
        </template>
      </el-table-column>
      <el-table-column
        label="申请时间"
        align="left"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="备注" align="left" prop="remark" />
      <el-table-column label="状态" align="left" prop="status" min-width="120px">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BROKERAGE_WITHDRAW_STATUS" :value="scope.row.status" />
          <div v-if="scope.row.auditTime" class="text-xs">
            时间：{{ formatDate(scope.row.auditTime) }}
          </div>
          <div v-if="scope.row.auditReason" class="text-xs">
            原因：{{ scope.row.auditReason }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="left" width="250px" fixed="right">
        <template #default="scope">
          <div class="operate-column">
            <template v-if="scope.row.status === BrokerageWithdrawStatusEnum.AUDITING.status">
              <div class="operate-row">
                <el-button
                  link
                  type="primary"
                  @click="handleApprove(scope.row.id)"
                  v-hasPermi="['trade:brokerage-withdraw:audit']"
                >
                  通过
                </el-button>
                <el-button
                  link
                  type="danger"
                  @click="openForm(scope.row.id)"
                  v-hasPermi="['trade:brokerage-withdraw:audit']"
                >
                  驳回
                </el-button>
              </div>
            </template>
            <template v-else-if="scope.row.status === BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.status">
              <div class="operate-row">
                <el-button
                  v-if="!scope.row.channelPayLocked"
                  link
                  type="primary"
                  @click="handleConfirmPay(scope.row.id)"
                  v-hasPermi="['trade:brokerage-withdraw:confirm']"
                >
                  手工打款
                </el-button>
                <el-button
                  v-if="scope.row.channelPayEnabled && !scope.row.channelPayLocked"
                  link
                  type="success"
                  @click="handleChannelPay(scope.row)"
                  v-hasPermi="['trade:brokerage-withdraw:channel-pay']"
                >
                  渠道打款
                </el-button>
              </div>
              <el-button
                v-if="scope.row.channelPayEnabled || scope.row.channelPayLocked"
                link
                type="info"
                @click="openChannelDetail(scope.row.id)"
              >
                渠道进度
              </el-button>
            </template>
            <template v-else>
              <el-button
                v-if="scope.row.channelPayEnabled || scope.row.channelPayLocked"
                link
                type="info"
                @click="openChannelDetail(scope.row.id)"
              >
                渠道进度
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <BrokerageWithdrawRejectForm ref="formRef" @success="getList" />
  <BrokerageWithdrawFinanceImportForm ref="financeImportRef" @success="getList" />

  <el-drawer
    v-model="channelDrawerVisible"
    size="520px"
    class="channel-drawer"
    :with-header="true"
    :close-on-click-modal="false"
  >
    <template #header>
      <div class="flex justify-between items-center w-full">
        <span>渠道打款进度</span>
        <el-space>
          <el-button text icon="ep:refresh" @click="refreshChannelDetail" />
          <el-button
            v-if="channelDetail?.transfer"
            type="primary"
            plain
            :loading="channelSyncLoading"
            @click="syncChannelDetail"
          >
            立即同步
          </el-button>
          <el-button
            v-if="canRetryChannelPay"
            type="danger"
            plain
            @click="openRetryDialog"
          >
            重新发起
          </el-button>
        </el-space>
      </div>
    </template>
    <div v-loading="channelDrawerLoading">
      <el-empty
        v-if="!channelDetail?.transfer"
        description="暂无渠道打款记录"
        :image-size="120"
        class="mt-10"
      />
      <template v-else>
        <el-card shadow="never" class="!border-none mb-4">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="提现编号">
              {{ channelDetail?.withdrawId }}
            </el-descriptions-item>
            <el-descriptions-item label="提现类型">
              {{ getDictLabel(DICT_TYPE.BROKERAGE_WITHDRAW_TYPE, channelDetail?.withdrawType) }}
            </el-descriptions-item>
            <el-descriptions-item label="提现金额">
              ￥{{ formatAmount(channelDetail?.withdrawPrice) }}
            </el-descriptions-item>
            <el-descriptions-item label="提现状态">
              {{ getDictLabel(DICT_TYPE.BROKERAGE_WITHDRAW_STATUS, channelDetail?.withdrawStatus) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="mb-4">
          <template #header>
            <div class="flex justify-between items-center">
              <span>渠道概览</span>
              <el-tag :type="transferStatusTagType(channelDetail?.transfer?.status)">
                {{ channelDetail?.transfer?.statusName }}
              </el-tag>
            </div>
          </template>
          <div class="text-sm text-gray-500 leading-6">
            <div>转账单号：{{ channelDetail?.transfer?.id }}</div>
            <div>渠道流水号：{{ channelDetail?.transfer?.channelTransferNo || '-' }}</div>
            <div>同步时间：{{ formatDate(channelDetail?.transfer?.updateTime) }}</div>
          </div>
          <el-alert
            v-if="channelDetail?.transfer?.channelErrorMsg"
            type="error"
            show-icon
            class="mt-3"
            :title="channelDetail?.transfer?.channelErrorMsg"
            :description="channelDetail?.transfer?.channelErrorCode"
          />
        </el-card>
        <el-card v-if="channelDetail?.transfer?.channelNotifyData" shadow="never" class="mb-4">
          <template #header>
            <span>渠道原始通知</span>
          </template>
          <div class="notify-raw">
            <pre>{{ prettyChannelNotify }}</pre>
          </div>
        </el-card>
        <el-card v-if="channelDetail?.transfer?.channelExtras" shadow="never" class="mb-4">
          <template #header>
            <span>渠道扩展参数</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item
              v-for="(value, key) in channelDetail?.transfer?.channelExtras"
              :key="key"
              :label="key"
            >
              {{ value || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        <el-card
          v-for="stage in displayedStages"
          :key="stage.stage"
          shadow="never"
          class="mb-3"
        >
          <template #header>
            <div class="flex justify-between items-center">
              <span>{{ stage.stage }}</span>
              <el-tag :type="stageStatusTagType(stage.status)">
                {{ stage.statusName || stage.status }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="渠道流水号">
              {{ stage.channelTransferNo || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="完成时间">
              {{ formatStageTime(stage.successTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="错误信息">
              <span v-if="stage.errorMessage">
                {{ stage.errorMessage }} {{ stage.errorCode ? `(${stage.errorCode})` : '' }}
              </span>
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </template>
    </div>
  </el-drawer>
  <el-dialog
    v-model="retryDialogVisible"
    title="输入打款口令"
    width="420px"
    :close-on-click-modal="false"
  >
    <el-alert
      type="warning"
      show-icon
      class="mb-4"
      title="再次发起渠道打款前请确认用户信息及金额无误"
    />
    <el-form>
      <el-form-item label="打款口令">
        <el-input
          v-model="retryForm.password"
          type="password"
          placeholder="请输入打款口令"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="retryDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="retryLoading" @click="submitRetry(retryForm.password)">
        确认重新发起
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { DICT_TYPE, getDictLabel, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import { dateFormatter, formatDate } from '@/utils/formatTime'
import * as BrokerageWithdrawApi from '@/api/mall/trade/brokerage/withdraw'
import BrokerageWithdrawRejectForm from './BrokerageWithdrawRejectForm.vue'
import BrokerageWithdrawFinanceImportForm from './BrokerageWithdrawFinanceImportForm.vue'
import { BrokerageWithdrawStatusEnum, BrokerageWithdrawTypeEnum } from '@/utils/constants'
import { fenToYuanFormat } from '@/utils/formatter'
import { fenToYuan } from '@/utils'
import * as MemberUserApi from '@/api/member/user'
import download from '@/utils/download'
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'

defineOptions({ name: 'BrokerageWithdraw' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const exportLoading = ref(false)
const financeExportLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: null,
  type: null,
  name: null,
  accountNo: null,
  bankName: null,
  status: null,
  auditReason: null,
  auditTime: [],
  remark: null,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

// 用户搜索相关
const userOptions = ref([]) // 用户选项列表
const userLoading = ref(false) // 用户搜索加载状态
const searchTimer = ref(null) // 防抖定时器

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await BrokerageWithdrawApi.getBrokerageWithdrawPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  userOptions.value = [] // 清空用户选项
  handleQuery()
}

/** 用户昵称搜索 */
const searchUsers = (query: string) => {
  if (!query || query.trim().length < 2) {
    userOptions.value = []
    return
  }

  // 防抖处理
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }

  searchTimer.value = setTimeout(async () => {
    try {
      userLoading.value = true
      const users = await MemberUserApi.getUserListByNickname(query.trim())
      userOptions.value = users.map(user => ({
        value: user.id,
        label: `${user.nickname} (ID: ${user.id})`,
        nickname: user.nickname,
        avatar: user.avatar
      }))
    } catch (error) {
      console.error('搜索用户失败:', error)
      userOptions.value = []
    } finally {
      userLoading.value = false
    }
  }, 300) // 300ms 防抖
}

/** 用户选择变化 */
const handleUserChange = (userId: number) => {
  // 当用户选择了具体用户后，使用该用户ID进行查询
  queryParams.userId = userId
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (id: number) => {
  formRef.value.open(id)
}

const financeImportRef = ref()
const openImportDialog = () => {
  financeImportRef.value.open()
}

const channelDrawerVisible = ref(false)
const channelDrawerLoading = ref(false)
const channelSyncLoading = ref(false)
// 渠道打款详情（包含提现概览 + 转账阶段）
// 渠道详情（包含提现信息 + 渠道阶段 + 原始报文）
const channelDetail = ref<BrokerageWithdrawApi.BrokerageWithdrawChannelTransferRespVO>()
const currentChannelWithdrawId = ref<number>()
// 可视化两个阶段（账户划拨/银行卡出款）
const displayedStages = computed(() => {
  const stages: BrokerageWithdrawApi.BrokerageWithdrawChannelTransferStageVO[] = []
  if (channelDetail.value?.transfer?.transferStage) {
    stages.push(channelDetail.value.transfer.transferStage)
  }
  if (channelDetail.value?.transfer?.withdrawStage) {
    stages.push(channelDetail.value.transfer.withdrawStage)
  }
  return stages
})

/** 审核通过 */
const handleApprove = async (id: number) => {
  try {
    loading.value = true
    await message.confirm('确定要审核通过吗？')
    await BrokerageWithdrawApi.approveBrokerageWithdraw(id)
    await message.success(t('common.success'))
    await getList()
  } finally {
    loading.value = false
  }
}

/** 确认打款 */
const handleConfirmPay = async (id: number) => {
  try {
    await message.confirm('确认该提现单已打款并提交银行处理？')
    loading.value = true
    await BrokerageWithdrawApi.confirmFinancePay({ id })
    await message.success(t('common.success'))
    await getList()
  } finally {
    loading.value = false
  }
}

/** 渠道打款 */
// 渠道打款：成功后锁定按钮，只允许查看进度
const handleChannelPay = async (row: BrokerageWithdrawApi.BrokerageWithdrawVO) => {
  try {
    await message.confirm('确认发起渠道打款？')
    loading.value = true
    await BrokerageWithdrawApi.channelPay(row.id)
    // 打款已发起，锁定渠道操作，仅保留进度入口
    row.channelPayLocked = true
    row.channelPayEnabled = false
    await message.success('已提交渠道打款')
    await getList()
    await openChannelDetail(row.id)
  } finally {
    loading.value = false
  }
}

// 点击“渠道进度”时打开抽屉并加载数据
const openChannelDetail = async (id: number) => {
  currentChannelWithdrawId.value = id
  channelDrawerVisible.value = true
  await fetchChannelDetail()
}

// 调用后端接口获取渠道详情
const fetchChannelDetail = async () => {
  if (!currentChannelWithdrawId.value) {
    return
  }
  try {
    channelDrawerLoading.value = true
    channelDetail.value = await BrokerageWithdrawApi.getChannelTransferDetail(currentChannelWithdrawId.value)
  } finally {
    channelDrawerLoading.value = false
  }
}

// 顶部刷新按钮
const refreshChannelDetail = async () => {
  if (channelDrawerVisible.value) {
    await fetchChannelDetail()
  }
}

// 立即同步：触发后台重新查询渠道
const syncChannelDetail = async () => {
  if (!currentChannelWithdrawId.value) {
    return
  }
  try {
    channelSyncLoading.value = true
    channelDetail.value = await BrokerageWithdrawApi.syncChannelTransfer(currentChannelWithdrawId.value)
    await message.success('已同步渠道状态')
  } finally {
    channelSyncLoading.value = false
  }
}

watch(channelDrawerVisible, visible => {
  if (!visible) {
    channelDetail.value = undefined
    currentChannelWithdrawId.value = undefined
  }
})

const formatAmount = (value?: number | null) => {
  if (value === null || value === undefined) {
    return '-'
  }
  return fenToYuan(value)
}

const transferStatusTagType = (status?: number) => {
  switch (status) {
    case 20:
      return 'success'
    case 30:
      return 'danger'
    case 10:
      return 'warning'
    default:
      return 'info'
  }
}

const stageStatusTagType = (status?: string) => {
  if (!status) {
    return 'info'
  }
  if (status.toUpperCase() === 'SUCCESS') {
    return 'success'
  }
  if (['FAILED', 'ERROR', 'CANCEL'].includes(status.toUpperCase())) {
    return 'danger'
  }
  return 'warning'
}

const formatStageTime = (time?: string) => {
  return time ? formatDate(time) : '--'
}

// 渠道原始 JSON 需要格式化后展示
const prettyChannelNotify = computed(() => {
  if (!channelDetail.value?.transfer?.channelNotifyData) {
    return ''
  }
  try {
    return JSON.stringify(JSON.parse(channelDetail.value.transfer.channelNotifyData), null, 2)
  } catch (error) {
    return channelDetail.value.transfer.channelNotifyData
  }
})

/** 导出全部 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await BrokerageWithdrawApi.exportBrokerageWithdraw(queryParams)
    download.excel(data, '佣金提现.xls')
  } finally {
    exportLoading.value = false
  }
}

/** 导出待打款 */
const handleExportFinance = async () => {
  try {
    await message.exportConfirm('是否导出当前筛选条件下的待打款提现单？')
    financeExportLoading.value = true
    const params = { ...queryParams }
    const data = await BrokerageWithdrawApi.exportFinanceBrokerageWithdraw(params)
    download.excel(data, '待打款提现单.xls')
  } finally {
    financeExportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})

/** 组件销毁时清理定时器 **/
onUnmounted(() => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
})
const retryDialogVisible = ref(false)
const retryLoading = ref(false)
const retryForm = reactive({
  password: ''
})

// 只有具备渠道打款能力时才允许重新发起
const canRetryChannelPay = computed(() => {
  return channelDetail.value?.channelPayEnabled
})

// 打开重新发起弹窗（根据配置决定是否需要口令）
const openRetryDialog = () => {
  if (!currentChannelWithdrawId.value || !canRetryChannelPay.value) {
    return
  }
  // 有口令要求则弹窗输入，否则直接提交
  if (channelDetail.value?.channelRetryPasswordRequired) {
    retryForm.password = ''
    retryDialogVisible.value = true
  } else {
    submitRetry('')
  }
}

// 提交重新发起操作并刷新抽屉
const submitRetry = async (password: string) => {
  if (!currentChannelWithdrawId.value) {
    return
  }
  try {
    retryLoading.value = true
    // 重新发起后后端立即返回最新渠道状态，直接刷新抽屉
    channelDetail.value = await BrokerageWithdrawApi.retryChannelPay({
      id: currentChannelWithdrawId.value,
      password
    })
    await message.success('已重新发起渠道打款')
    retryDialogVisible.value = false
  } finally {
    retryLoading.value = false
  }
}

</script>

<style scoped>
.operate-column {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.operate-row {
  display: flex;
  gap: 12px;
}

.operate-column :deep(.el-button) {
  padding: 0;
}

.notify-raw {
  max-height: 200px;
  padding: 8px;
  overflow: auto;
  background-color: #f6f8fa;
  border-radius: 4px;
}

.notify-raw pre {
  margin: 0;
  font-size: 12px;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
