<template>
  <s-layout title="我的互动">
    <view class="interaction-page">
      <!-- Tab切换 -->
      <su-sticky :customNavHeight="sys_navBar" bgColor="var(--ui-BG)">
        <view class="tab-header bg-white">
          <view class="tab-list">
            <view
              v-for="tab in state.tabs"
              :key="tab.type"
              class="tab-item"
              :class="{ active: state.currentTab === tab.type }"
              @tap="switchTab(tab.type)"
            >
              <text class="tab-text">{{ tab.name }}</text>
            </view>
          </view>

          <!-- 点赞收藏二级筛选 - 支持滑动 -->
          <view v-if="state.currentTab === 'favorite'" class="sub-tab-filter-scroll">
            <scroll-view
              class="sub-tab-filter-scroll-view"
              scroll-x
              :scroll-left="state.favoriteSubTabScrollLeft"
              :scroll-with-animation="true"
            >
              <view class="sub-tab-filter-wrapper">
                <view
                  v-for="(subTab, index) in state.favoriteSubTabs"
                  :key="subTab.key"
                  :id="`favorite-tab-${index}`"
                  class="filter-item"
                  :class="{ active: state.favoriteSubTab === subTab.key }"
                  @tap="handleFavoriteSubTabClick(subTab.key)"
                >
                  {{ subTab.name }}
                </view>
              </view>
            </scroll-view>
          </view>

          <!-- 我发布的二级筛选 -->
          <view v-if="state.currentTab === 'publish'" class="sub-tab-filter">
            <view
              v-for="(subTab, index) in publishSubTabs"
              :key="subTab.key"
              class="filter-item"
              :class="{ active: state.publishSubTab === subTab.key }"
              @tap="handlePublishSubTabClick(subTab.key, index)"
            >
              {{ subTab.name }}
            </view>
          </view>
        </view>
      </su-sticky>

      <!-- 内容区域 -->
      <view class="content-container">
        <template>
          <swiper
            class="main-content-swiper"
            :current="state.mainSwiperIndex"
            :duration="300"
            @change="onMainContentSwiperChange"
          >
            <swiper-item
              v-for="(slide, index) in extendedSlides"
              :key="slide.key"
              class="main-swiper-item"
            >
              <template v-if="slide.type === 'favorite'">
                <view class="tab-content favorite-tab-panel">
                  <view class="favorite-content-wrapper">
                    <view
                      v-if="getFavoriteDisplayList(slide.subKey).length === 0"
                      class="empty-state"
                    >
                      <view class="empty-icon">
                        <text
                          :class="getFavoriteEmptyMeta(slide.subKey).icon"
                          style="font-size: 120rpx; color: var(--ui-TC-4);"
                        ></text>
                      </view>
                      <view class="empty-text">{{ getFavoriteEmptyMeta(slide.subKey).title }}</view>
                      <view class="empty-desc">{{ getFavoriteEmptyMeta(slide.subKey).desc }}</view>
                      <button
                        v-if="getFavoriteEmptyMeta(slide.subKey).action"
                        class="browse-btn"
                        @tap="getFavoriteEmptyMeta(slide.subKey).action()"
                      >
                        {{ getFavoriteEmptyMeta(slide.subKey).actionText }}
                      </button>
                    </view>

                    <view v-else class="content-list">
                      <view
                        v-for="record in getFavoriteDisplayList(slide.subKey)"
                        :key="record.item.id"
                        class="interaction-card"
                        :class="record.meta.layoutClass"
                      >
                        <template v-if="record.meta.layoutClass === 'interaction-card-incoming'">
                          <view class="incoming-body" @tap="goToDetail(record.item.articleId)">
                            <view class="incoming-info">
                              <image
                                class="interaction-avatar"
                                :src="record.meta.actorAvatar"
                                mode="aspectFill"
                              />
                              <view class="incoming-text">
                                <text class="interaction-title">{{ record.meta.actorName }}</text>
                                <text class="interaction-desc">{{ record.meta.description }}</text>
                                <text class="interaction-time">{{ record.meta.timeText }}</text>
                              </view>
                            </view>
                            <image
                              v-if="record.meta.cover"
                              class="incoming-cover"
                              :src="record.meta.cover"
                              mode="aspectFill"
                            />
                          </view>
                        </template>

                        <template v-else>
                          <view class="outgoing-body">
                            <view class="outgoing-main" @tap="goToDetail(record.item.articleId)">
                              <view class="outgoing-header">
                                <image
                                  class="outgoing-author-avatar"
                                  :src="record.meta.authorAvatar"
                                  mode="aspectFill"
                                />
                                <view class="outgoing-info">
                                  <text class="article-title">{{ record.meta.articleTitle }}</text>
                                  <text
                                    v-if="record.meta.publishTimeText"
                                    class="article-time"
                                  >
                                    {{ record.meta.publishTimeText }}
                                  </text>
                                </view>
                              </view>
                              <view v-if="record.meta.gallery.length" class="outgoing-gallery">
                                <image
                                  v-for="(img, imgIndex) in record.meta.gallery"
                                  :key="imgIndex"
                                  class="outgoing-gallery-item"
                                  :src="img"
                                  mode="aspectFill"
                                />
                              </view>
                            </view>
                            <view class="outgoing-actions">
                              <text
                                class="outgoing-action-icon"
                                :class="record.meta.actionIcon"
                                :style="{ color: record.meta.actionColor }"
                                @tap.stop="record.meta.onAction && record.meta.onAction()"
                              ></text>
                            </view>
                          </view>
                        </template>
                      </view>
                    </view>
                  </view>
                </view>
              </template>

              <template v-else>
                <view class="tab-content publish-tab-panel">
                  <swiper
                    class="publish-content-swiper"
                    :current="state.publishSubTabIndex"
                    :duration="300"
                    @change="onPublishContentSwiperChange"
                    @touchstart="handlePublishSwiperTouchStart"
                    @touchmove="handlePublishSwiperTouchMove"
                    @touchend="handlePublishSwiperTouchEnd"
                    @touchcancel="handlePublishSwiperTouchEnd"
                  >
                    <swiper-item
                      v-for="(subTab, publishIndex) in publishSubTabs"
                      :key="subTab.key"
                      class="publish-swiper-item"
                    >
                      <view class="publish-content-wrapper">
                        <template
                          v-if="getPublishList(subTab.key).length === 0 && isPublishTabLoaded(subTab.key)"
                        >
                          <view class="empty-state">
                            <view class="empty-icon">
                              <text
                                :class="getPublishEmptyMeta(subTab.key).icon"
                                style="font-size: 120rpx; color: var(--ui-TC-4);"
                              ></text>
                            </view>
                            <view class="empty-text">{{ getPublishEmptyMeta(subTab.key).title }}</view>
                            <view class="empty-desc">{{ getPublishEmptyMeta(subTab.key).desc }}</view>
                            <button
                              v-if="getPublishEmptyMeta(subTab.key).action"
                              class="publish-btn"
                              @tap="getPublishEmptyMeta(subTab.key).action()"
                            >
                              {{ getPublishEmptyMeta(subTab.key).actionText }}
                            </button>
                          </view>
                        </template>

                        <view v-else class="content-list">
                          <view
                            v-for="item in getPublishList(subTab.key)"
                            :key="item.id"
                            class="content-item"
                            @tap="goToDetail(item.id)"
                          >
                            <view class="article-card">
                              <!-- 头部信息 -->
                              <view class="card-header ss-flex ss-col-center ss-m-b-20">
                                <image
                                  class="author-avatar"
                                  :src="sheep.$url.cdn(item.authorAvatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg')"
                                  mode="aspectFill"
                                />
                                <view class="author-info ss-flex-1">
                                  <view class="author-name-line ss-flex ss-col-center">
                                    <text class="author-name">{{ item.authorName || '我' }}</text>
                                    <text v-if="item.isOfficial" class="official-badge ss-m-l-10">官方</text>
                                  </view>
                                  <view class="publish-time">{{ formatTime(item.publishTime) }}</view>
                                </view>
                                <view
                                  class="status-badge"
                                  :class="{ published: item.auditStatus === 'approved' && item.publishStatus === 1 }"
                                >
                                  {{ getStatusText(item) }}
                                </view>
                              </view>

                              <!-- 内容区域 -->
                              <view class="card-content">
                                <text v-if="item.title" class="article-title">{{ item.title }}</text>
                                <text v-if="item.subtitle" class="article-subtitle">{{ item.subtitle }}</text>
                                <view
                                  v-if="item.coverImages && item.coverImages.length > 0"
                                  class="cover-section"
                                >
                                  <image
                                    :src="sheep.$url.cdn(item.coverImages[0])"
                                    mode="aspectFill"
                                    class="cover-image"
                                  />
                                </view>
                              </view>

                              <!-- 底部信息 -->
                              <view class="card-footer ss-flex ss-row-between ss-col-center">
                                <view class="article-stats ss-flex ss-col-center">
                                  <view class="stat-item ss-flex ss-col-center">
                                    <text class="cicon-thumb-up-line" style="font-size: 24rpx; color: var(--ui-TC-3);"></text>
                                    <text class="stat-text">{{ item.likeCount || 0 }}</text>
                                  </view>
                                  <view class="stat-item ss-flex ss-col-center ss-m-l-20">
                                    <text class="cicon-favorite-o" style="font-size: 24rpx; color: var(--ui-TC-3);"></text>
                                    <text class="stat-text">{{ item.collectCount || 0 }}</text>
                                  </view>
                                  <view class="stat-item ss-flex ss-col-center ss-m-l-20">
                                    <text class="cicon-eye" style="font-size: 24rpx; color: var(--ui-TC-3);"></text>
                                    <text class="stat-text">{{ item.viewCount || 0 }}</text>
                                  </view>
                                </view>

                                <view class="actions">
                                  <view class="action-btn edit-btn ss-flex ss-col-center" @tap.stop="handleEdit(item.id)">
                                    <text class="cicon-edit" style="font-size: 28rpx; color: var(--ui-TC-3);"></text>
                                    <text class="action-text">编辑</text>
                                  </view>
                                </view>
                              </view>
                            </view>
                          </view>
                        </view>

                        <view
                          v-if="getPublishList(subTab.key).length > 0 && getPublishTabState(subTab.key).loadStatus === 'loading'"
                          class="load-more"
                        >
                          <view class="loading-spinner"></view>
                        </view>
                      </view>
                    </swiper-item>
                  </swiper>
                </view>
              </template>
            </swiper-item>
          </swiper>
        </template>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive, computed, watch } from 'vue';
  import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import actionApi from '@/sheep/api/promotion/cms/action';
  import articleApi from '@/sheep/api/promotion/cms/article';
  import commentApi from '@/sheep/api/promotion/cms/comment';

  const sys_navBar = sheep.$platform.navbar;

  const state = reactive({
    tabs: [
      { type: 'favorite', name: '互动', count: 0 },
      { type: 'publish', name: '我发布', count: 0 },
      // { type: 'comment', name: '评论', count: 0 },
      // { type: 'reply', name: '回复', count: 0 },
    ],
    currentTab: 'favorite',

    // 点赞收藏子Tab筛选
    favoriteSubTab: 'likedByMe',
    favoriteSubTabs: [
      { key: 'likedByMe', name: '我点赞的' },
      { key: 'likedMe', name: '点赞我的' },
      { key: 'collectedByMe', name: '我收藏的' },
      { key: 'collectedMe', name: '收藏我的' },
    ],
    favoriteSubTabIndex: 0,
    mainSwiperIndex: 0,
    favoriteSubTabScrollLeft: 0,
    // 记录每个子tab是否已加载过数据
    favoriteSubTabLoaded: {
      likedByMe: false,
      likedMe: false,
      collectedByMe: false,
      collectedMe: false,
    },

    // 我发布子Tab筛选
    publishSubTab: 'all',
    publishSubTabIndex: 0,

    loading: true,
    likedByMeList: [],
    likedMeList: [],
    collectedByMeList: [],
    collectedMeList: [],
    commentList: [],
    replyList: [],
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    loadStatus: 'more',
  });

  const favoriteListKeyMap = {
    likedByMe: 'likedByMeList',
    likedMe: 'likedMeList',
    collectedByMe: 'collectedByMeList',
    collectedMe: 'collectedMeList',
  };

  const publishSubTabs = [
    { key: 'all', name: '全部' },
    { key: 'review', name: '待审核' },
    { key: 'published', name: '已发布' },
  ];

  const createPublishTabState = () => ({
    list: [],
    pageNo: 1,
    hasMore: true,
    loadStatus: 'more',
    loaded: false,
  });

  const publishTabState = reactive({
    all: createPublishTabState(),
    review: createPublishTabState(),
    published: createPublishTabState(),
  });

  const getPublishTabState = (subKey) => publishTabState[subKey] || publishTabState.all;

  const getPublishList = (subKey) => {
    const tabState = getPublishTabState(subKey);
    return tabState.list || [];
  };

  const isPublishTabLoaded = (subKey) => getPublishTabState(subKey).loaded === true;

  const syncPublishStateToGlobal = (subKey) => {
    const tabState = getPublishTabState(subKey);
    state.pageNo = tabState.pageNo;
    state.hasMore = tabState.hasMore;
    state.loadStatus = tabState.loadStatus;
  };

  const extractTouchPoint = (touch) => ({
    x: touch?.clientX ?? touch?.pageX ?? touch?.x ?? 0,
    y: touch?.clientY ?? touch?.pageY ?? touch?.y ?? 0,
  });

  const publishSwipeState = reactive({
    startX: 0,
    startY: 0,
    tracking: false,
    startIndex: 0,
  });

  const PUBLISH_SWIPE_MIN_DISTANCE = 80;
  const PUBLISH_SWIPE_MAX_VERTICAL = 90;


  const extendedSlides = computed(() => {
    const slides = state.favoriteSubTabs.map((tab) => ({
      type: 'favorite',
      key: `favorite-${tab.key}`,
      subKey: tab.key,
    }));
    slides.push({ type: 'publish', key: 'publish' });
    return slides;
  });

  watch(
    () => [state.currentTab, state.favoriteSubTab, state.favoriteSubTabIndex, state.favoriteSubTabs.length],
    () => {
      if (state.currentTab === 'favorite') {
        const index = state.favoriteSubTabs.findIndex((tab) => tab.key === state.favoriteSubTab);
        if (index !== -1 && state.mainSwiperIndex !== index) {
          state.mainSwiperIndex = index;
        }
      } else if (state.currentTab === 'publish') {
        const publishIndex = state.favoriteSubTabs.length;
        if (state.mainSwiperIndex !== publishIndex) {
          state.mainSwiperIndex = publishIndex;
        }
      }
    }
  );

  // 切换Tab
  const switchTab = async (tabType) => {
    if (state.currentTab === tabType) return;

    state.currentTab = tabType;

    if (tabType === 'favorite') {
      if (!state.favoriteSubTab) {
        state.favoriteSubTab = 'likedByMe';
      }
      if (state.favoriteSubTabIndex === undefined) {
        state.favoriteSubTabIndex = 0;
      }
      state.mainSwiperIndex = state.favoriteSubTabIndex;

      uni.nextTick(() => {
        updateFavoriteSubTabScrollLeft();
      });

      await ensureFavoriteSubTabActive(state.favoriteSubTab);
      state.loading = false;
      return;
    }

    if (tabType === 'publish') {
      const activeIndex = state.publishSubTabIndex ?? 0;
      const target = publishSubTabs[activeIndex] || publishSubTabs[0];
      state.publishSubTabIndex = publishSubTabs.findIndex((item) => item.key === target.key);
      await activatePublishSubTab(target.key);
      state.mainSwiperIndex = state.favoriteSubTabs.length;
      return;
    }

    state.pageNo = 1;
    state.hasMore = true;
    state.loadStatus = 'loading';
    await loadContent(true);
  };

  // 切换点赞收藏子Tab（仅切换，不加载数据）
  const switchFavoriteSubTabOnly = async (subTab) => {
    if (state.favoriteSubTab === subTab) return;

    const index = state.favoriteSubTabs.findIndex(tab => tab.key === subTab);
    if (index !== -1) {
      state.favoriteSubTabIndex = index;
      updateFavoriteSubTabScrollLeft();
    }

    state.favoriteSubTab = subTab;

    // 检查是否有数据，如果没有则加载
    const listKey = favoriteListKeyMap[subTab];
    if (listKey && state[listKey].length === 0 && !state.favoriteSubTabLoaded[subTab]) {
      // 如果数据为空且未加载过，则加载数据
      await switchFavoriteSubTabWithLoad(subTab);
    }
  };

  // 切换点赞收藏子Tab并加载数据
  const switchFavoriteSubTabWithLoad = async (subTab) => {
    if (state.favoriteSubTab === subTab && state.favoriteSubTabLoaded[subTab]) {
      // 如果已经是当前tab且已加载过，则不需要重新加载
      return;
    }

    const index = state.favoriteSubTabs.findIndex(tab => tab.key === subTab);
    if (index !== -1) {
      state.favoriteSubTabIndex = index;
      updateFavoriteSubTabScrollLeft();
    }

    state.favoriteSubTab = subTab;
    state.pageNo = 1;
    state.hasMore = true;
    state.loadStatus = 'loading';

    const listKey = favoriteListKeyMap[subTab];
    if (listKey) {
      state[listKey] = [];
    }

    await loadContent(true);
    // 标记为已加载
    state.favoriteSubTabLoaded[subTab] = true;
  };

  // 切换点赞收藏子Tab（兼容旧代码，点击时使用）
  const switchFavoriteSubTab = async (subTab) => {
    await switchFavoriteSubTabWithLoad(subTab);
  };

  // 处理头部tab点击事件（点击时如果有数据就切换，没有数据就加载）
  const handleFavoriteSubTabClick = async (subTab) => {
    if (state.favoriteSubTab === subTab) return;

    const listKey = favoriteListKeyMap[subTab];
    // 如果已有数据，只切换不加载；如果没有数据，则加载
    if (listKey && state[listKey].length > 0) {
      // 有数据，只切换
      switchFavoriteSubTabOnly(subTab);
    } else {
      // 没有数据，加载
      await switchFavoriteSubTabWithLoad(subTab);
    }
  };

  // 计算头部tab滚动位置（使其居中显示）
  const updateFavoriteSubTabScrollLeft = () => {
    uni.nextTick(() => {
      const index = state.favoriteSubTabIndex;
      const query = uni.createSelectorQuery();
      query.select(`#favorite-tab-${index}`).boundingClientRect();
      query.select('.sub-tab-filter-wrapper').boundingClientRect();
      query.selectViewport().scrollOffset();
      query.exec((res) => {
        if (res[0] && res[1]) {
          const itemLeft = res[0].left;
          const wrapperWidth = res[1].width;
          const itemWidth = res[0].width;
          const scrollLeft = itemLeft - (wrapperWidth / 2) + (itemWidth / 2);
          state.favoriteSubTabScrollLeft = Math.max(0, scrollLeft);
        }
      });
    });
  };

  const ensureFavoriteSubTabActive = async (subTabKey) => {
    const listKey = favoriteListKeyMap[subTabKey];
    const hasData = listKey ? (state[listKey] && state[listKey].length > 0) : false;
    const loaded = state.favoriteSubTabLoaded[subTabKey];

    if (state.favoriteSubTab !== subTabKey) {
      if (hasData) {
        await switchFavoriteSubTabOnly(subTabKey);
      } else {
        await switchFavoriteSubTabWithLoad(subTabKey);
      }
      return;
    }

    if (!hasData && !loaded) {
      await switchFavoriteSubTabWithLoad(subTabKey);
      return;
    }

    const index = state.favoriteSubTabs.findIndex((tab) => tab.key === subTabKey);
    if (index !== -1) {
      state.favoriteSubTabIndex = index;
      updateFavoriteSubTabScrollLeft();
    }
  };

  const activatePublishSubTab = async (subTabKey, options = {}) => {
    const target = publishSubTabs.find((tab) => tab.key === subTabKey) || publishSubTabs[0];
    if (!target) {
      return;
    }

    const force = options.force === true;
    const nextIndex = publishSubTabs.findIndex((tab) => tab.key === target.key);
    if (nextIndex !== -1) {
      state.publishSubTabIndex = nextIndex;
    }
    state.publishSubTab = target.key;
    syncPublishStateToGlobal(state.publishSubTab);

    const tabState = getPublishTabState(state.publishSubTab);
    if (!tabState.loaded || force) {
      tabState.pageNo = 1;
      tabState.hasMore = true;
      tabState.loadStatus = 'loading';
      state.pageNo = 1;
      state.hasMore = true;
      state.loadStatus = 'loading';
      state.loading = true;
      await loadContent(true);
      return;
    }

    state.loading = false;
  };

  const handlePublishSubTabClick = async (subTabKey, index) => {
    if (state.publishSubTab === subTabKey && state.publishSubTabIndex === index) {
      return;
    }
    state.publishSubTabIndex = index;
    await activatePublishSubTab(subTabKey);
  };

  const onPublishContentSwiperChange = async (e) => {
    const index = e.detail.current;
    const target = publishSubTabs[index];
    if (!target) return;
    state.publishSubTabIndex = index;
    await activatePublishSubTab(target.key);
  };

  const handlePublishSwiperTouchStart = (event) => {
    const touch = extractTouchPoint(event.touches?.[0] || event.changedTouches?.[0]);
    publishSwipeState.startX = touch.x;
    publishSwipeState.startY = touch.y;
    publishSwipeState.tracking = true;
    publishSwipeState.startIndex = state.publishSubTabIndex ?? 0;
  };

  const handlePublishSwiperTouchMove = (event) => {
    if (!publishSwipeState.tracking) return;
    const touch = extractTouchPoint(event.touches?.[0] || event.changedTouches?.[0]);
    const deltaY = Math.abs(touch.y - publishSwipeState.startY);
    if (deltaY > PUBLISH_SWIPE_MAX_VERTICAL) {
      publishSwipeState.tracking = false;
    }
  };

  const handlePublishSwiperTouchEnd = async (event) => {
    if (!publishSwipeState.tracking) {
      publishSwipeState.tracking = false;
      publishSwipeState.startIndex = 0;
      return;
    }

    const touch = extractTouchPoint(event.changedTouches?.[0]);
    publishSwipeState.tracking = false;
    const startIndex = publishSwipeState.startIndex;
    publishSwipeState.startIndex = 0;

    const deltaX = (touch.x ?? 0) - publishSwipeState.startX;
    const deltaY = Math.abs((touch.y ?? 0) - publishSwipeState.startY);

    if (deltaY > PUBLISH_SWIPE_MAX_VERTICAL) {
      return;
    }

    if (deltaX > PUBLISH_SWIPE_MIN_DISTANCE && startIndex === 0 && state.publishSubTabIndex === 0) {
      const lastFavorite = state.favoriteSubTabs[state.favoriteSubTabs.length - 1];
      const targetKey = lastFavorite?.key;
      if (targetKey) {
        state.currentTab = 'favorite';
        await ensureFavoriteSubTabActive(targetKey);
        const targetIndex = state.favoriteSubTabs.findIndex((tab) => tab.key === targetKey);
        state.mainSwiperIndex = targetIndex === -1 ? 0 : targetIndex;
      } else {
        state.mainSwiperIndex = 0;
        state.currentTab = 'favorite';
      }
    }
  };

  const onMainContentSwiperChange = async (e) => {
    const nextIndex = e.detail.current;
    state.mainSwiperIndex = nextIndex;
    const favoriteCount = state.favoriteSubTabs.length;

    if (nextIndex < favoriteCount) {
      const target = state.favoriteSubTabs[nextIndex];
      if (!target) return;
      if (state.currentTab !== 'favorite') {
        state.currentTab = 'favorite';
      }
      await ensureFavoriteSubTabActive(target.key);
      return;
    }

    const publishIndex = favoriteCount;
    if (state.currentTab !== 'publish') {
      await switchTab('publish');
      return;
    }

    state.mainSwiperIndex = publishIndex;
    if (state.publishSubTabIndex === undefined || state.publishSubTabIndex < 0) {
      state.publishSubTabIndex = 0;
    }
    const target = publishSubTabs[state.publishSubTabIndex] || publishSubTabs[0];
    if (!target) return;
    await activatePublishSubTab(target.key);
  };

  // 加载内容
  const loadContent = async (reset = false) => {
    if (state.loading && !reset) return;

    const currentTab = state.currentTab;
    state.loading = true;

    try {
      if (currentTab === 'favorite') {
        if (reset) {
          state.pageNo = 1;
          state.hasMore = true;
        }
        state.loadStatus = 'loading';

        const favoriteConfig = {
          likedByMe: {
            listKey: 'likedByMeList',
            request: (pageNo, pageSize) =>
              actionApi.getMyActionPage('like', { pageNo, pageSize }),
          },
          likedMe: {
            listKey: 'likedMeList',
            request: (pageNo, pageSize) =>
              actionApi.getReceivedActionPage('like', { pageNo, pageSize }),
          },
          collectedByMe: {
            listKey: 'collectedByMeList',
            request: (pageNo, pageSize) =>
              actionApi.getMyActionPage('collect', { pageNo, pageSize }),
          },
          collectedMe: {
            listKey: 'collectedMeList',
            request: (pageNo, pageSize) =>
              actionApi.getReceivedActionPage('collect', { pageNo, pageSize }),
          },
        }[state.favoriteSubTab];

        if (!favoriteConfig) {
          state.loading = false;
          return;
        }

        const res = await favoriteConfig.request(state.pageNo, state.pageSize);
        if (res && res.code === 0) {
          const newList = res.data?.list || [];

          if (reset) {
            state[favoriteConfig.listKey] = newList;
          } else {
            state[favoriteConfig.listKey] = [
              ...(state[favoriteConfig.listKey] || []),
              ...newList,
            ];
          }

          const total = res.data?.total;
          const hasMore = typeof total === 'number'
            ? state.pageNo * state.pageSize < total
            : newList.length >= state.pageSize;
          state.hasMore = hasMore;
          state.loadStatus = hasMore ? 'more' : 'noMore';

          if (hasMore) {
            state.pageNo++;
          }

          if (state.favoriteSubTab) {
            state.favoriteSubTabLoaded[state.favoriteSubTab] = true;
          }
        } else {
          state.loadStatus = 'error';
          sheep.$helper.toast(res?.msg || '加载失败');
        }

        return;
      }

      if (currentTab === 'publish') {
        const subKey = state.publishSubTab || publishSubTabs[0]?.key || 'all';
        const tabState = getPublishTabState(subKey);
        const pageNo = reset ? 1 : tabState.pageNo;
        state.pageNo = pageNo;

        if (reset) {
          tabState.pageNo = 1;
          tabState.hasMore = true;
          tabState.list = [];
        }
        tabState.loadStatus = 'loading';
        state.loadStatus = 'loading';

        const params = {
          pageNo,
          pageSize: state.pageSize,
        };

        if (subKey === 'review') {
          params.auditStatus = 'pending';
        } else if (subKey === 'published') {
          params.auditStatus = 'approved';
          params.publishStatus = 1;
        }

        const res = await articleApi.getMyArticles(params);

        if (res && res.code === 0) {
          const newList = res.data?.list || [];

          if (reset) {
            tabState.list = newList;
          } else {
            tabState.list = [...tabState.list, ...newList];
          }

          const total = res.data?.total;
          const hasMore = typeof total === 'number'
            ? pageNo * state.pageSize < total
            : newList.length >= state.pageSize;

          tabState.hasMore = hasMore;
          tabState.loadStatus = hasMore ? 'more' : 'noMore';
          tabState.pageNo = hasMore ? pageNo + 1 : pageNo;
          tabState.loaded = true;

          syncPublishStateToGlobal(subKey);
          state.pageNo = tabState.pageNo;
        } else {
          tabState.loadStatus = 'error';
          tabState.loaded = false;
          state.loadStatus = 'error';
          sheep.$helper.toast(res?.msg || '加载失败');
        }

        return;
      }

      if (reset) {
        state.pageNo = 1;
        state.hasMore = true;
      }
      state.loadStatus = 'loading';

      let res;
      let listKey = '';

      if (currentTab === 'comment') {
        res = await commentApi.getMyComments({
          pageNo: state.pageNo,
          pageSize: state.pageSize,
        });
        listKey = 'commentList';
      } else if (currentTab === 'reply') {
        res = await commentApi.getRepliesToMe({
          pageNo: state.pageNo,
          pageSize: state.pageSize,
        });
        listKey = 'replyList';
      } else {
        state.loadStatus = 'noMore';
        return;
      }

      if (res && res.code === 0) {
        const newList = res.data?.list || [];

        if (reset) {
          state[listKey] = newList;
        } else {
          state[listKey] = [...(state[listKey] || []), ...newList];
        }

        const total = res.data?.total;
        const hasMore = typeof total === 'number'
          ? state.pageNo * state.pageSize < total
          : newList.length >= state.pageSize;

        state.hasMore = hasMore;
        state.loadStatus = hasMore ? 'more' : 'noMore';

        if (hasMore) {
          state.pageNo++;
        }
      } else {
        state.loadStatus = 'error';
        sheep.$helper.toast(res?.msg || '加载失败');
      }

    } catch (error) {
      console.error('加载内容失败:', error);
      state.loadStatus = 'error';
      sheep.$helper.toast('网络错误，请重试');
    } finally {
      state.loading = false;
    }
};

  // 加载统计数据
  const loadStats = async () => {
    try {
      const params = { pageNo: 1, pageSize: 1 };
      const [likedByMeRes, collectedByMeRes, likedMeRes, collectedMeRes, publishRes] = await Promise.all([
        actionApi.getMyActionPage('like', params),
        actionApi.getMyActionPage('collect', params),
        actionApi.getReceivedActionPage('like', params),
        actionApi.getReceivedActionPage('collect', params),
        articleApi.getMyArticles(params),
      ]);

      const totalOf = (res) => (res?.data?.total || 0);
      state.tabs[0].count =
        totalOf(likedByMeRes) + totalOf(collectedByMeRes) + totalOf(likedMeRes) + totalOf(collectedMeRes);
      state.tabs[1].count = totalOf(publishRes);
    } catch (error) {
      console.error('加载统计数据失败:', error);
    }
  };

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

    if (year === now.getFullYear()) {
      return `${month}-${day} ${hour}:${minute}`;
    }

    return `${year}-${month}-${day} ${hour}:${minute}`;
  };

  // 格式化操作时间
  const formatActionTime = (time) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now - date;

    // 小于1小时
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return `${minutes}分钟前`;
    }

    // 小于24小时
    if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000));
      return `${hours}小时前`;
    }

    // 小于30天
    if (diff < 30 * 24 * 60 * 60 * 1000) {
      const days = Math.floor(diff / (24 * 60 * 60 * 1000));
      return `${days}天前`;
    }

    // 超过30天显示完整日期
    return formatTime(time);
  };

  // 获取状态文本
  const getStatusText = (item) => {
    // 根据auditStatus和publishStatus组合判断
    const auditStatus = item.auditStatus;
    const publishStatus = item.publishStatus;

    if (auditStatus === 'pending') {
      return '审核中';
    } else if (auditStatus === 'rejected') {
      return '已拒绝';
    } else if (auditStatus === 'approved') {
      if (publishStatus === 1) {
        return '已发布';
      } else {
        return '已通过';
      }
    }

    return '未知';
  };

  // 取消点赞
  const handleUnlike = async (articleId) => {
    try {
      const res = await actionApi.unlike(articleId, 'like');
      if (res.code === 0) {
        sheep.$helper.toast('已取消点赞');
        await loadContent(true);
        await loadStats();
      } else {
        sheep.$helper.toast(res.msg || '操作失败');
      }
    } catch (error) {
      console.error('取消点赞失败:', error);
      sheep.$helper.toast('操作失败，请重试');
    }
  };

  // 取消收藏
  const handleUncollect = async (articleId) => {
    try {
      const res = await actionApi.uncollect(articleId, 'collect');
      if (res.code === 0) {
        sheep.$helper.toast('已取消收藏');
        await loadContent(true);
        await loadStats();
      } else {
        sheep.$helper.toast(res.msg || '操作失败');
      }
    } catch (error) {
      console.error('取消收藏失败:', error);
      sheep.$helper.toast('操作失败，请重试');
    }
  };
  //
  // // 编辑文章
  // const handleEdit = (articleId) => {
  //   uni.navigateTo({
  //     url: `/pages/cms/publish?id=${articleId}`
  //   });
  // };

  // 删除评论
  const handleDeleteComment = async (commentId) => {
    try {
      const confirmed = await new Promise((resolve) => {
        uni.showModal({
          title: '提示',
          content: '确定要删除这条评论吗?',
          success: (res) => {
            resolve(res.confirm);
          },
        });
      });

      if (!confirmed) return;

      const res = await commentApi.deleteComment(commentId);
      if (res.code === 0) {
        sheep.$helper.toast('删除成功');
        await loadContent(true);
        await loadStats();
      } else {
        sheep.$helper.toast(res.msg || '删除失败');
      }
    } catch (error) {
      console.error('删除评论失败:', error);
      sheep.$helper.toast('操作失败，请重试');
    }
  };

  // 回复评论
  const handleReply = async (replyItem) => {
    // 标记为已读
    if (!replyItem.readStatus) {
      try {
        await commentApi.markAsRead([replyItem.id]);
        // 更新本地状态
        const index = state.replyList.findIndex(item => item.id === replyItem.id);
        if (index !== -1) {
          state.replyList[index].readStatus = true;
        }
        // 刷新统计数据
        await loadStats();
      } catch (error) {
        console.error('标记已读失败:', error);
      }
    }

    // 跳转到文章详情页进行回复
    uni.navigateTo({
      url: `/pages/cms/detail?id=${replyItem.articleId}&commentId=${replyItem.id}`
    });
  };

  // 跳转到文章详情
  const goToDetail = (articleId) => {
    uni.navigateTo({
      url: `/pages/cms/detail?id=${articleId}`
    });
  };

  // 去浏览
  const goToBrowse = () => {
    uni.switchTab({
      url: '/yehu/pages/community/index'
    });
  };

  // 去发布
  const goToPublish = () => {
    uni.navigateTo({
      url: '/pages/cms/publish'
    });
  };

  const formatAvatar = (avatar) => sheep.$url.cdn(avatar || 'https://cdn.example.com/prod/2025/11/05/9b940f25460a1321f74b9b1d12e2dbaeb690739c0568c22570a3ef4f4603e333.jpg');

  const getCoverImage = (images) => {
    if (!images) return '';
    const list = Array.isArray(images) ? images : [images];
    const first = list.find((img) => !!img);
    return first ? sheep.$url.cdn(first) : '';
  };

  const extractArticleTitle = (item) => item.articleTitle || item.title || '未命名内容';
  const extractArticleSubtitle = (item) => item.articleSubtitle || item.subtitle || '';
  const extractArticleCoverImages = (item) => item.articleCoverImages ?? item.coverImages ?? [];
  const extractArticleAuthorAvatar = (item) => item.articleAuthorAvatar || item.authorAvatar || '';

  const buildFavoriteMeta = (item, subTab) => {
    const articleTitle = extractArticleTitle(item);
    const cover = getCoverImage(extractArticleCoverImages(item));
    const actorName = item.actionUserNickname || item.nickname || 'TA';
    const timeText = formatActionTime(item.createTime);
    const publishTime = item.articlePublishTime || item.publishTime;
    const publishTimeText = publishTime ? formatTime(publishTime) : '';
    const titleTag = articleTitle ? `「${articleTitle}」` : '你的内容';

    if (subTab === 'likedByMe') {
      return {
        layoutClass: 'interaction-card-outgoing',
        cover,
        authorAvatar: formatAvatar(extractArticleAuthorAvatar(item)),
        authorName: item.articleAuthorName || '匿名作者',
        articleTitle,
        publishTimeText,
        gallery: extractArticleCoverImages(item)
          .map((img) => (img ? sheep.$url.cdn(img) : ''))
          .filter(Boolean)
          .slice(0, 3),
        actionIcon: 'cicon-thumb-up-line',
        actionColor: 'var(--ui-BG-Main)',
        onAction: () => handleUnlike(item.articleId),
      };
    }

    if (subTab === 'likedMe') {
      return {
        layoutClass: 'interaction-card-incoming',
        actorAvatar: formatAvatar(item.actionUserAvatar),
        actorName,
        description: `点赞了你的${titleTag}`,
        timeText,
        articleTitle,
        publishTimeText,
        cover,
      };
    }

    if (subTab === 'collectedByMe') {
      return {
        layoutClass: 'interaction-card-outgoing',
        cover,
        authorAvatar: formatAvatar(extractArticleAuthorAvatar(item)),
        authorName: item.articleAuthorName || '匿名作者',
        articleTitle,
        publishTimeText,
        gallery: extractArticleCoverImages(item)
          .map((img) => (img ? sheep.$url.cdn(img) : ''))
          .filter(Boolean)
          .slice(0, 3),
        actionIcon: 'cicon-favorite',
        actionColor: 'var(--ui-BG-Main)',
        onAction: () => handleUncollect(item.articleId),
      };
    }

    return {
      layoutClass: 'interaction-card-incoming',
      actorAvatar: formatAvatar(item.actionUserAvatar),
      actorName,
      description: `收藏了你的${titleTag}`,
      timeText,
      articleTitle,
      publishTimeText,
      cover,
    };
  };

  // 根据subTab key获取显示列表
  const getFavoriteDisplayList = (subTabKey) => {
    const listMap = {
      likedByMe: state.likedByMeList,
      likedMe: state.likedMeList,
      collectedByMe: state.collectedByMeList,
      collectedMe: state.collectedMeList,
    };
    const sourceList = listMap[subTabKey] || [];
    return sourceList.map((item) => ({
      item,
      meta: buildFavoriteMeta(item, subTabKey),
    }));
  };

  const favoriteEmptyMeta = computed(() => {
    switch (state.favoriteSubTab) {
      case 'likedByMe':
        return {
          icon: 'cicon-thumb-up-line',
          title: '还没有点赞任何内容',
          desc: '为你喜欢的内容点赞，记录每一个心动瞬间',
          actionText: '去浏览',
          action: goToBrowse,
        };
      case 'likedMe':
        return {
          icon: 'cicon-thumb-up-line',
          title: '还没有人点赞你的内容',
          desc: '发布更多优质内容，吸引伙伴为你点赞',
          actionText: '去发布',
          action: goToPublish,
        };
      case 'collectedByMe':
        return {
          icon: 'cicon-favorite',
          title: '还没有收藏任何内容',
          desc: '收藏喜欢的内容，随时回顾灵感与重点',
          actionText: '去浏览',
          action: goToBrowse,
        };
      case 'collectedMe':
        return {
          icon: 'cicon-favorite',
          title: '还没有人收藏你的内容',
          desc: '分享更多精彩内容，让大家为你收藏',
          actionText: '去发布',
          action: goToPublish,
        };
      default:
        return {
          icon: 'cicon-heart',
          title: '暂时没有数据',
          desc: '快去发现或发布更多精彩内容吧',
          actionText: '去浏览',
          action: goToBrowse,
        };
    }
  });

  // 根据subTab key获取空状态信息
  const getFavoriteEmptyMeta = (subTabKey) => {
    switch (subTabKey) {
      case 'likedByMe':
        return {
          icon: 'cicon-thumb-up-line',
          title: '还没有点赞任何内容',
          desc: '为你喜欢的内容点赞，记录每一个心动瞬间',
          actionText: '去浏览',
          action: goToBrowse,
        };
      case 'likedMe':
        return {
          icon: 'cicon-thumb-up-line',
          title: '还没有人点赞你的内容',
          desc: '发布更多优质内容，吸引伙伴为你点赞',
          actionText: '去发布',
          action: goToPublish,
        };
      case 'collectedByMe':
        return {
          icon: 'cicon-favorite',
          title: '还没有收藏任何内容',
          desc: '收藏喜欢的内容，随时回顾灵感与重点',
          actionText: '去浏览',
          action: goToBrowse,
        };
      case 'collectedMe':
        return {
          icon: 'cicon-favorite',
          title: '还没有人收藏你的内容',
          desc: '分享更多精彩内容，让大家为你收藏',
          actionText: '去发布',
          action: goToPublish,
        };
      default:
        return {
          icon: 'cicon-heart',
          title: '暂时没有数据',
          desc: '快去发现或发布更多精彩内容吧',
          actionText: '去浏览',
          action: goToBrowse,
        };
    }
  };

  const getPublishEmptyMeta = (subTabKey) => {
    switch (subTabKey) {
      case 'review':
        return {
          icon: 'cicon-time',
          title: '没有待审核的内容',
          desc: '发布的内容正在审核中，请耐心等待',
          actionText: '去发布',
          action: goToPublish,
        };
      case 'published':
        return {
          icon: 'cicon-check-circle',
          title: '没有已发布的内容',
          desc: '审核通过的内容将在这里显示',
          actionText: '去发布',
          action: goToPublish,
        };
      default:
        return {
          icon: 'cicon-edit',
          title: '还没有发布任何内容',
          desc: '发布你的第一篇文章，分享精彩内容',
          actionText: '去发布',
          action: goToPublish,
        };
    }
  };

  // 判断是否有内容
  const hasContent = () => {
    if (state.currentTab === 'favorite') {
      return getFavoriteDisplayList(state.favoriteSubTab).length > 0;
    }
    if (state.currentTab === 'publish') {
      return getPublishList(state.publishSubTab).length > 0;
    }
    if (state.currentTab === 'comment') return state.commentList.length > 0;
    if (state.currentTab === 'reply') return state.replyList.length > 0;
    return false;
  };

  // 触底加载更多
  onReachBottom(() => {
    if (state.hasMore && !state.loading && state.loadStatus !== 'loading') {
      loadContent(false);
    }
  });

  // 下拉刷新
  onPullDownRefresh(async () => {
    try {
      await loadStats();
      // 如果是点赞收藏tab，刷新当前子tab的数据
      if (state.currentTab === 'favorite') {
        const listKey = favoriteListKeyMap[state.favoriteSubTab];
        if (listKey) {
          // 清空当前tab的数据，重新加载
          state[listKey] = [];
          state.favoriteSubTabLoaded[state.favoriteSubTab] = false;
          state.pageNo = 1;
          state.hasMore = true;
          state.loadStatus = 'loading';
        }
      } else if (state.currentTab === 'publish') {
        const subKey = state.publishSubTab || publishSubTabs[0]?.key || 'all';
        const tabState = getPublishTabState(subKey);
        tabState.list = [];
        tabState.pageNo = 1;
        tabState.hasMore = true;
        tabState.loadStatus = 'loading';
        tabState.loaded = false;
        state.pageNo = 1;
        state.hasMore = true;
        state.loadStatus = 'loading';
      }
      await loadContent(true);
    } catch (error) {
      console.error('刷新失败:', error);
    } finally {
      uni.stopPullDownRefresh();
    }
  });

  onLoad(async (options) => {
    // 如果有指定tab，切换到对应tab
    if (options.tab && ['favorite', 'publish'].includes(options.tab)) {
      state.currentTab = options.tab;
    }

    await loadStats();

    // 初始化加载数据
    if (state.currentTab === 'favorite') {
      // 检查当前tab是否有数据，如果没有则加载
      const currentListKey = favoriteListKeyMap[state.favoriteSubTab];
      if (currentListKey && state[currentListKey].length === 0 && !state.favoriteSubTabLoaded[state.favoriteSubTab]) {
        await loadContent(true);
      }
      // 初始化时更新滚动位置
      uni.nextTick(() => {
        updateFavoriteSubTabScrollLeft();
      });
    } else if (state.currentTab === 'publish') {
      await activatePublishSubTab(state.publishSubTab || 'all', { force: true });
    } else {
      await loadContent(true);
    }
  });
</script>

<style lang="scss" scoped>
  .interaction-page {
    min-height: 100vh;
    background-color: var(--ui-BG-1);
    padding-bottom: 40rpx;
  }

  /* Tab头部 */
  .tab-header {
    border-bottom: 1rpx solid var(--ui-Line);

    .tab-list {
      display: flex;
      padding: 0 20rpx;

      .tab-item {
        flex: 1;
        position: relative;
        padding: 30rpx 0;
        display: flex;
        align-items: center;
        justify-content: center;

        .tab-text {
          font-size: 30rpx;
          color: var(--ui-TC-2);
          font-weight: 500;
        }

        &.active {
          .tab-text {
            color: var(--ui-BG-Main);
            font-weight: 600;
          }

          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 60rpx;
            height: 4rpx;
            background-color: var(--ui-BG-Main);
            border-radius: 2rpx;
          }
        }
      }
    }

    /* 二级筛选 */
    .sub-tab-filter {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20rpx 20rpx 24rpx;

      .filter-item {
        padding: 10rpx 24rpx;
        margin: 0 8rpx;
        font-size: 26rpx;
        color: var(--ui-TC-2);
        background-color: var(--ui-BG-1);
        border-radius: 20rpx;
        transition: all 0.3s;
        white-space: nowrap;

        &.active {
          color: var(--ui-BG-Main);
          background: linear-gradient(135deg, var(--ui-BG-Main-opacity-1) 0%, var(--ui-BG-Main-opacity-1) 100%);
          font-weight: 500;
        }
      }
    }

    /* 点赞收藏二级筛选 - 支持滑动 */
    .sub-tab-filter-scroll {
      padding: 20rpx 0 24rpx;
      height: 70rpx;

      .sub-tab-filter-scroll-view {
        width: 100%;
        height: 100%;
        white-space: nowrap;
      }

      .sub-tab-filter-wrapper {
        display: inline-flex;
        align-items: center;
        padding: 0 20rpx;
        height: 100%;
        white-space: nowrap;
      }

      .filter-item {
        display: inline-block;
        padding: 10rpx 24rpx;
        margin: 0 8rpx;
        font-size: 26rpx;
        color: var(--ui-TC-2);
        background-color: var(--ui-BG-1);
        border-radius: 20rpx;
        transition: all 0.3s;
        white-space: nowrap;
        flex-shrink: 0;

        &.active {
          color: var(--ui-BG-Main);
          background: linear-gradient(135deg, var(--ui-BG-Main-opacity-1) 0%, var(--ui-BG-Main-opacity-1) 100%);
          font-weight: 500;
        }
      }
    }
  }

  /* 内容容器 */
  .content-container {
    .tab-content {
      .loading-container {
        padding: 40rpx;
      }
    }

    .main-content-swiper {
      width: 100%;
      height: calc(100vh - 200rpx);
      min-height: 600px;
    }

    .main-swiper-item {
      height: 100%;
    }

    .favorite-tab-panel,
    .publish-tab-panel {
      height: 100%;
    }

    .favorite-content-wrapper,
    .publish-content-wrapper {
      width: 100%;
      height: 100%;
      //overflow-y: auto;
    }

    .publish-content-swiper,
    .publish-swiper-item {
      height: 100%;
    }
  }

  /* 空状态 */
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 60rpx;
    text-align: center;

    .empty-icon {
      margin-bottom: 40rpx;
    }

    .empty-text {
      font-size: 32rpx;
      color: var(--ui-TC);
      font-weight: 500;
      margin-bottom: 16rpx;
    }

    .empty-desc {
      font-size: 26rpx;
      color: var(--ui-TC-3);
      line-height: 1.5;
      margin-bottom: 60rpx;
    }

    .browse-btn,
    .publish-btn {
      background: linear-gradient(135deg, var(--ui-BG-Main) 0%, var(--ui-BG-Main-gradient) 100%);
      color: white;
      border: none;
      border-radius: 50rpx;
      padding: 20rpx 60rpx;
      font-size: 28rpx;
      font-weight: 500;
    }
  }

  /* 内容列表 */
  .content-list {
    padding: 20rpx;
  }

  .content-item {
    margin-bottom: 20rpx;
  }

  .interaction-card {
    background-color: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  }

  .interaction-avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    object-fit: cover;
  }

  .interaction-title {
    font-size: 26rpx;
    color: var(--ui-TC);
    font-weight: 600;
  }

  .interaction-desc {
    font-size: 24rpx;
    color: var(--ui-TC-2);
    line-height: 1.5;
  }

  .interaction-time {
    font-size: 22rpx;
    color: var(--ui-TC-3);
  }

  .interaction-card-incoming {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20rpx;
  }

  .incoming-body {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20rpx;
    width: 100%;
  }

  .incoming-info {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .incoming-text {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }

  .incoming-cover {
    width: 120rpx;
    height: 120rpx;
    border-radius: 12rpx;
    object-fit: cover;
    flex-shrink: 0;
  }

  .outgoing-body {
    display: flex;
    align-items: stretch;
    gap: 20rpx;
    width: 100%;
  }

  .outgoing-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    min-width: 0;
  }

  .outgoing-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .outgoing-author-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .outgoing-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 8rpx;
    min-width: 0;
  }

  .outgoing-info .article-title {
    font-size: 28rpx;
    color: var(--ui-TC);
    font-weight: 600;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .outgoing-info .article-time {
    font-size: 24rpx;
    color: var(--ui-TC-3);
  }

  .outgoing-gallery {
    display: flex;
    gap: 12rpx;
    width: 100%;
    padding: 0rpx 90rpx;
  }

  .outgoing-gallery-item {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    object-fit: cover;
    flex-shrink: 0;

  }

  .outgoing-actions {
    width: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .outgoing-action-icon {
    font-size: 32rpx;
  }

  .article-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

    .card-header {
      .author-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 16rpx;
        flex-shrink: 0;
      }

      .author-info {
        .author-name-line {
          margin-bottom: 6rpx;

          .author-name {
            font-size: 26rpx;
            font-weight: 600;
            color: var(--ui-TC);
          }

          .official-badge {
            background: linear-gradient(135deg, var(--ui-BG-Main) 0%, var(--ui-BG-Main-gradient) 100%);
            color: white;
            padding: 4rpx 10rpx;
            border-radius: 6rpx;
            font-size: 18rpx;
            font-weight: 500;
          }
        }

        .publish-time {
          font-size: 22rpx;
          color: var(--ui-TC-3);
        }
      }

      .action-time {
        font-size: 22rpx;
        color: var(--ui-TC-2);
        white-space: nowrap;
      }

      .status-badge {
        font-size: 22rpx;
        color: var(--ui-TC-3);
        background-color: var(--ui-BG-1);
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
        white-space: nowrap;

        &.published {
          color: var(--ui-success-color, #07c160);
          background-color: var(--ui-success-bg, rgba(7, 193, 96, 0.12));
        }

        &.review {
          color: var(--ui-BG-Main);
          background-color: var(--ui-BG-Main-opacity-1);
        }
      }
    }

    .card-content {
      margin: 20rpx 0;

      .article-title {
        display: block;
        font-size: 30rpx;
        font-weight: 600;
        color: var(--ui-TC);
        line-height: 1.4;
        margin-bottom: 12rpx;
      }

      .article-subtitle {
        display: block;
        font-size: 26rpx;
        color: var(--ui-TC-2);
        line-height: 1.6;
        margin-bottom: 16rpx;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .cover-section {
        .cover-image {
          width: 100%;
          //height: 200rpx;
          border-radius: 12rpx;
          object-fit: cover;
        }
      }
    }

    .card-footer {
      border-top: 1rpx solid var(--ui-Line);
      padding-top: 16rpx;
      margin-top: 16rpx;

      .article-stats {
        .stat-item {
          .stat-text {
            font-size: 22rpx;
            color: var(--ui-TC-3);
            margin-left: 6rpx;
          }
        }
      }

      .actions {
        .action-btn {
          .action-text {
            font-size: 22rpx;
            color: var(--ui-TC-3);
            margin-left: 6rpx;
          }
        }
      }
    }
  }

  /* 评论卡片 */
  .comment-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

    .comment-content {
      .comment-text {
        font-size: 28rpx;
        color: var(--ui-TC);
        line-height: 1.6;
        display: block;
        margin-bottom: 12rpx;
      }

      .comment-images {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        margin-top: 12rpx;

        .comment-image {
          width: 200rpx;
          height: 200rpx;
          border-radius: 8rpx;
          object-fit: cover;
        }
      }
    }

    .comment-time {
      font-size: 22rpx;
      color: var(--ui-TC-3);
      margin-top: 12rpx;
    }

    .related-article {
      background-color: var(--ui-BG-1);
      border-radius: 12rpx;
      padding: 16rpx;

      .related-label {
        font-size: 22rpx;
        color: var(--ui-TC-3);
        margin-bottom: 12rpx;
      }

      .article-preview {
        display: flex;
        align-items: flex-start;

        .article-preview-content {
          .article-preview-title {
            display: block;
            font-size: 26rpx;
            font-weight: 600;
            color: var(--ui-TC);
            line-height: 1.4;
            margin-bottom: 8rpx;
            display: -webkit-box;
            -webkit-line-clamp: 1;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .article-preview-subtitle {
            display: block;
            font-size: 24rpx;
            color: var(--ui-TC-2);
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .article-preview-image {
          width: 120rpx;
          height: 120rpx;
          border-radius: 8rpx;
          margin-left: 16rpx;
          flex-shrink: 0;
          object-fit: cover;
        }
      }
    }

    .comment-footer {
      border-top: 1rpx solid var(--ui-Line);
      padding-top: 16rpx;
      margin-top: 16rpx;

      .comment-stats {
        .stat-item {
          .stat-text {
            font-size: 22rpx;
            color: var(--ui-TC-3);
            margin-left: 6rpx;
          }
        }
      }

      .actions {
        .action-btn {
          .action-text {
            font-size: 22rpx;
            color: var(--ui-TC-3);
            margin-left: 6rpx;
          }
        }
      }
    }
  }

  /* 回复卡片 */
  .reply-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    position: relative;

    &.unread {
      border-left: 4rpx solid var(--ui-BG-Main);
    }

    .unread-badge {
      position: absolute;
      top: 24rpx;
      right: 24rpx;
      background: linear-gradient(135deg, var(--ui-BG-Main) 0%, var(--ui-BG-Main-gradient) 100%);
      color: white;
      padding: 4rpx 12rpx;
      border-radius: 12rpx;
      font-size: 20rpx;
      font-weight: 500;
    }

    .reply-header {
      .replier-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 16rpx;
        flex-shrink: 0;
      }

      .replier-info {
        .replier-name-line {
          margin-bottom: 6rpx;

          .replier-name {
            font-size: 26rpx;
            font-weight: 600;
            color: var(--ui-TC);
          }

          .official-badge {
            background: linear-gradient(135deg, var(--ui-BG-Main) 0%, var(--ui-BG-Main-gradient) 100%);
            color: white;
            padding: 4rpx 10rpx;
            border-radius: 6rpx;
            font-size: 18rpx;
            font-weight: 500;
          }
        }

        .reply-time {
          font-size: 22rpx;
          color: var(--ui-TC-3);
        }
      }
    }

    .reply-content {
      .reply-text {
        font-size: 28rpx;
        color: var(--ui-TC);
        line-height: 1.6;
        display: block;
        margin-bottom: 12rpx;
      }

      .reply-images {
        display: flex;
        flex-wrap: wrap;
        gap: 12rpx;
        margin-top: 12rpx;

        .reply-image {
          width: 200rpx;
          height: 200rpx;
          border-radius: 8rpx;
          object-fit: cover;
        }
      }
    }

    .original-comment {
      background-color: var(--ui-BG-1);
      border-radius: 12rpx;
      padding: 16rpx;
      border-left: 3rpx solid var(--ui-BG-Main);

      .original-label {
        font-size: 22rpx;
        color: var(--ui-TC-3);
        margin-bottom: 12rpx;
      }

      .original-content {
        .original-text {
          font-size: 26rpx;
          color: var(--ui-TC-2);
          line-height: 1.5;
          display: block;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .related-article {
      background-color: var(--ui-BG-1);
      border-radius: 12rpx;
      padding: 16rpx;

      .related-label {
        font-size: 22rpx;
        color: var(--ui-TC-3);
        margin-bottom: 12rpx;
      }

      .article-preview {
        display: flex;
        align-items: flex-start;

        .article-preview-content {
          .article-preview-title {
            display: block;
            font-size: 26rpx;
            font-weight: 600;
            color: var(--ui-TC);
            line-height: 1.4;
            margin-bottom: 8rpx;
            display: -webkit-box;
            -webkit-line-clamp: 1;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .article-preview-subtitle {
            display: block;
            font-size: 24rpx;
            color: var(--ui-TC-2);
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .article-preview-image {
          width: 120rpx;
          height: 120rpx;
          border-radius: 8rpx;
          margin-left: 16rpx;
          flex-shrink: 0;
          object-fit: cover;
        }
      }
    }

    .reply-footer {
      margin-top: 20rpx;

      .action-btn {
        padding: 8rpx 20rpx;
        border-radius: 20rpx;
        background-color: var(--ui-BG-Main-opacity-1);
        color: var(--ui-BG-Main);

        .action-text {
          font-size: 24rpx;
          font-weight: 500;
          margin-left: 6rpx;
        }
      }
    }
  }

  /* 加载更多 */
  .load-more {
    padding: 20rpx;
  }

  /* 加载动画 */
  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 100rpx 0;
  }

  .loading-spinner {
    width: 40rpx;
    height: 40rpx;
    border: 3rpx solid var(--ui-BG-2);
    border-top: 3rpx solid var(--ui-BG-Main);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
</style>
