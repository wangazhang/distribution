<template>
  <s-layout title="储值卡">
    <view class="prepaid-card-container">
      <!-- 选项卡 -->
      <view class="tabs">
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'balance' }"
          @click="activeTab = 'balance'"
        >
          我的余额
          <view class="active-indicator" v-if="activeTab === 'balance'"></view>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: activeTab === 'gift' }"
          @click="activeTab = 'gift'"
        >
          我的礼品卡
          <view class="active-indicator" v-if="activeTab === 'gift'"></view>
        </view>
      </view>

      <!-- 余额选项卡内容 -->
      <view v-if="activeTab === 'balance'">
        <!-- 余额展示区域 -->
        <view class="balance-card">
          <view class="balance-title">我的储值卡(元)</view>
          <view class="balance-amount">{{ balanceAmount }}</view>
        </view>

        <!-- 明细区域 -->
        <view class="detail-section">
          <view class="detail-header">
            <text class="detail-title">储值卡明细</text>
            <view class="view-all" @click="viewAllDetails">
              全部 >
            </view>
          </view>

          <!-- 空状态展示 -->
          <view class="empty-state" v-if="!cardDetails.length">
            <image class="empty-image" src="https://cdn.example.com/static/pic/empty/empty_data.png" mode="aspectFit"></image>
            <text class="empty-text">暂无相关数据</text>
          </view>

          <!-- 有数据时的列表 -->
          <view class="detail-list" v-else>
            <view class="detail-item" v-for="(item, index) in cardDetails" :key="index">
              <view class="detail-info">
                <text class="detail-desc">{{ item.description }}</text>
                <text class="detail-time">{{ item.time }}</text>
              </view>
              <text class="detail-amount" :class="{ 'amount-plus': item.amount > 0 }">
                {{ item.amount > 0 ? '+' : '' }}{{ item.amount }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 礼品卡选项卡内容 -->
      <view v-else-if="activeTab === 'gift'" class="gift-card-container">
        <!-- 筛选栏 -->
        <view class="filter-bar">
          <view class="filter-item">
            <text>卡状态</text>
            <view class="filter-arrow">▼</view>
          </view>
          <view class="filter-item">
            <text>卡名称</text>
            <view class="filter-arrow">▼</view>
          </view>
          <view class="exchange-btn" @click="openExchangeDialog">
            <text class="icon-exchange">⟳</text>
            <text>兑换卡</text>
          </view>
        </view>

        <!-- 礼品卡内容区域 -->
        <view class="gift-content">
          <!-- 空状态区域 -->
          <view class="gift-empty-state">
            <image class="gift-empty-image" src="https://cdn.example.com/static/pic/empty/empty_data.png" mode="aspectFit"></image>
            <text class="gift-empty-text">您还没有礼品卡哦~</text>
            <view class="exchange-action-btn" @click="openExchangeDialog">
              去兑换
            </view>
          </view>
        </view>
        
        <!-- 历史礼品卡 -->
        <view class="history-section">
          <text class="history-title">历史礼品卡</text>
        </view>
      </view>
    </view>
    
    <!-- 使用uni-popup组件 -->
    <view class="popup-container">
      <view v-if="showExchangePopup" class="mask" @click="closeExchangeDialog"></view>
      <view v-if="showExchangePopup" class="exchange-popup">
        <view class="exchange-popup-header">
          <text class="exchange-popup-title">兑换卡</text>
          <view class="exchange-popup-close" @click="closeExchangeDialog">×</view>
        </view>
        <view class="exchange-popup-content">
          <view class="exchange-popup-input-area">
            <text class="exchange-popup-label">礼品卡卡密</text>
            <input 
              class="exchange-popup-input" 
              type="text" 
              v-model="exchangeCode" 
              placeholder="请输入礼品卡卡密"
            />
          </view>
          <view class="exchange-popup-btn" @click="exchangeCard">兑换</view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref } from 'vue';

// 当前激活的选项卡
const activeTab = ref('balance');

// 储值卡余额
const balanceAmount = ref(0);

// 储值卡明细
const cardDetails = ref([]);

// 兑换弹窗控制
const showExchangePopup = ref(false);
const exchangeCode = ref('');

// 打开兑换弹窗
const openExchangeDialog = () => {
  showExchangePopup.value = true;
};

// 关闭兑换弹窗
const closeExchangeDialog = () => {
  showExchangePopup.value = false;
};

// 查看全部明细
const viewAllDetails = () => {
  // 跳转到完整明细页面
  uni.navigateTo({
    url: '/yehu/pages/mine/prepaid-card/detail'
  });
};

// 兑换卡
const exchangeCard = () => {
  if (!exchangeCode.value) {
    uni.showToast({
      title: '请输入卡密',
      icon: 'none'
    });
    return;
  }
  
  // 这里应该调用API进行卡密兑换
  console.log('兑换卡密:', exchangeCode.value);
  
  // 模拟兑换成功
  uni.showToast({
    title: '兑换失败',
    icon: 'error'
  });
  // uni.showToast({
  //   title: '兑换成功',
  //   icon: 'success'
  // });
  
  // 关闭弹窗并清空输入
  closeExchangeDialog();
  exchangeCode.value = '';
};
</script>

<style lang="scss" scoped>
.prepaid-card-container {
  padding: 20rpx;
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.tab-item {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
  position: relative;
  
  &.active {
    font-weight: bold;
    color: #000;
  }
  
  .active-indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60rpx;
    height: 6rpx;
    background-color: #8F1911;
    border-radius: 4rpx;
  }
}

.balance-card {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
}

.balance-amount {
  font-size: 80rpx;
  font-weight: bold;
  color: #000;
}

.detail-section {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding: 30rpx;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.view-all {
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.detail-list {
  margin-top: 20rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-info {
  display: flex;
  flex-direction: column;
}

.detail-desc {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.detail-time {
  font-size: 24rpx;
  color: #999;
}

.detail-amount {
  font-size: 30rpx;
  color: #333;
  
  &.amount-plus {
    color: #f56c6c;
  }
}

/* 礼品卡选项卡样式 */
.gift-card-container {
  background-color: #ffffff;
  border-radius: 12rpx;
  padding-bottom: 30rpx;
  display: flex;
  flex-direction: column;
  min-height: 80vh;
}

.filter-bar {
  display: flex;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-right: 40rpx;
  font-size: 26rpx;
  color: #333;
}

.filter-arrow {
  font-size: 20rpx;
  color: #999;
  margin-left: 8rpx;
}

.exchange-btn {
  display: flex;
  align-items: center;
  margin-left: auto;
  color: #8F1911;
  font-size: 26rpx;
}

.icon-exchange {
  margin-right: 6rpx;
}

.gift-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.gift-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  flex: 1;
}

.gift-empty-image {
  width: 300rpx;
  height: 300rpx;
  margin-bottom: 20rpx;
}

.gift-empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.exchange-action-btn {
  background-color: #8F1911;
  color: #fff;
  font-size: 28rpx;
  padding: 20rpx 80rpx;
  border-radius: 40rpx;
}

.history-section {
  text-align: center;
  padding: 30rpx;
  margin-top: auto;
  border-top: 1rpx solid #f0f0f0;
}

.history-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

/* 弹窗样式 */
.popup-container {
  position: relative;
  z-index: 999;
}

.mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 998;
}

/* 兑换卡密弹窗样式 */
.exchange-popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600rpx;
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  z-index: 999;
}

.exchange-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.exchange-popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.exchange-popup-close {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.exchange-popup-content {
  padding: 30rpx;
}

.exchange-popup-input-area {
  margin-bottom: 30rpx;
}

.exchange-popup-label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.exchange-popup-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.exchange-popup-btn {
  height: 80rpx;
  background-color: #8F1911;
  color: #fff;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8rpx;
}
</style>
