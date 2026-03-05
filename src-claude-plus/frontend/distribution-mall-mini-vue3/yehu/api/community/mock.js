// 社区页mock数据

export default {
  // 轮播图数据
  banners: [
    {
      id: 1,
      image: 'https://cdn.example.com/static/pic/banner/community_banner.png',
      link: '/pages/community/topic?id=T001'
    },
    {
      id: 2,
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      link: '/pages/community/topic?id=T002'
    }
  ],
  
  // 动态列表
  posts: [
    {
      id: 'P001',
      userId: 'U001',
      userName: '美妆达人Lisa',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/1.png',
      content: '今天用了ECM胶原蛋白精华液，感觉皮肤瞬间水润了许多，细纹也有所减少，真的很惊喜！大家有没有类似的体验呢？#胶原蛋白 #护肤心得',
      images: [
        'https://cdn.example.com/static/pic/banner/index/1.png',
        'https://cdn.example.com/static/pic/banner/index/2.png'
      ],
      createTime: '2023-09-15 15:30',
      likeCount: 56,
      commentCount: 12,
      favoriteCount: 8,
      isLiked: true,
      isFavorite: false,
      topics: ['胶原蛋白', '护肤心得'],
      productId: 'P001',
      productName: 'ECM胶原蛋白精华液'
    },
    {
      id: 'P002',
      userId: 'U002',
      userName: '皮肤管理专家',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/2.png',
      content: '分享一下我的肌肤管理秘诀：坚持使用含有胶原蛋白的产品，保持良好的作息习惯，每周至少做两次面膜，多补充水分和维生素C。#肌肤管理 #胶原蛋白',
      images: [
        'https://cdn.example.com/static/pic/banner/index/3.png'
      ],
      createTime: '2023-09-14 10:20',
      likeCount: 128,
      commentCount: 32,
      favoriteCount: 15,
      isLiked: false,
      isFavorite: true,
      topics: ['肌肤管理', '胶原蛋白'],
      productId: '',
      productName: ''
    },
    {
      id: 'P003',
      userId: 'U003',
      userName: '小小医美顾问',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/3.png',
      content: '今天参加了ECM胶原蛋白新品发布会，现场体验了新产品，效果确实不错！官方说这款产品采用了最新的ECM提取技术，比传统的胶原蛋白更容易被皮肤吸收。期待上市后给大家带来更多评测！#新品预告 #ECM科技',
      images: [
        'https://cdn.example.com/static/pic/banner/index/4.png',
        'https://cdn.example.com/static/pic/banner/index/5.png',
        'https://cdn.example.com/static/pic/banner/index/6.png'
      ],
      createTime: '2023-09-13 18:45',
      likeCount: 86,
      commentCount: 24,
      favoriteCount: 12,
      isLiked: true,
      isFavorite: true,
      topics: ['新品预告', 'ECM科技'],
      productId: '',
      productName: ''
    }
  ],
  
  // 评论列表
  comments: [
    {
      id: 'C001',
      postId: 'P001',
      userId: 'U004',
      userName: '觅美达人',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/4.png',
      content: '我也在用这款产品，确实很好用，用了一个月感觉皮肤变得更有弹性了',
      createTime: '2023-09-15 16:10',
      likeCount: 5,
      isLiked: false,
      replyTo: ''
    },
    {
      id: 'C002',
      postId: 'P001',
      userId: 'U005',
      userName: '肌肤顾问',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/5.png',
      content: '建议搭配胶原蛋白面膜一起使用，效果会更好',
      createTime: '2023-09-15 16:30',
      likeCount: 8,
      isLiked: true,
      replyTo: 'C001'
    }
  ],
  
  // 话题列表
  topics: [
    {
      id: 'T001',
      name: '胶原蛋白',
      icon: 'https://cdn.example.com/static/pic/topic/1.png',
      postCount: 1256,
      isHot: true
    },
    {
      id: 'T002',
      name: '护肤心得',
      icon: 'https://cdn.example.com/static/pic/topic/2.png',
      postCount: 983,
      isHot: true
    },
    {
      id: 'T003',
      name: '肌肤管理',
      icon: 'https://cdn.example.com/static/pic/topic/3.png',
      postCount: 756,
      isHot: false
    },
    {
      id: 'T004',
      name: '新品预告',
      icon: 'https://cdn.example.com/static/pic/topic/4.png',
      postCount: 432,
      isHot: false
    },
    {
      id: 'T005',
      name: 'ECM科技',
      icon: 'https://cdn.example.com/static/pic/topic/5.png',
      postCount: 321,
      isHot: false
    }
  ],
  
  // 关注用户的动态
  followingPosts: [
    {
      id: 'P004',
      userId: 'U006',
      userName: '美妆博主',
      userAvatar: 'https://cdn.example.com/static/pic/avatar/6.png',
      content: '今天给大家分享我的日常护肤步骤：1.洁面 2.爽肤水 3.精华液 4.面霜 5.防晒。重点是第三步，使用含ECM胶原蛋白的精华，能够深层滋养肌肤，提高保湿效果。#日常护肤 #护肤步骤',
      images: [
        'https://cdn.example.com/static/pic/banner/index/6.png'
      ],
      createTime: '2023-09-12 09:15',
      likeCount: 92,
      commentCount: 18,
      favoriteCount: 7,
      isLiked: true,
      isFavorite: false,
      topics: ['日常护肤', '护肤步骤'],
      productId: '',
      productName: ''
    }
  ],
  
  // 推荐用户
  recommendUsers: [
    {
      id: 'U007',
      name: '护肤达人',
      avatar: 'https://cdn.example.com/static/pic/avatar/7.png',
      signature: '专注于肌肤问题解决方案',
      isFollowed: false,
      fansCount: 2580
    },
    {
      id: 'U008',
      name: '医美顾问Anna',
      avatar: 'https://cdn.example.com/static/pic/avatar/8.png',
      signature: '让每个人都能找到适合自己的美丽方案',
      isFollowed: false,
      fansCount: 1860
    },
    {
      id: 'U009',
      name: '科学护肤KOL',
      avatar: 'https://cdn.example.com/static/pic/avatar/9.png',
      signature: '用科学的方法护肤，揭秘护肤品成分',
      isFollowed: true,
      fansCount: 3240
    }
  ]
} 