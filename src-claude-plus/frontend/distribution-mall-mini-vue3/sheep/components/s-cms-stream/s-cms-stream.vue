<template>
  <!-- 主内容容器 -->
  <view class="cms-stream-container" :style="containerStyle">
    <!-- 分类和标签筛选 - 完全参考DIY吸顶导航，放在流内部但使用特殊样式撑满宽度 -->
    <view v-if="showCategoryFilter && categories.length > 0" class="filter-nav-wrapper">
      <view class="filter-nav-fixed" :class="{ 'is-sticky': isSticky }" :style="categoryNavStyle">
        <!-- 分类筛选 -->
        <view class="category-nav-container">
          <!-- 固定的"全部"分类 -->
          <view
            class="category-item category-all-fixed"
            :class="{ active: selectedCategory === null }"
            @tap="selectCategory(null)"
          >
            <text class="nav-text">全部</text>
            <view v-if="selectedCategory === null" class="nav-underline"></view>
          </view>

          <!-- 可滚动的分类列表 -->
          <scroll-view
            scroll-x
            class="category-nav"
            enhanced
            :show-scrollbar="false"
            :scroll-left="categoryScrollLeft"
            scroll-with-animation
          >
            <view class="category-list">
              <view
                v-for="(category, index) in displayCategories"
                :key="category.id"
                class="category-item"
                :class="{
                  active: selectedCategory === category.id,
                  'auto-width': categories.length <= 5,
                }"
                :data-index="index"
                @tap="selectCategoryByIndex(category.id, index)"
              >
                <text class="nav-text">{{ category.name }}</text>
                <view v-if="selectedCategory === category.id" class="nav-underline"></view>
              </view>
            </view>
          </scroll-view>

          <!-- 固定的搜索图标按钮 -->
          <view class="search-icon-btn" @tap="expandSearch">
            <view class="_icon-search"></view>
            <!-- 有搜索内容时显示小红点 -->
            <view v-if="searchKeyword" class="search-badge"></view>
          </view>
        </view>

        <!-- 标签筛选 - 分类下方显示，选择分类后显示 -->
        <view v-if="showTagFilter && selectedCategory && tags.length > 0" class="tag-nav-container">
          <!-- 固定的"全部"标签 -->
          <view
            class="tag-item tag-all-fixed"
            :class="{ active: selectedTag === null }"
            @tap="selectTag(null)"
          >
            <text>全部</text>
          </view>

          <!-- 可滚动的标签列表 -->
          <scroll-view
            scroll-x
            class="tag-nav"
            enhanced
            :show-scrollbar="false"
            :scroll-left="tagScrollLeft"
            scroll-with-animation
            @scroll="onTagScroll"
          >
            <view class="tag-list">
              <view
                v-for="(tag, index) in displayTags"
                :key="`tag-${tag.id}-${index}`"
                class="tag-item"
                :class="{ active: selectedTag === tag.id }"
                :data-index="index"
                @tap="selectTagByIndex(tag.id, index)"
              >
                <text>{{ tag.name }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 占位元素，当导航栏吸顶时保持布局 -->
    <view
      v-if="isSticky && showCategoryFilter && categories.length > 0"
      class="navbar-placeholder"
      :style="{ height: navbarHeight + 'px' }"
    ></view>
    <!-- 文章列表 - 支持分类/标签滑动切换 -->
    <swiper
      class="article-swiper"
      :current="categorySwiperCurrent"
      :duration="categorySwiperDuration"
      :touchable="categoryTabs.length > 1"
      :circular="false"
      :style="articleSwiperStyle"
      @change="handleCategorySwiperChange"
      @animationfinish="handleCategorySwiperAnimationEnd"
    >
      <swiper-item
        v-for="(categoryTab, categoryIndex) in categoryTabs"
        :key="categoryTab.id === null ? 'category-all' : `category-${categoryTab.id}`"
        class="category-swiper-item"
      >
        <view class="category-swiper-item-inner">
          <!-- 当前分类：支持标签内滑动 -->
          <template v-if="categoryIndex === activeCategoryIndex">
            <swiper
              v-if="activeCategoryTagTabs.length > 1"
              class="tag-swiper"
              :current="tagSwiperCurrent"
              :duration="tagSwiperDuration"
              :touchable="true"
              :circular="false"
              @change="(e) => handleTagSwiperChange(e, categoryTab.id)"
              @animationfinish="(e) => handleTagSwiperAnimationEnd(e, categoryTab.id)"
              @touchstart="(e) => handleTagSwiperTouchStart(e, categoryTab.id)"
              @touchmove="handleTagSwiperTouchMove"
              @touchend="handleTagSwiperTouchEnd"
              @touchcancel="handleTagSwiperTouchEnd"
            >
              <swiper-item
                v-for="(tagTab, tagIndex) in activeCategoryTagTabs"
                :key="tagTab.id === null ? 'tag-all' : `tag-${tagTab.id}`"
                class="tag-swiper-item"
              >
                <view
                  :class="[
                    'article-list',
                    tagIndex === activeTagIndex ? 'article-list--active' : 'article-list--cached',
                  ]"
                >
                  <template v-if="shouldShowSkeleton(categoryTab.id, tagTab.id)">
                    <view class="skeleton-list">
                      <s-cms-card-skeleton
                        v-for="placeholderIndex in skeletonPlaceholders"
                        :key="`skeleton-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${placeholderIndex}`"
                        :type="sectionType"
                      />
                    </view>
                  </template>
                  <template v-else>
                    <!-- 文章类卡片 -->
                    <s-cms-card-article
                      v-if="sectionType === 'article'"
                      v-for="article in getArticlesFor(categoryTab.id, tagTab.id)"
                      :key="`article-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${article.id}`"
                      :article="article"
                      :config="sectionConfig"
                      @click="goDetail"
                      @like="handleLike"
                      @collect="handleCollect"
                      @share="handleShare"
                    />

                    <!-- 动态类卡片 -->
                    <s-cms-card-dynamic
                      v-else-if="sectionType === 'dynamic'"
                      v-for="article in getArticlesFor(categoryTab.id, tagTab.id)"
                      :key="`dynamic-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${article.id}`"
                      :article="article"
                      :config="sectionConfig"
                      @click="goDetail"
                      @like="handleLike"
                      @collect="handleCollect"
                      @share="handleShare"
                    />

                    <!-- 课程类卡片 -->
                    <s-cms-card-course
                      v-else-if="sectionType === 'course'"
                      v-for="article in getArticlesFor(categoryTab.id, tagTab.id)"
                      :key="`course-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${article.id}`"
                      :article="article"
                      :config="sectionConfig"
                      @click="goDetail"
                      @like="handleLike"
                      @collect="handleCollect"
                      @enroll="handleEnroll"
                      @share="handleShare"
                    />

                    <!-- 自定义类型 - 使用默认简单布局 -->
                    <view
                      v-else
                      v-for="article in getArticlesFor(categoryTab.id, tagTab.id)"
                      :key="`custom-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${article.id}`"
                      class="article-card-simple"
                      @tap="goDetail(article.id)"
                    >
                      <view class="article-title ss-line-2">{{ article.title }}</view>
                      <view v-if="article.subtitle" class="article-subtitle ss-line-1">{{
                        article.subtitle
                      }}</view>
                    </view>

                    <!-- 空状态 -->
                    <view
                      v-if="
                        !loading && getArticlesFor(categoryTab.id, tagTab.id).length === 0 && !loadingMore
                      "
                      class="empty-box"
                    >
                      <text class="empty-text">暂无内容</text>
                    </view>

                    <!-- 初次加载状态 -->
                    <view v-if="loading" class="loading-box">
                      <text class="loading-text">加载中...</text>
                    </view>

                    <!-- 加载更多状态 -->
                    <view v-if="loadingMore" class="loading-more-box">
                      <view class="skeleton-list skeleton-list--loading-more">
                        <s-cms-card-skeleton
                          v-for="placeholderIndex in loadMoreSkeletonPlaceholders"
                          :key="`loading-more-${categoryTab.id ?? 'all'}-${tagTab.id ?? 'all'}-${placeholderIndex}`"
                          :type="sectionType"
                        />
                      </view>
                      <text class="loading-more-text">正在加载...</text>
                    </view>

                    <!-- 没有更多数据 -->
                    <view
                      v-if="
                        !loading && !loadingMore && getArticlesFor(categoryTab.id, tagTab.id).length > 0 && !hasMore
                      "
                      class="no-more-box"
                    >
                      <text class="no-more-text">没有更多了</text>
                    </view>
                  </template>
                </view>
              </swiper-item>
            </swiper>

            <view v-else class="article-list article-list--active">
              <template v-if="shouldShowSkeleton(categoryTab.id, selectedTag)">
                <view class="skeleton-list">
                  <s-cms-card-skeleton
                    v-for="placeholderIndex in skeletonPlaceholders"
                    :key="`skeleton-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${placeholderIndex}`"
                    :type="sectionType"
                  />
                </view>
              </template>
              <template v-else>
                <!-- 文章类卡片 -->
                <s-cms-card-article
                  v-if="sectionType === 'article'"
                  v-for="article in getArticlesFor(categoryTab.id, selectedTag)"
                  :key="`article-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @share="handleShare"
                />

                <!-- 动态类卡片 -->
                <s-cms-card-dynamic
                  v-else-if="sectionType === 'dynamic'"
                  v-for="article in getArticlesFor(categoryTab.id, selectedTag)"
                  :key="`dynamic-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @share="handleShare"
                />

                <!-- 课程类卡片 -->
                <s-cms-card-course
                  v-else-if="sectionType === 'course'"
                  v-for="article in getArticlesFor(categoryTab.id, selectedTag)"
                  :key="`course-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @enroll="handleEnroll"
                  @share="handleShare"
                />

                <!-- 自定义类型 - 使用默认简单布局 -->
                <view
                  v-else
                  v-for="article in getArticlesFor(categoryTab.id, selectedTag)"
                  :key="`custom-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${article.id}`"
                  class="article-card-simple"
                  @tap="goDetail(article.id)"
                >
                  <view class="article-title ss-line-2">{{ article.title }}</view>
                  <view v-if="article.subtitle" class="article-subtitle ss-line-1">{{
                    article.subtitle
                  }}</view>
                </view>

                <!-- 空状态 -->
                <view
                  v-if="!loading && getArticlesFor(categoryTab.id, selectedTag).length === 0"
                  class="empty-box"
                >
                  <text class="empty-text">暂无内容</text>
                </view>

                <!-- 初次加载状态 -->
                <view v-if="loading" class="loading-box">
                  <text class="loading-text">加载中...</text>
                </view>

                <!-- 加载更多状态 -->
                <view v-if="loadingMore" class="loading-more-box">
                  <view class="skeleton-list skeleton-list--loading-more">
                    <s-cms-card-skeleton
                      v-for="placeholderIndex in loadMoreSkeletonPlaceholders"
                      :key="`loading-more-${categoryTab.id ?? 'all'}-${selectedTag ?? 'all'}-${placeholderIndex}`"
                      :type="sectionType"
                    />
                  </view>
                  <text class="loading-more-text">正在加载...</text>
                </view>

                <!-- 没有更多数据 -->
                <view
                  v-if="
                    !loading && !loadingMore && getArticlesFor(categoryTab.id, selectedTag).length > 0 &&
                    !hasMore
                  "
                  class="no-more-box"
                >
                  <text class="no-more-text">没有更多了</text>
                </view>
              </template>
            </view>
          </template>

          <!-- 非当前分类：展示缓存数据 -->
          <template v-else>
            <view class="article-list article-list--cached">
              <template v-if="getArticlesFor(categoryTab.id, getSelectedTagForCategory(categoryTab.id)).length > 0">
                <!-- 文章类卡片 -->
                <s-cms-card-article
                  v-if="sectionType === 'article'"
                  v-for="article in getArticlesFor(categoryTab.id, getSelectedTagForCategory(categoryTab.id))"
                  :key="`article-${categoryTab.id ?? 'all'}-cached-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @share="handleShare"
                />

                <!-- 动态类卡片 -->
                <s-cms-card-dynamic
                  v-else-if="sectionType === 'dynamic'"
                  v-for="article in getArticlesFor(categoryTab.id, getSelectedTagForCategory(categoryTab.id))"
                  :key="`dynamic-${categoryTab.id ?? 'all'}-cached-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @share="handleShare"
                />

                <!-- 课程类卡片 -->
                <s-cms-card-course
                  v-else-if="sectionType === 'course'"
                  v-for="article in getArticlesFor(categoryTab.id, getSelectedTagForCategory(categoryTab.id))"
                  :key="`course-${categoryTab.id ?? 'all'}-cached-${article.id}`"
                  :article="article"
                  :config="sectionConfig"
                  @click="goDetail"
                  @like="handleLike"
                  @collect="handleCollect"
                  @enroll="handleEnroll"
                  @share="handleShare"
                />

                <!-- 自定义类型 -->
                <view
                  v-else
                  v-for="article in getArticlesFor(categoryTab.id, getSelectedTagForCategory(categoryTab.id))"
                  :key="`custom-${categoryTab.id ?? 'all'}-cached-${article.id}`"
                  class="article-card-simple"
                  @tap="goDetail(article.id)"
                >
                  <view class="article-title ss-line-2">{{ article.title }}</view>
                  <view v-if="article.subtitle" class="article-subtitle ss-line-1">{{
                    article.subtitle
                  }}</view>
                </view>
              </template>

              <view v-else class="loading-box">
                <text class="loading-text">滑动加载内容...</text>
              </view>
            </view>
          </template>
        </view>
      </swiper-item>
    </swiper>

    <!-- 悬浮操作按钮组 - iOS 26 Liquid Glass 设计 -->
    <view class="fab-container">
      <!-- 悬浮发布按钮 -->
      <view v-if="showPublishButton" class="fab-btn fab-publish" @tap="goToPublish">
        <view class="fab-icon">
          <text class="cicon-add"></text>
        </view>
      </view>

      <!-- 悬浮互动按钮 -->
      <view class="fab-btn fab-interaction" @tap="goToInteraction">
        <view class="fab-icon">
          <text class="cicon-favorite"></text>
        </view>
      </view>
    </view>
  </view>

  <!-- 全屏搜索遮罩层 -->
  <view v-if="isSearchExpanded" class="search-modal-overlay" @tap="cancelSearch">
    <view class="search-modal-content" @tap.stop>
      <view class="search-modal-header">
        <view class="search-bar-wrapper">
          <view class="search-icon-wrapper">
            <view class="_icon-search"></view>
          </view>
          <input
            class="search-modal-input"
            type="text"
            placeholder="搜索你感兴趣的内容..."
            v-model="searchKeyword"
            @confirm="handleSearch"
            @input="handleSearchInput"
            :focus="isSearchFocused"
          />
          <view v-if="searchKeyword" class="clear-icon-wrapper" @tap="clearSearch">
            <view class="_icon-close"></view>
          </view>
        </view>
        <view class="cancel-text" @tap="cancelSearch">取消</view>
      </view>
    </view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import {
    ref,
    computed,
    onMounted,
    nextTick,
    getCurrentInstance,
    onUnmounted,
    shallowRef,
    reactive,
    watch,
  } from 'vue';
  import { onPageScroll, onReachBottom } from '@dcloudio/uni-app';
  import articleApi from '@/sheep/api/promotion/cms/article';
  import sectionApi from '@/sheep/api/promotion/cms/section';
  import categoryApi from '@/sheep/api/promotion/cms/category';
  import tagApi from '@/sheep/api/promotion/cms/tag';
  import sCmsCardArticle from '@/sheep/components/s-cms-card-article/s-cms-card-article.vue';
  import sCmsCardDynamic from '@/sheep/components/s-cms-card-dynamic/s-cms-card-dynamic.vue';
  import sCmsCardCourse from '@/sheep/components/s-cms-card-course/s-cms-card-course.vue';
  import sCmsCardSkeleton from '@/sheep/components/s-cms-card-skeleton/s-cms-card-skeleton.vue';
  import { SharePageEnum } from '@/sheep/util/const';

  // 开发环境标志
  const isDev = process.env.NODE_ENV === 'development';

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

  /**
   * 性能优化：节流函数
   * @param {Function} func - 要节流的函数
   * @param {Number} delay - 延迟时间（毫秒）
   */
  const throttle = (func, delay = 16) => {
    let lastTime = 0;
    let timer = null;

    return function (...args) {
      const now = Date.now();
      const remaining = delay - (now - lastTime);

      if (remaining <= 0) {
        if (timer) {
          clearTimeout(timer);
          timer = null;
        }
        lastTime = now;
        func.apply(this, args);
      } else if (!timer) {
        timer = setTimeout(() => {
          lastTime = Date.now();
          timer = null;
          func.apply(this, args);
        }, remaining);
      }
    };
  };

  /**
   * 性能优化：防抖函数
   * @param {Function} func - 要防抖的函数
   * @param {Number} delay - 延迟时间（毫秒）
   */
  const debounce = (func, delay = 300) => {
    let timer = null;

    return function (...args) {
      if (timer) {
        clearTimeout(timer);
      }
      timer = setTimeout(() => {
        func.apply(this, args);
        timer = null;
      }, delay);
    };
  };

  const measureActiveListHeight = () => {
    uni
      .createSelectorQuery()
      .in(instance?.proxy)
      .select('.article-list--active')
      .boundingClientRect((rect) => {
        if (rect && rect.height) {
          articleSwiperHeight.value = Math.max(rect.height + 100, defaultSwiperHeight);
        } else {
          articleSwiperHeight.value = defaultSwiperHeight;
        }
      })
      .exec();
  };

  const updateArticleSwiperHeight = throttle(() => {
    nextTick(() => {
      measureActiveListHeight();
      setTimeout(measureActiveListHeight, 120);
      setTimeout(measureActiveListHeight, 280);
    });
  }, 150);

  /**
   * CMS信息流组件（完整版 - 性能优化版）
   *
   * 性能优化措施：
   * 1. 使用 shallowRef 减少大数据的深度响应式开销（list、categories、tags、sectionInfo）
   * 2. 滚动事件处理使用节流（16ms，约 60fps）
   * 3. DOM 查询添加缓存机制，避免重复查询
   * 4. 生产环境移除所有调试 console.log
   * 5. 列表渲染使用带类型前缀的唯一 key 值
   * 6. 计算属性避免不必要的重新计算
   */
  const props = defineProps({
    data: {
      type: Object,
      default: () => ({}),
    },
    styles: {
      type: Object,
      default: () => ({}),
    },
    // 兼容直接传参的方式
    sectionId: {
      type: Number,
      default: null,
    },
    config: {
      type: Object,
      default: () => ({}),
    },
  });

  // 获取实际的配置数据
  const actualSectionId = computed(() => props.data?.sectionId || props.sectionId);
  const actualConfig = computed(() => props.data || props.config);

  // 性能优化：使用 shallowRef 减少深度响应式开销
  // 列表数据通常是大数组，不需要深度响应式
  const list = shallowRef([]);
  const loading = ref(false);
  const categories = shallowRef([]); // 分类列表
  const tags = shallowRef([]); // 标签列表
  const selectedCategory = ref(null); // 当前选中的分类
  const selectedTag = ref(null); // 当前选中的标签
  const sectionInfo = shallowRef(null); // 板块信息

  // 分页相关状态
  const currentPage = ref(0); // 当前页码（已加载的最后一页）
  const pageSize = ref(10); // 每页大小
  const totalCount = ref(0); // 总数据量
  const hasMore = ref(true); // 是否还有更多数据
  const loadingMore = ref(false); // 是否正在加载更多

  // 搜索相关状态
  const searchKeyword = ref(''); // 搜索关键词
  const isSearchExpanded = ref(false); // 搜索框是否展开
  const isSearchFocused = ref(false); // 搜索框是否聚焦

  // 滑动相关状态
  const categoryScrollLeft = ref(0); // 分类滚动位置
  const tagScrollLeft = ref(0); // 标签滚动位置
  const categoryContainerWidth = ref(0); // 分类容器宽度
  const tagContainerWidth = ref(0); // 标签容器宽度
  const categoryItemWidths = ref([]); // 分类项宽度数组
  const tagItemWidths = ref([]); // 标签项宽度数组

  // 状态栏高度
  const statusBarHeight = ref(0);
  // 获取组件实例
  const instance = getCurrentInstance();

  // 智能吸顶状态
  const isSticky = ref(false);
  // 导航栏高度（缓存值，避免频繁查询）
  const navbarHeight = ref(0);
  // 导航栏原始位置（缓存值）
  const navbarOriginalTop = ref(0);
  // 导航栏偏移量
  const navbarOffset = ref(0);
  // DOM 查询标志位，避免重复查询
  const hasQueriedNavbar = ref(false);
  // 当前页面路由，用于区分刷新事件归属
  const pageRoute = ref('');

  // 计算属性：从配置中获取显示控制
  const showCategoryFilter = computed(() => actualConfig.value?.showCategoryFilter !== false);
  const showTagFilter = computed(() => actualConfig.value?.showTagFilter !== false);

  // 板块类型
  const sectionType = computed(() => sectionInfo.value?.type || 'article');

  // 板块配置
  const sectionConfig = computed(() => sectionInfo.value?.config || {});

  const parseSkeletonCount = () => {
    const raw = sectionConfig.value?.skeletonCount;
    if (raw === undefined || raw === null || raw === '') {
      return null;
    }
    const parsed = Number(raw);
    if (Number.isNaN(parsed) || parsed <= 0) {
      return null;
    }
    return Math.max(2, Math.min(Math.floor(parsed), 6));
  };

  const skeletonItemCount = computed(() => {
    const configured = parseSkeletonCount();
    if (configured !== null) {
      return configured;
    }

    switch (sectionType.value) {
      case 'dynamic':
        return 3;
      case 'course':
        return 3;
      default:
        return 3;
    }
  });

  const createPlaceholders = (count) => {
    const safeCount = Math.max(0, count);
    return Array.from({ length: safeCount }, (_, index) => index);
  };

  const skeletonPlaceholders = computed(() => createPlaceholders(skeletonItemCount.value));
  const loadMoreSkeletonPlaceholders = computed(() =>
    createPlaceholders(Math.min(Math.max(1, skeletonItemCount.value - 1), skeletonItemCount.value)),
  );

  // 分类显示数据（按原始顺序渲染即可）
  const displayCategories = computed(() => categories.value || []);

  // 创建智能循环的标签显示数据 - 根据数据量优化
  const displayTags = computed(() => tags.value || []);

  // 分类滑动面板 - 合并"全部"与实际分类
  const categoryTabs = computed(() => [{ id: null, name: '全部' }, ...(displayCategories.value || [])]);

  const categorySwiperDuration = 260;
  const tagSwiperDuration = 220;

  const categoryTagMap = reactive({ all: [] });
  const categoryTagSelection = reactive({ all: null });
  const listCache = reactive({});
  const paginationCache = reactive({});

  const getCategoryKey = (categoryId) =>
    categoryId === null || categoryId === undefined ? 'all' : String(categoryId);

  const getTagTabsForCategory = (categoryId) => {
    const key = getCategoryKey(categoryId);
    const baseTags = categoryTagMap[key] || [];
    return [{ id: null, name: '全部' }, ...baseTags];
  };

  const getSelectedTagForCategory = (categoryId) => {
    const key = getCategoryKey(categoryId);
    return categoryTagSelection[key] ?? null;
  };

  const activeCategoryIndex = computed(() => {
    const currentId = selectedCategory.value;
    const index = categoryTabs.value.findIndex((item) => item.id === currentId);
    return index === -1 ? 0 : index;
  });

  const activeCategoryTagTabs = computed(() => getTagTabsForCategory(selectedCategory.value));

  const activeTagIndex = computed(() => {
    const tabs = activeCategoryTagTabs.value;
    const currentTagId = selectedTag.value ?? null;
    const index = tabs.findIndex((item) => item.id === currentTagId);
    return index === -1 ? 0 : index;
  });

  const applyCachedState = (cacheKey) => {
    const cached = paginationCache[cacheKey];
    if (!cached) {
      list.value = [];
      totalCount.value = 0;
      hasMore.value = true;
      currentPage.value = 0;
      return;
    }

    list.value = Array.isArray(cached.list) ? [...cached.list] : [];
    totalCount.value = cached.total ?? list.value.length;
    hasMore.value = cached.hasMore ?? false;
    currentPage.value = cached.page ?? 0;
    listCache[cacheKey] = Array.isArray(cached.list) ? [...cached.list] : [];
  };

  const clearAllCaches = () => {
    Object.keys(listCache).forEach((key) => delete listCache[key]);
    Object.keys(paginationCache).forEach((key) => delete paginationCache[key]);
  };

  watch(
    () => [selectedCategory.value, selectedTag.value],
    () => {
      updateArticleSwiperHeight();
    }
  );

  watch(
    () => list.value,
    () => {
      updateArticleSwiperHeight();
    }
  );

  const categorySwiperCurrent = ref(0);
  const tagSwiperCurrent = ref(0);

  const syncCategorySwiperCurrent = () => {
    const index = categoryTabs.value.findIndex((item) => item.id === selectedCategory.value);
    categorySwiperCurrent.value = index === -1 ? 0 : index;
  };

  const syncTagSwiperCurrent = () => {
    const tabs = getTagTabsForCategory(selectedCategory.value);
    const currentTagId = selectedTag.value ?? null;
    const index = tabs.findIndex((item) => item.id === currentTagId);
    tagSwiperCurrent.value = index === -1 ? 0 : index;
  };

  watch(
    () => selectedCategory.value,
    () => {
      syncCategorySwiperCurrent();
      syncTagSwiperCurrent();
    },
    { immediate: true }
  );

  watch(
    () => selectedTag.value,
    () => {
      syncTagSwiperCurrent();
    }
  );

  watch(
    () => categoryTabs.value.length,
    () => {
      syncCategorySwiperCurrent();
    }
  );

  watch(
    () => activeCategoryTagTabs.value.length,
    () => {
      syncTagSwiperCurrent();
    }
  );

  const tagSwipeState = reactive({
    startX: 0,
    startY: 0,
    tracking: false,
    initialIndex: 0,
  });

  const TAG_SWIPE_MIN_DISTANCE = 80;
  const TAG_SWIPE_MAX_VERTICAL = 90;

  const extractTouchPoint = (touch) => ({
    x: touch?.clientX ?? touch?.pageX ?? touch?.x ?? 0,
    y: touch?.clientY ?? touch?.pageY ?? touch?.y ?? 0,
  });

  const switchToAdjacentCategory = async (direction) => {
    const nextIndex = activeCategoryIndex.value + direction;
    if (nextIndex < 0 || nextIndex >= categoryTabs.value.length) {
      return false;
    }

    const target = categoryTabs.value[nextIndex];
    if (!target) {
      return false;
    }

    categorySwiperCurrent.value = nextIndex;

    if (target.id === null) {
      await selectCategory(null);
      return true;
    }

    const displayIndex = displayCategories.value.findIndex((cat) => cat.id === target.id);
    await selectCategoryByIndex(target.id, displayIndex === -1 ? 0 : displayIndex);
    return true;
  };

  const buildCacheKey = (categoryId, tagId) => `${categoryId ?? 'all'}|${tagId ?? 'all'}`;

  const currentCacheKey = computed(() => buildCacheKey(selectedCategory.value, selectedTag.value));

  const shouldShowSkeleton = (categoryId, tagId) => {
    if (!loading.value || loadingMore.value) {
      return false;
    }
    const cacheKey = buildCacheKey(categoryId, tagId);
    if (cacheKey !== currentCacheKey.value) {
      return false;
    }
    return !Array.isArray(list.value) || list.value.length === 0;
  };

  const getArticlesFor = (categoryId, tagId) => {
    const cacheKey = buildCacheKey(categoryId, tagId);
    if (cacheKey === currentCacheKey.value) {
      return list.value || [];
    }
    return listCache[cacheKey] || [];
  };

  const systemInfo =
    typeof uni !== 'undefined' && typeof uni.getSystemInfoSync === 'function'
      ? uni.getSystemInfoSync()
      : {};
  const defaultSwiperHeight = Math.max(systemInfo?.windowHeight || 0, 600);
  const articleSwiperHeight = ref(defaultSwiperHeight);
  const articleSwiperStyle = computed(() => {
    const height = Math.max(articleSwiperHeight.value, 320);
    return {
      height: `${height}px`,
      minHeight: `${height}px`,
    };
  });

  // 检查用户是否已登录
  const isLogin = computed(() => sheep.$store('user').isLogin);

  // 检查是否显示发布按钮
  const showPublishButton = computed(() => {
    const ugcEnabled = sectionInfo.value?.config?.ugc_enabled === true;
    const userLoggedIn = isLogin.value;

    if (isDev) {
      console.log('发布按钮检查:', {
        sectionInfo: sectionInfo.value,
        config: sectionInfo.value?.config,
        ugc_enabled: sectionInfo.value?.config?.ugc_enabled,
        ugcEnabled,
        userLoggedIn,
        shouldShow: ugcEnabled && userLoggedIn,
      });
    }

    return ugcEnabled && userLoggedIn;
  });

  // 智能吸顶样式 - 参考 s-sticky-navbar 组件的实现
  const categoryNavStyle = computed(() => {
    // 吸顶状态下添加固定定位，完全脱离文档流
    if (isSticky.value) {
      return {
        position: 'fixed',
        top: `${navbarOffset.value}px`,
        left: '0',
        right: '0',
        zIndex: 999,
      };
    }

    // 非吸顶状态：不设置定位，保持在文档流中
    // 这样导航栏会自然占据空间，不会遮挡下面的内容
    // 图片加载后高度变化也不会影响导航栏位置
    return {};
  });

  // 容器样式 - 简化处理，让导航栏通过负边距自行突破
  const containerStyle = computed(() => {
    const style = props.styles || {};
    return {
      backgroundColor: style.bgColor || '#f5f5f5',
      // marginLeft: (style.marginLeft || 0) + 'rpx',
      // marginRight: (style.marginRight || 0) + 'rpx',
      marginBottom: (style.marginBottom || 0) + 'rpx',
      borderRadius: (style.borderRadius || 0) + 'rpx',
      // padding: (style.padding || 0) + 'rpx',
    };
  });

  const getActivePageRoute = () => {
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    return pages.length ? pages[pages.length - 1].route : '';
  };

  const currentShareArticleId = ref(null);

  const extractShareImage = (article) => {
    if (!article) return '';
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

  const extractShareDesc = (article) => {
    if (!article) return '';
    if (article.subtitle) return article.subtitle;
    if (article.summary) return article.summary;
    if (article.description) return article.description;
    if (article.content) {
      return article.content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').slice(0, 60);
    }
    return '';
  };

  const buildArticleShareInfo = (article) => {
    if (!article || !article.id) return null;

    const shareTitle = article.title || article.name || '精选内容推荐';
    const cover = extractShareImage(article);
    const desc = extractShareDesc(article);
    const shareInfo = sheep.$platform.share.getShareInfo(
      {
        title: shareTitle,
        image: cover,
        desc,
        params: {
          page: SharePageEnum.CMS_ARTICLE.value,
          query: article.id,
        },
      },
      {
        type: 'article',
        title: shareTitle,
        image: cover,
      },
    );

    const sharePath = `/pages/cms/detail?id=${article.id}`;
    shareInfo.path = sharePath;
    shareInfo.query = `id=${article.id}`;
    shareInfo.image = shareInfo.image || cover;
    shareInfo.title = shareInfo.title || shareTitle;
    if (!shareInfo.forward) {
      shareInfo.forward = {};
    }
    shareInfo.forward.path = sharePath;

    return shareInfo;
  };

  const publishShareInfo = (shareInfo, articleId) => {
    if (!shareInfo) return;
    currentShareArticleId.value = articleId || null;
    sheep.$platform.share.updateShareInfo(shareInfo);
    uni.$emit('cms:update-share-info', shareInfo);
  };

  // 性能优化：获取导航栏位置和高度（带缓存机制）
  const getNavbarPosition = () => {
    // 如果已经查询过且有有效值，不重复查询
    if (hasQueriedNavbar.value && navbarHeight.value > 0) {
      isDev && console.log('使用缓存的导航栏位置信息');
      return;
    }

    uni
      .createSelectorQuery()
      .in(instance?.proxy)
      .select('.filter-nav-fixed')
      .boundingClientRect((rect) => {
        if (!rect) {
          isDev && console.log('找不到筛选导航栏元素');
          return;
        }

        navbarHeight.value = rect.height;
        navbarOriginalTop.value = rect.top;
        hasQueriedNavbar.value = true;

        isDev && console.log('筛选导航栏高度:', navbarHeight.value);
        isDev && console.log('筛选导航栏原始位置:', navbarOriginalTop.value);

        // 获取系统信息
        uni.getSystemInfo({
          success: (res) => {
            statusBarHeight.value = res.statusBarHeight || 0;
            // 小程序导航栏高度一般为 44px
            const appNavbarHeight = 44;

            // 计算导航栏应该吸顶的位置（状态栏 + 应用导航栏）
            navbarOffset.value = statusBarHeight.value + appNavbarHeight;
            isDev && console.log('筛选导航栏吸顶偏移量:', navbarOffset.value);
          },
        });
      })
      .exec();
  };

  // 性能优化：页面滚动事件处理（核心逻辑，不添加节流）
  const handlePageScrollCore = (scrollTop) => {
    // 如果导航栏原始位置还未获取，重新获取
    if (navbarOriginalTop.value === 0 && !isSticky.value) {
      getNavbarPosition();
      return;
    }

    // 判断是否需要吸顶
    // 当页面滚动位置超过导航栏原始位置时，启用吸顶
    const shouldSticky = scrollTop > navbarOriginalTop.value - navbarOffset.value;

    if (shouldSticky !== isSticky.value) {
      isSticky.value = shouldSticky;
      isDev && console.log(shouldSticky ? '导航栏吸顶' : '导航栏取消吸顶');
    }
  };

  // 性能优化：滚动事件处理入口（使用节流优化性能）
  const handlePageScroll = throttle((e) => {
    handlePageScrollCore(e.scrollTop);
  }, 16); // 约 60fps

  const handleGlobalPullDownRefresh = async (payload = {}) => {
    if (!pageRoute.value) {
      pageRoute.value = getActivePageRoute();
    }

    if (payload.pagePath && payload.pagePath !== pageRoute.value) {
      return;
    }

    if (loading.value || loadingMore.value) {
      isDev && console.log('列表正在加载，忽略下拉刷新事件');
      return;
    }

    if (isDev) {
      console.log('收到页面下拉刷新事件，重新加载数据:', {
        pagePath: payload.pagePath,
        currentRoute: pageRoute.value,
      });
    }

    try {
      await loadData({ force: true });
      // 数据刷新后重置吸顶缓存，重新测量
      hasQueriedNavbar.value = false;
      navbarOriginalTop.value = 0;
      setTimeout(() => {
        getNavbarPosition();
        initializeDimensions();
      }, 300);
    } catch (error) {
      console.error('下拉刷新CMS信息流失败:', error);
    } finally {
      if (typeof payload?.done === 'function') {
        try {
          payload.done();
        } catch (callbackError) {
          console.error('执行下拉刷新完成回调失败:', callbackError);
        }
      }
    }
  };

  // 搜索相关函数
  // 展开搜索框
  const expandSearch = () => {
    isSearchExpanded.value = true;
    isSearchFocused.value = true;
  };

  // 取消搜索
  const cancelSearch = () => {
    // 保存当前搜索关键词,判断是否需要重新加载
    const hadKeyword = searchKeyword.value && searchKeyword.value.trim();

    isSearchExpanded.value = false;
    isSearchFocused.value = false;
    searchKeyword.value = '';

    // 只有在之前有搜索关键词的情况下才重新加载数据
    if (hadKeyword) {
      clearAllCaches();
      loadData({ force: true });
    }
  };

  // 清空搜索
  const clearSearch = () => {
    searchKeyword.value = '';
    // 重新加载数据
    clearAllCaches();
    loadData({ force: true });
  };

  // 处理搜索输入（防抖）
  const handleSearchInput = debounce(() => {
    if (searchKeyword.value.trim()) {
      // 搜索后关闭遮罩层
      isSearchExpanded.value = false;
      isSearchFocused.value = false;
      clearAllCaches();
      loadData({ force: true });
    }
  }, 500);

  // 处理搜索确认
  const handleSearch = () => {
    // 搜索后关闭遮罩层
    isSearchExpanded.value = false;
    isSearchFocused.value = false;
    clearAllCaches();
    loadData({ force: true });
  };

  // 初始化容器和元素尺寸信息
  const initializeDimensions = () => {
    nextTick(() => {
      // 获取分类容器尺寸
      uni
        .createSelectorQuery()
        .in(instance?.proxy)
        .select('.category-nav')
        .boundingClientRect((rect) => {
          if (rect) {
            categoryContainerWidth.value = rect.width;
          }
        })
        .exec();

      // 获取标签容器尺寸
      uni
        .createSelectorQuery()
        .in(instance?.proxy)
        .select('.tag-nav')
        .boundingClientRect((rect) => {
          if (rect) {
            tagContainerWidth.value = rect.width;
          }
        })
        .exec();

      // 获取所有分类项的宽度
      uni
        .createSelectorQuery()
        .in(instance?.proxy)
        .selectAll('.category-list .category-item')
        .boundingClientRect((rects) => {
          if (rects && rects.length > 0) {
            categoryItemWidths.value = rects.map((rect) => rect.width);
          }
        })
        .exec();

      // 获取所有标签项的宽度
      uni
        .createSelectorQuery()
        .in(instance?.proxy)
        .selectAll('.tag-item')
        .boundingClientRect((rects) => {
          if (rects && rects.length > 0) {
            tagItemWidths.value = rects.map((rect) => rect.width);
          }
        })
        .exec();
    });
  };

  // 标签滚动事件处理 - 智能无限循环
  const onTagScroll = (e) => {
    const scrollLeft = e.detail.scrollLeft;
    const originalLength = tags.value.length;

    if (originalLength === 0) return;

    // 单个项目时不处理循环滚动
    if (originalLength === 1) {
      return;
    }

    // 计算一个完整循环的宽度
    let totalWidth = 0;
    for (let i = 0; i < originalLength; i++) {
      totalWidth += tagItemWidths.value[i] || 80; // 默认宽度80rpx转px
    }

    if (originalLength === 2) {
      // 两个项目时的简化循环逻辑
      if (scrollLeft >= totalWidth * 2) {
        // 滚动超过两倍总宽度时重置到开始位置
        const newPosition = scrollLeft - totalWidth * 2;
        tagScrollLeft.value = newPosition;
      } else if (scrollLeft < 0) {
        // 向左滚动到边界时重置到结束位置
        const newPosition = scrollLeft + totalWidth * 2;
        tagScrollLeft.value = newPosition;
      }
    } else {
      // 三个及以上项目时的完整循环逻辑
      if (scrollLeft >= totalWidth && scrollLeft < totalWidth * 2) {
        // 在中间区域，不需要处理
      } else if (scrollLeft >= totalWidth * 2) {
        // 滚动到第三份数据，切换回第二份数据的对应位置
        const newPosition = scrollLeft - totalWidth * 2;
        tagScrollLeft.value = totalWidth + newPosition;
      } else if (scrollLeft < 0) {
        // 处理向左滚动到边界的情况
        const newPosition = scrollLeft + totalWidth;
        tagScrollLeft.value = totalWidth * 2 + newPosition;
      }
    }
  };

  // 根据实际点击位置滑动到居中的分类
  const scrollToCategoryCenterByIndex = (displayIndex) => {
    if (displayIndex === -1 || categoryContainerWidth.value === 0) return;

    // 等待DOM更新
    nextTick(() => {
      const widths = categoryItemWidths.value;
      if (!widths || widths.length === 0) {
        return;
      }

      const targetIndex = Math.max(0, Math.min(displayIndex, widths.length - 1));

      // 计算到该位置的累计宽度
      let scrollToLeft = 0;
      for (let i = 0; i < targetIndex; i++) {
        scrollToLeft += widths[i] || 120;
      }

      // 添加当前项的宽度的一半，使其居中
      const currentItemWidth = widths[targetIndex] || 120;
      scrollToLeft += currentItemWidth / 2;

      // 减去容器宽度的一半，实现居中
      scrollToLeft -= categoryContainerWidth.value / 2;

      // 确保不小于0
      scrollToLeft = Math.max(0, scrollToLeft);

      // 滚动到计算的位置
      categoryScrollLeft.value = scrollToLeft;
    });
  };

  // 根据实际点击位置滑动到居中的标签
  const scrollToTagCenterByIndex = (displayIndex) => {
    if (displayIndex === -1 || tagContainerWidth.value === 0) return;

    // 等待DOM更新
    nextTick(() => {
      // 计算到该位置的累计宽度
      let scrollToLeft = 0;
      for (let i = 0; i < displayIndex; i++) {
        scrollToLeft += tagItemWidths.value[i] || 80;
      }

      // 添加当前项的宽度的一半，使其居中
      const currentItemWidth = tagItemWidths.value[displayIndex] || 80;
      scrollToLeft += currentItemWidth / 2;

      // 减去容器宽度的一半，实现居中
      scrollToLeft -= tagContainerWidth.value / 2;

      // 确保不小于0
      scrollToLeft = Math.max(0, scrollToLeft);

      // 滚动到计算的位置
      tagScrollLeft.value = scrollToLeft;
    });
  };

  // 根据categoryId滚动到居中位置
  const scrollToCategoryCenter = (categoryId) => {
    if (!categoryId || categories.value.length === 0) {
      categoryScrollLeft.value = 0;
      return;
    }

    nextTick(() => {
      const displayIndex = displayCategories.value.findIndex((cat) => cat.id === categoryId);
      scrollToCategoryCenterByIndex(displayIndex);
    });
  };

  // 兼容的居中算法 - 根据tagId选择最近的相同选项
  const scrollToTagCenter = (tagId) => {
    if (!tagId || tags.value.length === 0) return;

    // 先初始化尺寸，确保有准确的数据
    initializeDimensions();

    nextTick(() => {
      // 获取当前滚动位置
      const currentScrollLeft = tagScrollLeft.value;
      const originalLength = tags.value.length;

      // 单个项目时直接居中
      if (originalLength === 1) {
        const itemWidth = tagItemWidths.value[0] || 80;
        const scrollLeft = Math.max(0, (itemWidth - tagContainerWidth.value) / 2);
        tagScrollLeft.value = scrollLeft;
        return;
      }

      // 找到所有相同tagId的displayIndex
      const sameTagIndices = [];
      displayTags.value.forEach((tag, index) => {
        if (tag.id === tagId) {
          sameTagIndices.push(index);
        }
      });

      if (sameTagIndices.length === 0) return;

      // 计算每个相同选项的当前位置，选择最近的
      let closestIndex = sameTagIndices[0];
      let minDistance = Infinity;

      sameTagIndices.forEach((index) => {
        // 计算该选项的左边界位置
        let itemLeft = 0;
        for (let i = 0; i < index; i++) {
          itemLeft += tagItemWidths.value[i] || 80;
        }

        // 计算该选项与当前可见区域中心的距离
        const itemCenter = itemLeft + (tagItemWidths.value[index] || 80) / 2;
        const visibleCenter = currentScrollLeft + tagContainerWidth.value / 2;
        const distance = Math.abs(itemCenter - visibleCenter);

        if (distance < minDistance) {
          minDistance = distance;
          closestIndex = index;
        }
      });

      // 滚动到最近的相同选项
      scrollToTagCenterByIndex(closestIndex);
    });
  };

  const handleCategorySwiperChange = async (e) => {
    const nextIndex = e?.detail?.current ?? 0;
    const targetTab = categoryTabs.value[nextIndex];

    categorySwiperCurrent.value = nextIndex;

    if (!targetTab || targetTab.id === selectedCategory.value) {
      return;
    }

    if (targetTab.id === null) {
      await selectCategory(null);
    } else {
      const displayIndex = displayCategories.value.findIndex((cat) => cat.id === targetTab.id);
      await selectCategoryByIndex(targetTab.id, displayIndex === -1 ? 0 : displayIndex);
    }
  };

  const handleCategorySwiperAnimationEnd = (e) => {
    const index = e?.detail?.current ?? activeCategoryIndex.value;
    const targetTab = categoryTabs.value[index];

    categorySwiperCurrent.value = index;

    if (!targetTab) {
      return;
    }

    if (targetTab.id === null) {
      categoryScrollLeft.value = 0;
    } else {
      const displayIndex = displayCategories.value.findIndex((cat) => cat.id === targetTab.id);
      scrollToCategoryCenterByIndex(displayIndex);
    }

    updateArticleSwiperHeight();
  };

  const handleTagSwiperChange = async (e, categoryId) => {
    const nextIndex = e?.detail?.current ?? 0;
    const tagTabsForCategory = getTagTabsForCategory(categoryId);
    const targetTag = tagTabsForCategory[nextIndex];

    if (categoryId === selectedCategory.value) {
      tagSwiperCurrent.value = nextIndex;
    }

    if (!targetTag) {
      return;
    }

    if (categoryId !== selectedCategory.value) {
      categoryTagSelection[getCategoryKey(categoryId)] = targetTag.id ?? null;
      return;
    }

    if ((targetTag.id ?? null) === (selectedTag.value ?? null)) {
      return;
    }

    if (targetTag.id === null) {
      await selectTag(null);
    } else {
      await selectTag(targetTag.id);
    }
  };

  const handleTagSwiperAnimationEnd = (e, categoryId) => {
    if (categoryId !== selectedCategory.value) {
      return;
    }

    const index = e?.detail?.current ?? activeTagIndex.value;
    const tagTabsForCategory = getTagTabsForCategory(categoryId);
    const targetTag = tagTabsForCategory[index];

    tagSwiperCurrent.value = index;

    if (!targetTag) {
      return;
    }

    if (targetTag.id === null) {
      tagScrollLeft.value = 0;
    } else {
      scrollToTagCenter(targetTag.id);
    }

    updateArticleSwiperHeight();
  };

  const handleTagSwiperTouchStart = (e, categoryId) => {
    if (categoryId !== selectedCategory.value || activeCategoryTagTabs.value.length <= 1) {
      tagSwipeState.tracking = false;
      return;
    }
    if (!e.touches || e.touches.length !== 1) {
      tagSwipeState.tracking = false;
      return;
    }

    const point = extractTouchPoint(e.touches[0]);
    tagSwipeState.startX = point.x;
    tagSwipeState.startY = point.y;
    tagSwipeState.tracking = true;
    tagSwipeState.initialIndex = tagSwiperCurrent.value;
  };

  const handleTagSwiperTouchMove = (e) => {
    if (!tagSwipeState.tracking || !e.touches || e.touches.length !== 1) {
      return;
    }

    const point = extractTouchPoint(e.touches[0]);
    const deltaX = point.x - tagSwipeState.startX;
    const deltaY = point.y - tagSwipeState.startY;

    if (Math.abs(deltaY) > Math.abs(deltaX) + 10 || Math.abs(deltaY) > TAG_SWIPE_MAX_VERTICAL) {
      tagSwipeState.tracking = false;
    }
  };

  const handleTagSwiperTouchEnd = async (e) => {
    if (!tagSwipeState.tracking) {
      tagSwipeState.tracking = false;
      return;
    }

    tagSwipeState.tracking = false;

    if (!e.changedTouches || e.changedTouches.length === 0) {
      return;
    }

    const point = extractTouchPoint(e.changedTouches[0]);
    const deltaX = point.x - tagSwipeState.startX;
    const deltaY = point.y - tagSwipeState.startY;

    if (Math.abs(deltaX) < TAG_SWIPE_MIN_DISTANCE || Math.abs(deltaX) < Math.abs(deltaY)) {
      return;
    }

    if (tagSwiperCurrent.value !== tagSwipeState.initialIndex) {
      return;
    }

    if (deltaX > 0 && tagSwiperCurrent.value === 0) {
      await switchToAdjacentCategory(-1);
    } else if (
      deltaX < 0 &&
      activeCategoryTagTabs.value.length > 0 &&
      tagSwiperCurrent.value === activeCategoryTagTabs.value.length - 1
    ) {
      await switchToAdjacentCategory(1);
    }
  };

  // 根据实际点击索引选择分类
  const selectCategoryByIndex = async (categoryId, displayIndex) => {
    selectedCategory.value = categoryId;
    const categoryKey = getCategoryKey(categoryId);
    selectedTag.value = getSelectedTagForCategory(categoryId);
    const targetIndex = categoryTabs.value.findIndex((item) => item.id === categoryId);
    categorySwiperCurrent.value = targetIndex === -1 ? 0 : targetIndex;
    initializeDimensions();
    setTimeout(() => scrollToCategoryCenterByIndex(displayIndex), 200);

    const tagList = await loadTagsForCategory(categoryId);
    if (
      selectedTag.value &&
      (!Array.isArray(tagList) || !tagList.some((tag) => tag.id === selectedTag.value))
    ) {
      selectedTag.value = null;
      categoryTagSelection[categoryKey] = null;
    }

    await loadData({ useCache: true });
    updateArticleSwiperHeight();
  };

  const selectTagByIndex = async (tagId, displayIndex) => {
    selectedTag.value = tagId;
    categoryTagSelection[getCategoryKey(selectedCategory.value)] = tagId ?? null;
    tagSwiperCurrent.value = displayIndex === -1 ? 0 : displayIndex;
    initializeDimensions();
    if (!tagId) {
      tagScrollLeft.value = 0;
    }
    setTimeout(() => scrollToTagCenterByIndex(displayIndex), 200);
    await loadData({ useCache: true });
    updateArticleSwiperHeight();
  };

  const selectCategory = async (categoryId) => {
    selectedCategory.value = categoryId;
    const categoryKey = getCategoryKey(categoryId);
    selectedTag.value = getSelectedTagForCategory(categoryId);
    syncCategorySwiperCurrent();
    syncTagSwiperCurrent();
    initializeDimensions();
    setTimeout(() => scrollToCategoryCenter(categoryId), 200);

    const tagList = await loadTagsForCategory(categoryId);
    if (
      selectedTag.value &&
      (!Array.isArray(tagList) || !tagList.some((tag) => tag.id === selectedTag.value))
    ) {
      selectedTag.value = null;
      categoryTagSelection[categoryKey] = null;
    }

    await loadData({ useCache: true });
    updateArticleSwiperHeight();
  };

  const selectTag = async (tagId) => {
    selectedTag.value = tagId;
    categoryTagSelection[getCategoryKey(selectedCategory.value)] = tagId ?? null;
    syncTagSwiperCurrent();
    initializeDimensions();
    if (!tagId) {
      tagScrollLeft.value = 0;
    }
    setTimeout(() => scrollToTagCenter(tagId), 200);
    await loadData({ useCache: true });
    updateArticleSwiperHeight();
  };

  // 加载指定分类下的标签
  const loadTagsForCategory = async (categoryId) => {
    const categoryKey = getCategoryKey(categoryId);

    if (!categoryId) {
      categoryTagMap[categoryKey] = [];
      if (selectedCategory.value === categoryId) {
        tags.value = [];
        tagScrollLeft.value = 0;
      }
      return [];
    }

    if (!showTagFilter.value) {
      categoryTagMap[categoryKey] = categoryTagMap[categoryKey] || [];
      if (selectedCategory.value === categoryId) {
        tags.value = categoryTagMap[categoryKey];
      }
      return categoryTagMap[categoryKey];
    }

    if (categoryTagMap[categoryKey]) {
      if (selectedCategory.value === categoryId) {
        tags.value = categoryTagMap[categoryKey];
        tagScrollLeft.value = 0;
        await nextTick();
        setTimeout(() => initializeDimensions(), 100);
      }
      return categoryTagMap[categoryKey];
    }

    try {
      const res = await tagApi.list({ categoryId });
      isDev && console.log('分类下标签返回:', res);
      if (res.data && Array.isArray(res.data)) {
        categoryTagMap[categoryKey] = res.data;
      } else {
        categoryTagMap[categoryKey] = [];
      }
    } catch (error) {
      console.error('加载分类标签失败:', error);
      categoryTagMap[categoryKey] = [];
    }

    if (selectedCategory.value === categoryId) {
      tags.value = categoryTagMap[categoryKey];
      tagScrollLeft.value = 0;
      await nextTick();
      setTimeout(() => initializeDimensions(), 100);
    }

    return categoryTagMap[categoryKey];
  };

  // 加载分类列表
  const loadCategoriesAndTags = async () => {
    const currentSectionId = actualSectionId.value;
    if (!currentSectionId) return;

    // 重置分类和标签选择
    selectedCategory.value = null;
    selectedTag.value = null;
    categoryTagSelection.all = null;
    categoryTagMap.all = [];
    Object.keys(categoryTagMap).forEach((key) => {
      if (key !== 'all') {
        delete categoryTagMap[key];
      }
    });
    Object.keys(categoryTagSelection).forEach((key) => {
      if (key !== 'all') {
        delete categoryTagSelection[key];
      }
    });
    clearAllCaches();

    // 加载分类
    if (showCategoryFilter.value) {
      try {
        const res = await categoryApi.list({ sectionId: currentSectionId });
        isDev && console.log('分类列表返回:', res);
        if (res.data && Array.isArray(res.data)) {
          categories.value = res.data;
          // 数据加载完成后重新计算导航栏位置
          await nextTick();
          getNavbarPosition();

          // 初始化分类尺寸信息
          setTimeout(() => initializeDimensions(), 100);
        }
      } catch (error) {
        console.error('加载分类列表失败:', error);
        categories.value = [];
      }
    }

    // 初始时不加载标签，等待用户选择分类后再加载
    tags.value = [];
    tagScrollLeft.value = 0;
  };

  // 加载文章数据
  const loadData = async (options = {}) => {
    let isLoadMore = false;
    let useCache = false;
    let force = false;

    if (typeof options === 'boolean') {
      isLoadMore = options;
    } else if (options && typeof options === 'object') {
      ({ isLoadMore = false, useCache = false, force = false } = options);
    }

    const cacheKey = currentCacheKey.value;
    const cachedState = paginationCache[cacheKey];

    if (!isLoadMore && useCache && !force && cachedState) {
      applyCachedState(cacheKey);
      updateArticleSwiperHeight();
      loading.value = false;
      loadingMore.value = false;
      return;
    }

    // 使用配置的板块ID
    const currentSectionId = actualSectionId.value;

    // 如果没有板块ID，则不加载
    if (!currentSectionId) {
      isDev && console.warn('CMS组件未配置板块ID');
      return;
    }

    const hasMoreForCombo = cachedState ? cachedState.hasMore : hasMore.value;
    if (isLoadMore && !hasMoreForCombo) {
      isDev && console.log('没有更多数据了');
      hasMore.value = false;
      return;
    }

    if (isLoadMore && loadingMore.value) {
      isDev && console.log('正在加载更多，跳过重复请求');
      return;
    }

    if (isLoadMore) {
      loadingMore.value = true;
    } else {
      loading.value = true;
      if (force || !cachedState) {
        list.value = [];
        totalCount.value = 0;
        hasMore.value = true;
        currentPage.value = 0;
      } else {
        applyCachedState(cacheKey);
      }
    }

    const basePage = cachedState?.page ?? currentPage.value ?? 0;
    const targetPage = isLoadMore ? basePage + 1 : 1;

    const params = {
      sectionId: currentSectionId,
      pageNo: targetPage,
      pageSize: pageSize.value,
    };

    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value;
    }

    if (selectedTag.value) {
      params.tagId = selectedTag.value;
    }

    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.title = searchKeyword.value.trim();
    }

    try {
      isDev && console.log('加载CMS文章数据:', params);
      const res = await articleApi.page(params);
      isDev && console.log('CMS文章数据返回:', res);

      let newList = [];
      let total = 0;

      if (res.data && res.data.list) {
        newList = res.data.list;
        total = res.data.total || 0;
      } else if (Array.isArray(res.data)) {
        newList = res.data;
        total = res.data.length;
      }

      if (isLoadMore) {
        list.value = [...(list.value || []), ...newList];
      } else {
        list.value = newList;
      }

      totalCount.value = total;
      hasMore.value = total > list.value.length;

      if (isLoadMore) {
        if (newList.length > 0) {
          currentPage.value = targetPage;
        }
      } else {
        currentPage.value = targetPage;
      }

      const cachedRecord = {
        list: Array.isArray(list.value) ? [...list.value] : [],
        total,
        hasMore: hasMore.value,
        page: currentPage.value,
      };
      paginationCache[cacheKey] = cachedRecord;
      listCache[cacheKey] = [...cachedRecord.list];
      categoryTagSelection[getCategoryKey(selectedCategory.value)] = selectedTag.value ?? null;

      updateArticleSwiperHeight();

      isDev &&
        console.log('数据加载完成:', {
          currentPage: currentPage.value,
          listLength: list.value.length,
          total: total,
          hasMore: hasMore.value,
        });
    } catch (error) {
      console.error('加载文章数据失败:', error);
      if (!isLoadMore) {
        list.value = [];
        listCache[cacheKey] = [];
        paginationCache[cacheKey] = {
          list: [],
          total: 0,
          hasMore: false,
          page: 0,
        };
      }
    } finally {
      if (isLoadMore) {
        loadingMore.value = false;
      } else {
        loading.value = false;
      }
      updateArticleSwiperHeight();
    }
  };

  // 跳转到详情页
  const goDetail = (id) => {
    sheep.$router.go('/pages/cms/detail', { id });
  };

  // 跳转到发布页面
  const goToPublish = () => {
    if (!isLogin.value) {
      sheep.$router.go('/pages/user/login');
      return;
    }

    sheep.$router.go('/pages/cms/publish', {
      sectionId: actualSectionId.value,
    });
  };

  // 跳转到互动页面
  const goToInteraction = () => {
    if (!isLogin.value) {
      sheep.$router.go('/pages/user/login');
      return;
    }

    sheep.$router.go('/pages/cms/interaction');
  };

  // 处理点赞 - 串行执行+优化乐观更新
  const handleLike = async (id, successCallback, errorCallback) => {
    try {
      const { article: beforeArticle } = findArticleRecord(id);
      const previousLiked = normalizeBoolean(beforeArticle?.userLiked);

      // 先调用成功回调，立即执行乐观更新（UI状态变化+点赞数+1/-1）
      if (successCallback) {
        successCallback();
      }

      // 串行执行API调用
      const res = await articleApi.like(id);
      isDev && console.log('点赞结果:', res);

      if (res.code === 0) {
        // API成功后，刷新单条记录获取准确数据
        await refreshSingleArticle(id);
        const { article: updatedArticle } = findArticleRecord(id);
        const likedNow =
          updatedArticle !== null
            ? normalizeBoolean(updatedArticle.userLiked)
            : !previousLiked;
        showFeedbackToast(likedNow ? '已点赞' : '已取消点赞');
      } else {
        // API失败，回滚乐观更新
        if (errorCallback) {
          errorCallback(new Error(res.msg || '点赞失败'));
        } else {
          showFeedbackToast(res.msg || '点赞失败');
        }
      }
    } catch (error) {
      console.error('点赞失败:', error);
      // API异常，回滚乐观更新
      if (errorCallback) {
        errorCallback(new Error('点赞失败'));
      } else {
        showFeedbackToast('点赞失败');
      }
    }
  };

  // 刷新单条文章记录 - 优化时差处理
  const refreshSingleArticle = async (articleId) => {
    try {
      // 获取文章详情，包含最新的点赞数、收藏数等
      const res = await articleApi.detail(articleId);
      if (res.code === 0 && res.data) {
        // 更新列表中对应文章的数据
        const articleIndex = list.value.findIndex((article) => article.id === articleId);
        if (articleIndex !== -1) {
          // 获取当前文章数据
          const currentArticle = list.value[articleIndex];

          // 平滑更新：只更新确实需要更新的字段
          const updatedArticle = {
            ...currentArticle,
            // 更新统计数据（使用后端准确数据）
            likeCount:
              res.data.likeCount !== undefined ? res.data.likeCount : currentArticle.likeCount,
            collectCount:
              res.data.collectCount !== undefined
                ? res.data.collectCount
                : currentArticle.collectCount,
            viewCount:
              res.data.viewCount !== undefined ? res.data.viewCount : currentArticle.viewCount,
            shareCount:
              res.data.shareCount !== undefined ? res.data.shareCount : currentArticle.shareCount,
            // 更新用户状态（后端权威数据）
            userLiked:
              res.data.userLiked !== undefined
                ? normalizeBoolean(res.data.userLiked)
                : normalizeBoolean(currentArticle.userLiked),
            userCollected:
              res.data.userCollected !== undefined
                ? normalizeBoolean(res.data.userCollected)
                : normalizeBoolean(currentArticle.userCollected),
          };

          // 使用 nextTick 确保 UI 更新平滑
          await nextTick(() => {
            list.value[articleIndex] = updatedArticle;
            isDev &&
              console.log('单条文章数据已平滑更新:', {
                id: articleId,
                likeCount: updatedArticle.likeCount,
                userLiked: updatedArticle.userLiked,
                collectCount: updatedArticle.collectCount,
                userCollected: updatedArticle.userCollected,
              });
          });
        }
      }
    } catch (error) {
      console.error('刷新单条文章失败:', error);
      // 刷新失败不影响用户体验，因为已经有乐观更新
    }
  };

  const findArticleRecord = (articleId) => {
    const index = list.value.findIndex((article) => article.id === articleId);
    return {
      index,
      article: index !== -1 ? list.value[index] : null,
    };
  };

  const showFeedbackToast = (message) => {
    if (!message) return;
    uni.showToast({
      title: message,
      icon: 'none',
    });
  };

  // 处理收藏 - 串行执行+优化乐观更新
  const handleCollect = async (id, successCallback, errorCallback) => {
    try {
      const { article: beforeArticle } = findArticleRecord(id);
      const previousCollected = normalizeBoolean(beforeArticle?.userCollected);

      // 先调用成功回调，立即执行乐观更新（UI状态变化+收藏数+1/-1）
      if (successCallback) {
        successCallback();
      }

      // 串行执行API调用
      const res = await articleApi.collect(id);
      isDev && console.log('收藏结果:', res);

      if (res.code === 0) {
        // API成功后，刷新单条记录获取准确数据
        await refreshSingleArticle(id);
        const { article: updatedArticle } = findArticleRecord(id);
        const collectedNow =
          updatedArticle !== null
            ? normalizeBoolean(updatedArticle.userCollected)
            : !previousCollected;
        showFeedbackToast(collectedNow ? '已收藏' : '已取消收藏');
      } else {
        // API失败，回滚乐观更新
        if (errorCallback) {
          errorCallback(new Error(res.msg || '收藏失败'));
        } else {
          showFeedbackToast(res.msg || '收藏失败');
        }
      }
    } catch (error) {
      console.error('收藏失败:', error);
      // API异常，回滚乐观更新
      if (errorCallback) {
        errorCallback(new Error('收藏失败'));
      } else {
        showFeedbackToast('收藏失败');
      }
    }
  };

  const handleEnroll = (id) => {
    goDetail(id);
  };

  // 处理分享
  const handleShare = async (payload) => {
    if (!payload || !payload.article) {
      return;
    }

    const { article, type = 'friend' } = payload;
    const articleId = article.id;
    if (!articleId) {
      return;
    }

    if (type === 'friend') {
      const shareInfo = buildArticleShareInfo(article);
      if (shareInfo) {
        publishShareInfo(shareInfo, articleId);
      }
    }

    try {
      await articleApi.share(articleId);
      await refreshSingleArticle(articleId);
    } catch (error) {
      console.error('分享API调用失败:', error);
    }
  };

  // 加载板块信息
  const loadSectionInfo = async () => {
    const currentSectionId = actualSectionId.value;
    if (!currentSectionId) return;

    try {
      const res = await sectionApi.detail(currentSectionId);
      isDev && console.log('板块信息返回:', res);
      if (res.code === 0 && res.data) {
        sectionInfo.value = res.data;
      }
    } catch (error) {
      console.error('加载板块信息失败:', error);
    }
  };

  // 监听页面滚动
  onPageScroll((e) => {
    handlePageScroll(e);
  });

  // 触底加载更多
  onReachBottom(() => {
    isDev && console.log('触底，触发加载更多');
    loadData({ isLoadMore: true });
  });

  // 初始化
  onMounted(async () => {
    pageRoute.value = getActivePageRoute();
    uni.$on('diy:pull-down-refresh', handleGlobalPullDownRefresh);

    if (isDev) {
      console.log('CMS组件挂载，配置数据:', {
        sectionId: actualSectionId.value,
        config: actualConfig.value,
        showCategoryFilter: showCategoryFilter.value,
        showTagFilter: showTagFilter.value,
      });
    }

    // 加载板块信息
    await loadSectionInfo();

    // 加载分类
    await loadCategoriesAndTags();

    // 加载文章数据
    await loadData({ force: true });

    // 延迟获取导航栏位置，确保DOM已渲染
    setTimeout(() => {
      getNavbarPosition();
    }, 1000);

    // 初始化尺寸信息并保持选中项居中
    setTimeout(() => {
      initializeDimensions();

      if (!categories.value.length) {
        return;
      }

      if (selectedCategory.value) {
        scrollToCategoryCenter(selectedCategory.value);
      } else {
        categoryScrollLeft.value = 0;
      }
    }, 600);
  });

  // 页面卸载时清理
  onUnmounted(() => {
    isDev && console.log('CMS组件卸载');
    uni.$off('diy:pull-down-refresh', handleGlobalPullDownRefresh);
  });
</script>

<style lang="scss" scoped>
  .cms-stream-container {
    width: 100%;
    background-color: #f5f5f5;
    min-height: 200rpx;
  }

  /* 分类导航容器 - 包含全部、分类滚动和搜索图标 */
  .category-nav-container {
    position: relative; /* 为搜索覆盖层提供定位上下文 */
    display: flex;
    align-items: center;
    padding: 10rpx 20rpx 10rpx 20rpx;
  }

  /* 搜索图标按钮 - 固定在右侧 */
  .search-icon-btn {
    position: relative; /* 为小红点提供定位上下文 */
    flex-shrink: 0;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 16rpx;
    transition: all 0.3s ease;
    box-shadow: -4rpx 0 8rpx rgba(0, 0, 0, 0.06);
    border-radius: 8rpx;
    background-color: #fff;
    z-index: 2; /* 确保在滚动层之上 */

    &:active {
      transform: scale(0.95);
    }
  }

  /* 搜索图标右上角的小红点 */
  .search-badge {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 16rpx;
    height: 16rpx;
    background-color: #ff4d4f;
    border-radius: 50%;
    border: 2rpx solid #fff;
    box-shadow: 0 2rpx 4rpx rgba(255, 77, 79, 0.4);
    animation: badgePulse 2s ease-in-out infinite;
  }

  @keyframes badgePulse {
    0%,
    100% {
      transform: scale(1);
      opacity: 1;
    }
    50% {
      transform: scale(1.1);
      opacity: 0.8;
    }
  }

  /* ==================== 全屏搜索遮罩层样式 ==================== */

  /* 遮罩层背景 */
  .search-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 9999;
    display: flex;
    flex-direction: column;
    justify-content: center; /* 垂直居中 */
    align-items: center; /* 水平居中 */
    animation: fadeIn 0.3s ease;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  /* 搜索内容区 */
  .search-modal-content {
    background-color: transparent;
    width: 100%;
    max-width: 100%; /* 确保不超出屏幕 */
    padding: 0 30rpx; /* 左右留出边距 */
    box-sizing: border-box; /* 包含padding在宽度内 */
    animation: slideDownIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }

  @keyframes slideDownIn {
    from {
      transform: translateY(-100rpx);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }

  /* 搜索头部 */
  .search-modal-header {
    display: flex;
    align-items: center;
    gap: 20rpx;
    width: 100%; /* 确保占满宽度 */
    box-sizing: border-box; /* 包含padding在宽度内 */
  }

  /* 搜索栏包裹器 */
  .search-bar-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: #fff;
    border-radius: 50rpx;
    padding: 24rpx 32rpx;
    gap: 16rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  }

  /* 搜索图标包裹器 */
  .search-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 32rpx;
  }

  /* 搜索输入框 */
  .search-modal-input {
    flex: 1;
    font-size: 32rpx;
    color: #333;
    height: 100%;
    background: transparent;
    border: none;
    outline: none;
  }

  /* 清除图标包裹器 */
  .clear-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8rpx;
    color: #ccc;
    font-size: 28rpx;
    background-color: #f5f5f5;
    border-radius: 50%;
    transition: all 0.2s ease;

    &:active {
      background-color: #e0e0e0;
      transform: scale(0.9);
    }
  }

  /* 取消文本按钮 */
  .cancel-text {
    font-size: 32rpx;
    color: #fff;
    padding: 0 8rpx;
    white-space: nowrap;
    transition: opacity 0.2s ease;

    &:active {
      opacity: 0.7;
    }
  }

  /* 筛选导航外层包裹容器 - 提供定位上下文，撑满视口宽度 */
  .filter-nav-wrapper {
    /* 关键修复：提供定位上下文给子元素 */
    position: relative;
    /* 使用100vw撑满视口，配合负边距突破父容器限制 */
    width: 100vw;
    /* 使用更可靠的负边距计算：直接从左边缘偏移到视口左边缘 */
    margin-left: calc(50% - 50vw);
    /* 保持溢出可见，确保子元素可以正常显示 */
    overflow: visible;
  }

  /* 筛选导航固定定位 - 参考 s-sticky-navbar 的实现 */
  .filter-nav-fixed {
    background-color: #fff;
    box-sizing: border-box;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    /* 非吸顶状态下不设置 position，保持在文档流中，避免遮挡内容 */
    /* 吸顶状态的样式由内联样式控制（position: fixed） */
    /* 关键修复：隐藏X轴溢出，防止滑动标签导航时移动整个容器，但允许Y轴溢出以显示搜索框 */
    overflow-x: hidden;
    overflow-y: visible;

    /* 吸顶状态样式 */
    &.is-sticky {
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    }

    /* 隐藏滚动条 */
    &::-webkit-scrollbar {
      display: none;
    }
  }

  /* 标签容器样式 */
  .tag-nav-container {
    background-color: #fff;
    border-top: 1rpx solid #f5f5f5;
    display: flex;
    align-items: center;
    padding: 16rpx 20rpx 16rpx 20rpx;
  }

  /* 占位元素样式 */
  .navbar-placeholder {
    width: 100%;
  }

  /* 分类导航 - 适应固定布局 */
  .category-nav {
    flex: 1;
    white-space: nowrap;
    overflow-x: auto;
    overflow-y: hidden;
  }

  .category-list {
    display: inline-flex;
    padding: 0;
    /* 添加等距分布 - 与DIY导航一致 */
    justify-content: flex-start;
  }

  /* 固定的"全部"分类 */
  .category-all-fixed {
    position: relative;
    flex-shrink: 0;
    padding: 16rpx 24rpx;
    text-align: center;
    display: inline-flex;
    justify-content: center;
    min-width: 120rpx;
    background-color: #fff;
    z-index: 2; /* 确保在滚动层之上 */

    .nav-text {
      font-size: 28rpx;
      transition: color 0.3s;
      white-space: nowrap;
      color: #666;
    }

    .nav-underline {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 6rpx;
      border-radius: 3rpx;
      background-color: var(--ui-BG-Main, #ff6b00);
      transition: all 0.3s;
    }

    &.active {
      font-weight: bold;
    }
  }

  .category-item {
    position: relative;
    padding: 16rpx 24rpx;
    text-align: center;

    /* 默认情况下不使用flex:1，适用于导航项较多的情况 */
    display: inline-flex;
    justify-content: center;
    min-width: 120rpx; /* 设置最小宽度 */

    /* 如果分类项较少，使用等宽布局 */
    &.auto-width {
      flex: 1;
    }

    .nav-text {
      font-size: 28rpx;
      transition: color 0.3s;
      white-space: nowrap;
      color: #666;
    }

    .nav-underline {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 6rpx;
      border-radius: 3rpx;
      background-color: var(--ui-BG-Main, #ff6b00);
      transition: all 0.3s;
    }

    &.active {
      font-weight: bold;
    }
  }

  /* 固定的"全部"标签 */
  .tag-all-fixed {
    position: relative;
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10rpx 24rpx;
    margin-right: 16rpx;
    background-color: #f8f8f8;
    border-radius: 20rpx;
    font-size: 26rpx;
    color: #666;
    white-space: nowrap;
    min-width: 60rpx;
    transition: all 0.3s ease;
    z-index: 2; /* 确保在滚动层之上 */

    &.active {
      background-color: var(--ui-BG-Main-light, #ffead9);
      color: var(--ui-BG-Main, #ff6b00);
      font-weight: bold;
      border-color: var(--ui-BG-Main, #ff6b00);
      transform: scale(1.02);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  /* 标签导航 */
  .tag-nav {
    flex: 1;
    white-space: nowrap;
    background-color: transparent;
    overflow-x: auto;
    overflow-y: hidden;
  }

  .tag-list {
    display: inline-flex;
    padding: 0;
  }

  .tag-item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10rpx 24rpx;
    margin-right: 16rpx;
    margin-left: 0;
    background-color: #f8f8f8;
    border-radius: 20rpx;
    font-size: 26rpx;
    color: #666;
    white-space: nowrap;
    min-width: 60rpx;
    transition: all 0.3s ease;
    //border: 1rpx solid transparent;

    &:last-child {
      margin-right: 0;
    }

    &.active {
      background-color: var(--ui-BG-Main-light, #ffead9);
      color: var(--ui-BG-Main, #ff6b00);
      font-weight: bold;
      border-color: var(--ui-BG-Main, #ff6b00);
      transform: scale(1.02);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  .article-swiper {
    width: 100%;
  }

  .category-swiper-item,
  .category-swiper-item-inner,
  .tag-swiper,
  .tag-swiper-item {
    width: 100%;
    height: inherit;
  }

  .category-swiper-item-inner {
    min-height: 200rpx;
  }

  .article-list--active {
    opacity: 1;
  }

  .article-list--cached {
    opacity: 0.45;
  }

  /* 文章列表 */
  .article-list {
    padding: 20rpx;
  }

  .skeleton-list {
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  .skeleton-list--loading-more {
    width: 100%;
  }

  /* 自定义类型简单布局 */
  .article-card-simple {
    background-color: white;
    padding: 24rpx;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    .article-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      line-height: 1.5;
      margin-bottom: 12rpx;
    }

    .article-subtitle {
      font-size: 26rpx;
      color: #999;
      line-height: 1.4;
    }
  }

  /* 空状态 */
  .empty-box {
    padding: 100rpx 0;
    text-align: center;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }

  /* 加载状态 */
  .loading-box {
    padding: 40rpx 0;
    text-align: center;
  }

  .loading-text {
    font-size: 28rpx;
    color: #999;
  }

  /* 加载更多状态 */
  .loading-more-box {
    padding: 30rpx 0;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
  }

  .loading-more-text {
    font-size: 26rpx;
    color: #999;
  }

  /* 没有更多数据 */
  .no-more-box {
    padding: 30rpx 0;
    text-align: center;
  }

  .no-more-text {
    font-size: 26rpx;
    color: #ccc;
  }

  /* ==================== iOS 26 Liquid Glass 悬浮按钮 ==================== */
  .fab-container {
    position: fixed;
    bottom: 200rpx;
    right: 50rpx;
    display: flex;
    flex-direction: column;
    gap: 24rpx;
    z-index: 999;
  }

  .fab-btn {
    width: 100rpx;
    height: 100rpx;
    border-radius: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: visible;

    /* iOS 26 Liquid Glass 核心特性 - 高通透度 */
    background: linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.65) 0%,
      rgba(255, 255, 255, 0.55) 50%,
      rgba(255, 255, 255, 0.6) 100%
    );
    backdrop-filter: blur(50rpx) saturate(180%);
    -webkit-backdrop-filter: blur(50rpx) saturate(180%);

    /* 多层次阴影 - 模拟真实玻璃的深度 */
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04), 0 8rpx 24rpx rgba(0, 0, 0, 0.08),
      0 16rpx 48rpx rgba(0, 0, 0, 0.12), inset 0 1rpx 2rpx rgba(255, 255, 255, 0.9),
      inset 0 -1rpx 1rpx rgba(0, 0, 0, 0.02);

    /* 边框 - 液态玻璃边缘 */
    border: 0.5rpx solid rgba(255, 255, 255, 0.8);

    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);

    /* 镜面高光层 - iOS 26 特色 */
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 56rpx;
      background: linear-gradient(
        135deg,
        rgba(255, 255, 255, 0.95) 0%,
        rgba(255, 255, 255, 0.5) 30%,
        transparent 60%,
        rgba(255, 255, 255, 0.3) 100%
      );
      pointer-events: none;
      z-index: 1;
    }

    /* 折射光晕层 - 动态光效 */
    &::after {
      content: '';
      position: absolute;
      top: 8rpx;
      left: 8rpx;
      right: 8rpx;
      bottom: 8rpx;
      border-radius: 48rpx;
      background: radial-gradient(
        circle at 30% 30%,
        rgba(255, 255, 255, 0.6) 0%,
        rgba(255, 255, 255, 0.2) 30%,
        transparent 60%
      );
      pointer-events: none;
      z-index: 1;
      animation: liquidShimmer 3s ease-in-out infinite;
    }

    /* 按下效果 - 液态响应 */
    &:active {
      transform: scale(0.92);
      box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.08), 0 4rpx 12rpx rgba(0, 0, 0, 0.12),
        0 8rpx 24rpx rgba(0, 0, 0, 0.16), inset 0 1rpx 2rpx rgba(255, 255, 255, 0.8),
        inset 0 -1rpx 1rpx rgba(0, 0, 0, 0.04);

      &::before {
        opacity: 0.8;
      }
    }

    .fab-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      position: relative;
      z-index: 2;

      text {
        font-size: 44rpx;
        color: #1d1d1f;
        filter: drop-shadow(0 1rpx 2rpx rgba(255, 255, 255, 0.8));
        transition: all 0.3s ease;
      }
    }

    /* 发布按钮 - 纯透明液态玻璃 */
    &.fab-publish {
      .fab-icon text {
        color: #ffffff;
        filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.25));
      }
    }

    /* 互动按钮 - 纯透明液态玻璃 */
    &.fab-interaction {
      .fab-icon text {
        color: #ff2d55;
        filter: drop-shadow(0 1rpx 2rpx rgba(255, 45, 85, 0.3));
      }
    }
  }

  /* iOS 26 Liquid Glass 液态波光动画 */
  @keyframes liquidShimmer {
    0%,
    100% {
      transform: translate(0, 0) scale(1);
      opacity: 0.6;
    }
    33% {
      transform: translate(2rpx, -2rpx) scale(1.05);
      opacity: 0.8;
    }
    66% {
      transform: translate(-2rpx, 2rpx) scale(0.95);
      opacity: 0.4;
    }
  }
</style>
