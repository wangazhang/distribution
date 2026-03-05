<template>
  <s-layout title="我的社群" :back="true">
    <view class="page-container">
      <!-- 搜索框 -->
      <view class="search-box">
        <uni-icons type="search" size="16" color="#999"></uni-icons>
        <input
          class="search-input"
          type="text"
          v-model="searchKeyword"
          placeholder="会员昵称 昵称/会员编号/手机号"
          @confirm="handleSearch"
        />
      </view>

      <!-- 数据统计卡片 -->
      <view class="stats-cards">
        <!-- 我的社群卡片 -->
        <view class="stats-card community-card">
          <view class="stats-number">{{ overviewStats.community.total }}</view>
          <view class="stats-title">
            <text>我的社群</text>
            <uni-icons type="help" size="14" color="#fff" @tap="showStatsHint('community')"></uni-icons>
          </view>
          <view class="stats-info">今日新增: {{ overviewStats.community.todayNew }}</view>
          <view class="stats-info">本月新增: {{ overviewStats.community.monthNew }}</view>
        </view>

        <!-- 我的代理卡片 -->
        <view class="stats-card agent-card">
          <view class="stats-number">{{ overviewStats.direct.total }}</view>
          <view class="stats-title">
            <text>我的代理</text>
            <uni-icons type="help" size="14" color="#fff" @tap="showStatsHint('direct')"></uni-icons>
          </view>
          <view class="stats-info">今日新增: {{ overviewStats.direct.todayNew }}</view>
          <view class="stats-info">本月新增: {{ overviewStats.direct.monthNew }}</view>
        </view>
      </view>

      <!-- 标签栏 - 使用su-tab组件 -->
      <view class="tab-section">
        <su-tab
          v-model="activeTabIndex"
          :tab="tabOptions"
          tpl="line"
          mark="short-line"
          @change="handleTabChange"
          :titleStyle="tabTitleStyle"
        ></su-tab>

        <view class="filter-container">
          <text>人数: {{ getActiveTabStats.total }}人</text>
          <view class="filter-btn" @tap="showFilterPopup">
            <text>筛选</text>
            <uni-icons type="filter" size="16" color="#666"></uni-icons>
            <view v-if="hasActiveFilter" class="filter-dot"></view>
          </view>
        </view>
      </view>

      <!-- 用户列表 -->
      <view class="user-list" v-if="activeTab === 'community'">
        <view v-if="communityMembers.length > 0">
          <view class="user-card" v-for="(member, index) in communityMembers" :key="index">
            <view class="user-info">
              <image class="user-avatar" :src="member.avatar || defaultAvatar"></image>
              <view class="user-details">
                <view class="user-name">
                  {{ member.nickname || '未命名' }}
                  <view v-if="member.levelName" class="level-tag">{{ member.levelName }}</view>
                </view>
                <view class="user-contact">
                  <text>手机:{{ member.phone || '未绑定' }}</text>
                  <text>邀请人:{{ member.inviter || '无' }}</text>
                </view>
              </view>
            </view>

            <view class="user-stats">
<!--              <view class="stat-item">-->
<!--                <text class="stat-label">消费额</text>-->
<!--                <text class="stat-value">¥ {{ (member.consumption / 100).toFixed(2) }}</text>-->
<!--              </view>-->
              <view class="stat-item">
                <text class="stat-label">佣金收益</text>
                <text class="stat-value">¥ {{ (member.revenue / 100).toFixed(2) }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">社群人数</text>
                <text class="stat-value">{{ member.communityCount }}</text>
              </view>
            </view>

            <view class="user-register-time">
              <text>注册时间: {{ formatDate(member.registerTime) }}</text>
            </view>
          </view>

          <!-- 加载更多 -->
          <uni-load-more
            :status="loadMoreStatus"
            v-if="communityMembers.length > 0"
          ></uni-load-more>
        </view>

        <!-- 空数据展示 -->
        <view class="empty-data" v-else>
          <uni-icons type="info" size="40" color="#ddd"></uni-icons>
          <text>暂无社群数据</text>
        </view>
      </view>

      <!-- 代理列表 -->
      <view class="user-list" v-if="activeTab === 'direct'">
        <view v-if="directAgentMembers.length > 0">
          <view class="user-card" v-for="(member, index) in directAgentMembers" :key="index">
            <view class="user-info">
              <image class="user-avatar" :src="member.avatar || defaultAvatar"></image>
              <view class="user-details">
                <view class="user-name">
                  {{ member.nickname || '未命名' }}
                  <view v-if="member.levelName" class="level-tag">{{ member.levelName }}</view>
                </view>
                <view class="user-contact">
                  <text>手机:{{ member.phone || '未绑定' }}</text>
                  <text>邀请人:{{ member.inviter || '无' }}</text>
                </view>
              </view>
            </view>

            <view class="user-stats">
              <view class="stat-item">
                <text class="stat-label">消费额</text>
                <text class="stat-value">¥ {{ (member.consumption / 100).toFixed(2) }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">佣金收益</text>
                <text class="stat-value">¥ {{ (member.revenue / 100).toFixed(2) }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">社群人数</text>
                <text class="stat-value">{{ member.communityCount }}</text>
              </view>
            </view>

            <view class="user-register-time">
              <text>注册时间: {{ formatDate(member.registerTime) }}</text>
            </view>
          </view>

          <!-- 加载更多 -->
          <uni-load-more
            :status="directLoadMoreStatus"
            v-if="directAgentMembers.length > 0"
          ></uni-load-more>
        </view>

        <view class="empty-data" v-else>
          <uni-icons type="info" size="40" color="#ddd"></uni-icons>
          <text>暂无直接代理数据</text>
        </view>
      </view>

      <!-- 间接代理列表 -->
      <view class="user-list" v-if="activeTab === 'indirect'">
        <view v-if="indirectAgentMembers.length > 0">
          <view class="user-card" v-for="(member, index) in indirectAgentMembers" :key="index">
            <view class="user-info">
              <image class="user-avatar" :src="member.avatar || defaultAvatar"></image>
              <view class="user-details">
                <view class="user-name">
                  {{ member.nickname || '未命名' }}
                  <view v-if="member.levelName" class="level-tag">{{ member.levelName }}</view>
                </view>
                <view class="user-contact">
                  <text>手机:{{ member.phone || '未绑定' }}</text>
                  <text>邀请人:{{ member.inviter || '无' }}</text>
                </view>
              </view>
            </view>

            <view class="user-stats">
              <view class="stat-item">
                <text class="stat-label">消费额</text>
                <text class="stat-value">¥ {{ (member.consumption / 100).toFixed(2) }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">佣金收益</text>
                <text class="stat-value">¥ {{ (member.revenue / 100).toFixed(2) }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-label">社群人数</text>
                <text class="stat-value">{{ member.communityCount }}</text>
              </view>
            </view>

            <view class="user-register-time">
              <text>注册时间: {{ formatDate(member.registerTime) }}</text>
            </view>
          </view>

          <!-- 加载更多 -->
          <uni-load-more
            :status="indirectLoadMoreStatus"
            v-if="indirectAgentMembers.length > 0"
          ></uni-load-more>
        </view>

        <view class="empty-data" v-else>
          <uni-icons type="info" size="40" color="#ddd"></uni-icons>
          <text>暂无间接代理数据</text>
        </view>
      </view>

      <!-- 筛选弹出框 -->
      <view class="filter-popup" v-if="showFilter" @touchmove.stop.prevent>
        <view class="filter-overlay" @click="hideFilterPopup"></view>
        <view class="filter-content">
          <view class="filter-title">等级筛选</view>
          <view class="filter-options">
            <view
              class="filter-option"
              v-for="option in levelOptions"
              :key="option.id"
              :class="{ active: tempSelectedLevelId === option.id }"
              @tap="selectFilter(option.id)"
            >
              {{ option.name }}
            </view>
          </view>
          <view class="filter-actions">
            <view class="filter-action cancel" @tap="hideFilterPopup">取 消</view>
            <view class="filter-action confirm" @tap="confirmFilter">确 定</view>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted, computed, reactive } from 'vue';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';

  // 添加时间格式化函数
  const formatDate = (dateString) => {
    if (!dateString) return '未知';
    // 如果后端返回的是已格式化的字符串，直接返回
    if (typeof dateString === 'string') return dateString;

    // 如果是Date对象或时间戳，进行格式化
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '未知';

    return (
      date.getFullYear() +
      '-' +
      padZero(date.getMonth() + 1) +
      '-' +
      padZero(date.getDate()) +
      ' ' +
      padZero(date.getHours()) +
      ':' +
      padZero(date.getMinutes())
    );
  };

  // 补零函数
  const padZero = (num) => {
    return num < 10 ? '0' + num : num;
  };

  // 默认头像
  const defaultAvatar =
    'https://cdn.example.com/static/pic/avatar/avatar_2.png';

  // 顶部总览数据
  const overviewStats = reactive({
    community: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
    direct: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
    indirect: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
  });

  // 各标签统计数据（受筛选影响）
  const tabStats = reactive({
    community: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
    direct: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
    indirect: {
      total: 0,
      todayNew: 0,
      monthNew: 0,
    },
  });

  const assignStats = (target, stats) => {
    Object.assign(target, stats || { total: 0, todayNew: 0, monthNew: 0 });
  };

  const statsHintMap = {
    community: '您团队里的所有成员',
    direct: '您直接邀请成为代理的成员',
  };

  const showStatsHint = (type) => {
    const content = statsHintMap[type];
    if (!content) return;
    uni.showModal({
      title: '提示',
      content,
      showCancel: false,
    });
  };

  // 列表数据
  const communityMembers = ref([]);
  const directAgentMembers = ref([]);
  const indirectAgentMembers = ref([]);

  const hasLoaded = reactive({
    community: false,
    direct: false,
    indirect: false,
  });

  const loadingFlags = reactive({
    community: false,
    direct: false,
    indirect: false,
  });

  const requestSerials = reactive({
    community: 0,
    direct: 0,
    indirect: 0,
  });

  // 分页参数
  const communityParams = reactive({
    pageNo: 1,
    pageSize: 10,
    nickname: '',
    level: 0, // 0表示全部
    type: 'community',
  });

  const directAgentParams = reactive({
    pageNo: 1,
    pageSize: 10,
    nickname: '',
    level: 0,
    type: 'direct',
  });

  const indirectAgentParams = reactive({
    pageNo: 1,
    pageSize: 10,
    nickname: '',
    level: 0,
    type: 'indirect',
  });

  // 加载状态
  const loadMoreStatus = ref('more');
  const directLoadMoreStatus = ref('more');
  const indirectLoadMoreStatus = ref('more');

  // 搜索关键词（输入框）
  const searchKeyword = ref('');
  // 已生效的搜索关键词
  const appliedKeyword = ref('');

  // Tab配置
  const tabOptions = [{ title: '我的社群' }, { title: '直接下级' }, { title: '间接下级' }];

  // 激活的标签索引
  const activeTabIndex = ref(0);

  // 激活的标签类型
  const activeTab = ref('community');

  // 根据标签索引切换标签类型
  const handleTabChange = (e) => {
    const tabMap = {
      0: 'community',
      1: 'direct',
      2: 'indirect',
    };
    const target = tabMap[e.index];
    activeTab.value = target;

    if (!hasLoaded[target] && !loadingFlags[target]) {
      loadCurrentTabData();
    }
  };

  // 获取当前激活标签的统计数据
  const getActiveTabStats = computed(() => {
    return tabStats[activeTab.value] || { total: 0, todayNew: 0, monthNew: 0 };
  });

  // 筛选功能
  const showFilter = ref(false);
  const levelOptions = ref([{ id: 0, name: '全部' }]);
  const selectedLevelId = ref(0);
  const tempSelectedLevelId = ref(0);
  const hasActiveFilter = computed(() => Number(selectedLevelId.value) !== 0);

  // 自定义Tab标题样式
  const tabTitleStyle = {
    activeBg: 'transparent',
    activeColor: '#000000',
    color: '#666666',
  };

  // 显示筛选弹窗
  const showFilterPopup = () => {
    tempSelectedLevelId.value = selectedLevelId.value;
    showFilter.value = true;
  };

  // 隐藏筛选弹窗
  const hideFilterPopup = () => {
    showFilter.value = false;
  };

  // 选择筛选选项
  const selectFilter = (levelId) => {
    tempSelectedLevelId.value = Number(levelId);
  };

  // 确认筛选
  const confirmFilter = async () => {
    selectedLevelId.value = tempSelectedLevelId.value;
    hideFilterPopup();

    // 根据筛选条件重置并重新加载数据
    try {
      await resetAndReloadData();
    } catch (error) {
      console.error('筛选刷新失败', error);
    }
  };

  // 搜索处理
  const handleSearch = async () => {
    appliedKeyword.value = searchKeyword.value;
    try {
      await resetAndReloadData();
    } catch (error) {
      console.error('搜索刷新失败', error);
    }
  };

  const applyFiltersToParams = (params) => {
    params.pageNo = 1;
    params.level = Number(selectedLevelId.value) || 0;
    params.nickname = appliedKeyword.value;
  };

  const resetAllListStates = () => {
    communityMembers.value = [];
    directAgentMembers.value = [];
    indirectAgentMembers.value = [];
    loadMoreStatus.value = 'more';
    directLoadMoreStatus.value = 'more';
    indirectLoadMoreStatus.value = 'more';
    hasLoaded.community = false;
    hasLoaded.direct = false;
    hasLoaded.indirect = false;
    loadingFlags.community = false;
    loadingFlags.direct = false;
    loadingFlags.indirect = false;
    requestSerials.community++;
    requestSerials.direct++;
    requestSerials.indirect++;
    assignStats(tabStats.community, null);
    assignStats(tabStats.direct, null);
    assignStats(tabStats.indirect, null);
  };

  // 获取团队总览
  const loadTeamOverview = async () => {
    try {
      const response = await BrokerageApi.getTeamOverview();
      if (response.code === 0 && response.data) {
        const { communityStats, directStats, indirectStats, levelOptions: options } = response.data;
        assignStats(overviewStats.community, communityStats);
        assignStats(overviewStats.direct, directStats);
        assignStats(overviewStats.indirect, indirectStats);

        const normalizedOptions = [];
        const seenIds = new Set();
        const pushOption = (option) => {
          if (!option) return;
          const id = Number(option.id ?? 0);
          if (Number.isNaN(id) || seenIds.has(id)) return;
          seenIds.add(id);
          normalizedOptions.push({ id, name: option.name || '未命名等级' });
        };

        // 始终包含“全部”
        pushOption({ id: 0, name: '全部' });
        if (Array.isArray(options)) {
          options.forEach((item) => pushOption(item));
        }
        levelOptions.value = normalizedOptions;

        // 当前选择如果不存在则回退到首个选项
        if (!levelOptions.value.some((item) => item.id === Number(selectedLevelId.value))) {
          selectedLevelId.value = levelOptions.value[0]?.id ?? 0;
        }
      }
    } catch (error) {
      console.error('获取团队总览失败', error);
    }
  };

  // 加载社群数据
  const loadCommunityData = async (options = {}) => {
    const { force = false } = options;
    if (loadingFlags.community && !force) return;

    const currentSerial = ++requestSerials.community;
    loadMoreStatus.value = 'loading';
    loadingFlags.community = true;

    try {
      const response = await BrokerageApi.getTeamMemberList({
        ...communityParams,
        type: 'community',
      });

      if (currentSerial !== requestSerials.community) {
        return;
      }

      if (response.code === 0 && response.data) {
        assignStats(tabStats.community, response.data.stats);

        if (communityParams.pageNo === 1) {
          communityMembers.value = response.data.list || [];
        } else {
          communityMembers.value = [...communityMembers.value, ...(response.data.list || [])];
        }

        if (response.data.list && response.data.list.length < communityParams.pageSize) {
          loadMoreStatus.value = 'noMore';
        } else {
          loadMoreStatus.value = 'more';
          communityParams.pageNo++;
        }
        hasLoaded.community = true;
      } else {
        loadMoreStatus.value = 'noMore';
      }
    } catch (error) {
      if (currentSerial === requestSerials.community) {
        loadMoreStatus.value = 'more';
      }
      console.error('获取社群数据失败', error);
    } finally {
      if (currentSerial === requestSerials.community) {
        loadingFlags.community = false;
      }
    }
  };

  // 加载直接代理数据
  const loadDirectAgentData = async (options = {}) => {
    const { force = false } = options;
    if (loadingFlags.direct && !force) return;

    const currentSerial = ++requestSerials.direct;
    directLoadMoreStatus.value = 'loading';
    loadingFlags.direct = true;

    try {
      const response = await BrokerageApi.getTeamMemberList({
        ...directAgentParams,
        type: 'direct',
      });

      if (currentSerial !== requestSerials.direct) {
        return;
      }

      if (response.code === 0 && response.data) {
        // 更新统计数据
        assignStats(tabStats.direct, response.data.stats);

        // 追加数据
        if (directAgentParams.pageNo === 1) {
          directAgentMembers.value = response.data.list || [];
        } else {
          directAgentMembers.value = [...directAgentMembers.value, ...(response.data.list || [])];
        }

        // 更新加载状态
        if (response.data.list && response.data.list.length < directAgentParams.pageSize) {
          directLoadMoreStatus.value = 'noMore';
        } else {
          directLoadMoreStatus.value = 'more';
          directAgentParams.pageNo++;
        }
        hasLoaded.direct = true;
      } else {
        directLoadMoreStatus.value = 'noMore';
      }
    } catch (error) {
      if (currentSerial === requestSerials.direct) {
        directLoadMoreStatus.value = 'more';
      }
      console.error('获取直接代理数据失败', error);
    } finally {
      if (currentSerial === requestSerials.direct) {
        loadingFlags.direct = false;
      }
    }
  };

  // 加载间接代理数据
  const loadIndirectAgentData = async (options = {}) => {
    const { force = false } = options;
    if (loadingFlags.indirect && !force) return;

    const currentSerial = ++requestSerials.indirect;
    indirectLoadMoreStatus.value = 'loading';
    loadingFlags.indirect = true;

    try {
      const response = await BrokerageApi.getTeamMemberList({
        ...indirectAgentParams,
        type: 'indirect',
      });

      if (currentSerial !== requestSerials.indirect) {
        return;
      }

      if (response.code === 0 && response.data) {
        // 更新统计数据
        assignStats(tabStats.indirect, response.data.stats);

        // 追加数据
        if (indirectAgentParams.pageNo === 1) {
          indirectAgentMembers.value = response.data.list || [];
        } else {
          indirectAgentMembers.value = [
            ...indirectAgentMembers.value,
            ...(response.data.list || []),
          ];
        }

        // 更新加载状态
        if (response.data.list && response.data.list.length < indirectAgentParams.pageSize) {
          indirectLoadMoreStatus.value = 'noMore';
        } else {
          indirectLoadMoreStatus.value = 'more';
          indirectAgentParams.pageNo++;
        }
        hasLoaded.indirect = true;
      } else {
        indirectLoadMoreStatus.value = 'noMore';
      }
    } catch (error) {
      if (currentSerial === requestSerials.indirect) {
        indirectLoadMoreStatus.value = 'more';
      }
      console.error('获取间接代理数据失败', error);
    } finally {
      if (currentSerial === requestSerials.indirect) {
        loadingFlags.indirect = false;
      }
    }
  };

  const loadCurrentTabData = async (options = {}) => {
    if (activeTab.value === 'community') {
      await loadCommunityData(options);
    } else if (activeTab.value === 'direct') {
      await loadDirectAgentData(options);
    } else if (activeTab.value === 'indirect') {
      await loadIndirectAgentData(options);
    }
  };

  // 重置并重新加载数据
  const resetAndReloadData = async ({ refreshOverview = false } = {}) => {
    applyFiltersToParams(communityParams);
    applyFiltersToParams(directAgentParams);
    applyFiltersToParams(indirectAgentParams);

    resetAllListStates();

    if (refreshOverview) {
      await loadTeamOverview();
    }

    await loadCurrentTabData({ force: true });
  };

  // 下拉刷新
  onPullDownRefresh(async () => {
    try {
      await resetAndReloadData({ refreshOverview: true });
    } catch (error) {
      console.error('下拉刷新失败', error);
    } finally {
      uni.stopPullDownRefresh();
    }
  });

  // 上拉加载更多
  onReachBottom(() => {
    if (activeTab.value === 'community' && loadMoreStatus.value === 'more') {
      loadCommunityData();
    } else if (activeTab.value === 'direct' && directLoadMoreStatus.value === 'more') {
      loadDirectAgentData();
    } else if (activeTab.value === 'indirect' && indirectLoadMoreStatus.value === 'more') {
      loadIndirectAgentData();
    }
  });

  // 初始化加载数据
  onMounted(async () => {
    try {
      appliedKeyword.value = searchKeyword.value;
      applyFiltersToParams(communityParams);
      applyFiltersToParams(directAgentParams);
      applyFiltersToParams(indirectAgentParams);
      await loadTeamOverview();
      await loadCurrentTabData();
    } catch (error) {
      console.error('初始化团队数据失败', error);
    }
  });
</script>

<style lang="scss" scoped>
  .page-container {
    padding: 30rpx;
    background-color: #f7f7f7;
    min-height: 100vh;
  }

  .search-box {
    display: flex;
    align-items: center;
    background-color: #fff;
    border-radius: 40rpx;
    padding: 20rpx 30rpx;
    margin-bottom: 30rpx;

    .search-input {
      flex: 1;
      height: 40rpx;
      padding-left: 15rpx;
      font-size: 26rpx;
      color: #333;
    }
  }

  .stats-cards {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .stats-card {
      width: 44%;
      border-radius: 16rpx;
      padding: 30rpx;
      color: #fff;
      margin: 0 10rpx;

      .stats-number {
        font-size: 60rpx;
        font-weight: bold;
        margin-bottom: 20rpx;
      }

      .stats-title {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        font-size: 32rpx;
        font-weight: 500;

        text {
          margin-right: 10rpx;
        }
      }

      .stats-info {
        font-size: 24rpx;
        margin-top: 10rpx;
        opacity: 0.8;
      }
    }

    .community-card {
      background-color: #f95356;
    }

    .agent-card {
      background-color: #4080ff;
    }
  }

  .tab-section {
    background-color: #fff;
    border-radius: 10rpx;
    margin-bottom: 20rpx;
    padding: 0 0 20rpx 0;
    position: relative;

    /* 自定义短线条样式 */
    :deep(.ui-tab.line .ui-tab-mark-warp) {
      .ui-tab-mark {
        width: 20px !important;
        margin: 0 auto;
        left: 0;
        right: 0;
      }
    }

    /* 减小标签栏高度 */
    :deep(.ui-tab) {
      height: 80rpx !important;

      .ui-tab-item {
        min-height: 80rpx;
        line-height: 80rpx;
      }
    }

    .filter-container {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 20rpx 30rpx 0;
      font-size: 24rpx;
      color: #666;

      .filter-btn {
        display: flex;
        align-items: center;
        margin-left: 20rpx;
        position: relative;

        text {
          margin-right: 5rpx;
        }

        .filter-dot {
          position: absolute;
          top: 4rpx;
          right: -4rpx;
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          background-color: #ff4d4f;
        }
      }
    }
  }

  .user-list {
    .user-card {
      background-color: #fff;
      border-radius: 10rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;

      .user-info {
        display: flex;
        margin-bottom: 20rpx;

        .user-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 50%;
          margin-right: 20rpx;
        }

        .user-details {
          flex: 1;

          .user-name {
            font-size: 28rpx;
            font-weight: 500;
            color: #333;
            margin-bottom: 10rpx;
            display: flex;
            align-items: center;
            gap: 10rpx;

            .level-tag {
              font-size: 20rpx;
              color: #ff7d00;
              background: rgba(255, 125, 0, 0.12);
              border-radius: 24rpx;
              padding: 4rpx 12rpx;
            }
          }

          .user-contact {
            font-size: 24rpx;
            color: #666;

            text {
              margin-right: 20rpx;

              &:last-child {
                margin-right: 0;
              }
            }
          }
        }
      }

      .user-stats {
        display: flex;
        border-top: 1px solid #f2f2f2;
        border-bottom: 1px solid #f2f2f2;
        padding: 20rpx 0;
        margin-bottom: 20rpx;

        .stat-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          .stat-label {
            font-size: 24rpx;
            color: #666;
            margin-bottom: 10rpx;
          }

          .stat-value {
            font-size: 26rpx;
            color: #333;
          }
        }
      }

      .user-register-time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  /* 筛选弹出框样式 */
  .filter-popup {
    position: fixed;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    z-index: 100;

    .filter-overlay {
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
    }

    .filter-content {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: #fff;
      border-top-left-radius: 20rpx;
      border-top-right-radius: 20rpx;
      overflow: hidden;

      .filter-title {
        padding: 30rpx;
        text-align: center;
        font-size: 30rpx;
        font-weight: 500;
        color: #333;
        border-bottom: 1px solid #f0f0f0;
      }

      .filter-options {
        padding: 30rpx;
        display: flex;
        flex-wrap: wrap;

        .filter-option {
          width: 200rpx;
          height: 80rpx;
          border-radius: 40rpx;
          background-color: #f5f5f5;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 28rpx;
          color: #333;
          margin-right: 30rpx;
          margin-bottom: 20rpx;

          &.active {
            background-color: #f95356;
            color: #fff;
          }
        }
      }

      .filter-actions {
        display: flex;
        border-top: 1px solid #f0f0f0;
        padding-bottom: constant(safe-area-inset-bottom); /* iOS 11.0 */
        padding-bottom: env(safe-area-inset-bottom); /* iOS 11.2+ */

        .filter-action {
          flex: 1;
          height: 100rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 30rpx;

          &.cancel {
            background-color: #f5f5f5;
            color: #666;
          }

          &.confirm {
            background-color: #f95356;
            color: #fff;
          }
        }
      }
    }
  }

  .empty-data {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #fff;
    border-radius: 10rpx;
    padding: 60rpx 30rpx;
    margin-bottom: 20rpx;

    text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }
  }
</style>
