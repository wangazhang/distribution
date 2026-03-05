<!-- 装修图文组件：图片轮播 -->
<template>
  <su-swiper
    :list="imgList"
    :dotStyle="data.indicator === 'dot' ? 'long' : 'tag'"
    :imageMode="convertObjectFit(data.objectFit)"
    dotCur="bg-mask-40"
    :height="data.height ? data.height * 2 : 0"
    :seizeHeight="data.height ? data.height * 2 : 300"
    :autoplay="data.autoplay"
    :interval="data.interval * 1000"
    :mode="data.type"
    :centerHighlight="data.centerHighlight"
    :circular="true"
  />
</template>

<script setup>
  import { computed } from 'vue';
  import sheep from '@/sheep';

  // 轮播图
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
  });

  // 将PC端的objectFit转换为小程序支持的格式
  const convertObjectFit = (objectFit) => {
    switch (objectFit) {
      case 'fill':
        return 'scaleToFill';
      case 'contain':
        return 'aspectFit';
      case 'cover':
        return 'aspectFill';
      default:
        return 'aspectFit';
    }
  };

  const imgList = computed(() =>
    props.data.items.map((item) => {
      const src = item.type === 'img' ? item.imgUrl : item.videoUrl;
      return {
        ...item,
        type: item.type === 'img' ? 'image' : 'video',
        src: sheep.$url.cdn(src),
        poster: sheep.$url.cdn(item.imgUrl),
      };
    }),
  );
</script>

<style></style>
