<template>
  <s-layout title="消息中心">
    <view class="page-container" >
<!--      &lt;!&ndash; 自定义顶部导航栏 &ndash;&gt;-->
<!--      <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
<!--        <view class="navbar-content">-->
<!--          <view class="navbar-left">-->
<!--            <view class="navbar-back" @tap="goBack">-->
<!--              <uni-icons type="arrow-left" size="36rpx" color="#333"></uni-icons>-->
<!--            </view>-->
<!--            <view class="navbar-home" @tap="navigateToHome">-->
<!--              <uni-icons type="home" size="36rpx" color="#333"></uni-icons>-->
<!--            </view>-->
<!--          </view>-->
<!--          <view class="navbar-title">消息中心</view>-->
<!--          <view class="navbar-right"></view>-->
<!--        </view>-->
<!--      </view>-->

      <!-- 消息列表 -->
      <view class="message-list">
        <!-- 交易通知 -->
        <view class="message-item" @tap="navigateToDetail('trade')">
          <view class="message-icon trade-icon">
            <uni-icons type="wallet" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">交易通知</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>

        <!-- 活动通知 -->
        <view class="message-item" @tap="navigateToDetail('activity')">
          <view class="message-icon activity-icon">
            <uni-icons type="flag" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">活动通知</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>

        <!-- 互动消息 -->
        <view class="message-item" @tap="navigateToDetail('interaction')">
          <view class="message-icon interaction-icon">
            <uni-icons type="checkmark" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">互动消息</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>

        <!-- 系统消息 -->
        <view class="message-item" @tap="navigateToDetail('system')">
          <view class="message-icon system-icon">
            <uni-icons type="notification" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">系统消息</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>

        <!-- 客服消息 -->
        <view class="message-item" @tap="navigateToDetail('service')">
          <view class="message-icon service-icon">
            <uni-icons type="chat" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">客服消息</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>

        <!-- 服务通知 -->
        <view class="message-item" @tap="navigateToDetail('notification')">
          <view class="message-icon notification-icon">
            <uni-icons type="sound" size="24" color="#fff"></uni-icons>
          </view>
          <view class="message-content">
            <view class="message-title">服务通知</view>
            <view class="message-empty">暂无消息</view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted } from 'vue';

  // 状态栏高度
  const statusBarHeight = ref(0);

  // 返回上一页
  const goBack = () => {
    uni.navigateBack();
  };

  // 跳转到首页
  const navigateToHome = () => {
    uni.navigateTo({
      url: '/pages/index/index',
    });
  };

  // 跳转到详情页
  const navigateToDetail = (type) => {
    uni.navigateTo({
      url: `/pages/mine/message/detail?type=${type}`,
    });
  };

  onMounted(() => {
    // 获取状态栏高度
    uni.getSystemInfo({
      success: (res) => {
        statusBarHeight.value = res.statusBarHeight || 0;
      },
    });
  });
</script>

<style lang="scss" scoped>
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
    position: relative;
  }

  .custom-navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10;
    background-color: #fff;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .navbar-content {
      height: 44px;
      display: flex;
      align-items: center;
      padding: 0 30rpx;

      .navbar-left {
        display: flex;
        align-items: center;

        .navbar-back,
        .navbar-home {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .navbar-title {
        flex: 1;
        text-align: center;
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }

      .navbar-right {
        width: 72px; /* 两个按钮的宽度，保持对称 */
      }
    }
  }

  .message-list {
    padding: 20rpx 0;

    .message-item {
      margin: 20rpx 0;
      padding: 30rpx;
      background-color: #fff;
      display: flex;
      align-items: center;

      .message-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .trade-icon {
        background: linear-gradient(135deg, #ff7f50, #ff6347);
      }

      .activity-icon {
        background: linear-gradient(135deg, #ffd700, #ffa500);
      }

      .interaction-icon {
        background: linear-gradient(135deg, #ffd700, #ffa500);
      }

      .system-icon {
        background: linear-gradient(135deg, #6495ed, #4169e1);
      }

      .service-icon {
        background: linear-gradient(135deg, #3cb371, #2e8b57);
      }

      .notification-icon {
        background: linear-gradient(135deg, #6a5acd, #483d8b);
      }

      .message-content {
        flex: 1;

        .message-title {
          font-size: 30rpx;
          font-weight: 500;
          color: #333;
          margin-bottom: 8rpx;
        }

        .message-empty {
          font-size: 26rpx;
          color: #999;
        }
      }
    }
  }
</style>