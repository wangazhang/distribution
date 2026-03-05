<template>
  <s-layout title="中台管理" navbar="normal">
    <view class="page-container">
      <!--    &lt;!&ndash; 顶部导航栏 &ndash;&gt;-->
      <!--    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
      <!--      <view class="navbar-content">-->
      <!--        <view class="navbar-left">-->
      <!--          <view class="navbar-back" @click="goBack">-->
      <!--            <u-icon name="arrow-left" size="36" color="#000"></u-icon>-->
      <!--          </view>-->
      <!--          <view class="navbar-home" @click="navigateToHome">-->
      <!--            <u-icon name="home" size="36" color="#000"></u-icon>-->
      <!--          </view>-->
      <!--        </view>-->
      <!--        <view class="navbar-title">中台管理</view>-->
      <!--        <view class="navbar-right">-->
      <!--          <u-icon name="more-dot-fill" size="36" color="#000"></u-icon>-->
      <!--        </view>-->
      <!--      </view>-->
      <!--    </view>-->

      <!-- 页面主体内容 -->
      <view class="page-content">
        <!-- 用户信息区域 -->
        <view class="user-profile">
          <view class="notification-icon">
            <u-icon name="bell" size="26" color="#000"></u-icon>
          </view>
          <view class="user-info">
            <view class="user-avatar-wrapper">
              <image
                class="user-avatar"
                src="https://cdn.example.com/static/pic/avatar/avatar_2.png"
                mode="aspectFill"
              ></image>
            </view>
            <view class="user-detail">
              <view class="user-name-row">
                <text class="user-name">王阿章</text>
                <view class="user-tag">
                  <text class="tag-icon">V</text>
                  <text class="tag-text">游客</text>
                </view>
              </view>
              <view class="user-id"
                >会员编号: 401
                <u-icon name="file-text" size="16" color="#999"></u-icon>
              </view>
            </view>
          </view>
        </view>

        <!-- 收入统计卡片 -->
        <view class="income-card">
          <view class="income-item">
            <text class="income-value">0.00</text>
            <text class="income-label"
              >今日收入(元)
              <u-icon name="arrow-right" size="16" color="#fff"></u-icon>
            </text>
          </view>
          <view class="income-item">
            <text class="income-value">0.00</text>
            <text class="income-label"
              >本月收入(元)
              <u-icon name="arrow-right" size="16" color="#fff"></u-icon>
            </text>
          </view>
          <view class="income-item">
            <text class="income-value">0.00</text>
            <text class="income-label"
              >累计收益
              <u-icon name="arrow-right" size="16" color="#fff"></u-icon>
            </text>
          </view>
        </view>

        <!-- 账户余额和收入 -->
        <view class="balance-section">
          <view class="balance-info">
            <view class="balance-item">
              <text class="balance-value">0.00</text>
              <text class="balance-label"
                >账户余额(元)
                <u-icon name="question-circle" size="16" color="#999"></u-icon>
              </text>
            </view>
            <view class="balance-item">
              <text class="balance-value">0.00</text>
              <text class="balance-label"
                >我的收入(元)
                <u-icon name="question-circle" size="16" color="#999"></u-icon>
              </text>
            </view>
          </view>
          <view class="action-buttons">
            <view class="action-button withdraw">提现</view>
            <view class="action-button detail">明细</view>
          </view>
        </view>

        <!-- 收益统计 -->
        <view class="stat-section" @tap="navigateToEarnings">
          <view class="stat-header">
            <text class="stat-title">我的收益</text>
            <u-icon name="question-circle" size="24" color="#999"></u-icon>
            <u-icon name="arrow-right" size="24" color="#999" class="arrow-icon"></u-icon>
          </view>
          <view class="stat-content">
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">累计收益</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">今日新增</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">本月新增</text>
            </view>
          </view>
        </view>

        <!-- 订单统计 -->
        <view class="stat-section">
          <view class="stat-header">
            <text class="stat-title">所有订单</text>
            <u-icon name="question-circle" size="24" color="#999"></u-icon>
            <u-icon name="arrow-right" size="24" color="#999" class="arrow-icon"></u-icon>
          </view>
          <view class="stat-content">
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">累计订单</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">今日新增</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">本月新增</text>
            </view>
          </view>
        </view>

        <!-- 社群统计 -->
        <view class="stat-section" @tap="navigateToCommunity">
          <view class="stat-header">
            <text class="stat-title">我的社群</text>
            <u-icon name="question-circle" size="24" color="#999"></u-icon>
            <u-icon name="arrow-right" size="24" color="#999" class="arrow-icon"></u-icon>
          </view>
          <view class="stat-content">
            <view class="stat-item">
              <text class="stat-number">2</text>
              <text class="stat-label">累计人群</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">0</text>
              <text class="stat-label">今日新增</text>
            </view>
            <view class="stat-item">
              <text class="stat-number">1</text>
              <text class="stat-label">本月新增</text>
            </view>
          </view>
        </view>

        <!-- 我的频道 -->
        <view class="channel-section">
          <text class="channel-title">我的频道</text>
          <view class="channel-grid">
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="star" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">推品助手</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="bookmark" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">活动中心</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="star-fill" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">我的待办</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="footprint" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">浏览足迹</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="chart" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">月度业绩</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="account-fill" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">团队排行</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="shopping-cart-fill" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">店铺流量</text>
            </view>
            <view class="channel-item">
              <view class="channel-icon">
                <u-icon name="integral-fill" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">业绩分析</text>
            </view>
            <!-- mock配置入口 -->
            <view class="channel-item" @click="navigateToMockConfig">
              <view class="channel-icon">
                <u-icon name="setting-fill" size="36" color="#333"></u-icon>
              </view>
              <text class="channel-name">接口Mock配置</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';

  // 状态栏高度
  const statusBarHeight = ref(0);
  // 导航栏内容高度（固定值）
  const navbarContentHeight = 44;
  // 计算导航栏总高度
  const navbarHeight = computed(() => {
    return statusBarHeight.value + navbarContentHeight;
  });

  // 返回上一页
  const goBack = () => {
    uni.navigateBack();
  };

  // 返回首页
  const navigateToHome = () => {
    uni.navigateTo({
      url: '/pages/index/index',
    });
  };

  // 跳转到收益页面
  const navigateToEarnings = () => {
    uni.navigateTo({
      url: '/yehu/pages/mine/management/earnings/index',
    });
  };

  // 跳转到社群页面
  const navigateToCommunity = () => {
    uni.navigateTo({
      url: '/yehu/pages/mine/management/community/index',
    });
  };

  // 跳转到接口Mock配置页面
  const navigateToMockConfig = () => {
    uni.navigateTo({
      url: '/yehu/pages/mine/management/mock-config/index',
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
    background-color: #f5f5f5;
    padding-bottom: 30rpx;
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

      .navbar-back,
      .navbar-home {
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
    /* 移除之前的padding-top，使用内联样式动态设置 */
    /* padding-top: calc(44px + var(--status-bar-height)); */

    .user-profile {
      position: relative;
      background-color: #fff3f3;
      padding: 30rpx;

      .notification-icon {
        position: absolute;
        top: 30rpx;
        right: 30rpx;
      }

      .user-info {
        display: flex;
        align-items: center;

        .user-avatar-wrapper {
          width: 100rpx;
          height: 100rpx;
          border-radius: 50%;
          overflow: hidden;
          margin-right: 20rpx;

          .user-avatar {
            width: 100%;
            height: 100%;
          }
        }

        .user-detail {
          .user-name-row {
            display: flex;
            align-items: center;
            margin-bottom: 10rpx;

            .user-name {
              font-size: 34rpx;
              font-weight: 500;
              margin-right: 16rpx;
            }

            .user-tag {
              display: flex;
              align-items: center;
              background-color: #ffb74d;
              border-radius: 30rpx;
              padding: 4rpx 16rpx;

              .tag-icon {
                color: #fff;
                font-size: 24rpx;
                font-weight: bold;
                margin-right: 4rpx;
              }

              .tag-text {
                color: #fff;
                font-size: 22rpx;
              }
            }
          }

          .user-id {
            font-size: 24rpx;
            color: #666;
            display: flex;
            align-items: center;
          }
        }
      }
    }

    .income-card {
      background: linear-gradient(to right, #ff6b6b, #ff4081);
      border-radius: 16rpx;
      padding: 30rpx;
      display: flex;
      justify-content: space-between;
      color: #fff;
      margin: 0 30rpx;

      .income-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;

        .income-value {
          font-size: 40rpx;
          font-weight: 500;
          margin-bottom: 10rpx;
        }

        .income-label {
          font-size: 24rpx;
          opacity: 0.9;
          display: flex;
          align-items: center;
        }
      }
    }

    .balance-section {
      background-color: #fff;
      padding: 30rpx;
      display: flex;
      justify-content: space-between;
      margin: 20rpx 30rpx 0;
      border-radius: 16rpx;

      .balance-info {
        flex: 1;
        display: flex;
        flex-wrap: wrap;

        .balance-item {
          width: 50%;
          display: flex;
          flex-direction: column;
          margin-bottom: 10rpx;

          .balance-value {
            font-size: 36rpx;
            font-weight: 500;
            color: #333;
            margin-bottom: 10rpx;
          }

          .balance-label {
            font-size: 24rpx;
            color: #666;
            display: flex;
            align-items: center;
          }
        }
      }

      .action-buttons {
        display: flex;
        flex-direction: column;
        justify-content: center;

        .action-button {
          padding: 10rpx 30rpx;
          border-radius: 50rpx;
          font-size: 24rpx;
          margin-bottom: 10rpx;
          width: 120rpx;
          text-align: center;

          &.withdraw {
            background-color: #ff4081;
            color: #fff;
          }

          &.detail {
            background-color: #f5f5f5;
            color: #666;
          }
        }
      }
    }

    .stat-section {
      margin: 20rpx 30rpx 0;
      background-color: #fff;
      padding: 30rpx;
      border-radius: 16rpx;

      .stat-header {
        display: flex;
        align-items: center;
        margin-bottom: 30rpx;
        position: relative;

        .stat-title {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
          margin-right: 10rpx;
        }

        .arrow-icon {
          position: absolute;
          right: 0;
        }
      }

      .stat-content {
        display: flex;
        justify-content: space-between;

        .stat-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          .stat-number {
            font-size: 36rpx;
            font-weight: 500;
            color: #333;
            margin-bottom: 10rpx;
          }

          .stat-label {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }

    .channel-section {
      margin: 20rpx 30rpx 0;
      background-color: #fff;
      padding: 30rpx;
      border-radius: 16rpx;

      .channel-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 30rpx;
        display: block;
      }

      .channel-grid {
        display: flex;
        flex-wrap: wrap;

        .channel-item {
          width: 25%;
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-bottom: 30rpx;

          .channel-icon {
            width: 80rpx;
            height: 80rpx;
            border-radius: 16rpx;
            background-color: #f8f8f8;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 12rpx;
          }

          .channel-name {
            font-size: 24rpx;
            color: #333;
          }
        }
      }
    }
  }
</style>