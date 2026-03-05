<template>
  <view class="cart-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="购物车"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 购物车列表 -->
    <view class="cart-list" v-if="cartList.length > 0">
      <view class="cart-item" v-for="(item, index) in cartList" :key="index">
        <view class="item-select">
          <u-checkbox 
            v-model="item.selected" 
            shape="circle" 
            active-color="#FFA0B4"
            @change="updateSelection"
          ></u-checkbox>
        </view>
        <view class="item-image">
          <image :src="item.image" mode="aspectFill"></image>
        </view>
        <view class="item-info">
          <view class="item-title">{{ item.title }}</view>
          <view class="item-spec">{{ item.spec }}</view>
          <view class="item-bottom">
            <view class="item-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ item.price }}</text>
            </view>
            <view class="item-quantity">
              <u-number-box
                v-model="item.quantity"
                :min="1"
                :max="99"
                :step="1"
                :input-width="80"
                @change="updateSubtotal(index)"
              ></u-number-box>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 空购物车 -->
    <view class="empty-cart" v-else>
      <u-empty text="购物车还是空的" mode="cart"></u-empty>
      <view class="go-shopping-btn" @tap="goShopping">去购物</view>
    </view>
    
    <!-- 底部结算栏 -->
    <view class="cart-footer" v-if="cartList.length > 0">
      <view class="footer-left">
        <view class="all-select">
          <u-checkbox 
            v-model="isAllSelected" 
            shape="circle" 
            active-color="#FFA0B4"
            @change="selectAll"
          ></u-checkbox>
          <text>全选</text>
        </view>
      </view>
      <view class="footer-right">
        <view class="total-section">
          <text class="total-label">合计:</text>
          <view class="total-price">
            <text class="price-symbol">¥</text>
            <text class="price-value">{{ totalPrice }}</text>
          </view>
        </view>
        <view class="checkout-btn" @tap="goToCheckout">
          结算({{ selectedCount }})
        </view>
      </view>
    </view>
  </view>
</template>

<script setup >
import { ref, reactive, computed, onMounted } from 'vue';

// 购物车列表
const cartList = ref([
  {
    id: '1',
    title: 'MEETHER 居家胶原美塑',
    spec: '1盒5支装',
    price: '2400.00',
    originalPrice: '9800',
    quantity: 1,
    image: 'https://cdn.example.com/static/pic/product-detail1.jpg',
    selected: true
  },
  {
    id: '2',
    title: 'MEETHER 精华液',
    spec: '50ml',
    price: '599.00',
    originalPrice: '999',
    quantity: 2,
    image: 'https://cdn.example.com/static/pic/product-detail2.jpg',
    selected: true
  }
]);

// 计算属性：是否全选
const isAllSelected = computed({
  get: () => {
    return cartList.value.length > 0 && cartList.value.every(item => item.selected);
  },
  set: (value) => {
    cartList.value.forEach(item => {
      item.selected = value;
    });
  }
});

// 计算属性：选中商品数量
const selectedCount = computed(() => {
  return cartList.value.filter(item => item.selected).length;
});

// 计算属性：总价
const totalPrice = computed(() => {
  let total = 0;
  cartList.value.forEach(item => {
    if (item.selected) {
      total += parseFloat(item.price) * item.quantity;
    }
  });
  return total.toFixed(2);
});

// 选择商品
const updateSelection = () => {
  // 更新选择状态的逻辑
};

// 更新小计
const updateSubtotal = (index) => {
  // 更新数量后的小计逻辑
};

// 全选/取消全选
const selectAll = (value) => {
  cartList.value.forEach(item => {
    item.selected = value;
  });
};

// 去购物
const goShopping = () => {
  uni.switchTab({
    url: '/pages/mall/index'
  });
};

// 去结算
const goToCheckout = () => {
  const selectedItems = cartList.value.filter(item => item.selected);
  
  if (selectedItems.length === 0) {
    uni.showToast({
      title: '请选择要结算的商品',
      icon: 'none'
    });
    return;
  }
  
  // 跳转到结算页面
  uni.navigateTo({
    url: `/pages/mall/checkout/index?cartItems=${encodeURIComponent(JSON.stringify(selectedItems))}`
  });
};

onMounted(() => {
  // 获取购物车数据
  // 实际应用中可能是API请求获取用户购物车数据
});
</script>

<style lang="scss" scoped>
.cart-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 100rpx;
}

.cart-list {
  padding: 20rpx;
}

.cart-item {
  display: flex;
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  padding: 20rpx;
  
  .item-select {
    display: flex;
    align-items: center;
    margin-right: 20rpx;
  }
  
  .item-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 8rpx;
    overflow: hidden;
    margin-right: 20rpx;
    
    image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .item-title {
      font-size: 28rpx;
      color: #333333;
      margin-bottom: 10rpx;
      font-weight: 500;
    }
    
    .item-spec {
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 20rpx;
    }
    
    .item-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .item-price {
        display: flex;
        align-items: baseline;
        
        .price-symbol {
          font-size: 24rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
        
        .price-value {
          font-size: 32rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
      }
    }
  }
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  
  .go-shopping-btn {
    margin-top: 30rpx;
    width: 200rpx;
    height: 80rpx;
    background-color: #FFA0B4;
    color: #ffffff;
    font-size: 28rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 40rpx;
  }
}

.cart-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  height: 100rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 99;
  
  .footer-left {
    display: flex;
    align-items: center;
    
    .all-select {
      display: flex;
      align-items: center;
      
      text {
        font-size: 28rpx;
        color: #333333;
        margin-left: 10rpx;
      }
    }
  }
  
  .footer-right {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    
    .total-section {
      display: flex;
      align-items: baseline;
      margin-right: 20rpx;
      
      .total-label {
        font-size: 28rpx;
        color: #333333;
        margin-right: 10rpx;
      }
      
      .total-price {
        display: flex;
        align-items: baseline;
        
        .price-symbol {
          font-size: 24rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
        
        .price-value {
          font-size: 32rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
      }
    }
    
    .checkout-btn {
      width: 200rpx;
      height: 70rpx;
      background-color: #FFA0B4;
      color: #ffffff;
      font-size: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 35rpx;
    }
  }
}
</style> 