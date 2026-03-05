<template>
<!--  <s-layout navbar="home">-->
    <view class="page-container">
      <!-- 自定义顶部导航栏 -->
      <view class="custom-navbar"
        :style="{
          paddingTop: statusBarHeight + 'px',
          backgroundColor: `rgba(255, 255, 255, ${navOpacity})`
        }"
      >
        <view class="navbar-content">
          <view class="navbar-title" :style="{
            color: navbarTitleColor,
            textShadow: navbarTitleShadow
          }">
            <span class="title-en">MeetHer</span>
            <span class="title-zh">·觅她</span>
          </view>
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
                <image :src="item.imageUrl || item" mode="aspectFill" style="width: 100%; height: 100%;"></image>
              </swiper-item>
            </swiper>
          </view>

          <!-- 功能图标导航 -->
          <view class="function-nav">
            <view class="function-row">
              <view class="function-item" v-for="(item, index) in functionIcons.slice(0, 4)" :key="index" @tap="navigateToFunction(item.id)">
                <view class="function-icon-container">
                  <uni-icons :type="item.icon" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">{{item.name}}</text>
              </view>
            </view>
            <view class="function-row">
              <view class="function-item" v-for="(item, index) in functionIcons.slice(4, 8)" :key="index" @tap="navigateToFunction(item.id)">
                <view class="function-icon-container">
                  <uni-icons :type="item.icon" size="40" color="#000000"></uni-icons>
                </view>
                <text class="function-text">{{item.name}}</text>
              </view>
            </view>
          </view>

          <!-- 底部标签导航 (吸顶) -->
          <view id="nav-anchor" class="bottom-nav-container">
            <view class="bottom-nav" :class="{'sticky': isSticky}" :style="stickyStyle">
              <view class="nav-item"
                v-for="(item, index) in sections"
                :key="index"
                :class="{active: index === activeBottomNav}"
                @tap="switchTab(index)"
              >
                <text class="nav-text">{{ item.navText }}</text>
              </view>
            </view>
          </view>

          <!-- 导航内容区 -->
          <view class="tab-content-area">
            <!-- 平铺所有内容区块，不使用v-show控制显示隐藏 -->
            <yh-content-block
              v-for="(block, index) in sections"
              :key="index"
              :id="`content-${index}`"
              :title="block.title"
              :header-image="block.headerImage"
              :content-type="block.type"
              :content-data="block.data"
              @item-click="recordContentInteraction"
            ></yh-content-block>
          </view>
        </block>
      </view>

      <!-- 自定义底部导航栏 -->
<!--      <yh-tabbar activeColor="#FFA0B4" color="#999999"></yh-tabbar>-->
    </view>
<!--  </s-layout>-->
  </template>

  <script setup >
  import { ref, onMounted, nextTick, computed, onBeforeUnmount, getCurrentInstance, reactive } from 'vue';
  import YhTabbar from '@/yehu/components/yh/components/yh-tabbar/yh-tabbar.vue';
  import YhContentBlock from '@/yehu/components/yh/components/yh-content-block/yh-content-block.vue';
  import HomeApi from '@/yehu/api/home';

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
  const contentBlocks = ref([]); // 首页配置统一存放点
  const bannerList = ref([]);
  const functionIcons = ref([]);
  const sections = ref([]); // 导航

  // 当前活跃的底部导航
  const activeBottomNav = ref(0);

  // 添加标签锁定功能，防止抖动
  const isTabLocked = ref(false);
  const tabLockTimeout = ref(null);

  // 锁定标签一段时间
  const lockTab = (duration = 1000) => {
    isTabLocked.value = true;

    // 清除之前的定时器
    if (tabLockTimeout.value) {
      clearTimeout(tabLockTimeout.value);
    }

    // 设置新的定时器
    tabLockTimeout.value = setTimeout(() => {
      isTabLocked.value = false;
    }, duration);
  };

  // 解锁标签
  const unlockTab = () => {
    isTabLocked.value = false;
    if (tabLockTimeout.value) {
      clearTimeout(tabLockTimeout.value);
      tabLockTimeout.value = null;
    }
  };

  // 存储各内容区域位置信息
  const contentPositions = ref([]);

  // 获取组件实例
  const instance = getCurrentInstance();

  // 加载状态
  const loading = ref(true);
  const error = ref(false);
  const errorMessage = ref('数据加载失败，请重试');

  // 页面数据
  const data = reactive({
    // ... existing code ...
    academyPrograms: [],
    trafficResources: { originalIp: [], trafficSecrets: [] },
    // ... existing code ...
  });

  // 功能导航处理方法
  const navigateToFunction = (id) => {
    console.log('点击功能导航:', id);

    // 找到对应的功能项
    const functionItem = functionIcons.value.find(item => item.id === id);
    if (!functionItem) {
      console.error('未找到对应的功能项:', id);
      return;
    }

    console.log('功能项:', functionItem);

    // 触发短震动反馈
    uni.vibrateShort({
      success: () => {
        console.log('功能导航震动成功');
      }
    });

    // 使用功能项中的pagePath属性进行导航
    if (functionItem.pagePath) {
      uni.navigateTo({
        url: functionItem.pagePath,
        fail: (err) => {
          console.error('导航失败', err);
          uni.showToast({
            title: '该功能正在开发中',
            icon: 'none'
          });
        }
      });
    } else {
      console.log('功能项没有提供页面路径，显示开发中提示');
      uni.showToast({
        title: '该功能正在开发中',
        icon: 'none'
      });
    }
  };

  // 记录内容交互事件（仅用于统计分析）
  const recordContentInteraction = (eventData) => {
    console.log('内容交互记录:', eventData);
    // 在这里可以添加统计代码，如埋点、用户行为记录等
  };

  // 更新各元素位置的函数
  const updatePositions = () => {
    console.log('开始更新内容区域位置');
    // 确保contentBlocks数据已加载
    if (!sections.value || sections.value.length === 0) {
      console.log('sections数据尚未加载，无法获取位置');
      return;
    }

    // 重要：先通过类选择器查找所有内容区块
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .selectAll('.content-block')  // 通过类名查找所有内容区块
      .boundingClientRect(rects => {
        if (!rects || rects.length === 0) {
          console.log('无法通过类选择器获取内容区域');
          // 尝试通过ID查找
          getContentByIds();
          return;
        }

        console.log(`找到${rects.length}个内容区域`, rects);
        processRects(rects);
      })
      .exec();
  };

  // 通过ID查找内容区块
  const getContentByIds = () => {
    console.log('尝试通过ID查找内容区块');
    contentPositions.value = [];

    // 获取导航锚点位置
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .select('#nav-anchor')
      .boundingClientRect((data) => {
        if (data) {
          navTopValue.value = data.top;
          console.log('导航锚点位置:', data.top);
        } else {
          console.log('无法获取导航锚点位置');
        }
      })
      .exec();

    // 逐个获取内容区域位置
    for (let i = 0; i < sections.value.length; i++) {
      uni.createSelectorQuery()
        .in(instance?.proxy)
        .select(`#content-${i}`)
        .boundingClientRect((data) => {
          if (data) {
            contentPositions.value[i] = {
              index: i,
              top: data.top,
              height: data.height
            };
            console.log(`内容区${i}位置:`, contentPositions.value[i]);
          } else {
            console.log(`无法获取内容区${i}的位置信息，ID=#content-${i}`);
          }
        })
        .exec();
    }
  };

  // 处理获取到的内容区域位置
  const processRects = (rects) => {
    contentPositions.value = [];

    // 获取导航锚点位置
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .select('#nav-anchor')
      .boundingClientRect((data) => {
        if (data) {
          navTopValue.value = data.top;
          console.log('导航锚点位置:', data.top);
        } else {
          console.log('无法获取导航锚点位置');
        }
      })
      .exec();

    // 处理所有内容区域位置
    rects.forEach((rect, i) => {
      if (i < sections.value.length) {
        contentPositions.value[i] = {
          index: i,
          top: rect.top,
          height: rect.height
        };
        console.log(`内容区${i}位置:`, contentPositions.value[i]);
      }
    });
  };

  // 页面生命周期函数，处理下拉刷新
  const onPullDownRefresh = () => {
    console.log('触发下拉刷新');
    // 重新加载数据
    fetchData().then(() => {
      // 数据加载完成后停止下拉刷新动画
      uni.stopPullDownRefresh();
    }).catch((err) => {
      console.error('下拉刷新加载数据失败', err);
      // 即使失败也停止下拉刷新动画
      uni.stopPullDownRefresh();
    });
  };

  // 获取数据方法
  const fetchData = async () => {
    loading.value = true;
    error.value = false;

    try {
      // 并行请求所有数据
      const [
        // bannerData,
        // functionData,
        contentBlocksData  // 内容区块统一数据（包含底部导航）
      ] = await Promise.all([
        HomeApi.getContentBlocks()
      ]);

      // 设置数据

      // API返回的数据结构可能包含data字段
      if (contentBlocksData && contentBlocksData.data && contentBlocksData.data.configContent) {
        contentBlocks.value = JSON.parse(contentBlocksData.data.configContent);
        bannerList.value = contentBlocks.value.banners || [];
        functionIcons.value = contentBlocks.value.functionIcons || [];
        sections.value = contentBlocks.value.sections || [];
      } else {
        contentBlocks.value = contentBlocksData || [];
      }

      console.log('首页数据加载完成, contentBlocks:', contentBlocks.value);

      // 在数据加载完成且DOM更新后再获取元素位置
      loading.value = false;

      // 延迟更长时间以确保DOM完全渲染
      setTimeout(() => {
        updatePositions();
      }, 1500); // 给足够的时间让DOM渲染完成

      // 返回Promise以便下拉刷新时使用
      return Promise.resolve();
    } catch (err) {
      console.error('获取首页数据失败', err);
      error.value = true;
      errorMessage.value = err.message || '数据加载失败，请重试';
      loading.value = false;

      // 返回失败的Promise以便下拉刷新时使用
      return Promise.reject(err);
    }
  };

  // 修改switchTab函数，调整DOM查询
  const switchTab = (index) => {
    if (activeBottomNav.value === index) return;

    // 确保sections数据已加载
    if (!sections.value || sections.value.length === 0) {
      console.log('sections数据尚未加载，无法切换标签');
      return;
    }

    // 触发短震动反馈
    uni.vibrateShort({
      success: () => {
        console.log('内部标签切换震动成功');
      },
      fail: (err) => {
        console.log('内部标签切换震动失败', err);
      }
    });

    // 设置活跃标签并锁定
    activeBottomNav.value = index;
    lockTab(1500); // 锁定1.5秒

    console.log('点击标签:', index, sections.value[index]?.navText);

    // 延迟执行，确保UI更新完成 - 增加延迟时间
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

              // 动态计算导航栏高度，考虑状态栏
              const navHeight = statusBarHeight.value + 44 + 50; // 状态栏 + 顶部导航 + 底部导航的高度
              const targetScrollTop = Math.max(0, absoluteTop - navHeight);

              // console.log('内容区域绝对位置:', absoluteTop);
              // console.log('动态导航高度:', navHeight);
              // console.log('最终滚动目标位置:', targetScrollTop);

              // 执行滚动 - 增加完成回调以确认滚动已完成
              uni.pageScrollTo({
                scrollTop: targetScrollTop,
                duration: 300,
                success: () => {
                  console.log('滚动到指定位置成功');
                  // 滚动完成后，再次验证位置是否正确
                  setTimeout(() => {
                    verifyScrollPosition(index, targetScrollTop);
                  }, 350);
                },
                fail: (err) => {
                  console.log('滚动失败', err);
                }
              });
            })
            .exec();
        })
        .exec();
    }, 200); // 增加延迟时间确保DOM已更新
  };

  // 添加滚动位置校验函数
  const verifyScrollPosition = (index, expectedPosition) => {
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .select(`#content-${index}`)
      .boundingClientRect((rect) => {
        if (!rect) return;
        
        uni.createSelectorQuery()
          .selectViewport()
          .scrollOffset((res) => {
            if (!res) return;
            
            const currentScrollTop = res.scrollTop;
            const navHeight = statusBarHeight.value + 44 + 50;
            const actualPosition = currentScrollTop + rect.top - navHeight;
            
            // 如果位置偏差超过一定范围，进行微调
            if (Math.abs(actualPosition - expectedPosition) > 20) {
              console.log('位置需要微调', actualPosition, expectedPosition);
              uni.pageScrollTo({
                scrollTop: currentScrollTop + (expectedPosition - actualPosition),
                duration: 100
              });
            }
          })
          .exec();
      })
      .exec();
  };

  // 顶部导航原始位置坐标
  const navTopValue = ref(0);
  // 页面滚动事件处理
  const handlePageScroll = (e) => {
    // 获取当前滚动位置
    const scrollTop = e.scrollTop;
    
    // 处理顶部导航栏透明度
    // 设置在0-100px的滚动范围内，导航栏从透明逐渐变为不透明
    const maxScrollForNav = 100;
    const newOpacity = Math.min(scrollTop / maxScrollForNav, 1);
    navOpacity.value = newOpacity;
    
    // 处理底部导航吸顶效果
    // 获取导航元素的位置信息
    uni.createSelectorQuery()
      .in(instance?.proxy)
      .select('#nav-anchor')
      .boundingClientRect(rect => {
        if (!rect) return;
        
        // 如果导航条顶部到达或超过屏幕顶部，则吸顶
        const shouldSticky = rect.top <= statusBarHeight.value + 44; // 状态栏+导航栏高度
        
        // 仅在状态变化时更新，减少不必要的重渲染
        if (isSticky.value !== shouldSticky) {
          isSticky.value = shouldSticky;
        }
      })
      .exec();
    
    // 如果滚动位置小于导航条原始位置，取消吸顶
    if (scrollTop < navTopValue.value - statusBarHeight.value - 44) {
      isSticky.value = false;
    }
    
    // 更新当前活跃的底部导航
    updateActiveTab(scrollTop);
  };

  // 更新活跃标签
  const updateActiveTab = (scrollTop) => {
    // 如果标签被锁定，不更新
    if (isTabLocked.value) return;
    
    // 如果没有内容区域信息，不更新
    if (!contentPositions.value.length) return;
    
    // 获取当前视口信息
    uni.getSystemInfo({
      success: (res) => {
        const viewportHeight = res.windowHeight;
        
        // 找出最佳可见的内容区
        let bestVisibility = -10000;
        let bestIndex = 0;
        
        // 循环所有内容区域计算可见度
        contentPositions.value.forEach((item, index) => {
          if (!item) return;
          
          // 计算元素顶部和底部相对于视口的位置
          const elementTop = item.top;
          const elementBottom = item.top + item.height;
          
          // 计算元素可见度
          const visibility = calculateVisibility(
            elementTop,
            elementBottom,
            statusBarHeight.value + 44, // 视口顶部边距
            viewportHeight
          );
          
          // 更新最佳可见元素
          if (visibility > bestVisibility) {
            bestVisibility = visibility;
            bestIndex = index;
          }
        });
        
        // 更新活跃标签
        if (activeBottomNav.value !== bestIndex) {
          activeBottomNav.value = bestIndex;
        }
      }
    });
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

  // 检查并创建静态资源
  const checkAndCreateStaticResources = () => {
    console.log('确保静态资源存在');
  };

  onMounted(() => {
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

  // 页面卸载时清理
  onBeforeUnmount(() => {
    // 清除锁定定时器
    if (tabLockTimeout.value) {
      clearTimeout(tabLockTimeout.value);
    }
  });
  // 暴露方法给父组件
  defineExpose({
    handlePageScroll,
    onPullDownRefresh
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
        display: flex;
        align-items: center;
        white-space: nowrap;
        transition: all 0.3s;
        flex: 1;

        .title-en {
          font-size: 38rpx;
          font-weight: 600;
        }

        .title-zh {
          font-size: 32rpx;
          font-weight: 500;
        }
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
          width: 60%;

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
      // background-color: #ffffff;
      border-radius: 20rpx 20rpx 0 0;
      margin: 0 10rpx;
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
      //border-radius: 20rpx 20rpx 0 0;
      //margin: 0 20rpx;
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
          color: #333333a8;
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

  // ECM胶原产品网格样式
  .ecm-product-grid {
    display: flex;
    flex-wrap: wrap;
    padding: 0;
    background-color: #f0f0f0;

    .ecm-product-item {
      width: 50%;
      height: 420rpx;
      position: relative;
      border-right: 2rpx solid #f0f0f0;
      border-bottom: 2rpx solid #f0f0f0;
      box-sizing: border-box;
      background-color: #fff;
      display: flex;
      flex-direction: column;

      &:nth-child(2n) {
        border-right: none;
      }

      &:nth-child(3), &:nth-child(4) {
        border-bottom: none;
      }

      .ecm-product-image {
        width: 100%;
        height: 340rpx;
        object-fit: contain;
        padding: 30rpx;
        box-sizing: border-box;
        flex: 1;
      }

      .ecm-product-footer {
        width: 100%;
        height: 80rpx;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        padding: 0 30rpx;
        box-sizing: border-box;
        background-color: #f8f8f8;

        .ecm-buy-btn {
          font-size: 28rpx;
          color: #333;
        }

        .ecm-product-price {
          font-size: 32rpx;
          color: #000;
          font-weight: bold;
          &::before {
            content: "¥";
            font-size: 26rpx;
            padding-right: 2rpx;
            font-weight: normal;
          }
        }
      }
    }
  }

  // 商学院课程样式
  .academy-programs {
    display: grid;
    // grid-template-columns: repeat(2, 1fr);
    // grid-template-rows: repeat(3, auto);
    // gap: 2rpx;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rpx;
    background-color: #f0f0f0;
    padding: 0 20rpx;

    .program-item {
      position: relative;
      background-color: #fff;
      height: 380rpx;
      overflow: hidden;

      .program-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .program-content {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        padding: 20rpx;
        background-color: rgba(255, 255, 255, 0.9);

        .program-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
          margin-bottom: 6rpx;
          text-align: center;
        }

        .program-subtitle {
          font-size: 22rpx;
          color: #666;
          text-align: center;
        }
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

  // 私域活动样式
  .private-activities {
    padding: 20rpx 30rpx;

    .activity-section {
      margin-bottom: 40rpx;

      .section-title {
        font-size: 40rpx;
        font-weight: bold;
        color: #333;
        padding: 10rpx 0;
      }

      .section-divider {
        height: 2rpx;
        background-color: #e5e5e5;
        margin: 10rpx 0 30rpx 0;
        width: 100%;
      }

      .activity-list {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .activity-item {
          width: 220rpx;
          display: flex;
          flex-direction: column;
          align-items: center;

          .activity-circle-image {
            width: 180rpx;
            height: 180rpx;
            border-radius: 50%;
            margin-bottom: 20rpx;
          }

          .activity-item-title {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 6rpx;
            text-align: center;
          }

          .activity-item-subtitle {
            font-size: 22rpx;
            color: #666;
            text-align: center;
            line-height: 1.2;
          }
        }
      }
    }
  }

  // 流量密码资源样式
  .traffic-resources {
    padding: 20rpx 30rpx;

    .resource-section {
      margin-bottom: 40rpx;

      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        padding: 10rpx 0;
      }

      .section-divider {
        height: 2rpx;
        background-color: #e5e5e5;
        margin: 10rpx 0 30rpx 0;
        width: 100%;
      }

      .resource-list {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;

        .resource-item {
          width: 220rpx;
          display: flex;
          flex-direction: column;
          align-items: center;

          .resource-circle-image {
            width: 180rpx;
            height: 180rpx;
            border-radius: 50%;
            margin-bottom: 20rpx;
          }

          .resource-item-title {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
            margin-bottom: 6rpx;
            text-align: center;
          }

          .resource-item-subtitle {
            font-size: 24rpx;
            color: #666;
            text-align: center;
          }
        }
      }
    }
  }
  </style>