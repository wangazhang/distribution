<template>
  <view>
    <!-- 吸顶导航栏 -->
    <view class="sticky-navbar" :class="{ 'is-fixed': isFixed }" :style="navbarStyle">
      <scroll-view scroll-x class="nav-scroll" show-scrollbar="false">
        <view class="nav-items">
          <view
            v-for="(item, index) in contentSections"
            :key="index"
            class="nav-item"
            :class="{
              active: index === activeIndex,
              'auto-width': contentSections.length <= 5,
            }"
            :style="contentSections.length <= 5 ? 'flex: 1' : ''"
            @tap="switchTab(index)"
          >
            <text
              class="nav-text"
              :style="{
                color: index === activeIndex ? activeColor : textColor,
              }"
            >
              {{ item.title || `导航${index + 1}` }}
            </text>
            <view
              v-if="showUnderline && index === activeIndex"
              class="nav-underline"
              :style="{ backgroundColor: underlineColor }"
            ></view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 占位元素，当导航栏吸顶时保持布局 -->
    <view v-if="isFixed" class="navbar-placeholder" :style="{ height: navbarHeight + 'px' }"></view>

    <!-- 内容区域 -->
    <view v-for="(section, sectionIndex) in contentSections" :key="section.id || sectionIndex">
      <view class="sticky-nav-section nav-section-item" :data-index="sectionIndex">
        <!-- 渲染内容区域组件 -->
        <template
          v-for="(component, componentIndex) in section.components || []"
          :key="componentIndex"
        >
          <!-- 使用 s-block 还原装修容器能力，保证子组件尺寸与样式一致 -->
          <s-block :styles="component?.property?.style || {}" class="sticky-nav-component">
            <component-renderer
              :component-data="{ ...component, styles: component?.property?.style || {} }"
            />
          </s-block>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
  /**
   * 吸顶导航组件
   */
  import { ref, computed, onMounted, getCurrentInstance, onUnmounted, nextTick } from 'vue';
  import { onPageScroll } from '@dcloudio/uni-app';
  import ComponentRenderer from '../component-renderer/component-renderer.vue';

  const props = defineProps({
    data: {
      type: Object,
      default() {
        return {};
      },
    },
    styles: {
      type: Object,
      default() {
        return {};
      },
    },
  });

  // 获取组件实例
  const instance = getCurrentInstance();

  // 当前激活的导航索引
  const activeIndex = ref(0);

  // 是否锁定标签切换
  const isTabLocked = ref(false);
  const tabLockTimeout = ref(null);

  // 存储内容区域位置信息
  const contentPositions = ref([]);

  // 导航栏吸顶相关状态
  const isFixed = ref(false);
  const navbarHeight = ref(0);
  const navbarTop = ref(0);
  const navbarOriginalTop = ref(0);
  const statusBarHeight = ref(0);
  const navbarOffset = ref(0); // 导航栏距离页面顶部的偏移量

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

  // 获取导航栏位置和高度
  const getNavbarPosition = () => {
    uni
      .createSelectorQuery()
      .in(instance?.proxy)
      .select('.sticky-navbar')
      .boundingClientRect((rect) => {
        if (!rect) {
          console.log('找不到导航栏元素');
          return;
        }

        navbarHeight.value = rect.height;
        navbarOriginalTop.value = rect.top;

        console.log('导航栏高度:', navbarHeight.value);
        console.log('导航栏原始位置:', navbarOriginalTop.value);

        // 获取系统状态栏高度和导航栏高度
        uni.getSystemInfo({
          success: (res) => {
            statusBarHeight.value = res.statusBarHeight || 0;
            // 小程序导航栏高度一般为 44px
            const appNavbarHeight = 44;

            // 计算导航栏应该吸顶的位置（状态栏 + 应用导航栏）
            navbarOffset.value = statusBarHeight.value + appNavbarHeight;
            console.log('导航栏吸顶偏移量:', navbarOffset.value);
          },
        });
      })
      .exec();
  };

  // 获取所有内容区域位置
  const updatePositions = () => {
    console.log('开始更新内容区域位置');
    contentPositions.value = [];

    // 使用组件内查询
    uni
      .createSelectorQuery()
      .in(instance?.proxy)
      .selectAll('.nav-section-item')
      .boundingClientRect((rects) => {
        if (!rects || !rects.length) {
          console.log('找不到内容区域元素');
          return;
        }

        console.log(`找到${rects.length}个内容区域`, rects);

        // 处理所有内容区域位置
        rects.forEach((rect, i) => {
          if (i < contentSections.value.length) {
            contentPositions.value[i] = {
              index: i,
              top: rect.top,
              height: rect.height,
            };
            console.log(`内容区${i}位置:`, contentPositions.value[i]);
          }
        });
      })
      .exec();
  };

  // 页面滚动事件处理
  const handlePageScroll = (e) => {
    // 处理吸顶效果
    const scrollTop = e.scrollTop;

    // 如果导航栏原始位置还未获取，重新获取
    if (navbarOriginalTop.value === 0 && !isFixed.value) {
      getNavbarPosition();
      return;
    }

    // 判断是否需要吸顶
    // 当页面滚动位置超过导航栏原始位置时，启用吸顶
    if (scrollTop > navbarOriginalTop.value - navbarOffset.value) {
      if (!isFixed.value) {
        isFixed.value = true;
        console.log('导航栏吸顶');
      }
    } else {
      if (isFixed.value) {
        isFixed.value = false;
        console.log('导航栏取消吸顶');
      }
    }

    // 如果标签被锁定，不更新激活标签
    if (isTabLocked.value) return;

    // 如果没有内容区域信息，不更新激活标签
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
          const elementTop = item.top - scrollTop;
          const elementBottom = elementTop + item.height;

          // 计算元素可见度，考虑导航栏高度
          const visibility = calculateVisibility(
            elementTop,
            elementBottom,
            navbarOffset.value + (isFixed.value ? navbarHeight.value : 0), // 考虑吸顶导航栏高度
            viewportHeight,
          );

          // 更新最佳可见元素
          if (visibility > bestVisibility) {
            bestVisibility = visibility;
            bestIndex = index;
          }
        });

        // 更新活跃标签
        if (activeIndex.value !== bestIndex) {
          activeIndex.value = bestIndex;
        }
      },
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

  // 监听页面滚动
  onPageScroll((e) => {
    handlePageScroll(e);
  });

  // 计算属性：获取内容区域数据
  const contentSections = computed(() => {
    if (props.data?.contentSections?.length > 0) {
      return props.data.contentSections;
    }
    // 兼容旧版list数据
    if (props.data?.list?.length > 0) {
      return props.data.list.map((item) => ({
        id: `nav-${Math.random().toString(36).substring(2, 10)}`,
        title: item.text,
        anchorId: item.anchorId || `content-${index}`,
        components: [],
      }));
    }
    // 默认返回空数组
    return [];
  });

  // 是否显示下划线
  const showUnderline = computed(() => {
    return props.data?.showUnderline ?? true;
  });

  // 文字颜色
  const textColor = computed(() => {
    return props.data?.textColor || '#333333';
  });

  // 激活颜色
  const activeColor = computed(() => {
    return props.data?.activeColor || '#8F1911';
  });

  // 下划线颜色
  const underlineColor = computed(() => {
    return props.data?.underlineColor || '#8F1911';
  });

  // 导航栏样式
  const navbarStyle = computed(() => {
    const style = props.styles || {};

    // 基础样式
    const baseStyle = {
      backgroundColor: props.data?.bgColor || '#ffffff',
      paddingTop: `${style.paddingTop || 10}rpx`,
      paddingRight: `${style.paddingRight || 0}rpx`,
      paddingBottom: `${style.paddingBottom || 10}rpx`,
      paddingLeft: `${style.paddingLeft || 0}rpx`,
      marginTop: `${style.marginTop || 0}rpx`,
      marginRight: `${style.marginRight || 0}rpx`,
      marginBottom: `${style.marginBottom || 0}rpx`,
      marginLeft: `${style.marginLeft || 0}rpx`,
      borderRadius: `${style.borderRadius || 0}rpx`,
    };

    // 如果是吸顶状态，添加固定定位样式
    if (isFixed.value) {
      return {
        ...baseStyle,
        position: 'fixed',
        top: `${navbarOffset.value}px`,
        left: '0',
        zIndex: '999',
        width: '100%',
        boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
        borderRadius: '0',
      };
    }

    return baseStyle;
  });

  // 处理标签切换
  const switchTab = (index) => {
    if (activeIndex.value === index) return;

    // 触发短震动反馈
    uni.vibrateShort({
      success: () => {
        console.log('标签切换震动成功');
      },
    });

    // 设置活跃标签并锁定
    activeIndex.value = index;
    lockTab(1000); // 锁定1秒

    console.log('点击标签:', index, contentSections.value[index]?.title);

    // 查询目标内容区域
    setTimeout(() => {
      // 使用组件内查询
      uni
        .createSelectorQuery()
        .in(instance?.proxy)
        .selectAll('.nav-section-item')
        .boundingClientRect((rects) => {
          if (!rects || !rects.length) {
            console.log('找不到内容区域元素');
            return;
          }

          // 找到对应索引的内容区域
          const rect = rects[index];
          if (!rect) {
            console.log('找不到目标内容区域:', index);
            return;
          }

          console.log('目标内容区域信息:', rect);

          // 查询视口信息获取当前滚动位置
          uni
            .createSelectorQuery()
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
              const absoluteTop = currentScrollTop + rect.top;

              // 计算导航栏高度，考虑吸顶状态
              const navHeight = navbarOffset.value + (isFixed.value ? navbarHeight.value : 0);
              const targetScrollTop = Math.max(0, absoluteTop - navHeight);

              // 执行滚动
              uni.pageScrollTo({
                scrollTop: targetScrollTop,
                duration: 300,
                success: () => {
                  console.log('滚动到指定位置成功');
                },
                fail: (err) => {
                  console.log('滚动失败', err);
                },
              });
            })
            .exec();
        })
        .exec();
    }, 100);
  };

  // 初始化
  onMounted(() => {
    console.log('吸顶导航组件初始化完成');

    // 延迟获取元素位置，确保DOM已渲染
    setTimeout(() => {
      // 获取导航栏位置和高度
      getNavbarPosition();

      // 获取内容区域位置
      updatePositions();

      // 添加窗口尺寸变化监听，重新计算位置
      uni.onWindowResize(() => {
        console.log('窗口尺寸变化，重新获取位置');
        getNavbarPosition();
        updatePositions();
      });
    }, 1000); // 增加延迟时间
  });

  // 页面卸载时清理
  onUnmounted(() => {
    // 清除锁定定时器
    if (tabLockTimeout.value) {
      clearTimeout(tabLockTimeout.value);
    }
  });

  // 暴露方法给父组件
  defineExpose({
    handlePageScroll,
    updatePositions,
    // 提供一个公共方法，用于外部主动更新位置
    refreshPositions: () => {
      console.log('外部调用刷新位置');
      getNavbarPosition();
      updatePositions();
    },
  });
</script>

<style lang="scss">
  .sticky-navbar {
    width: 100%;
    background-color: #fff;
    box-sizing: border-box;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
    overflow-x: auto;
    transition: all 0.3s ease;

    /* 吸顶状态样式 */
    &.is-fixed {
      position: fixed;
      left: 0;
      z-index: 999;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }

    /* 隐藏滚动条 */
    &::-webkit-scrollbar {
      display: none;
    }

    .nav-scroll {
      width: 100%;
      white-space: nowrap;
    }

    .nav-items {
      display: flex;
      padding: 0 20rpx;
      width: 100%;
      /* 添加等距分布 */
      justify-content: space-around;
    }

    .nav-item {
      position: relative;
      padding: 16rpx 24rpx;
      text-align: center;

      /* 默认情况下不使用flex:1，适用于导航项较多的情况 */
      display: inline-flex;
      justify-content: center;
      min-width: 120rpx; /* 设置最小宽度 */

      &.auto-width {
        /* 当导航项较少时使用等宽布局 */
        flex: 1;
      }

      .nav-text {
        font-size: 28rpx;
        transition: color 0.3s;
        white-space: nowrap;
      }

      .nav-underline {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 6rpx;
        border-radius: 3rpx;
        transition: all 0.3s;
      }

      &.active {
        font-weight: bold;
      }
    }
  }

  /* 占位元素样式 */
  .navbar-placeholder {
    width: 100%;
  }

  /* 内容区域样式 */
  .sticky-nav-section {
    background-color: #ebebeb;
    //.nav-section-item{
    //
    //}
  }
</style>
