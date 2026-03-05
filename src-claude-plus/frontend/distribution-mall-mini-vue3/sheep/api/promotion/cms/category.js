import request from '@/sheep/request';

export default {
  // 获取分类列表
  list: (params) => {
    return request({
      url: '/promotion/cms-category/list',
      method: 'GET',
      params,
    });
  },

  // 根据板块ID获取分类列表
  listBySection: (sectionId) => {
    return request({
      url: '/promotion/cms-category/list',
      method: 'GET',
      params: { sectionId },
    });
  },

  // 获取分类详情
  detail: (id) => {
    return request({
      url: '/promotion/cms-category/get',
      method: 'GET',
      params: { id },
    });
  },
};
