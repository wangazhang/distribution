<template>
  <s-layout title="选择商品">
    <view class="product-select-page">
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-input-container">
          <view class="search-input">
            <uni-icons type="search" size="20" color="#999"></uni-icons>
            <input
              v-model="searchKeyword"
              placeholder="搜索商品名称"
              @input="onSearchInput"
              @confirm="searchProducts"
            />
          </view>
        </view>
      </view>

      <!-- 商品列表 -->
      <scroll-view
        scroll-y
        class="product-scroll"
        @scrolltolower="loadMore"
        :style="{ height: scrollHeight + 'px' }"
      >
        <view class="product-list-container">
          <view class="product-list">
            <view
              v-for="product in productList"
              :key="product.id"
              class="product-item"
              @tap="selectAndReturnProduct(product)"
            >
              <!-- 商品图片 -->
              <view class="product-image-container">
                <image :src="product.picUrl" class="product-image" mode="aspectFill" />
              </view>

              <!-- 商品信息 -->
              <view class="product-info">
                <view class="product-name">{{ product.name }}</view>
                <view class="product-meta">
                  <view class="product-price">¥{{ (product.price / 100).toFixed(2) }}</view>
                  <view class="product-stock" v-if="product.stock">库存{{ product.stock }}</view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="state.loading" class="loading-more">
          <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 没有更多数据 -->
        <view v-if="!state.hasMore && productList.length > 0" class="no-more">
          <text class="no-more-text">已加载全部商品</text>
        </view>

        <!-- 空状态 -->
        <view
          v-if="!state.loading && productList.length === 0 && !state.loadError"
          class="empty-state"
        >
          <uni-icons type="shop" size="80" color="#ccc"></uni-icons>
          <text class="empty-text">暂无商品</text>
        </view>

        <!-- 加载错误状态 -->
        <view
          v-if="!state.loading && state.loadError && productList.length === 0"
          class="error-state"
        >
          <uni-icons type="warning" size="80" color="#ff6b00"></uni-icons>
          <text class="error-text">加载失败</text>
          <view class="retry-btn" @tap="loadProductList">
            <uni-icons type="refresh" size="20" color="#ff6b00"></uni-icons>
            <text class="retry-text">点击重试</text>
          </view>
        </view>
      </scroll-view>

      <!-- 快速选择提示 -->
      <view class="quick-tip">
        <view class="tip-content">
          <uni-icons type="info" size="14" color="white"></uni-icons>
          <text class="tip-text">点击商品即可添加到文章</text>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, reactive, onMounted } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import spuApi from '@/sheep/api/product/spu';

  const state = reactive({
    loading: false,
    hasMore: true,
    searchTimer: null, // 搜索防抖定时器
    loadError: null, // 加载错误信息
  });

  const searchKeyword = ref('');
  const productList = ref([]);
  const pagination = reactive({
    pageNo: 1,
    pageSize: 20,
    total: 0,
  });

  // 计算滚动区域高度
  const scrollHeight = ref(500);

  onMounted(() => {
    // 获取屏幕高度，计算滚动区域高度
    uni.getSystemInfo({
      success: (res) => {
        // 导航栏高度 + 底部操作栏高度 + 搜索栏高度 + 间距
        const navBarHeight = 44; // 导航栏高度
        const tabBarHeight = 60; // 底部操作栏高度
        const searchBarHeight = 48; // 搜索栏高度（缩小后）
        const padding = 40; // 间距

        scrollHeight.value =
          res.windowHeight - navBarHeight - tabBarHeight - searchBarHeight - padding;
      },
    });
  });

  onLoad(async () => {
    await loadProductList();
  });

  // 搜索输入处理
  const onSearchInput = () => {
    // 防抖处理
    clearTimeout(state.searchTimer);
    state.searchTimer = setTimeout(() => {
      searchProducts();
    }, 500);
  };

  // 搜索商品
  const searchProducts = () => {
    pagination.pageNo = 1;
    productList.value = [];
    state.hasMore = true;
    loadProductList();
  };

  // 加载商品列表
  const loadProductList = async () => {
    if (state.loading || !state.hasMore) return;

    state.loading = true;
    state.loadError = null; // 清除之前的错误

    try {
      const params = {
        pageNo: pagination.pageNo,
        pageSize: pagination.pageSize,
        status: 0, // 只显示上架商品
      };

      if (searchKeyword.value.trim()) {
        params.keyword = searchKeyword.value.trim();
      }

      const res = await spuApi.getSpuPage(params);

      if (res.code === 0 && res.data) {
        const newProducts = res.data.list || [];

        if (pagination.pageNo === 1) {
          productList.value = newProducts;
        } else {
          productList.value = [...productList.value, ...newProducts];
        }

        pagination.total = res.data.total || 0;
        state.hasMore = productList.value.length < pagination.total;
      } else {
        throw new Error(res.msg || '加载商品数据失败');
      }
    } catch (error) {
      console.error('加载商品列表失败:', error);
      state.loadError = error.message || '加载商品列表失败';

      if (pagination.pageNo === 1) {
        // 首次加载失败时显示toast
        uni.showToast({
          title: state.loadError,
          icon: 'none',
          duration: 2000,
        });
      }
    } finally {
      state.loading = false;
    }
  };

  // 加载更多
  const loadMore = () => {
    if (state.hasMore && !state.loading) {
      pagination.pageNo++;
      loadProductList();
    } else if (!state.hasMore) {
      console.log('已加载所有商品，无需继续加载');
    }
  };

  // 选中商品并返回
  const selectAndReturnProduct = (product) => {
    console.log('选择商品:', product);

    // 添加选择成功提示
    uni.showToast({
      title: '已添加商品',
      icon: 'success',
      duration: 1500,
    });

    // 通过全局事件总线发送商品数据
    uni.$emit('selectedProducts', {
      products: [product], // 单个商品
    });
    console.log('已发送商品数据到发布页面');

    // 立即返回上一页
    setTimeout(() => {
      uni.navigateBack();
    }, 500);
  };
</script>

<style lang="scss" scoped>
  .product-select-page {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    padding: 0;
    box-sizing: border-box;
  }

  /* 搜索栏 - 电商风格 */
  .search-bar {
    background: white;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    position: relative;
    z-index: 10;

    .search-input-container {
      max-width: 600rpx;
      margin: 0 auto;
      padding: 12rpx 32rpx;

      .search-input {
        display: flex;
        align-items: center;
        padding: 16rpx 24rpx;
        background: #f8f9fa;
        border-radius: 32rpx;
        border: 2rpx solid transparent;
        transition: all 0.3s ease;

        &:focus-within {
          background: white;
          border-color: #ff6b00;
          box-shadow: 0 0 0 6rpx rgba(255, 107, 0, 0.1);
        }

        uni-icons {
          margin-right: 20rpx;
          color: #999;
          transition: color 0.3s ease;
        }

        input {
          flex: 1;
          font-size: 30rpx;
          color: #333;
          font-weight: 500;

          &::placeholder {
            color: #999;
            font-weight: normal;
          }
        }
      }
    }
  }

  /* 商品滚动区域 */
  .product-scroll {
    flex: 1;
    padding: 20rpx 0 0 0;
    overflow-y: auto;

    .product-list-container {
      max-width: 700rpx;
      margin: 0 auto;
      padding: 0 32rpx;
    }

    /* 商品列表 - 现代卡片设计 */
    .product-list {
      display: grid;
      grid-template-columns: 1fr;
      gap: 20rpx;
    }
  }

  .product-item {
    background: white;
    border-radius: 20rpx;
    padding: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
    border: 1rpx solid rgba(0, 0, 0, 0.04);
    display: flex;
    align-items: center;
    position: relative;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:active {
      transform: scale(0.98);
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    }

    /* 商品图片容器 */
    .product-image-container {
      width: 140rpx;
      height: 140rpx;
      border-radius: 16rpx;
      overflow: hidden;
      margin-right: 24rpx;
      position: relative;
      background: #f8f9fa;

      .product-image {
        width: 100%;
        height: 100%;
        border-radius: 16rpx;
        transition: transform 0.3s ease;
      }
    }

    /* 商品信息 */
    .product-info {
      flex: 1;
      min-width: 0;

      .product-name {
        font-size: 28rpx; // 商品名称字体最大
        color: #1a1a1a; // 颜色最深，最突出
        font-weight: 700; // 最粗，强调重要性
        margin-bottom: 12rpx;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        word-break: break-word;
        letter-spacing: 0.5rpx; // 稍微增加字间距
      }

      .product-meta {
        display: flex;
        flex-direction: column;
        gap: 6rpx;

        .product-price {
          font-size: 26rpx; // 价格字体明显更小
          color: #888; // 颜色较浅，不抢眼
          font-weight: 400; // 正常字重，不突出
          letter-spacing: 0.2rpx; // 字间距较小
        }

        .product-stock {
          font-size: 22rpx; // 最小字体
          color: #aaa; // 最浅颜色，最不突出
          background: #f5f5f5; // 背景色更浅
          padding: 3rpx 10rpx; // 内边距更小
          border-radius: 10rpx;
          display: inline-block;
          width: fit-content;
          font-weight: 400; // 正常字重
        }
      }
    }
  }

  /* 加载状态 - 现代设计 */
  .loading-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60rpx 0;
    gap: 16rpx;

    .loading-text {
      font-size: 28rpx;
      color: #7f8c8d;
      font-weight: 500;
    }
  }

  .no-more {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60rpx 0;

    .no-more-text {
      font-size: 28rpx;
      color: #95a5a6;
      font-weight: 500;
    }
  }

  /* 空状态 - 现代设计 */
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 0;
    text-align: center;

    .empty-text {
      font-size: 30rpx;
      color: #7f8c8d;
      margin-top: 24rpx;
      font-weight: 500;
    }
  }

  /* 错误状态 - 现代设计 */
  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 0;
    text-align: center;

    .error-text {
      font-size: 30rpx;
      color: #ff6b00;
      margin-top: 24rpx;
      font-weight: 500;
    }

    .retry-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 40rpx;
      padding: 16rpx 32rpx;
      background: linear-gradient(135deg, #ff6b00 0%, #ff8f33 100%);
      border-radius: 40rpx;
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        opacity: 0.8;
      }

      .retry-text {
        font-size: 28rpx;
        color: white;
        font-weight: 500;
      }
    }
  }

  /* 快速选择提示 */
  .quick-tip {
    background: linear-gradient(90deg, #ff6b00 0%, #ff8f33 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border-top: 1rpx solid rgba(255, 255, 255, 0.1);

    .tip-content {
      max-width: 700rpx;
      margin: 0 auto;
      padding: 24rpx 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      width: 100%;
      box-sizing: border-box;

      .tip-text {
        font-size: 28rpx;
        color: white;
        font-weight: 500;
      }
    }
  }

  /* 动画效果 */
  @keyframes pulse {
    0% {
      transform: scale(1);
      opacity: 1;
    }
    50% {
      transform: scale(1.1);
      opacity: 0.8;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }

  /* 响应式优化 */
  @media (prefers-color-scheme: dark) {
    .product-select-page {
      background: linear-gradient(180deg, #1a1a1a 0%, #2c2c2c 100%);
    }

    .product-item {
      background: #2c2c2c;
      border-color: rgba(255, 255, 255, 0.1);
    }

    .product-name {
      color: #ffffff;
    }

    .product-stock {
      background: #3a3a3a;
      color: #b0b0b0;
    }
  }
</style>
