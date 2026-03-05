// 商城页API服务

import request from '@/sheep/request'
import mockData from './mock'

// 判断是否使用mock数据
const useMock = import.meta.env.VITE_USE_MOCK === 'true'

// 打印环境变量值，便于调试
console.log('当前环境是否使用mock数据:', useMock);

// 获取商城轮播图
export const getMallBanners = () => {
  if (useMock) {
    console.log('使用mock数据: 轮播图');
    const banners = mockData.banners || [];
    return Promise.resolve(Array.isArray(banners) ? banners : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 轮播图');
  return request.get('/mall/banners')
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取轮播图失败:', err);
      return [];
    });
}

// 获取商品分类
export const getCategories = () => {
  if (useMock) {
    console.log('使用mock数据: 商品分类');
    // 确保返回的是数组
    const categories = mockData.categories || [];
    return Promise.resolve(Array.isArray(categories) ? categories : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 商品分类');
  return request.get('/mall/categories')
    .then((res) => {
      // 确保返回的是数组
      return Array.isArray(res) ? res : [];
    })
    .catch((err) => {
      console.error('获取分类数据失败:', err);
      return []; // 发生错误时返回空数组
    });
}

// 获取热门商品
export const getHotProducts = (params) => {
  if (useMock) {
    console.log('使用mock数据: 热门商品');
    const products = mockData.hotProducts || [];
    return Promise.resolve(Array.isArray(products) ? products : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 热门商品');
  return request.get('/mall/products/hot', { params })
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取热门商品失败:', err);
      return [];
    });
}

// 获取新品上市
export const getNewProducts = (params) => {
  if (useMock) {
    console.log('使用mock数据: 新品上市');
    const products = mockData.newProducts || [];
    return Promise.resolve(Array.isArray(products) ? products : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 新品上市');
  return request.get('/mall/products/new', { params })
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取新品上市失败:', err);
      return [];
    });
}

// 获取推荐商品
export const getRecommendProducts = (params) => {
  if (useMock) {
    console.log('使用mock数据: 推荐商品');
    const products = mockData.recommendProducts || [];
    return Promise.resolve(Array.isArray(products) ? products : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 推荐商品');
  return request.get('/mall/products/recommend', { params })
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取推荐商品失败:', err);
      return [];
    });
}

// 获取商品详情
export const getProductDetail = (id) => {
  if (useMock) {
    console.log('使用mock数据: 商品详情');
    return Promise.resolve(mockData.productDetail || {});
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 商品详情');
  return request.get(`/mall/products/${id}`)
    .then((res) => res || {})
    .catch((err) => {
      console.error('获取商品详情失败:', err);
      return {};
    });
}

// 获取商品评价
export const getProductComments = (productId, params) => {
  if (useMock) {
    console.log('使用mock数据: 商品评价');
    const comments = mockData.productComments || [];
    return Promise.resolve(Array.isArray(comments) ? comments : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 商品评价');
  return request.get(`/mall/products/${productId}/comments`, { params })
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取商品评价失败:', err);
      return [];
    });
}

// 搜索商品
export const searchProducts = (params) => {
  if (useMock) {
    console.log('使用mock数据: 搜索商品');
    const products = mockData.searchResult || [];
    return Promise.resolve(Array.isArray(products) ? products : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 搜索商品');
  return request.get('/mall/products/search', { params })
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('搜索商品失败:', err);
      return [];
    });
}

// 添加购物车
export const addToCart = (data) => {
  if (useMock) {
    console.log('使用mock数据: 添加购物车');
    return Promise.resolve({ success: true });
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 添加购物车');
  return request.post('/mall/cart/add', data)
    .catch((err) => {
      console.error('添加购物车失败:', err);
      return { success: false, message: err.message };
    });
}

// 获取购物车列表
export const getCartList = () => {
  if (useMock) {
    console.log('使用mock数据: 购物车列表');
    const cartList = mockData.cartList || [];
    return Promise.resolve(Array.isArray(cartList) ? cartList : []);
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 购物车列表');
  return request.get('/mall/cart/list')
    .then((res) => Array.isArray(res) ? res : [])
    .catch((err) => {
      console.error('获取购物车列表失败:', err);
      return [];
    });
}

// 更新购物车
export const updateCart = (data) => {
  if (useMock) {
    console.log('使用mock数据: 更新购物车');
    return Promise.resolve({ success: true });
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 更新购物车');
  return request.post('/mall/cart/update', data)
    .catch((err) => {
      console.error('更新购物车失败:', err);
      return { success: false, message: err.message };
    });
}

// 删除购物车商品
export const removeFromCart = (data) => {
  if (useMock) {
    console.log('使用mock数据: 删除购物车商品');
    return Promise.resolve({ success: true });
  }
  // 只有非mock模式才发送请求
  console.log('使用真实API: 删除购物车商品');
  return request.post('/mall/cart/remove', data)
    .catch((err) => {
      console.error('删除购物车商品失败:', err);
      return { success: false, message: err.message };
    });
} 