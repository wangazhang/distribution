// 社区页API服务

import request from '@/sheep/request'
import mockData from './mock'

// 判断是否使用mock数据
const useMock = true

// 打印环境变量值，便于调试
console.log('社区API - 当前环境是否使用mock数据:', useMock);

// Community API对象
const CommunityApi = {
  // 获取社区banner
  getCommunityBanners: () => {
    if (useMock) {
      console.log('使用mock数据: 社区轮播图');
      return Promise.resolve(mockData.banners || []);
    }
    return request({
      url: '/community/banners',
      method: 'GET'
    });
  },

  // 获取社区动态列表
  getPosts: (params) => {
    if (useMock) {
      console.log('使用mock数据: 社区动态列表');
      return Promise.resolve(mockData.posts || []);
    }
    return request({
      url: '/community/posts',
      method: 'GET',
      params
    });
  },

  // 获取动态详情
  getPostDetail: (id) => {
    if (useMock) {
      console.log('使用mock数据: 动态详情');
      // 从mock数据中查找对应id的动态
      const post = mockData.posts.find((item) => item.id === id) || mockData.posts[0];
      return Promise.resolve(post);
    }
    return request({
      url: `/community/posts/${id}`,
      method: 'GET'
    });
  },

  // 获取动态评论
  getPostComments: (postId, params) => {
    if (useMock) {
      console.log('使用mock数据: 动态评论');
      return Promise.resolve(mockData.comments || []);
    }
    return request({
      url: `/community/posts/${postId}/comments`,
      method: 'GET',
      params
    });
  },

  // 添加评论
  addComment: (data) => {
    if (useMock) {
      console.log('使用mock数据: 添加评论');
      return Promise.resolve({ success: true, id: 'CM' + Date.now() });
    }
    return request({
      url: '/community/comments',
      method: 'POST',
      data
    });
  },

  // 点赞/取消点赞
  toggleLike: (data) => {
    if (useMock) {
      console.log('使用mock数据: 点赞/取消点赞');
      return Promise.resolve({ success: true });
    }
    return request({
      url: '/community/like',
      method: 'POST',
      data
    });
  },

  // 收藏/取消收藏
  toggleFavorite: (data) => {
    if (useMock) {
      console.log('使用mock数据: 收藏/取消收藏');
      return Promise.resolve({ success: true });
    }
    return request({
      url: '/community/favorite',
      method: 'POST',
      data
    });
  },

  // 发布动态
  publishPost: (data) => {
    if (useMock) {
      console.log('使用mock数据: 发布动态');
      return Promise.resolve({ success: true, id: 'P' + Date.now() });
    }
    return request({
      url: '/community/posts',
      method: 'POST',
      data
    });
  },

  // 上传图片
  uploadImage: (filePath) => {
    if (useMock) {
      console.log('使用mock数据: 上传图片');
      return Promise.resolve({
        success: true,
        url: 'https://cdn.example.com/static/pic/banner/index/1.png'
      });
    }
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${request.config.baseUrl}/upload/image`,
        filePath,
        name: 'file',
        success: (res) => {
          const data = JSON.parse(res.data);
          resolve(data);
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  // 获取话题列表
  getTopics: () => {
    if (useMock) {
      console.log('使用mock数据: 话题列表');
      return Promise.resolve(mockData.topics || []);
    }
    return request({
      url: '/community/topics',
      method: 'GET'
    });
  },

  // 获取关注的用户动态
  getFollowingPosts: (params) => {
    if (useMock) {
      console.log('使用mock数据: 关注用户动态');
      return Promise.resolve(mockData.followingPosts || []);
    }
    return request({
      url: '/community/following/posts',
      method: 'GET',
      params
    });
  },

  // 获取推荐用户
  getRecommendUsers: () => {
    if (useMock) {
      console.log('使用mock数据: 推荐用户');
      return Promise.resolve(mockData.recommendUsers || []);
    }
    return request({
      url: '/community/recommend/users',
      method: 'GET'
    });
  },

  // 关注/取消关注用户
  toggleFollow: (userId) => {
    if (useMock) {
      console.log('使用mock数据: 关注/取消关注用户');
      return Promise.resolve({ success: true });
    }
    return request({
      url: '/community/follow',
      method: 'POST',
      data: { userId }
    });
  }
};

export default CommunityApi; 