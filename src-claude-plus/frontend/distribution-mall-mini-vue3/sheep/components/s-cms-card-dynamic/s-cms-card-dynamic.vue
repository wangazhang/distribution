<template>
  <view class="dynamic-card" @tap="handleClick">
    <!-- 顶部：头像+昵称+时间 -->
    <view class="dynamic-header ss-flex ss-col-center">
      <image
        class="avatar"
        :src="sheep.$url.cdn(article.authorAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg')"
        mode="aspectFill"
      />
      <view class="author-info ss-flex-1">
        <view class="author-name-line ss-flex ss-col-center">
          <text class="author-name">{{ article.authorName || '匿名用户' }}</text>
          <text v-if="article.isOfficial" class="official-badge ss-m-l-10">官方</text>
        </view>
        <view class="publish-time">{{ formatTimeAgo(article.publishTime) }}</view>
      </view>
    </view>

    <!-- 动态标题和副标题 -->
    <view
      v-if="isDynamicType && (article.title || article.subtitle)"
      class="dynamic-content-section ss-m-b-20"
    >
      <!-- 动态标题 -->
      <text v-if="article.title" class="dynamic-title ss-m-b-12">{{ truncatedTitle }}</text>

      <!-- 动态副标题 -->
      <text v-if="article.subtitle" class="dynamic-subtitle">{{ truncatedSubtitle }}</text>
    </view>

    <!-- 内容摘要 -->
    <view class="dynamic-content ss-line-3">{{ getContentText }}</view>

    <!-- 图片展示(最多9张，九宫格) -->
    <view v-if="images.length" class="image-grid" :class="`grid-${gridClass}`">
      <image
        v-for="(img, index) in images"
        :key="index"
        :src="sheep.$url.cdn(img)"
        mode="aspectFill"
        class="grid-image"
        @tap.stop="previewImage(index)"
      />
    </view>

    <!-- 会员等级标识 -->
    <view v-if="showPermissionBadge" class="permission-badge">
      <text class="cicon-lock" style="font-size: 24rpx; color: #ff6b00"></text>
      <text class="permission-text">{{ permissionText }}</text>
    </view>

    <!-- 底部统计和操作 -->
    <view v-if="showActions" class="dynamic-footer ss-flex ss-row-between ss-col-center">
      <view
        v-if="isButtonEnabled('like')"
        class="footer-btn like-btn ss-flex ss-col-center"
        :class="{ active: isLiked, 'with-divider': hasDivider('like') }"
        @tap.stop="handleLike"
      >
        <text
          :class="isLiked ? 'cicon-thumb-up-line' : 'cicon-thumb-up-line-o'"
          :style="'font-size: 30rpx; color: ' + (isLiked ? 'var(--ui-BG-Main, #8b0000)' : '#999')"
        ></text>
        <text class="btn-text" :class="{ active: isLiked }">{{ article.likeCount || 0 }}</text>
      </view>

      <view
        v-if="isButtonEnabled('collect')"
        class="footer-btn collect-btn ss-flex ss-col-center"
        :class="{ active: isCollected, 'with-divider': hasDivider('collect') }"
        @tap.stop="handleCollect"
      >
        <text
          :class="isCollected ? 'cicon-favorite' : 'cicon-favorite-o'"
          :style="'font-size: 28rpx; color: ' + (isCollected ? 'var(--ui-BG-Main, #8b0000)' : '#999')"
        ></text>
        <text class="btn-text" :class="{ active: isCollected }">{{ article.collectCount || 0 }}</text>
      </view>

      <view
        v-if="isButtonEnabled('share')"
        class="footer-btn share-btn ss-flex ss-col-center"
        :class="{ 'with-divider': hasDivider('share') }"
      >
        <button
          class="share-button ss-flex ss-col-center"
          open-type="share"
          @tap.stop="handleFriendShare"
        >
          <text class="cicon-share" style="font-size: 28rpx; color: #999"></text>
          <text class="btn-text">分享</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { ref, computed, watch } from 'vue';

  /**
   * 动态类卡片组件
   * 适用于：用户动态、朋友圈、社区帖子等
   */
  const props = defineProps({
    // 动态数据
    article: {
      type: Object,
      required: true,
    },
    // 板块配置
    config: {
      type: Object,
      default: () => ({}),
    },
  });

  const emit = defineEmits(['click', 'like', 'collect', 'share']);

  // 图片列表(最多9张)
  const images = computed(() => {
    if (!props.article.coverImages || !Array.isArray(props.article.coverImages)) {
      return [];
    }
    return props.article.coverImages.slice(0, 9);
  });

  // 九宫格样式类
  const gridClass = computed(() => {
    const count = images.value.length;
    if (count === 1) return '1';
    if (count <= 4) return '2';
    return '3';
  });

  // 判断是否为动态类型
  const isDynamicType = computed(() => {
    return props.article.sectionType === 'dynamic' || props.article.contentType === 'dynamic';
  });

  // 截断标题 - 最多15字
  const truncatedTitle = computed(() => {
    if (!props.article.title) return '';
    const title = props.article.title.trim();
    return title.length > 15 ? title.substring(0, 15) + '...' : title;
  });

  // 截断副标题 - 最多2行显示，使用CSS控制
  const truncatedSubtitle = computed(() => {
    if (!props.article.subtitle) return '';
    const subtitle = props.article.subtitle.trim();
    return subtitle;
  });

  // 获取纯文本内容
  const getContentText = computed(() => {
    if (!props.article.content) return '';
    // 移除HTML标签
    return props.article.content.replace(/<[^>]+>/g, '');
  });

  const BUTTON_ORDER = ['like', 'collect', 'download', 'enroll', 'share'];
  const SUPPORTED_ACTIONS = new Set(['like', 'collect', 'share']);
  const DEFAULT_BUTTON_FLAGS = {
    like: true,
    collect: true,
    share: true,
    enroll: false,
  };
  const ARTICLE_FLAG_PROPS = {
    like: 'enableLike',
    collect: 'enableCollect',
    share: 'enableShare',
  };

  const rawButtonConfig = computed(() => props.config?.cardButtons || props.config?.buttons || {});

  const resolveButtonFlag = (value, fallback) => {
    if (value === undefined || value === null) {
      return fallback;
    }
    if (typeof value === 'object') {
      if (Object.prototype.hasOwnProperty.call(value, 'show')) {
        return resolveButtonFlag(value.show, fallback);
      }
      return fallback;
    }
    if (typeof value === 'string') {
      const lower = value.trim().toLowerCase();
      if (lower === '') return fallback;
      return lower === 'true' || lower === '1' || lower === 'yes';
    }
    if (typeof value === 'number') {
      return value !== 0;
    }
    return !!value;
  };

  const isButtonEnabled = (key) => {
    if (!SUPPORTED_ACTIONS.has(key)) {
      return false;
    }
    const raw = rawButtonConfig.value?.[key];
    const fallback = DEFAULT_BUTTON_FLAGS[key] ?? false;
    if (!resolveButtonFlag(raw, fallback)) {
      return false;
    }
    const articleProp = ARTICLE_FLAG_PROPS[key];
    if (articleProp && props.article && props.article[articleProp] !== undefined) {
      return resolveButtonFlag(props.article[articleProp], true);
    }
    return true;
  };

  const visibleButtons = computed(() =>
    BUTTON_ORDER.filter((key) => SUPPORTED_ACTIONS.has(key) && isButtonEnabled(key)),
  );

  const showActions = computed(() => visibleButtons.value.length > 0);

  const hasDivider = (key) => {
    const list = visibleButtons.value;
    const index = list.indexOf(key);
    return index !== -1 && index !== list.length - 1;
  };

  // 用户行为状态 - 优先使用从列表接口返回的状态
  const normalizeBoolean = (value) => {
    if (value === true || value === false) return value;
    if (typeof value === 'number') return value === 1;
    if (typeof value === 'string') {
      const lower = value.trim().toLowerCase();
      if (lower === 'true' || lower === '1') return true;
      if (lower === 'false' || lower === '0') return false;
    }
    return false;
  };

  const isLiked = ref(normalizeBoolean(props.article.userLiked));
  const isCollected = ref(normalizeBoolean(props.article.userCollected));

  watch(
    () => props.article.userLiked,
    (val) => {
      isLiked.value = normalizeBoolean(val);
    },
    { immediate: true },
  );

  watch(
    () => props.article.userCollected,
    (val) => {
      isCollected.value = normalizeBoolean(val);
    },
    { immediate: true },
  );

  // 权限标识
  const showPermissionBadge = computed(() => {
    return props.article.permissionType && props.article.permissionType !== 'public';
  });

  const permissionText = computed(() => {
    if (props.article.permissionType === 'member') {
      return '会员专享';
    } else if (props.article.permissionType === 'level') {
      return props.article.requiredLevelName || '高级会员';
    }
    return '';
  });

  // 格式化相对时间
  const formatTimeAgo = (time) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now - date;

    // 1分钟内
    if (diff < 60000) {
      return '刚刚';
    }

    // 1小时内
    if (diff < 3600000) {
      const minutes = Math.floor(diff / 60000);
      return `${minutes}分钟前`;
    }

    // 24小时内
    if (diff < 86400000) {
      const hours = Math.floor(diff / 3600000);
      return `${hours}小时前`;
    }

    // 7天内
    if (diff < 604800000) {
      const days = Math.floor(diff / 86400000);
      return `${days}天前`;
    }

    // 超过7天显示具体时间
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');

    return `${month}-${day} ${hour}:${minute}`;
  };

  // 预览图片
  const previewImage = (index) => {
    const urls = images.value.map((img) => sheep.$url.cdn(img));
    uni.previewImage({
      current: index,
      urls: urls,
    });
  };

  // 事件处理
  const handleClick = () => {
    emit('click', props.article.id);
  };

  const handleLike = () => {
    const previousState = isLiked.value;
    isLiked.value = !isLiked.value;

    emit(
      'like',
      props.article.id,
      () => {
        if (isLiked.value) {
          props.article.likeCount = (props.article.likeCount || 0) + 1;
        } else {
          props.article.likeCount = Math.max((props.article.likeCount || 0) - 1, 0);
        }
        props.article.userLiked = isLiked.value;
      },
      (error) => {
        isLiked.value = previousState;
        uni.showToast({
          title: error.message || '操作失败',
          icon: 'none',
        });
      },
    );
  };

  const handleCollect = () => {
    const previousState = isCollected.value;
    isCollected.value = !isCollected.value;

    emit(
      'collect',
      props.article.id,
      () => {
        if (isCollected.value) {
          props.article.collectCount = (props.article.collectCount || 0) + 1;
        } else {
          props.article.collectCount = Math.max((props.article.collectCount || 0) - 1, 0);
        }
        props.article.userCollected = isCollected.value;
      },
      (error) => {
        isCollected.value = previousState;
        uni.showToast({
          title: error.message || '操作失败',
          icon: 'none',
        });
      },
    );
  };

  const handleFriendShare = () => {
    emit('share', { type: 'friend', article: props.article });
  };
</script>

<style lang="scss" scoped>
  .dynamic-card {
    background-color: white;
    padding: 20rpx;
    margin-bottom: 16rpx;
    border-radius: 18rpx;
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.06);
  }

  /* 顶部：头像+昵称 */
  .dynamic-header {
    margin-bottom: 16rpx;

    .avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 16rpx;
      flex-shrink: 0;
    }

    .author-info {
      .author-name-line {
        margin-bottom: 8rpx;
      }

      .author-name {
        font-size: 26rpx;
        font-weight: 600;
        color: #333;
      }

      .official-badge {
        background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
        color: white;
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 20rpx;
        font-weight: 500;
      }

      .publish-time {
        font-size: 22rpx;
        font-size: 20rpx;
        color: #999;
      }
    }
  }

  /* 动态内容区域 */
  .dynamic-content-section {
    .dynamic-title {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      line-height: 1.4;
      margin-bottom: 8rpx;
    }

    .dynamic-subtitle {
      display: block;
      font-size: 24rpx;
      color: #666;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  /* 内容摘要 */
  .dynamic-content {
    font-size: 26rpx;
    line-height: 1.5;
    color: #333;
    margin-bottom: 16rpx;
    word-break: break-all;
  }

  /* 九宫格图片 */
  .image-grid {
    display: grid;
    gap: 8rpx;
    margin-bottom: 20rpx;

    &.grid-1 {
      grid-template-columns: 1fr;
      max-width: 440rpx;

      .grid-image {
        width: 100%;
        height: 400rpx;
        border-radius: 12rpx;
      }
    }

    &.grid-2 {
      grid-template-columns: repeat(2, 1fr);

      .grid-image {
        width: 100%;
        height: 300rpx;
        border-radius: 8rpx;
      }
    }

    &.grid-3 {
      grid-template-columns: repeat(3, 1fr);

      .grid-image {
        width: 100%;
        height: 200rpx;
        border-radius: 8rpx;
      }
    }

    .grid-image {
      object-fit: cover;
    }
  }

  /* 权限标识 */
  .permission-badge {
    display: inline-flex;
    align-items: center;
    padding: 6rpx 14rpx;
    background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
    border-radius: 24rpx;
    margin-bottom: 12rpx;

    .permission-text {
      font-size: 20rpx;
      color: #ff6b00;
      margin-left: 6rpx;
      font-weight: 500;
    }
  }

  /* 底部统计和操作 */
  .dynamic-footer {
    padding-top: 12rpx;
    border-top: 1rpx solid #f0f0f0;
    margin-top: 12rpx;

    .footer-btn {
      flex: 1;
      flex-direction: column;
      align-items: center;
      padding: 6rpx 0;
      gap: 2rpx;
      position: relative;

      .btn-text {
        font-size: 18rpx;
        color: #666;
        white-space: nowrap;

        &.active {
          color: var(--ui-BG-Main, #8b0000);
          font-weight: 600;
        }
      }

      &:active {
        opacity: 0.7;
      }
    }

    .footer-btn.like-btn.active,
    .footer-btn.collect-btn.active {
      .btn-text {
        color: var(--ui-BG-Main, #8b0000);
        font-weight: 600;
      }
    }

    .footer-btn.with-divider::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 1rpx;
      height: 32rpx;
      background-color: #f0f0f0;
    }
  }

  .share-button {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2rpx;
    width: 100%;
    height: 100%;
    background: transparent;
    border: none;
    padding: 0;
    margin: 0;
    line-height: 1;

    &::after {
      display: none;
    }
  }
</style>
