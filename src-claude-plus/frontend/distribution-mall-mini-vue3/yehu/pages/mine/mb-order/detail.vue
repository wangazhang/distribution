<template>
  <s-layout title="订单详情" >
    <template v-if="!loading">
    <!-- 状态横幅 -->
    <view class="status-banner ss-m-20">
      <view class="status-banner__content ss-flex ss-row-between ss-col-center">
        <view class="status-banner__info ss-flex ss-row-start">
          <view class="status-icon-wrapper ss-flex ss-row-center ss-col-center">
            <text class="status-icon" :class="getStatusIcon(state.orderInfo.status)"></text>
          </view>
          <view class="status-text">
            <view class="status-title ss-font-32 ss-font-bold ss-m-b-4">
              {{ getStatusName(state.orderInfo.status) }}
            </view>
            <view class="status-desc ss-font-26">
              {{ formatOrderStatusDescription(state.orderInfo) }}
            </view>
            <view class="status-meta ss-font-24">
              <text class="meta-item">订单号：{{ state.orderInfo.orderNo || state.orderInfo.id }}</text>
              <text class="meta-item" v-if="orderTimeText">下单时间：{{ orderTimeText }}</text>
            </view>
          </view>
        </view>
        <view v-if="state.orderInfo.canReceive" class="status-actions">
          <button class="confirm-btn ss-reset-button" @tap="onConfirmReceive">
            确认收货
          </button>
        </view>
      </view>
    </view>
    <!-- 商品信息卡片 - 参考图片设计 -->
    <view class="product-card ss-m-20">
      <view class="product-content ss-p-24">
        <view class="product-main ss-flex ss-row-center">
          <!-- 商品图片 - 放大尺寸 -->
          <view class="product-image-wrapper ss-m-r-20">
            <image class="product-image"
                   :src="state.orderInfo.productImage || '/static/images/default-product.png'"
                   mode="aspectFill"></image>
          </view>

          <!-- 商品信息 -->
          <view class="product-info ss-flex-1">
            <view class="product-name ss-font-30 ss-font-bold ss-m-b-8">
              {{ state.orderInfo.productName || '商品名称' }}
            </view>
            <view class="product-spec ss-font-26 ss-color-subtitle ss-m-b-16">
              {{ state.orderInfo.bizTypeDisplay || getBizTypeName(state.orderInfo.bizType) }}
            </view>
            <view class="product-price ss-flex ss-row-between ss-col-center">
              <text class="price-text ss-font-32 ss-color-danger ss-font-bold">
                ¥{{ state.orderInfo.unitPriceDisplay || formatPrice(state.orderInfo.unitPrice) }} × {{ state.orderInfo.quantity }}
              </text>

              <!-- 暂时隐藏售后相关按钮 -->
              <view class="action-wrapper-inline">
                <!-- 暂时隐藏物料补货和转化的售后按钮 -->
                <!--
                <button
                  v-if="state.orderInfo.status === ORDER_STATUS.COMPLETED"
                  class="refund-btn-inline ss-reset-button"
                  @tap="onRefund"
                >
                  申请售后
                </button>
                <button
                  v-if="state.orderInfo.status === ORDER_STATUS.REFUNDING"
                  class="refund-btn-inline ss-reset-button"
                  @tap="onRefundDetail"
                >
                  退款详情
                </button>
                -->
              </view>
            </view>
          </view>
        </view>
      </view>
<!--    </view>-->
<!--    &lt;!&ndash; 金额信息卡片 - 简洁设计 &ndash;&gt;-->
<!--    <view class="amount-card ss-m-20">-->
      <view class="amount-content ss-p-24">
        <view class="amount-row ss-flex ss-row-between ss-col-center ss-m-b-16">
          <text class="amount-label ss-font-28">商品单价</text>
          <text class="amount-value ss-font-28">
            ¥{{ state.orderInfo.unitPriceDisplay || formatPrice(state.orderInfo.unitPrice) }}
          </text>
        </view>

        <view class="amount-row ss-flex ss-row-between ss-col-center ss-m-b-20">
          <text class="amount-label ss-font-28">购买数量</text>
          <text class="amount-value ss-font-28">{{ state.orderInfo.quantity }} 件</text>
        </view>

        <view class="amount-divider"></view>

        <view class="total-row ss-flex ss-row-between ss-col-center ss-m-t-20">
          <text class="total-label ss-font-32 ss-font-bold">订单总额</text>
          <text class="total-amount ss-font-36 ss-color-danger ss-font-bold">
            ¥{{ state.orderInfo.totalPriceDisplay || formatPrice(state.orderInfo.totalPrice) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 订单详情卡片 - 简洁设计 -->
    <view class="detail-card ss-m-20">
      <view class="detail-content ss-p-24">
        <view class="detail-row ss-flex ss-row-between ss-col-center ss-m-b-20">
          <text class="detail-label ss-font-28">创建时间</text>
          <text class="detail-value ss-font-28">
            {{ state.orderInfo.createTimeDisplay || formatTime(state.orderInfo.createTime) }}
          </text>
        </view>
        <view class="detail-row ss-flex ss-row-between ss-col-center ss-m-b-20">
          <text class="detail-label ss-font-28">订单号</text>
          <text class="detail-value ss-font-28" @tap="copyOrderNo">
            {{ state.orderInfo.orderNo || state.orderInfo.id }}
          </text>
        </view>
        <view class="detail-row ss-flex ss-row-between ss-col-center ss-m-b-20" v-if="state.orderInfo.paymentId">
          <text class="detail-label ss-font-28">支付单号</text>
          <text class="detail-value ss-font-28" @tap="copyPaymentId">
            {{ state.orderInfo.paymentId }}
          </text>
        </view>

<!--        <view class="detail-row ss-flex ss-row-between ss-col-center">-->
<!--          <text class="detail-label ss-font-28">订单状态</text>-->
<!--          <text class="detail-value ss-font-28 ss-font-bold"-->
<!--                :style="{ color: getStatusColor(state.orderInfo.status) }">-->
<!--            {{ getStatusName(state.orderInfo.status) }}-->
<!--          </text>-->
<!--        </view>-->
      </view>
    </view>


    </template>
  </s-layout>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getMbOrderDetail, receiveMbOrder, checkMbOrderChannelConfirm, ORDER_STATUS, ORDER_STATUS_NAME, ORDER_STATUS_COLORS, BIZ_TYPE_NAME } from '@/yehu/api/mine/mb-order';
import { isEmpty } from 'lodash-es';
import {
  fen2yuan,
  formatOrderColor,
  formatOrderStatus,
} from '@/sheep/hooks/useGoods';
import sheep from '@/sheep';

// 数据
const state = reactive({
  orderInfo: {},
});
const pendingChannelConfirm = ref(null);
const loading = ref(true);
const orderTimeText = computed(() => {
  if (!state.orderInfo) {
    return '';
  }
  if (state.orderInfo.createTimeDisplay) {
    return state.orderInfo.createTimeDisplay;
  }
  if (!state.orderInfo.createTime) {
    return '';
  }
  return formatTime(state.orderInfo.createTime);
});

// 获取订单详情
async function getOrderDetail(id, options = {}) {
  const skipSync = options.skipSync === true;
  if (!skipSync) {
    loading.value = true;
  }
  try {
    const { code, data } = await getMbOrderDetail(id);
    if (code !== 0) {
      if (!skipSync) {
        loading.value = false;
      }
      return;
    }
    state.orderInfo = data;
    if (!state.orderInfo.wechat_extra_data && state.orderInfo.wechatExtraData) {
      state.orderInfo.wechat_extra_data = state.orderInfo.wechatExtraData;
    }
    pendingChannelConfirm.value = null;
    if (data?.canReceive && !skipSync) {
      await trySyncChannelConfirm(data.id);
    }
  } catch (error) {
    console.error('getOrderDetail error', error);
    if (!skipSync) {
      loading.value = false;
    }
    return;
  }
  if (!skipSync) {
    loading.value = false;
  }
}

async function trySyncChannelConfirm(orderId) {
  try {
    const { code, data } = await checkMbOrderChannelConfirm(orderId);
    if (code === 0 && data === true) {
      await getOrderDetail(orderId, { skipSync: true });
    }
  } catch (err) {
    console.warn('渠道确认状态同步失败', err);
  }
}

async function onConfirmReceive() {
  if (!state.orderInfo?.id) return;
  const orderId = state.orderInfo.id;
  if (canInvokeChannelConfirm()) {
    const extra = state.orderInfo.wechat_extra_data || state.orderInfo.wechatExtraData || {};
    pendingChannelConfirm.value = {
      orderId,
      merchantTradeNo: extra.merchant_trade_no,
    };
    openConfirmBusinessView(extra);
    return;
  }
  const { confirm } = await uni.showModal({
    title: '提示',
    content: '确认已收到物料吗？',
    confirmText: '确认',
    cancelText: '再等等',
  });
  if (!confirm) {
    return;
  }
  uni.showLoading({ title: '确认中...' });
  try {
    const success = await submitConfirmReceive(orderId);
    if (!success) {
      sheep.$helper.toast('确认收货失败');
    }
  } catch (err) {
    console.error('确认收货失败', err);
    sheep.$helper.toast(err.message || '确认收货失败');
  } finally {
    uni.hideLoading();
  }
}

function canInvokeChannelConfirm() {
  if (sheep.$platform.name !== 'WechatMiniProgram') {
    return false;
  }
  const extra = state.orderInfo.wechat_extra_data || state.orderInfo.wechatExtraData || {};
  return !isEmpty(extra) && extra.merchant_id && extra.transaction_id;
}

async function submitConfirmReceive(orderId) {
  const { code } = await receiveMbOrder(orderId);
  if (code === 0) {
    sheep.$helper.toast('已确认收货');
    await getOrderDetail(orderId, { skipSync: true });
    return true;
  }
  return false;
}

async function handleChannelConfirmResult(extraData) {
  const pending = pendingChannelConfirm.value;
  if (!pending) {
    return;
  }
  if (
    extraData?.merchant_trade_no &&
    pending.merchantTradeNo &&
    extraData.merchant_trade_no !== pending.merchantTradeNo
  ) {
    return;
  }
  pendingChannelConfirm.value = null;
  const status = extraData?.status;
  if (status === 'success') {
    const synced = await syncChannelConfirmStatus(pending.orderId);
    if (synced) {
      await getOrderDetail(pending.orderId, { skipSync: true });
      return;
    }
    await submitConfirmReceive(pending.orderId);
    return;
  }
  if (status === 'cancel') {
    const synced = await syncChannelConfirmStatus(pending.orderId);
    if (synced) {
      await getOrderDetail(pending.orderId, { skipSync: true });
      return;
    }
    sheep.$helper.toast('已取消确认收货');
    return;
  }
  if (status) {
    const errMsg = extraData?.err_msg || extraData?.fail_msg || '确认收货失败，请稍后重试';
    sheep.$helper.toast(errMsg);
  }
}

async function syncChannelConfirmStatus(orderId) {
  try {
    const { code, data } = await checkMbOrderChannelConfirm(orderId);
    return code === 0 && data === true;
  } catch (error) {
    console.error('syncChannelConfirmStatus error', error);
    return false;
  }
}

// #ifdef MP-WEIXIN
function openConfirmBusinessView(extra) {
  if (!wx.openBusinessView) {
    sheep.$helper.toast('请升级微信版本');
    pendingChannelConfirm.value = null;
    return;
  }
  const payload = {
    merchant_id: extra.merchant_id,
    merchant_trade_no: extra.merchant_trade_no,
    transaction_id: extra.transaction_id,
  };
  if (!payload.merchant_id || !payload.transaction_id) {
    pendingChannelConfirm.value = null;
    sheep.$helper.toast('缺少渠道确认参数');
    return;
  }
  if (extra.sub_merchant_id) {
    payload.sub_merchant_id = extra.sub_merchant_id;
  }
  wx.openBusinessView({
    businessType: 'weappOrderConfirm',
    extraData: payload,
    success(response) {
      if (response.errMsg === 'openBusinessView:ok') {
        handleChannelConfirmResult(response.extraData || {});
      }
    },
    fail() {
      pendingChannelConfirm.value = null;
      sheep.$helper.toast('暂时无法唤起确认组件，请稍后再试');
    },
  });
}
// #endif

// 格式化订单状态描述
function formatOrderStatusDescription(order) {
  switch (order.status) {
    case ORDER_STATUS.PENDING:
      return '您的订单正在处理中，请耐心等待';
    case ORDER_STATUS.PROCESSING:
      return '订单处理中，我们会尽快为您完成';
    case ORDER_STATUS.DELIVERED:
      return '订单已发货，请确认收货';
    case ORDER_STATUS.COMPLETED:
      return '订单已完成，感谢您的使用';
    case ORDER_STATUS.FAILED:
      return '订单处理失败，请联系客服';
    case ORDER_STATUS.REFUNDING:
      return '退款申请已提交，正在处理中';
    case ORDER_STATUS.REFUNDED:
      return '退款已完成，请查收';
    case ORDER_STATUS.REFUND_FAILED:
      return '退款失败，请联系客服';
    default:
      return '订单状态未知';
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return '';
  const date = new Date(time);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

// 格式化价格
function formatPrice(price) {
  if (!price) return '0.00';
  return fen2yuan(price);
}

// 获取业务类型名称
function getBizTypeName(bizType) {
  return BIZ_TYPE_NAME[bizType] || '未知类型';
}

// 获取业务类型描述
function getBizTypeDescription(bizType) {
  const descriptions = {
    'MATERIAL_REPLENISHMENT': '为您的美容院补充所需物料',
    'COLLAGEN_CONVERSION': '胶原蛋白转化服务',
    'EQUIPMENT_MAINTENANCE': '设备维护保养服务',
    'TRAINING_SERVICE': '专业技能培训服务',
    'CONSULTATION': '专业咨询服务'
  };
  return descriptions[bizType] || '专业美业服务';
}

// 获取状态名称
function getStatusName(status) {
  return ORDER_STATUS_NAME[status] || '未知状态';
}

// 获取状态颜色
function getStatusColor(status) {
  return ORDER_STATUS_COLORS[status] || '#999999';
}

// 获取状态图标
function getStatusIcon(status) {
  const iconMap = {
    [ORDER_STATUS.PENDING]: 'cicon-time ss-font-24',
    [ORDER_STATUS.PROCESSING]: 'cicon-loading ss-font-24',
    [ORDER_STATUS.DELIVERED]: 'cicon-time ss-font-24',
    [ORDER_STATUS.COMPLETED]: 'cicon-check ss-font-24',
    [ORDER_STATUS.FAILED]: 'cicon-close ss-font-24',
    [ORDER_STATUS.REFUNDING]: 'cicon-undo ss-font-24',
    [ORDER_STATUS.REFUNDED]: 'cicon-check ss-font-24',
    [ORDER_STATUS.REFUND_FAILED]: 'cicon-close ss-font-24',
  };
  return iconMap[status] || 'cicon-help ss-font-24';
}

// 复制订单号
function copyOrderNo() {
  uni.setClipboardData({
    data: state.orderInfo.orderNo || state.orderInfo.id,
    success: () => {
      uni.showToast({
        title: '订单号已复制',
        icon: 'success'
      });
    }
  });
}

// 复制支付单号
function copyPaymentId() {
  uni.setClipboardData({
    data: state.orderInfo.paymentId,
    success: () => {
      uni.showToast({
        title: '支付单号已复制',
        icon: 'success'
      });
    }
  });
}

// 申请退款
function onRefund() {
  uni.showModal({
    title: '申请退款',
    content: '确定要申请退款吗？',
    success: (res) => {
      if (res.confirm) {
        sheep.$router.go('/yehu/pages/mine/mb-order/refund-detail', {
          id: state.orderInfo.id,
          action: 'apply'
        });
      }
    }
  });
}

// 退款详情
function onRefundDetail() {
  sheep.$router.go('/yehu/pages/mine/mb-order/refund-detail', {
    id: state.orderInfo.id
  });
}



onMounted(() => {
  uni.$on('wechat-order-confirm', handleChannelConfirmResult);
});

onBeforeUnmount(() => {
  uni.$off('wechat-order-confirm', handleChannelConfirmResult);
});

// 页面加载
onLoad(async (options) => {
  if (options.id) {
    await getOrderDetail(options.id);
  } else {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
// 引入系统变量
@import '@/sheep/scss/_var.scss';

.status-banner {
  background: linear-gradient(135deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  color: #fff;
  box-shadow: 0 12rpx 40rpx rgba(17, 27, 54, 0.15);

  &__content {
    width: 100%;
  }

  &__info {
    flex: 1;
  }

  .status-icon-wrapper {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.2);
    margin-right: 20rpx;

    .status-icon {
      color: #fff;
      font-size: 32rpx;
    }
  }

  .status-text {
    .status-title {
      line-height: 1.2;
    }

    .status-desc {
      line-height: 1.4;
      opacity: 0.9;
    }

    .status-meta {
      margin-top: 12rpx;
      display: flex;
      flex-direction: column;
      gap: 6rpx;
      opacity: 0.8;

      .meta-item {
        display: block;
      }
    }
  }

  .status-actions {
    margin-left: 24rpx;

    .confirm-btn {
      padding: 12rpx 28rpx;
      border-radius: 36rpx;
      background: #fff;
      color: var(--ui-BG-Main, #8b0000);
      font-size: 26rpx;
      box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
    }
  }
}

/* 商品信息卡片 - 简洁设计 */
.product-card {
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

  .product-content {
    .product-main {
      .product-image-wrapper {
        .product-image {
          width: 140rpx;
          height: 140rpx;
          border-radius: 12rpx;
          background: #f8f8f8;
        }
      }

      .product-info {
        .product-name {
          color: #1a1a1a;
          line-height: 1.3;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }

        .product-spec {
          line-height: 1.2;
        }

        .product-quantity {
          .quantity-text {
            opacity: 0.8;
          }

          .unit-price {
            font-family: 'DIN', 'Helvetica Neue', sans-serif;
          }
        }
      }
    }
  }
}

/* 金额信息卡片 - 简洁设计 */
.amount-card {
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

  .amount-content {
    .amount-row {
      .amount-label {
        color: #666;
      }

      .amount-value {
        color: #1a1a1a;
        font-family: 'DIN', 'Helvetica Neue', sans-serif;
      }
    }

    .amount-divider {
      height: 1rpx;
      background: #f0f0f0;
      margin: 20rpx 0;
    }

    .total-row {
      .total-label {
        color: #1a1a1a;
      }

      .total-amount {
        font-family: 'DIN', 'Helvetica Neue', sans-serif;
        letter-spacing: 1rpx;
      }
    }
  }
}

/* 订单详情卡片 - 简洁设计 */
.detail-card {
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

  .detail-content {
    .detail-row {
      .detail-label {
        color: #666;
      }

      .detail-value {
        color: #1a1a1a;
        word-break: break-all;
        line-height: 1.3;
      }
    }
  }
}

/* 申请退款按钮区域 - 与金额水平对齐 */
.action-wrapper-inline {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}

.refund-btn-inline {
  width: 120rpx;
  height: 56rpx;
  background: #f6f6f6;
  font-size: 24rpx;
  border-radius: 28rpx;
  border: 1rpx solid #e0e0e0;
  flex-shrink: 0;

  &:active {
    transform: scale(0.95);
    background: #e8e8e8;
  }
}

/* 操作按钮区域 - 参考订单列表页面样式 */
.order-card-footer {
  border-top: 1rpx solid #f0f0f0;
  margin-top: 20rpx;
  padding-top: 20rpx;
}

.tool-btn {
  width: 160rpx;
  height: 60rpx;
  background: #f6f6f6;
  font-size: 26rpx;
  border-radius: 30rpx;
  margin-right: 10rpx;

  &:last-of-type {
    margin-right: 0;
  }

  &:active {
    transform: scale(0.95);
    background: #e8e8e8;
  }
}









</style>
