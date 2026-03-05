import request from '@/sheep/request';

export default {
  // 获取标签列表
  list: (params) => {
    return request({
      url: '/promotion/cms-tag/list',
      method: 'GET',
      params,
    });
  },

  // 根据分类ID获取标签列表
  listByCategory: (categoryId) => {
    return request({
      url: '/promotion/cms-tag/list',
      method: 'GET',
      params: { categoryId },
    });
  },

  // 获取标签详情
  detail: (id) => {
    return request({
      url: '/promotion/cms-tag/get',
      method: 'GET',
      params: { id },
    });
  },
};
