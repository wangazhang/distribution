<template>
  <s-layout navbar="normal">
    <view class="points-exchange-container">
      <!-- 自定义导航栏 -->
<!--      <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">-->
<!--        <view class="navbar-content">-->
<!--          <view class="navbar-left" @tap="goBack">-->
<!--            <uni-icons type="back" size="24" color="#333"></uni-icons>-->
<!--          </view>-->
<!--          <view class="navbar-title">积分兑换</view>-->
<!--          <view class="navbar-right">-->
<!--            <uni-icons type="home" size="24" color="#333" @tap="goHome"></uni-icons>-->
<!--          </view>-->
<!--        </view>-->
<!--      </view>-->

      <!-- 页面内容 -->
      <view class="page-content">
        <!-- 积分信息 -->
        <view class="points-info-card">
          <view class="points-header">
            <view class="points-title">我的积分</view>
            <view class="points-action" @tap="goToPointsDetail">积分明细 ></view>
          </view>
          <view class="points-amount">{{ userPoints }}</view>
          <view class="points-tips">积分可兑换商品或优惠券</view>
        </view>

        <!-- 积分任务 -->
        <view class="points-tasks-section">
          <view class="section-title">赚取积分</view>
          <view class="tasks-list">
            <view class="task-item" v-for="(item, index) in tasksList" :key="index" @tap="doTask(item)">
              <view class="task-left">
                <text class="task-name">{{ item.name }}</text>
                <text class="task-desc">{{ item.description }}</text>
              </view>
              <view class="task-right">
                <text class="task-reward">+{{ item.points }}</text>
                <view class="task-btn" :class="{'task-completed': item.completed}">
                  {{ item.completed ? '已完成' : '去完成' }}
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 类别选项卡 -->
        <view class="category-tabs">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index" 
            class="tab-item" 
            :class="{'active': activeTab === index}"
            @tap="changeTab(index)"
          >
            {{ tab.name }}
          </view>
        </view>

        <!-- 兑换商品列表 -->
        <view class="products-section" v-if="activeTab === 0">
          <view class="products-grid">
            <view class="product-item" v-for="(item, index) in productsList" :key="index" @tap="goToProductDetail(item)">
              <image :src="item.image" mode="aspectFill" class="product-image"></image>
              <text class="product-name">{{ item.name }}</text>
              <view class="product-points">{{ item.points }}积分</view>
              <view class="exchange-btn">立即兑换</view>
            </view>
          </view>
        </view>

        <!-- 优惠券列表 -->
        <view class="coupons-section" v-if="activeTab === 1">
          <view class="coupons-list">
            <view class="coupon-item" v-for="(item, index) in couponsList" :key="index" @tap="exchangeCoupon(item)">
              <view class="coupon-left">
                <text class="coupon-amount">¥{{ item.amount }}</text>
                <text class="coupon-condition">满{{ item.minSpend }}元可用</text>
              </view>
              <view class="coupon-center">
                <text class="coupon-name">{{ item.name }}</text>
                <text class="coupon-validity">{{ item.validityPeriod }}</text>
                <text class="coupon-scope">{{ item.scope }}</text>
              </view>
              <view class="coupon-right">
                <text class="coupon-points">{{ item.points }}积分</text>
                <view class="coupon-btn">兑换</view>
              </view>
            </view>
          </view>
        </view>

        <!-- 虚拟商品列表 -->
        <view class="virtual-section" v-if="activeTab === 2">
          <view class="virtual-grid">
            <view class="virtual-item" v-for="(item, index) in virtualItemsList" :key="index" @tap="exchangeVirtualItem(item)">
              <image :src="item.image" mode="aspectFill" class="virtual-image"></image>
              <text class="virtual-name">{{ item.name }}</text>
              <view class="virtual-desc">{{ item.description }}</view>
              <view class="virtual-points">{{ item.points }}积分</view>
              <view class="exchange-btn">立即兑换</view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 状态栏高度
const statusBarHeight = ref(0);

// 用户积分
const userPoints = ref(3650);

// 选项卡
const tabs = ref([
  { name: '实物商品' },
  { name: '优惠券' },
  { name: '虚拟商品' }
]);
const activeTab = ref(0);

// 积分任务列表
const tasksList = ref([
  {
    id: 1,
    name: '每日签到',
    description: '每日签到可获得5积分',
    points: 5,
    completed: false
  },
  {
    id: 2,
    name: '邀请好友',
    description: '成功邀请1位好友可获得50积分',
    points: 50,
    completed: false
  },
  {
    id: 3,
    name: '完善个人信息',
    description: '完善个人信息可获得20积分',
    points: 20,
    completed: true
  },
  {
    id: 4,
    name: '首次购物',
    description: '首次购物可获得100积分',
    points: 100,
    completed: false
  }
]);

// 兑换商品列表
const productsList = ref([
  {
    id: 1,
    name: 'MEETHER 精华液旅行装',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-mini.png',
    points: 500,
    price: '50.00'
  },
  {
    id: 2,
    name: 'MEETHER 面膜礼盒',
    image: 'https://cdn.example.com/static/pic/MEETHER/ecm-mask.png',
    points: 1200,
    price: '128.00'
  },
  {
    id: 3,
    name: '品牌化妆镜',
    image: 'https://cdn.example.com/static/pic/banner/index/1.png',
    points: 800,
    price: '88.00'
  },
  {
    id: 4,
    name: '品牌保温杯',
    image: 'https://cdn.example.com/static/pic/banner/index/2.png',
    points: 1000,
    price: '99.00'
  }
]);

// 优惠券列表
const couponsList = ref([
  {
    id: 1,
    name: '满减优惠券',
    amount: '50',
    minSpend: '300',
    points: 200,
    validityPeriod: '有效期至2024-12-31',
    scope: '全场通用'
  },
  {
    id: 2,
    name: '护肤品类优惠券',
    amount: '100',
    minSpend: '500',
    points: 350,
    validityPeriod: '有效期至2024-12-31',
    scope: '仅限护肤品类'
  },
  {
    id: 3,
    name: '生日特别优惠券',
    amount: '150',
    minSpend: '600',
    points: 500,
    validityPeriod: '有效期至2024-12-31',
    scope: '全场通用'
  }
]);

// 虚拟商品列表
const virtualItemsList = ref([
  {
    id: 1,
    name: 'VIP会员1个月',
    description: '畅享VIP会员专属权益',
    image: 'https://cdn.example.com/static/pic/banner/index/3.png',
    points: 2000
  },
  {
    id: 2,
    name: '线上皮肤咨询',
    description: '专业医师一对一咨询服务',
    image: 'https://cdn.example.com/static/pic/banner/index/4.png',
    points: 3000
  },
  {
    id: 3,
    name: '护肤视频课程',
    description: '专业护肤技巧教学视频',
    image: 'https://cdn.example.com/static/pic/banner/index/5.png',
    points: 1500
  }
]);

// 切换选项卡
const changeTab = (index) => {
  activeTab.value = index;
};

// 跳转到积分明细页面
const goToPointsDetail = () => {
  uni.navigateTo({
    url: '/yehu/pages/mine/points-detail/index'
  });
};

// 返回上一页
const goBack = () => {
  uni.navigateBack();
};

// 返回首页
const goHome = () => {
  uni.switchTab({
    url: '/yehu/pages/home/index'
  });
};

// 执行任务
const doTask = (task) => {
  if (task.completed) {
    uni.showToast({
      title: '任务已完成',
      icon: 'none'
    });
    return;
  }

  // 根据任务ID处理不同的任务
  switch (task.id) {
    case 1: // 每日签到
      signIn(task);
      break;
    case 2: // 邀请好友
      inviteFriend();
      break;
    case 4: // 首次购物
      goShopping();
      break;
    default:
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      });
  }
};

// 签到
const signIn = (task) => {
  uni.showLoading({
    title: '签到中...'
  });

  // 模拟签到请求
  setTimeout(() => {
    uni.hideLoading();
    
    // 更新任务状态和用户积分
    const index = tasksList.value.findIndex(item => item.id === task.id);
    if (index !== -1) {
      tasksList.value[index].completed = true;
      userPoints.value += task.points;
    }

    uni.showToast({
      title: `签到成功，获得${task.points}积分`,
      icon: 'success'
    });
  }, 1000);
};

// 邀请好友
const inviteFriend = () => {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
    success: () => {
      console.log('打开分享菜单成功');
    },
    fail: (err) => {
      console.log('打开分享菜单失败', err);
      uni.showToast({
        title: '分享功能暂不可用',
        icon: 'none'
      });
    }
  });
};

// 去购物
const goShopping = () => {
  uni.switchTab({
    url: '/yehu/pages/mall/index'
  });
};

// 跳转到商品详情
const goToProductDetail = (product) => {
  uni.navigateTo({
    url: `/yehu/pages/points/product-detail/index?id=${product.id}`
  });
};

// 兑换优惠券
const exchangeCoupon = (coupon) => {
  // 检查积分是否足够
  if (userPoints.value < coupon.points) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    });
    return;
  }

  uni.showModal({
    title: '兑换确认',
    content: `确定使用${coupon.points}积分兑换${coupon.name}吗？`,
    success: (res) => {
      if (res.confirm) {
        // 模拟兑换请求
        uni.showLoading({
          title: '兑换中...'
        });
        
        setTimeout(() => {
          uni.hideLoading();
          // 扣除积分
          userPoints.value -= coupon.points;
          
          uni.showToast({
            title: '兑换成功',
            icon: 'success'
          });
        }, 1000);
      }
    }
  });
};

// 兑换虚拟商品
const exchangeVirtualItem = (item) => {
  // 检查积分是否足够
  if (userPoints.value < item.points) {
    uni.showToast({
      title: '积分不足',
      icon: 'none'
    });
    return;
  }

  uni.showModal({
    title: '兑换确认',
    content: `确定使用${item.points}积分兑换${item.name}吗？`,
    success: (res) => {
      if (res.confirm) {
        // 模拟兑换请求
        uni.showLoading({
          title: '兑换中...'
        });
        
        setTimeout(() => {
          uni.hideLoading();
          // 扣除积分
          userPoints.value -= item.points;
          
          uni.showToast({
            title: '兑换成功',
            icon: 'success'
          });
        }, 1000);
      }
    }
  });
};

onMounted(() => {
  // 获取状态栏高度
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0;
    }
  });
});
</script>

<style lang="scss" scoped>
.points-exchange-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #fff;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
  
  .navbar-content {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 15px;
    
    .navbar-left, .navbar-right {
      width: 60px;
      display: flex;
      align-items: center;
    }
    
    .navbar-title {
      flex: 1;
      text-align: center;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }
  }
}

.page-content {
  .points-info-card {
    background: linear-gradient(135deg, #FF7BAC 0%, #FF5777 100%);
    padding: 30rpx;
    color: #fff;
    margin-bottom: 20rpx;
    
    .points-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;
      
      .points-title {
        font-size: 28rpx;
      }
      
      .points-action {
        font-size: 24rpx;
      }
    }
    
    .points-amount {
      font-size: 60rpx;
      font-weight: bold;
      margin-bottom: 10rpx;
    }
    
    .points-tips {
      font-size: 24rpx;
      opacity: 0.8;
    }
  }
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    padding: 30rpx;
    padding-bottom: 20rpx;
  }
  
  .points-tasks-section {
    background-color: #fff;
    margin-bottom: 20rpx;
    
    .tasks-list {
      padding: 0 30rpx 30rpx;
      
      .task-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 1px solid #f5f5f5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .task-left {
          .task-name {
            font-size: 28rpx;
            color: #333;
            display: block;
            margin-bottom: 10rpx;
          }
          
          .task-desc {
            font-size: 24rpx;
            color: #999;
            display: block;
          }
        }
        
        .task-right {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          
          .task-reward {
            font-size: 26rpx;
            color: #FF5777;
            margin-bottom: 10rpx;
          }
          
          .task-btn {
            padding: 6rpx 20rpx;
            background-color: #FF5777;
            color: #fff;
            border-radius: 30rpx;
            font-size: 24rpx;
            
            &.task-completed {
              background-color: #ccc;
            }
          }
        }
      }
    }
  }
  
  .category-tabs {
    display: flex;
    background-color: #fff;
    margin-bottom: 20rpx;
    
    .tab-item {
      flex: 1;
      text-align: center;
      padding: 25rpx 0;
      font-size: 28rpx;
      color: #666;
      position: relative;
      
      &.active {
        color: #FF5777;
        font-weight: 500;
        
        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 40rpx;
          height: 6rpx;
          background-color: #FF5777;
          border-radius: 3rpx;
        }
      }
    }
  }
  
  .products-section, .virtual-section {
    background-color: #fff;
    padding: 20rpx;
    
    .products-grid, .virtual-grid {
      display: flex;
      flex-wrap: wrap;
      
      .product-item, .virtual-item {
        width: 48%;
        margin: 0 1% 20rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        
        .product-image, .virtual-image {
          width: 100%;
          height: 300rpx;
          border-radius: 12rpx;
          margin-bottom: 15rpx;
        }
        
        .product-name, .virtual-name {
          font-size: 28rpx;
          color: #333;
          text-align: center;
          margin-bottom: 10rpx;
        }
        
        .product-points, .virtual-points {
          font-size: 26rpx;
          color: #FF5777;
          margin-bottom: 15rpx;
        }
        
        .virtual-desc {
          font-size: 24rpx;
          color: #666;
          text-align: center;
          margin-bottom: 10rpx;
        }
        
        .exchange-btn {
          width: 80%;
          background-color: #FF5777;
          color: #fff;
          text-align: center;
          padding: 15rpx 0;
          border-radius: 40rpx;
          font-size: 26rpx;
        }
      }
    }
  }
  
  .coupons-section {
    background-color: #fff;
    padding: 20rpx;
    
    .coupons-list {
      .coupon-item {
        display: flex;
        background: linear-gradient(90deg, #FFE8EE 0%, #FFF 100%);
        border-radius: 12rpx;
        margin-bottom: 20rpx;
        overflow: hidden;
        
        .coupon-left {
          background-color: #FF5777;
          color: #fff;
          width: 180rpx;
          padding: 30rpx 20rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          
          .coupon-amount {
            font-size: 40rpx;
            font-weight: bold;
            margin-bottom: 10rpx;
          }
          
          .coupon-condition {
            font-size: 20rpx;
          }
        }
        
        .coupon-center {
          flex: 1;
          padding: 20rpx;
          display: flex;
          flex-direction: column;
          justify-content: center;
          
          .coupon-name {
            font-size: 28rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 10rpx;
          }
          
          .coupon-validity, .coupon-scope {
            font-size: 22rpx;
            color: #999;
            margin-bottom: 5rpx;
          }
        }
        
        .coupon-right {
          width: 120rpx;
          padding: 20rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          
          .coupon-points {
            font-size: 24rpx;
            color: #FF5777;
            margin-bottom: 15rpx;
          }
          
          .coupon-btn {
            padding: 8rpx 20rpx;
            background-color: #FF5777;
            color: #fff;
            border-radius: 30rpx;
            font-size: 24rpx;
          }
        }
      }
    }
  }
}
</style> 