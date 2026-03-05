<template>
  <view v-if="visible" class="success-overlay" @tap="handleOverlayTap">
    <view class="success-card" :class="{ show: showContent }">
      <view class="icon-wrapper">
        <view class="icon-circle" :class="{ animate: iconAnimated }">
          <text class="cicon-check-round icon-symbol"></text>
        </view>
      </view>
      <view class="success-text" :class="{ animate: textAnimated }">
        <text class="success-title">{{ title }}</text>
        <text class="success-subtitle">{{ subtitle }}</text>
      </view>
      <view class="button-group" :class="{ animate: buttonAnimated }">
        <button v-if="showCancelButton" class="cancel-btn" @tap="handleCancel">
          {{ cancelText }}
        </button>
        <button class="confirm-btn" @tap="handleConfirm">
          {{ confirmText }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { ref, watch } from 'vue';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false,
    },
    title: {
      type: String,
      default: '发布成功',
    },
    subtitle: {
      type: String,
      default: '内容正在审核中，请耐心等待',
    },
    confirmText: {
      type: String,
      default: '确定',
    },
    cancelText: {
      type: String,
      default: '取消',
    },
    showCancelButton: {
      type: Boolean,
      default: false,
    },
    closeOnTapOverlay: {
      type: Boolean,
      default: true,
    },
    duration: {
      type: Number,
      default: 0,
    },
  });

  const emit = defineEmits(['confirm', 'cancel', 'close']);

  const showContent = ref(false);
  const iconAnimated = ref(false);
  const textAnimated = ref(false);
  const buttonAnimated = ref(false);

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        showAnimation();
      } else {
        resetAnimation();
      }
    },
  );

  const showAnimation = () => {
    showContent.value = false;
    iconAnimated.value = false;
    textAnimated.value = false;
    buttonAnimated.value = false;

    setTimeout(() => {
      showContent.value = true;

      setTimeout(() => {
        iconAnimated.value = true;
      }, 80);

      setTimeout(() => {
        textAnimated.value = true;
      }, 320);

      setTimeout(() => {
        buttonAnimated.value = true;
      }, 450);

      if (props.duration > 0) {
        setTimeout(() => {
          handleClose();
        }, props.duration + 600);
      }
    }, 30);
  };

  const resetAnimation = () => {
    showContent.value = false;
    iconAnimated.value = false;
    textAnimated.value = false;
    buttonAnimated.value = false;
  };

  const handleConfirm = () => {
    emit('confirm');
    handleClose();
  };

  const handleCancel = () => {
    emit('cancel');
    handleClose();
  };

  const handleOverlayTap = () => {
    if (props.closeOnTapOverlay) {
      handleClose();
    }
  };

  const handleClose = () => {
    emit('close');
    resetAnimation();
  };
</script>

<style lang="scss" scoped>
  .success-overlay {
    position: fixed;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40rpx;
    z-index: 9999;
  }

  .success-card {
    width: 100%;
    max-width: 520rpx;
    padding: 56rpx 48rpx 52rpx;
    background-color: #fff;
    border-radius: 28rpx;
    text-align: center;
    box-shadow: 0 24rpx 80rpx rgba(0, 0, 0, 0.12);
    opacity: 0;
    transform: translateY(30rpx) scale(0.9);
    transition: all 0.32s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .success-card.show {
    opacity: 1;
    transform: translateY(0) scale(1);
  }

  .icon-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 32rpx;
  }

  .icon-circle {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: linear-gradient(140deg, #c64c4c 0%, #8b0000 100%);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 36rpx rgba(139, 0, 0, 0.28);
    opacity: 0;
    transform: scale(0.8);
  }

  .icon-circle.animate {
    animation: iconPop 0.48s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  }

  .icon-symbol {
    font-size: 78rpx;
  }

  .success-text {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    opacity: 0;
    transform: translateY(12rpx);
  }

  .success-text.animate {
    animation: fadeInUp 0.35s ease forwards;
  }

  .success-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }

  .success-subtitle {
    font-size: 26rpx;
    color: #666;
  }

  .button-group {
    margin-top: 40rpx;
    display: flex;
    justify-content: center;
    gap: 24rpx;
    opacity: 0;
    transform: translateY(16rpx);
  }

  .button-group.animate {
    animation: fadeInUp 0.35s ease forwards 0.08s;
  }

  .cancel-btn,
  .confirm-btn {
    min-width: 180rpx;
    padding: 20rpx 32rpx;
    border-radius: 999rpx;
    font-size: 28rpx;
    border: none;
    outline: none;
    color: #fff;
    background: linear-gradient(140deg, #c64c4c 0%, #8b0000 100%);
    box-shadow: 0 10rpx 24rpx rgba(139, 0, 0, 0.24);
    transition: transform 0.2s ease;
  }

  .cancel-btn {
    background: #f5f5f5;
    color: #666;
    box-shadow: none;
  }

  .cancel-btn:active,
  .confirm-btn:active {
    transform: scale(0.96);
  }

  @keyframes iconPop {
    0% {
      transform: scale(0.6);
      opacity: 0;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }

  @keyframes fadeInUp {
    0% {
      opacity: 0;
      transform: translateY(18rpx);
    }
    100% {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
