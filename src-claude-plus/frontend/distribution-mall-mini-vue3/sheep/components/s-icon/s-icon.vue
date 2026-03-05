<template>
  <text class="s-icon" :style="iconStyle">{{ iconChar }}</text>
</template>

<script setup>
  import { computed } from 'vue';
  import { getIconType } from '@/sheep/config/icons';

  /**
   * 统一图标组件
   * 使用字体图标实现，支持自定义大小和颜色
   */
  const props = defineProps({
    // 图标名称
    name: {
      type: String,
      required: true,
    },
    // 图标大小(rpx)
    size: {
      type: [Number, String],
      default: 40,
    },
    // 图标颜色
    color: {
      type: String,
      default: '#666',
    },
    // 是否粗体
    bold: {
      type: Boolean,
      default: false,
    },
  });

  // 图标字符映射（使用Unicode字符）
  const iconChars = {
    // 浏览/查看
    eye: '👁',
    view: '👁',

    // 点赞
    heart: '♡',
    'heart-filled': '♥',
    like: '♡',
    liked: '♥',

    // 收藏/星标
    star: '☆',
    'star-filled': '★',
    collect: '☆',
    collected: '★',

    // 分享
    redo: '↗',
    share: '↗',

    // 下载
    download: '⬇',

    // 评论
    chat: '💬',
    'chat-filled': '💬',

    // 用户/人群
    person: '👤',
    'person-filled': '👥',
    people: '👥',

    // 时间
    clock: '⏰',

    // 位置
    location: '📍',
    'location-filled': '📍',

    // 锁定
    locked: '🔒',
    unlocked: '🔓',

    // 其他
    'play-filled': '▶',
    'more-filled': '⋯',
    closeempty: '✕',
    search: '🔍',
    refreshempty: '↻',
  };

  // 获取图标类型
  const iconType = computed(() => getIconType(props.name));

  // 获取图标字符
  const iconChar = computed(() => {
    return iconChars[iconType.value] || '•';
  });

  // 图标样式
  const iconStyle = computed(() => {
    const size = typeof props.size === 'number' ? props.size : parseInt(props.size);
    return {
      fontSize: size + 'rpx',
      color: props.color,
      fontWeight: props.bold ? 'bold' : 'normal',
      lineHeight: 1,
      display: 'inline-block',
    };
  });
</script>

<style lang="scss" scoped>
  .s-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    vertical-align: middle;
  }
</style>
