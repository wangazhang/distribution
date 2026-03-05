// 首页API服务

import request from '@/sheep/request'
import mockData from './home-config.json'

// 判断是否使用mock数据
const useMock = false

// 打印环境变量值，便于调试
console.log('首页API - 当前环境是否使用mock数据:', useMock);

// Home API对象
const HomeApi = {
  // // 获取轮播图数据
  // getBannerList: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 首页轮播图');
  //     return Promise.resolve(mockData.bannerList || []);
  //   }
  //   return request({
  //     url: '/home/banner',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取功能导航数据
  // getFunctionList: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 功能导航');
  //     return Promise.resolve(mockData.functionIcons || []);
  //   }
  //   return request({
  //     url: '/home/function',
  //     method: 'GET'
  //   }).then(res => {
  //     // 处理API返回的数据，确保有pagePath属性
  //     if (res && Array.isArray(res)) {
  //       return res.map(item => {
  //         // 如果API返回的数据没有pagePath，则设置为null
  //         if (!item.pagePath) {
  //           console.warn(`功能导航项 ${item.name}(ID: ${item.id}) 没有提供页面路径`);
  //         }
  //         return {
  //           ...item,
  //           pagePath: item.pagePath || null
  //         };
  //       });
  //     }
  //     return res || [];
  //   });
  // },
  
  // 获取内容区块统一数据（包含底部导航和内容区块）
  getContentBlocks: () => {
    if (useMock) {
      console.log('使用mock数据: 内容区块统一数据');
      return Promise.resolve({data:{configContent:JSON.stringify(mockData)}} || []);
    }
    return  request({
      url: '/home/content-blocks/home-config',
      method: 'GET'
    });
  },

  // 以下API保留向后兼容，建议新功能直接使用getContentBlocks
  
  // // 获取底部导航数据 - 已废弃，请使用getContentBlocks
  // getBottomNavItems: () => {
  //   console.warn('警告: getBottomNavItems方法已废弃，请使用getContentBlocks替代');
  //   if (useMock) {
  //     // 从contentBlocks中提取导航数据
  //     return Promise.resolve(
  //       mockData.contentBlocks.map(item => ({
  //         text: item.navText,
  //         id: item.id
  //       })) || []
  //     );
  //   }
  //   return request({
  //     url: '/home/bottomNav',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取活动策划数据
  // getActivityList: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 活动策划');
  //     return Promise.resolve(mockData.activityList || []);
  //   }
  //   return request({
  //     url: '/home/activity',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取ECM胶原产品数据
  // getEcmProducts: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: ECM胶原产品');
  //     return Promise.resolve(mockData.ecmProducts || []);
  //   }
  //   return request({
  //     url: '/home/ecm',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取商学院课程数据
  // getCourseList: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 商学院课程');
  //     return Promise.resolve(mockData.courseList || []);
  //   }
  //   return request({
  //     url: '/home/course',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取直播MCN数据
  // getLiveList: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 直播MCN');
  //     return Promise.resolve(mockData.liveList || []);
  //   }
  //   return request({
  //     url: '/home/live',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取私域运营活动数据
  // getPrivateActivities: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 私域运营活动');
  //     return Promise.resolve(mockData.privateActivities || {});
  //   }
  //   return request({
  //     url: '/home/private-activities',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取商学院课程分类
  // getAcademyPrograms: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 商学院课程分类');
  //     return Promise.resolve(mockData.academyPrograms || []);
  //   }
  //   return request({
  //     url: '/home/academy-programs',
  //     method: 'GET'
  //   });
  // },
  //
  // // 获取流量密码资源
  // getTrafficResources: () => {
  //   if (useMock) {
  //     console.log('使用mock数据: 流量密码资源');
  //     return Promise.resolve(mockData.trafficResources || {});
  //   }
  //   return request({
  //     url: '/home/traffic-resources',
  //     method: 'GET'
  //   });
  // }
};

export default HomeApi;