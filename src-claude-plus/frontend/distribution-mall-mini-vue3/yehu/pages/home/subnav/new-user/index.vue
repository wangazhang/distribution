<template>
  <s-layout navbar="normal">
    <view class="new-user-container">
      <!-- 自定义导航栏 -->
<!--      <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
<!--        <view class="navbar-content">-->
<!--          <view class="navbar-left" @tap="goBack">-->
<!--            <uni-icons type="back" size="24" color="#333"></uni-icons>-->
<!--          </view>-->
<!--          <view class="navbar-title">新人专区</view>-->
<!--          <view class="navbar-right">-->
<!--            <uni-icons type="home" size="24" color="#333" @tap="goHome"></uni-icons>-->
<!--          </view>-->
<!--        </view>-->
<!--      </view>-->

      <!-- 页面内容 -->
      <view class="page-content">
        <!-- 用户信息区 -->
        <view class="user-info-section">
          <image class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
          <view class="welcome-info">
            <text class="welcome-text">{{ isNewUser ? '欢迎加入' : '欢迎回来' }}</text>
            <text class="user-name">{{ userInfo.nickname }}</text>
          </view>
        </view>

        <!-- 新人福利展示 -->
        <view class="benefits-section">
          <view class="section-title">新人专享福利</view>
          <view class="benefits-list">
            <view class="benefit-item" v-for="(item, index) in benefitsList" :key="index" @tap="claimBenefit(item)">
              <image :src="item.image" mode="aspectFill" class="benefit-image"></image>
              <view class="benefit-info">
                <text class="benefit-name">{{ item.name }}</text>
                <text class="benefit-desc">{{ item.description }}</text>
                <view :class="['claim-btn', {'claimed': item.claimed}]">{{ item.claimed ? '已领取' : '立即领取' }}</view>
              </view>
            </view>
          </view>
        </view>

        <!-- 新人专享商品 -->
        <view class="new-products-section">
          <view class="section-title">新人专享好物</view>
          <view class="products-grid">
            <view class="product-item" v-for="(item, index) in newProductsList" :key="index" @tap="goToProductDetail(item)">
              <image :src="item.image" mode="aspectFill" class="product-image"></image>
              <text class="product-name">{{ item.name }}</text>
              <view class="product-price-row">
                <text class="product-price">¥{{ item.price }}</text>
                <text class="product-original-price">¥{{ item.originalPrice }}</text>
              </view>
              <view class="product-tag">{{ item.tag }}</view>
            </view>
          </view>
        </view>

        <!-- 邀请好友 -->
        <view class="invite-section">
          <view class="invite-content">
            <view class="invite-left">
              <text class="invite-title">邀请好友共享福利</text>
              <text class="invite-desc">邀请好友注册，双方各得50积分</text>
            </view>
            <view class="invite-btn" @tap="shareInvitation">立即邀请</view>
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

// 用户信息
const userInfo = ref({
  avatar: 'https://cdn.example.com/static/pic/banner/index/1.png',
  nickname: '匿名用户',
  userId: null
});

// 是否是新用户
const isNewUser = ref(true);

// 新人福利列表
const benefitsList = ref([
  {
    id: 1,
    name: '新人礼包',
    description: '价值100元好礼，一键领取',
    image: 'https://cdn.example.com/static/pic/banner/index/1.png',
    claimed: false
  },
  {
    id: 2,
    name: '首单减50',
    description: '新人首单满199减50元',
    image: 'https://cdn.example.com/static/pic/banner/index/2.png',
    claimed: false
  },
  {
    id: 3,
    name: '新人积分',
    description: '注册即送100积分',
    image: 'https://cdn.example.com/static/pic/banner/index/3.png',
    claimed: true
  }
]);

// 新人专享商品
const newProductsList = ref([
  {
    id: 1,
    name: 'MEETHER 胶原蛋白套装',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-set.png',
    price: '1199.00',
    originalPrice: '2400.00',
    tag: '新人价'
  },
  {
    id: 2,
    name: 'MEETHER 胶原精华液',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-single.png',
    price: '399.00',
    originalPrice: '580.00',
    tag: '新人价'
  },
  {
    id: 3,
    name: 'MEETHER 面膜套装',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-mask.png',
    price: '99.00',
    originalPrice: '128.00',
    tag: '首单减'
  },
  {
    id: 4,
    name: 'MEETHER 精华液试用装',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-mini.png',
    price: '9.90',
    originalPrice: '18.00',
    tag: '新人价'
  }
]);

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 返回首页
const goHome = () => {
  uni.switchTab({
    url: '/yehu/pages/home/index'
  });
};

// 领取福利
const claimBenefit = (benefit) => {
  if (benefit.claimed) {
    uni.showToast({
      title: '福利已领取',
      icon: 'none'
    });
    return;
  }

  uni.showLoading({
    title: '领取中...'
  });

  // 模拟领取请求
  setTimeout(() => {
    uni.hideLoading();
    // 更新福利状态
    const index = benefitsList.value.findIndex(item => item.id === benefit.id);
    if (index !== -1) {
      benefitsList.value[index].claimed = true;
    }

    uni.showToast({
      title: '领取成功',
      icon: 'success'
    });
  }, 1000);
};

// 跳转到商品详情页
const goToProductDetail = (product) => {
  uni.navigateTo({
    url: `/yehu/pages/mall/product-detail/index?id=${product.id}`
  });
};

// 分享邀请
const shareInvitation = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
    success: () => {
      console.log('打开分享菜单成功');
    },
    fail: (err) => {
      console.log('打开分享菜单失败', err);
      uni.showToast({
        title: '分享功能暂不可用',
        icon: 'none'
      });
    }
  });
};

// 获取用户信息
const getUserInfo = () => {
  // 模拟获取用户信息
  // 实际项目中应该从本地存储或服务器获取
  setTimeout(() => {
    const savedUserInfo = uni.getStorageSync('userInfo');
    if (savedUserInfo) {
      userInfo.value = JSON.parse(savedUserInfo);
      isNewUser.value = false;
    } else {
      // 没有用户信息，视为新用户
      isNewUser.value = true;
    }
  }, 500);
};

onMounted(() => {
  // 获取状态栏高度
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0;
    }
  });

  // 获取用户信息
  getUserInfo();
});
</script>

<style lang="scss" scoped>
.new-user-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
  
  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 15px;
    
    .navbar-left, .navbar-right {
      width: 60px;
      display: flex;
      align-items: center;
    }
    
    .navbar-title {
      flex: 1;
      text-align: center;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }
  }
}

.page-content {
  .user-info-section {
    background-color: #FF5777;
    padding: 30rpx;
    display: flex;
    align-items: center;
    
    .avatar {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      border: 4rpx solid #fff;
      margin-right: 20rpx;
    }
    
    .welcome-info {
      color: #fff;
      
      .welcome-text {
        font-size: 28rpx;
        display: block;
        margin-bottom: 10rpx;
      }
      
      .user-name {
        font-size: 36rpx;
        font-weight: bold;
        display: block;
      }
    }
  }
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    padding: 30rpx;
    padding-bottom: 20rpx;
  }
  
  .benefits-section {
    background-color: #fff;
    margin-bottom: 20rpx;
    
    .benefits-list {
      padding: 0 30rpx 30rpx;
      
      .benefit-item {
        display: flex;
        margin-bottom: 20rpx;
        padding: 20rpx;
        background-color: #FFF9FA;
        border-radius: 12rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .benefit-image {
          width: 120rpx;
          height: 120rpx;
          border-radius: 12rpx;
          margin-right: 20rpx;
        }
        
        .benefit-info {
          flex: 1;
          position: relative;
          
          .benefit-name {
            font-size: 28rpx;
            font-weight: bold;
            color: #333;
            display: block;
            margin-bottom: 10rpx;
          }
          
          .benefit-desc {
            font-size: 24rpx;
            color: #666;
            display: block;
            margin-bottom: 20rpx;
          }
          
          .claim-btn {
            position: absolute;
            right: 0;
            bottom: 0;
            background-color: #FF5777;
            color: #fff;
            padding: 8rpx 20rpx;
            border-radius: 30rpx;
            font-size: 24rpx;
            
            &.claimed {
              background-color: #ccc;
            }
          }
        }
      }
    }
  }
  
  .new-products-section {
    background-color: #fff;
    margin-bottom: 20rpx;
    padding-bottom: 30rpx;
    
    .products-grid {
      display: flex;
      flex-wrap: wrap;
      padding: 0 20rpx;
      
      .product-item {
        width: 48%;
        margin: 0 1% 20rpx;
        position: relative;
        
        .product-image {
          width: 100%;
          height: 300rpx;
          border-radius: 12rpx;
          margin-bottom: 15rpx;
        }
        
        .product-name {
          font-size: 26rpx;
          color: #333;
          display: block;
          margin-bottom: 10rpx;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .product-price-row {
          display: flex;
          align-items: center;
          
          .product-price {
            font-size: 32rpx;
            color: #FF5777;
            font-weight: bold;
            margin-right: 10rpx;
          }
          
          .product-original-price {
            font-size: 24rpx;
            color: #999;
            text-decoration: line-through;
          }
        }
        
        .product-tag {
          position: absolute;
          top: 10rpx;
          right: 10rpx;
          background-color: #FF5777;
          color: #fff;
          font-size: 22rpx;
          padding: 6rpx 12rpx;
          border-radius: 6rpx;
        }
      }
    }
  }
  
  .invite-section {
    background-color: #fff;
    padding: 30rpx;
    
    .invite-content {
      background-color: #FFF9FA;
      border-radius: 12rpx;
      padding: 30rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .invite-left {
        .invite-title {
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
          display: block;
          margin-bottom: 10rpx;
        }
        
        .invite-desc {
          font-size: 24rpx;
          color: #666;
          display: block;
        }
      }
      
      .invite-btn {
        background-color: #FF5777;
        color: #fff;
        padding: 15rpx 30rpx;
        border-radius: 30rpx;
        font-size: 26rpx;
      }
    }
  }
}
</style> 