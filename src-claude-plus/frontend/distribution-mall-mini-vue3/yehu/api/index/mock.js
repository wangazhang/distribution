// index页mock数据

export default {
  // 首页数据
  indexData: {
    welcome: {
      title: '欢迎回来，美丽达人',
      subtitle: '今天又是美丽的一天',
      background: 'https://cdn.example.com/static/pic/banner/index/1.png'
    },
    quickActions: [
      {
        icon: 'shopping-cart-fill',
        name: '购物车',
        url: '/pages/mall/cart'
      },
      {
        icon: 'star-fill',
        name: '收藏',
        url: '/pages/mine/favorite'
      },
      {
        icon: 'coupon',
        name: '优惠券',
        url: '/pages/mine/coupon'
      },
      {
        icon: 'service',
        name: '在线客服',
        url: '/pages/service/index'
      }
    ],
    notices: [
      {
        id: 'N001',
        title: '新品上市通知',
        content: '新品ECM胶原蛋白精华乳已上市，限时9.5折',
        time: '刚刚'
      },
      {
        id: 'N002',
        title: '订单发货通知',
        content: '您的订单已发货，请注意查收',
        time: '昨天'
      }
    ],
    recommendations: [
      {
        id: 'R001',
        title: '为你推荐',
        products: [
          {
            id: 'P001',
            name: 'ECM胶原蛋白精华液',
            price: '699.00',
            image: 'https://cdn.example.com/static/pic/banner/index/1.png'
          },
          {
            id: 'P002',
            name: '胶原蛋白修复面膜',
            price: '399.00',
            image: 'https://cdn.example.com/static/pic/banner/index/2.png'
          }
        ]
      }
    ]
  },
  
  // 通知消息
  notifications: [
    {
      id: 'N001',
      title: '订单发货通知',
      content: '您的订单ORD20230910003已发货，请注意查收',
      time: '2023-09-11 14:30',
      read: false,
      type: 'order' // order-订单通知 system-系统通知 promotion-促销通知
    },
    {
      id: 'N002',
      title: '新品上市通知',
      content: '新品ECM胶原蛋白精华乳已上市，限时9.5折',
      time: '2023-09-10 10:00',
      read: true,
      type: 'promotion'
    }
  ],
  
  // 应用配置
  appConfig: {
    version: '1.0.0',
    servicePhone: '400-123-4567',
    serviceWorkTime: '9:00-18:00',
    agreementUrl: 'https://example.com/agreement',
    privacyUrl: 'https://example.com/privacy',
    aboutUsUrl: 'https://example.com/about',
    faqUrl: 'https://example.com/faq',
    shareConfig: {
      title: 'ECM胶原蛋白专业护肤品牌',
      desc: '源自深海鱼类的纯净ECM胶原蛋白，深层滋养肌肤',
      link: 'https://example.com/share',
      image: 'https://cdn.example.com/static/pic/share/logo.png'
    }
  },
  
  // 更新信息
  updateInfo: {
    hasUpdate: false,
    version: '1.0.1',
    updateContent: '1.修复已知问题\n2.优化用户体验\n3.新增功能',
    forceUpdate: false,
    downloadUrl: 'https://example.com/download'
  }
} 