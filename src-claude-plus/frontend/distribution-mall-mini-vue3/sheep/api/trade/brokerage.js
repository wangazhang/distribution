import request from '@/sheep/request';

const BrokerageApi = {
  // 绑定分销用户
  bindBrokerageUser: (data) => {
    return request({
      url: '/trade/brokerage-user/bind',
      method: 'PUT',
      data,
    });
  },
  // 获得最近提现账户
  getLatestWithdrawAccounts: () => {
    return request({
      url: '/trade/brokerage-withdraw/account/last-list',
      method: 'GET',
    });
  },
  // 获得个人分销信息
  getBrokerageUser: () => {
    return request({
      url: '/trade/brokerage-user/get',
      method: 'GET',
      custom: {
        showLoading: false,
      },
    });
  },
  // 获得个人分销统计
  getBrokerageUserSummary: () => {
    return request({
      url: '/trade/brokerage-user/get-summary',
      method: 'GET',
      custom: {
        showLoading: false,
      },
    });
  },
  // 获得分销记录分页
  getBrokerageRecordPage: (params) => {
    if (params.status === undefined) {
      delete params.status;
    }
    const queryString = Object.keys(params)
      .map((key) => encodeURIComponent(key) + '=' + params[key])
      .join('&');
    return request({
      url: `/trade/brokerage-record/page?${queryString}`,
      method: 'GET',
    });
  },
  // 获得分销记录详情
  getBrokerageRecord: (id) => {
    return request({
      url: '/trade/brokerage-record/get',
      method: 'GET',
      params: { id },
    });
  },
  // 获得分销记录业务详情
  getBrokerageRecordBizDetail: (id) => {
    return request({
      url: '/trade/brokerage-record/mobile/biz-detail',
      method: 'GET',
      params: { id },
    });
  },
  // 获得提现记录分页
  getBrokerageWithdrawPage: params => {
    return request({
      url: '/trade/brokerage-withdraw/page',
      method: 'GET',
      params,
    });
  },
  // 获得提现详情
  getBrokerageWithdraw: id => {
    return request({
      url: '/trade/brokerage-withdraw/get',
      method: 'GET',
      params: { id },
    });
  },
  // 创建分销提现
  createBrokerageWithdraw: (data) => {
    return request({
      url: '/trade/brokerage-withdraw/create',
      method: 'POST',
      data,
    });
  },
  // 获得商品的分销金额
  getProductBrokeragePrice: (spuId) => {
    return request({
      url: '/trade/brokerage-record/get-product-brokerage-price',
      method: 'GET',
      params: { spuId },
    });
  },
  // 获得分销用户排行（基于佣金）
  getRankByPrice: (params) => {
    const queryString = `times=${params.times[0]}&times=${params.times[1]}`;
    return request({
      url: `/trade/brokerage-user/get-rank-by-price?${queryString}`,
      method: 'GET',
    });
  },
  // 获得分销用户排行分页（基于佣金）
  getBrokerageUserChildSummaryPageByPrice: (params) => {
    const queryString = Object.keys(params)
      .map((key) => encodeURIComponent(key) + '=' + params[key])
      .join('&');
    return request({
      url: `/trade/brokerage-user/rank-page-by-price?${queryString}`,
      method: 'GET',
    });
  },
  // 获得分销用户排行分页（基于用户量）
  getBrokerageUserRankPageByUserCount: (params) => {
    const queryString = Object.keys(params)
      .map((key) => encodeURIComponent(key) + '=' + params[key])
      .join('&');
    return request({
      url: `/trade/brokerage-user/rank-page-by-user-count?${queryString}`,
      method: 'GET',
    });
  },
  // 获得下级分销统计分页
  getBrokerageUserChildSummaryPage: (params) => {
    return request({
      url: '/trade/brokerage-user/child-summary-page',
      method: 'GET',
      params,
    });
  },
  // 获取团队成员列表（我的社群）
  getTeamMemberList: (params) => {
    return request({
      url: '/trade/brokerage-user/team-member-list',
      method: 'GET',
      params,
    });
  },
  // 获取团队总览（统计 + 等级选项）
  getTeamOverview: () => {
    return request({
      url: '/trade/brokerage-user/team-overview',
      method: 'GET',
    });
  },
};

export default BrokerageApi;
