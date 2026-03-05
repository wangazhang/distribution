import request from '@/sheep/request';

const CommentApi = {
  // 获取我的评论列表
  getMyComments: (params) => {
    return request({
      url: '/promotion/cms-article-comment/my-page',
      method: 'GET',
      params,
    });
  },

  // 获取收到的回复列表
  getRepliesToMe: (params) => {
    return request({
      url: '/promotion/cms-article-comment/replies-to-me',
      method: 'GET',
      params,
    });
  },

  // 获取未读回复数量
  getUnreadCount: () => {
    return request({
      url: '/promotion/cms-article-comment/unread-count',
      method: 'GET',
    });
  },

  // 标记为已读
  markAsRead: (ids) => {
    return request({
      url: '/promotion/cms-article-comment/mark-read',
      method: 'PUT',
      data: { ids },
    });
  },

  // 删除评论
  deleteComment: (id) => {
    return request({
      url: '/promotion/cms-article-comment/delete',
      method: 'DELETE',
      params: { id },
    });
  },

  // 发表评论
  createComment: (data) => {
    return request({
      url: '/promotion/cms-article-comment/create',
      method: 'POST',
      data,
    });
  },

  // 获取文章评论列表
  getArticleComments: (params) => {
    return request({
      url: '/promotion/cms-article-comment/page',
      method: 'GET',
      params,
    });
  },
};

export default CommentApi;
