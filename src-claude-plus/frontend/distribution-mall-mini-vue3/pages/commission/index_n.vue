<!-- 分销中心  -->
<template>
  <s-layout
    navbar="inner"
    class="commission-center"
    title="我的事业"
    :onShareAppMessage="shareInfo"
  >
    <!-- 顶部背景 -->
    <view class="page-bg">
      <image class="bg-image" src="/static/images/commission/beauty-bg.svg" mode="aspectFill" />
      <view class="bg-mask"></view>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info">
        <image
          class="avatar"
          :src="userInfo.avatar || '/static/images/commission/default-avatar.svg'"
          mode="aspectFill"
        />
        <view class="info-content">
          <text class="nickname">{{ userInfo.nickname || '美丽顾问' }}</text>
          <text class="level">{{ userInfo.level || 'LV1 初级顾问' }}</text>
        </view>
      </view>
      <view class="user-stats">
        <view class="stat-item">
          <text class="number">{{ formatNumber(stats.totalCustomers) }}</text>
          <text class="label">我的客户</text>
        </view>
        <view class="stat-item">
          <text class="number">{{ formatNumber(stats.monthlyIncome) }}</text>
          <text class="label">本月收益</text>
        </view>
        <view class="stat-item">
          <text class="number">{{ formatNumber(stats.totalIncome) }}</text>
          <text class="label">累计收益</text>
        </view>
      </view>
    </view>

    <!-- 快捷功能区 -->
    <view class="quick-actions">
      <view class="action-item" @tap="navigateTo('/pages/commission/goods')">
        <view class="action-icon">
          <image src="/static/images/commission/icon-goods.svg" mode="aspectFit" />
        </view>
        <text>推广商品</text>
      </view>
      <view class="action-item" @tap="navigateTo('/pages/commission/team')">
        <view class="action-icon">
          <image src="/static/images/commission/icon-team.svg" mode="aspectFit" />
        </view>
        <text>我的团队</text>
      </view>
      <view class="action-item" @tap="navigateTo('/pages/commission/customer')">
        <view class="action-icon">
          <image src="/static/images/commission/icon-customer.svg" mode="aspectFit" />
        </view>
        <text>客户管理</text>
      </view>
      <view class="action-item" @tap="navigateTo('/pages/commission/poster')">
        <view class="action-icon">
          <image src="/static/images/commission/icon-poster.svg" mode="aspectFit" />
        </view>
        <text>推广海报</text>
      </view>
    </view>

    <!-- 收益卡片 -->
    <view class="earnings-card">
      <view class="card-header">
        <text class="title">收益概览</text>
        <text class="more" @tap="navigateTo('/pages/commission/earnings')">查看明细</text>
      </view>
      <view class="earnings-content">
        <view class="earnings-item">
          <text class="amount">¥{{ formatNumber(earnings.today) }}</text>
          <text class="label">今日预估</text>
        </view>
        <view class="earnings-item">
          <text class="amount">¥{{ formatNumber(earnings.yesterday) }}</text>
          <text class="label">昨日收益</text>
        </view>
        <view class="earnings-item">
          <text class="amount">¥{{ formatNumber(earnings.thisMonth) }}</text>
          <text class="label">本月收益</text>
        </view>
      </view>
    </view>

    <!-- 业绩走势 -->
    <view class="performance-card">
      <view class="card-header">
        <text class="title">业绩走势</text>
        <view class="time-filter">
          <text
            v-for="(item, index) in timeFilters"
            :key="index"
            :class="['filter-item', { active: currentTimeFilter === item.value }]"
            @tap="changeTimeFilter(item.value)"
          >
            {{ item.label }}
          </text>
        </view>
      </view>
      <view class="chart-container">
        <!-- 这里可以接入图表组件 -->
      </view>
    </view>

    <!-- 最近业绩 -->
    <view class="recent-performance">
      <view class="card-header">
        <text class="title">最近业绩</text>
      </view>
      <view class="performance-list">
        <view class="performance-item" v-for="(item, index) in recentPerformance" :key="index">
          <image class="customer-avatar" :src="item.customerAvatar" mode="aspectFill" />
          <view class="performance-info">
            <text class="customer-name">{{ item.customerName }}</text>
            <text class="order-info">{{ item.productName }}</text>
          </view>
          <view class="performance-amount">
            <text class="amount">+¥{{ formatNumber(item.amount) }}</text>
            <text class="time">{{ item.time }}</text>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import sheep from '@/sheep';
  import { SharePageEnum } from '@/sheep/util/const';

  // 分享信息
  const shareInfo = computed(() => {
    return sheep.$platform.share.getShareInfo(
      {
        params: {
          page: SharePageEnum.HOME.value,
        },
      },
      {
        type: 'user',
      },
    );
  });

  // 用户信息
  const userInfo = ref({
    avatar: '',
    nickname: '',
    level: '',
  });

  // 统计数据
  const stats = ref({
    totalCustomers: 0,
    monthlyIncome: 0,
    totalIncome: 0,
  });

  // 收益数据
  const earnings = ref({
    today: 0,
    yesterday: 0,
    thisMonth: 0,
  });

  // 时间筛选选项
  const timeFilters = [
    { label: '7天', value: '7d' },
    { label: '30天', value: '30d' },
    { label: '90天', value: '90d' },
  ];
  const currentTimeFilter = ref('7d');

  // 最近业绩列表
  const recentPerformance = ref([]);

  // 格式化数字
  const formatNumber = (num) => {
    return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  };

  // 页面跳转
  const navigateTo = (url) => {
    sheep.$router.go(url);
  };

  // 切换时间筛选
  const changeTimeFilter = (value) => {
    currentTimeFilter.value = value;
    // TODO: 加载对应时间段的数据
  };

  // 初始化数据
  onMounted(() => {
    // TODO: 加载用户信息和统计数据
  });
</script>

<style lang="scss" scoped>
  .commission-center {
    min-height: 100vh;
    background: #f8f9fc;
    position: relative;
  }

  .page-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400rpx;
    z-index: 0;

    .bg-image {
      width: 100%;
      height: 100%;
    }

    .bg-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, rgba(248, 249, 252, 1) 100%);
    }
  }

  .user-card {
    margin: 20rpx;
    padding: 30rpx;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 20rpx;
    backdrop-filter: blur(10px);
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
    position: relative;
    z-index: 1;

    .user-info {
      display: flex;
      align-items: center;
      margin-bottom: 30rpx;

      .avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 60rpx;
        margin-right: 20rpx;
      }

      .info-content {
        flex: 1;

        .nickname {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 8rpx;
        }

        .level {
          font-size: 24rpx;
          color: #ff9500;
          background: rgba(255, 149, 0, 0.1);
          padding: 4rpx 16rpx;
          border-radius: 20rpx;
        }
      }
    }

    .user-stats {
      display: flex;
      justify-content: space-between;
      padding-top: 20rpx;
      border-top: 2rpx solid rgba(0, 0, 0, 0.05);

      .stat-item {
        flex: 1;
        text-align: center;

        .number {
          display: block;
          font-size: 36rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 8rpx;
        }

        .label {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }

  .quick-actions {
    margin: 20rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 20rpx;
    display: flex;
    justify-content: space-between;

    .action-item {
      flex: 1;
      text-align: center;

      .action-icon {
        width: 80rpx;
        height: 80rpx;
        margin: 0 auto 12rpx;
        background: #f8f9fc;
        border-radius: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        image {
          width: 40rpx;
          height: 40rpx;
        }
      }

      text {
        font-size: 24rpx;
        color: #333;
      }
    }
  }

  .earnings-card,
  .performance-card {
    margin: 20rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 20rpx;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30rpx;

      .title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }

      .more {
        font-size: 24rpx;
        color: #999;
      }

      .time-filter {
        display: flex;
        background: #f8f9fc;
        border-radius: 30rpx;
        padding: 4rpx;

        .filter-item {
          padding: 8rpx 20rpx;
          font-size: 24rpx;
          color: #666;
          border-radius: 26rpx;

          &.active {
            background: #fff;
            color: #333;
            box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
          }
        }
      }
    }
  }

  .earnings-content {
    display: flex;
    justify-content: space-between;

    .earnings-item {
      flex: 1;
      text-align: center;

      .amount {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: #ff9500;
        margin-bottom: 8rpx;
      }

      .label {
        font-size: 24rpx;
        color: #666;
      }
    }
  }

  .recent-performance {
    margin: 20rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 20rpx;

    .performance-list {
      .performance-item {
        display: flex;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 2rpx solid #f8f9fc;

        &:last-child {
          border-bottom: none;
        }

        .customer-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 40rpx;
          margin-right: 20rpx;
        }

        .performance-info {
          flex: 1;

          .customer-name {
            font-size: 28rpx;
            color: #333;
            margin-bottom: 8rpx;
          }

          .order-info {
            font-size: 24rpx;
            color: #999;
          }
        }

        .performance-amount {
          text-align: right;

          .amount {
            display: block;
            font-size: 32rpx;
            font-weight: 600;
            color: #ff9500;
            margin-bottom: 8rpx;
          }

          .time {
            font-size: 24rpx;
            color: #999;
          }
        }
      }
    }
  }
</style>
