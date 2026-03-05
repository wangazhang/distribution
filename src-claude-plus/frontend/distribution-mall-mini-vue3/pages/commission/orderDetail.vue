<template>
  <s-layout title="分佣订单详情"  >
    <view v-if="loading" class="loading-box">
      <text>加载中...</text>
    </view>
    <view v-else-if="detail" class="detail-wrapper">
      <view class="summary-card">
        <view class="status-row ss-flex ss-row-between ss-col-center">
          <view class="status-text">{{ statusName }}</view>
          <view class="biz-category">{{ bizCategoryName }}</view>
        </view>
        <view class="price-block">
          <text class="price">{{ fen2yuan(detail.price || 0) }}</text>
          <text class="price-suffix">元</text>
        </view>
        <view class="meta-row">
          <text>业务编号：{{ detail.bizId || '--' }}</text>
        </view>
        <view class="meta-row">
          <text>业务类型：{{ bizTypeName }}</text>
        </view>
        <view class="meta-row">
          <text>创建时间：{{ formatTime(detail.createTime) }}</text>
        </view>
        <view class="meta-row" v-if="detail.description">
          <text>说明：{{ detail.description }}</text>
        </view>
      </view>

      <view v-if="detail.mallOrder" class="card">
        <view class="card-title">商城订单</view>
        <view class="cell">
          <text class="label">订单编号</text>
          <text class="value">{{ detail.mallOrder.orderNo || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">订单状态</text>
          <text class="value">{{ detail.mallOrder.orderStatusName || '--' }}</text>
        </view>
<!--        <view class="cell">-->
<!--          <text class="label">支付渠道</text>-->
<!--          <text class="value">{{ detail.mallOrder.payChannelCode || '&#45;&#45;' }}</text>-->
<!--        </view>-->
        <view class="cell">
          <text class="label">支付金额</text>
          <text class="value">{{ fen2yuan(detail.mallOrder.orderPayPrice || 0) }} 元</text>
        </view>
        <view class="cell">
          <text class="label">支付时间</text>
          <text class="value">{{ formatTime(detail.mallOrder.payTime) }}</text>
        </view>
<!--        <view class="cell">-->
<!--          <text class="label">收件人</text>-->
<!--          <text class="value">{{ detail.mallOrder.receiver?.name || '&#45;&#45;' }}</text>-->
<!--        </view>-->
<!--        <view class="desc" v-if="detail.mallOrder.receiver?.detailAddress">-->
<!--          <text class="label">收货地址</text>-->
<!--          <text class="value">-->
<!--            {{ detail.mallOrder.receiver?.areaName || '' }} {{ detail.mallOrder.receiver?.detailAddress || '' }}-->
<!--          </text>-->
<!--        </view>-->
        <view class="divider"></view>
        <view class="cell">
          <text class="label">商品</text>
          <text class="value">{{ detail.mallOrder.item?.spuName || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">规格</text>
          <text class="value">{{ detail.mallOrder.item?.propertiesText || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">数量</text>
          <text class="value">{{ detail.mallOrder.item?.count || 0 }}</text>
        </view>
        <view class="cell">
          <text class="label">实付单价</text>
          <text class="value">{{ fen2yuan(detail.mallOrder.item?.payPrice || 0) }} 元</text>
        </view>
        <view class="goods-img" v-if="detail.mallOrder.item?.picUrl">
          <image :src="detail.mallOrder.item.picUrl" mode="aspectFill" />
        </view>
      </view>

      <view v-if="detail.mbOrder" class="card">
        <view class="card-title">物料订单</view>
        <view class="cell">
          <text class="label">订单编号</text>
          <text class="value">{{ detail.mbOrder.orderNo || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">订单状态</text>
          <text class="value">{{ detail.mbOrder.statusName || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">业务类型</text>
          <text class="value">{{ detail.mbOrder.bizTypeName || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">商品</text>
          <text class="value">{{ detail.mbOrder.productName || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">数量</text>
          <text class="value">{{ detail.mbOrder.quantity || 0 }}</text>
        </view>
        <view class="cell">
          <text class="label">总价</text>
          <text class="value">{{ fen2yuan(detail.mbOrder.totalPrice || 0) }} 元</text>
        </view>
        <view class="cell">
          <text class="label">支付状态</text>
          <text class="value">{{ detail.mbOrder.payStatusName || '--' }}</text>
        </view>
        <view class="cell">
          <text class="label">支付时间</text>
          <text class="value">{{ formatTime(detail.mbOrder.paySuccessTime) }}</text>
        </view>
      </view>
    </view>
    <s-empty v-else icon="/static/order-empty.png" text="暂无详情" />
  </s-layout>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  const detail = ref(null);
  const loading = ref(true);
  const recordId = ref(null);

  const bizCategoryName = computed(() => {
    const map = {
      1: '商城订单',
      2: '物料订单',
    };
    return map[detail.value?.bizCategory] || '其他';
  });

  const statusName = computed(() => {
    const status = Number(detail.value?.status);
    if (status === 1) return '已结算';
    if (status === 0) return '待结算';
    if (status === 2) return '已取消';
    return '未知';
  });

const bizTypeName = computed(() => detail.value?.title || '--');

const mallItemImage = computed(() => detail.value?.mallOrder?.item?.picUrl || '');

  function formatTime(val) {
    if (!val) return '--';
    return sheep.$helper.timeFormat(val, 'yyyy-mm-dd hh:MM:ss');
  }

  async function fetchDetail() {
    if (!recordId.value) {
      sheep.$helper.toast('记录编号缺失');
      loading.value = false;
      return;
    }
    loading.value = true;
    try {
      const { code, data } = await BrokerageApi.getBrokerageRecordBizDetail(recordId.value);
      if (code === 0) {
        detail.value = data;
      } else {
        sheep.$helper.toast('获取详情失败');
      }
    } catch (error) {
      console.error('获取分佣订单详情失败', error);
      sheep.$helper.toast('获取详情失败');
    } finally {
      loading.value = false;
    }
  }

  onLoad((options) => {
    recordId.value = options?.id;
    fetchDetail();
  });
</script>

<style lang="scss" scoped>
.detail-wrapper {
  padding: 20rpx;
  box-sizing: border-box;
}

.loading-box {
  padding: 80rpx 0;
  text-align: center;
  color: #666;
}

.summary-card {
  background: linear-gradient(135deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 26rpx;
  color: #fff;
  box-shadow: 0 8rpx 22rpx rgba(0, 0, 0, 0.08);

  .status-row {
    margin-bottom: 16rpx;

    .status-text {
      font-size: 32rpx;
      font-weight: 600;
    }

    .biz-category {
      font-size: 24rpx;
      padding: 6rpx 16rpx;
      border-radius: 999rpx;
      background: rgba(255, 255, 255, 0.2);
    }
  }

  .price-block {
    display: flex;
    align-items: flex-end;
    gap: 8rpx;
    margin-bottom: 16rpx;

    .price {
      font-size: 60rpx;
      font-weight: 700;
    }

    .price-suffix {
      font-size: 26rpx;
    }
  }

  .meta-row {
    font-size: 24rpx;
    line-height: 1.8;
    opacity: 0.9;
  }
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-sizing: border-box;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.04);

  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 20rpx;
  }

  .cell {
    display: flex;
    align-items: flex-start;
    font-size: 26rpx;
    color: #333333;
    margin-bottom: 16rpx;

    .label {
      color: #999999;
      width: 140rpx;
      flex-shrink: 0;
      line-height: 1.5;
    }

    .value {
      flex: 1;
      text-align: right;
      line-height: 1.6;
      word-break: break-all;
    }

    .price {
      color: var(--ui-BG-Main);
      font-weight: 600;
    }
  }

  .desc {
    margin-top: 10rpx;

    .label {
      display: block;
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 6rpx;
    }

    .value {
      font-size: 26rpx;
      color: #333333;
      line-height: 1.6;
    }
  }

  .divider {
    height: 1rpx;
    background: #f2f2f2;
    margin: 20rpx 0;
  }

  .goods-img {
    margin-top: 10rpx;

    image {
      width: 100%;
      height: 320rpx;
      border-radius: 12rpx;
    }
  }
}
</style>
