import request from '@/sheep/request';

export default {
  // 获取文章分页列表
  page: (params) => {
    return request({
      url: '/promotion/cms-article/page',
      method: 'GET',
      params,
    });
  },

  // 获取文章详情
  detail: (id) => {
    return request({
      url: '/promotion/cms-article/get',
      method: 'GET',
      params: { id },
    });
  },

  // 获取热门文章列表
  hotList: (limit = 10) => {
    return request({
      url: '/promotion/cms-article/hot-list',
      method: 'GET',
      params: { limit },
    });
  },

  // 浏览文章(增加浏览数)
  view: (id) => {
    return request({
      url: `/promotion/cms-article/view/${id}`,
      method: 'POST',
    });
  },

  // 分享文章(增加分享数)
  share: (id) => {
    return request({
      url: `/promotion/cms-article/share/${id}`,
      method: 'POST',
    });
  },

  // 点赞/取消点赞文章
  like: (id) => {
    return request({
      url: `/promotion/cms-article/like/${id}`,
      method: 'POST',
    });
  },

  // 收藏/取消收藏文章
  collect: (id) => {
    return request({
      url: `/promotion/cms-article/collect/${id}`,
      method: 'POST',
    });
  },

  // 查询是否已点赞
  isLiked: (id) => {
    return request({
      url: `/promotion/cms-article/is-liked/${id}`,
      method: 'GET',
    });
  },

  // 查询是否已收藏
  isCollected: (id) => {
    return request({
      url: `/promotion/cms-article/is-collected/${id}`,
      method: 'GET',
    });
  },

  // 用户发布文章(UGC)
  publish: (data) => {
    return request({
      url: '/promotion/cms-article/publish',
      method: 'POST',
      data,
    });
  },

  // 获取我的发布文章列表
  getMyArticles: (params) => {
    return request({
      url: '/promotion/cms-article/my-articles/page',
      method: 'GET',
      params,
    });
  },
};
