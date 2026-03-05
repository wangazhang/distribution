<template>
  <s-layout  :onShareAppMessage="state.shareInfo" navbar="inner">
    <view
      class="article-detail"
      v-if="state.article.id"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
      @touchcancel="handleTouchEnd"
    >

      <!-- 封面图片轮播 -->
      <view
        v-if="state.article.coverImages && state.article.coverImages.length > 0"
        class="cover-swiper-section"
        :style="[coverSectionStyle, coverParallaxStyle]"
      >
        <swiper
          class="cover-swiper"
          :indicator-dots="false"
          :autoplay="false"
          :circular="true"
          @change="handleCoverSwiperChange"
        >
          <swiper-item v-for="(img, index) in state.article.coverImages" :key="index">
            <image
              :src="sheep.$url.cdn(img)"
              mode="aspectFill"
              class="cover-image"
              @tap="previewCoverImage(index)"
            />
          </swiper-item>
        </swiper>
        <view
          v-if="state.article.coverImages && state.article.coverImages.length > 1"
          class="cover-swiper__dots"
          :style="coverDotsStyle"
        >
          <view
            v-for="(img, dotIndex) in state.article.coverImages"
            :key="`cover-dot-${dotIndex}`"
            class="cover-swiper__dot"
            :class="{ 'is-active': dotIndex === coverCurrentIndex }"
          ></view>
        </view>
      </view>
      <view class="article-body" :style="contentParallaxStyle">
        <view class="article-body-inner">
          <!-- 顶部信息栏：发布者信息 + 分享按钮 -->
          <view
            class="top-header bg-white"
            :class="{ 'with-cover': state.article.coverImages && state.article.coverImages.length > 0 }"
          >
            <view class="author-section ss-flex ss-row-between ss-col-center">
              <!-- 左侧：发布者信息 -->
              <view class="author-info ss-flex ss-col-center">
                <image
                    class="author-avatar"
                    :src="
                    sheep.$url.cdn(state.article.authorAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg')
                  "
                    mode="aspectFill"
                />
                <view class="author-details">
                  <view class="author-name-line ss-flex ss-col-center">
                    <text class="author-name">{{ state.article.authorName || '匿名用户' }}</text>
                    <text v-if="state.article.isOfficial" class="official-badge ss-m-l-10">官方</text>
                  </view>
                  <view class="publish-time">{{ formatTime(state.article.publishTime) }}</view>
                </view>
              </view>

              <!-- 右侧：分享按钮组 -->
              <view class="share-buttons ss-flex ss-col-center">
                <view class="share-btn moments-btn ss-flex ss-col-center" @tap="handleMomentsShare">
                  <text class="cicon-share-line-o" style="font-size: 32rpx; color: #666"></text>
                  <text class="share-text">朋友圈</text>
                </view>
                <button
                    class="share-btn friend-btn share-button ss-flex ss-col-center"
                    open-type="share"
                    @tap.stop="handleFriendShare"
                >
                  <text class="cicon-weixin" style="font-size: 32rpx; color: #666"></text>
                  <text class="share-text">好友</text>
                </button>
              </view>
            </view>
          </view>

          <!-- 素材内容区域 -->
          <view class="content-section bg-white ss-m-t-1">
            <!-- 标题 -->
            <view class="content-title">{{ state.article.title }}</view>

            <!-- 副标题 -->
            <view v-if="state.article.subtitle" class="content-subtitle ss-m-t-16">
              {{ state.article.subtitle }}
            </view>

            <!-- 正文内容 -->
            <view class="content-text ss-m-t-24">
              <!-- 富文本内容渲染 -->
              <mp-html
                v-if="state.article.contentType === 'richtext' && processedContent"
                :content="processedContent"
                :lazy-load="false"
                :selectable="true"
                :show-img-menu="true"
                :preview-img="true"
              ></mp-html>
              <!-- 纯文本内容 -->
              <text v-else class="plain-text">{{ state.article.content || '无内容' }}</text>
            </view>
          </view>

          <!-- 关联商品展示 -->
          <view
            v-if="state.article.products && state.article.products.length > 0"
            class="related-products-section bg-white ss-m-t-20"
          >
            <view class="section-header">
              <view class="section-title">
                <uni-icons type="shop" size="20" color="#ff6b00"></uni-icons>
                <text class="title-text">商品</text>
              </view>
              <text class="product-count">{{ state.article.products.length }}个商品</text>
            </view>

            <view class="products-list">
              <view
                v-for="product in state.article.products"
                :key="product.id"
                class="product-strip"
                @tap="goToProduct(product.id)"
              >
                <image
                  :src="sheep.$url.cdn(product.picUrl)"
                  class="product-thumbnail"
                  mode="aspectFill"
                />
                <view class="product-content">
                  <view class="product-name">{{ product.name }}</view>
                  <view class="product-meta">
                    <view class="price-row">
                      <text class="product-price">¥{{ (product.price / 100).toFixed(2) }}</text>
                      <text
                        v-if="product.marketPrice && product.marketPrice > product.price"
                        class="market-price"
                      >
                        ¥{{ (product.marketPrice / 100).toFixed(2) }}
                      </text>
                    </view>
                    <view class="sales-row" v-if="product.salesCount">
                      <text class="sales-text">已售{{ formatSalesCount(product.salesCount) }}</text>
                    </view>
                  </view>
                </view>
                <view class="product-actions">
                  <view class="product-badge" v-if="product.isRecommend">
                    <text class="badge-text">推荐</text>
                  </view>
                  <view class="arrow-icon">
                    <uni-icons type="right" size="16" color="#999"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </view>

        </view>
      </view>

      <!-- 底部操作栏 -->
      <view class="bottom-bar ss-flex ss-row-between ss-col-center" v-if="hasAnyAction">
        <!-- 点赞按钮 -->
        <view
          v-if="state.article.enableLike === 1"
          class="action-btn like-btn ss-flex ss-col-center"
          @tap="handleLike"
        >
          <text
            :class="state.userAction.liked ? 'cicon-thumb-up-line' : 'cicon-thumb-up-line-o'"
            :style="
              'font-size: 36rpx; color: ' +
              (state.userAction.liked ? 'var(--ui-BG-Main, #8b0000)' : '#666')
            "
          ></text>
          <text class="btn-text" :class="{ active: state.userAction.liked }">
            <template v-if="state.article.showLikeCount === 1">
              {{ getDisplayCount(state.article.likeCount, state.article.initialLikeCount) }}
            </template>
            <template v-else>
              {{ state.userAction.liked ? '已赞' : '点赞' }}
            </template>
          </text>
        </view>

        <!-- 收藏按钮 -->
        <view
          v-if="state.article.enableCollect === 1"
          class="action-btn collect-btn ss-flex ss-col-center"
          @tap="handleCollect"
        >
          <text
            :class="state.userAction.collected ? 'cicon-favorite' : 'cicon-favorite-o'"
            :style="
              'font-size: 36rpx; color: ' +
              (state.userAction.collected ? 'var(--ui-BG-Main, #8b0000)' : '#666')
            "
          ></text>
          <text class="btn-text" :class="{ active: state.userAction.collected }">
            <template v-if="state.article.showCollectCount === 1">
              {{ getDisplayCount(state.article.collectCount, state.article.initialCollectCount) }}
            </template>
            <template v-else>
              {{ state.userAction.collected ? '已收藏' : '收藏' }}
            </template>
          </text>
        </view>

        <!-- 分享按钮 -->
        <button
          v-if="state.article.enableShare === 1"
          class="action-btn share-btn share-action-button ss-flex ss-col-center"
          open-type="share"
          @tap.stop="handleFriendShare"
        >
          <text class="cicon-share" style="font-size: 36rpx; color: #666"></text>
          <text class="btn-text">
            <template v-if="state.article.showShareCount === 1">
              {{ getDisplayCount(state.article.shareCount, state.article.initialShareCount) }}
            </template>
            <template v-else> 分享 </template>
          </text>
        </button>

        <!-- 下载按钮 -->
        <view
          v-if="state.article.enableDownload === 1"
          class="action-btn download-btn ss-flex ss-col-center"
          @tap="handleDownload"
        >
          <uni-icons type="download" size="20" color="#666"></uni-icons>
          <text class="btn-text">下载</text>
        </view>

        <!-- 报名按钮 -->
        <view
          v-if="state.article.enableRegister === 1"
          class="action-btn register-btn ss-flex ss-col-center"
          @tap="handleRegister"
        >
          <uni-icons type="compose" size="20" color="#666"></uni-icons>
          <text class="btn-text">报名</text>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else class="loading-container">
      <view class="loading-text">加载中...</view>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive, computed, ref, watch } from 'vue';
  import { onLoad, onPageScroll } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import articleApi from '@/sheep/api/promotion/cms/article';
  import actionApi from '@/sheep/api/promotion/cms/action';
  import { SharePageEnum } from '@/sheep/util/const';

  const state = reactive({
    article: {},
    userAction: {
      liked: false,
      collected: false,
    },
    shareInfo: {},
  });

  // 小程序审核要求：未登录用户浏览详情时禁止弹出授权弹窗，因此需要在本地重置交互状态
  const resetUserActionState = () => {
    state.userAction.liked = false;
    state.userAction.collected = false;
  };

  const currentArticleId = ref(null);
  const userStore = sheep.$store('user');
  const isLogin = computed(() => userStore.isLogin);

  // 根据板块类型获取页面标题
  const getPageTitle = computed(() => {
    const sectionType = state.article.sectionType || 'article';
    switch (sectionType) {
      case 'article':
        return '文章详情';
      case 'dynamic':
        return '动态详情';
      case 'course':
        return '课程详情';
      case 'custom':
        return '内容详情';
      default:
        return '内容详情';
    }
  });

  // 判断是否有任何操作按钮
  const hasAnyAction = computed(() => {
    return (
      state.article.enableLike === 1 ||
      state.article.enableCollect === 1 ||
      state.article.enableShare === 1 ||
      state.article.enableDownload === 1 ||
      state.article.enableRegister === 1
    );
  });

  const COVER_INDICATOR_BOTTOM = 120;
  const MAX_PULL_DISTANCE = 180;
  const coverCurrentIndex = ref(0);

  const coverSectionStyle = computed(() => {
    const hasCover = Array.isArray(state.article.coverImages) && state.article.coverImages.length > 0;
    if (!hasCover) {
      return {};
    }
    const navHeight = sheep.$platform?.navbar || 0;
    if (!navHeight) {
      return {};
    }
    return {
      marginTop: `-${navHeight}px`,
    };
  });

  const coverDotsStyle = computed(() => ({
    bottom: `${COVER_INDICATOR_BOTTOM}rpx`,
  }));

  const scrollTop = ref(0);
  const pullDistance = ref(0);
  const isReleasing = ref(false);
  const touchState = reactive({
    startY: 0,
    pulling: false,
  });

  const coverParallaxStyle = computed(() => {
    const distance = Math.min(pullDistance.value, MAX_PULL_DISTANCE);
    const ratio = distance / MAX_PULL_DISTANCE;
    const scale = (1 + ratio * 0.1).toFixed(3);
    return {
      transform: `scale(${scale})`,
      transition: isReleasing.value ? 'transform 0.2s ease-out' : 'transform 0s',
    };
  });

  const contentParallaxStyle = computed(() => {
    const distance = Math.min(pullDistance.value, MAX_PULL_DISTANCE);
    const translate = Number((distance * 0.1).toFixed(2));
    return {
      transform: `translate3d(0, ${translate}px, 0)`,
      transition: isReleasing.value ? 'transform 0.2s ease-out' : 'transform 0s',
    };
  });

  const handleCoverSwiperChange = (event) => {
    const current = event?.detail?.current ?? 0;
    coverCurrentIndex.value = current;
  };

  watch(
    () => (Array.isArray(state.article.coverImages) ? state.article.coverImages.length : 0),
    (length) => {
      if (length <= 1) {
        coverCurrentIndex.value = 0;
      } else if (coverCurrentIndex.value >= length) {
        coverCurrentIndex.value = 0;
      }
    },
    { immediate: true },
  );

  onPageScroll((event) => {
    scrollTop.value = event?.scrollTop ?? 0;
    if (scrollTop.value > 2 && !touchState.pulling) {
      if (pullDistance.value !== 0) {
        pullDistance.value = 0;
      }
      isReleasing.value = false;
    }
  });

  const handleTouchStart = (event) => {
    if (!event.touches || !event.touches.length) {
      return;
    }
    touchState.startY = event.touches[0].clientY;
    touchState.pulling = scrollTop.value <= 2;
    isReleasing.value = false;
  };

  const handleTouchMove = (event) => {
    if (!event.touches || !event.touches.length) {
      return;
    }
    const currentY = event.touches[0].clientY;
    const delta = currentY - touchState.startY;
    if (delta > 0 && scrollTop.value <= 2) {
      touchState.pulling = true;
      const easedDistance = delta * 0.75;
      pullDistance.value = Math.min(easedDistance, MAX_PULL_DISTANCE);
    } else if (touchState.pulling) {
      pullDistance.value = 0;
    }
  };

  const handleTouchEnd = () => {
    if (pullDistance.value > 0) {
      isReleasing.value = true;
      pullDistance.value = 0;
      setTimeout(() => {
        isReleasing.value = false;
      }, 240);
    } else {
      isReleasing.value = false;
    }
    touchState.pulling = false;
  };

  const extractShareImage = (article = {}) => {
    if (Array.isArray(article.coverImages) && article.coverImages.length > 0) {
      return sheep.$url.cdn(article.coverImages[0]);
    }
    if (article.coverImage) {
      return sheep.$url.cdn(article.coverImage);
    }
    if (article.picUrl) {
      return sheep.$url.cdn(article.picUrl);
    }
    return '';
  };

  const extractShareDesc = (article = {}) => {
    if (article.subtitle) return article.subtitle;
    if (article.summary) return article.summary;
    if (article.description) return article.description;
    if (article.content) {
      return article.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').slice(0, 60);
    }
    return '';
  };

  const buildShareInfo = (article = {}) => {
    const articleId = article.id;
    const title = article.title || '精选内容';
    const image = extractShareImage(article);
    const desc = extractShareDesc(article);

    const info = sheep.$platform.share.getShareInfo(
      {
        title,
        image,
        desc,
        params: {
          page: SharePageEnum.CMS_ARTICLE.value,
          query: articleId || '0',
        },
      },
      {
        type: 'article',
        title,
        image,
      },
    );

    const sharePath = articleId ? `/pages/cms/detail?id=${articleId}` : '/pages/cms/detail';
    const query = articleId ? `id=${articleId}` : '';
    info.path = sharePath;
    info.query = query;
    info.title = info.title || title;
    info.image = info.image || image;
    if (!info.forward) {
      info.forward = {};
    }
    info.forward.path = sharePath;

    return info;
  };

  const prepareShareInfo = () => {
    state.shareInfo = buildShareInfo(state.article);
    sheep.$platform.share.updateShareInfo(state.shareInfo);
  };

  const recordShareStat = async () => {
    if (!state.article.id) return;
    try {
      await actionApi.share(state.article.id);
      if (state.article.shareCount !== undefined) {
        state.article.shareCount++;
      }
    } catch (error) {
      console.error('分享计数失败:', error);
    }
  };

  // 处理HTML内容,使其兼容mp-html
  const processHtmlContent = (htmlContent) => {
    if (!htmlContent) return '';

    let processedHtml = htmlContent;

    // 处理自定义video标签的div包裹
    processedHtml = processedHtml.replace(
      /<div[^>]*data-w-e-type="video"[^>]*>(.*?)<\/div>/gs,
      (match, content) => {
        // 提取video标签
        const videoMatch = content.match(/<video[^>]*>(.*?)<\/video>/s);
        if (videoMatch) {
          let videoTag = videoMatch[0];

          // 修复poster属性(如果值是"mp4"则移除)
          videoTag = videoTag.replace(/poster="mp4"/g, '');

          // 修复width和height属性
          videoTag = videoTag.replace(/width="auto"/g, 'width="100%"');
          videoTag = videoTag.replace(/height="auto"/g, '');

          return videoTag;
        }
        return match;
      },
    );

    // 移除data-w-e-*属性
    processedHtml = processedHtml.replace(/\sdata-w-e-[^=]*="[^"]*"/g, '');

    // 移除空的p标签或只包含br的p标签
    processedHtml = processedHtml.replace(/<p>\s*<br\s*\/?>\s*<\/p>/g, '');
    processedHtml = processedHtml.replace(/<p>\s*<\/p>/g, '');

    // 确保图片有正确的样式
    processedHtml = processedHtml.replace(
      /<img([^>]*)style="width: [^;]*;height: [^;]*;"/g,
      '<img$1style="max-width: 100%; height: auto;"',
    );

    return processedHtml.trim();
  };

  // 处理后的HTML内容
  const processedContent = computed(() => {
    return processHtmlContent(state.article.content);
  });

  // 获取显示的数量(真实数据 + 初始假数据)
  const getDisplayCount = (realCount, initialCount) => {
    const total = (realCount || 0) + (initialCount || 0);
    if (total >= 10000) {
      return (total / 10000).toFixed(1) + 'w';
    } else if (total >= 1000) {
      return (total / 1000).toFixed(1) + 'k';
    }
    return total;
  };

  // 加载文章详情
  const normalizeFlag = (value, fallback = 0) => {
    if (value === undefined || value === null) return fallback;
    if (typeof value === 'boolean') return value ? 1 : 0;
    if (typeof value === 'number') return value ? 1 : 0;
    if (typeof value === 'string') {
      const lower = value.trim().toLowerCase();
      if (lower === 'true' || lower === '1') return 1;
      if (lower === 'false' || lower === '0') return 0;
    }
    return fallback;
  };

  const normalizeCoverImages = (images) => {
    if (Array.isArray(images)) {
      return images.filter((item) => !!item);
    }
    if (typeof images === 'string') {
      const trimmed = images.trim();
      if (!trimmed) {
        return [];
      }
      try {
        const parsed = JSON.parse(trimmed);
        if (Array.isArray(parsed)) {
          return parsed.filter((item) => !!item);
        }
      } catch (error) {
        // 非 JSON 字符串，降级为单图数组
      }
      return [trimmed];
    }
    return [];
  };

  const loadArticle = async (id) => {
    currentArticleId.value = id;
    const res = await articleApi.detail(id);
    if (res.code === 0) {
      state.article = res.data;
      let coverImages = normalizeCoverImages(res.data.coverImages);
      if (coverImages.length === 0 && res.data.coverImage) {
        coverImages = normalizeCoverImages(res.data.coverImage);
      }
      if (coverImages.length === 0 && res.data.picUrl) {
        coverImages = normalizeCoverImages(res.data.picUrl);
      }
      state.article.coverImages = coverImages;
      state.article.enableLike = normalizeFlag(state.article.enableLike, 1);
      state.article.enableCollect = normalizeFlag(state.article.enableCollect, 1);
      state.article.enableShare = normalizeFlag(state.article.enableShare, 1);
      state.article.enableDownload = normalizeFlag(state.article.enableDownload, 1);
      state.article.enableRegister = normalizeFlag(state.article.enableRegister, 0);
      state.article.showLikeCount = normalizeFlag(state.article.showLikeCount, 1);
      state.article.showCollectCount = normalizeFlag(state.article.showCollectCount, 1);
      state.article.showShareCount = normalizeFlag(state.article.showShareCount, 1);
      prepareShareInfo();
      // 增加浏览数
      await articleApi.view(id);
      // 检查用户操作状态（仅在登录后触发，避免未浏览完就弹登录框）
      if (isLogin.value) {
        await checkUserAction(id);
      } else {
        resetUserActionState();
      }
      coverCurrentIndex.value = 0;
      pullDistance.value = 0;
      isReleasing.value = false;
      touchState.pulling = false;
    }
  };

  // 检查用户操作状态
  const checkUserAction = async (articleId) => {
    if (!isLogin.value) {
      resetUserActionState();
      return;
    }
    try {
      // 分别检查点赞和收藏状态
      const [likeRes, collectRes] = await Promise.all([
        actionApi.check(articleId, 'like'),
        actionApi.check(articleId, 'collect'),
      ]);

      if (likeRes.code === 0) {
        state.userAction.liked =
          likeRes.data === true ||
          likeRes.data === 1 ||
          likeRes.data === '1' ||
          (typeof likeRes.data === 'string' && likeRes.data.toLowerCase() === 'true');
      }
      if (collectRes.code === 0) {
        state.userAction.collected =
          collectRes.data === true ||
          collectRes.data === 1 ||
          collectRes.data === '1' ||
          (typeof collectRes.data === 'string' && collectRes.data.toLowerCase() === 'true');
      }
    } catch (error) {
      console.error('检查用户操作状态失败:', error);
    }
  };

  // 监听登录状态变更，只有用户主动登录后才去请求个人交互数据
  watch(
    () => isLogin.value,
    (loggedIn) => {
      if (!currentArticleId.value) {
        if (!loggedIn) {
          resetUserActionState();
        }
        return;
      }
      if (loggedIn) {
        checkUserAction(currentArticleId.value);
      } else {
        resetUserActionState();
      }
    },
  );

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

    return `${year}-${month}-${day} ${hour}:${minute}`;
  };

  // 预览封面图片
  const previewCoverImage = (index) => {
    if (!state.article.coverImages || state.article.coverImages.length === 0) return;

    const urls = state.article.coverImages.map((img) => sheep.$url.cdn(img));
    uni.previewImage({
      current: index,
      urls: urls,
    });
  };

  // 分享朋友圈
  const handleMomentsShare = async () => {
    await recordShareStat();
    handleDownload();
  };

  // 分享给好友
  const handleFriendShare = async () => {
    prepareShareInfo();
    await recordShareStat();
  };

  // 处理下载/保存操作
  const handleDownload = () => {
    const articleId = state.article.id;
    uni.navigateTo({
      url: `/pages/cms/download?id=${articleId}`,
    });
  };

  // 点赞
  const handleLike = async () => {
    try {
      const res = await actionApi.like(state.article.id);
      if (res.code === 0) {
        const newLikedState = !!res.data;
        if (newLikedState && !state.userAction.liked) {
          state.article.likeCount++;
        } else if (!newLikedState && state.userAction.liked && state.article.likeCount > 0) {
          state.article.likeCount--;
        }
        state.userAction.liked = newLikedState;
        sheep.$helper.toast(newLikedState ? '已点赞' : '已取消点赞');
      } else {
        sheep.$helper.toast(res.msg || '点赞失败');
      }
    } catch (error) {
      console.error('点赞失败:', error);
      sheep.$helper.toast('点赞失败');
    }
  };
  // 收藏
  const handleCollect = async () => {
    try {
      const res = await actionApi.collect(state.article.id);
      if (res.code === 0) {
        const newCollectedState = !!res.data;
        if (newCollectedState && !state.userAction.collected) {
          if (state.article.collectCount !== undefined) {
            state.article.collectCount++;
          }
        } else if (
          !newCollectedState &&
          state.userAction.collected &&
          state.article.collectCount !== undefined &&
          state.article.collectCount > 0
        ) {
          state.article.collectCount--;
        }
        state.userAction.collected = newCollectedState;
        sheep.$helper.toast(newCollectedState ? '已收藏' : '已取消收藏');
      } else {
        sheep.$helper.toast(res.msg || '收藏失败');
      }
    } catch (error) {
      console.error('收藏失败:', error);
      sheep.$helper.toast('收藏失败');
    }
  };

  // 报名（课程）
  const handleRegister = () => {
    // TODO: 实现课程报名功能
    sheep.$helper.toast('课程报名功能开发中');
    // 未来可以跳转到报名页面或弹出报名表单
    // uni.navigateTo({
    //   url: `/pages/course/register?id=${state.article.id}`
    // });
  };

  // 格式化销量数字
  const formatSalesCount = (count) => {
    if (!count || count === 0) return '0';
    if (count >= 10000) {
      return (count / 10000).toFixed(1) + 'w+';
    } else if (count >= 1000) {
      return (count / 1000).toFixed(1) + 'k+';
    }
    return count.toString();
  };

  // 跳转到商品详情
  const goToProduct = (productId) => {
    sheep.$router.go('/pages/goods/index', { id: productId });
  };

  onLoad(async (options) => {
    if (options.id) {
      await loadArticle(options.id);
    }
  });
</script>

<style lang="scss" scoped>
  .article-detail {
    min-height: 100vh;
    background-color: #f7f8fa;
    padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
  }

  .article-body {
    margin-top: -64rpx;
    position: relative;
    z-index: 2;
    background: #ffffff;
    border-top-left-radius: 48rpx;
    border-top-right-radius: 48rpx;
    box-shadow: 0 -12rpx 32rpx rgba(0, 0, 0, 0.04);
    will-change: transform;
    min-height: calc(100vh - 200rpx);
    overflow: hidden;
    transform-origin: center top;
  }

  .article-body-inner {
    position: relative;
    z-index: 2;
    background: transparent;
    padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
  }

  /* 封面图片轮播 */
  .cover-swiper-section {
    width: 100%;
    background-color: #000;
    position: relative;
    will-change: transform;
    transform-origin: center top;
    z-index: 1;

    .cover-swiper {
      width: 100%;
      height: 800rpx;
      overflow: hidden;

      .cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .cover-swiper__dots {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 120rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16rpx;
      z-index: 3;
      pointer-events: none;
      transform-origin: center;

      .cover-swiper__dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.4);
        transition: all 0.2s ease;
        transform: scale(1);

        &.is-active {
          background: #ffffff;
          transform: scale(1.3);
        }
      }
    }
  }

  /* 顶部信息栏 */
  .top-header {
    padding: 30rpx;
    border-top-left-radius: 50rpx;
    border-top-right-radius: 50rpx;
    background-color: #ffffff;
    will-change: transform;
    //box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.08);
    position: relative;
    z-index: 2;
    overflow: hidden;

    &.with-cover {
      //margin-top: -64rpx;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, #ffffff 65%);
      backdrop-filter: blur(18rpx);
    }

    .author-section {
      .author-info {
        flex: 1;

        .author-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 50%;
          margin-right: 16rpx;
          flex-shrink: 0;
        }

        .author-details {
          .author-name-line {
            margin-bottom: 8rpx;

            .author-name {
              font-size: 30rpx;
              font-weight: 600;
              color: #333;
            }

            .official-badge {
              background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
              color: white;
              padding: 6rpx 12rpx;
              border-radius: 8rpx;
              font-size: 20rpx;
              font-weight: 500;
            }
          }

          .publish-time {
            font-size: 22rpx;
            color: #999;
          }
        }
      }

      .share-buttons {
        gap: 24rpx;

        .share-btn {
          flex-direction: column;
          align-items: center;
          padding: 6rpx 12rpx;
          gap: 2rpx;
          position: relative;
          transition: all 0.2s ease;

          .share-text {
            font-size: 18rpx;
            color: #999;
            white-space: nowrap;
            font-weight: 400;
          }

          &:active {
            transform: scale(0.95);

            text {
              color: #ff6b00 !important;
            }
          }

          // 朋友圈按钮特殊样式
          // 好友按钮特殊样式
          &.friend-btn {
            &:hover text {
              color: #07c160;
            }
          }
        }
      }
    }
  }

  /* 内容区域 */
  .content-section {
    padding: 30rpx;

    .content-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      line-height: 1.5;
      text-align: center;
    }

    .content-subtitle {
      font-size: 28rpx;
      color: #666;
      line-height: 1.5;
      text-align: center;
    }

    .content-text {
      font-size: 28rpx;
      line-height: 1.8;
      color: #333;

      .plain-text {
        white-space: pre-wrap;
        word-break: break-word;
      }

      :deep(img) {
        max-width: 100%;
        height: auto;
        display: block;
        margin: 20rpx 0;
        border-radius: 8rpx;
      }

      // mp-html 组件样式
      :deep(.mp-html) {
        img {
          max-width: 100%;
          height: auto;
          display: block;
          margin: 20rpx 0;
          border-radius: 8rpx;
        }

        p {
          margin: 16rpx 0;
          line-height: 1.8;
        }

        h1,
        h2,
        h3,
        h4,
        h5,
        h6 {
          margin: 20rpx 0;
          font-weight: bold;
          line-height: 1.4;
        }

        h1 {
          font-size: 36rpx;
        }
        h2 {
          font-size: 34rpx;
        }
        h3 {
          font-size: 32rpx;
        }
        h4 {
          font-size: 30rpx;
        }
        h5 {
          font-size: 28rpx;
        }
        h6 {
          font-size: 26rpx;
        }

        ul,
        ol {
          margin: 16rpx 0;
          padding-left: 32rpx;
        }

        li {
          margin: 8rpx 0;
        }

        blockquote {
          margin: 20rpx 0;
          padding: 16rpx;
          background-color: #f7f8fa;
          border-left: 8rpx solid #e6e8eb;
          border-radius: 4rpx;
        }

        table {
          width: 100%;
          border-collapse: collapse;
          margin: 20rpx 0;
        }

        th,
        td {
          border: 1rpx solid #e6e8eb;
          padding: 16rpx;
          text-align: left;
        }

        th {
          background-color: #f7f8fa;
          font-weight: bold;
        }

        a {
          color: #007aff;
          text-decoration: none;
        }

        code {
          padding: 4rpx 8rpx;
          background-color: #f7f8fa;
          border-radius: 4rpx;
          font-family: monospace;
        }

        pre {
          padding: 20rpx;
          background-color: #f7f8fa;
          border-radius: 8rpx;
          overflow-x: auto;
          margin: 20rpx 0;

          code {
            padding: 0;
            background: none;
          }
        }
      }
    }
  }

  /* 底部操作栏 */
  .bottom-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 24rpx 30rpx calc(24rpx + env(safe-area-inset-bottom));
    border-top: 1rpx solid #f0f0f0;
    background: #ffffff;
    box-shadow: 0 -4rpx 14rpx rgba(0, 0, 0, 0.06);
    z-index: 5;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 10rpx 0;
      gap: 4rpx;

      .btn-text {
        font-size: 22rpx;
        color: #666;
        margin-top: 2rpx;

        &.active {
          font-weight: 600;
        }
      }

      &:active {
        opacity: 0.6;
      }

      // 特殊按钮样式
      &.like-btn {
        .btn-text.active {
          color: var(--ui-BG-Main, #8b0000);
        }
      }

      &.collect-btn {
        .btn-text.active {
          color: var(--ui-BG-Main, #8b0000);
        }
      }
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
  }

  .loading-text {
    font-size: 28rpx;
    color: #999;
  }

  /* 关联商品展示样式 */
  .related-products-section {
    padding: 30rpx;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;
      padding-bottom: 16rpx;
      border-bottom: 1rpx solid #f0f0f0;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .title-text {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
        }
      }

      .product-count {
        font-size: 24rpx;
        color: #999;
      }
    }

    .products-list {
      display: flex;
      flex-direction: column;
      gap: 16rpx;
    }

    .product-strip {
      background-color: #fff;
      border-radius: 12rpx;
      border: 1rpx solid #f0f0f0;
      padding: 20rpx;
      display: flex;
      align-items: center;
      gap: 20rpx;
      position: relative;
      transition: all 0.2s ease;

      &:active {
        transform: translateX(4rpx);
        background-color: #fafafa;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
      }

      .product-thumbnail {
        width: 120rpx;
        height: 120rpx;
        border-radius: 8rpx;
        object-fit: cover;
        flex-shrink: 0;
      }

      .product-content {
        flex: 1;
        min-width: 0;

        .product-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
          line-height: 1.4;
          margin-bottom: 12rpx;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          overflow: hidden;
        }

        .product-meta {
          display: flex;
          flex-direction: column;
          gap: 6rpx;

          .price-row {
            display: flex;
            align-items: center;
            gap: 12rpx;

            .product-price {
              font-size: 32rpx;
              font-weight: bold;
              color: #ff6b00;
            }

            .market-price {
              font-size: 24rpx;
              color: #999;
              text-decoration: line-through;
            }
          }

          .sales-row {
            .sales-text {
              font-size: 22rpx;
              color: #999;
            }
          }
        }
      }

      .product-actions {
        display: flex;
        align-items: center;
        gap: 12rpx;
        flex-shrink: 0;

        .product-badge {
          background: linear-gradient(135deg, #ff6b00 0%, #ff8c00 100%);
          color: white;
          padding: 6rpx 12rpx;
          border-radius: 16rpx;
          font-size: 20rpx;

          .badge-text {
            font-weight: 500;
            white-space: nowrap;
          }
        }

        .arrow-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 32rpx;
          height: 32rpx;
          border-radius: 50%;
          background-color: #f5f5f5;
        }
      }
    }
  }

  .share-button,
  .share-action-button {
    border: none;
    background: transparent;
    padding: 0;
    margin: 0;
    line-height: 1;

    &::after {
      display: none;
    }
  }
</style>
