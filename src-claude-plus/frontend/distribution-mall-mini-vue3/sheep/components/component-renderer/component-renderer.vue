<template>
  <!-- 基础组件 -->
  <s-search-block
    v-if="componentType === 'SearchBar' || componentType === 'Search'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-notice-block
    v-else-if="componentType === 'NoticeBar'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-menu-button
    v-else-if="componentType === 'MenuSwiper'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-menu-list
    v-else-if="componentType === 'MenuList' || componentType === 'TextNav'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-menu-grid
    v-else-if="componentType === 'MenuGrid'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-popup-image
    v-else-if="componentType === 'Popover'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-float-menu
    v-else-if="componentType === 'FloatingActionButton'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- 图文组件 -->
  <s-image-block
    v-else-if="componentType === 'ImageBar' || componentType === 'ImageAds'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-image-banner
    v-else-if="componentType === 'Carousel' || componentType === 'Banner'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-title-block
    v-else-if="componentType === 'TitleBar'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-image-cube
    v-else-if="componentType === 'MagicCube'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-video-block
    v-else-if="componentType === 'VideoPlayer'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-line-block
    v-else-if="componentType === 'Divider'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-hotzone-block
    v-else-if="componentType === 'HotZone'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- 商品组件 -->
  <s-goods-item
    v-else-if="componentType === 'Goods'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-goods-card
    v-else-if="componentType === 'ProductCard'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-goods-shelves
    v-else-if="componentType === 'ProductList' || componentType === 'GoodsList'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- 营销组件 -->
  <s-groupon-block
    v-else-if="componentType === 'PromotionCombination'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-seckill-block
    v-else-if="componentType === 'PromotionSeckill'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-point-block
    v-else-if="componentType === 'PromotionPoint'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-live-block
    v-else-if="componentType === 'MpLive'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-coupon-block
    v-else-if="componentType === 'CouponCard'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-richtext-block
    v-else-if="componentType === 'PromotionArticle'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- 用户组件 -->
  <s-user-card
    v-else-if="componentType === 'UserCard'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-order-card
    v-else-if="componentType === 'UserOrder'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-wallet-card
    v-else-if="componentType === 'UserWallet'"
    :data="componentData.property"
    :styles="componentStyles"
  />
  <s-coupon-card
    v-else-if="componentType === 'UserCoupon'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- CMS 内容组件 -->
  <s-cms-stream
    v-else-if="componentType === 'CmsContent'"
    :data="componentData.property"
    :styles="componentStyles"
  />

  <!-- 如果没有匹配的组件类型，显示空组件 -->
  <s-empty v-else :data="componentData.property" :styles="componentStyles" />
</template>

<script setup>
  /**
   * 组件渲染器 - 用于渲染不同类型的组件
   */
  import { computed, watch } from 'vue';

  const props = defineProps({
    componentData: {
      type: Object,
      default() {
        return {};
      },
    },
  });

  // 组件类型
  const componentType = computed(() => {
    // 优先使用 id 字段,其次使用 type 字段
    return props.componentData?.id || props.componentData?.type || '';
  });

  // 组件样式：优先使用显式 styles，其次兜底 property.style，避免丢失装修容器配置
  const componentStyles = computed(() => {
    return props.componentData?.styles || props.componentData?.property?.style || {};
  });

  // 调试输出
  watch(
    () => props.componentData,
    (newData) => {
      if (newData?.id === 'CmsContent' || newData?.type === 'CmsContent') {
        console.log('===== CMS组件数据 =====');
        console.log('组件ID:', newData.id);
        console.log('组件类型:', componentType.value);
        console.log('组件属性:', newData.property);
        console.log('==================');
      }
    },
    { immediate: true, deep: true },
  );
</script>
