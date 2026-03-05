import request from '@/sheep/request';

export default {
  // 获取板块列表
  list: () => {
    return request({
      url: '/promotion/cms-section/list',
      method: 'GET',
    });
  },

  // 获取板块详情
  detail: (id) => {
    return request({
      url: '/promotion/cms-section/get',
      method: 'GET',
      params: { id },
    });
  },
};
