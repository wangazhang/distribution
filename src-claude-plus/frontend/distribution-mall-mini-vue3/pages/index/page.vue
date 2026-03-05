<!-- 自定义页面：支持装修 -->
<template>
  <s-layout
    :title="state.name"
    navbar="custom"
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
  import { onLoad, onPageScroll, onPullDownRefresh } from '@dcloudio/uni-app';
  import DiyApi from '@/sheep/api/promotion/diy';
  import sheep from '@/sheep';
  import { SharePageEnum } from '@/sheep/util/const';

  const state = reactive({
    id: null,
    name: '',
    components: [],
    navigationBar: {},
    page: {},
  });

  const buildDefaultShareInfo = (options = {}) => {
    const title = options.title || state.name || '自定义页面';
    const image = options.image || (state.page?.shareImage ? sheep.$url.cdn(state.page.shareImage) : '');
    const desc = options.desc || state.page?.description || '';
    const pageId =
      typeof options.pageId === 'undefined' || options.pageId === null ? state.id : options.pageId;

    const info = sheep.$platform.share.getShareInfo(
      {
        title,
        image,
        desc,
        params: {
          page: SharePageEnum.HOME.value,
          query: pageId || '0',
        },
      },
      {
        type: 'article',
        title,
        image,
      },
    );

    const sharePath = pageId ? `/pages/index/page?id=${pageId}` : '/pages/index/page';
    const query = pageId ? `id=${pageId}` : '';
    info.path = sharePath;
    info.query = query;
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
    const next = buildDefaultShareInfo();
    shareInfo.value = next;
    sheep.$platform.share.updateShareInfo(next);
  };

  const getCurrentRoute = () => {
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    return pages.length ? pages[pages.length - 1].route : '';
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

  onMounted(() => {
    uni.$on('cms:update-share-info', handleShareInfoUpdate);
  });

  onUnmounted(() => {
    uni.$off('cms:update-share-info', handleShareInfoUpdate);
  });

  onLoad(async (options) => {
    let id = options.id;

    // #ifdef MP
    // 小程序预览自定义页面
    if (options.scene) {
      const sceneParams = decodeURIComponent(options.scene).split('=');
      id = sceneParams[1];
    }
    // #endif

    await fetchPageData(id);
  });

  onPullDownRefresh(async () => {
    const currentRoute = getCurrentRoute();
    const pageId = state.id;
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
