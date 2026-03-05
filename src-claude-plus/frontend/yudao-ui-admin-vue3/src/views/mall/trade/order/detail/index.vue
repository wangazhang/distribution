<template>
  <ContentWrap>
    <!-- 订单信息 -->
    <el-descriptions title="订单信息">
      <el-descriptions-item label="订单号: ">{{ formData.no }}</el-descriptions-item>
      <el-descriptions-item label="买家: ">{{ formData?.user?.nickname }}</el-descriptions-item>
      <el-descriptions-item label="订单类型: ">
        <dict-tag :type="DICT_TYPE.TRADE_ORDER_TYPE" :value="formData.type!" />
      </el-descriptions-item>
      <el-descriptions-item label="订单来源: ">
        <dict-tag :type="DICT_TYPE.TERMINAL" :value="formData.terminal!" />
      </el-descriptions-item>
      <el-descriptions-item label="买家留言: ">{{ formData.userRemark }}</el-descriptions-item>
      <el-descriptions-item label="商家备注: ">{{ formData.remark }}</el-descriptions-item>
      <el-descriptions-item label="支付单号: ">{{ formData.payOrderId }}</el-descriptions-item>
      <el-descriptions-item label="付款方式: ">
        <dict-tag :type="DICT_TYPE.PAY_CHANNEL_CODE" :value="formData.payChannelCode!" />
      </el-descriptions-item>
      <el-descriptions-item v-if="formData.brokerageUser" label="推广用户: ">
        {{ formData.brokerageUser?.nickname }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 订单状态 -->
    <el-descriptions :column="1" title="订单状态">
      <el-descriptions-item label="订单状态: ">
        <dict-tag :type="DICT_TYPE.TRADE_ORDER_STATUS" :value="formData.status!" />
      </el-descriptions-item>
      <el-descriptions-item v-hasPermi="['trade:order:update']" label-class-name="no-colon">
        <el-button
          v-if="formData.status! === TradeOrderStatusEnum.UNPAID.status"
          type="primary"
          @click="updatePrice"
        >
          调整价格
        </el-button>
        <el-button type="primary" @click="remark">备注</el-button>
        <el-button
          v-if="canRefund"
          type="danger"
          plain
          :loading="refunding"
          @click="handleRefund"
        >
          退款
        </el-button>
        <!-- 待发货 -->
        <template v-if="formData.status! === TradeOrderStatusEnum.UNDELIVERED.status">
          <!-- 快递发货 -->
          <el-button
            v-if="formData.deliveryType === DeliveryTypeEnum.EXPRESS.type"
            type="primary"
            @click="delivery"
          >
            发货
          </el-button>
          <el-button
            v-if="formData.deliveryType === DeliveryTypeEnum.EXPRESS.type"
            type="primary"
            @click="updateAddress"
          >
            修改地址
          </el-button>
          <!-- 到店自提 -->
          <el-button
            v-if="formData.deliveryType === DeliveryTypeEnum.PICK_UP.type && showPickUp"
            type="primary"
            @click="handlePickUp"
          >
            核销
          </el-button>
        </template>
        <template
          v-if="
            [TradeOrderStatusEnum.DELIVERED.status, TradeOrderStatusEnum.COMPLETED.status].includes(
              formData.status!
            ) &&
            formData.deliveryType === DeliveryTypeEnum.EXPRESS.type &&
            formData.payChannelCode &&
            formData.payChannelCode.toLowerCase().includes('wx')
          "
        >
          <el-button type="primary" @click="pushChannelShipping">推送渠道发货</el-button>
        </template>
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label><span style="color: red">提醒: </span></template>
        买家付款成功后，货款将直接进入您的商户号（微信、支付宝）<br />
        请及时关注你发出的包裹状态，确保可以配送至买家手中 <br />
        如果买家表示没收到货或货物有问题，请及时联系买家处理，友好协商
      </el-descriptions-item>
    </el-descriptions>

    <!-- 商品信息 -->
    <el-descriptions title="商品信息">
      <el-descriptions-item labelClassName="no-colon">
        <el-row :gutter="20">
          <el-col :span="15">
            <el-table :data="formData.items" border>
              <el-table-column label="商品" prop="spuName" width="auto">
                <template #default="{ row }">
                  {{ row.spuName }}
                  <el-tag v-for="property in row.properties" :key="property.propertyId">
                    {{ property.propertyName }}: {{ property.valueName }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="商品原价" prop="price" width="150">
                <template #default="{ row }">{{ fenToYuan(row.price) }}元</template>
              </el-table-column>
              <el-table-column label="数量" prop="count" width="100" />
              <el-table-column label="合计" prop="payPrice" width="150">
                <template #default="{ row }">{{ fenToYuan(row.payPrice) }}元</template>
              </el-table-column>
              <el-table-column label="售后状态" prop="afterSaleStatus" width="120">
                <template #default="{ row }">
                  <dict-tag
                    :type="DICT_TYPE.TRADE_ORDER_ITEM_AFTER_SALE_STATUS"
                    :value="row.afterSaleStatus"
                  />
                </template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col :span="10" />
        </el-row>
      </el-descriptions-item>
    </el-descriptions>
    <el-descriptions :column="4">
      <!-- 第一层 -->
      <el-descriptions-item label="商品总额: ">
        {{ fenToYuan(formData.totalPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item label="运费金额: ">
        {{ fenToYuan(formData.deliveryPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item label="订单调价: ">
        {{ fenToYuan(formData.adjustPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item v-for="item in 1" :key="item" label-class-name="no-colon" />
      <!-- 第二层 -->
      <el-descriptions-item>
        <template #label><span style="color: red">优惠劵优惠: </span></template>
        {{ fenToYuan(formData.couponPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label><span style="color: red">VIP 优惠: </span></template>
        {{ fenToYuan(formData.vipPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label><span style="color: red">活动优惠: </span></template>
        {{ fenToYuan(formData.discountPrice!) }} 元
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label><span style="color: red">积分抵扣: </span></template>
        {{ fenToYuan(formData.pointPrice!) }} 元
      </el-descriptions-item>
      <!-- 第三层 -->
      <el-descriptions-item v-for="item in 3" :key="item" label-class-name="no-colon" />
      <el-descriptions-item label="应付金额: ">
        {{ fenToYuan(formData.payPrice!) }} 元
      </el-descriptions-item>
    </el-descriptions>

    <!-- 物流信息 -->
    <el-descriptions :column="4" title="收货信息">
      <el-descriptions-item label="配送方式: ">
        <dict-tag :type="DICT_TYPE.TRADE_DELIVERY_TYPE" :value="formData.deliveryType!" />
      </el-descriptions-item>
      <el-descriptions-item label="收货人: ">{{ formData.receiverName }}</el-descriptions-item>
      <el-descriptions-item label="联系电话: ">{{ formData.receiverMobile }}</el-descriptions-item>
      <!-- 快递配送 -->
      <div v-if="formData.deliveryType === DeliveryTypeEnum.EXPRESS.type">
        <el-descriptions-item v-if="formData.receiverDetailAddress" label="收货地址: ">
          {{ formData.receiverAreaName }} {{ formData.receiverDetailAddress }}
          <el-link
            v-clipboard:copy="formData.receiverAreaName + ' ' + formData.receiverDetailAddress"
            v-clipboard:success="clipboardSuccess"
            icon="ep:document-copy"
            type="primary"
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="formData.logisticsId" label="物流公司: ">
          {{ deliveryExpressList.find((item) => item.id === formData.logisticsId)?.name }}
        </el-descriptions-item>
        <el-descriptions-item v-if="formData.logisticsId" label="运单号: ">
          {{ formData.logisticsNo }}
        </el-descriptions-item>
        <el-descriptions-item v-if="formatDate.deliveryTime" label="发货时间: ">
          {{ formatDate(formData.deliveryTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-for="item in 2" :key="item" label-class-name="no-colon" />
        <el-descriptions-item v-if="expressTrackList.length > 0" label="物流详情: ">
          <el-timeline>
            <el-timeline-item
              v-for="(express, index) in expressTrackList"
              :key="index"
              :timestamp="formatDate(express.time)"
            >
              {{ express.content }}
            </el-timeline-item>
          </el-timeline>
        </el-descriptions-item>
      </div>
      <!-- 自提门店 -->
      <div v-if="formData.deliveryType === DeliveryTypeEnum.PICK_UP.type">
        <el-descriptions-item v-if="formData.pickUpStoreId" label="自提门店: ">
          {{ pickUpStore?.name }}
        </el-descriptions-item>
      </div>
    </el-descriptions>

    <!-- 订单日志 -->
    <el-descriptions title="订单操作日志">
      <el-descriptions-item labelClassName="no-colon">
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in formData.logs"
            :key="index"
            :timestamp="formatDate(log.createTime!)"
            placement="top"
          >
            <div class="el-timeline-right-content">
              {{ log.content }}
            </div>
            <template #dot>
              <span
                :style="{ backgroundColor: getUserTypeColor(log.userType!) }"
                class="dot-node-style"
              >
                {{ getDictLabel(DICT_TYPE.USER_TYPE, log.userType)[0] }}
              </span>
            </template>
          </el-timeline-item>
        </el-timeline>
      </el-descriptions-item>
    </el-descriptions>
  </ContentWrap>

  <!-- 各种操作的弹窗 -->
  <OrderDeliveryForm ref="deliveryFormRef" @success="getDetail" />
  <OrderUpdateRemarkForm ref="updateRemarkForm" @success="getDetail" />
  <OrderUpdateAddressForm ref="updateAddressFormRef" @success="getDetail" />
  <OrderUpdatePriceForm ref="updatePriceFormRef" @success="getDetail" />
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, unref } from 'vue'
import * as TradeOrderApi from '@/api/mall/trade/order'
import { fenToYuan } from '@/utils'
import { formatDate } from '@/utils/formatTime'
import { DICT_TYPE, getDictLabel, getDictObj } from '@/utils/dict'
import OrderUpdateRemarkForm from '@/views/mall/trade/order/form/OrderUpdateRemarkForm.vue'
import OrderDeliveryForm from '@/views/mall/trade/order/form/OrderDeliveryForm.vue'
import OrderUpdateAddressForm from '@/views/mall/trade/order/form/OrderUpdateAddressForm.vue'
import OrderUpdatePriceForm from '@/views/mall/trade/order/form/OrderUpdatePriceForm.vue'
import * as DeliveryExpressApi from '@/api/mall/trade/delivery/express'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { DeliveryTypeEnum, TradeOrderRefundStatusEnum, TradeOrderStatusEnum } from '@/utils/constants'
import * as DeliveryPickUpStoreApi from '@/api/mall/trade/delivery/pickUpStore'
import { propTypes } from '@/utils/propTypes'

defineOptions({ name: 'TradeOrderDetail' })

const message = useMessage() // 消息弹窗

/** 获得 userType 颜色 */
const getUserTypeColor = (type: number) => {
  const dict = getDictObj(DICT_TYPE.USER_TYPE, type)
  switch (dict?.colorType) {
    case 'success':
      return '#67C23A'
    case 'info':
      return '#909399'
    case 'warning':
      return '#E6A23C'
    case 'danger':
      return '#F56C6C'
  }
  return '#409EFF'
}

// 订单详情
const formData = ref<TradeOrderApi.OrderVO>({
  logs: []
})
const requireRefundPassword = ref(false)

const loadRefundConfig = async () => {
  try {
    requireRefundPassword.value = await TradeOrderApi.getRefundConfig()
  } catch {
    requireRefundPassword.value = false
  }
}

/** 各种操作 */
const updateRemarkForm = ref() // 订单备注表单 Ref
const remark = () => {
  updateRemarkForm.value?.open(formData.value)
}
const deliveryFormRef = ref() // 发货表单 Ref
const delivery = () => {
  deliveryFormRef.value?.open(formData.value)
}
const updateAddressFormRef = ref() // 收货地址表单 Ref
const updateAddress = () => {
  updateAddressFormRef.value?.open(formData.value)
}
const updatePriceFormRef = ref() // 订单调价表单 Ref
const updatePrice = () => {
  updatePriceFormRef.value?.open(formData.value)
}
const refunding = ref(false)
const canRefund = computed(
  () =>
    Boolean(formData.value.payStatus) &&
    formData.value.refundStatus !== TradeOrderRefundStatusEnum.ALL.status
)
const handleRefund = async () => {
  if (!formData.value.id) {
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
  refunding.value = true
  try {
    await TradeOrderApi.refundOrder({
      id: formData.value.id,
      reason: refundReason || undefined,
      password: refundPassword
    })
    message.success('退款申请已提交')
    await getDetail()
  } finally {
    refunding.value = false
  }
}
const pushChannelShipping = async () => {
  if (!formData.value.id) {
    return
  }
  try {
    await message.confirm('确认推送渠道发货？')
  } catch {
    return
  }
  try {
    await TradeOrderApi.pushChannelShipping(formData.value.id)
    message.success('渠道发货推送成功')
    await getDetail()
  } catch (error) {
    // 全局已弹错误，此处无需处理
  }
}

/** 核销 */
const handlePickUp = async () => {
  try {
    // 二次确认
    await message.confirm('确认核销订单吗？')
    // 提交
    await TradeOrderApi.pickUpOrder(formData.value.id!)
    message.success('核销成功')
    // 刷新列表
    await getDetail()
  } catch {}
}

/** 获得详情 */
const { params } = useRoute() // 查询参数
const props = defineProps({
  id: propTypes.number.def(undefined), // 订单ID
  showPickUp: propTypes.bool.def(true) // 显示核销按钮
})
const id = (params.id || props.id) as unknown as number
const getDetail = async () => {
  if (id) {
    const res = (await TradeOrderApi.getOrder(id)) as TradeOrderApi.OrderVO
    // 没有表单信息则关闭页面返回
    if (!res) {
      message.error('交易订单不存在')
      close()
    }
    formData.value = res
  }
}

/** 关闭 tag */
const { delView } = useTagsViewStore() // 视图操作
const { push, currentRoute } = useRouter() // 路由
const close = () => {
  delView(unref(currentRoute))
  push({ name: 'TradeOrder' })
}

/** 复制 */
const clipboardSuccess = () => {
  message.success('复制成功')
}

/** 初始化 **/
const deliveryExpressList = ref([]) // 物流公司
const expressTrackList = ref([]) // 物流详情
const pickUpStore = ref({}) // 自提门店
onMounted(async () => {
  await loadRefundConfig()
  await getDetail()
  // 如果配送方式为快递，则查询物流公司
  if (formData.value.deliveryType === DeliveryTypeEnum.EXPRESS.type) {
    deliveryExpressList.value = await DeliveryExpressApi.getSimpleDeliveryExpressList()
    if (formData.value.logisticsId) {
      expressTrackList.value = await TradeOrderApi.getExpressTrackList(formData.value.id!)
    }
  } else if (formData.value.deliveryType === DeliveryTypeEnum.PICK_UP.type) {
    if (formData.value.pickUpStoreId) {
      pickUpStore.value = await DeliveryPickUpStoreApi.getDeliveryPickUpStore(formData.value.pickUpStoreId)
    }
  }
})
</script>
<style lang="scss" scoped>
:deep(.el-descriptions) {
  &:not(:nth-child(1)) {
    margin-top: 20px;
  }

  .el-descriptions__title {
    display: flex;
    align-items: center;

    &::before {
      display: inline-block;
      width: 3px;
      height: 20px;
      margin-right: 10px;
      background-color: #409eff;
      content: '';
    }
  }

  .el-descriptions-item__container {
    margin: 0 10px;

    .no-colon {
      margin: 0;

      &::after {
        content: '';
      }
    }
  }
}

// 时间线样式调整
:deep(.el-timeline) {
  margin: 10px 0 0 160px;

  .el-timeline-item__wrapper {
    position: relative;
    top: -20px;

    .el-timeline-item__timestamp {
      position: absolute !important;
      top: 10px;
      left: -150px;
    }
  }

  .el-timeline-right-content {
    display: flex;
    min-height: 30px;
    padding: 10px;
    background-color: var(--app-content-bg-color);
    border-radius: var(--el-card-border-radius);
    align-items: center;

    &::before {
      position: absolute;
      top: 10px;
      left: 13px; /* 将伪元素水平居中 */
      border-color: transparent var(--app-content-bg-color) transparent transparent; /* 尖角颜色，左侧朝向 */
      border-style: solid;
      border-width: 8px; /* 调整尖角大小 */
      content: ''; /* 必须设置 content 属性 */
    }
  }

  .dot-node-style {
    position: absolute;
    left: -5px;
    display: flex;
    width: 20px;
    height: 20px;
    font-size: 10px;
    color: #fff;
    border-radius: 50%;
    justify-content: center;
    align-items: center;
  }
}
</style>
