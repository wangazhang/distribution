<template>
  <view class="page-container">
    <!-- 自定义顶部导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <!-- 左侧logo -->
        <view class="logo-box">
          <u-icon name="star" size="40" color="#333"></u-icon>
        </view>
        
        <!-- 中间搜索框 -->
        <view class="search-box">
          <u-icon name="search" size="36" color="#999"></u-icon>
          <text class="search-placeholder">搜好货</text>
        </view>
        
        <!-- 右侧预留空间 -->
        <view class="capsule-placeholder" :style="{ width: (menuButtonInfo.width + 20) + 'px' }"></view>
        
        <!-- 右侧扩展区域 -->
        <view class="extra-icons">
          <u-icon name="more-dot-fill" size="40" color="#333"></u-icon>
        </view>
      </view>
    </view>
    
    <!-- 标签切换栏 - 固定在顶部 -->
    <view class="fixed-tab-nav" :style="{ top: (statusBarHeight + 44) + 'px' }">
      <view 
        class="tab-item" 
        v-for="(item, index) in tabList" 
        :key="index" 
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text>{{ item.name }}</text>
        <view class="tab-line" v-if="currentTab === index"></view>
      </view>
    </view>
    
    <!-- 页面内容区域 -->
    <view class="page-content" :style="{ paddingTop: (statusBarHeight + 44 + 40) + 'px' }">
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <u-loading mode="circle" size="36"></u-loading>
        <text class="loading-text">数据加载中...</text>
      </view>
      
      <!-- 错误状态 -->
      <view v-else-if="error" class="error-container">
        <u-icon name="error-circle" size="60" color="#FF5777"></u-icon>
        <text class="error-text">{{errorMessage}}</text>
        <view class="retry-btn" @tap="fetchMallData">
          <text>重新加载</text>
        </view>
      </view>
      
      <!-- 正常内容 -->
      <block v-else>
        <!-- 觅她活动标签内容 -->
        <view v-if="currentTab === 0">
          <!-- 轮播图 -->
          <view class="banner-section">
            <u-swiper
              :list="bannerList"
              height="360"
              :autoplay="true"
              indicator
              indicatorMode="dot"
            ></u-swiper>
          </view>
          
          <!-- 品牌介绍区域 -->
          <view class="brand-info">
            <view class="brand-tags">
              <view class="tag">
                <u-icon name="checkmark-circle" color="#333" size="34"></u-icon>
                <text>正品保证</text>
              </view>
              <view class="tag">
                <u-icon name="tags" color="#333" size="34"></u-icon>
                <text>大牌小样</text>
              </view>
              <view class="tag">
                <u-icon name="rmb-circle" color="#333" size="34"></u-icon>
                <text>购物得积分</text>
              </view>
            </view>
            
            <!-- 会员卡片 -->
            <view class="member-cards">
              <view class="member-card">
                <text class="card-title">成为VIP会员</text>
              </view>
              <view class="member-card">
                <text class="card-title">成为院长助理</text>
              </view>
            </view>
          </view>
          
          <!-- 商品分类 -->
          <view class="category-section">
            <scroll-view scroll-x class="category-scroll">
              <view class="category-list">
                <view 
                  class="category-item" 
                  v-for="(item, index) in categoryList" 
                  :key="index"
                  :class="{ active: currentCategory === index }"
                  @click="switchCategory(index)"
                >
                  <text>{{ item.name }}</text>
                </view>
              </view>
            </scroll-view>
          </view>
          
          <!-- 商品列表 -->
          <view class="product-cards">
            <view class="product-card" v-for="(item, index) in productList" :key="index" @tap="navigateToProductDetail(item.id)">
              <!-- <view class="product-type">居家美塑疗法</view> -->
              <view class="product-image">
                <image :src="item.image" mode="aspectFill"></image>
              </view>
              <view class="product-info">
                <view class="product-title">{{ item.title }} 【1盒5支装】</view>
                <view class="product-desc">{{ item.desc }}</view>
                <view class="product-price-row">
                  <view class="price-left">
                    <text class="price-symbol">¥</text>
                    <text class="product-price">{{ item.price }}</text>
                    <text class="product-original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
                  </view>
                  <view class="add-cart-btn" @tap.stop="handleAddToCart(item)">
                    <u-icon name="shopping-cart" color="#ffffff" size="40"></u-icon>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 品牌馆标签内容 -->
        <view v-if="currentTab === 1" class="empty-tab-content">
          <u-empty text="品牌馆内容正在开发中" mode="data"></u-empty>
        </view>
        
        <!-- 商品分组标签内容 -->
        <view v-if="currentTab === 2" class="category-container">
          <!-- 左侧分类菜单 -->
          <scroll-view scroll-y class="category-menu" :scroll-top="scrollTop">
            <view 
              v-for="(item, index) in categoryGroups" 
              :key="index"
              class="category-menu-item"
              :class="{ active: currentCategoryGroup === index }"
              @tap="switchCategoryGroup(index)"
            >
              <text>{{ item.name }}</text>
            </view>
          </scroll-view>
          
          <!-- 右侧内容区域 -->
          <scroll-view 
            scroll-y 
            class="category-content" 
            @scroll="onCategoryScroll"
            :scroll-into-view="'category-'+activeCategoryId"
            :scroll-with-animation="true"
          >
            <!-- 分类广告图 -->
            <view class="category-banner">
              <image src="https://cdn.example.com/static/pic/beauty-banner.jpg" mode="widthFix"></image>
            </view>
            
            <!-- 分类内容 -->
            <view 
              v-for="(group, groupIndex) in categoryGroups" 
              :key="groupIndex"
              class="category-group"
              :id="'category-'+groupIndex"
            >
              <view class="category-group-title">{{ group.name }}</view>
              <view class="category-group-content">
                <view 
                  class="category-item-card"
                  v-for="(item, itemIndex) in group.items"
                  :key="itemIndex"
                  @tap="navigateToList(group.name, item.name)"
                >
                  <image :src="item.image" mode="aspectFit"></image>
                  <text>{{ item.name }}</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </block>
    </view>
    
    <!-- 自定义底部导航栏 -->
    <!-- <yh-tabbar activeColor="#FFA0B4" color="#999999"></yh-tabbar> -->
  </view>
</template>

<script setup >
import { ref, onMounted } from 'vue';
import {
  getMallBanners,
  getCategories,
  getHotProducts,
  getNewProducts,
  addToCart
} from '@/yehu/api/mall';

// 状态栏高度
const statusBarHeight = ref(0);

// 胶囊按钮信息
const menuButtonInfo = ref({
  width: 80,
  height: 32,
  left: 320,
  top: 24,
  right: 400,
  bottom: 56
});

// 加载状态
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('数据加载失败，请重试');

// 表示当前tab页
const currentTab = ref(0);
const tabList = ref([
  { name: '觅她活动' },
  { name: '品牌馆' },
  { name: '分类' }
]);

// 轮播图数据
const bannerList = ref([]);

// 分类数据
const categoryList = ref([]);
const currentCategory = ref(0);

// 商品列表
const productList = ref([]);

// 商品分组数据
const categoryGroups = ref([]);
const currentCategoryGroup = ref(0);
const activeCategoryId = ref(0);
const scrollTop = ref(0);

// 获取数据方法
const fetchMallData = async () => {
  loading.value = true;
  error.value = false;
  
  try {
    // 并行请求数据
    const [bannersData, categoriesData, hotProductsData] = await Promise.all([
      getMallBanners(),
      getCategories(),
      getHotProducts()
    ]);
    
    // 设置轮播图数据
    bannerList.value = Array.isArray(bannersData) ? bannersData : [];
    
    // 处理分类数据
    categoryList.value = [
      { id: 'all', name: '全部' }
    ];
    
    // 确保categoriesData是数组类型
    if (Array.isArray(categoriesData)) {
      // 添加其他分类
      if (categoriesData.length > 0) {
        categoryList.value = [
          { id: 'all', name: '全部' },
          ...categoriesData.slice(0, 7)
        ];
      }
      
      // 处理商品分组数据
      const groupedCategories = [];
      for (let i = 0; i < categoriesData.length; i += 8) {
        const group = {
          name: `分类${Math.floor(i/8) + 1}`,
          items: categoriesData.slice(i, i + 8)
        };
        groupedCategories.push(group);
      }
      categoryGroups.value = groupedCategories.length > 0 ? groupedCategories : [{
        name: '默认分类',
        items: []
      }];
    } else {
      console.error('分类数据不是数组:', categoriesData);
      // 设置一个默认的分类组
      categoryGroups.value = [{
        name: '默认分类',
        items: []
      }];
    }
    
    // 处理商品数据
    productList.value = Array.isArray(hotProductsData) ? hotProductsData.map((item) => ({
      id: item.id || '',
      title: item.name || '商品',
      desc: item.tag || '描述',
      price: item.price || '0.00',
      originalPrice: item.marketPrice || '',
      image: item.image || ''
    })) : [];
    
    console.log('商城数据加载完成');
  } catch (err) {
    console.error('获取商城数据失败', err);
    error.value = true;
    errorMessage.value = err.message || '数据加载失败，请重试';
  } finally {
    loading.value = false;
  }
};

// 切换顶部标签
const switchTab = (index) => {
  currentTab.value = index;
};

// 切换分类
const switchCategory = (index) => {
  currentCategory.value = index;
  loadCategoryProducts(index);
};

// 加载分类商品
const loadCategoryProducts = async (categoryIndex) => {
  try {
    let productsData = [];
    
    // 全部分类时加载热门商品，其他分类根据categoryId加载
    if (categoryIndex === 0) {
      productsData = await getHotProducts();
    } else {
      const categoryId = categoryList.value[categoryIndex]?.id;
      if (categoryId) {
        productsData = await getHotProducts({ categoryId });
      } else {
        productsData = await getHotProducts();
      }
    }
    
    // 确保返回的数据是数组
    if (Array.isArray(productsData)) {
      productList.value = productsData.map((item) => ({
        id: item.id || '',
        title: item.name || '商品',
        desc: item.tag || '描述',
        price: item.price || '0.00',
        originalPrice: item.marketPrice || '',
        image: item.image || ''
      }));
    } else {
      console.error('商品数据不是数组:', productsData);
      productList.value = [];
      uni.showToast({
        title: '分类商品数据格式错误',
        icon: 'none'
      });
    }
  } catch (err) {
    console.error('加载分类商品失败', err);
    productList.value = [];
    uni.showToast({
      title: '加载分类商品失败',
      icon: 'none'
    });
  }
};

// 切换分类分组
const switchCategoryGroup = (index) => {
  currentCategoryGroup.value = index;
  activeCategoryId.value = index;
};

// 监听分类内容滚动
const onCategoryScroll = (e) => {
  // 监听滚动，更新左侧当前选中的分类
  // 简单实现，实际应该计算每个分类组的位置来决定
};

// 跳转到分类列表
const navigateToList = (groupName, itemName) => {
  uni.navigateTo({
    url: `/pages/mall/category/index?group=${encodeURIComponent(groupName)}&name=${encodeURIComponent(itemName)}`
  });
};

// 跳转到商品详情页
const navigateToProductDetail = (id) => {
  uni.navigateTo({
    url: `/pages/mall/product-detail/index?id=${id}`
  });
};

// 添加到购物车
const handleAddToCart = async (item) => {
  try {
    const result = await addToCart({
      productId: item.id,
      quantity: 1
    });
    
    if (result.success) {
      uni.showToast({
        title: '已加入购物车',
        icon: 'success'
      });
    }
  } catch (err) {
    console.error('添加购物车失败', err);
    uni.showToast({
      title: '添加购物车失败',
      icon: 'none'
    });
  }
};

onMounted(() => {
  // 获取系统信息，设置状态栏高度
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0;
      
      // 获取胶囊按钮位置信息
      // @ts-ignore
      const menuButton = uni.getMenuButtonBoundingClientRect();
      menuButtonInfo.value = menuButton;
    }
  });
  
  // 获取商城数据
  fetchMallData();
});
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #ffffff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  
  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 20rpx;
    
    .logo-box {
      width: 60rpx;
      height: 60rpx;
      margin-right: 20rpx;
    }
    
    .search-box {
      flex: 1;
      display: flex;
      align-items: center;
      background-color: #f5f5f5;
      border-radius: 30rpx;
      padding: 8rpx 20rpx;
      height: 60rpx;
      max-width: 50%;
      
      .search-placeholder {
        font-size: 28rpx;
        color: #999;
        margin-left: 10rpx;
      }
    }
    
    .capsule-placeholder {
      // 不需要固定宽度，使用动态计算的值
    }
    
    .extra-icons {
      margin-left: 20rpx;
      display: flex;
      align-items: center;
    }
  }
}

.fixed-tab-nav {
  position: fixed;
  top: 44px;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #ffffff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  height: 80rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    
    text {
      font-size: 28rpx;
      color: #333;
    }
    
    &.active {
      text {
        color: #FFA0B4;
        font-weight: bold;
      }
    }
    
    .tab-line {
      position: absolute;
      bottom: 0;
      width: 40rpx;
      height: 4rpx;
      background-color: #FFA0B4;
      border-radius: 2rpx;
    }
  }
}

.page-content {
  padding-bottom: 140rpx;
  
  .banner-section {
    width: 100%;
  }
  
  .brand-info {
    background-color: #ffffff;
    padding: 20rpx;
    margin-top: 20rpx;
    
    .brand-tags {
      display: flex;
      justify-content: space-around;
      margin-bottom: 20rpx;
      
      .tag {
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #333;
        
        text {
          margin-left: 8rpx;
        }
      }
    }
    
    .member-cards {
      display: flex;
      justify-content: space-between;
      
      .member-card {
        width: 48%;
        height: 120rpx;
        background-color: #000;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 20rpx;
        
        .card-title {
          color: #fff;
          font-size: 28rpx;
          font-weight: bold;
        }
      }
    }
  }
  
  .category-section {
    background-color: #ffffff;
    padding: 20rpx 0;
    margin-top: 20rpx;
    
    .category-scroll {
      width: 100%;
      white-space: nowrap;
      
      .category-list {
        padding: 0 20rpx;
        
        .category-item {
          display: inline-block;
          padding: 10rpx 30rpx;
          margin-right: 20rpx;
          background-color: #f5f5f5;
          border-radius: 30rpx;
          font-size: 26rpx;
          color: #666666;
          
          &.active {
            background-color: rgba(255, 160, 180, 0.1);
            color: #FFA0B4;
          }
        }
      }
    }
  }
  
  .product-cards {
    padding: 20rpx;
    
    .product-card {
      background-color: #ffffff;
      border-radius: 12rpx;
      overflow: hidden;
      margin-bottom: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      
      .product-type {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
        padding: 20rpx;
        border-bottom: 1px solid #f5f5f5;
      }
      
      .product-image {
        width: 100%;
        height: 400rpx;
        
        image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .product-info {
        padding: 20rpx;
        
        .product-title {
          font-size: 32rpx;
          font-weight: 600;
          color: #333333;
          margin-bottom: 10rpx;
        }
        
        .product-desc {
          font-size: 24rpx;
          color: #666666;
          margin-bottom: 20rpx;
        }
        
        .product-price-row {
          display: flex;
          align-items: center;
          justify-content: space-between;
          
          .price-left {
            display: flex;
            align-items: baseline;
            
            .price-symbol {
              font-size: 24rpx;
              color: #c82436;
              font-weight: bold;
              margin-right: 4rpx;
            }
            
            .product-price {
              font-size: 36rpx;
              font-weight: 600;
              color: #c82436;
              margin-right: 12rpx;
            }
            
            .product-original-price {
              font-size: 24rpx;
              color: #999999;
              text-decoration: line-through;
            }
          }
          
          .add-cart-btn {
            width: 60rpx;
            height: 60rpx;
            border-radius: 50%;
            background-color: #9f1f35;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
    }
  }
  
  .empty-tab-content {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 100rpx 0;
  }
}

.category-container {
  display: flex;
  height: calc(100vh - 44px - var(--status-bar-height) - 40px);
  overflow: hidden;
}

.category-menu {
  width: 180rpx;
  height: 100%;
  background-color: #f5f5f5;
  overflow-y: auto;
}

.category-menu-item {
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #333;
  position: relative;
  text-align: center;
  padding: 20rpx 10rpx;
  
  &.active {
    color: #FFA0B4;
    font-weight: bold;
    background-color: #ffffff;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 8rpx;
      height: 36rpx;
      background-color: #FFA0B4;
      border-radius: 0 4rpx 4rpx 0;
    }
  }
}

.category-content {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
  padding-bottom: 100rpx;
}

.category-banner {
  width: 100%;
  height: 220rpx;
  padding: 20rpx;
  box-sizing: border-box;
  
  image {
    width: 100%;
    height: 100%;
    border-radius: 12rpx;
    object-fit: cover;
  }
}

.category-group {
  padding: 20rpx;
}

.category-group-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 20rpx;
  padding-left: 10rpx;
}

.category-group-content {
  display: flex;
  flex-wrap: wrap;
}

.category-item-card {
  width: 33.33%;
  padding: 10rpx;
  box-sizing: border-box;
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  image {
    width: 120rpx;
    height: 120rpx;
    object-fit: contain;
    margin-bottom: 10rpx;
  }
  
  text {
    display: block;
    text-align: center;
    font-size: 24rpx;
    color: #333;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.error-text {
  color: #FF5777;
  margin-top: 20rpx;
  margin-bottom: 20rpx;
}

.retry-btn {
  padding: 20rpx 40rpx;
  background-color: #FFA0B4;
  border-radius: 30rpx;
  text-align: center;
  color: #ffffff;
  font-size: 28rpx;
}
</style> 