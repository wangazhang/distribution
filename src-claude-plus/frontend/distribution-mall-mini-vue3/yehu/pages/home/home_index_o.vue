<template>
    <view class="page-container">
      <!-- 自定义顶部导航栏 -->
      <view class="custom-navbar" 
        :style="{ 
          paddingTop: statusBarHeight + 'px',
          backgroundColor: `rgba(255, 255, 255, ${navOpacity})`
        }"
      >
        <view class="navbar-content">
          <text class="navbar-title" :style="{
            color: navbarTitleColor,
            textShadow: navbarTitleShadow
          }">Hao · 院长</text>
          <view class="navbar-center">
            <view class="search-box" :style="{ backgroundColor: `rgba(245, 245, 245, ${navOpacity ? 0.8 : 0.4})` }">
              <uni-icons type="search" size="18" color="#999"></uni-icons>
              <text class="search-placeholder">搜好货</text>
            </view>
          </view>
          <view class="navbar-right">
            <text class="more-icon">···</text>
          </view>
        </view>
      </view>
      
      <!-- 页面内容区域 -->
      <view class="page-content" :style="{ paddingTop: (0) + 'px' }">
        <!-- 加载中状态 -->
        <view class="loading-container" v-if="loading">
          <uni-load-more status="loading" :contentText="loadingText"></uni-load-more>
          <text class="loading-text">数据加载中...</text>
        </view>
        
        <!-- 错误状态 -->
        <view class="error-container" v-else-if="error">
          <uni-icons type="error" size="30" color="#FF5777"></uni-icons>
          <text class="error-text">{{errorMessage}}</text>
          <view class="retry-btn" @tap="fetchData">
            <text>重新加载</text>
          </view>
        </view>
        
        <!-- 正常内容区域 -->
        <block v-else>
          <view class="banner-section">
            <swiper
              class="swiper"
              :indicator-dots="true"
              indicator-color="rgba(255, 255, 255, 0.6)"
              indicator-active-color="#fff"
              :autoplay="true"
              :interval="3000"
              :duration="500"
              circular
              style="height: 1000rpx;"
            >
              <swiper-item v-for="(item, index) in bannerList" :key="index" class="swiper-item">
                <image :src="item.image || item" mode="aspectFill" style="width: 100%; height: 100%;"></image>
              </swiper-item>
            </swiper>
          </view>
          
          <!-- 功能图标导航 -->
          <view class="function-nav">
            <view class="function-row">
              <view class="function-item" @tap="navigateToFunction(0)">
                <view class="function-icon-container">
                  <uni-icons type="star" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">限时特卖</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(1)">
                <view class="function-icon-container">
                  <uni-icons type="contact" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">新人专区</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(2)">
                <view class="function-icon-container">
                  <uni-icons type="wallet" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">积分兑换</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(3)">
                <view class="function-icon-container">
                  <uni-icons type="person" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">觅她圈层</text>
              </view>
            </view>
            <view class="function-row">
              <view class="function-item" @tap="navigateToFunction(4)">
                <view class="function-icon-container">
                  <uni-icons type="map" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">美塑疗法</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(5)">
                <view class="function-icon-container">
                  <uni-icons type="heart" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">营养健康</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(6)">
                <view class="function-icon-container">
                  <uni-icons type="home" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">美妆护肤</text>
              </view>
              <view class="function-item" @tap="navigateToFunction(7)">
                <view class="function-icon-container">
                  <uni-icons type="gift" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">尖货惠选</text>
              </view>
            </view>
          </view>
          
          <!-- 底部标签导航 (吸顶) -->
          <view id="nav-anchor" class="bottom-nav-container">
            <view class="bottom-nav" :class="{'sticky': isSticky}" :style="stickyStyle">
              <view class="nav-item" 
                v-for="(item, index) in bottomNavItems" 
                :key="index" 
                :class="{active: index === activeBottomNav}"
                @tap="switchTab(index)"
              >
                <text class="nav-text">{{ item.text }}</text>
              </view>
            </view>
          </view>
          
          <!-- 导航内容区 -->
          <view class="tab-content-area">

            <!-- 活动策划内容 -->
            <view id="content-0" class="tab-content">
              <view class="tab-header">
                <view class="tab-title">活动策划</view>
                <image class="tab-header-image" src="https://cdn.example.com/static/pic/banner/index/2.png" mode="widthFix"></image>
              </view>
              <view class="activity-list">
                <view class="activity-item" v-for="(item, index) in activityList" :key="index">
                  <image :src="item.image" mode="aspectFill" class="activity-image"></image>
                  <view class="activity-info">
                    <text class="activity-title">{{ item.title }}</text>
                    <text class="activity-desc">{{ item.desc }}</text>
                    <view class="activity-meta">
                      <text class="activity-time">{{ item.time }}</text>
                      <text class="activity-location">{{ item.location }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>

            <!-- ECM胶原内容 -->
            <view id="content-1" class="tab-content">
              <view class="tab-header">
                <view class="tab-title">ECM胶原</view>
                <image class="tab-header-image" src="https://cdn.example.com/static/pic/banner/index/1.png" mode="widthFix"></image>
              </view>
              <view class="product-grid">
                <view class="product-item" v-for="(item, index) in ecmProducts" :key="index">
                  <image :src="item.image" mode="aspectFill" class="product-image"></image>
                  <text class="product-name">{{ item.name }}</text>
                  <text class="product-price">¥{{ item.price }}</text>
                </view>
              </view>
            </view>

                        <!-- 商学院内容 -->
             <view id="content-2" class="tab-content">
              <view class="tab-header">
                <view class="tab-title">商学院</view>
                <image class="tab-header-image" src="https://cdn.example.com/static/pic/banner/index/3.png" mode="widthFix"></image>
              </view>
              <view class="course-list">
                <view class="course-item" v-for="(item, index) in courseList" :key="index">
                  <view class="course-top">
                    <image :src="item.image" mode="aspectFill" class="course-image"></image>
                    <view class="course-tag">{{ item.tag }}</view>
                  </view>
                  <view class="course-info">
                    <text class="course-title">{{ item.title }}</text>
                    <text class="course-desc">{{ item.desc }}</text>
                    <view class="course-bottom">
                      <text class="course-price">¥{{ item.price }}</text>
                      <text class="course-students">{{ item.students }}人已学</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
            <!-- 直播MCN内容 -->
            <view id="content-3" class="tab-content">
              <view class="tab-header">
                <view class="tab-title">直播MCN</view>
                <image class="tab-header-image" src="https://cdn.example.com/static/pic/banner/index/4.png" mode="widthFix"></image>
              </view>
              <view class="live-list">
                <view class="live-item" v-for="(item, index) in liveList" :key="index">
                  <view class="live-status">
                    <image :src="item.avatar" mode="aspectFill" class="live-avatar"></image>
                    <view class="live-info">
                      <text class="live-name">{{ item.name }}</text>
                      <text class="live-viewers">{{ item.viewers }}人观看</text>
                    </view>
                    <view :class="['live-tag', item.isLive ? 'live' : '']">
                      {{ item.isLive ? '直播中' : '未开播' }}
                    </view>
                  </view>
                  <image :src="item.cover" mode="aspectFill" class="live-cover"></image>
                  <text class="live-title">{{ item.title }}</text>
                </view>
              </view>
            </view>
          </view>
        </block>
      </view>
      
      <!-- 自定义底部导航栏 -->
      <yh-tabbar activeColor="#FFA0B4" color="#999999"></yh-tabbar>
    </view>
  </template>
  
  <script setup >
  import { ref, onMounted, nextTick, computed, onBeforeUnmount, getCurrentInstance } from 'vue';
  import YhTabbar from '@/yehu/components/yh/components/yh-tabbar/yh-tabbar.vue';
  import { 
    getBannerList, 
    getFunctionList, 
    getBottomNavItems, 
    getActivityList, 
    getEcmProducts, 
    getCourseList, 
    getLiveList 
  } from '@/yehu/api/home';
  
  // 状态栏高度
  const statusBarHeight = ref(0);
  // 胶囊按钮信息
  const menuButtonInfo = ref({});
  // 是否吸顶
  const isSticky = ref(false);
  
  // 导航栏背景透明度
  const navOpacity = ref(0);
  
  // loading文本配置
  const loadingText = {
    contentdown: "上拉显示更多",
    contentrefresh: "正在加载...",
    contentnomore: "没有更多数据了"
  };
  
  // 导航栏标题文字颜色，计算为背景色的反色
  const navbarTitleColor = computed(() => {
    // 背景色随着navOpacity从透明变为白色
    // 当navOpacity为0时为白色(#FFFFFF)，为1时为黑色(#333333)
    // 计算中间过渡的颜色
    const r = Math.round(255 - (255 - 51) * navOpacity.value);
    const g = Math.round(255 - (255 - 51) * navOpacity.value);
    const b = Math.round(255 - (255 - 51) * navOpacity.value);
    return `rgb(${r}, ${g}, ${b})`;
  });
  
  const navbarTitleShadow = computed(() => {
    // 当navOpacity为0时显示阴影，为1时不显示阴影
    return navOpacity.value < 0.5 ? '0 1px 2px rgba(0, 0, 0, 0.2)' : 'none';
  });
  
  // 吸顶样式
  const stickyStyle = computed(() => {
    if (!isSticky.value) return {};
    
    // 计算胶囊底部到状态栏底部的距离
    const capsuleHeight = menuButtonInfo.value.height || 0;
    const capsuleTop = menuButtonInfo.value.top || 0;
    const paddingTop = capsuleTop + capsuleHeight - statusBarHeight.value;
    
    return { 
      paddingTop: `${paddingTop}px`,
      top: `${statusBarHeight.value}px`
    };
  });
  
  // 数据ref定义
  const bannerList = ref([]);
  const functionList1 = ref([]);
  const functionList2 = ref([]);
  const bottomNavItems = ref([]);
  const activityList = ref([]);
  const ecmProducts = ref([]);
  const courseList = ref([]);
  const liveList = ref([]);
  
  // 当前活跃的底部导航
  const activeBottomNav = ref(0);
  
  // 存储各内容区域位置信息
  const contentPositions = ref([]);
  
  // 获取组件实例
  const instance = getCurrentInstance();
  
  // 加载状态
  const loading = ref(true);
  const error = ref(false);
  const errorMessage = ref('数据加载失败，请重试');
  
  // 功能导航处理方法
  const navigateToFunction = (index) => {
    console.log('点击功能导航:', index);
    
    // 根据索引执行不同的导航逻辑
    const functionPages = [
      '/yehu/pages/special/time-limited', // 限时特卖
      '/yehu/pages/user/newcomer',        // 新人专区
      '/yehu/pages/points/exchange',      // 积分兑换
      '/yehu/pages/community/index',      // 觅她圈层
      '/yehu/pages/beauty/sculpting',     // 美塑疗法
      '/yehu/pages/health/nutrition',     // 营养健康
      '/yehu/pages/beauty/skincare',      // 美妆护肤
      '/yehu/pages/special/selected',     // 尖货惠选
    ];
    
    // 触发短震动反馈
    uni.vibrateShort({
      success: () => {
        console.log('功能导航震动成功');
      }
    });
    
    // 导航到对应页面
    uni.navigateTo({
      url: functionPages[index],
      fail: (err) => {
        console.error('导航失败', err);
        uni.showToast({
          title: '该功能正在开发中',
          icon: 'none'
        });
      }
    });
  };
  
  // 获取数据方法
  const fetchData = async () => {
    loading.value = true;
    error.value = false;
    
    try {
      // 并行请求所有数据
      const [
        bannerData,
        functionData,
        navData,
        activityData,
        ecmData,
        courseData,
        liveData
      ] = await Promise.all([
        getBannerList(),
        getFunctionList(),
        getBottomNavItems(),
        getActivityList(),
        getEcmProducts(),
        getCourseList(),
        getLiveList()
      ]);
      
      // 设置数据
      bannerList.value = bannerData;
      functionList1.value = functionData.functionList1;
      functionList2.value = functionData.functionList2;
      bottomNavItems.value = navData;
      activityList.value = activityData;
      ecmProducts.value = ecmData;
      courseList.value = courseData;
      liveList.value = liveData;
      
      console.log('首页数据加载完成');
      
      // 在页面布局完成后获取元素位置
      nextTick(() => {
        setTimeout(() => {
          updatePositions();
        }, 500);
      });
    } catch (err) {
      console.error('获取首页数据失败', err);
      error.value = true;
      errorMessage.value = err.message || '数据加载失败，请重试';
    } finally {
      loading.value = false;
    }
  };
  
  // 修改switchTab函数，调整DOM查询
  const switchTab = (index) => {
    if (activeBottomNav.value === index) return;
    
    // 触发短震动反馈
    uni.vibrateShort({
      success: () => {
        console.log('内部标签切换震动成功');
      },
      fail: (err) => {
        console.log('内部标签切换震动失败', err);
      }
    });
    
    activeBottomNav.value = index;
    
    console.log('点击标签:', index, bottomNavItems.value[index].text);
    
    // 延迟执行，确保UI更新完成
    setTimeout(() => {
      // 查询目标内容区域位置
      uni.createSelectorQuery()
        .in(instance?.proxy) // 使用组件实例而不是页面实例
        .select(`#content-${index}`)
        .boundingClientRect((rect) => {
          if (!rect) {
            console.log('找不到目标内容区域');
            return;
          }
          
          console.log('目标内容区域信息:', rect);
          
          // 查询视口信息获取当前滚动位置
          uni.createSelectorQuery()
            .selectViewport()
            .scrollOffset((res) => {
              if (!res) {
                console.log('无法获取视口信息');
                return;
              }
              
              // 获取当前滚动位置
              const currentScrollTop = res.scrollTop;
              console.log('当前页面滚动位置:', currentScrollTop);
              
              // 计算需要滚动的目标位置
              // 内容区域相对于页面顶部的绝对位置 = 当前滚动位置 + 内容区域相对于视口顶部的位置
              const absoluteTop = currentScrollTop + rect.top;
              
              // 计算最终滚动位置：考虑导航栏高度
              const navHeight = 160; // 预留导航栏高度，根据实际情况调整
              const targetScrollTop = absoluteTop - navHeight;
              
              console.log('内容区域绝对位置:', absoluteTop);
              console.log('导航高度偏移:', navHeight);
              console.log('最终滚动目标位置:', targetScrollTop);
              
              // 执行滚动
              uni.pageScrollTo({
                scrollTop: targetScrollTop,
                duration: 300
              });
            })
            .exec();
        })
        .exec();
    }, 100);
  };
  
  // 保存nav-anchor的top值
  const navTopValue = ref(0);
  
  // 处理页面滚动函数
  const handlePageScroll = (e) => {
    const scrollTop = e.scrollTop;
    
    // 导航栏背景色渐变
    navOpacity.value = Math.min(scrollTop / 200, 1);
    
    // 判断是否吸顶
    const capsuleBottom = menuButtonInfo.value.top + menuButtonInfo.value.height;
    isSticky.value = scrollTop >= navTopValue.value - capsuleBottom;
    
    // 更新活跃标签
    uni.createSelectorQuery()
      .in(instance?.proxy) // 使用组件实例而不是页面实例
      .selectAll('.tab-content')
      .boundingClientRect((rects) => {
        if (!rects || rects.length === 0) {
          console.log('无法获取内容区域位置');
          return;
        }
        
        // 获取当前滚动位置和视口高度
        uni.createSelectorQuery()
          .selectViewport()
          .scrollOffset((res) => {
            if (!res) return;
            
            const currentScrollTop = res.scrollTop;
            const viewportHeight = uni.getSystemInfoSync().windowHeight;
            
            // 视口顶部偏移量 - 考虑吸顶导航栏的高度
            const viewportOffset = isSticky.value ? 160 : 0;
            
            // 日志：当前滚动状态
            console.log('当前滚动位置:', currentScrollTop);
            console.log('视口高度:', viewportHeight);
            console.log('视口顶部偏移:', viewportOffset);
            
            // 为每个内容区域计算可见度
            let bestIndex = 0;
            let bestVisibility = -Infinity;
            
            rects.forEach((rect, i) => {
              // 计算内容区域相对视口的位置
              const relativeTop = rect.top;
              const relativeBottom = rect.bottom;
              
              // 计算可见度分数
              const visibility = calculateVisibility(relativeTop, relativeBottom, viewportOffset, viewportHeight);
              
              // console.log(`内容区${i}:`,
              //   `位置=${relativeTop}~${relativeBottom}`,
              //   `可见度=${visibility.toFixed(2)}`);
              
              // 更新最佳可见内容区域
              if (visibility > bestVisibility) {
                bestVisibility = visibility;
                bestIndex = i;
              }
            });
            
            console.log('选中的活跃标签:', bestIndex, bottomNavItems.value[bestIndex].text);
            
            // 更新活跃标签
            if (activeBottomNav.value !== bestIndex) {
              activeBottomNav.value = bestIndex;
            }
          })
          .exec();
      })
      .exec();
  };
  
  // 计算元素在视口中的可见度
  const calculateVisibility = (top, bottom, viewportOffset, viewportHeight) => {
    const viewportTop = viewportOffset;
    const viewportBottom = viewportHeight;
    
    // 完全在视口上方
    if (bottom < viewportTop) return -9999 - top;
    
    // 完全在视口下方
    if (top > viewportBottom) return -9999 - (top - viewportBottom);
    
    // 元素顶部刚好在视口内前100px范围（最高优先级）
    if (top >= viewportTop && top <= viewportTop + 100) {
      return 10000 - Math.abs(top - viewportTop);
    }
    
    // 元素显著部分在视口内（中优先级）
    const visibleHeight = Math.min(bottom, viewportBottom) - Math.max(top, viewportTop);
    const elementHeight = bottom - top;
    const visibilityRatio = visibleHeight / elementHeight;
    
    // 可见比例 * 1000 + 靠近顶部的奖励
    return visibilityRatio * 1000 + (1000 - Math.min(Math.max(top - viewportTop, 0), 1000));
  };
  
  // 更新各元素位置的函数
  const updatePositions = () => {
    const query = uni.createSelectorQuery().in(instance?.proxy);
    
    // 获取导航锚点位置
    query.select('#nav-anchor').boundingClientRect((data) => {
      if (data) {
        navTopValue.value = data.top;
        console.log('导航锚点位置:', data.top);
      }
    }).exec();
    
    // 获取各内容区域位置
    contentPositions.value = [];
    for (let i = 0; i < bottomNavItems.value.length; i++) {
      const contentQuery = uni.createSelectorQuery().in(instance?.proxy);
      contentQuery.select(`#content-${i}`).boundingClientRect((data) => {
        if (data) {
          contentPositions.value[i] = { 
            index: i, 
            top: data.top,
            height: data.height 
          };
          // console.log(`内容区${i}位置:`, contentPositions.value[i]);
        }
      }).exec();
    }
  };
  
  // 检查并创建静态资源
  const checkAndCreateStaticResources = () => {
    console.log('确保静态资源存在');
  };
  
  onMounted(() => {
    // 获取状态栏高度和胶囊按钮信息
    uni.getSystemInfo({
      success: (res) => {
        statusBarHeight.value = res.statusBarHeight || 0;
        
        // 获取胶囊按钮位置信息
        // @ts-ignore
        menuButtonInfo.value = uni.getMenuButtonBoundingClientRect();
        console.log('胶囊按钮信息:', menuButtonInfo.value);
      }
    });
    
    // 获取首页数据
    fetchData();
    
    // 确保静态资源存在
    checkAndCreateStaticResources();
  });
  </script>
  
  <style lang="scss" scoped>
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
  }
  
  .custom-navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 999;
    background-color: transparent;
    
    .navbar-content {
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20rpx;
      
      .navbar-title {
        font-size: 34rpx;
        font-weight: 500;
        transition: all 0.3s;
        flex: 1;
      }
  
      .navbar-center {
        flex: 2;
        display: flex;
        justify-content: center;
        
        .search-box {
          display: flex;
          align-items: center;
          background-color: rgba(245, 245, 245, 0.8);
          border-radius: 30rpx;
          padding: 6rpx 20rpx;
          width: 80%;
          
          .search-placeholder {
            font-size: 26rpx;
            color: #999;
            margin-left: 8rpx;
          }
        }
      }
      
      .navbar-right {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        flex: 1;
        
        .more-icon {
          font-size: 40rpx;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }
  
  .page-content {
    padding-bottom: 140rpx;
    
    .banner-section {
      padding: 0rpx 0rpx 20rpx 0rpx;
    }
  
    .brand-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 30rpx 0;
      
      .brand-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 16rpx;
      }
      
      .brand-slogan {
        margin-bottom: 10rpx;
        
        .slogan-text {
          font-size: 40rpx;
          font-weight: bold;
          color: #000;
        }
      }
      
      .brand-subtitle {
        font-size: 26rpx;
        color: #666;
      }
    }
    
    .function-nav {
      background-color: #ffffff;
      border-radius: 20rpx 20rpx 0 0;
      margin: 0 20rpx;
      padding: 30rpx 20rpx;
      
      .function-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 60rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .function-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          width: 25%;
          
          .function-icon-container {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 90rpx;
            height: 80rpx;
            background-color: transparent;
            border-radius: 0;
            margin-bottom: 0;
          }
          
          .function-text {
            font-size: 26rpx;
            color: #000000;
            margin-top: 0;
            line-height: 1;
          }
        }
      }
    }
    
    .bottom-nav-container {
      position: relative;
      z-index: 10;
    }
  
    .bottom-nav {
      display: flex;
      justify-content: space-around;
      align-items: center;
      background-color: #ffffff;
      padding: 20rpx 0;
      border-radius: 20rpx 20rpx 0 0;
      margin: 0 20rpx;
      transition: all 0.3s;
      
      &.sticky {
        position: fixed;
        left: 0;
        right: 0;
        margin: 0;
        padding-bottom: 10rpx;
        border-radius: 0;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        z-index: 999;
      }
      
      .nav-item {
        position: relative;
        padding: 10rpx 0;
        
        &.active {
          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 40rpx;
            height: 6rpx;
            background-color: #8F1911;
            border-radius: 3rpx;
          }
        }
        
        .nav-text {
          font-size: 32rpx;
          color: #333;
        }
        
        &.active .nav-text {
          font-weight: bold;
          color: #8F1911;
        }
      }
    }
  }
  
  .tab-content-area {
    background-color: #fff;
    margin: 0 0 20rpx 0;
    border-radius: 0 0 20rpx 20rpx;
    overflow: hidden;
  }
  
  .tab-content {
    padding: 30rpx 0;
    
    .tab-header {
      margin-bottom: 30rpx;
      padding: 0;
      overflow: hidden;
      height: 200rpx;
      position: relative;
      
      .tab-title {
        position: absolute;
        top: 20rpx;
        left: 30rpx;
        z-index: 5;
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
        background: rgba(0, 0, 0, 0.2);
        padding: 8rpx 20rpx;
        border-radius: 6rpx;
      }
      
      .tab-header-image {
        width: 100%;
        height: 300rpx;
        border-radius: 0;
        object-fit: cover;
        display: block;
      }
    }
  }
  
  // 活动列表样式
  .activity-list {
    padding: 0 20rpx;
    
    .activity-item {
      margin-bottom: 30rpx;
      background-color: #fff;
      border-radius: 12rpx;
      overflow: hidden;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      
      .activity-image {
        width: 100%;
        height: 300rpx;
      }
      
      .activity-info {
        padding: 20rpx;
        
        .activity-title {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 10rpx;
          display: block;
        }
        
        .activity-desc {
          font-size: 24rpx;
          color: #666;
          margin-bottom: 15rpx;
          display: block;
        }
        
        .activity-meta {
          display: flex;
          justify-content: space-between;
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
  
  // 产品网格样式
  .product-grid {
    padding: 0 20rpx;
    display: flex;
    flex-wrap: wrap;
    margin: 0 -10rpx;
    
    .product-item {
      width: calc(50% - 20rpx);
      margin: 0 10rpx 20rpx;
      
      .product-image {
        width: 100%;
        height: 300rpx;
        border-radius: 12rpx;
        margin-bottom: 10rpx;
      }
      
      .product-name {
        font-size: 26rpx;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
      }
      
      .product-price {
        font-size: 28rpx;
        font-weight: bold;
        color: #FF5777;
      }
    }
  }
  
  // 课程列表样式
  .course-list {
    padding: 0 20rpx;
    
    .course-item {
      margin-bottom: 30rpx;
      background-color: #fff;
      border-radius: 12rpx;
      overflow: hidden;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      
      .course-top {
        position: relative;
        
        .course-image {
          width: 100%;
          height: 300rpx;
        }
        
        .course-tag {
          position: absolute;
          top: 16rpx;
          right: 16rpx;
          background-color: #FF5777;
          color: #fff;
          font-size: 22rpx;
          padding: 4rpx 12rpx;
          border-radius: 6rpx;
        }
      }
      
      .course-info {
        padding: 20rpx;
        
        .course-title {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 10rpx;
          display: block;
        }
        
        .course-desc {
          font-size: 24rpx;
          color: #666;
          margin-bottom: 15rpx;
          display: block;
        }
        
        .course-bottom {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .course-price {
            font-size: 28rpx;
            font-weight: bold;
            color: #FF5777;
          }
          
          .course-students {
            font-size: 22rpx;
            color: #999;
          }
        }
      }
    }
  }
  
  // 直播列表样式
  .live-list {
    padding: 0 20rpx;
    
    .live-item {
      margin-bottom: 30rpx;
      
      .live-status {
        display: flex;
        align-items: center;
        margin-bottom: 15rpx;
        
        .live-avatar {
          width: 70rpx;
          height: 70rpx;
          border-radius: 50%;
          margin-right: 15rpx;
        }
        
        .live-info {
          flex: 1;
          
          .live-name {
            font-size: 26rpx;
            font-weight: 500;
            color: #333;
            display: block;
          }
          
          .live-viewers {
            font-size: 22rpx;
            color: #999;
          }
        }
        
        .live-tag {
          font-size: 22rpx;
          padding: 4rpx 12rpx;
          border-radius: 6rpx;
          background-color: #999;
          color: #fff;
          
          &.live {
            background-color: #FF5777;
          }
        }
      }
      
      .live-cover {
        width: 100%;
        height: 350rpx;
        border-radius: 12rpx;
        margin-bottom: 10rpx;
      }
      
      .live-title {
        font-size: 26rpx;
        color: #333;
        display: block;
      }
    }
  }
  
  // 加载和错误状态样式
  .loading-container, .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;
    
    .loading-text, .error-text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }
    
    .retry-btn {
      margin-top: 30rpx;
      padding: 16rpx 40rpx;
      background-color: #FF5777;
      color: #fff;
      border-radius: 30rpx;
      font-size: 28rpx;
    }
  }
  </style>