<template>
  <view class="article-card" @tap="handleClick">
    <!-- 头部:作者信息 -->
    <view class="card-header ss-flex ss-col-center ss-m-b-20">
      <image
        class="author-avatar"
        :src="sheep.$url.cdn(article.authorAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg')"
        mode="aspectFill"
      />
      <view class="author-info ss-flex-1">
        <view class="author-name-line ss-flex ss-col-center">
          <text class="author-name">{{ article.authorName || '匿名用户' }}</text>
          <text v-if="article.isOfficial" class="official-badge ss-m-l-10">官方助手</text>
        </view>
        <view class="publish-time">{{ formatTime(article.publishTime) }}</view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="card-content">
      <!-- 内容要点列表 -->
      <view v-if="contentPoints.length" class="content-points ss-m-b-20">
        <view
          v-for="(point, index) in contentPoints"
          :key="index"
          class="point-item ss-flex ss-col-top ss-m-b-12"
        >
          <text class="point-icon">{{ point.icon }}</text>
          <text class="point-text">{{ point.text }}</text>
        </view>
      </view>

      <!-- 文章标题和副标题 -->
      <view
        v-if="isArticleType && (article.title || article.subtitle)"
        class="article-content-section ss-m-b-20"
      >
        <!-- 文章标题 -->
        <text v-if="article.title" class="article-title ss-m-b-12">{{ truncatedTitle }}</text>

        <!-- 文章副标题 -->
        <text v-if="article.subtitle" class="article-subtitle">{{ truncatedSubtitle }}</text>
      </view>

      <!-- 全文标记 -->
      <view v-if="showFullText" class="full-text-badge ss-m-b-20">全文</view>

      <!-- 封面图片 -->
      <view v-if="showCover && coverImages.length" class="cover-image ss-m-b-20">
        <image :src="sheep.$url.cdn(coverImages[0])" mode="aspectFill" class="main-cover" />
      </view>

      <!-- 会员等级标识 -->
      <view v-if="showPermissionBadge" class="permission-badge ss-m-b-20">
        <text class="cicon-lock" style="font-size: 24rpx; color: #ff6b00"></text>
        <text class="permission-text">{{ permissionText }}</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="card-footer ss-flex ss-row-between ss-col-center">
      <!-- 点赞按钮 -->
      <view
        v-if="isButtonEnabled('like')"
        class="footer-btn like-btn ss-flex ss-col-center"
        :class="{ active: isLiked, 'with-divider': hasDivider('like') }"
        @tap.stop="handleLike"
      >
        <text
          :class="isLiked ? 'cicon-thumb-up-line' : 'cicon-thumb-up-line-o'"
          :style="'font-size: 32rpx; color: ' + (isLiked ? 'var(--ui-BG-Main, #8b0000);' : '#999')"
        ></text>
        <text class="btn-text" :class="{ active: isLiked }">
          <template v-if="showLikeCount">
            {{ likeDisplayCount }}
          </template>
          <template v-else>
            {{ isLiked ? '已赞' : '点赞' }}
          </template>
        </text>
      </view>

      <!-- 收藏按钮 -->
      <view
        v-if="isButtonEnabled('collect')"
        class="footer-btn collect-btn ss-flex ss-col-center"
        :class="{ active: isCollected, 'with-divider': hasDivider('collect') }"
        @tap.stop="handleCollect"
      >
        <text
          :class="isCollected ? 'cicon-favorite' : 'cicon-favorite-o'"
          :style="'font-size: 30rpx; color: ' + (isCollected ? 'var(--ui-BG-Main, #8b0000);' : '#999')"
        ></text>
        <text class="btn-text" :class="{ active: isCollected }">
          <template v-if="showCollectCount">
            {{ collectDisplayCount }}
          </template>
          <template v-else>
            {{ isCollected ? '已收藏' : '收藏' }}
          </template>
        </text>
      </view>

      <!-- 下载按钮 -->
      <view
        v-if="isButtonEnabled('download')"
        class="footer-btn download-btn ss-flex ss-col-center"
        :class="{ 'with-divider': hasDivider('download') }"
        @tap.stop="handleDownload"
      >
        <text class="cicon-cloud-download" style="font-size: 28rpx; color: #666"></text>
        <text class="btn-text">下载</text>
      </view>

      <!-- 报名按钮（默认不展示） -->
      <view
        v-if="isButtonEnabled('enroll')"
        class="footer-btn enroll-btn ss-flex ss-col-center"
        :class="{ 'with-divider': hasDivider('enroll') }"
        @tap.stop="handleEnroll"
      >
        <text class="cicon-ticket" style="font-size: 28rpx; color: #666"></text>
        <text class="btn-text">报名</text>
      </view>

      <!-- 微信好友按钮 -->
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
          <text class="cicon-share" style="font-size: 28rpx; color: #666"></text>
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
   * 素材卡片组件 - 优化版
   * 适用于:素材库、知识快讯等场景
   */
  const props = defineProps({
    // 文章数据
    article: {
      type: Object,
      required: true,
    },
    // 板块配置
    config: {
      type: Object,
      default: () => ({}),
    },
    // 是否显示封面
    showCover: {
      type: Boolean,
      default: true,
    },
    // 是否显示全文标记
    showFullText: {
      type: Boolean,
      default: true,
    },
  });

  const emit = defineEmits(['click', 'like', 'collect', 'download', 'share', 'enroll']);

  const BUTTON_ORDER = ['like', 'collect', 'download', 'enroll', 'share'];
  const SUPPORTED_ACTIONS = new Set(['like', 'collect', 'download', 'share', 'enroll']);
  const DEFAULT_BUTTON_FLAGS = {
    like: true,
    collect: true,
    download: true,
    enroll: false,
    share: true,
  };
  const ARTICLE_FLAG_PROPS = {
    like: 'enableLike',
    collect: 'enableCollect',
    download: 'enableDownload',
    enroll: 'enableRegister',
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

  const hasDivider = (key) => {
    const list = visibleButtons.value;
    const index = list.indexOf(key);
    return index !== -1 && index !== list.length - 1;
  };

  // 封面图片(只显示第一张)
  const coverImages = computed(() => {
    if (!props.article.coverImages || !Array.isArray(props.article.coverImages)) {
      return [];
    }
    return props.article.coverImages.slice(0, 1);
  });

  // 内容要点列表 - 解析文章内容提取要点
  const contentPoints = computed(() => {
    if (!props.article.content) return [];

    // 简单示例:从标题和副标题提取要点
    const points = [];

    if (props.article.title) {
      // 提取标题中的关键信息,用不同emoji标记
      const titleParts = props.article.title.split('|').map((t) => t.trim());
      titleParts.forEach((part, index) => {
        const icons = ['✨', '🔬', '💎'];
        points.push({
          icon: icons[index % icons.length],
          text: part,
        });
      });
    }

    // 如果没有分段标题,使用副标题或内容摘要
    if (points.length === 0 && props.article.subtitle) {
      points.push({
        icon: '✨',
        text: props.article.subtitle,
      });
    }

    return points.slice(0, 3); // 最多显示3个要点
  });

  // 用户行为状态 - 使用后端返回的状态
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

  // 统一处理互动功能开关，保持与详情页一致的容错逻辑
  const normalizeFlag = (value, fallback = true) => {
    if (value === undefined || value === null) return fallback;
    if (typeof value === 'boolean') return value;
    if (typeof value === 'number') return value !== 0;
    if (typeof value === 'string') {
      const lower = value.trim().toLowerCase();
      if (lower === 'true' || lower === '1') return true;
      if (lower === 'false' || lower === '0') return false;
    }
    return fallback;
  };

  // 计算展示用的互动统计值（真实值叠加初始引导量，并格式化）
  const getDisplayCount = (realCount, initialCount) => {
    const total = (realCount || 0) + (initialCount || 0);
    if (total >= 10000) {
      return (total / 10000).toFixed(1) + 'w';
    }
    if (total >= 1000) {
      return (total / 1000).toFixed(1) + 'k';
    }
    return total;
  };

  const likeCount = ref(props.article?.likeCount ?? 0);
  const collectCount = ref(props.article?.collectCount ?? 0);

  watch(
    () => props.article?.likeCount,
    (val) => {
      likeCount.value = val ?? 0;
    },
    { immediate: true },
  );

  watch(
    () => props.article?.collectCount,
    (val) => {
      collectCount.value = val ?? 0;
    },
    { immediate: true },
  );

  const showLikeCount = computed(() => normalizeFlag(props.article?.showLikeCount, true));
  const showCollectCount = computed(() => normalizeFlag(props.article?.showCollectCount, true));

  const likeDisplayCount = computed(() =>
    getDisplayCount(likeCount.value, props.article?.initialLikeCount),
  );
  const collectDisplayCount = computed(() =>
    getDisplayCount(collectCount.value, props.article?.initialCollectCount),
  );

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

  // 判断是否为文章类型
  const isArticleType = computed(() => {
    return props.article.sectionType === 'article' || props.article.contentType === 'article';
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

  // 格式化时间
  const formatTime = (time) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');

    // 显示完整日期时间
    if (year === now.getFullYear()) {
      return `${month}-${day} ${hour}:${minute}`;
    }

    return `${year}-${month}-${day} ${hour}:${minute}`;
  };

  // 事件处理
  const handleClick = () => {
    emit('click', props.article.id);
  };

  // 点赞 - 只触发事件，让父组件处理API调用
  const handleLike = () => {
    // 乐观更新UI
    const previousState = isLiked.value;
    isLiked.value = !isLiked.value;

    // 触发事件，让父组件处理API调用
    emit(
      'like',
      props.article.id,
      () => {
        // 成功回调：更新点赞数（最终状态由后端refreshSingleArticle更新）
        // 这里只做临时乐观更新，最终状态以父组件刷新为准
        likeCount.value = Math.max(
          (likeCount.value || 0) + (isLiked.value ? 1 : -1),
          0,
        );
        props.article.likeCount = likeCount.value;
        props.article.userLiked = isLiked.value;
      },
      (error) => {
        // 失败回调：回滚UI状态
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
        collectCount.value = Math.max(
          (collectCount.value || 0) + (isCollected.value ? 1 : -1),
          0,
        );
        props.article.collectCount = collectCount.value;
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

  // 下载素材
  const handleDownload = () => {
    // 跳转到下载页面
    const articleId = props.article.id;
    emit('share', { type: 'timeline', article: props.article });
    uni.navigateTo({
      url: `/pages/cms/download?id=${articleId}`,
    });

    // 触发下载事件
    emit('download', props.article.id);
  };

  // 微信好友分享
  const handleFriendShare = () => {
    emit('share', { type: 'friend', article: props.article });
  };

  const handleEnroll = () => {
    emit('enroll', props.article.id);
  };
</script>

<style lang="scss" scoped>
  .article-card {
    background-color: white;
    border-radius: 18rpx;
    padding: 20rpx;
    margin-bottom: 16rpx;
    box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.06);
  }

  /* 卡片头部 */
  .card-header {
    .author-avatar {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      margin-right: 14rpx;
      flex-shrink: 0;
    }

    .author-info {
      .author-name-line {
        margin-bottom: 6rpx;
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
        font-size: 20rpx;
        color: #999;
      }
    }
  }

  /* 卡片内容区域 */
  .card-content {
    /* 文章内容区域 */
    .article-content-section {
      .article-title {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        line-height: 1.4;
        margin-bottom: 8rpx;
      }

      .article-subtitle {
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

    /* 内容要点列表 */
    .content-points {
      .point-item {
        .point-icon {
          font-size: 26rpx;
          margin-right: 10rpx;
          flex-shrink: 0;
        }

        .point-text {
          font-size: 24rpx;
          color: #333;
          line-height: 1.6;
          flex: 1;
        }
      }
    }

    /* 全文标记 */
    .full-text-badge {
      display: inline-block;
      padding: 6rpx 16rpx;
      //background: linear-gradient(135deg, #8b0000 0%, #a00000 100%);
      color: var(--ui-BG-Main, #8b0000);
      //border-radius: 8rpx;
      font-size: 20rpx;
      font-weight: 500;
    }

    /* 封面图片 */
    .cover-image {
      .main-cover {
        width: 100%;
        height: 240rpx;
        border-radius: 12rpx;
        object-fit: cover;
      }
    }

    /* 权限标识 */
    .permission-badge {
      display: inline-flex;
      align-items: center;
      padding: 6rpx 16rpx;
      background: linear-gradient(135deg, #fff5e6 0%, #ffe8cc 100%);
      border-radius: 24rpx;

      .permission-text {
        font-size: 20rpx;
        color: #ff6b00;
        margin-left: 8rpx;
        font-weight: 500;
      }
    }
  }

  /* 卡片底部操作栏 */
  .card-footer {
    padding-top: 12rpx;
    border-top: 1rpx solid #f0f0f0;
    margin-top: 12rpx;

    .footer-btn {
      flex: 1;
      flex-direction: column;
      align-items: center;
      gap: 2rpx;
      position: relative;

      .btn-text {
        font-size: 18rpx;
        color: #666;
        white-space: nowrap;

        &.active {
          color: #ff6b00;
        }
      }

      // 按钮样式特效
      &:active {
        opacity: 0.7;
      }

      &.like-btn.active {
        .btn-text {
          color: var(--ui-BG-Main, #8b0000);
          font-weight: 600;
        }
      }

      &.collect-btn.active {
        .btn-text {
          color: var(--ui-BG-Main, #8b0000);
          font-weight: 600;
        }
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
