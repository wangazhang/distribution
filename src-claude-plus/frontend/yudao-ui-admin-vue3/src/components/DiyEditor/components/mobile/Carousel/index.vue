<template>
  <!-- 无图片 -->
  <div
    class="flex items-center justify-center bg-gray-3"
    :style="{ height: `${property.height}px` }"
    v-if="property.items.length === 0"
  >
    <Icon icon="tdesign:image" class="text-gray-8 text-120px!" />
  </div>
  <div v-else class="relative">
    <el-carousel
      :height="`${property.height}px`"
      :type="property.type === 'card' ? 'card' : ''"
      :autoplay="property.autoplay"
      :interval="property.interval * 1000"
      :indicator-position="property.indicator === 'number' ? 'none' : undefined"
      @change="handleIndexChange"
      :class="{
        'carousel-center-highlight': property.centerHighlight && property.type !== 'card'
      }"
    >
      <el-carousel-item v-for="(item, index) in property.items" :key="index">
        <el-image 
          class="h-full w-full" 
          :src="item.imgUrl" 
          :fit="property.objectFit"
        />
      </el-carousel-item>
    </el-carousel>
    <div
      v-if="property.indicator === 'number'"
      class="absolute bottom-10px right-10px rounded-xl bg-black p-x-8px p-y-2px text-10px text-white opacity-40"
      >{{ currentIndex }} / {{ property.items.length }}</div
    >
  </div>
</template>
<script setup lang="ts">
import { CarouselProperty } from './config'

/** 轮播图 */
defineOptions({ name: 'Carousel' })

defineProps<{ property: CarouselProperty }>()

const currentIndex = ref(0)
const handleIndexChange = (index: number) => {
  currentIndex.value = index + 1
}
</script>

<style scoped lang="scss">
.carousel-center-highlight {
  :deep(.el-carousel__container) {
    overflow: visible;
  }

  :deep(.el-carousel__item) {
    opacity: 0.8;
    transform: scale(0.8);
    transition: transform 0.4s ease;
  }

  :deep(.el-carousel__item.is-active) {
    z-index: 2;
    opacity: 1;
    transform: scale(1);
  }
}
</style>
