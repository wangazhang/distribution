<template>
  <s-layout title="我的社群" :back="true">
    <view class="page-container">
      <!-- 搜索框 -->
      <view class="search-box">
        <uni-icons type="search" size="16" color="#999"></uni-icons>
        <input class="search-input" type="text" placeholder="会员昵称 昵称/会员编号/手机号" />
      </view>

      <!-- 数据统计卡片 -->
      <view class="stats-cards">
        <!-- 我的社群卡片 -->
        <view class="stats-card community-card">
          <view class="stats-number">3</view>
          <view class="stats-title">
            <text>我的社群</text>
            <uni-icons type="help" size="14" color="#fff"></uni-icons>
          </view>
          <view class="stats-info">今日新增: 0</view>
          <view class="stats-info">本月新增: 2</view>
        </view>

        <!-- 我的代理卡片 -->
        <view class="stats-card agent-card">
          <view class="stats-number">0</view>
          <view class="stats-title">
            <text>我的代理</text>
            <uni-icons type="help" size="14" color="#fff"></uni-icons>
          </view>
          <view class="stats-info">今日新增: 0</view>
          <view class="stats-info">本月新增: 0</view>
        </view>
      </view>

      <!-- 标签栏 -->
      <view class="tab-container">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'community' }"
          @tap="switchTab('community')"
        >
          <text>我的社群</text>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'agent' }"
          @tap="switchTab('agent')"
        >
          <text>我的代理</text>
        </view>
        <view class="filter-container">
          <text>人数: {{ activeTab === 'community' ? communityStats.total : agentStats.total }}人</text>
          <view class="filter-btn" @tap="showFilterPopup">
            <text>筛选</text>
            <uni-icons type="filter" size="16" color="#666"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 用户列表 -->
      <view class="user-list" v-if="activeTab === 'community'">
        <!-- 用户1 -->
        <view class="user-card">
          <view class="user-info">
            <image class="user-avatar" src="https://cdn.example.com/static/pic/avatar/avatar_2.png"></image>
            <view class="user-details">
              <view class="user-name">微信用户515🔲-游客(515)</view>
              <view class="user-contact">
                <text>手机: </text>
                <text>邀请人:王阿章</text>
              </view>
            </view>
          </view>

          <view class="user-stats">
            <view class="stat-item">
              <text class="stat-label">消费额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">直接营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">社群人数</text>
              <text class="stat-value">0</text>
            </view>
          </view>

          <view class="user-register-time">
            <text>注册时间: 2025-04-18 22:51:45</text>
          </view>
        </view>

        <!-- 用户2 -->
        <view class="user-card">
          <view class="user-info">
            <image class="user-avatar" src="https://cdn.example.com/static/pic/avatar/avatar_2.png"></image>
            <view class="user-details">
              <view class="user-name">微信用户444🔲-游客(444)</view>
              <view class="user-contact">
                <text>手机: </text>
                <text>邀请人:王阿章</text>
              </view>
            </view>
          </view>

          <view class="user-stats">
            <view class="stat-item">
              <text class="stat-label">消费额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">直接营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">社群人数</text>
              <text class="stat-value">0</text>
            </view>
          </view>

          <view class="user-register-time">
            <text>注册时间: 2025-04-01 22:06:24</text>
          </view>
        </view>

        <!-- 用户3 -->
        <view class="user-card">
          <view class="user-info">
            <image class="user-avatar" src="https://cdn.example.com/static/pic/avatar/avatar_2.png"></image>
            <view class="user-details">
              <view class="user-name">微信用户423🔲-游客(423)</view>
              <view class="user-contact">
                <text>手机: </text>
                <text>邀请人:王阿章</text>
              </view>
            </view>
          </view>

          <view class="user-stats">
            <view class="stat-item">
              <text class="stat-label">消费额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">直接营业额</text>
              <text class="stat-value">¥ 0</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">社群人数</text>
              <text class="stat-value">0</text>
            </view>
          </view>

          <view class="user-register-time">
            <text>注册时间: 2025-04-01 22:06:24</text>
          </view>
        </view>
      </view>
      
      <!-- 代理列表 -->
      <view class="user-list" v-if="activeTab === 'agent'">
        <view class="empty-data">
          <uni-icons type="info" size="40" color="#ddd"></uni-icons>
          <text>暂无代理数据</text>
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
              :class="{ active: tempSelectedFilter === 'all' }"
              @tap="selectFilter('all')"
            >
              全部
            </view>
            <view 
              class="filter-option" 
              :class="{ active: tempSelectedFilter === 'guest' }"
              @tap="selectFilter('guest')"
            >
              游客
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
  import { ref, onMounted } from 'vue';

  // 页面数据
  const communityStats = ref({
    total: 3,
    todayNew: 0,
    monthNew: 2
  });

  const agentStats = ref({
    total: 0,
    todayNew: 0,
    monthNew: 0
  });

  const communityMembers = ref([
    {
      id: '515',
      name: '微信用户515🔲-游客(515)',
      avatar: 'https://cdn.example.com/static/pic/avatar/avatar_2.png',
      phone: '',
      inviter: '王阿章',
      consumption: 0,
      revenue: 0,
      directRevenue: 0,
      communityCount: 0,
      registerTime: '2025-04-18 22:51:45'
    },
    {
      id: '444',
      name: '微信用户444🔲-游客(444)',
      avatar: 'https://cdn.example.com/static/pic/avatar/avatar_2.png',
      phone: '',
      inviter: '王阿章',
      consumption: 0,
      revenue: 0,
      directRevenue: 0,
      communityCount: 0,
      registerTime: '2025-04-01 22:06:24'
    },
    {
      id: '423',
      name: '微信用户423🔲-游客(423)',
      avatar: 'https://cdn.example.com/static/pic/avatar/avatar_2.png',
      phone: '',
      inviter: '王阿章',
      consumption: 0,
      revenue: 0,
      directRevenue: 0,
      communityCount: 0,
      registerTime: '2025-04-01 22:06:24'
    }
  ]);

  // 激活的标签
  const activeTab = ref('community');

  // 切换标签
  const switchTab = (tab) => {
    activeTab.value = tab;
  };

  // 筛选功能
  const showFilter = ref(false);
  const selectedFilter = ref('all');
  const tempSelectedFilter = ref('all');

  // 显示筛选弹窗
  const showFilterPopup = () => {
    tempSelectedFilter.value = selectedFilter.value;
    showFilter.value = true;
  };

  // 隐藏筛选弹窗
  const hideFilterPopup = () => {
    showFilter.value = false;
  };

  // 选择筛选选项
  const selectFilter = (filter) => {
    tempSelectedFilter.value = filter;
  };

  // 确认筛选
  const confirmFilter = () => {
    selectedFilter.value = tempSelectedFilter.value;
    hideFilterPopup();
    
    // 根据筛选条件过滤数据
    // 这里可以添加实际的筛选逻辑
    console.log('应用筛选条件:', selectedFilter.value);
  };

  // 初始化加载数据
  onMounted(() => {
    // 从API获取数据
    // fetchCommunityData();
  });

  // 获取社群数据
  const fetchCommunityData = async () => {
    try {
      // 这里添加API调用
      // const response = await getCommunityData();
      // communityStats.value = response.stats;
      // communityMembers.value = response.members;
    } catch (error) {
      console.error('获取社群数据失败', error);
    }
  };
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

  .tab-container {
    display: flex;
    background-color: #fff;
    border-radius: 10rpx;
    margin-bottom: 20rpx;
    overflow: hidden;

    .tab-item {
      padding: 20rpx 30rpx;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #333;
        font-weight: 500;

        &:after {
          content: '';
          position: absolute;
          left: 30rpx;
          right: 30rpx;
          bottom: 0;
          height: 4rpx;
          background-color: #333;
          border-radius: 2rpx;
        }
      }
    }

    .filter-container {
      display: flex;
      align-items: center;
      margin-left: auto;
      padding: 0 20rpx;
      font-size: 24rpx;
      color: #666;

      .filter-btn {
        display: flex;
        align-items: center;
        margin-left: 20rpx;

        text {
          margin-right: 5rpx;
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
          }

          .user-contact {
            font-size: 24rpx;
            color: #666;
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
          width: 220rpx;
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
