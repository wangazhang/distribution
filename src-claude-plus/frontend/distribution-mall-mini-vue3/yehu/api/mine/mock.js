// 个人中心页mock数据

export default {
  // 用户基本信息
  userInfo: {
    id: 10086,
    nickname: '张小美',
    avatar: 'https://cdn.example.com/static/pic/avatar/avatar_1.png',
    phone: '138****8888',
    gender: 1, // 0未知 1女 2男
    level: 2,
    levelName: '黄金会员',
    points: 2580,
    vip: true,
    signature: '美丽从每一天开始',
    certificationStatus: 1, // 0未认证 1已认证
    background: 'https://cdn.example.com/static/pic/banner/index/1.png'
  },
  
  // 用户统计数据
  userStats: {
    waitPay: 2,  // 待付款
    waitShip: 3, // 待发货
    waitReceive: 1, // 待收货
    waitComment: 5, // 待评价
    coupons: 8,  // 优惠券
    favorites: 12, // 收藏
    history: 36, // 浏览记录
    notifications: 3 // 未读消息
  },
  
  // 订单列表
  orderList: [
    {
      id: 'ORD20230915001',
      status: 1, // 0待付款 1待发货 2待收货 3待评价 4已完成 5已取消
      statusText: '待发货',
      createTime: '2023-09-15 15:30',
      totalAmount: '699.00',
      products: [
        {
          id: 'P001',
          name: 'ECM胶原蛋白精华液',
          price: '699.00',
          image: 'https://cdn.example.com/static/pic/banner/index/1.png',
          quantity: 1
        }
      ]
    },
    {
      id: 'ORD20230912002',
      status: 0,
      statusText: '待付款',
      createTime: '2023-09-12 10:20',
      totalAmount: '1298.00',
      products: [
        {
          id: 'P002',
          name: '胶原蛋白修复面膜',
          price: '399.00',
          image: 'https://cdn.example.com/static/pic/banner/index/2.png',
          quantity: 2
        },
        {
          id: 'P003',
          name: '高浓度胶原精华霜',
          price: '500.00',
          image: 'https://cdn.example.com/static/pic/banner/index/3.png',
          quantity: 1
        }
      ]
    }
  ],
  
  // 收藏列表
  favoriteList: [
    {
      id: 'P001',
      name: 'ECM胶原蛋白精华液',
      price: '699.00',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      saleCount: 1580
    },
    {
      id: 'P002',
      name: '胶原蛋白修复面膜',
      price: '399.00',
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      saleCount: 2160
    },
    {
      id: 'P003',
      name: '高浓度胶原精华霜',
      price: '899.00',
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      saleCount: 986
    }
  ],
  
  // 消息通知列表
  notificationList: [
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
    },
    {
      id: 'N003',
      title: '系统维护通知',
      content: '系统将于9月15日凌晨2:00-4:00进行维护升级',
      time: '2023-09-08 18:00',
      read: true,
      type: 'system'
    }
  ],
  
  // 地址列表
  addressList: [
    {
      id: 'A001',
      name: '张小美',
      phone: '13812345678',
      province: '上海市',
      city: '上海市',
      district: '浦东新区',
      address: '张江高科技园区科苑路88号',
      isDefault: true
    },
    {
      id: 'A002',
      name: '张小美',
      phone: '13812345678',
      province: '上海市',
      city: '上海市',
      district: '静安区',
      address: '南京西路1266号恒隆广场46楼',
      isDefault: false
    }
  ]
} 