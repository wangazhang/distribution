<!-- 分销 - 订单明细 -->
<template>
  <s-layout title="分佣订单" :class="state.scrollTop ? 'order-warp' : ''" navbar="inner">
    <view
      class="header-box"
      :style="[
        {
          marginTop: '-' + Number(statusBarHeight + 88) + 'rpx',
          paddingTop: Number(statusBarHeight + 108) + 'rpx',
        },
      ]"
    >
      <!-- 团队数据总览 -->
      <view class="team-data-box ss-flex ss-col-center ss-row-between" style="width: 100%">
        <view class="data-card" style="width: 100%">
          <view class="total-item" style="width: 100%">
            <view class="item-title" style="text-align: center">累计推广订单（单）</view>
            <view class="total-num" style="text-align: center">
              {{ state.totals }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- tab -->
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="tabMaps"
        :scrollable="false"
        :current="state.currentTab"
        @change="onTabsChange"
      >
      </su-tabs>
    </su-sticky>

    <!-- 订单 -->
    <view class="order-box">
      <view
        class="order-item"
        v-for="item in state.pagination.list"
        :key="item.id || item.bizId"
        @tap="goOrderDetail(item.id)"
      >
        <view class="order-row">
          <view class="order-title">{{ item.title || '--' }}</view>
          <view class="order-amount">
            <text class="label">佣金</text>
            <text class="value">{{ fen2yuan(item.price) }}</text>
            <text class="unit">元</text>
          </view>
        </view>
        <view class="order-row order-meta">
          <text class="meta">业务编号：{{ item.bizId || '--' }}</text>
          <text class="meta">
            {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM:ss') }}
          </text>
        </view>
        <view class="order-row order-status">
          <view class="status-tag" :class="statusClass(item.status)">
            {{ statusText(item.status) }}
          </view>
          <text class="detail-tip">查看详情</text>
        </view>
      </view>
      <!-- 数据为空 -->
      <s-empty v-if="state.pagination.total === 0" icon="/static/order-empty.png" text="暂无订单" />
      <!-- 加载更多 -->
      <uni-load-more
        v-if="state.pagination.total > 0"
        :status="state.loadStatus"
        :content-text="{
          contentdown: '上拉加载更多',
        }"
        @tap="loadMore"
      />
    </view>
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad, onPageScroll, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import _ from 'lodash-es';
  import { resetPagination } from '@/sheep/util';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import DictApi from '@/sheep/api/system/dict';
  import { fen2yuan } from '../../sheep/hooks/useGoods';

  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;
  const headerBg = sheep.$url.css('/static/img/shop/user/withdraw_bg.png');

  onPageScroll((e) => {
    state.scrollTop = e.scrollTop <= 100;
  });

  const WITHDRAW_BIZ_TYPE = 2; // 字典 brokerage_record_biz_type 中提现对应值

  const state = reactive({
    totals: 0, // 累计推广订单（单）
    scrollTop: false,

    currentTab: 0,
    loadStatus: '',
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
    withdrawBizType: WITHDRAW_BIZ_TYPE,
    refreshing: false,
  });

  const tabMaps = [
    {
      name: '全部',
      value: -1,
    },
    {
      name: '待结算',
      value: 0, // 待结算
    },
    {
      name: '已结算',
      value: 1, // 已结算
    },
  ];

  // 切换选项卡
  function onTabsChange(e) {
    resetPagination(state.pagination);
    state.currentTab = e.index;
    state.loadStatus = '';
    getOrderList({ reset: true });
  }

  // 获取订单列表
  async function getOrderList(options = {}) {
    const { reset = false } = options;
    if (state.loadStatus === 'loading' && !reset) {
      return;
    }
    state.loadStatus = 'loading';
    if (reset) {
      state.pagination.list = [];
      state.pagination.pageNo = 1;
      state.pagination.total = 0;
    }
    const tab = tabMaps[state.currentTab];
    let requestPageNo = state.pagination.pageNo;
    const isInitialLoad = state.pagination.list.length === 0;
    const buildQueryParams = (pageNo) => {
      const params = {
        pageSize: state.pagination.pageSize,
        pageNo,
      };
      if (tab.value >= 0) {
        params.status = tab.value;
      }
      return params;
    };
    try {
      while (true) {
        const { code, data } = await BrokerageApi.getBrokerageRecordPage(buildQueryParams(requestPageNo));
        if (code !== 0) {
          state.loadStatus = '';
          return;
        }
        const pageList = Array.isArray(data?.list) ? data.list : [];
        const pageSize = data?.pageSize || state.pagination.pageSize;
        const withdrawCode = Number(state.withdrawBizType ?? WITHDRAW_BIZ_TYPE);
        const filteredList = pageList.filter(
          (item) => Number(item?.bizType) !== withdrawCode,
        );
        if (filteredList.length > 0) {
          state.pagination.list = _.concat(state.pagination.list, filteredList);
          state.pagination.total = state.pagination.list.length;
          state.loadStatus = pageList.length < pageSize ? 'noMore' : 'more';
          requestPageNo += 1;
          break;
        }
        if (pageList.length < pageSize) {
          state.loadStatus = 'noMore';
          requestPageNo += 1;
          break;
        }
        requestPageNo += 1;
      }
      if (state.currentTab === 0 && isInitialLoad) {
        await refreshTotalCount(tab.value);
      }
    } catch (error) {
      console.error('获取分佣订单失败', error);
      state.loadStatus = '';
    } finally {
      state.pagination.pageNo = requestPageNo;
      if (reset) {
        state.refreshing = false;
      }
    }
  }

  onLoad(async () => {
    await initWithdrawBizType();
    getOrderList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore' || state.loadStatus === 'loading') {
      return;
    }
    getOrderList();
  }

  function goOrderDetail(id) {
    if (!id) {
      sheep.$helper.toast('记录编号缺失');
      return;
    }
    uni.navigateTo({
      url: `/pages/commission/orderDetail?id=${id}`,
    });
  }

  function statusText(status) {
    if (status === 1) return '已结算';
    if (status === 0) return '待结算';
    return '已取消';
  }

  function statusClass(status) {
    if (status === 1) return 'is-success';
    if (status === 0) return 'is-pending';
    return 'is-cancel';
  }

  onPullDownRefresh(async () => {
    state.refreshing = true;
    await getOrderList({ reset: true });
    uni.stopPullDownRefresh();
  });

  // 拉取提现业务类型，便于动态匹配字典配置
  async function initWithdrawBizType() {
    try {
      const { code, data } = await DictApi.getDictDataListByType('brokerage_record_biz_type');
      if (code !== 0 || !Array.isArray(data)) {
        return;
      }
      const withdrawItem = data.find(
        (item) => item?.label?.includes('提现') && !item?.label?.includes('驳回'),
      );
      if (withdrawItem && withdrawItem.value !== undefined) {
        const parsedValue = Number(withdrawItem.value);
        if (!Number.isNaN(parsedValue)) {
          state.withdrawBizType = parsedValue;
        }
      }
    } catch (error) {
      console.error('获取分佣业务类型字典失败', error);
    }
  }

  // 重新计算指定状态下的总订单数（排除提现类型）
  async function refreshTotalCount(status) {
    const baseParams = {
      pageSize: 1,
      pageNo: 1,
    };
    if (status >= 0) {
      baseParams.status = status;
    }
    try {
      const totalPromise = BrokerageApi.getBrokerageRecordPage(baseParams);
      const withdrawCode = state.withdrawBizType;
      const withdrawPromise =
        withdrawCode === undefined || withdrawCode === null
          ? Promise.resolve(null)
          : BrokerageApi.getBrokerageRecordPage({
              ...baseParams,
              bizType: withdrawCode,
            });
      const [totalResp, withdrawResp] = await Promise.all([totalPromise, withdrawPromise]);
      if (!totalResp || totalResp.code !== 0) {
        return;
      }
      let total = totalResp.data?.total ?? 0;
      if (withdrawResp?.code === 0) {
        total -= withdrawResp.data?.total ?? 0;
      }
      state.totals = total > 0 ? total : 0;
    } catch (error) {
      console.error('统计分佣订单数量失败', error);
      state.totals = state.pagination.total;
    }
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  .header-box {
    box-sizing: border-box;
    padding: 0 20rpx 20rpx 20rpx;
    width: 750rpx;
    background: v-bind(headerBg) no-repeat,
      linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    background-size: 750rpx 100%;

    // 团队信息总览
    .team-data-box {
      .data-card {
        width: 305rpx;
        background: #ffffff;
        border-radius: 20rpx;
        padding: 20rpx;

        .total-item {
          margin-bottom: 30rpx;

          .item-title {
            font-size: 24rpx;
            font-weight: 500;
            color: #999999;
            line-height: normal;
            margin-bottom: 20rpx;
          }

          .total-num {
            font-size: 38rpx;
            font-weight: 500;
            color: #333333;
            font-family: OPPOSANS;
          }
        }

        .category-num {
          font-size: 26rpx;
          font-weight: 500;
          color: #333333;
          font-family: OPPOSANS;
        }
      }
    }

    // 直推
    .direct-box {
      margin-top: 20rpx;

      .direct-item {
        width: 340rpx;
        background: #ffffff;
        border-radius: 20rpx;
        padding: 20rpx;
        box-sizing: border-box;

        .item-title {
          font-size: 22rpx;
          font-weight: 500;
          color: #999999;
          margin-bottom: 6rpx;
        }

        .item-value {
          font-size: 38rpx;
          font-weight: 500;
          color: #333333;
          font-family: OPPOSANS;
        }
      }
    }
  }

  // 订单
  .order-box {
    .order-item {
      background: #ffffff;
      border-radius: 16rpx;
      margin: 20rpx;
      padding: 26rpx;
      box-shadow: 0 8rpx 26rpx rgba(0, 0, 0, 0.05);
      box-sizing: border-box;

      .order-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;
      }

      .order-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        flex: 1;
        padding-right: 20rpx;
      }

      .order-amount {
        display: flex;
        align-items: flex-end;
        gap: 6rpx;

        .label {
          font-size: 24rpx;
          color: #999;
        }

        .value {
          font-size: 42rpx;
          font-weight: 700;
          color: var(--ui-BG-Main);
        }

        .unit {
          font-size: 24rpx;
          color: #999;
          margin-bottom: 4rpx;
        }
      }

      .order-meta {
        .meta {
          font-size: 24rpx;
          color: #999;
        }

        .meta + .meta {
          margin-left: 20rpx;
        }
      }

      .order-status {
        margin-bottom: 0;

        .status-tag {
          font-size: 22rpx;
          padding: 6rpx 20rpx;
          border-radius: 30rpx;
        }

        .detail-tip {
          font-size: 22rpx;
          color: #bbb;
        }
      }

      .status-tag.is-success {
        color: #1bbc9c;
        background: rgba(27, 188, 156, 0.16);
      }

      .status-tag.is-pending {
        color: #ff9900;
        background: rgba(255, 153, 0, 0.16);
      }

      .status-tag.is-cancel {
        color: #999;
        background: rgba(153, 153, 153, 0.16);
      }
    }
  }
</style>
