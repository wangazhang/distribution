<template>
  <view class="product-detail-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="商品详情"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 商品轮播图 -->
    <view class="swiper-section">
      <u-swiper
        :list="productImages"
        height="750rpx"
        :autoplay="true"
        indicator
        indicatorMode="dot"
      ></u-swiper>
    </view>
    
    <!-- 商品基本信息 -->
    <view class="product-info-card">
      <view class="product-title">{{ productInfo.title }}</view>
      <view class="product-desc">{{ productInfo.desc }}</view>
      <view class="product-price-row">
        <view class="price-section">
          <text class="price-symbol">¥</text>
          <text class="product-price">{{ productInfo.price }}</text>
          <text class="product-original-price" v-if="productInfo.originalPrice">¥{{ productInfo.originalPrice }}</text>
        </view>
        <view class="sales-count">已售 {{ productInfo.sales }}</view>
      </view>
    </view>
    
    <!-- 选择规格 -->
    <view class="option-card" @tap="openSpecsPopup">
      <view class="option-title">选择</view>
      <view class="option-value">
        <text>{{ selectedSpecs || "请选择规格" }}</text>
        <u-icon name="arrow-right" size="28" color="#999"></u-icon>
      </view>
    </view>
    
    <!-- 服务承诺 -->
    <view class="service-card">
      <view class="service-title">服务</view>
      <view class="service-tags">
        <view class="service-tag">
          <u-icon name="checkmark-circle" size="30" color="#666"></u-icon>
          <text>正品保证</text>
        </view>
        <view class="service-tag">
          <u-icon name="checkmark-circle" size="30" color="#666"></u-icon>
          <text>7天无理由退货</text>
        </view>
        <view class="service-tag">
          <u-icon name="checkmark-circle" size="30" color="#666"></u-icon>
          <text>48小时发货</text>
        </view>
      </view>
    </view>
    
    <!-- 商品详情 -->
    <view class="detail-section">
      <view class="detail-header">
        <text class="detail-title">商品详情</text>
      </view>
      <view class="detail-content">
        <rich-text :nodes="productInfo.detailHtml"></rich-text>
        <image 
          v-for="(img, index) in productInfo.detailImages" 
          :key="index" 
          :src="img" 
          mode="widthFix" 
          class="detail-image"
        ></image>
      </view>
    </view>
    
    <!-- 底部操作栏 -->
    <view class="bottom-action-bar">
      <view class="action-icons">
        <view class="action-icon-item" @tap="navigateToHome">
          <u-icon name="home" size="36" color="#666"></u-icon>
          <text>首页</text>
        </view>
        <view class="action-icon-item" @tap="navigateToCart">
          <u-icon name="shopping-cart" size="40" color="#666"></u-icon>
          <text>购物车</text>
          <view class="cart-badge" v-if="cartCount > 0">{{ cartCount }}</view>
        </view>
      </view>
      <view class="action-buttons">
        <view class="add-cart-btn" @tap="openSpecsPopup('cart')">加入购物车</view>
        <view class="buy-now-btn" @tap="openSpecsPopup('buy')">立即购买</view>
      </view>
    </view>
    
    <!-- 规格选择弹窗 -->
    <u-popup
      v-model="specsPopupVisible"
      mode="bottom"
      border-radius="24"
      closeable
    >
      <view class="specs-popup-container">
        <view class="specs-product-info">
          <image :src="productInfo.image" mode="aspectFill" class="specs-product-image"></image>
          <view class="specs-product-details">
            <view class="specs-product-price">
              <text class="price-symbol">¥</text>
              <text class="product-price">{{ productInfo.price }}</text>
            </view>
            <view class="specs-selected">
              已选：{{ selectedSpecs || "请选择规格" }}
            </view>
          </view>
        </view>
        
        <view class="specs-options">
          <view class="specs-title">规格</view>
          <view class="specs-values">
            <view 
              v-for="(spec, index) in productInfo.specs" 
              :key="index"
              class="spec-item"
              :class="{ 'active': selectedSpecIndex === index }"
              @tap="selectSpec(index)"
            >
              {{ spec.name }}
            </view>
          </view>
        </view>
        
        <view class="quantity-selector">
          <view class="quantity-title">数量</view>
          <u-number-box
            v-model="quantity"
            :min="1"
            :max="99"
            :step="1"
            :input-width="120
"
          ></u-number-box>
        </view>
        
        <view class="specs-buttons">
          <view 
            class="specs-add-cart-btn" 
            v-if="actionType === 'cart'"
            @tap="addToCart"
          >加入购物车</view>
          <view 
            class="specs-buy-now-btn"
            v-if="actionType === 'buy'"
            @tap="buyNow"
          >立即购买</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup >
import { ref, reactive, computed, onMounted } from 'vue';

// 商品ID
const productId = ref('');

// 接收页面参数
const onLoad = (options) => {
  console.log('页面参数:', options);
  if (options && options.id) {
    productId.value = options.id;
    // 根据商品ID加载商品详情
    // loadProductDetail(productId.value);
  }
};

// 商品图片轮播
const productImages = ref([
  'https://cdn.example.com/static/pic/product-detail1.jpg',
  'https://cdn.example.com/static/pic/product-detail2.jpg',
  'https://cdn.example.com/static/pic/product-detail3.jpg',
]);

// 商品信息
const productInfo = reactive({
  id: '123456',
  title: 'MEETHER 居家胶原美塑',
  desc: '唯一重组人胶原二类碱，唯一无菌技术加工',
  price: '2400.00',
  originalPrice: '9800',
  sales: 1208,
  image: 'https://cdn.example.com/static/pic/product-detail1.jpg',
  specs: [
    { id: '1', name: '1盒5支装', price: '2400.00' },
    { id: '2', name: '2盒10支装', price: '4500.00' },
    { id: '3', name: '3盒15支装', price: '6500.00' }
  ],
  detailHtml: '<p style="font-size: 28rpx; color: #333; line-height: 1.8;">MEETHER胶原蛋白是一种高效美容产品，富含优质胶原蛋白，能有效促进皮肤修复，增强皮肤弹性，减少皱纹，令肌肤焕发年轻光彩。</p>',
  detailImages: [
    'https://cdn.example.com/static/pic/detail1.jpg',
    'https://cdn.example.com/static/pic/detail2.jpg',
    'https://cdn.example.com/static/pic/detail3.jpg'
  ]
});

// 购物车商品数量
const cartCount = ref(2);

// 规格选择相关
const specsPopupVisible = ref(false);
const selectedSpecIndex = ref(-1);
const selectedSpecs = computed(() => {
  if (selectedSpecIndex.value >= 0 && selectedSpecIndex.value < productInfo.specs.length) {
    return productInfo.specs[selectedSpecIndex.value].name;
  }
  return '';
});
const quantity = ref(1);
const actionType = ref('');

// 打开规格选择弹窗
const openSpecsPopup = (type = 'select') => {
  actionType.value = type;
  specsPopupVisible.value = true;
};

// 选择规格
const selectSpec = (index) => {
  selectedSpecIndex.value = index;
};

// 加入购物车
const addToCart = () => {
  if (selectedSpecIndex.value < 0) {
    uni.showToast({
      title: '请选择规格',
      icon: 'none'
    });
    return;
  }
  
  uni.showLoading({ title: '加载中' });
  
  // 模拟添加购物车请求
  setTimeout(() => {
    uni.hideLoading();
    cartCount.value += quantity.value;
    specsPopupVisible.value = false;
    uni.showToast({
      title: '已加入购物车',
      icon: 'success'
    });
  }, 1000);
};

// 立即购买
const buyNow = () => {
  if (selectedSpecIndex.value < 0) {
    uni.showToast({
      title: '请选择规格',
      icon: 'none'
    });
    return;
  }
  
  // 构建订单信息
  const orderInfo = {
    productId: productInfo.id,
    productName: productInfo.title,
    productImage: productInfo.image,
    specId: productInfo.specs[selectedSpecIndex.value].id,
    specName: productInfo.specs[selectedSpecIndex.value].name,
    price: productInfo.specs[selectedSpecIndex.value].price,
    quantity: quantity.value
  };
  
  // 跳转到结算页面
  uni.navigateTo({
    url: `/pages/mall/checkout/index?orderInfo=${encodeURIComponent(JSON.stringify([orderInfo]))}`
  });
};

// 导航到首页
const navigateToHome = () => {
  uni.switchTab({
    url: '/pages/mall/index'
  });
};

// 导航到购物车
const navigateToCart = () => {
  uni.navigateTo({
    url: '/pages/mall/cart/index'
  });
};

onMounted(() => {
  // 页面挂载后进行其他初始化操作
});

// 加载商品详情
const loadProductDetail = (id) => {
  uni.showLoading({
    title: '加载中'
  });
  
  // 模拟请求商品详情
  setTimeout(() => {
    uni.hideLoading();
    // 实际应用中这里会是API请求获取商品详情
  }, 1000);
};
</script>

<style lang="scss" scoped>
.product-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 100rpx;
}

.swiper-section {
  width: 100%;
}

.product-info-card {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .product-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333333;
    margin-bottom: 16rpx;
  }
  
  .product-desc {
    font-size: 26rpx;
    color: #666666;
    margin-bottom: 30rpx;
  }
  
  .product-price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
    
    .price-section {
      display: flex;
      align-items: baseline;
      
      .price-symbol {
        font-size: 28rpx;
        color: #FFA0B4;
        font-weight: 600;
      }
      
      .product-price {
        font-size: 40rpx;
        color: #FFA0B4;
        font-weight: 600;
        margin-right: 10rpx;
      }
      
      .product-original-price {
        font-size: 26rpx;
        color: #999999;
        text-decoration: line-through;
      }
    }
    
    .sales-count {
      font-size: 24rpx;
      color: #999999;
    }
  }
}

.option-card, .service-card {
  background-color: #ffffff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  
  .option-title, .service-title {
    font-size: 28rpx;
    color: #333333;
    font-weight: 500;
    margin-right: 50rpx;
    width: 80rpx;
  }
  
  .option-value {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: 1;
    font-size: 28rpx;
    color: #666666;
  }
}

.service-card {
  align-items: flex-start;
  
  .service-tags {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    
    .service-tag {
      display: flex;
      align-items: center;
      margin-right: 30rpx;
      margin-bottom: 16rpx;
      
      text {
        font-size: 24rpx;
        color: #666666;
        margin-left: 8rpx;
      }
    }
  }
}

.detail-section {
  background-color: #ffffff;
  padding-bottom: 40rpx;
  
  .detail-header {
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .detail-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333333;
      position: relative;
      padding-left: 20rpx;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 8rpx;
        height: 30rpx;
        background-color: #c82436;
        border-radius: 4rpx;
      }
    }
  }
  
  .detail-content {
    padding: 30rpx;
    
    .detail-image {
      width: 100%;
      margin-bottom: 20rpx;
    }
  }
}

.bottom-action-bar {
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
  
  .action-icons {
    display: flex;
    
    .action-icon-item {
      width: 80rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-right: 10rpx;
      
      text {
        font-size: 20rpx;
        color: #666666;
        margin-top: 4rpx;
      }
      
      .cart-badge {
        position: absolute;
        top: 0;
        right: 5rpx;
        font-size: 20rpx;
        padding: 0 8rpx;
        min-width: 30rpx;
        height: 30rpx;
        border-radius: 15rpx;
        background-color: #FFA0B4;
        color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
        transform: translateX(50%);
      }
    }
  }
  
  .action-buttons {
    flex: 1;
    display: flex;
    margin-left: 20rpx;
    
    .add-cart-btn, .buy-now-btn {
      flex: 1;
      height: 70rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      font-weight: 500;
      border-radius: 35rpx;
    }
    
    .add-cart-btn {
      background-color: rgba(255, 160, 180, 0.1);
      color: #FFA0B4;
      margin-right: 20rpx;
    }
    
    .buy-now-btn {
      background-color: #FFA0B4;
      color: #ffffff;
    }
  }
}

.specs-popup-container {
  padding: 30rpx;
  max-height: 70vh;
  
  .specs-product-info {
    display: flex;
    padding-bottom: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .specs-product-image {
      width: 180rpx;
      height: 180rpx;
      border-radius: 12rpx;
      margin-right: 20rpx;
    }
    
    .specs-product-details {
      flex: 1;
      margin-left: 20rpx;
      
      .specs-product-price {
        display: flex;
        align-items: baseline;
        
        .price-symbol {
          font-size: 24rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
        
        .product-price {
          font-size: 36rpx;
          color: #FFA0B4;
          font-weight: 600;
        }
      }
      
      .specs-selected {
        font-size: 26rpx;
        color: #666666;
      }
    }
  }
  
  .specs-options {
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    .specs-title {
      font-size: 28rpx;
      color: #333333;
      font-weight: 500;
      margin-bottom: 20rpx;
    }
    
    .specs-values {
      display: flex;
      flex-wrap: wrap;
      
      .spec-item {
        padding: 10rpx 30rpx;
        background-color: #f5f5f5;
        border-radius: 30rpx;
        font-size: 26rpx;
        color: #666666;
        margin-right: 20rpx;
        margin-bottom: 20rpx;
        
        &.active {
          background-color: rgba(255, 160, 180, 0.1);
          color: #FFA0B4;
          border: 1rpx solid #FFA0B4;
        }
      }
    }
  }
  
  .quantity-selector {
    padding: 30rpx 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1rpx solid #f0f0f0;
    
    .quantity-title {
      font-size: 28rpx;
      color: #333333;
      font-weight: 500;
    }
  }
  
  .specs-buttons {
    padding-top: 50rpx;
    
    .specs-add-cart-btn, .specs-buy-now-btn {
      width: 100%;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      font-weight: 500;
      border-radius: 40rpx;
    }
    
    .specs-add-cart-btn {
      background-color: rgba(255, 160, 180, 0.1);
      color: #FFA0B4;
      margin-bottom: 20rpx;
    }
    
    .specs-buy-now-btn {
      background-color: #FFA0B4;
      color: #ffffff;
    }
  }
}
</style> 