<template>
  <view class="success-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="下单成功"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 成功提示 -->
    <view class="success-section">
      <u-icon name="checkmark-circle-fill" size="120" color="#c82436"></u-icon>
      <view class="success-text">订单提交成功</view>
      <view class="success-tips">感谢您的信任，我们将尽快为您发货</view>
    </view>
    
    <!-- 订单信息 -->
    <view class="order-info-card">
      <view class="order-info-item">
        <text class="info-label">订单编号</text>
        <text class="info-value">{{ orderId }}</text>
      </view>
      <view class="order-info-item">
        <text class="info-label">支付金额</text>
        <view class="info-price">
          <text class="price-symbol">¥</text>
          <text>{{ orderTotal }}</text>
        </view>
      </view>
      <view class="order-info-item">
        <text class="info-label">下单时间</text>
        <text class="info-value">{{ orderTime }}</text>
      </view>
    </view>
    
    <!-- 订单操作 -->
    <view class="action-section">
      <view class="action-button primary" @tap="viewOrder">查看订单</view>
      <view class="action-button secondary" @tap="backToHome">返回首页</view>
    </view>
    
    <!-- 推荐商品 -->
    <view class="recommend-section">
      <view class="section-title">为您推荐</view>
      <view class="recommend-list">
        <view class="recommend-item" v-for="(item, index) in recommendList" :key="index" @tap="goToDetail(item.id)">
          <view class="item-image">
            <image :src="item.image" mode="aspectFill"></image>
          </view>
          <view class="item-title">{{ item.title }}</view>
          <view class="item-price">¥{{ item.price }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup >
import { ref, onMounted } from 'vue';

// 订单ID
const orderId = ref('');
// 订单金额
const orderTotal = ref('0.00');
// 下单时间
const orderTime = ref(formatDate(new Date()));

// 推荐商品
const recommendList = ref([
  {
    id: '1',
    title: 'MEETHER 精华液',
    price: '599.00',
    image: 'https://cdn.example.com/static/pic/product-detail2.jpg'
  },
  {
    id: '2',
    title: 'MEETHER 面膜套装',
    price: '299.00',
    image: 'https://cdn.example.com/static/pic/product-detail3.jpg'
  },
  {
    id: '3',
    title: 'MEETHER 护肤套装',
    price: '799.00',
    image: 'https://cdn.example.com/static/pic/product-detail1.jpg'
  }
]);

// 格式化日期
function formatDate(date) {
  if (!date) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

// 查看订单
const viewOrder = () => {
  uni.navigateTo({
    url: '/pages/mall/order/detail?id=' + orderId.value
  });
};

// 返回首页
const backToHome = () => {
  uni.switchTab({
    url: '/pages/mall/index'
  });
};

// 跳转到商品详情
const goToDetail = (id) => {
  uni.navigateTo({
    url: '/pages/mall/product-detail/index?id=' + id
  });
};

// 接收页面参数
const onLoad = (options) => {
  console.log('订单成功页面参数:', options);
  if (options.orderId) {
    orderId.value = options.orderId;
  }
  
  if (options.total) {
    orderTotal.value = options.total;
  }
};

onMounted(() => {
  // 页面挂载后的初始化操作
});
</script>

<style lang="scss" scoped>
.success-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.success-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
  background-color: #ffffff;
  
  .success-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #333333;
    margin: 30rpx 0 20rpx;
  }
  
  .success-tips {
    font-size: 28rpx;
    color: #999999;
  }
}

.order-info-card {
  margin: 20rpx;
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
  
  .order-info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .info-label {
      font-size: 28rpx;
      color: #666666;
    }
    
    .info-value {
      font-size: 28rpx;
      color: #333333;
    }
    
    .info-price {
      display: flex;
      align-items: baseline;
      font-size: 32rpx;
      font-weight: 600;
      color: #c82436;
      
      .price-symbol {
        font-size: 24rpx;
        margin-right: 4rpx;
      }
    }
  }
}

.action-section {
  margin: 40rpx 20rpx;
  display: flex;
  
  .action-button {
    flex: 1;
    height: 80rpx;
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    font-weight: 500;
    margin: 0 15rpx;
    
    &.primary {
      background-color: #c82436;
      color: #ffffff;
    }
    
    &.secondary {
      background-color: #ffffff;
      color: #666666;
      border: 1rpx solid #dcdcdc;
    }
  }
}

.recommend-section {
  margin: 0 20rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333333;
    margin-bottom: 20rpx;
    padding: 0 10rpx;
  }
  
  .recommend-list {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10rpx;
    
    .recommend-item {
      width: 33.33%;
      padding: 0 10rpx;
      margin-bottom: 20rpx;
      
      .item-image {
        width: 100%;
        height: 220rpx;
        border-radius: 8rpx;
        overflow: hidden;
        margin-bottom: 10rpx;
        
        image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .item-title {
        font-size: 26rpx;
        color: #333333;
        margin-bottom: 6rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .item-price {
        font-size: 28rpx;
        color: #c82436;
        font-weight: 500;
      }
    }
  }
}
</style> 