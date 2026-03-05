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
      <el-form-item label="业务类型" prop="bizType">
        <el-select
          v-model="queryParams.bizType"
          placeholder="请选择业务类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="业务编号" prop="bizId">
        <el-input
          v-model="queryParams.bizId"
          placeholder="请输入业务编号"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.BROKERAGE_RECORD_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
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
        <el-checkbox v-model="queryParams.includePlatformAccount" @change="handleIncludeSpecialToggle">
          显示平台账户
        </el-checkbox>
        <el-checkbox
          v-model="queryParams.includeHqAccount"
          class="ml-20px"
          @change="handleIncludeSpecialToggle"
        >
          显示总部账户
        </el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="编号" align="center" prop="id" min-width="60" />
      <el-table-column label="用户编号" align="center" prop="userId" min-width="80" />
      <el-table-column label="头像" align="center" prop="userAvatar" width="70px">
        <template #default="scope">
          <el-avatar :src="scope.row.userAvatar" />
        </template>
      </el-table-column>
      <el-table-column label="昵称" align="center" prop="userNickname" min-width="80px" />
      <el-table-column label="业务类型" align="center" prop="bizType" min-width="85">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE" :value="scope.row.bizType" />
        </template>
      </el-table-column>
      <el-table-column label="业务编号" align="center" prop="bizId" min-width="120">
        <template #default="scope">
          <el-space>
            <span>{{ scope.row.bizId || '--' }}</span>
            <el-link
              v-if="scope.row.bizId"
              type="primary"
              :underline="false"
              @click="handleCopyBizId(scope.row.bizId)"
            >
              复制
            </el-link>
          </el-space>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" min-width="110" />
      <el-table-column
        label="金额"
        align="center"
        prop="price"
        min-width="60"
        :formatter="fenToYuanFormat"
      />
      <el-table-column label="说明" align="center" prop="description" min-width="120" />
      <el-table-column label="状态" align="center" prop="status" min-width="85">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.BROKERAGE_RECORD_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        label="解冻时间"
        align="center"
        prop="unfreezeTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openBizDetail(row.id)">
            业务详情
          </el-button>
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

  <el-drawer
    v-model="bizDetailVisible"
    title="业务详情"
    size="720px"
    destroy-on-close
    @close="handleBizDetailClose"
  >
    <el-skeleton v-if="bizDetailLoading" :rows="6" animated />
    <div v-else-if="bizDetailData" class="detail-sections">
      <section class="detail-section">
        <h3 class="section-title">记录信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录 ID">
            {{ bizDetailData.recordId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户 ID">
            {{ bizDetailData.userId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">
            <dict-tag
              :type="DICT_TYPE.BROKERAGE_RECORD_BIZ_TYPE"
              :value="bizDetailData.bizType"
            />
          </el-descriptions-item>
          <el-descriptions-item label="业务大类">
            {{ bizCategoryMap[bizDetailData.bizCategory || 0] }}
          </el-descriptions-item>
          <el-descriptions-item label="业务编号">
            {{ bizDetailData.bizId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            {{ fenToYuan(bizDetailData.price || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="当前总佣金">
            {{ fenToYuan(bizDetailData.totalPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ bizDetailData.createTime || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            {{ bizDetailData.title || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="说明" :span="2">
            {{ bizDetailData.description || '--' }}
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section v-if="bizDetailData.mallOrder" class="detail-section">
        <div class="section-header">
          <h3 class="section-title">商城订单</h3>
          <el-button type="primary" link @click="handleGotoMallOrder">
            前往订单
          </el-button>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单 ID">
            {{ bizDetailData.mallOrder?.orderId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单号">
            {{ bizDetailData.mallOrder?.orderNo || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            {{ bizDetailData.mallOrder?.orderStatusName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="售后状态">
            {{ bizDetailData.mallOrder?.refundStatusName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">
            {{ fenToYuan(bizDetailData.mallOrder?.orderPayPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="支付渠道">
            {{ bizDetailData.mallOrder?.payChannelCode || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付完成">
            {{ bizDetailData.mallOrder?.payFinished ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ bizDetailData.mallOrder?.payTime || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="配送方式">
            {{ bizDetailData.mallOrder?.deliveryTypeName || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border class="mt-16px">
          <el-descriptions-item label="收件人">
            {{ bizDetailData.mallOrder?.receiver?.name || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ bizDetailData.mallOrder?.receiver?.mobile || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="地区" :span="2">
            {{ bizDetailData.mallOrder?.receiver?.areaName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ bizDetailData.mallOrder?.receiver?.detailAddress || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border class="mt-16px">
          <el-descriptions-item label="商品">
            {{ bizDetailData.mallOrder?.item?.spuName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="规格">
            {{ bizDetailData.mallOrder?.item?.propertiesText || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="数量">
            {{ bizDetailData.mallOrder?.item?.count || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="价格">
            {{ fenToYuan(bizDetailData.mallOrder?.item?.unitPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="实付">
            {{ fenToYuan(bizDetailData.mallOrder?.item?.payPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="售后状态">
            {{ bizDetailData.mallOrder?.item?.afterSaleStatusName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品图" :span="2">
            <el-image
              v-if="bizDetailData.mallOrder?.item?.picUrl"
              :src="bizDetailData.mallOrder?.item?.picUrl"
              fit="cover"
              class="h-80px w-80px rounded border"
            />
            <span v-else>--</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>

      <section v-if="bizDetailData.mbOrder" class="detail-section">
        <div class="section-header">
          <h3 class="section-title">物料订单</h3>
          <el-button type="primary" link @click="handleGotoMbOrder">
            前往订单
          </el-button>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单 ID">
            {{ bizDetailData.mbOrder?.orderId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单号">
            {{ bizDetailData.mbOrder?.orderNo || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ bizDetailData.mbOrder?.statusName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="业务类型">
            {{ bizDetailData.mbOrder?.bizTypeName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="数量">
            {{ bizDetailData.mbOrder?.quantity || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="总价">
            {{ fenToYuan(bizDetailData.mbOrder?.totalPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">
            {{ fenToYuan(bizDetailData.mbOrder?.payPrice || 0) }} 元
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            {{ bizDetailData.mbOrder?.payStatusName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="代理用户">
            {{ bizDetailData.mbOrder?.agentUserNickname || '--' }}（ID：
            {{ bizDetailData.mbOrder?.agentUserId || '--' }}）
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ bizDetailData.mbOrder?.agentUserMobile || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ bizDetailData.mbOrder?.paySuccessTime || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ bizDetailData.mbOrder?.createTime || '--' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border class="mt-16px">
          <el-descriptions-item label="商品">
            {{ bizDetailData.mbOrder?.productName || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品 ID">
            {{ bizDetailData.mbOrder?.productId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="商品图片" :span="2">
            <el-image
              v-if="bizDetailData.mbOrder?.productImage"
              :src="bizDetailData.mbOrder?.productImage"
              fit="cover"
              class="h-80px w-80px rounded border"
            />
            <span v-else>--</span>
          </el-descriptions-item>
        </el-descriptions>
      </section>
    </div>
    <div v-else class="py-30px text-center text-gray-400">暂无业务详情</div>
  </el-drawer>
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as BrokerageRecordApi from '@/api/mall/trade/brokerage/record'
import type { BrokerageRecordBizDetailRespVO } from '@/api/mall/trade/brokerage/record'
import * as MemberUserApi from '@/api/member/user'
import { fenToYuanFormat } from '@/utils/formatter'
import { fenToYuan } from '@/utils'
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from '@/hooks/web/useMessage'

defineOptions({ name: 'TradeBrokerageRecord' })

const router = useRouter()
const message = useMessage()
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const bizCategoryMap: Record<number, string> = {
  0: '其他',
  1: '商城订单',
  2: '物料订单'
}
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: null,
  bizType: null,
  price: null,
  bizId: '',
  status: null,
  createTime: [],
  includePlatformAccount: false,
  includeHqAccount: false
})
const queryFormRef = ref() // 搜索的表单
const bizDetailVisible = ref(false)
const bizDetailLoading = ref(false)
const bizDetailData = ref<BrokerageRecordBizDetailRespVO | null>(null)

// 用户搜索相关
const userOptions = ref([]) // 用户选项列表
const userLoading = ref(false) // 用户搜索加载状态
const searchTimer = ref(null) // 防抖定时器

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await BrokerageRecordApi.getBrokerageRecordPage(queryParams)
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

/** 平台账户开关 */
const handleIncludeSpecialToggle = () => {
  queryParams.pageNo = 1
  getList()
}

const handleCopyBizId = async (bizId: string | number) => {
  if (!bizId) {
    message.warning('暂无业务编号可复制')
    return
  }
  const text = String(bizId)
  try {
    await navigator.clipboard.writeText(text)
    message.success('业务编号已复制')
  } catch (err) {
    try {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.focus()
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      message.success('业务编号已复制')
    } catch (error) {
      console.error('复制业务编号失败', error)
      message.error('复制失败，请手动复制')
    }
  }
}

const openBizDetail = async (recordId: number) => {
  if (!recordId) {
    message.warning('记录编号缺失')
    return
  }
  bizDetailVisible.value = true
  bizDetailLoading.value = true
  try {
    bizDetailData.value = await BrokerageRecordApi.getBrokerageRecordBizDetail(recordId)
  } catch (error) {
    console.error('获取业务详情失败:', error)
    message.error('获取业务详情失败')
    bizDetailVisible.value = false
  } finally {
    bizDetailLoading.value = false
  }
}

const handleBizDetailClose = () => {
  bizDetailData.value = null
}

const handleGotoMallOrder = () => {
  const orderId = bizDetailData.value?.mallOrder?.orderId
  if (!orderId) {
    message.warning('当前记录未关联商城订单')
    return
  }
  router.push({ name: 'TradeOrderDetail', params: { id: orderId } })
}

const handleGotoMbOrder = async () => {
  const orderId = bizDetailData.value?.mbOrder?.orderId
  if (!orderId) {
    message.warning('当前记录未关联物料订单')
    return
  }
  try {
    await router.push({ path: '/material/order', query: { highlightOrderId: orderId } })
  } catch (error) {
    console.warn('跳转物料订单失败:', error)
    window.open(`/material/order?highlightOrderId=${orderId}`, '_blank')
  }
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
</script>

<style scoped>
.space-y-18px > * + * {
  margin-top: 18px;
}

.text-ellipsis {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-section {
  padding-bottom: 6px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.mt-16px {
  margin-top: 16px;
}

.detail-section .el-image {
  border-radius: 6px;
}
</style>
