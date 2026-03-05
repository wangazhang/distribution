<!-- 订单列表 -->
<template>
  <s-layout title="我的订单">
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="tabMaps"
        :scrollable="false"
        @change="onTabsChange"
        :current="state.currentTab"
      />
    </su-sticky>
    
    <!-- 使用 swiper 支持左右滑动切换 -->
    <swiper
      class="order-swiper"
      :current="state.currentTab"
      @change="onSwiperChange"
      :duration="300"
    >
      <swiper-item
        v-for="(tab, tabIndex) in tabMaps"
        :key="tabIndex"
        class="swiper-item"
      >
        <scroll-view
          class="swiper-content"
          scroll-y
          @scrolltolower="() => loadMore(tabIndex)"
          lower-threshold="50"
          :enable-back-to-top="false"
          refresher-enabled
          :refresher-triggered="isTabRefreshing(tabIndex)"
          refresher-background="#f5f5f5"
          @refresherrefresh="() => onRefresherRefresh(tabIndex)"
        >
          <s-empty
            v-if="
              getTabPagination(tabIndex).total === 0 &&
              state.tabLoadStatus[tabIndex] !== 'loading'
            "
            icon="/static/order-empty.png"
            text="暂无订单"
          />
          <template v-else>
            <view
              class="bg-white order-list-card-box ss-r-10 ss-m-t-14 ss-m-20"
              v-for="order in getTabPagination(tabIndex).list"
              :key="order.id"
              @tap="onOrderDetail(order.id)"
            >
              <view class="order-card-header ss-flex ss-col-center ss-row-between ss-p-x-20">
                <view class="order-no">订单号：{{ order.no }}</view>
                <view class="order-state ss-font-26" :class="formatOrderColor(order)">
                  {{ formatOrderStatus(order) }}
                </view>
              </view>
              <view class="border-bottom" v-for="item in order.items" :key="item.id">
                <s-goods-item
                  :img="item.picUrl"
                  :title="item.spuName"
                  :skuText="item.properties.map((property) => property.valueName).join(' ')"
                  :price="item.price"
                  :num="item.count"
                />
              </view>
              <view class="pay-box ss-m-t-30 ss-flex ss-row-right ss-p-r-20">
                <view class="ss-flex ss-col-center">
                  <view class="discounts-title pay-color"
                    >共 {{ order.productCount }} 件商品,总金额:</view
                  >
                  <view class="discounts-money pay-color"> ￥{{ fen2yuan(order.payPrice) }} </view>
                </view>
              </view>
              <view
                class="order-card-footer ss-flex ss-col-center ss-p-x-20"
                :class="order.buttons.length > 3 ? 'ss-row-between' : 'ss-row-right'"
              >
                <view class="ss-flex ss-col-center">
                  <button
                    v-if="order.buttons.includes('combination')"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onOrderGroupon(order)"
                  >
                    拼团详情
                  </button>
                  <button
                    v-if="order.buttons.length === 0"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onOrderDetail(order.id)"
                  >
                    查看详情
                  </button>
                  <button
                    v-if="order.buttons.includes('confirm')"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onConfirm(order)"
                  >
                    确认收货
                  </button>
                  <button
                    v-if="order.buttons.includes('express')"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onExpress(order.id)"
                  >
                    查看物流
                  </button>
                  <button
                    v-if="order.buttons.includes('cancel')"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onCancel(order.id)"
                  >
                    取消订单
                  </button>
                  <button
                    v-if="order.buttons.includes('comment')"
                    class="tool-btn ss-reset-button"
                    @tap.stop="onComment(order.id)"
                  >
                    评价
                  </button>
                  <button
                    v-if="order.buttons.includes('delete')"
                    class="delete-btn ss-reset-button"
                    @tap.stop="onDelete(order.id)"
                  >
                    删除订单
                  </button>
                  <button
                    v-if="order.buttons.includes('pay')"
                    class="tool-btn ss-reset-button ui-BG-Main-Gradient"
                    @tap.stop="onPay(order.payOrderId)"
                  >
                    继续支付
                  </button>
                </view>
              </view>
            </view>
            <!-- 加载更多 -->
            <uni-load-more
              v-if="getTabPagination(tabIndex).total > 0"
              :status="state.tabLoadStatus[tabIndex] || ''"
              :content-text="{
                contentdown: '上拉加载更多',
              }"
              @tap="() => loadMore(tabIndex)"
            />
          </template>
        </scroll-view>
      </swiper-item>
    </swiper>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
  import {
    fen2yuan,
    formatOrderColor,
    formatOrderStatus,
    handleOrderButtons,
  } from '@/sheep/hooks/useGoods';
  import sheep from '@/sheep';
  import _ from 'lodash-es';
  import OrderApi from '@/sheep/api/trade/order';
  import { resetPagination } from '@/sheep/util';

  // 数据
  const state = reactive({
    currentTab: 0, // 选中的 tabMaps 下标
    // 为每个 tab 维护独立的分页数据
    tabPagination: [],
    // 为每个 tab 维护独立的加载状态
    tabLoadStatus: [],
    tabRefreshing: [],
  });

  const tabMaps = [
    {
      name: '全部',
    },
    {
      name: '待付款',
      value: 0,
    },
    {
      name: '待发货',
      value: 10,
    },
    {
      name: '待收货',
      value: 20,
    },
    {
      name: '待评价',
      value: 30,
    },
  ];

  // 初始化每个 tab 的分页数据
  function initTabPagination() {
    tabMaps.forEach(() => {
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

  // 获取指定 tab 的分页数据
  function getTabPagination(tabIndex) {
    if (!state.tabPagination[tabIndex]) {
      state.tabPagination[tabIndex] = {
        list: [],
        total: 0,
        pageNo: 1,
        pageSize: 5,
      };
    }
    if (state.tabRefreshing[tabIndex] === undefined) {
      state.tabRefreshing[tabIndex] = false;
    }
    return state.tabPagination[tabIndex];
  }

  // 获取指定 tab 的加载状态
  function getTabLoadStatus(tabIndex) {
    return state.tabLoadStatus[tabIndex] || '';
  }

  function isTabRefreshing(tabIndex) {
    return !!state.tabRefreshing[tabIndex];
  }

  // 切换选项卡（通过点击 tab）
  function onTabsChange(e) {
    if (state.currentTab === e.index) {
      return;
    }
    state.currentTab = e.index;
    // 如果该 tab 还没有加载过数据，则加载
    if (!state.tabPagination[e.index] || 
        (state.tabPagination[e.index].list.length === 0 && state.tabLoadStatus[e.index] === '')) {
      getOrderList(e.index);
    }
  }

  // swiper 滑动切换事件
  function onSwiperChange(e) {
    const newIndex = e.detail.current;
    if (state.currentTab === newIndex) {
      return;
    }
    state.currentTab = newIndex;
    // 如果该 tab 还没有加载过数据，则加载
    if (!state.tabPagination[newIndex] || 
        (state.tabPagination[newIndex].list.length === 0 && state.tabLoadStatus[newIndex] === '')) {
      getOrderList(newIndex);
    }
  }

  // 订单详情
  function onOrderDetail(id) {
    sheep.$router.go('/pages/order/detail', {
      id,
    });
  }

  // 跳转拼团记录的详情
  function onOrderGroupon(order) {
    sheep.$router.go('/pages/activity/groupon/detail', {
      id: order.combinationRecordId,
    });
  }

  // 继续支付
  function onPay(payOrderId) {
    sheep.$router.go('/pages/pay/index', {
      id: payOrderId,
    });
  }

  // 评价
  function onComment(id) {
    sheep.$router.go('/pages/goods/comment/add', {
      id,
    });
  }

  async function onConfirm(order) {
    onOrderDetail(order.id);
  }

  // 查看物流
  async function onExpress(id) {
    sheep.$router.go('/pages/order/express/log', {
      id,
    });
  }

  // 取消订单
  async function onCancel(orderId) {
    uni.showModal({
      title: '提示',
      content: '确定要取消订单吗?',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        const { code } = await OrderApi.cancelOrder(orderId);
        if (code === 0) {
          // 在所有 tab 中查找并更新订单状态
          state.tabPagination.forEach((pagination) => {
            const index = pagination.list.findIndex((order) => order.id === orderId);
            if (index !== -1) {
              const orderInfo = pagination.list[index];
              orderInfo.status = 40;
              handleOrderButtons(orderInfo);
            }
          });
        }
      },
    });
  }

  // 删除订单
  function onDelete(orderId) {
    uni.showModal({
      title: '提示',
      content: '确定要删除订单吗?',
      success: async function (res) {
        if (res.confirm) {
          const { code } = await OrderApi.deleteOrder(orderId);
          if (code === 0) {
            // 在所有 tab 中删除订单
            state.tabPagination.forEach((pagination) => {
              const index = pagination.list.findIndex((order) => order.id === orderId);
              if (index !== -1) {
                pagination.list.splice(index, 1);
                pagination.total--;
              }
            });
          }
        }
      },
    });
  }

  // 获取订单列表
  async function getOrderList(tabIndex = null) {
    const index = tabIndex !== null ? tabIndex : state.currentTab;
    const pagination = getTabPagination(index);
    
    state.tabLoadStatus[index] = 'loading';
    let { code, data } = await OrderApi.getOrderPage({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      status: tabMaps[index].value,
      commentStatus: tabMaps[index].value === 30 ? false : null,
    });
    if (code !== 0) {
      state.tabLoadStatus[index] = '';
      return;
    }
    data.list.forEach((order) => handleOrderButtons(order));
    
    if (pagination.pageNo === 1) {
      // 第一页，重置列表 - 使用响应式更新
      state.tabPagination[index].list = data.list;
    } else {
      // 追加数据 - 使用响应式更新
      state.tabPagination[index].list = _.concat(state.tabPagination[index].list, data.list);
    }
    state.tabPagination[index].total = data.total;
    state.tabLoadStatus[index] = state.tabPagination[index].list.length < state.tabPagination[index].total ? 'more' : 'noMore';
  }

  // 初始化
  initTabPagination();

  onLoad(async (options) => {
    if (options.type) {
      state.currentTab = parseInt(options.type);
    }
    // 加载当前 tab 的数据
    await getOrderList(state.currentTab);
  });

  // 加载更多
  function loadMore(tabIndex = null) {
    const index = tabIndex !== null ? tabIndex : state.currentTab;
    if (!state.tabPagination[index]) {
      return;
    }
    const pagination = state.tabPagination[index];
    const loadStatus = state.tabLoadStatus[index] || '';
    
    // 如果已经是 noMore 或者正在加载中，则不重复加载
    if (loadStatus === 'noMore' || loadStatus === 'loading') {
      return;
    }
    
    // 检查是否还有更多数据
    if (pagination.list.length >= pagination.total) {
      state.tabLoadStatus[index] = 'noMore';
      return;
    }
    
    pagination.pageNo++;
    getOrderList(index);
  }

  // 上拉加载更多（作为备用方案，如果 scroll-view 的 scrolltolower 不触发）
  onReachBottom(() => {
    // 确保当前 tab 有数据，且可以加载更多
    const loadStatus = state.tabLoadStatus[state.currentTab] || '';
    if (loadStatus !== 'noMore' && loadStatus !== 'loading') {
      loadMore(state.currentTab);
    }
  });

  // 下拉刷新回调：重置分页并重新拉取
  async function onRefresherRefresh(tabIndex = null) {
    const index = tabIndex !== null ? tabIndex : state.currentTab;
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

  // 下拉刷新
  onPullDownRefresh(async () => {
    await onRefresherRefresh(state.currentTab);
    uni.stopPullDownRefresh();
  });
</script>

<style lang="scss" scoped>
  .order-swiper {
    width: 100%;
    height: calc(100vh - 180rpx);
  }

  .swiper-item {
    width: 100%;
    height: 100%;
  }

  .swiper-content {
    width: 100%;
    height: 100%;
    padding-bottom: 20rpx;
  }

  .score-img {
    width: 36rpx;
    height: 36rpx;
    margin: 0 4rpx;
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
  }

  .delete-btn {
    width: 160rpx;
    height: 56rpx;
    //color: #ff3000;
    background: #f6f6f6;
    border-radius: 28rpx;
    font-size: 26rpx;
    margin-right: 10rpx;
    line-height: normal;

    &:last-of-type {
      margin-right: 0;
    }
  }

  .apply-btn {
    width: 140rpx;
    height: 50rpx;
    border-radius: 25rpx;
    font-size: 24rpx;
    border: 2rpx solid #dcdcdc;
    line-height: normal;
    margin-left: 16rpx;
  }

  .swiper-box {
    flex: 1;

    .swiper-item {
      height: 100%;
      width: 100%;
    }
  }

  .order-list-card-box {
    .order-card-header {
      height: 80rpx;

      .order-no {
        font-size: 26rpx;
        font-weight: 500;
      }

      .order-state {
      }
    }

    .pay-box {
      .discounts-title {
        font-size: 24rpx;
        line-height: normal;
        color: #999999;
      }

      .discounts-money {
        font-size: 24rpx;
        line-height: normal;
        color: #999;
        font-family: OPPOSANS;
      }

      .pay-color {
        color: #333;
      }
    }

    .order-card-footer {
      height: 100rpx;

      .more-item-box {
        padding: 20rpx;

        .more-item {
          height: 60rpx;

          .title {
            font-size: 26rpx;
          }
        }
      }

      .more-btn {
        color: $dark-9;
        font-size: 24rpx;
      }

      .content {
        width: 154rpx;
        color: #333333;
        font-size: 26rpx;
        font-weight: 500;
      }
    }
  }

  :deep(.uni-tooltip-popup) {
    background: var(--ui-BG);
  }

  .warning-color {
    color: #faad14;
  }

  .danger-color {
    color: #ff3000;
  }

  .success-color {
    color: #52c41a;
  }

  .info-color {
    color: #999999;
  }
</style>
