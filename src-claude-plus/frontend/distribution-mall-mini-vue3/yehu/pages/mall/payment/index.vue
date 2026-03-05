<template>
  <view class="checkout-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="确认订单"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 收货地址 -->
    <view class="address-card" @tap="selectAddress">
      <view class="address-info" v-if="address.name">
        <view class="address-header">
          <text class="address-name">{{ address.name }}</text>
          <text class="address-phone">{{ address.phone }}</text>
        </view>
        <view class="address-detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</view>
      </view>
      <view class="address-empty" v-else>
        <text>选择收货地址</text>
      </view>
      <view class="address-arrow">
        <u-icon name="arrow-right" size="28" color="#999"></u-icon>
      </view>
    </view>
    
    <!-- 商品列表 -->
    <view class="order-card">
      <view class="card-title">商品信息</view>
      <view class="order-items">
        <view class="order-item" v-for="(item, index) in orderItems" :key="index">
          <view class="item-image">
            <image :src="item.productImage" mode="aspectFill"></image>
          </view>
          <view class="item-info">
            <view class="item-title">{{ item.productName }}</view>
            <view class="item-spec">{{ item.specName }}</view>
          </view>
          <view class="item-price-info">
            <view class="item-price">¥{{ item.price }}</view>
            <view class="item-count">x{{ item.quantity }}</view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 支付方式 -->
    <view class="payment-card">
      <view class="card-title">支付方式</view>
      <view class="payment-methods">
        <view 
          class="payment-method" 
          v-for="(method, index) in paymentMethods" 
          :key="index"
          :class="{ active: selectedPaymentMethod === method.value }"
          @tap="selectPaymentMethod(method.value)"
        >
          <view class="method-icon">
            <u-icon :name="method.icon" size="40" :color="selectedPaymentMethod === method.value ? '#c82436' : '#666'"></u-icon>
          </view>
          <view class="method-name">{{ method.name }}</view>
          <view class="method-check" v-if="selectedPaymentMethod === method.value">
            <u-icon name="checkmark-circle" size="36" color="#FFA0B4"></u-icon>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 订单摘要 -->
    <view class="summary-card">
      <view class="summary-item">
        <text class="summary-label">商品金额</text>
        <text class="summary-value">¥{{ orderTotal }}</text>
      </view>
      <view class="summary-item">
        <text class="summary-label">运费</text>
        <text class="summary-value">¥{{ shipping }}</text>
      </view>
      <view class="summary-divider"></view>
      <view class="summary-total">
        <text class="total-label">实付款</text>
        <view class="total-amount">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ totalAmount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 订单备注 -->
    <view class="remark-card">
      <view class="remark-title">订单备注</view>
      <view class="remark-input">
        <u-input
          v-model="remark"
          placeholder="选填，填写您对商品的特殊需求"
          border="none"
          :clearable="true"
        ></u-input>
      </view>
    </view>
    
    <!-- 底部结算栏 -->
    <view class="checkout-footer">
      <view class="total-section">
        <text>合计:</text>
        <view class="total-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ totalAmount }}</text>
        </view>
      </view>
      <view class="submit-btn" @tap="submitOrder">提交订单</view>
    </view>
  </view>
</template>

<script setup >
import { ref, reactive, computed, onMounted } from 'vue';

// 订单商品
const orderItems = ref([]);

// 收货地址
const address = reactive({
  id: '',
  name: '张三',
  phone: '138****1234',
  province: '上海市',
  city: '上海市',
  district: '浦东新区',
  detail: '张江高科技园区博云路2号'
});

// 支付方式
const paymentMethods = [
  { name: '微信支付', value: 'wxpay', icon: 'weixin-circle-fill' },
  { name: '支付宝支付', value: 'alipay', icon: 'zhifubao-circle-fill' }
];
const selectedPaymentMethod = ref('wxpay');

// 订单备注
const remark = ref('');

// 商品总金额
const orderTotal = computed(() => {
  let total = 0;
  orderItems.value.forEach(item => {
    total += parseFloat(item.price) * item.quantity;
  });
  return total.toFixed(2);
});

// 运费
const shipping = ref('0.00');

// 实付金额
const totalAmount = computed(() => {
  return (parseFloat(orderTotal.value) + parseFloat(shipping.value)).toFixed(2);
});

// 选择收货地址
const selectAddress = () => {
  uni.navigateTo({
    url: '/pages/user/address/index'
  });
};

// 选择支付方式
const selectPaymentMethod = (method) => {
  selectedPaymentMethod.value = method;
};

// 提交订单
const submitOrder = () => {
  if (!address.name) {
    uni.showToast({
      title: '请选择收货地址',
      icon: 'none'
    });
    return;
  }
  
  uni.showLoading({
    title: '提交中'
  });
  
  // 模拟提交订单
  setTimeout(() => {
    uni.hideLoading();
    
    // 构建订单信息
    const orderInfo = {
      orderId: generateOrderId(),
      items: orderItems.value,
      address: address,
      payment: selectedPaymentMethod.value,
      total: totalAmount.value,
      remark: remark.value,
      createTime: new Date().toISOString()
    };
    
    // 跳转到支付页面或订单成功页面
    uni.navigateTo({
      url: `/pages/mall/order-success/index?orderId=${orderInfo.orderId}&total=${orderInfo.total}`
    });
  }, 1500);
};

// 生成订单ID
const generateOrderId = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hour = String(now.getHours()).padStart(2, '0');
  const minute = String(now.getMinutes()).padStart(2, '0');
  const second = String(now.getSeconds()).padStart(2, '0');
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
  
  return `${year}${month}${day}${hour}${minute}${second}${random}`;
};

// 接收页面参数
const onLoad = (options) => {
  console.log('结算页面参数:', options);
  
  // 解析订单信息
  try {
    if (options.cartItems) {
      // 来自购物车
      const cartItems = JSON.parse(decodeURIComponent(options.cartItems));
      orderItems.value = cartItems.map(item => ({
        productId: item.id,
        productName: item.title,
        productImage: item.image,
        specId: '',
        specName: item.spec,
        price: item.price,
        quantity: item.quantity
      }));
    } else if (options.orderInfo) {
      // 来自立即购买
      orderItems.value = JSON.parse(decodeURIComponent(options.orderInfo));
    }
  } catch (e) {
    console.error('解析订单信息错误', e);
  }
};

onMounted(() => {
  // 页面挂载后的初始化操作
});
</script>

<style lang="scss" scoped>
.checkout-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.address-card, .order-card, .payment-card, .summary-card, .remark-card {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin: 20rpx;
  padding: 30rpx;
}

.address-card {
  display: flex;
  align-items: center;
  
  .address-info {
    flex: 1;
    
    .address-header {
      margin-bottom: 10rpx;
      
      .address-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333333;
        margin-right: 20rpx;
      }
      
      .address-phone {
        font-size: 28rpx;
        color: #666666;
      }
    }
    
    .address-detail {
      font-size: 28rpx;
      color: #666666;
      line-height: 1.5;
    }
  }
  
  .address-empty {
    flex: 1;
    font-size: 28rpx;
    color: #999999;
  }
  
  .address-arrow {
    padding-left: 20rpx;
  }
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 20rpx;
}

.order-items {
  .order-item {
    display: flex;
    margin-bottom: 20rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .item-image {
      width: 120rpx;
      height: 120rpx;
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
      justify-content: space-between;
      
      .item-title {
        font-size: 28rpx;
        color: #333333;
        margin-bottom: 10rpx;
      }
      
      .item-spec {
        font-size: 24rpx;
        color: #999999;
      }
    }
    
    .item-price-info {
      text-align: right;
      
      .item-price {
        font-size: 28rpx;
        color: #333333;
        font-weight: 500;
        margin-bottom: 10rpx;
      }
      
      .item-count {
        font-size: 24rpx;
        color: #999999;
      }
    }
  }
}

.payment-methods {
  .payment-method {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .method-icon {
      margin-right: 20rpx;
    }
    
    .method-name {
      flex: 1;
      font-size: 28rpx;
      color: #333333;
    }
    
    &.active {
      .method-name {
        color: #c82436;
        font-weight: 500;
      }
    }
  }
}

.summary-card {
  .summary-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .summary-label {
      font-size: 28rpx;
      color: #666666;
    }
    
    .summary-value {
      font-size: 28rpx;
      color: #333333;
    }
  }
  
  .summary-divider {
    height: 1rpx;
    background-color: #f0f0f0;
    margin: 20rpx 0;
  }
  
  .summary-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .total-label {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
    }
    
    .total-amount {
      display: flex;
      align-items: baseline;
      
      .price-symbol {
        font-size: 28rpx;
        color: #FFA0B4;
        font-weight: 600;
      }
      
      .price-value {
        font-size: 40rpx;
        color: #FFA0B4;
        font-weight: 600;
      }
    }
  }
}

.remark-card {
  .remark-title {
    font-size: 28rpx;
    color: #333333;
    font-weight: 500;
    margin-bottom: 20rpx;
  }
  
  .remark-input {
    border: 1rpx solid #f0f0f0;
    border-radius: 8rpx;
    padding: 0 20rpx;
  }
}

.checkout-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100rpx;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 99;
  
  .total-section {
    flex: 1;
    display: flex;
    align-items: baseline;
    
    text {
      font-size: 28rpx;
      color: #333333;
      margin-right: 10rpx;
    }
    
    .total-price {
      display: flex;
      align-items: baseline;
      
      .price-symbol {
        font-size: 28rpx;
        color: #FFA0B4;
        font-weight: 600;
      }
      
      .price-value {
        font-size: 40rpx;
        color: #FFA0B4;
        font-weight: 600;
      }
    }
  }
  
  .submit-btn {
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
</style> 