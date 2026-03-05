<template>
  <s-layout title="申请提现" back>
    <view class="withdraw-page">
      <view class="withdraw-amount-section">
        <view class="section-title">提现金额</view>
        <view class="amount-input-wrapper">
          <text class="currency-symbol">¥</text>
          <input 
            type="digit" 
            class="amount-input" 
            v-model="amount" 
            placeholder="请输入提现金额"
            maxlength="10"
            @input="validateAmount"
            @blur="formatAmount"
          />
        </view>
        <view class="available-amount">
          <text>可提现金额：¥{{availableAmount}}</text>
          <text class="all-btn" @tap="setMaxAmount">全部提现</text>
        </view>
      </view>

      <view class="withdraw-info-section">
        <view class="info-item">
          <text class="info-label">提现方式</text>
          <view class="info-value">
            <text>微信</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
        <view class="info-item">
          <text class="info-label">预计到账</text>
          <view class="info-value">
            <text>实时到账</text>
          </view>
        </view>
        <view class="tips-box">
          <text class="tips-title">温馨提示：</text>
          <text class="tips-content">1. 提现金额最低1元起；</text>
          <text class="tips-content">2. 提现将扣除0.6%手续费，最低0.1元；</text>
          <text class="tips-content">3. 每日提现次数不限，单笔提现上限1万元；</text>
          <text class="tips-content">4. 提现金额将原路返回至微信钱包；</text>
        </view>
      </view>

      <view class="withdraw-btn-section">
        <button 
          class="withdraw-btn" 
          :class="{ disabled: !isFormValid }" 
          :disabled="!isFormValid"
          @tap="submitWithdraw"
        >
          确认提现
        </button>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
import { ref, computed } from 'vue';
import sheep from '@/sheep';

// 表单数据
const amount = ref('');
const availableAmount = ref('0.00');
const minAmount = 1; // 最小提现金额
const maxAmount = 10000; // 最大提现金额

// 获取可提现金额数据
const fetchAvailableAmount = async () => {
  try {
    // 实际开发中应该调用API获取可提现金额
    // const res = await getAvailableAmount();
    // availableAmount.value = res.amount || '0.00';
    
    // 模拟数据
    availableAmount.value = '0.00';
  } catch (error) {
    console.error('获取可提现金额失败', error);
    sheep.$helper.toast('获取可提现金额失败');
  }
};

// 设置最大提现金额
const setMaxAmount = () => {
  if (parseFloat(availableAmount.value) === 0) {
    sheep.$helper.toast('暂无可提现金额');
    return;
  }
  amount.value = availableAmount.value;
};

// 验证金额
const validateAmount = () => {
  if (amount.value === '') return;
  
  // 限制输入两位小数
  const regex = /^\d+(\.\d{0,2})?$/;
  if (!regex.test(amount.value)) {
    amount.value = amount.value.substring(0, amount.value.length - 1);
  }
};

// 格式化金额
const formatAmount = () => {
  if (amount.value === '') return;
  
  let num = parseFloat(amount.value);
  
  // 检查是否超过可提现金额
  if (num > parseFloat(availableAmount.value)) {
    num = parseFloat(availableAmount.value);
    sheep.$helper.toast('提现金额不能超过可提现金额');
  }
  
  // 检查是否低于最小提现金额
  if (num < minAmount && num !== 0) {
    sheep.$helper.toast(`提现金额不能低于${minAmount}元`);
  }
  
  // 检查是否超过最大提现金额
  if (num > maxAmount) {
    num = maxAmount;
    sheep.$helper.toast(`单笔提现金额不能超过${maxAmount}元`);
  }
  
  amount.value = num === 0 ? '' : num.toFixed(2);
};

// 表单验证
const isFormValid = computed(() => {
  if (!amount.value) return false;
  
  const num = parseFloat(amount.value);
  return num >= minAmount && num <= maxAmount && num <= parseFloat(availableAmount.value);
});

// 提交提现申请
const submitWithdraw = async () => {
  if (!isFormValid.value) return;
  
  try {
    // 显示加载中
    uni.showLoading({
      title: '提交中...',
      mask: true
    });
    
    // 实际开发中应该调用API提交提现申请
    // const res = await submitWithdrawRequest({
    //   amount: parseFloat(amount.value)
    // });
    
    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    // 隐藏加载中
    uni.hideLoading();
    
    // 提示成功
    sheep.$helper.toast('提现申请已提交');
    
    // 返回上一页
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
  } catch (error) {
    console.error('提现申请失败', error);
    uni.hideLoading();
    sheep.$helper.toast('提现申请失败，请重试');
  }
};

// 页面初始化时获取可提现金额
fetchAvailableAmount();
</script>

<style lang="scss" scoped>
.withdraw-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 100rpx;
}

.withdraw-amount-section {
  background-color: #fff;
  padding: 40rpx 30rpx;
  
  .section-title {
    font-size: 30rpx;
    color: #333;
    margin-bottom: 40rpx;
  }
  
  .amount-input-wrapper {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 20rpx;
    margin-bottom: 20rpx;
    
    .currency-symbol {
      font-size: 48rpx;
      font-weight: bold;
      color: #333;
      margin-right: 10rpx;
    }
    
    .amount-input {
      flex: 1;
      font-size: 48rpx;
      font-weight: bold;
      color: #333;
    }
  }
  
  .available-amount {
    display: flex;
    justify-content: space-between;
    font-size: 24rpx;
    color: #666;
    
    .all-btn {
      color: #8F1911;
    }
  }
}

.withdraw-info-section {
  background-color: #fff;
  margin-top: 20rpx;
  padding: 0 30rpx;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1px solid #f5f5f5;
    
    .info-label {
      font-size: 28rpx;
      color: #333;
    }
    
    .info-value {
      display: flex;
      align-items: center;
      
      text {
        font-size: 28rpx;
        color: #666;
        margin-right: 10rpx;
      }
    }
  }
  
  .tips-box {
    padding: 30rpx 0;
    display: flex;
    flex-direction: column;
    
    .tips-title {
      font-size: 26rpx;
      color: #999;
      margin-bottom: 10rpx;
    }
    
    .tips-content {
      font-size: 24rpx;
      color: #999;
      line-height: 1.8;
    }
  }
}

.withdraw-btn-section {
  padding: 60rpx 30rpx;
  
  .withdraw-btn {
    width: 100%;
    height: 90rpx;
    line-height: 90rpx;
    background-color: #FFA0B4;
    color: #fff;
    font-size: 32rpx;
    font-weight: 500;
    border-radius: 45rpx;
    border: none;
    
    &.disabled {
      background-color: #ccc;
    }
  }
}
</style>
