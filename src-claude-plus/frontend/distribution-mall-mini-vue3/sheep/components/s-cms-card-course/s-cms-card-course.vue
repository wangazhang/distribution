<template>
  <view class="course-card" @tap="handleClick">
    <!-- 封面大图(16:9) -->
    <view class="course-cover">
      <image :src="sheep.$url.cdn(coverImage)" mode="aspectFill" class="cover-image" />
      <!-- 权限标识(叠加在封面上) -->
      <view v-if="showPermissionBadge" class="permission-overlay">
        <view class="permission-badge-large">
          <text class="cicon-lock" style="font-size: 32rpx; color: #fff"></text>
          <text class="permission-text-large">{{ permissionText }}</text>
        </view>
      </view>
    </view>

    <view class="course-info">
      <!-- 标题 -->
      <view class="course-title ss-line-2">{{ article.title }}</view>

      <!-- 副标题 -->
      <view v-if="article.subtitle" class="course-subtitle ss-line-1">
        {{ article.subtitle }}
      </view>

      <!-- 讲师和学习人数 -->
      <view class="course-meta ss-flex ss-row-between ss-col-center">
        <view class="instructor ss-flex ss-col-center">
          <text class="cicon-person" style="font-size: 28rpx; color: #666"></text>
          <text class="instructor-name ss-m-l-8">{{ article.authorName || '未知讲师' }}</text>
          <text v-if="article.isOfficial" class="official-badge ss-m-l-10">官方</text>
        </view>
        <view class="learners ss-flex ss-col-center">
          <text class="cicon-avatars" style="font-size: 28rpx; color: #666"></text>
          <text class="learners-count ss-m-l-8">{{ article.viewCount || 0 }}人学习</text>
        </view>
      </view>

      <!-- 底部操作按钮 -->
      <view v-if="showActions" class="course-footer ss-flex ss-row-between ss-col-center">
        <view
          v-if="isButtonEnabled('like')"
          class="footer-btn like-btn ss-flex ss-col-center"
          :class="{ active: isLiked, 'with-divider': hasDivider('like') }"
          @tap.stop="handleLike"
        >
          <text
            :class="isLiked ? 'cicon-thumb-up-line' : 'cicon-thumb-up-line-o'"
            :style="'font-size: 30rpx; color: ' + (isLiked ? 'var(--ui-BG-Main, #8b0000)' : '#666')"
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
            :style="'font-size: 28rpx; color: ' + (isCollected ? 'var(--ui-BG-Main, #8b0000)' : '#666')"
          ></text>
          <text class="btn-text" :class="{ active: isCollected }">{{
            article.collectCount || 0
          }}</text>
        </view>

        <view
          v-if="isButtonEnabled('enroll')"
          class="footer-btn enroll-btn ss-flex ss-col-center"
          :class="{ 'with-divider': hasDivider('enroll') }"
          @tap.stop="handleEnroll"
        >
          <text class="cicon-ticket" style="font-size: 28rpx; color: #666"></text>
          <text class="btn-text">报名</text>
        </view>

        <view
          v-if="isButtonEnabled('share')"
          class="footer-btn share-btn ss-flex ss-col-center"
          :class="{ 'with-divider': hasDivider('share') }"
        >
          <button
            class="share-button ss-flex ss-col-center"
            open-type="share"
            @tap.stop="handleShareToFriend"
          >
            <text class="cicon-share" style="font-size: 28rpx; color: #666"></text>
            <text class="btn-text">分享</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { computed, ref, watch } from 'vue';

  /**
   * 课程类卡片组件
   * 适用于：在线课程、培训视频、教学资源等
   */
  const props = defineProps({
    // 课程数据
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

  const emit = defineEmits(['click', 'like', 'collect', 'share', 'enroll']);

  // 封面图片(取第一张)
  const coverImage = computed(() => {
    if (!props.article.coverImages || !Array.isArray(props.article.coverImages)) {
      return '/static/images/default-course-cover.png';
    }
    return props.article.coverImages[0] || '/static/images/default-course-cover.png';
  });

  const BUTTON_ORDER = ['like', 'collect', 'enroll', 'share'];
  const SUPPORTED_ACTIONS = new Set(['like', 'collect', 'enroll', 'share']);
  const DEFAULT_BUTTON_FLAGS = {
    like: true,
    collect: true,
    enroll: true,
    share: true,
  };
  const ARTICLE_FLAG_PROPS = {
    like: 'enableLike',
    collect: 'enableCollect',
    enroll: 'enableRegister',
    share: 'enableShare',
  };

  const rawButtonConfig = computed(() => props.config?.cardButtons || props.config?.buttons || {});

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

  // 事件处理
  const handleClick = () => {
    emit('click', props.article.id);
  };

  const handleShareToFriend = () => {
    emit('share', { type: 'friend', article: props.article });
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

  const handleEnroll = () => {
    emit('enroll', props.article.id);
  };
</script>

<style lang="scss" scoped>
  .course-card {
    background-color: white;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
  }

  /* 封面大图 */
  .course-cover {
    position: relative;
    width: 100%;
    height: 0;
    padding-bottom: 56.25%; /* 16:9 */
    overflow: hidden;

    .cover-image {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .permission-overlay {
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.4) 100%);
      display: flex;
      align-items: flex-end;
      justify-content: flex-start;
      padding: 24rpx;
    }

    .permission-badge-large {
      display: inline-flex;
      align-items: center;
      padding: 12rpx 24rpx;
      background: linear-gradient(135deg, rgba(255, 107, 0, 0.95) 0%, rgba(255, 140, 0, 0.95) 100%);
      border-radius: 32rpx;
      box-shadow: 0 4rpx 12rpx rgba(255, 107, 0, 0.3);

      .permission-text-large {
        font-size: 26rpx;
        color: white;
        margin-left: 8rpx;
        font-weight: 600;
      }
    }
  }

  /* 课程信息 */
  .course-info {
    padding: 24rpx;
  }

  .course-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.5;
    margin-bottom: 12rpx;
  }

  .course-subtitle {
    font-size: 26rpx;
    color: #999;
    line-height: 1.4;
    margin-bottom: 16rpx;
  }

  /* 讲师和学习人数 */
  .course-meta {
    margin-bottom: 20rpx;
    font-size: 24rpx;

    .instructor {
      flex: 1;

      .instructor-name {
        color: #666;
      }

      .official-badge {
        background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
        color: white;
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 20rpx;
        font-weight: 500;
      }
    }

    .learners {
      .learners-count {
        color: #666;
        white-space: nowrap;
      }
    }
  }

  /* 底部操作按钮 */
  .course-footer {
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
      }

      &:active {
        opacity: 0.7;
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

    .footer-btn.enroll-btn .btn-text {
      color: var(--ui-BG-Main, #8b0000);
      font-weight: 600;
    }

    .footer-btn.like-btn.active .btn-text,
    .footer-btn.collect-btn.active .btn-text {
      color: var(--ui-BG-Main, #8b0000);
      font-weight: 600;
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
