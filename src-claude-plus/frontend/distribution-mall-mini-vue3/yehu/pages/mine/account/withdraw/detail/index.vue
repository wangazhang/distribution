<template>
  <s-layout title="提现明细" back>
    <view class="withdraw-detail-page">
      <!-- 顶部筛选区域 -->
      <view class="filter-section">
<!--        <view class="time-filter">-->
<!--          <view class="filter-item" :class="{ active: timeFilter === 'all' }" @tap="changeTimeFilter('all')">全部</view>-->
<!--          <view class="filter-item" :class="{ active: timeFilter === 'month' }" @tap="changeTimeFilter('month')">本月</view>-->
<!--          <view class="filter-item" :class="{ active: timeFilter === 'week' }" @tap="changeTimeFilter('week')">本周</view>-->
<!--        </view>-->
<!--        <view class="status-filter" @tap="showStatusFilter">-->
<!--          <text>{{ statusFilterText }}</text>-->
<!--          <uni-icons type="bottom" size="12" color="#666"></uni-icons>-->
<!--        </view>-->
      </view>

      <!-- 明细列表 -->
      <view v-if="filteredWithdrawList.length > 0" class="detail-list">
        <view v-for="item in filteredWithdrawList" :key="item.id" class="detail-item" @tap="goToDetailPage(item)">
          <view class="item-top">
            <text class="item-title">提现</text>
            <text :class="['item-status', getStatusClass(item.status)]">{{ item.statusName || '处理中' }}</text>
          </view>
          <view class="item-middle">
            <text class="item-amount">{{ formatAmount(item.price) }}</text>
            <text class="item-time">{{ formatTime(item.createTime) }}</text>
          </view>
          <view class="item-bottom">
            <text class="item-desc">{{ getTypeDesc(item) }}</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <image class="empty-image" src="https://cdn.example.com/static/pic/empty/empty_data.png" mode="aspectFit"></image>
        <text class="empty-text">暂无提现记录</text>
      </view>

      <!-- 加载更多 -->
  <uni-load-more v-if="withdrawList.length > 0" :status="loadMoreStatus" :content-text="loadMoreText"></uni-load-more>
    </view>

<!--    &lt;!&ndash; 状态筛选弹窗 &ndash;&gt;-->
<!--    <uni-popup ref="statusPopup" type="bottom">-->
<!--      <view class="status-popup">-->
<!--        <view class="popup-title">提现状态</view>-->
<!--        <view class="status-options">-->
<!--          <view -->
<!--            class="status-option" -->
<!--            :class="{ active: statusFilter === 'all' }" -->
<!--            @tap="selectStatus('all')"-->
<!--          >-->
<!--            <text>全部</text>-->
<!--            <uni-icons v-if="statusFilter === 'all'" type="checkmarkempty" size="16" color="#8F1911"></uni-icons>-->
<!--          </view>-->
<!--          <view -->
<!--            class="status-option" -->
<!--            :class="{ active: statusFilter === 'success' }" -->
<!--            @tap="selectStatus('success')"-->
<!--          >-->
<!--            <text>提现成功</text>-->
<!--            <uni-icons v-if="statusFilter === 'success'" type="checkmarkempty" size="16" color="#8F1911"></uni-icons>-->
<!--          </view>-->
<!--          <view -->
<!--            class="status-option" -->
<!--            :class="{ active: statusFilter === 'pending' }" -->
<!--            @tap="selectStatus('pending')"-->
<!--          >-->
<!--            <text>处理中</text>-->
<!--            <uni-icons v-if="statusFilter === 'pending'" type="checkmarkempty" size="16" color="#8F1911"></uni-icons>-->
<!--          </view>-->
<!--          <view -->
<!--            class="status-option" -->
<!--            :class="{ active: statusFilter === 'failed' }" -->
<!--            @tap="selectStatus('failed')"-->
<!--          >-->
<!--            <text>提现失败</text>-->
<!--            <uni-icons v-if="statusFilter === 'failed'" type="checkmarkempty" size="16" color="#8F1911"></uni-icons>-->
<!--          </view>-->
<!--        </view>-->
<!--        <view class="popup-close" @tap="closeStatusPopup">取消</view>-->
<!--      </view>-->
<!--    </uni-popup>-->
  </s-layout>
</template>


<script setup>
import { computed, ref } from 'vue';
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
import dayjs from 'dayjs';
import sheep from '@/sheep';
import BrokerageApi from '@/sheep/api/trade/brokerage';
import { fen2yuan } from '@/sheep/hooks/useGoods';

const statusPopup = ref(null);
const timeFilter = ref('all');
const statusFilter = ref('all');

const withdrawList = ref([]);
const loading = ref(false);
const loadMoreStatus = ref('more');
const loadMoreText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了',
};
const pageNo = ref(1);
const pageSize = 10;
const hasMore = ref(true);
const total = ref(0);

const STATUS_GROUP_MAP = {
  success: [11,16],
  pending: [0, 10, 12, 13, 14,15],
  failed: [20, 21],
};

const statusFilterText = computed(() => {
  switch (statusFilter.value) {
    case 'success':
      return '提现成功';
    case 'pending':
      return '处理中';
    case 'failed':
      return '提现失败';
    default:
      return '全部状态';
  }
});

const filteredWithdrawList = computed(() =>
  withdrawList.value.filter(
    (item) => matchStatusFilter(item.status) && matchTimeFilter(item.createTime),
  ),
);

const matchStatusFilter = (status) => {
  if (statusFilter.value === 'all') {
    return true;
  }
  const statusList = STATUS_GROUP_MAP[statusFilter.value] || [];
  return statusList.includes(status);
};

const matchTimeFilter = (createTime) => {
  if (timeFilter.value === 'all' || !createTime) {
    return true;
  }
  const recordTime = dayjs(createTime);
  if (!recordTime.isValid()) {
    return true;
  }
  if (timeFilter.value === 'month') {
    return recordTime.isAfter(dayjs().startOf('month'));
  }
  if (timeFilter.value === 'week') {
    return recordTime.isAfter(dayjs().startOf('week'));
  }
  return true;
};

const changeTimeFilter = (filter) => {
  if (timeFilter.value === filter) return;
  timeFilter.value = filter;
};

const showStatusFilter = () => {
  statusPopup.value.open();
};

const closeStatusPopup = () => {
  statusPopup.value.close();
};

const selectStatus = (status) => {
  statusFilter.value = status;
  closeStatusPopup();
};

const formatAmount = (price) => `-${fen2yuan(price || 0)}`;

const formatTime = (time) => {
  if (!time) return '--';
  return sheep.$helper.timeFormat(time, 'yyyy-mm-dd hh:MM:ss');
};

const getTypeDesc = (item) => {
  if (item.typeName && item.bankName) {
    return `${item.typeName} · ${item.bankName}`;
  }
  if (item.typeName) {
    return item.typeName;
  }
  return '提现';
};

const getStatusClass = (status) => {
  if (STATUS_GROUP_MAP.success.includes(status)) {
    return 'status-success';
  }
  if (STATUS_GROUP_MAP.failed.includes(status)) {
    return 'status-failed';
  }
  return 'status-pending';
};

const goToDetailPage = (item) => {
  uni.navigateTo({
    url: `/yehu/pages/mine/account/withdraw/detail/detail?id=${item.id}`,
  });
};

const resetList = () => {
  withdrawList.value = [];
  pageNo.value = 1;
  hasMore.value = true;
  total.value = 0;
  loadMoreStatus.value = 'more';
};

const fetchWithdrawList = async () => {
  if (loading.value || !hasMore.value) {
    return;
  }
  loading.value = true;
  loadMoreStatus.value = 'loading';
  try {
    const params = {
      pageNo: pageNo.value,
      pageSize,
    };
    const { code, data } = await BrokerageApi.getBrokerageWithdrawPage(params);
    if (code !== 0) {
      throw new Error('获取提现明细失败');
    }
    const list = data?.list || [];
    withdrawList.value = withdrawList.value.concat(list);
    total.value = data?.total || 0;
    hasMore.value = withdrawList.value.length < total.value;
    pageNo.value += 1;
    loadMoreStatus.value = hasMore.value ? 'more' : 'nomore';
  } catch (error) {
    console.error('获取提现明细失败', error);
    sheep.$helper.toast('获取提现明细失败');
    loadMoreStatus.value = 'more';
  } finally {
    loading.value = false;
  }
};

onLoad(() => {
  resetList();
  fetchWithdrawList();
});

onReachBottom(() => {
  if (hasMore.value) {
    fetchWithdrawList();
  }
});

// 下拉刷新：重置分页并重新拉取
onPullDownRefresh(async () => {
  resetList();
  await fetchWithdrawList();
  uni.stopPullDownRefresh();
});
</script>


<style lang="scss" scoped>
.withdraw-detail-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 30rpx;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .time-filter {
    display: flex;
    
    .filter-item {
      padding: 8rpx 24rpx;
      font-size: 26rpx;
      color: #666;
      background-color: #f5f5f5;
      border-radius: 30rpx;
      margin-right: 16rpx;
      
      &.active {
        background-color: #8F1911;
        color: #fff;
      }
    }
  }
  
  .status-filter {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #666;
    
    text {
      margin-right: 6rpx;
    }
  }
}

.detail-list {
  background-color: #fff;
  
  .detail-item {
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .item-top {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15rpx;
      
      .item-title {
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
      }
      
      .item-status {
        font-size: 26rpx;
        
        &.status-success {
          color: #4CAF50;
        }
        
        &.status-pending {
          color: #FFC107;
        }
        
        &.status-failed {
          color: #FF5722;
        }
      }
    }
    
    .item-middle {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15rpx;
      
      .item-amount {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
      
      .item-time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .item-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .item-desc {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 150rpx 0;
  
  .empty-image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.status-popup {
  background-color: #fff;
  border-radius: 20rpx 20rpx 0 0;
  
  .popup-title {
    text-align: center;
    font-size: 32rpx;
    font-weight: 500;
    color: #333;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
  }
  
  .status-options {
    padding: 0 30rpx;
    
    .status-option {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx 0;
      border-bottom: 1rpx solid #f5f5f5;
      
      text {
        font-size: 30rpx;
        color: #333;
      }
      
      &.active {
        text {
          color: #8F1911;
        }
      }
    }
  }
  
  .popup-close {
    text-align: center;
    font-size: 32rpx;
    color: #333;
    padding: 30rpx 0;
    border-top: 20rpx solid #f5f5f5;
  }
}
</style>
