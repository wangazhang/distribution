<template>
  <s-layout title="消息详情">
  <view class="page-container">
<!--    &lt;!&ndash; 自定义顶部导航栏 &ndash;&gt;-->
<!--    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
<!--      <view class="navbar-content">-->
<!--        <view class="navbar-left">-->
<!--          <view class="navbar-back" @tap="goBack">-->
<!--            <u-icon name="arrow-left" size="36" color="#333"></u-icon>-->
<!--          </view>-->
<!--          <view class="navbar-home" @tap="navigateToHome">-->
<!--            <u-icon name="home" size="36" color="#333"></u-icon>-->
<!--          </view>-->
<!--        </view>-->
<!--        <view class="navbar-title">{{ getTypeTitle() }}</view>-->
<!--        <view class="navbar-right"></view>-->
<!--      </view>-->
<!--    </view>-->
    
    <!-- 消息列表 -->
    <view class="message-list" :style="{ paddingTop: (44 + statusBarHeight) + 'px' }">
      <template v-if="messageList.length > 0">
        <view v-for="(item, index) in messageList" :key="index" class="message-card">
          <view class="message-header">
            <view class="message-title">{{ item.title }}</view>
            <view class="message-time">{{ item.time }}</view>
          </view>
          <view class="message-content">{{ item.content }}</view>
        </view>
      </template>
      
      <!-- 空状态 -->
      <view v-else class="empty-state">
        <image class="empty-image" src="https://cdn.example.com/static/pic/empty/empty_message.png" mode="aspectFit"></image>
        <view class="empty-text">暂无{{ getTypeTitle() }}</view>
        <view class="refresh-button" @tap="refreshData">
          <text>刷新</text>
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

// 页面参数
const messageType = ref('');
const messageList = ref([]);

// 根据消息类型获取标题
const getTypeTitle = () => {
  switch (messageType.value) {
    case 'trade':
      return '交易通知';
    case 'activity':
      return '活动通知';
    case 'interaction':
      return '互动消息';
    case 'system':
      return '系统消息';
    case 'service':
      return '客服消息';
    case 'notification':
      return '服务通知';
    default:
      return '消息详情';
  }
};

// 获取消息数据
const fetchMessageData = () => {
  // 模拟API请求，实际项目中应该改为真实的接口调用
  setTimeout(() => {
    // 暂时返回空数据，假设没有消息
    messageList.value = [];
  }, 500);
};

// 刷新数据
const refreshData = () => {
  uni.showLoading({
    title: '加载中'
  });
  
  fetchMessageData();
  
  setTimeout(() => {
    uni.hideLoading();
  }, 500);
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 跳转到首页
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
  
  // 获取页面传参
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const options = currentPage.$page?.options;
  
  if (options && options.type) {
    messageType.value = options.type;
  }
  
  // 获取消息数据
  fetchMessageData();
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
      
      .navbar-back, .navbar-home {
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
  padding: 30rpx;
  
  .message-card {
    margin-bottom: 20rpx;
    padding: 30rpx;
    background-color: #fff;
    border-radius: 12rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
      
      .message-title {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
      
      .message-time {
        font-size: 24rpx;
        color: #999;
      }
    }
    
    .message-content {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
  
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
    
    .empty-image {
      width: 200rpx;
      height: 200rpx;
      margin-bottom: 30rpx;
    }
    
    .empty-text {
      font-size: 28rpx;
      color: #999;
      margin-bottom: 40rpx;
    }
    
    .refresh-button {
      padding: 16rpx 60rpx;
      background-color: #FFA0B4;
      color: #fff;
      font-size: 28rpx;
      border-radius: 30rpx;
    }
  }
}
</style> 