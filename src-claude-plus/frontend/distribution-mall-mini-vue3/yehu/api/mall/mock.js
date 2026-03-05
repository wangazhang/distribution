// 商城页mock数据

export default {
  // 轮播图数据
  banners: [
    {
      id: 1,
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      link: '/pages/mall/detail?id=P001'
    },
    {
      id: 2,
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      link: '/pages/mall/detail?id=P002'
    },
    {
      id: 3,
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      link: '/pages/activity/detail?id=A001'
    }
  ],
  
  // 商品分类
  categories: [
    {
      id: 'C001',
      name: '护肤套装',
      icon: 'https://cdn.example.com/static/pic/category/1.png'
    },
    {
      id: 'C002',
      name: '精华面膜',
      icon: 'https://cdn.example.com/static/pic/category/2.png'
    },
    {
      id: 'C003',
      name: '面部精华',
      icon: 'https://cdn.example.com/static/pic/category/3.png'
    },
    {
      id: 'C004',
      name: '眼部护理',
      icon: 'https://cdn.example.com/static/pic/category/4.png'
    },
    {
      id: 'C005',
      name: '乳液面霜',
      icon: 'https://cdn.example.com/static/pic/category/5.png'
    },
    {
      id: 'C006',
      name: '清洁用品',
      icon: 'https://cdn.example.com/static/pic/category/6.png'
    },
    {
      id: 'C007',
      name: '面膜水',
      icon: 'https://cdn.example.com/static/pic/category/7.png'
    },
    {
      id: 'C008',
      name: '防晒隔离',
      icon: 'https://cdn.example.com/static/pic/category/8.png'
    }
  ],
  
  // 热门商品
  hotProducts: [
    {
      id: 'P001',
      name: 'ECM胶原蛋白精华液',
      price: '699.00',
      marketPrice: '899.00',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      saleCount: 1580,
      tag: '热销'
    },
    {
      id: 'P002',
      name: '胶原蛋白修复面膜',
      price: '399.00',
      marketPrice: '499.00',
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      saleCount: 2160,
      tag: '推荐'
    },
    {
      id: 'P003',
      name: '高浓度胶原精华霜',
      price: '899.00',
      marketPrice: '1099.00',
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      saleCount: 986,
      tag: '新品'
    }
  ],
  
  // 新品上市
  newProducts: [
    {
      id: 'P003',
      name: '高浓度胶原精华霜',
      price: '899.00',
      marketPrice: '1099.00',
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      saleCount: 986,
      tag: '新品'
    },
    {
      id: 'P004',
      name: '胶原蛋白眼部精华',
      price: '569.00',
      marketPrice: '699.00',
      image: 'https://cdn.example.com/static/pic/banner/index/4.png',
      saleCount: 756,
      tag: '新品'
    },
    {
      id: 'P005',
      name: 'ECM补水保湿面膜',
      price: '299.00',
      marketPrice: '359.00',
      image: 'https://cdn.example.com/static/pic/banner/index/5.png',
      saleCount: 432,
      tag: '新品'
    }
  ],
  
  // 推荐商品
  recommendProducts: [
    {
      id: 'P002',
      name: '胶原蛋白修复面膜',
      price: '399.00',
      marketPrice: '499.00',
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      saleCount: 2160,
      tag: '推荐'
    },
    {
      id: 'P006',
      name: '多效修护晚霜',
      price: '459.00',
      marketPrice: '599.00',
      image: 'https://cdn.example.com/static/pic/banner/index/6.png',
      saleCount: 890,
      tag: '推荐'
    },
    {
      id: 'P007',
      name: '活颜焕肤精华水',
      price: '359.00',
      marketPrice: '459.00',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      saleCount: 1240,
      tag: '推荐'
    }
  ],
  
  // 商品详情
  productDetail: {
    id: 'P001',
    name: 'ECM胶原蛋白精华液',
    price: '699.00',
    marketPrice: '899.00',
    stock: 999,
    saleCount: 1580,
    tag: '热销',
    categoryId: 'C003',
    categoryName: '面部精华',
    description: '源自深海鱼类的纯净ECM胶原蛋白，深层滋养肌肤，提升紧致度，减少细纹，恢复肌肤弹性与光彩。',
    specifications: '30ml/瓶',
    images: [
      'https://cdn.example.com/static/pic/banner/index/1.png',
      'https://cdn.example.com/static/pic/banner/index/2.png',
      'https://cdn.example.com/static/pic/banner/index/3.png'
    ],
    detailImages: [
      'https://cdn.example.com/static/pic/banner/index/1.png',
      'https://cdn.example.com/static/pic/banner/index/2.png'
    ],
    parameters: [
      {
        name: '品名',
        value: 'ECM胶原蛋白精华液'
      },
      {
        name: '规格',
        value: '30ml'
      },
      {
        name: '成分',
        value: '水、ECM胶原蛋白、甘油、透明质酸钠...'
      },
      {
        name: '保质期',
        value: '36个月'
      },
      {
        name: '适用肤质',
        value: '所有肤质'
      }
    ],
    skus: [
      {
        id: 'SKU001',
        name: '标准版',
        price: '699.00',
        stock: 500
      },
      {
        id: 'SKU002',
        name: '礼盒装',
        price: '899.00',
        stock: 200
      }
    ]
  },
  
  // 商品评价
  productComments: [
    {
      id: 'C001',
      userId: 'U001',
      userName: '张小美',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/1.png',
      content: '使用了一周，皮肤明显改善，很滋润，细纹有所减少，会继续使用的！',
      rating: 5,
      images: [
        'https://cdn.example.com/static/pic/banner/index/1.png'
      ],
      createTime: '2023-09-10 15:30',
      reply: '感谢您的支持，我们将持续提供更好的产品！'
    },
    {
      id: 'C002',
      userId: 'U002',
      userName: '李**',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/2.png',
      content: '物流很快，包装精美，用起来很舒服，味道也很好闻',
      rating: 4,
      images: [],
      createTime: '2023-09-08 09:15',
      reply: ''
    }
  ],
  
  // 搜索结果
  searchResult: [
    {
      id: 'P001',
      name: 'ECM胶原蛋白精华液',
      price: '699.00',
      marketPrice: '899.00',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      saleCount: 1580,
      tag: '热销'
    },
    {
      id: 'P003',
      name: '高浓度胶原精华霜',
      price: '899.00',
      marketPrice: '1099.00',
      image: 'https://cdn.example.com/static/pic/banner/index/3.png',
      saleCount: 986,
      tag: '新品'
    }
  ],
  
  // 购物车列表
  cartList: [
    {
      id: 'CART001',
      productId: 'P001',
      productName: 'ECM胶原蛋白精华液',
      productImage: 'https://cdn.example.com/static/pic/banner/index/1.png',
      price: '699.00',
      quantity: 1,
      selected: true,
      sku: 'SKU001',
      skuName: '标准版'
    },
    {
      id: 'CART002',
      productId: 'P002',
      productName: '胶原蛋白修复面膜',
      productImage: 'https://cdn.example.com/static/pic/banner/index/2.png',
      price: '399.00',
      quantity: 2,
      selected: false,
      sku: '',
      skuName: ''
    }
  ]
} 