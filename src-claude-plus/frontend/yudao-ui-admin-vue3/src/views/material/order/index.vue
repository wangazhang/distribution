<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="90px"
    >
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="支付单号" prop="paymentId">
        <el-input
          v-model="queryParams.paymentId"
          placeholder="请输入支付单号"
          clearable
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="代理昵称" prop="agentUserName">
        <el-input
          v-model="queryParams.agentUserName"
          placeholder="请输入代理昵称"
          clearable
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入物料/商品名称"
          clearable
          class="!w-220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select
          v-model="queryParams.bizType"
          placeholder="全部"
          clearable
          class="!w-220px"
        >
          <el-option
            v-for="item in bizTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="全部"
          clearable
          class="!w-220px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-320px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-hasPermi="['mb:order:export']"
        >
          <Icon icon="ep:download" class="mr-5px" />
          导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column label="订单号" min-width="160">
        <template #default="{ row }">
          <el-tooltip effect="dark" :content="row.orderNo || row.id" placement="top">
            <div class="font-medium text-ellipsis">
              {{ row.orderNo || row.id }}
            </div>
          </el-tooltip>
          <div class="text-xs text-gray-500">ID：{{ row.id }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status] || 'info'">
            {{ row.statusName || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品信息" min-width="220">
        <template #default="{ row }">
          <div class="flex items-center gap-10px">
            <el-image
              v-if="row.productImage"
              :src="row.productImage"
              fit="cover"
              class="h-60px w-60px rounded-md border border-gray-100"
              loading="lazy"
            >
              <template #error>
                <div class="flex h-full w-full items-center justify-center bg-gray-100 text-gray-400">
                  <Icon icon="ep:picture-outline" />
                </div>
              </template>
            </el-image>
            <div class="flex-1">
              <el-tooltip
                v-if="row.productName"
                effect="dark"
                :content="row.productName"
                placement="top"
              >
                <div class="font-medium leading-5 text-ellipsis">
                  {{ row.productName }}
                </div>
              </el-tooltip>
              <div v-else class="font-medium leading-5">-</div>
              <div class="text-xs text-gray-500">商品 ID：{{ row.productId || '-' }}</div>
              <div class="text-xs text-gray-500">
                <el-tooltip
                  v-if="row.bizType"
                  effect="dark"
                  :content="bizTypeLabel[row.bizType] || row.bizType"
                  placement="top"
                >
                  <div class="text-ellipsis">
                    {{ bizTypeLabel[row.bizType] || row.bizType }}
                  </div>
                </el-tooltip>
                <span v-else>-</span>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="80" align="center" />
      <el-table-column label="单价(元)" width="120" align="right">
        <template #default="{ row }">
          {{ row.unitPriceDisplay || fenToYuan(row.unitPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="总价(元)" width="120" align="right">
        <template #default="{ row }">
          {{ row.totalPriceDisplay || fenToYuan(row.totalPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="代理用户" min-width="200">
        <template #default="{ row }">
          <div class="font-medium leading-5">
            <el-tooltip
              v-if="row.agentUserNickname"
              effect="dark"
              :content="row.agentUserNickname"
              placement="top"
            >
              <div class="text-ellipsis">
                {{ row.agentUserNickname }}
              </div>
            </el-tooltip>
            <span v-else>-</span>
          </div>
          <div class="text-xs text-gray-500">
            ID：{{ row.agentUserId || '-' }}
          </div>
          <div class="text-xs text-gray-500">
            <el-tooltip
              v-if="row.agentUserMobile"
              effect="dark"
              :content="row.agentUserMobile"
              placement="top"
            >
              <div class="text-ellipsis">
                {{ row.agentUserMobile }}
              </div>
            </el-tooltip>
            <span v-else>-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="支付单号" min-width="160">
        <template #default="{ row }">
          <el-tooltip
            v-if="row.paymentId"
            effect="dark"
            :content="row.paymentId"
            placement="top"
          >
            <div class="text-ellipsis">
              {{ row.paymentId }}
            </div>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">
          {{ formatDateSafe(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="170">
        <template #default="{ row }">
          {{ formatDateSafe(row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">
            详情
          </el-button>
          <el-dropdown
            trigger="click"
            @command="(command: string) => handleUpdateStatus(row, command)"
            v-hasPermi="['mb:order:update-status']"
          >
            <el-button link type="primary" :loading="updatingId === row.id">
              状态
              <Icon icon="ep:d-arrow-right" class="ml-5px" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in statusOptions"
                  :key="item.value"
                  :command="item.value"
                  :disabled="item.value === row.status"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            v-if="row.refundable"
            link
            type="danger"
            :loading="refundingId === row.id"
            @click="handleRefund(row)"
            v-hasPermi="['mb:order:refund']"
          >
            退款
          </el-button>
          <el-button
            v-if="row.status === 'COMPLETED' && row.paymentId"
            link
            type="warning"
            :loading="retryingId === row.id"
            @click="handleRetryVirtualDelivery(row.id)"
            v-hasPermi="['mb:order:retry-virtual-delivery']"
          >
            重推发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-if="total > 0"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="handlePagination"
    />
  </ContentWrap>

  <el-drawer v-model="detailVisible" title="订单详情" size="520px" destroy-on-close>
    <el-skeleton v-if="detailLoading" :rows="6" animated />
    <div v-else-if="detailData" class="space-y-18px">
      <section>
        <div class="mb-10px flex items-center justify-between">
          <h3 class="text-base font-semibold">基本信息</h3>
          <el-tag :type="statusTagType[detailData.status] || 'info'">
            {{ detailData.statusName || detailData.status }}
          </el-tag>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">
            {{ detailData.orderNo || detailData.id }}
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">
            {{ bizTypeLabel[detailData.bizType] || detailData.bizType }}
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            {{ detailData.totalPriceDisplay || fenToYuan(detailData.totalPrice) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="数量">
            {{ detailData.quantity }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateSafe(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateSafe(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section>
        <h3 class="mb-10px text-base font-semibold">商品信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品名称">
            {{ detailData.productName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品 ID">
            {{ detailData.productId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="单价">
            {{ detailData.unitPriceDisplay || fenToYuan(detailData.unitPrice) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="商品图片">
            <el-image
              v-if="detailData.productImage"
              :src="detailData.productImage"
              fit="cover"
              class="h-80px w-80px rounded-md border border-gray-100"
            >
              <template #error>
                <div class="flex h-full w-full items-center justify-center bg-gray-100 text-gray-400">
                  <Icon icon="ep:picture-outline" />
                </div>
              </template>
            </el-image>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section>
        <h3 class="mb-10px text-base font-semibold">用户信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="代理用户 ID">
            {{ detailData.agentUserId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="代理用户昵称">
            {{ detailData.agentUserNickname || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ detailData.agentUserMobile || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section>
        <div class="mb-10px flex items-center justify-between">
          <h3 class="text-base font-semibold">支付信息</h3>
          <div class="flex items-center gap-10px">
            <el-button
              v-if="detailData.canRetryVirtualDelivery"
              type="warning"
              plain
              size="small"
              :loading="retryingId === detailData.id"
              @click="handleRetryVirtualDelivery(detailData.id)"
              v-hasPermi="['mb:order:retry-virtual-delivery']"
            >
              重推虚拟发货
            </el-button>
            <el-button
              v-if="detailData.refundable"
              type="danger"
              plain
              size="small"
              :loading="refundingId === detailData.id"
              @click="handleRefund(detailData)"
              v-hasPermi="['mb:order:refund']"
            >
              退款
            </el-button>
            <el-button
              v-if="detailData.paymentId"
              type="primary"
              plain
              size="small"
              @click="handleQueryChannelOrder(detailData.paymentId)"
            >
              查询渠道订单
            </el-button>
          </div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="支付单号">
            {{ detailData.paymentId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付渠道">
            {{ detailData.payChannelCode || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            {{ detailData.payStatusName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">
            {{ detailData.payPriceDisplay || fenToYuan(detailData.payPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="支付成功时间">
            {{ formatDateSafe(detailData.paySuccessTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </div>
    <div v-else class="py-30px text-center text-gray-400">暂无详情</div>
  </el-drawer>

  <el-dialog
    v-model="channelOrderDialogVisible"
    title="渠道订单详情"
    width="640px"
    append-to-body
    @close="channelOrderData = null"
  >
    <el-skeleton v-if="channelOrderLoading" :rows="5" animated />
    <div v-else-if="channelOrderData" class="space-y-16px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="渠道状态">
          {{ channelOrderData.statusName || channelOrderData.status || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="妥投状态">
          {{ channelOrderData.deliveryStatusName || channelOrderData.deliveryStatus || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="商户订单号">
          {{ channelOrderData.outTradeNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="渠道订单号">
          {{ channelOrderData.channelOrderNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="渠道流水号">
          {{ channelOrderData.channelTransactionNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="渠道用户">
          {{ channelOrderData.channelUserId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付完成时间">
          {{ formatDateSafe(channelOrderData.successTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <div>
        <div class="mb-5px text-12px text-gray-500">渠道原始响应</div>
        <el-scrollbar class="channel-json-viewer">
          <pre>{{ channelOrderJson || '{}' }}</pre>
        </el-scrollbar>
      </div>
    </div>
    <div v-else class="py-20px text-center text-gray-400">暂无渠道数据</div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, toRaw } from 'vue'
import dayjs from 'dayjs'
import { MbOrderApi, type MbOrderPageReqVO, type MbOrderSummaryVO, type MbOrderDetailVO } from '@/api/mb/order'
import { queryChannelOrder, type ChannelOrderRespVO } from '@/api/pay/order'
import { useMessage } from '@/hooks/web/useMessage'
import { fenToYuan } from '@/utils'
import download from '@/utils/download'
import type { FormInstance } from 'element-plus'

const message = useMessage()

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '处理失败', value: 'FAILED' },
  { label: '退款中', value: 'REFUNDING' },
  { label: '已退款', value: 'REFUNDED' },
  { label: '退款失败', value: 'REFUND_FAILED' }
]
const statusTagType: Record<string, string> = {
  PENDING: 'warning',
  PROCESSING: 'primary',
  COMPLETED: 'success',
  FAILED: 'danger',
  REFUNDING: 'warning',
  REFUNDED: 'info',
  REFUND_FAILED: 'danger'
}
const bizTypeOptions = [
  { label: '物料补货', value: 'restock' },
  { label: '物料转化', value: 'materialConvert' },
  { label: '事业产品', value: 'careerProduct' },
  { label: '普通产品', value: 'normalProduct' }
]
const bizTypeLabel = bizTypeOptions.reduce<Record<string, string>>((map, item) => {
  map[item.value] = item.label
  return map
}, {})

const queryParams = reactive<MbOrderPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  orderNo: undefined,
  paymentId: undefined,
  agentUserName: undefined,
  productName: undefined,
  bizType: undefined,
  status: undefined,
  createTime: []
})
const queryFormRef = ref<FormInstance>()
const loading = ref(false)
const exportLoading = ref(false)
const list = ref<MbOrderSummaryVO[]>([])
const total = ref(0)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<MbOrderDetailVO | null>(null)

const updatingId = ref<number | null>(null)
const retryingId = ref<number | null>(null)
const refundingId = ref<number | null>(null)
const requireRefundPassword = ref(false)
const loadRefundConfig = async () => {
  try {
    requireRefundPassword.value = await MbOrderApi.getRefundConfig()
  } catch {
    requireRefundPassword.value = false
  }
}
const channelOrderDialogVisible = ref(false)
const channelOrderLoading = ref(false)
const channelOrderData = ref<ChannelOrderRespVO | null>(null)

const buildQueryParams = () => {
  const params = { ...toRaw(queryParams) }
  if (!params.createTime || params.createTime.length === 0) {
    delete params.createTime
  }
  if (!params.orderNo) delete params.orderNo
  if (!params.productId) delete params.productId
  if (!params.agentUserName) delete params.agentUserName
  if (!params.agentUserId) delete params.agentUserId
  if (!params.productName) delete params.productName
  if (!params.bizType) delete params.bizType
  if (!params.status) delete params.status
  if (!params.paymentId) delete params.paymentId
  return params
}

const getList = async () => {
  loading.value = true
  try {
    const params = buildQueryParams()
    const data = await MbOrderApi.getOrderPage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handlePagination = ({ page, limit }: { page: number; limit: number }) => {
  if (queryParams.pageNo !== page) {
    queryParams.pageNo = page
  }
  if (queryParams.pageSize !== limit) {
    queryParams.pageSize = limit
  }
  getList()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  queryParams.pageSize = 10
  getList()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
  } catch {
    return
  }
  exportLoading.value = true
  try {
    const data = await MbOrderApi.exportOrder(buildQueryParams())
    download.excel(data, '物料订单.xls')
  } finally {
    exportLoading.value = false
  }
}

const handleUpdateStatus = async (row: MbOrderSummaryVO, status: string) => {
  if (row.status === status) {
    return
  }
  updatingId.value = row.id
  try {
    await MbOrderApi.updateStatus({ id: row.id, status })
    message.success('订单状态更新成功')
    await getList()
  } finally {
    updatingId.value = null
  }
}

const handleRetryVirtualDelivery = async (orderId: number) => {
  retryingId.value = orderId
  try {
    await MbOrderApi.retryVirtualDelivery(orderId)
    message.success('已触发虚拟发货')
  } finally {
    retryingId.value = null
  }
}

const handleRefund = async (row: MbOrderSummaryVO) => {
  if (!row?.id) {
    return
  }
  let refundReason = ''
  try {
    const { value } = await message.prompt('请输入退款原因（可留空）', '发起退款')
    refundReason = value?.trim() || ''
  } catch {
    return
  }
  let refundPassword = ''
  if (requireRefundPassword.value) {
    try {
      const { value } = await message.prompt('请输入退款密码', '身份校验', {
        inputType: 'password'
      })
      refundPassword = value?.trim() || ''
      if (!refundPassword) {
        message.warning('退款密码不能为空')
        return
      }
    } catch {
      return
    }
  }
  refundingId.value = row.id
  try {
    await MbOrderApi.refundOrder({ id: row.id, reason: refundReason || undefined, password: refundPassword })
    message.success('退款申请已提交')
    await getList()
    if (detailVisible.value && detailData.value?.id === row.id) {
      await openDetail(row.id)
    }
  } finally {
    refundingId.value = null
  }
}

// 查询渠道侧实时订单
const handleQueryChannelOrder = async (payOrderId?: string | number | null) => {
  if (!payOrderId) {
    message.warning('缺少支付单号，无法查询渠道订单')
    return
  }
  const idStr = String(payOrderId).trim()
  if (!/^\d+$/.test(idStr)) {
    message.warning('支付单号格式异常，无法查询渠道订单')
    return
  }
  channelOrderDialogVisible.value = true
  channelOrderLoading.value = true
  try {
    channelOrderData.value = await queryChannelOrder(idStr)
  } finally {
    channelOrderLoading.value = false
  }
}

const openDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detailData.value = await MbOrderApi.getOrder(id)
  } finally {
    detailLoading.value = false
  }
}

const formatDateSafe = (value?: string | Date) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

const channelOrderJson = computed(() => {
  if (!channelOrderData.value) {
    return ''
  }
  try {
    const payload =
      channelOrderData.value.rawData && Object.keys(channelOrderData.value.rawData).length > 0
        ? channelOrderData.value.rawData
        : channelOrderData.value
    return JSON.stringify(payload, null, 2)
  } catch (error) {
    console.error('渠道订单 JSON 序列化失败', error)
    return JSON.stringify(channelOrderData.value)
  }
})

getList()
loadRefundConfig()
</script>

<style scoped>
.space-y-18px > * + * {
  margin-top: 18px;
}

.space-y-16px > * + * {
  margin-top: 16px;
}

.text-ellipsis {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.channel-json-viewer {
  max-height: 260px;
  padding: 12px;
  font-family: SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  color: #e6edf3;
  background-color: #0f111a;
  border-radius: 8px;
}

.channel-json-viewer pre {
  margin: 0;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
