<template>
  <s-layout title="事业订单">
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="statusTabs"
        :scrollable="false"
        :current="state.currentTab"
        @change="onTabsChange"
      />
    </su-sticky>

    <swiper
      class="order-swiper"
      :current="state.currentTab"
      @change="onSwiperChange"
      :duration="300"
    >
      <swiper-item
        v-for="(tab, tabIndex) in statusTabs"
        :key="tab.value || tabIndex"
        class="swiper-item"
      >
        <scroll-view
          class="swiper-content"
          scroll-y
          scroll-with-animation
          lower-threshold="60"
          @scrolltolower="() => loadMore(tabIndex)"
          refresher-enabled
          :refresher-triggered="isTabRefreshing(tabIndex)"
          refresher-background="#f5f5f5"
          @refresherrefresh="() => onRefresherRefresh(tabIndex)"
        >
          <s-empty
            v-if="
              getTabPagination(tabIndex).total === 0 &&
              getTabLoadStatus(tabIndex) !== 'loading'
            "
            icon="/static/order-empty.png"
            text="暂无订单"
          />

          <template v-else>
            <view
              class="order-card ss-r-12 ss-m-x-20 ss-m-t-20"
              v-for="order in getTabPagination(tabIndex).list"
              :key="order.id"
              @tap="onOrderDetail(order.id)"
            >
              <view class="order-card__header ss-flex ss-row-between ss-col-center">
                <view class="order-card__meta">
                  <view class="order-no">订单号：{{ order.orderNo || order.id }}</view>
                  <view class="order-time">时间：{{ formatOrderTime(order) }}</view>
                </view>
                <view class="order-status" :class="formatMbOrderColor(order)">
                  {{ formatMbOrderStatus(order) }}
                </view>
              </view>

              <view class="order-card__body">
                <s-goods-item
                  :img="order.productImage"
                  :title="order.productName || '商品名称'"
                  :skuText="order.bizTypeDisplay || getBizTypeName(order.bizType)"
                  :price="order.unitPrice"
                  :num="order.quantity"
                  :priceColor="'var(--ui-BG-Main, #d10019)'"
                />
              </view>

              <view class="order-card__footer ss-flex ss-row-between ss-col-center">
                <view class="order-amount">
                  共 {{ order.quantity }} 件商品，实付
                  <text class="amount-number">
                    ¥{{ order.totalPriceDisplay || formatPrice(order.totalPrice) }}
                  </text>
                </view>
                <view class="order-actions">
                  <button
                    class="tool-btn ss-reset-button"
                    @tap.stop="onOrderDetail(order.id)"
                  >
                    查看详情
                  </button>
                  <button
                    v-if="order.canReceive"
                    class="tool-btn primary ss-reset-button"
                    @tap.stop="onConfirm(order.id)"
                  >
                    确认收货
                  </button>
                </view>
              </view>
            </view>

            <uni-load-more
              v-if="getTabPagination(tabIndex).total > 0"
              :status="getTabLoadStatus(tabIndex)"
              :content-text="loadingText"
              @tap="() => loadMore(tabIndex)"
            />
          </template>
        </scroll-view>
      </swiper-item>
    </swiper>
  </s-layout>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getMbOrderPage,
  ORDER_STATUS,
  ORDER_STATUS_NAME,
  BIZ_TYPE_NAME,
  formatMbOrderStatus,
  formatMbOrderColor,
} from '@/yehu/api/mine/mb-order';
import { fen2yuan } from '@/sheep/hooks/useGoods';
import sheep from '@/sheep';
import _ from 'lodash-es';
import { resetPagination } from '@/sheep/util';

const state = reactive({
  currentTab: 0,
  tabPagination: [],
  tabLoadStatus: [],
  tabRefreshing: [],
});

const loadingText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了',
};

const statusTabs = ref([
  { name: '全部', value: '' },
  { name: ORDER_STATUS_NAME[ORDER_STATUS.PENDING] || '待处理', value: 'PENDING' },
  { name: ORDER_STATUS_NAME[ORDER_STATUS.PROCESSING] || '处理中', value: 'PROCESSING' },
  { name: ORDER_STATUS_NAME[ORDER_STATUS.DELIVERED] || '待确认收货', value: 'DELIVERED' },
  { name: ORDER_STATUS_NAME[ORDER_STATUS.COMPLETED] || '已完成', value: 'COMPLETED' },
]);

function initTabs() {
  statusTabs.value.forEach(() => {
    state.tabPagination.push({
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 5,
    });
    state.tabLoadStatus.push('');
    state.tabRefreshing.push(false);
  });
}

function getTabPagination(index) {
  if (!state.tabPagination[index]) {
    state.tabPagination[index] = {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 5,
    };
    if (state.tabRefreshing[index] === undefined) {
      state.tabRefreshing[index] = false;
    }
  }
  return state.tabPagination[index];
}

function getTabLoadStatus(index) {
  return state.tabLoadStatus[index] || '';
}

function isTabRefreshing(index) {
  return !!state.tabRefreshing[index];
}

// 拉取订单数据
async function getOrderList(index = state.currentTab) {
  const pagination = getTabPagination(index);
  state.tabLoadStatus[index] = 'loading';
  const params = {
    pageNo: pagination.pageNo,
    pageSize: pagination.pageSize,
  };
  const statusValue = statusTabs.value[index]?.value;
  if (statusValue) {
    params.status = statusValue;
  }
  const { code, data } = await getMbOrderPage(params);
  if (code !== 0) {
    state.tabLoadStatus[index] = 'noMore';
    return;
  }
  const list = data?.list || [];
  if (pagination.pageNo === 1) {
    pagination.list = [];
  }
  pagination.list = _.concat(pagination.list, list);
  pagination.total = data?.total || 0;
  state.tabPagination[index] = pagination;
  state.tabLoadStatus[index] = pagination.list.length < pagination.total ? 'more' : 'noMore';
}

function onTabsChange(e) {
  if (state.currentTab === e.index) return;
  state.currentTab = e.index;
  if (!state.tabPagination[e.index] || state.tabPagination[e.index].list.length === 0) {
    resetPagination(getTabPagination(e.index));
    getOrderList(e.index);
  }
}

function onSwiperChange(e) {
  const index = e.detail.current;
  if (state.currentTab === index) return;
  state.currentTab = index;
  if (!state.tabPagination[index] || state.tabPagination[index].list.length === 0) {
    resetPagination(getTabPagination(index));
    getOrderList(index);
  }
}

function loadMore(index = state.currentTab) {
  const pagination = getTabPagination(index);
  if (pagination.list.length >= pagination.total || state.tabLoadStatus[index] === 'loading') {
    state.tabLoadStatus[index] = 'noMore';
    return;
  }
  pagination.pageNo += 1;
  getOrderList(index);
}

// 下拉刷新回调：重置分页并重新请求
async function onRefresherRefresh(index = state.currentTab) {
  const pagination = getTabPagination(index);
  resetPagination(pagination);
  state.tabLoadStatus[index] = '';
  state.tabRefreshing[index] = true;
  try {
    await getOrderList(index);
  } finally {
    state.tabRefreshing[index] = false;
  }
}

function onOrderDetail(id) {
  sheep.$router.go('/yehu/pages/mine/mb-order/detail', { id });
}

function onConfirm(orderId) {
  sheep.$router.go('/yehu/pages/mine/mb-order/detail', { id: orderId });
}

onReachBottom(() => {
  const status = getTabLoadStatus(state.currentTab);
  if (status !== 'loading' && status !== 'noMore') {
    loadMore(state.currentTab);
  }
});

onPullDownRefresh(async () => {
  await onRefresherRefresh(state.currentTab);
  uni.stopPullDownRefresh();
});

onLoad(async () => {
  initTabs();
  await getOrderList(0);
});

function formatPrice(price) {
  return price ? fen2yuan(price) : '0.00';
}

function formatOrderTime(order) {
  if (order?.createTimeDisplay) {
    return order.createTimeDisplay;
  }
  if (!order?.createTime) {
    return '--';
  }
  const date = new Date(order.createTime);
  if (Number.isNaN(date.getTime())) {
    return '--';
  }
  const y = date.getFullYear();
  const m = `${date.getMonth() + 1}`.padStart(2, '0');
  const d = `${date.getDate()}`.padStart(2, '0');
  const hh = `${date.getHours()}`.padStart(2, '0');
  const mm = `${date.getMinutes()}`.padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}`;
}

function getBizTypeName(bizType) {
  return BIZ_TYPE_NAME[bizType] || '未知类型';
}
</script>

<style lang="scss" scoped>
@import '@/sheep/scss/_var.scss';

.order-swiper {
  width: 100%;
  height: calc(100vh - 180rpx);
}

.swiper-item {
  width: 100%;
  height: 100%;
}

.swiper-content {
  height: 100%;
  padding-bottom: 40rpx;
}

.order-card {
  background: #fff;
  box-shadow: 0 8rpx 30rpx rgba(17, 27, 54, 0.06);
  padding: 24rpx 20rpx 18rpx;

  &__header {
    padding-bottom: 16rpx;
    border-bottom: 1rpx solid #f1f2f5;
  }

  &__meta {
    .order-no {
      font-size: 26rpx;
      font-weight: 600;
      color: #1a1a1a;
    }

    .order-time {
      margin-top: 6rpx;
      font-size: 24rpx;
      color: #999;
    }
  }

  .order-status {
    padding: 8rpx 22rpx;
    border-radius: 32rpx;
    font-size: 24rpx;
    background: rgba(139, 0, 0, 0.08);
    color: var(--ui-BG-Main, #8b0000);
  }

  &__body {
    padding: 18rpx 0;
    border-bottom: 1rpx solid #f1f2f5;
  }

  &__footer {
    padding-top: 16rpx;
    font-size: 24rpx;
    color: #333;

    .order-amount {
      color: #666;

      .amount-number {
        font-weight: 600;
        color: var(--ui-BG-Main, #8b0000);
      }
    }

    .order-actions {
      display: flex;
      align-items: center;
    }
  }
}

.tool-btn {
  width: 150rpx;
  height: 58rpx;
  background: #f6f6f6;
  font-size: 26rpx;
  border-radius: 30rpx;
  margin-left: 16rpx;
  color: #333;

  &.primary {
    background: linear-gradient(135deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #fff;
  }
}
</style>
