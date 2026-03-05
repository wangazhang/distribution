<template>
  <view class="s-empty">
    <!-- 使用uni-app内置图标 -->
    <text class="empty-icon cuIcon-info"></text>
    <view class="empty-text">{{ data?.text || '暂无数据' }}</view>
  </view>
</template>

<script setup>
  /**
   * 空状态组件
   */
  const props = defineProps({
    data: {
      type: Object,
      default() {
        return {
          text: '暂无数据',
        };
      },
    },
    styles: {
      type: Object,
      default() {
        return {};
      },
    },
  });
</script>

<style lang="scss">
  .s-empty {
    width: 100%;
    padding: 40rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .empty-icon {
      font-size: 80rpx;
      color: #dcdcdc;
      margin-bottom: 20rpx;
    }

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }
</style>
