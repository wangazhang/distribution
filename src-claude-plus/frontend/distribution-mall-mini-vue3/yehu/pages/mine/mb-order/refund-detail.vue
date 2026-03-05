<template>
  <s-layout title="退款详情">
    <view class="page-container">
      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <uni-load-more status="loading" :contentText="loadingText"></uni-load-more>
        <text class="loading-text">退款详情加载中...</text>
      </view>

      <!-- 错误状态 -->
      <view v-else-if="error" class="error-container">
        <uni-icons type="error" size="30" color="#8f1911"></uni-icons>
        <text class="error-text">{{ errorMessage }}</text>
        <view class="retry-btn" @tap="fetchRefundDetail">
          <text>重新加载</text>
        </view>
      </view>

      <!-- 退款详情内容 -->
      <view v-else-if="refundDetail" class="detail-content">
        <!-- 退款状态卡片 -->
        <view class="status-card">
          <view class="status-icon" :class="getRefundStatusClass(refundDetail.status)">
            <uni-icons :type="getRefundStatusIcon(refundDetail.status)" size="24" color="#fff"></uni-icons>
          </view>
          <view class="status-info">
            <text class="status-title">{{ getRefundStatusName(refundDetail.status) }}</text>
            <text class="status-desc">{{ getRefundStatusDesc(refundDetail.status) }}</text>
          </view>
          <view class="refund-amount">
            <text class="amount-label">退款金额</text>
            <text class="amount-value">¥{{ formatPrice(refundDetail.refundAmount) }}</text>
          </view>
        </view>

        <!-- 退款进度时间线 -->
        <view class="timeline-card">
          <view class="card-title">
            <text>退款进度</text>
          </view>
          <view class="timeline-list">
            <view 
              v-for="(item, index) in timelineData" 
              :key="index"
              class="timeline-item"
              :class="{ active: item.active, completed: item.completed }"
            >
              <view class="timeline-dot">
                <view class="dot-inner"></view>
              </view>
              <view class="timeline-content">
                <text class="timeline-title">{{ item.title }}</text>
                <text class="timeline-desc">{{ item.desc }}</text>
                <text v-if="item.time" class="timeline-time">{{ item.time }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 订单信息 -->
        <view class="order-card">
          <view class="card-title">
            <text>订单信息</text>
          </view>
          <view class="order-info-list">
            <view class="info-item">
              <text class="label">订单编号</text>
              <text class="value">{{ refundDetail.orderId }}</text>
            </view>
            <view class="info-item">
              <text class="label">商品名称</text>
              <text class="value">{{ refundDetail.productName || '商品名称' }}</text>
            </view>
            <view class="info-item">
              <text class="label">退款原因</text>
              <text class="value">{{ refundDetail.refundReason || '无' }}</text>
            </view>
            <view class="info-item">
              <text class="label">申请时间</text>
              <text class="value">{{ formatDateTime(refundDetail.applyTime) }}</text>
            </view>
            <view class="info-item" v-if="refundDetail.refundTime">
              <text class="label">退款时间</text>
              <text class="value">{{ formatDateTime(refundDetail.refundTime) }}</text>
            </view>
          </view>
        </view>

        <!-- 退款说明 -->
        <view class="notice-card">
          <view class="card-title">
            <text>退款说明</text>
          </view>
          <view class="notice-content">
            <text class="notice-text">1. 退款将原路返回到您的支付账户</text>
            <text class="notice-text">2. 退款到账时间：微信支付1-3个工作日，支付宝实时到账</text>
            <text class="notice-text">3. 如有疑问，请联系客服</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons" v-if="showActions">
          <view 
            v-if="refundDetail.status === REFUND_STATUS.WAITING"
            class="action-btn secondary"
            @tap="showCancelRefundModal"
          >
            <text>撤销退款</text>
          </view>
          <view class="action-btn primary" @tap="contactService">
            <text>联系客服</text>
          </view>
        </view>
      </view>

      <!-- 申请退款表单（如果是申请退款页面） -->
      <view v-else-if="isApplyMode" class="apply-content">
        <view class="apply-card">
          <view class="card-title">
            <text>申请退款</text>
          </view>
          
          <!-- 退款金额 -->
          <view class="refund-amount-section">
            <text class="amount-label">退款金额</text>
            <text class="amount-value">¥{{ formatPrice(orderDetail?.totalPrice) }}</text>
          </view>
          
          <!-- 退款原因 -->
          <view class="reason-section">
            <text class="section-title">退款原因</text>
            <view class="reason-options">
              <view 
                v-for="(reason, index) in refundReasons" 
                :key="index"
                class="reason-option"
                :class="{ active: selectedReason === reason }"
                @tap="selectReason(reason)"
              >
                <text>{{ reason }}</text>
                <uni-icons 
                  v-if="selectedReason === reason" 
                  type="checkmarkempty" 
                  size="16" 
                  color="#007aff"
                ></uni-icons>
              </view>
            </view>
          </view>
          
          <!-- 详细说明 -->
          <view class="desc-section">
            <text class="section-title">详细说明（可选）</text>
            <textarea 
              v-model="refundDesc"
              class="desc-input"
              placeholder="请详细描述退款原因"
              maxlength="200"
            ></textarea>
          </view>
        </view>

        <!-- 提交按钮 -->
        <view class="submit-section">
          <view 
            class="submit-btn" 
            :class="{ disabled: !selectedReason || submitLoading }"
            @tap="submitRefund"
          >
            <text v-if="!submitLoading">提交退款申请</text>
            <text v-else>提交中...</text>
          </view>
        </view>
      </view>
    </view>
  </s-layout>
</template>

<script>
// 组件声明
export default {
  components: {
    'uni-icons': () => import('@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'),
    'uni-load-more': () => import('@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue')
  }
};
</script>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getRefundDetail, getMbOrderDetail, applyRefund } from '@/yehu/api/mine/mb-order';
import sheep from '@/sheep';

// 页面参数
const orderId = ref('');
const isApplyMode = ref(false);

// 退款详情数据
const refundDetail = ref(null);
const orderDetail = ref(null);

// 加载状态
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('退款详情加载失败，请重试');

// 申请退款相关
const selectedReason = ref('');
const refundDesc = ref('');
const submitLoading = ref(false);

// 退款原因选项
const refundReasons = ref([
  '不想要了',
  '商品质量问题',
  '商品与描述不符',
  '发错商品',
  '其他原因'
]);

// 退款状态枚举
const REFUND_STATUS = {
  WAITING: 0,    // 等待退款
  SUCCESS: 10,   // 退款成功
  FAILURE: 20,   // 退款失败
};

// loading文本配置
const loadingText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了',
};

// 时间线数据
const timelineData = computed(() => {
  if (!refundDetail.value) return [];
  
  const status = refundDetail.value.status;
  const timeline = [
    {
      title: '提交退款申请',
      desc: '您已成功提交退款申请',
      time: formatDateTime(refundDetail.value.applyTime),
      completed: true,
      active: false
    },
    {
      title: '商家处理中',
      desc: status >= REFUND_STATUS.WAITING ? '商家正在处理您的退款申请' : '等待商家处理',
      time: '',
      completed: status > REFUND_STATUS.WAITING,
      active: status === REFUND_STATUS.WAITING
    },
    {
      title: '退款完成',
      desc: status === REFUND_STATUS.SUCCESS ? '退款已成功，请注意查收' : 
            status === REFUND_STATUS.FAILURE ? '退款失败，请联系客服' : '等待退款完成',
      time: refundDetail.value.refundTime ? formatDateTime(refundDetail.value.refundTime) : '',
      completed: status === REFUND_STATUS.SUCCESS,
      active: status === REFUND_STATUS.SUCCESS
    }
  ];
  
  return timeline;
});

// 是否显示操作按钮
const showActions = computed(() => {
  return refundDetail.value && refundDetail.value.status !== REFUND_STATUS.SUCCESS;
});

// 获取退款详情
const fetchRefundDetail = async () => {
  loading.value = true;
  error.value = false;

  try {
    const res = await getRefundDetail(orderId.value);
    console.log("获取退款详情响应:", res);

    if (res && res.code === 0 && res.data) {
      refundDetail.value = res.data;
    } else {
      throw new Error(res?.msg || '获取退款详情失败');
    }
  } catch (err) {
    console.error('获取退款详情失败:', err);
    error.value = true;
    errorMessage.value = err.message || '网络错误，请重试';
  } finally {
    loading.value = false;
  }
};

// 获取订单详情（申请退款模式）
const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = false;

  try {
    const res = await getMbOrderDetail(orderId.value);
    console.log("获取订单详情响应:", res);

    if (res && res.code === 0 && res.data) {
      orderDetail.value = res.data;
    } else {
      throw new Error(res?.msg || '获取订单详情失败');
    }
  } catch (err) {
    console.error('获取订单详情失败:', err);
    error.value = true;
    errorMessage.value = err.message || '网络错误，请重试';
  } finally {
    loading.value = false;
  }
};

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00';
  return (price / 100).toFixed(2);
};

// 格式化日期时间
const formatDateTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 获取退款状态名称
const getRefundStatusName = (status) => {
  switch (status) {
    case REFUND_STATUS.WAITING:
      return '退款处理中';
    case REFUND_STATUS.SUCCESS:
      return '退款成功';
    case REFUND_STATUS.FAILURE:
      return '退款失败';
    default:
      return '未知状态';
  }
};

// 获取退款状态描述
const getRefundStatusDesc = (status) => {
  switch (status) {
    case REFUND_STATUS.WAITING:
      return '我们正在为您处理退款申请';
    case REFUND_STATUS.SUCCESS:
      return '退款已成功到账，请注意查收';
    case REFUND_STATUS.FAILURE:
      return '退款处理失败，请联系客服';
    default:
      return '';
  }
};

// 获取退款状态样式类
const getRefundStatusClass = (status) => {
  switch (status) {
    case REFUND_STATUS.WAITING:
      return 'status-waiting';
    case REFUND_STATUS.SUCCESS:
      return 'status-success';
    case REFUND_STATUS.FAILURE:
      return 'status-failure';
    default:
      return 'status-default';
  }
};

// 获取退款状态图标
const getRefundStatusIcon = (status) => {
  switch (status) {
    case REFUND_STATUS.WAITING:
      return 'undo';
    case REFUND_STATUS.SUCCESS:
      return 'checkmarkempty';
    case REFUND_STATUS.FAILURE:
      return 'close';
    default:
      return 'info';
  }
};

// 选择退款原因
const selectReason = (reason) => {
  selectedReason.value = reason;
};

// 提交退款申请
const submitRefund = async () => {
  if (!selectedReason.value || submitLoading.value) return;

  submitLoading.value = true;

  try {
    const res = await applyRefund({
      orderId: orderId.value,
      reason: selectedReason.value + (refundDesc.value ? `：${refundDesc.value}` : '')
    });

    if (res && res.code === 0) {
      uni.showToast({
        title: '退款申请已提交',
        icon: 'success'
      });
      
      // 跳转到退款详情页面
      setTimeout(() => {
        uni.redirectTo({
          url: `/yehu/pages/mine/mb-order/refund-detail?id=${orderId.value}`
        });
      }, 1500);
    } else {
      throw new Error(res?.msg || '退款申请失败');
    }
  } catch (err) {
    console.error('提交退款申请失败:', err);
    uni.showToast({
      title: err.message || '退款申请失败',
      icon: 'none'
    });
  } finally {
    submitLoading.value = false;
  }
};

// 显示撤销退款弹窗
const showCancelRefundModal = () => {
  uni.showModal({
    title: '撤销退款',
    content: '确定要撤销退款申请吗？',
    success: (res) => {
      if (res.confirm) {
        // 这里可以调用撤销退款的API
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        });
      }
    }
  });
};

// 联系客服
const contactService = () => {
  uni.showModal({
    title: '联系客服',
    content: '请拨打客服电话：400-123-4567',
    showCancel: false
  });
};

// 页面加载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  orderId.value = currentPage.options.id;
  isApplyMode.value = currentPage.options.action === 'apply';
  
  if (orderId.value) {
    if (isApplyMode.value) {
      fetchOrderDetail();
    } else {
      fetchRefundDetail();
    }
  } else {
    error.value = true;
    errorMessage.value = '订单ID不能为空';
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.page-container {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 30rpx;
  
  .loading-text, .error-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #666;
  }
  
  .retry-btn {
    margin-top: 30rpx;
    padding: 20rpx 40rpx;
    background-color: #007aff;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}

.detail-content, .apply-content {
  padding: 30rpx;
}

.status-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .status-icon {
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    
    &.status-waiting {
      background-color: #ffc107;
    }
    
    &.status-success {
      background-color: #28a745;
    }
    
    &.status-failure {
      background-color: #dc3545;
    }
    
    &.status-default {
      background-color: #6c757d;
    }
  }
  
  .status-info {
    flex: 1;
    
    .status-title {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .status-desc {
      font-size: 26rpx;
      color: #666;
    }
  }
  
  .refund-amount {
    text-align: right;
    
    .amount-label {
      display: block;
      font-size: 24rpx;
      color: #666;
      margin-bottom: 8rpx;
    }
    
    .amount-value {
      font-size: 36rpx;
      color: #ff4757;
      font-weight: 600;
    }
  }
}

.timeline-card, .order-card, .notice-card, .apply-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    padding-bottom: 15rpx;
    border-bottom: 1rpx solid #eee;
  }
}

.timeline-list {
  .timeline-item {
    display: flex;
    position: relative;
    padding-bottom: 40rpx;
    
    &:last-child {
      padding-bottom: 0;
      
      .timeline-dot::after {
        display: none;
      }
    }
    
    .timeline-dot {
      width: 20rpx;
      height: 20rpx;
      border-radius: 50%;
      background-color: #ddd;
      margin-right: 20rpx;
      margin-top: 10rpx;
      position: relative;
      flex-shrink: 0;
      
      &::after {
        content: '';
        position: absolute;
        top: 20rpx;
        left: 50%;
        transform: translateX(-50%);
        width: 2rpx;
        height: 40rpx;
        background-color: #ddd;
      }
      
      .dot-inner {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        background-color: #fff;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
    }
    
    &.completed .timeline-dot {
      background-color: #28a745;
      
      &::after {
        background-color: #28a745;
      }
    }
    
    &.active .timeline-dot {
      background-color: #007aff;
      
      &::after {
        background-color: #007aff;
      }
    }
    
    .timeline-content {
      flex: 1;
      
      .timeline-title {
        display: block;
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 8rpx;
      }
      
      .timeline-desc {
        display: block;
        font-size: 26rpx;
        color: #666;
        margin-bottom: 8rpx;
        line-height: 1.4;
      }
      
      .timeline-time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.order-info-list {
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      font-size: 28rpx;
      color: #666;
      flex-shrink: 0;
      width: 160rpx;
    }
    
    .value {
      font-size: 28rpx;
      color: #333;
      text-align: right;
      flex: 1;
      word-break: break-all;
    }
  }
}

.notice-content {
  .notice-text {
    display: block;
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 12rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  
  .action-btn {
    flex: 1;
    padding: 24rpx;
    border-radius: 40rpx;
    font-size: 30rpx;
    text-align: center;
    font-weight: 500;
    
    &.primary {
      background-color: #007aff;
      color: #fff;
    }
    
    &.secondary {
      background-color: #f8f8f8;
      color: #333;
      border: 1rpx solid #ddd;
    }
  }
}

// 申请退款页面样式
.refund-amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
  margin-bottom: 30rpx;
  
  .amount-label {
    font-size: 30rpx;
    color: #333;
  }
  
  .amount-value {
    font-size: 36rpx;
    color: #ff4757;
    font-weight: 600;
  }
}

.reason-section, .desc-section {
  margin-bottom: 30rpx;
  
  .section-title {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.reason-options {
  .reason-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 20rpx;
    margin-bottom: 12rpx;
    border: 1rpx solid #ddd;
    border-radius: 12rpx;
    background-color: #f8f8f8;
    
    &.active {
      border-color: #007aff;
      background-color: #f0f8ff;
    }
    
    text {
      font-size: 28rpx;
      color: #333;
    }
  }
}

.desc-input {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #f8f8f8;
}

.submit-section {
  margin-top: 60rpx;
  
  .submit-btn {
    width: 100%;
    padding: 28rpx;
    background-color: #007aff;
    color: #fff;
    border-radius: 40rpx;
    font-size: 32rpx;
    text-align: center;
    font-weight: 500;
    
    &.disabled {
      background-color: #ccc;
      color: #999;
    }
  }
}
</style>
