import request from '@/sheep/request';

export default {
  // 点赞文章(切换点赞状态)
  like: (articleId) => {
    return request({
      url: `/promotion/cms-article/like/${articleId}`,
      method: 'POST',
    });
  },

  // 取消点赞(使用专用接口)
  unlike: (articleId, actionType = 'like') => {
    return request({
      url: '/promotion/cms-user-action/unlike',
      method: 'DELETE',
      data: { articleId, actionType },
    });
  },

  // 收藏文章(切换收藏状态)
  collect: (articleId) => {
    return request({
      url: `/promotion/cms-article/collect/${articleId}`,
      method: 'POST',
    });
  },

  // 取消收藏(使用专用接口)
  uncollect: (articleId, actionType = 'collect') => {
    return request({
      url: '/promotion/cms-user-action/uncollect',
      method: 'DELETE',
      data: { articleId, actionType },
    });
  },

  // 分享文章
  share: (articleId) => {
    return request({
      url: `/promotion/cms-article/share/${articleId}`,
      method: 'POST',
    });
  },

  // 检查用户是否已点赞/收藏
  check: (articleId, actionType) => {
    return request({
      url: '/promotion/cms-user-action/check',
      method: 'GET',
      params: { articleId, actionType },
    });
  },

  // 获取我的点赞/收藏列表
  getMyActionPage: (actionType, params) => {
    return request({
      url: '/promotion/cms-user-action/my-page',
      method: 'GET',
      params: {
        actionType,
        ...params,
      },
    });
  },

  // 获取针对我的点赞/收藏列表
  getReceivedActionPage: (actionType, params) => {
    return request({
      url: '/promotion/cms-user-action/received-page',
      method: 'GET',
      params: {
        actionType,
        ...params,
      },
    });
  },
};
