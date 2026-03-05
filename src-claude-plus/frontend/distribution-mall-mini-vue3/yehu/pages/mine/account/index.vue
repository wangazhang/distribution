<template>
  <s-layout title="账户余额" back>
    <view class="account-page">
      <!-- 账户余额显示 -->
      <view class="balance-header">
        <text class="balance-title">账户余额（元）</text>
        <text class="balance-amount">0.00</text>
      </view>

      <!-- 账户数据 -->
      <view class="balance-info">
        <view class="info-item">
          <text class="info-value">0.00</text>
          <text class="info-label">可提现金额</text>
        </view>
        <view class="info-item">
          <text class="info-value">0.00</text>
          <text class="info-label">未结算金额</text>
        </view>
        <view class="info-item">
          <text class="info-value">0.00</text>
          <text class="info-label">冻结金额</text>
        </view>
      </view>

      <!-- 提现按钮 -->
      <view class="withdraw-btn" @tap="navigateTo('/pages/commission/withdraw')">
<!--      <view class="withdraw-btn" @tap="navigateTo('/yehu/pages/mine/account/withdraw/index')">-->
        <text>提现</text>
      </view>

      <!-- 功能列表 -->
      <view class="function-list">
        <view class="function-item" @tap="navigateTo('/yehu/pages/mine/management/earnings/index')">
          <view class="function-left">
            <view class="icon-container">
              <uni-icons type="wallet" size="24" color="#333"></uni-icons>
            </view>
            <text class="function-name">我的收益</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>

        <view class="function-item" @tap="navigateTo('/yehu/pages/mine/account/withdraw/detail/index')">
          <view class="function-left">
            <view class="icon-container">
              <uni-icons type="list" size="24" color="#333"></uni-icons>
            </view>
            <text class="function-name">提现明细</text>
          </view>
          <uni-icons type="right" size="16" color="#999"></uni-icons>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import sheep from '@/sheep';

// 页面状态
const loading = ref(false);
const accountData = ref({
  balance: 0.00,
  availableBalance: 0.00,
  pendingBalance: 0.00,
  frozenBalance: 0.00
});

// 页面跳转
const navigateTo = (url) => {
  uni.navigateTo({ url });
};

// 提现操作
const handleWithdraw = () => {
  if (accountData.value.availableBalance <= 0) {
    sheep.$helper.toast('暂无可提现金额');
    return;
  }
  
  uni.navigateTo({
    url: '/yehu/pages/mine/withdraw/apply'
  });
};

// 获取账户数据
const fetchAccountData = async () => {
  loading.value = true;
  try {
    // 模拟API调用，实际项目中需要替换为真实API
    // const res = await getAccountBalance();
    // accountData.value = res;
    
    // 模拟数据
    accountData.value = {
      balance: 0.00,
      availableBalance: 0.00,
      pendingBalance: 0.00,
      frozenBalance: 0.00
    };
  } catch (error) {
    console.error('获取账户数据失败', error);
    sheep.$helper.toast('获取账户数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchAccountData();
});
</script>

<style lang="scss" scoped>
.account-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 60rpx;
}

.balance-header {
  background-color: #fff;
  padding: 60rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .balance-title {
    font-size: 32rpx;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .balance-amount {
    font-size: 80rpx;
    font-weight: bold;
    color: #000;
  }
}

.balance-info {
  background-color: #fff;
  margin-top: 2rpx;
  padding: 30rpx 0;
  display: flex;
  justify-content: space-around;
  
  .info-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .info-value {
      font-size: 32rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 10rpx;
    }
    
    .info-label {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.withdraw-btn {
  margin: 60rpx 30rpx;
  height: 90rpx;
  background-color: #8F1911;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
  //box-shadow: 0 4rpx 8rpx rgba(255, 160, 180, 0.3);
}

.function-list {
  background-color: #fff;
  margin-top: 20rpx;
  
  .function-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
    
    .function-left {
      display: flex;
      align-items: center;
      
      .icon-container {
        width: 50rpx;
        height: 50rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }
      
      .function-name {
        font-size: 30rpx;
        color: #333;
      }
    }
  }
  
  .function-item:last-child {
    border-bottom: none;
  }
}
</style>
