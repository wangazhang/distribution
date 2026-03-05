<template>
  <s-layout navbar="normal">
    <view class="special-sale-container">
      <view class="page-header">
        <view class="header-title">限时特卖</view>
        <view class="header-subtitle">限时折扣，手慢无</view>
      </view>
      
      <view class="sale-items">
        <view class="empty-tip" v-if="saleItems.length === 0">
          暂无特卖商品，敬请期待
        </view>
        <view class="item-card" v-for="(item, index) in saleItems" :key="index">
          <image class="item-image" :src="item.image" mode="aspectFill"></image>
          <view class="item-info">
            <view class="item-name">{{item.name}}</view>
            <view class="item-price-box">
              <text class="sale-price">¥{{item.salePrice}}</text>
              <text class="original-price">¥{{item.originalPrice}}</text>
            </view>
            <view class="countdown-box">
              <text class="countdown-label">距结束:</text>
              <text class="countdown-time">{{item.countdown}}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script>
export default {
  data() {
    return {
      saleItems: [] // 将在onLoad中加载数据
    };
  },
  onLoad() {
    // 模拟数据，实际应从API获取
    this.saleItems = [
      {
        id: 1,
        name: 'MEETHER 胶原蛋白精华液',
        image: 'https://cdn.example.com/static/pic/MEETHER/ecm-single.png',
        salePrice: '399.00',
        originalPrice: '580.00',
        countdown: '23:45:30'
      },
      {
        id: 2,
        name: 'MEETHER 胶原蛋白修复面膜',
        image: 'https://cdn.example.com/static/pic/MEETHER/ecm-mask.png',
        salePrice: '99.00',
        originalPrice: '128.00',
        countdown: '12:30:15'
      }
    ];
  }
};
</script>

<style lang="scss">
.special-sale-container {
  padding: 30rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
  
  .page-header {
    margin-bottom: 30rpx;
    padding-top: 80rpx;
    
    .header-title {
      font-size: 40rpx;
      font-weight: bold;
      color: #333;
    }
    
    .header-subtitle {
      font-size: 28rpx;
      color: #666;
      margin-top: 10rpx;
    }
  }
  
  .sale-items {
    .empty-tip {
      text-align: center;
      color: #999;
      font-size: 28rpx;
      padding: 100rpx 0;
    }
    
    .item-card {
      background-color: #fff;
      border-radius: 16rpx;
      padding: 20rpx;
      margin-bottom: 20rpx;
      display: flex;
      
      .item-image {
        width: 200rpx;
        height: 200rpx;
        border-radius: 10rpx;
        margin-right: 20rpx;
      }
      
      .item-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .item-name {
          font-size: 30rpx;
          font-weight: bold;
          color: #333;
        }
        
        .item-price-box {
          margin-top: 10rpx;
          
          .sale-price {
            font-size: 34rpx;
            font-weight: bold;
            color: #ff4d4f;
            margin-right: 10rpx;
          }
          
          .original-price {
            font-size: 26rpx;
            color: #999;
            text-decoration: line-through;
          }
        }
        
        .countdown-box {
          margin-top: 10rpx;
          background-color: #fff8e6;
          padding: 6rpx 12rpx;
          border-radius: 6rpx;
          display: inline-flex;
          align-items: center;
          
          .countdown-label {
            font-size: 24rpx;
            color: #ff8f1f;
            margin-right: 10rpx;
          }
          
          .countdown-time {
            font-size: 24rpx;
            color: #ff6b35;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style> 