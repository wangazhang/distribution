<template>
  <s-layout   >
  <view class="page-container">
    <!-- 页面主体内容 -->
    <view class="page-content" >
      <!-- 顶部选项卡 -->
      <view class="tab-bar">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'today' }"
          @tap="changeTab('today')"
        >
          今日
          <view class="tab-line" v-if="activeTab === 'today'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'month' }"
          @tap="changeTab('month')"
        >
          本月
          <view class="tab-line" v-if="activeTab === 'month'"></view>
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'total' }"
          @tap="changeTab('total')"
        >
          累计
          <view class="tab-line" v-if="activeTab === 'total'"></view>
        </view>
      </view>

      <!-- 收益卡片 -->
      <view class="earnings-card">
        <view class="earnings-title">我的收益</view>
        <view class="earnings-amount">
          <text class="currency">¥</text>
          <text class="value">0.</text>
          <text class="decimal">00</text>
        </view>

        <view class="settlement-info">
          <view class="settlement-item">
            <text class="settlement-title">未结算</text>
            <text class="settlement-value">¥ 0.00</text>
          </view>
          <view class="settlement-item">
            <text class="settlement-title">已结算</text>
            <text class="settlement-value green">¥ 0.00</text>
          </view>
        </view>
      </view>

      <!-- 筛选选项 -->
      <view class="filter-bar">
        <view class="filter-options">
          <view
            class="filter-item"
            :class="{ active: activeFilter === 'unsettled' }"
            @tap="changeFilter('unsettled')"
          >
            未结算
          </view>
          <view
            class="filter-item"
            :class="{ active: activeFilter === 'settled' }"
            @tap="changeFilter('settled')"
          >
            已结算
          </view>
          <view
            class="filter-item"
            :class="{ active: activeFilter === 'paid' }"
            @tap="changeFilter('paid')"
          >
            已支出
          </view>
        </view>
        <view class="filter-button">
          <u-icon name="filter" size="28" color="#999"></u-icon>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state">
        <image class="empty-image" src="https://cdn.example.com/static/pic/empty/empty_data.png" mode="aspectFit"></image>
        <text class="empty-text">暂无数据</text>
      </view>

      <!-- 浮动返回按钮 -->
      <view class="float-back-btn" @tap="goBack">
        <u-icon name="arrow-left" size="28" color="#666"></u-icon>
      </view>
    </view>
  </view>
</s-layout>
</template>

<script setup >
import { ref, computed, onMounted } from 'vue';

// 状态栏高度
const statusBarHeight = ref(0);
// 导航栏内容高度（固定值）
const navbarContentHeight = 44;
// 计算导航栏总高度
const navbarHeight = computed(() => {
  return statusBarHeight.value + navbarContentHeight;
});

// 激活的选项卡
const activeTab = ref('today');
// 激活的筛选选项
const activeFilter = ref('unsettled');

// 切换选项卡
const changeTab = (tab) => {
  activeTab.value = tab;
};

// 切换筛选选项
const changeFilter = (filter) => {
  activeFilter.value = filter;
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 返回首页
const navigateToHome = () => {
  uni.navigateTo({
    url: '/pages/index/index'
  });
};

onMounted(() => {
  // 获取状态栏高度
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0;
    }
  });
});
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  background-color: #fff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;

  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;
    }

    .navbar-back, .navbar-home {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .navbar-title {
      font-size: 34rpx;
      font-weight: 500;
      color: #000;
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
    }

    .navbar-right {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
    }
  }
}

.page-content {
  .tab-bar {
    display: flex;
    background-color: #fff;
    border-bottom: 1px solid #f0f0f0;

    .tab-item {
      flex: 1;
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #8F1911;
        font-weight: 500;
      }

      .tab-line {
        position: absolute;
        bottom: 0;
        width: 60rpx;
        height: 4rpx;
        background-color: #8F1911;
        border-radius: 2rpx;
      }
    }
  }

  .earnings-card {
    background-color: #fff;
    padding: 40rpx 30rpx;

    .earnings-title {
      font-size: 28rpx;
      color: #8F1911;
      text-align: center;
      margin-bottom: 20rpx;
    }

    .earnings-amount {
      text-align: center;
      margin-bottom: 40rpx;

      .currency {
        font-size: 40rpx;
        color: #8F1911;
      }

      .value {
        font-size: 72rpx;
        color: #8F1911;
        font-weight: 500;
      }

      .decimal {
        font-size: 40rpx;
        color: #8F1911;
      }
    }

    .settlement-info {
      display: flex;
      justify-content: space-around;
      border-top: 1rpx dashed #eee;
      padding-top: 30rpx;

      .settlement-item {
        display: flex;
        flex-direction: column;
        align-items: center;

        .settlement-title {
          font-size: 26rpx;
          color: #999;
          margin-bottom: 10rpx;
        }

        .settlement-value {
          font-size: 32rpx;
          color: #FFC107;
          font-weight: 500;

          &.green {
            color: #4CAF50;
          }
        }
      }
    }
  }

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    padding: 20rpx 30rpx;
    margin-top: 20rpx;

    .filter-options {
      display: flex;

      .filter-item {
        margin-right: 30rpx;
        font-size: 28rpx;
        color: #666;
        padding: 10rpx 20rpx;
        border-radius: 30rpx;
        background-color: #f5f5f5;

        &.active {
          color: #fff;
          background-color: #8F1911;
        }
      }
    }

    .filter-button {
      padding: 10rpx;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    margin-top: 100rpx;

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

  .float-back-btn {
    position: fixed;
    bottom: 100rpx;
    right: 30rpx;
    width: 80rpx;
    height: 80rpx;
    background-color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
  }
}
</style>