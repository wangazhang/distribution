<!-- 自定义页面：支持装修 -->
<template>
  <s-layout
    :title="state.name"
    navbar="custom" tabbar="/yehu/pages/community/index"
    :bgStyle="state.page"
    :navbarStyle="state.navigationBar"
    :onShareAppMessage="shareInfo"
    showLeftButton
  >
    <s-block v-for="(item, index) in state.components" :key="index" :styles="item.property.style">
      <s-block-item :type="item.id" :data="item.property" :styles="item.property.style" />
    </s-block>
  </s-layout>
</template>

<script setup>
  import { reactive, ref, onMounted, onUnmounted } from 'vue';
  import { onLoad, onPageScroll, onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import DiyApi from '@/sheep/api/promotion/diy';
  import sheep from '@/sheep';
  import { SharePageEnum } from '@/sheep/util/const';

  const state = reactive({
    id: 13,
    name: '',
    components: [],
    navigationBar: {},
    page: {},
  });

  const buildDefaultShareInfo = (title = '社群', image = '', desc = '') => {
    const info = sheep.$platform.share.getShareInfo(
      {
        title,
        image,
        desc,
        params: {
          page: SharePageEnum.HOME.value,
          query: 'community',
        },
      },
      {
        type: 'article',
        title,
        image,
      },
    );
    const sharePath = '/yehu/pages/community/index';
    info.path = sharePath;
    info.query = '';
    info.title = info.title || title;
    info.image = info.image || image;
    if (!info.forward) {
      info.forward = {};
    }
    info.forward.path = sharePath;
    return info;
  };

  const shareInfo = ref(buildDefaultShareInfo());

  const updateDefaultShareInfo = () => {
    const title = state.name || '社群';
    const image = state.page?.shareImage ? sheep.$url.cdn(state.page.shareImage) : '';
    const desc = state.page?.description || '';
    const next = buildDefaultShareInfo(title, image, desc);
    shareInfo.value = next;
    sheep.$platform.share.updateShareInfo(next);
  };

  const getCurrentRoute = () => {
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    return pages.length ? pages[pages.length - 1].route : '';
  };

  // 微信小程序静默登录，避免首屏弹窗打断浏览
  let silentLoginPromise = null;
  const trySilentLogin = async () => {
    if (sheep.$platform.name !== 'WechatMiniProgram') return;
    const userStore = sheep.$store('user');
    if (userStore.isLogin) return;
    if (silentLoginPromise) {
      return silentLoginPromise;
    }
    silentLoginPromise = (async () => {
      try {
        const ok = await sheep.$platform.useProvider('wechat').login();
        if (ok) {
          await userStore.updateUserData();
        }
      } catch (error) {
        console.warn('社区页静默登录失败:', error);
      } finally {
        silentLoginPromise = null;
      }
    })();
    return silentLoginPromise;
  };

  const fetchPageData = async (diyId) => {
    if (!diyId) return;
    try {
      const { code, data } = await DiyApi.getDiyPage(diyId);
      if (code === 0 && data) {
        state.id = diyId;
        state.name = data.name;
        state.components = data.property?.components || [];
        state.navigationBar = data.property?.navigationBar || {};
        state.page = data.property?.page || {};
        updateDefaultShareInfo();
      }
    } catch (error) {
      console.error('加载DIY页面失败:', error);
      uni.showToast({
        title: '刷新失败，请稍后重试',
        icon: 'none',
      });
    }
  };

  const handleShareInfoUpdate = (info) => {
    if (!info) return;
    shareInfo.value = info;
  };

  const handleLoginRefresh = async () => {
    await fetchPageData(state.id || 13);
    uni.$emit('diy:pull-down-refresh', {
      pagePath: 'yehu/pages/community/index',
      timestamp: Date.now(),
    });
  };

  onMounted(() => {
    uni.$on('cms:update-share-info', handleShareInfoUpdate);
    uni.$on('community:refresh-after-login', handleLoginRefresh);
  });

  onShow(() => {
    trySilentLogin();
    uni.hideTabBar();
  });

  onUnmounted(() => {
    uni.$off('cms:update-share-info', handleShareInfoUpdate);
    uni.$off('community:refresh-after-login', handleLoginRefresh);
  });

  onLoad(async (options) => {
    let id = 13;

    // #ifdef MP
    // 小程序预览自定义页面
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      id = sceneParams[1];
    }
    // #endif

    await trySilentLogin();
    await fetchPageData(id);
  });

  onPullDownRefresh(async () => {
    const currentRoute = getCurrentRoute();
    const pageId = state.id || 13;
    if (!pageId) {
      uni.stopPullDownRefresh();
      return;
    }
    await fetchPageData(pageId);
    uni.$emit('diy:pull-down-refresh', {
      pagePath: currentRoute,
      timestamp: Date.now(),
    });
    uni.stopPullDownRefresh();
  });

  onPageScroll(() => {});
</script>

<style></style>
