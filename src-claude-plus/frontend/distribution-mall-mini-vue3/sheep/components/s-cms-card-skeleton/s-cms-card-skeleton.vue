<template>
  <view class="cms-card-skeleton" :class="`cms-card-skeleton--${type}`">
    <view class="skeleton-header">
      <view class="skeleton-avatar skeleton-block"></view>
      <view class="skeleton-meta">
        <view class="skeleton-line skeleton-line--primary"></view>
        <view class="skeleton-line skeleton-line--secondary"></view>
      </view>
    </view>

    <view class="skeleton-body">
      <view class="skeleton-line skeleton-line--title"></view>
      <view class="skeleton-line skeleton-line--content"></view>
      <view class="skeleton-line skeleton-line--muted"></view>

      <view v-if="showCover" class="skeleton-cover skeleton-block"></view>

      <view v-if="gridPlaceholders.length" class="skeleton-grid">
        <view
          v-for="gridIndex in gridPlaceholders"
          :key="gridIndex"
          class="skeleton-grid-item skeleton-block"
        ></view>
      </view>
    </view>

    <view class="skeleton-footer">
      <view
        v-for="actionIndex in footerPlaceholders"
        :key="actionIndex"
        class="skeleton-footer-btn skeleton-block"
      ></view>
    </view>
  </view>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    type: {
      type: String,
      default: 'article',
    },
  });

  const typePreset = computed(() => {
    switch (props.type) {
      case 'dynamic':
        return {
          cover: false,
          grid: 3,
          actions: 3,
        };
      case 'course':
        return {
          cover: true,
          grid: 0,
          actions: 4,
        };
      default:
        return {
          cover: true,
          grid: 0,
          actions: 3,
        };
    }
  });

  const showCover = computed(() => typePreset.value.cover);
  const gridPlaceholders = computed(() =>
    Array.from({ length: typePreset.value.grid }, (_, index) => index),
  );
  const footerPlaceholders = computed(() =>
    Array.from({ length: typePreset.value.actions }, (_, index) => index),
  );
</script>

<style lang="scss" scoped>
  $skeleton-base: #f2f3f5;
  $skeleton-highlight: rgba(255, 255, 255, 0.65);

  @keyframes skeleton-loading {
    0% {
      background-position: -200rpx 0;
    }
    100% {
      background-position: 200rpx 0;
    }
  }

  .cms-card-skeleton {
    display: flex;
    flex-direction: column;
    padding: 20rpx;
    margin: 0 0 16rpx;
    border-radius: 18rpx;
    background-color: #ffffff;
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.06);
    box-sizing: border-box;
  }

  .skeleton-header {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
  }

  .skeleton-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: 20rpx;
  }

  .skeleton-meta {
    flex: 1;
  }

  .skeleton-body {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    margin-bottom: 20rpx;
  }

  .skeleton-footer {
    display: flex;
    justify-content: space-between;
    gap: 16rpx;
  }

  .skeleton-block {
    background: linear-gradient(90deg, $skeleton-base 25%, $skeleton-highlight 50%, $skeleton-base 75%);
    background-size: 400rpx 100%;
    animation: skeleton-loading 1.4s ease infinite;
  }

  .skeleton-line {
    height: 28rpx;
    border-radius: 14rpx;
    background: linear-gradient(90deg, $skeleton-base 25%, $skeleton-highlight 50%, $skeleton-base 75%);
    background-size: 400rpx 100%;
    animation: skeleton-loading 1.4s ease infinite;

    &--primary {
      width: 240rpx;
    }

    &--secondary {
      width: 180rpx;
      margin-top: 12rpx;
    }

    &--title {
      width: 80%;
      height: 36rpx;
    }

    &--content {
      width: 92%;
    }

    &--muted {
      width: 65%;
    }
  }

  .skeleton-cover {
    height: 240rpx;
    border-radius: 20rpx;
  }

  .skeleton-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12rpx;
  }

  .skeleton-grid-item {
    padding-top: 100%;
    border-radius: 16rpx;
  }

  .skeleton-footer-btn {
    flex: 1;
    height: 48rpx;
    border-radius: 24rpx;
  }
</style>
