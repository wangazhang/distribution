import request from '@/sheep/request';

// CMS板块相关API
export const cmsSection = {
  // 获取板块列表
  list: () => {
    return request({
      url: '/promotion/cms-section/app/list',
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

// CMS分类相关API
export const cmsCategory = {
  // 根据板块ID获取分类列表
  listBySection: (sectionId) => {
    return request({
      url: '/promotion/cms-category/list-by-section',
      method: 'GET',
      params: { sectionId },
    });
  },

  // 获取分类简单列表
  simpleList: (sectionId) => {
    return request({
      url: '/promotion/cms-category/simple-list',
      method: 'GET',
      params: { sectionId },
    });
  },
};

// CMS标签相关API
export const cmsTag = {
  // 根据分类ID获取标签列表
  listByCategory: (categoryId) => {
    return request({
      url: '/promotion/cms-tag/list-by-category',
      method: 'GET',
      params: { categoryId },
    });
  },

  // 获取标签简单列表
  simpleList: (categoryId) => {
    return request({
      url: '/promotion/cms-tag/simple-list',
      method: 'GET',
      params: { categoryId },
    });
  },
};

// CMS文章相关API
export const cmsArticle = {
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

  // 获取用户端文章详情
  appDetail: (id) => {
    return request({
      url: '/promotion/cms-article/app/detail',
      method: 'GET',
      params: { id },
    });
  },

  // 获取热门文章列表
  hotList: (params) => {
    return request({
      url: '/promotion/cms-article/hot-list',
      method: 'GET',
      params,
    });
  },

  // 获取推荐文章列表
  recommendList: (params) => {
    return request({
      url: '/promotion/cms-article/recommend-list',
      method: 'GET',
      params,
    });
  },

  // 记录浏览
  view: (id) => {
    return request({
      url: `/promotion/cms-article/view/${id}`,
      method: 'POST',
    });
  },

  // 记录分享
  share: (id) => {
    return request({
      url: `/promotion/cms-article/share/${id}`,
      method: 'POST',
    });
  },

  // 点赞/取消点赞
  like: (id) => {
    return request({
      url: `/promotion/cms-article/like/${id}`,
      method: 'POST',
    });
  },

  // 收藏/取消收藏
  collect: (id) => {
    return request({
      url: `/promotion/cms-article/collect/${id}`,
      method: 'POST',
    });
  },

  // 取消收藏
  uncollect: (id) => {
    return request({
      url: `/promotion/cms-article/uncollect/${id}`,
      method: 'POST',
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

  // 获取我的文章列表
  myArticles: (params) => {
    return request({
      url: '/promotion/cms-article/my-articles',
      method: 'GET',
      params,
    });
  },

  // 下载文章附件
  download: (id) => {
    return request({
      url: `/promotion/cms-article/download/${id}`,
      method: 'POST',
    });
  },
};

// 内容流相关API
export const cmsContentStream = {
  // 获取信息流内容列表
  list: (params) => {
    return request({
      url: '/promotion/cms-content/stream/list',
      method: 'GET',
      params,
    });
  },
};

// 导出默认对象（兼容旧版本）
export default {
  ...cmsArticle,
};
