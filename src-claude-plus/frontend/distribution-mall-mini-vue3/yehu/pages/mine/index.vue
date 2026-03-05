<template>
  <s-layout navbar="mine" tabbar="/yehu/pages/mine/index" >
    <view class="page-container">
      <!-- 自定义透明顶部导航栏 -->
      <!--    <view class="custom-navbar transparent" :style="{ paddingTop: statusBarHeight + 'px' }">-->
      <!--      <view class="navbar-content">-->
      <!--        &lt;!&ndash; <view class="navbar-home"> &ndash;&gt;-->
      <!--        &lt;!&ndash; <uni-icons type="home" size="18" color="#fff" @click="navigateToHome"></uni-icons> &ndash;&gt;-->
      <!--        &lt;!&ndash; </view> &ndash;&gt;-->
      <!--        <view class="navbar-actions">-->
      <!--          &lt;!&ndash; 预留空间保持导航栏平衡 &ndash;&gt;-->
      <!--        </view>-->
      <!--      </view>-->
      <!--    </view>-->

      <!-- 页面内容区域 -->
      <view class="page-content">
        <!-- 加载状态 - 只在首次加载或无数据时显示 -->
        <view v-if="loading && !hasData" class="loading-container">
          <uni-load-more status="loading" :contentText="loadingText"></uni-load-more>
<!--          <text class="loading-text">数据加载中...</text>-->
        </view>

        <!-- 错误状态 - 只在无数据时显示 -->
        <view v-else-if="error && !hasData" class="error-container">
          <uni-icons type="error" size="30" color="#FF5777"></uni-icons>
          <text class="error-text">{{ errorMessage }}</text>
          <view class="retry-btn" @tap="fetchUserData">
            <text>重新加载</text>
          </view>
        </view>

        <!-- 未登录状态 -->
        <view v-else-if="!isLogin" class="not-login-container">
          <image
            class="login-img"
            src="https://cdn.example.com/static/pic/avatar/avatar_2.png"
            mode="aspectFill"
          ></image>
          <text class="login-tips">登录后查看您的个人信息</text>
          <view class="login-btn" @tap="showLogin">
            <text>去登录</text>
          </view>
        </view>

        <!-- 正常内容 -->
        <block v-else>
          <!-- 静默刷新指示器 -->
          <view v-if="silentLoading" class="silent-loading-indicator">
            <uni-icons type="reload" size="16" color="#999"></uni-icons>
            <text class="silent-loading-text">更新中...</text>
          </view>
          <!-- 用户信息区块(包含用户信息和余额卡片) -->
          <view class="user-section">
            <!-- 用户信息 -->
            <view class="user-profile">
              <view class="user-info">
                <view class="user-avatar-wrapper" @tap="showMpAuthorization">
                  <image
                    class="user-avatar"
                    :src="
                      userInfo.avatar ||
                      'https://cdn.example.com/static/pic/avatar/avatar_2.png'
                    "
                    mode="aspectFill"
                  ></image>
                </view>
                <view class="user-detail">
                  <view class="user-name-row">
                    <text class="user-name">{{ userInfo.nickname || '游客' }}</text>
                    <view class="user-tag">
                      <uni-icons type="checkmark-filled" size="12" color="#FFA500"></uni-icons>
                      <text>{{ userInfo.levelName || '游客' }}</text>
                    </view>
                  </view>
                  <view class="user-id">会员编号: {{ userInfo.id || '-' }}</view>
                </view>
              </view>

              <!-- 余额信息 -->
              <view class="balance-card">
                <view class="balance-item" @tap="navigateTo('/yehu/pages/mine/prepaid-card/index')">
                  <text class="balance-value">{{ userInfo.points || 0 }}</text>
                  <text class="balance-label">储值余额(元)</text>
                </view>
                <view class="balance-divider"></view>
                <view class="balance-item" @tap="navigateTo('/pages/user/wallet/money')">
                  <!--                <view class="balance-item" @tap="navigateTo('/yehu/pages/mine/account/index')">-->
                  <text class="balance-value">{{ fen2yuan(payWalletInfo.balance) }}</text>
                  <text class="balance-label">账户余额(元)</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 订单区域 -->
          <view class="order-card">
            <view class="order-header">
              <text class="order-title">我的订单</text>
              <view class="order-more" @tap="navigateTo('/pages/order/list')">
                <text>全部订单</text>
                <uni-icons type="right" size="12" color="#666"></uni-icons>
              </view>
            </view>

            <view class="order-menu">
              <view class="order-item" @tap="navigateToOrderList(1)">
                <view class="order-icon">
                  <uni-icons type="wallet" size="30" color="#333"></uni-icons>
                  <view v-if="userStats.waitPay > 0" class="order-badge"
                    >{{ userStats.waitPay }}
                  </view>
                </view>
                <text class="order-label">待付款</text>
              </view>
              <view class="order-item" @tap="navigateToOrderList(2)">
                <view class="order-icon">
                  <uni-icons type="cart" size="30" color="#333"></uni-icons>
                  <view v-if="userStats.waitShip > 0" class="order-badge"
                    >{{ userStats.waitShip }}
                  </view>
                </view>
                <text class="order-label">待发货</text>
              </view>
              <view class="order-item" @tap="navigateToOrderList(3)">
                <view class="order-icon">
                  <uni-icons type="notification" size="30" color="#333"></uni-icons>
                  <view v-if="userStats.waitReceive > 0" class="order-badge"
                    >{{ userStats.waitReceive }}
                  </view>
                </view>
                <text class="order-label">待收货</text>
              </view>
              <view class="order-item" @tap="navigateToAfterSaleList()">
                <view class="order-icon">
                  <uni-icons type="undo" size="30" color="#333"></uni-icons>
                  <view v-if="userStats.afterSaleCount > 0" class="order-badge"
                    >{{ userStats.afterSaleCount }}
                  </view>
                </view>
                <text class="order-label">退款/售后</text>
              </view>
            </view>
          </view>



          <!-- 元气理事专区 -->
          <view class="special-area">
            <swiper
              class="special-swiper"
              circular
              autoplay
              interval="3000"
              duration="500"
              indicator-dots
              indicator-active-color="#fff"
              indicator-color="rgba(255, 255, 255, 0.5)"
              @change="handleSwiperChange"
            >
              <swiper-item v-for="(item, index) in specialAreaList" :key="index">
                <view class="special-content" @tap="navigateToSpecialArea(item.url)">
                  <view class="special-info">
                    <text class="special-title">{{ item.title }}</text>
                    <text class="special-subtitle">{{ item.subtitle }}</text>
                  </view>
                  <image class="special-image" :src="item.image" mode="aspectFill"></image>
                </view>
              </swiper-item>
            </swiper>
          </view>


          <!-- 事业区域 -->
          <view class="function-section"  v-show="brokerageUser.brokerageEnabled">
            <view class="section-title">我的事业</view>
            <view class="function-grid"  >
              <view class="function-item" @tap="navigateTo('/pages/commission/index')">
                <view class="function-icon">
                  <uni-icons type="vip" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">我的团队</text>
              </view>
              <view class="function-item" @tap="navigateTo('/yehu/pages/mine/material/index')">
                <view class="function-icon">
                  <uni-icons type="bars" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">我的物料</text>
              </view>
              <view class="function-item" @tap="navigateTo('/yehu/pages/mine/material/outbound/list')">
                <view class="function-icon">
                  <uni-icons type="upload" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">出库管理</text>
              </view>
              <view class="function-item" @tap="navigateTo('/pages/commission/goods')">
                <view class="function-icon">
                  <uni-icons type="gift" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">商品分享</text>
              </view>
              <view class="function-item" @tap="navigateTo('/yehu/pages/mine/mb-order/index')">
                <view class="function-icon">
                  <uni-icons type="paperplane" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">订单中心</text>
              </view>
            </view>
          </view>
          <!-- 我的收益区域 -->
<!--          <view class="income-card" @tap="navigateTo('/pages/commission/index')">-->
<!--            <view class="income-header">-->
<!--              <text class="income-title">我的收益</text>-->
<!--              <view-->
<!--                class="income-more"-->
<!--                @tap="navigateTo('/yehu/pages/mine/management/earnings/index')"-->
<!--              >-->
<!--                <uni-icons type="right" size="12" color="#666"></uni-icons>-->
<!--              </view>-->
<!--            </view>-->
<!--            <view class="income-content">-->
<!--              <view class="income-item">-->
<!--                <text class="income-value">{{ earningsData.totalEarnings }}</text>-->
<!--                <text class="income-label">累计收入</text>-->
<!--              </view>-->
<!--              <view class="income-item">-->
<!--                <text class="income-value">{{ earningsData.yesterdayPrice }}</text>-->
<!--                <text class="income-label">昨日新增</text>-->
<!--              </view>-->
<!--              <view class="income-item">-->
<!--                <text class="income-value">{{ earningsData.monthEarnings }}</text>-->
<!--                <text class="income-label">本月新增</text>-->
<!--              </view>-->
<!--            </view>-->
<!--          </view>-->
          <!-- 我的社群区域 -->
<!--          <view class="community-card"  @tap="navigateTo('/pages/commission/team')"-->
<!--          >-->
<!--            <view class="community-header">-->
<!--              <text class="community-title">我的社群</text>-->
<!--              <view-->
<!--                class="community-more"  @tap="navigateTo('/yehu/pages/mine/management/community/index')"-->
<!--              >-->
<!--                <uni-icons type="right" size="12" color="#666"></uni-icons>-->
<!--              </view>-->
<!--            </view>-->
<!--            <view class="community-content">-->
<!--              <view class="community-item">-->
<!--                <text class="community-value">{{ communityData.totalUsers }}</text>-->
<!--                <text class="community-label">累计人群</text>-->
<!--              </view>-->
<!--              <view class="community-item">-->
<!--                <text class="community-value">{{ communityData.todayUsers }}</text>-->
<!--                <text class="community-label">今日新增</text>-->
<!--              </view>-->
<!--              <view class="community-item">-->
<!--                <text class="community-value">{{ communityData.monthUsers }}</text>-->
<!--                <text class="community-label">本月新增</text>-->
<!--              </view>-->
<!--            </view>-->
<!--          </view>-->


          <!-- 我的应用 -->
          <view class="function-section">
            <view class="section-title">我的应用</view>
            <view class="function-grid"  >
              <view class="function-item" @tap="navigateTo('/pages/user/address/list')">
                <view class="function-icon">
                  <uni-icons type="location" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">收货地址</text>
              </view>
              <view class="function-item" @tap="navigateTo('/yehu/pages/mine/message/index')">
                <view class="function-icon">
                  <uni-icons type="chat" size="30" color="#333"></uni-icons>
                  <view v-if="userStats.notifications > 0" class="function-badge"
                    >{{ userStats.notifications }}
                  </view>
                </view>
                <text class="function-name">消息中心</text>
              </view>
              <view class="function-item" @tap="navigateTo('/pages/chat/index')">
                <view class="function-icon">
                  <uni-icons type="chatboxes" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">客服</text>
              </view>
              <view class="function-item" @tap="navigateTo('/pages/index/cart')">
                <view class="function-icon">
                  <uni-icons type="cart" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">购物车</text>
              </view>
            </view>
          </view>

          <!-- 更多服务 -->
          <view class="function-section">
            <view class="section-title">更多服务</view>
            <view class="function-grid">
<!--              <view class="function-item" @tap="navigateTo('/yehu/pages/mine/management/index')">-->
<!--                <view class="function-icon">-->
<!--                  <uni-icons type="person" size="30" color="#333"></uni-icons>-->
<!--                </view>-->
<!--                <text class="function-name">中台管理</text>-->
<!--              </view>-->
<!--              <view class="function-item" @tap="navigateTo('/yehu/pages/activity/index')">-->
<!--                <view class="function-icon">-->
<!--                  <uni-icons type="star" size="30" color="#333"></uni-icons>-->
<!--                </view>-->
<!--                <text class="function-name">活动中心</text>-->
<!--              </view>-->
              <view class="function-item" @tap="showShareModal">
                <view class="function-icon">
                  <uni-icons type="image" size="30" color="#333"></uni-icons>
                </view>
                <text class="function-name">我的海报</text>
              </view>
              <!-- 安全退出图标 -->
              <view class="function-item" v-if="isLogin" @tap="onLogout">
                <view class="function-icon">
                  <uni-icons type="locked" size="30" color="#e54d42"></uni-icons>
                </view>
                <text class="function-name">安全退出</text>
              </view>

            </view>
          </view>

          <!-- 品牌信息 -->
          <view class="brand-info">
            <!--        <image class="brand-logo" src="https://cdn.example.com/static/pic/logo/logo.png" mode="aspectFit"></image>-->
            <text class="brand-text">赛媄拓生物科技自研技术支持</text>
          </view>

        </block>
      </view>

      <!-- 自定义底部导航栏 -->
      <!-- <yh-tabbar activeColor="#FFA0B4" color="#999999"></yh-tabbar> -->
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted, computed, watch } from 'vue';
  import {
    getUserInfo,
    getUserStats,
    getOrderList,
    getEarningsData,
    getPayWalletInfo,
    getBrokerageUser,
  } from '@/yehu/api/mine';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import { showShareModal } from '@/sheep/hooks/useModal';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
  import brokerage from '@/sheep/api/trade/brokerage';

  // 状态栏高度
  const statusBarHeight = ref(0);

  // 用户数据
  const userInfo = ref({});
  const brokerageUser = ref({ brokerageEnabled: false });
  const payWalletInfo = ref({});
  const userStats = ref({});
  const orderList = ref([]);
  // 收益和社群数据
  const earningsData = ref({
    totalEarnings: 1,
    yesterdayPrice: 2,
    monthEarnings: 0,
  });
  const communityData = ref({
    totalUsers: 0,
    todayUsers: 0,
    monthUsers: 0,
  });

  // 加载状态
  const loading = ref(true);
  const silentLoading = ref(false); // 静默加载状态，不影响页面显示
  const error = ref(false);
  const errorMessage = ref('数据加载失败，请重试');
  const hasData = ref(false); // 是否已有数据

  // 登录状态
  const isLogin = computed(() => sheep.$store('user').isLogin);

  // 退出登录
  const onLogout = () => {
    uni.showModal({
      title: '确认退出',
      content: '确定要退出当前账号吗？',
      confirmText: '安全退出',
      cancelText: '取消',
      success: async (res) => {
        if (res.confirm) {
          try {
            await sheep.$store('user').logout();
            uni.showToast({ title: '已退出', icon: 'success', duration: 1200 });
            setTimeout(() => {
              uni.reLaunch({ url: '/pages/index/index' });
            }, 300);
          } catch (e) {
            uni.showToast({ title: '退出失败，请重试', icon: 'none' });
          }
        }
      },
    });
  };

  // loading文本配置
  const loadingText = {
    contentdown: '上拉显示更多',
    contentrefresh: '正在加载...',
    contentnomore: '没有更多数据了',
  };

  // 获取用户数据
  const fetchUserData = async (silent = false) => {
    // 如果是静默刷新，不显示loading状态
    if (silent) {
      silentLoading.value = true;
    } else {
      loading.value = true;
    }
    error.value = false;

    try {
      // 检查登录状态
      if (!isLogin.value) {
        loading.value = false;
        silentLoading.value = false;
        return;
      }

      // 并行请求数据
      const [
        userInfoData,
        payWalletData,
        userStatsData,
        orderListData,
        earningsResult,
        brokerageUserData,
      ] = await Promise.all([
        getUserInfo(),
        getPayWalletInfo(),
        getUserStats(),
        getOrderList({ pageNo: 1, pageSize: 5 }),
        getEarningsData(),
        getBrokerageUser(),
      ]);

      // 处理用户信息
      userInfo.value = userInfoData.data;
      if (!userInfo.value.levelName) {
        userInfo.value.levelName = userInfo.value.level ? userInfo.value.level.name : '游客';
      }
      // 处理用户信息
      payWalletInfo.value = payWalletData.data;
      console.log('******payWalletInfo', payWalletInfo.value);
      if (!payWalletInfo.value) {
        payWalletInfo.value.balance = payWalletInfo.value.balance
          ? payWalletInfo.value.balance
          : '0';
      }

      // 处理订单统计
      userStats.value = {
        waitPay: userStatsData.data.unpaidCount || 0,
        waitShip: userStatsData.data.undeliveredCount || 0,
        waitReceive: userStatsData.data.deliveredCount || 0,
        waitComment: userStatsData.data.uncommentedCount || 0,
        afterSaleCount: userStatsData.data.afterSaleCount || 0,
        notifications: 0, // 默认无消息
      };

      // 处理订单列表
      if (orderListData && orderListData.list) {
        orderList.value = orderListData.list;
      } else {
        orderList.value = [];
      }

      if (brokerageUserData && brokerageUserData.data && brokerageUserData.data.brokerageEnabled) {
        console.log('******brokerageUser', brokerageUser);
        brokerageUser.value = brokerageUserData.data;
        // 处理收益数据
        if (earningsResult) {
          earningsData.value = {
            totalEarnings: fen2yuan(earningsResult.data.brokeragePrice || 0),
            yesterdayPrice: fen2yuan(earningsResult.data.yesterdayPrice || 0),
            monthEarnings: fen2yuan(earningsResult.monthPrice || 0),
          };
        }
        //
        // // 处理社群数据
        // if (communityResult) {
        //   communityData.value = {
        //     totalUsers: communityResult.totalCount || 0,
        //     todayUsers: communityResult.todayCount || 0,
        //     monthUsers: communityResult.monthCount || 0,
        //   };
        // }
      }

      console.log('用户数据加载完成', userInfo.value, userStats.value);
      hasData.value = true; // 标记已有数据
    } catch (err) {
      console.error('获取用户数据失败', err);
      // 如果已有数据，静默处理错误，不影响用户体验
      if (!hasData.value) {
        error.value = true;
        errorMessage.value = err.message || '数据加载失败，请重试';
      } else {
        // 已有数据时，只在控制台显示错误，不影响页面显示
        console.warn('数据刷新失败，保持当前数据显示', err);
      }
    } finally {
      loading.value = false;
      silentLoading.value = false;
      uni.stopPullDownRefresh();
    }
  };

  // 显示登录弹窗
  const showLogin = () => {
    showAuthModal('accountLogin');
  };

  // 显示微信授权弹窗
  const showMpAuthorization = () => {
    showAuthModal('mpAuthorization');
  };

  // 页面跳转
  const navigateTo = (url) => {
    // 检查是否需要登录
    if (!isLogin.value) {
      showLogin();
      return;
    }
    uni.navigateTo({ url });
  };

  // 跳转到订单列表页面，根据状态参数
  async function navigateToOrderList(status) {
    // 检查是否需要登录
    if (!isLogin.value) {
      showLogin();
      return;
    }
    console.log('before 跳转到订单列表页面，根据状态参数', status);
    sheep.$router.go('/pages/order/list', { type: status });
    console.log('after 跳转到订单列表页面，根据状态参数', status);
  }
  // 跳转到订单列表页面，根据状态参数
  async function navigateToAfterSaleList() {
    // 检查是否需要登录
    if (!isLogin.value) {
      showLogin();
      return;
    }
    sheep.$router.go('/pages/order/aftersale/list');
  }
  // 跳转到首页
  const navigateToHome = () => {
    uni.navigateTo({
      url: '/pages/index/index',
    });
  };

  // 跳转到元气理事专区，使用新的函数
  const navigateToSpecialArea = (url) => {
    // 检查是否需要登录
    if (!isLogin.value) {
      showLogin();
      return;
    }
    uni.navigateTo({
      url,
    });
  };

  // 轮播图数据
  const specialAreaList = ref([
    {
      title: '觅她元气理事专区',
      subtitle: 'MEETHER',
      image: 'https://cdn.example.com/static/pic/banner/index/1.png',
      url: '/yehu/pages/mine/special-area/index',
    },
    {
      title: '元气理事专属福利',
      subtitle: 'MEETHER BENEFITS',
      image: 'https://cdn.example.com/static/pic/banner/index/2.png',
      url: '/yehu/pages/mine/benefits/index',
    },
  ]);

  // 处理轮播切换事件
  const handleSwiperChange = (e) => {};

  onMounted(() => {
    // 获取状态栏高度
    uni.getSystemInfo({
      success: (res) => {
        statusBarHeight.value = res.statusBarHeight || 0;
      },
    });

    // 如果未登录，弹出登录框
    if (!isLogin.value) {
      showLogin();
    }

    // 获取用户数据
    fetchUserData();
  });

  // 监听登录状态变化，当登录状态改变时重新获取数据
  watch(isLogin, (newVal, oldVal) => {
    if (newVal && !oldVal) {
      // 用户从未登录变为已登录，重新获取数据
      console.log('登录状态变化，重新获取数据');
      hasData.value = false; // 重置数据状态
      fetchUserData(false); // 正常加载
    } else if (!newVal && oldVal) {
      // 用户从已登录变为未登录，清空数据
      console.log('用户退出登录，清空数据');
      hasData.value = false;
      loading.value = false;
      silentLoading.value = false;
    }
  });

  // 页面每次显示时触发
  onShow(() => {
    uni.hideTabBar();
    console.log('页面显示 (onShow)');
    // 如果用户已登录，则刷新数据，确保登录弹窗关闭后数据是最新的
    if (isLogin.value) {
      console.log('用户已登录，在 onShow 中刷新数据');
      // 如果已有数据，使用静默刷新，避免白屏
      if (hasData.value) {
        console.log('已有数据，使用静默刷新');
        fetchUserData(true); // 静默刷新
      } else {
        console.log('首次加载数据');
        fetchUserData(false); // 正常加载
      }
    }
    // 如果未登录，也可以选择在这里再次触发 showLogin()，确保未登录状态下总会弹出登录框
    // else {
    //   showLogin();
    // }
  });

  // 处理下拉刷新
  onPullDownRefresh(async () => {
    console.log('触发下拉刷新');
    // 重置brokerageUser状态，确保每次刷新时都重新获取
    brokerageUser.value = { brokerageEnabled: false };
    // 下拉刷新使用静默刷新，保持页面内容显示
    await fetchUserData(hasData.value); // 如果已有数据则静默刷新
  });
</script>

<style lang="scss" scoped>
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
    position: relative;
  }

  .custom-navbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10;

    &.transparent {
      background-color: transparent;
    }

    .navbar-content {
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 30rpx;

      .navbar-home {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .navbar-actions {
        width: 36px;
      }
    }
  }

  .page-content {
    position: relative;
    z-index: 5;

    .user-section {
      position: relative;
      padding-top: calc(44px + var(--status-bar-height));
      padding-bottom: 20rpx;
      background-image: url('https://cdn.example.com/static/pic/bg/mine_bg_1.png');
      background-size: cover;
      background-position: center;

      /* 添加渐变覆盖层 */
      &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.4));
        z-index: 1;
      }

      .user-profile {
        position: relative;
        padding-bottom: 20rpx;
        z-index: 2; /* 确保内容在渐变覆盖层之上 */

        .user-info {
          display: flex;
          padding: 80rpx 30rpx 0;
          position: relative;

          .user-avatar-wrapper {
            width: 120rpx;
            height: 120rpx;
            border-radius: 50%;
            border: 4rpx solid #fff;
            overflow: hidden;
            margin-right: 20rpx;

            .user-avatar {
              width: 100%;
              height: 100%;
            }
          }

          .user-detail {
            margin-top: 20rpx;

            .user-name-row {
              display: flex;
              align-items: center;
              margin-bottom: 10rpx;

              .user-name {
                font-size: 36rpx;
                font-weight: 600;
                color: #fff;
                margin-right: 16rpx;
                text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
              }

              .user-tag {
                display: flex;
                align-items: center;
                padding: 2rpx 12rpx;
                border-radius: 20rpx;
                background: rgba(255, 255, 255, 0.2);

                text {
                  font-size: 22rpx;
                  color: #fff;
                  margin-left: 4rpx;
                }
              }
            }

            .user-id {
              font-size: 24rpx;
              color: rgba(255, 255, 255, 0.8);
            }
          }
        }

        .balance-card {
          margin: 60rpx 20rpx 0rpx;
          background: #fff;
          border-radius: 16rpx;
          display: flex;
          padding: 30rpx 0;
          box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
          position: relative;

          .balance-item {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;

            .balance-value {
              font-size: 38rpx;
              font-weight: 600;
              color: #333;
              margin-bottom: 8rpx;
            }

            .balance-label {
              font-size: 24rpx;
              color: #666;
            }
          }

          .balance-divider {
            width: 2rpx;
            background: #eee;
            margin: 10rpx 0;
          }
        }
      }
    }

    .order-card {
      margin: 20rpx 20rpx;
      background: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 20rpx;
        border-bottom: 1rpx solid #f0f0f0;

        .order-title {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }

        .order-more {
          display: flex;
          align-items: center;

          text {
            font-size: 24rpx;
            color: #666;
            margin-right: 4rpx;
          }
        }
      }

      .order-menu {
        display: flex;
        padding: 30rpx 0 10rpx;

        .order-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          .order-icon {
            margin-bottom: 16rpx;
          }

          .order-label {
            font-size: 24rpx;
            color: #333;
          }
        }
      }
    }

    .income-card {
      margin: 20rpx 20rpx;
      background: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

      .income-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        //padding-bottom: 20rpx;
        //border-bottom: 1rpx solid #f0f0f0;

        .income-title {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }

        .income-more {
          display: flex;
          align-items: center;
        }
      }

      .income-content {
        margin-top: 20rpx;
        display: flex;
        justify-content: space-between;

        .income-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          .income-value {
            font-size: 38rpx;
            font-weight: 600;
            color: #333;
            margin-bottom: 8rpx;
          }

          .income-label {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }

    .community-card {
      margin: 20rpx 20rpx;
      background: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

      .community-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        //padding-bottom: 20rpx;
        //border-bottom: 1rpx solid #f0f0f0;

        .community-title {
          font-size: 30rpx;
          font-weight: 600;
          color: #333;
        }

        .community-more {
          display: flex;
          align-items: center;
        }
      }

      .community-content {
        margin-top: 20rpx;
        display: flex;
        justify-content: space-between;

        .community-item {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;

          .community-value {
            font-size: 38rpx;
            font-weight: 600;
            color: #333;
            margin-bottom: 8rpx;
          }

          .community-label {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }

    .special-area {
      margin: 20rpx 20rpx;
      border-radius: 16rpx;
      overflow: hidden;
      background: #000;
      position: relative;

      .special-swiper {
        height: 200rpx;
        width: 100%;

        :deep(.special-content) {
          height: 100%;
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 0 30rpx;

          .special-info {
            z-index: 2;

            .special-title {
              display: block;
              font-size: 32rpx;
              font-weight: 600;
              color: #fff;
              margin-bottom: 10rpx;
            }

            .special-subtitle {
              font-size: 24rpx;
              color: rgba(255, 255, 255, 0.8);
              letter-spacing: 1rpx;
            }
          }

          .special-image {
            width: 100rpx;
            height: 100rpx;
            border-radius: 8rpx;
          }
        }
      }
    }

    .function-section {
      margin: 20rpx 20rpx;
      background: #fff;
      border-radius: 16rpx;
      padding: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

      .section-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 30rpx;
      }

      .function-grid {
        display: flex;
        flex-wrap: wrap;

        .function-item {
          width: 25%;
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-bottom: 20rpx;

          .function-icon {
            width: 80rpx;
            height: 80rpx;
            border-radius: 50%;
            background: #f8f8f8;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 16rpx;
          }

          .function-name {
            font-size: 24rpx;
            color: #333;
          }
        }
      }
    }

    .brand-info {
      margin: 40rpx 0;
      padding-bottom: 120rpx;
      display: flex;
      flex-direction: column;
      align-items: center;

      .brand-logo {
        width: 240rpx;
        height: 80rpx;
        margin-bottom: 20rpx;
      }

      .brand-text {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  // 加载和错误状态样式
  .loading-container,
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100rpx 0;

    .loading-text,
    .error-text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }

    .retry-btn {
      margin-top: 30rpx;
      padding: 16rpx 40rpx;
      background-color: #8f1911;
      color: #fff;
      border-radius: 30rpx;
      font-size: 28rpx;
    }
  }

  // 未登录状态样式
  .not-login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 80vh;
    padding: 60rpx 0;

    .login-img {
      width: 150rpx;
      height: 150rpx;
      border-radius: 50%;
      margin-bottom: 30rpx;
    }

    .login-tips {
      font-size: 30rpx;
      color: #666;
      margin-bottom: 40rpx;
    }

    .login-btn {
      padding: 20rpx 80rpx;
      background-color: #8f1911;
      color: #fff;
      border-radius: 40rpx;
      font-size: 30rpx;
    }
  }

  // 徽章样式
  .order-badge,
  .function-badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    background-color: #8f1911;
    color: #fff;
    font-size: 20rpx;
    min-width: 32rpx;
    height: 32rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 6rpx;
  }

  .order-icon,
  .function-icon {
    position: relative;
  }

  /* 静默刷新指示器样式 */
  .silent-loading-indicator {
    position: fixed;
    top: 100rpx;
    right: 30rpx;
    display: flex;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.7);
    color: #fff;
    padding: 10rpx 20rpx;
    border-radius: 30rpx;
    z-index: 1000;
    font-size: 24rpx;
  }

  .silent-loading-text {
    margin-left: 10rpx;
    color: #fff;
  }


</style>
