<template>
  <view class="page-container">
    <!-- 页面组件动态切换 -->
    <Home v-if="currentTabIndex === 0" @scroll-event="handlePageScroll" ref="homeComponent" />
    <Mall v-if="currentTabIndex === 1" />
    <Community v-if="currentTabIndex === 2" />
    <Mine v-if="currentTabIndex === 3" />

    <!-- 自定义底部导航栏 -->
    <!--    <yh-tabbar-->
    <!--      activeColor="#FFA0B4"-->
    <!--      color="#999999"-->
    <!--      @change="handleTabChange"-->
    <!--    ></yh-tabbar>  -->
    <yh-tabbar activeColor="#8F1911" color="#999999" @change="handleTabChange"></yh-tabbar>
  </view>
</template>

<script setup>
  import { ref, onMounted, onBeforeUnmount } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import { getIndexData, getAppConfig } from '@/yehu/api/index/index';
  import Home from '@/yehu/pages/home/index.vue';
  import Mall from '@/yehu/pages/mall/index.vue';
  import Community from '@/yehu/pages/community/index.vue';
  import Mine from '@/yehu/pages/mine/index.vue';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import $share from '@/sheep/platform/share';

  // 当前选中的标签页索引
  const currentTabIndex = ref(0);
  // 引用home组件实例
  const homeComponent = ref(null);

  // 应用数据
  const appData = ref({});
  const appConfig = ref({});

  // 初始化数据
  const initData = async () => {
    try {
      // 并行获取数据
      const [indexData, configData] = await Promise.all([
        // getIndexData(),
        // getAppConfig()
      ]);

      // appData.value = indexData;
      appConfig.value = configData;

      console.log('应用数据初始化完成');

      // 检查是否有需要处理的通知
      checkNotifications();
    } catch (err) {
      console.error('初始化数据失败', err);
    }
  };

  // 检查通知
  const checkNotifications = () => {
    if (appData.value?.notices?.length > 0) {
      // 显示最新通知
      const latestNotice = appData.value.notices[0];
      uni.showToast({
        title: latestNotice.title,
        icon: 'none',
        duration: 3000,
      });
    }
  };

  // 处理标签页切换
  const handleTabChange = (index) => {
    if (currentTabIndex.value === index) return;

    // 手动触发额外的震动反馈，确保体验一致
    uni.vibrateShort({
      success: () => {
        console.log('主标签切换震动成功');
      },
      fail: (err) => {
        console.log('主标签切换震动失败', err);
      },
    });

    // 如果切换到"觅己"(索引3)且未登录，则弹出登录弹窗
    // 注意：仅对"觅己"进行登录检查，不对"觅社区"(索引2)进行检查
    if (index === 3) {
      const isLogin = sheep.$store('user').isLogin;
      if (!isLogin) {
        showAuthModal('accountLogin');
      }
    }

    currentTabIndex.value = index;
  };

  // 处理页面滚动事件
  const handlePageScroll = (e) => {
    // 根据当前激活的标签页分发滚动事件
    if (currentTabIndex.value === 0 && homeComponent.value) {
      // @ts-ignore
      homeComponent.value.handlePageScroll(e);
    }
  };

  onMounted((options) => {
    // 初始化数据
    initData();

    // 注册页面滚动监听
    const page = getCurrentPages()[getCurrentPages().length - 1];
    // @ts-ignore
    page.onPageScroll = handlePageScroll;
  });

  onBeforeUnmount(() => {
    // 移除页面滚动监听
    const page = getCurrentPages()[getCurrentPages().length - 1];
    if (page) {
      // @ts-ignore
      page.onPageScroll = null;
    }
  });

  onLoad((options) => {
    // #ifdef MP
    // 小程序识别二维码

    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      console.log('sceneParams=>', sceneParams);
      options[sceneParams[0]] = sceneParams[1];
    }
    // #endif

    // 预览模板
    if (options.templateId) {
      sheep.$store('app').init(options.templateId);
    }

    // 解析分享信息
    if (options.spm) {
      console.log('******spm=>', options);
      $share.decryptSpm(options.spm);
    }

    // 进入指定页面(完整页面路径)
    if (options.page) {
      sheep.$router.go(decodeURIComponent(options.page));
    }
  });
</script>

<style lang="scss" scoped>
  .page-container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f8f8f8;
  }
</style>
