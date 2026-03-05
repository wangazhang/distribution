<template>
  <s-layout :title="getStatusText(outboundDetail.status)">
    <view class="detail-container">
  

      <!-- 物流信息卡片 -->
      <view v-if="outboundDetail.status >= 2" class="card logistics-card">
        <view class="card-title">
          <text>物流信息</text>
        </view>
        <view class="logistics-info">
          <view class="info-item">
            <text class="item-label">物流公司</text>
            <text class="item-value">{{ outboundDetail.logisticsCompany || '暂无' }}</text>
          </view>
          <view class="info-item">
            <text class="item-label">物流单号</text>
            <text class="item-value">{{ outboundDetail.trackingNumber || '暂无' }}</text>
          </view>
          <view class="copy-btn" @tap="copyLogisticsCode">
            <text>复制单号</text>
          </view>
        </view>
      </view>

      <!-- 收货地址卡片 -->
      <view class="card address-card">
        <view class="card-title">
          <text>收货信息</text>
        </view>
        <view class="address-info">
          <view class="address-header">
            <text class="address-name">{{ outboundDetail.receiverName }}</text>
            <text class="address-mobile">{{ outboundDetail.receiverMobile }}</text>
          </view>
          <text class="address-detail">{{ formatAddress(outboundDetail) }}</text>
        </view>
      </view>

      <!-- 物料明细卡片 -->
      <view class="card material-card">
        <view class="card-title">
          <text>物料明细</text>
        </view>
        <view class="material-list">
          <view 
            v-for="(item, index) in outboundDetail.items" 
            :key="index" 
            class="material-item"
          >
            <image class="material-image" :src="item.materialImage || defaultImage" mode="aspectFill"></image>
            <view class="material-info">
              <text class="material-name">{{ item.materialName }}</text>
              <text class="material-id">编号: {{ item.materialId }}</text>
            </view>
            <view class="material-quantity">
              <text>{{ item.quantity }} {{ item.materialUnit }}</text>
            </view>
          </view>
        </view>
        <view class="material-total">
          <text>共 {{ outboundDetail.items?.length || 0 }} 种物料，总计 {{ getTotalQuantity() }} 件</text>
        </view>
      </view>

      <!-- 备注信息卡片 -->
      <view class="card remark-card">
        <view class="card-title">
          <text>备注信息</text>
        </view>
        <view class="remark-content">
          <text>{{ outboundDetail.remark || '无备注信息' }}</text>
        </view>
      </view>

      <!-- 订单信息卡片 -->
      <view class="card order-card">
        <view class="card-title">
          <text>申请信息</text>
        </view>
        <view class="order-info">
          <view class="info-item">
            <text class="item-label">申请单号</text>
            <text class="item-value">{{ outboundDetail.outboundNo }}</text>
          </view>
          <view class="info-item">
            <text class="item-label">申请时间</text>
            <text class="item-value">{{ formatDateTime(outboundDetail.createTime) }}</text>
          </view>
          <view v-if="outboundDetail.approveTime" class="info-item">
            <text class="item-label">审核时间</text>
            <text class="item-value">{{ formatDateTime(outboundDetail.approveTime) }}</text>
          </view>
          <view v-if="outboundDetail.shipTime" class="info-item">
            <text class="item-label">发货时间</text>
            <text class="item-value">{{ formatDateTime(outboundDetail.shipTime) }}</text>
          </view>
          <view v-if="outboundDetail.completeTime" class="info-item">
            <text class="item-label">完成时间</text>
            <text class="item-value">{{ formatDateTime(outboundDetail.completeTime) }}</text>
          </view>
          <view v-if="outboundDetail.cancelTime" class="info-item">
            <text class="item-label">取消时间</text>
            <text class="item-value">{{ formatDateTime(outboundDetail.cancelTime) }}</text>
          </view>
          <view v-if="outboundDetail.cancelReason" class="info-item">
            <text class="item-label">取消原因</text>
            <text class="item-value">{{ outboundDetail.cancelReason }}</text>
          </view>
        </view>
      </view>

      <!-- 底部操作按钮 -->
      <view v-if="showActions" class="bottom-actions">
        <!-- 待审核状态可取消 -->
        <view 
          v-if="outboundDetail.status === 0" 
          class="action-btn cancel-btn"
          @tap="showCancelConfirm"
        >
          取消申请
        </view>
        
        <!-- 已发货状态可确认收货 -->
        <view 
          v-if="outboundDetail.status === 2" 
          class="action-btn confirm-btn"
          @tap="showConfirmReceiptConfirm"
        >
          确认收货
        </view>
      </view>

      <!-- 底部安全区 -->
      <view class="safe-area-bottom"></view>
    </view>
    
    <!-- 取消确认弹窗 -->
    <su-dialog
      :show="showCancelDialog"
      title="取消申请"
      content="确认取消此出库申请吗？"
      :before-close="true"
      @confirm="confirmCancel"
      @close="closeCancelPopup"
    >
      <textarea 
        class="cancel-reason" 
        placeholder="请输入取消原因（选填）" 
        v-model="cancelReason"
        maxlength="100"
      ></textarea>
    </su-dialog>
    
    <!-- 确认收货弹窗 -->
    <su-dialog
      :show="showConfirmReceiptDialog"
      title="确认收货"
      content="确认已收到物料吗？"
      :before-close="true"
      @confirm="confirmReceipt"
      @close="closeConfirmReceiptPopup"
    ></su-dialog>
  </s-layout>
</template>

<script>
// 组件声明
export default {
  components: {
    'uni-icons': () => import('@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'),
    'su-dialog': () => import('@/sheep/ui/su-dialog/su-dialog.vue'),
    'su-sticky': () => import('@/sheep/ui/su-sticky/su-sticky.vue')
  }
};
</script>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getMaterialOutboundDetail, cancelMaterialOutbound, confirmMaterialOutbound } from '@/yehu/api/mine/material-outbound';

// 默认图片
const defaultImage = 'https://cdn.example.com/static/pic/product/1.png';

// 详情数据
const outboundDetail = ref({
  id: 0,
  outboundNo: '',
  status: 0,
  receiverName: '',
  receiverMobile: '',
  receiverProvince: '',
  receiverCity: '',
  receiverDistrict: '',
  receiverDetailAddress: '',
  items: [],
  logisticsCompany: '',
  trackingNumber: '',
  remark: '',
  createTime: null,
  approveTime: null,
  shipTime: null,
  completeTime: null,
  cancelTime: null,
  cancelReason: ''
});

// 取消申请相关
const showCancelDialog = ref(false);
const cancelReason = ref('');

// 确认收货相关
const showConfirmReceiptDialog = ref(false);

// 是否显示底部操作按钮
const showActions = computed(() => {
  return outboundDetail.value.status === 0 || outboundDetail.value.status === 2;
});

// 获取出库申请详情
const fetchOutboundDetail = async () => {
  uni.showLoading({
    title: '加载中...'
  });
  
  try {
    // 获取页面参数
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    const id = currentPage.options.id;
    
    if (!id) {
      throw new Error('未找到出库申请ID');
    }
    
    const res = await getMaterialOutboundDetail(id);
    
    if (res && res.data) {
      outboundDetail.value = res.data;
    } else {
      throw new Error('获取详情失败');
    }
  } catch (err) {
    console.error('获取出库申请详情失败', err);
    uni.showToast({
      title: err.message || '获取详情失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核';
    case 1: return '待发货';
    case 2: return '已发货';
    case 3: return '已完成';
    case 4: return '已取消';
    default: return '未知状态';
  }
};

// 获取状态描述
const getStatusDesc = (status) => {
  switch (status) {
    case 0: return '您的申请正在等待审核';
    case 1: return '申请已审核通过，等待发货';
    case 2: return '物料已发出，请注意查收';
    case 3: return '物料已签收，出库完成';
    case 4: return '申请已取消';
    default: return '';
  }
};

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case 0: return 'info';
    case 1: return 'checkmarkempty';
    case 2: return 'paperplane';
    case 3: return 'checkbox-filled';
    case 4: return 'closeempty';
    default: return 'info';
  }
};

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending';
    case 1: return 'status-approved';
    case 2: return 'status-shipped';
    case 3: return 'status-completed';
    case 4: return 'status-canceled';
    default: return '';
  }
};

// 格式化地址
const formatAddress = (outbound) => {
  if (!outbound) return '';
  return `${outbound.receiverProvince || ''} ${outbound.receiverCity || ''} ${outbound.receiverDistrict || ''} ${outbound.receiverDetailAddress || ''}`;
};

// 格式化日期时间
const formatDateTime = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  });
};

// 获取物料总数量
const getTotalQuantity = () => {
  if (!outboundDetail.value.items || outboundDetail.value.items.length === 0) {
    return 0;
  }
  
  return outboundDetail.value.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
};

// 复制物流单号
const copyLogisticsCode = () => {
  if (!outboundDetail.value.trackingNumber) {
    uni.showToast({
      title: '暂无物流单号',
      icon: 'none'
    });
    return;
  }

  uni.setClipboardData({
    data: outboundDetail.value.trackingNumber,
    success: () => {
      uni.showToast({
        title: '复制成功'
      });
    }
  });
};

// 显示取消确认弹窗
const showCancelConfirm = () => {
  cancelReason.value = '';
  showCancelDialog.value = true;
};

// 关闭取消确认弹窗
const closeCancelPopup = () => {
  showCancelDialog.value = false;
};

// 确认取消申请
const confirmCancel = async () => {
  uni.showLoading({
    title: '取消申请中...'
  });
  
  try {
    await cancelMaterialOutbound({
      id: outboundDetail.value.id,
      cancelReason: cancelReason.value
    });
    
    // 刷新详情
    await fetchOutboundDetail();
    
    uni.showToast({
      title: '取消申请成功'
    });
  } catch (err) {
    console.error('取消申请失败', err);
    uni.showToast({
      title: '取消申请失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
    showCancelDialog.value = false;
  }
};

// 显示确认收货弹窗
const showConfirmReceiptConfirm = () => {
  showConfirmReceiptDialog.value = true;
};

// 关闭确认收货弹窗
const closeConfirmReceiptPopup = () => {
  showConfirmReceiptDialog.value = false;
};

// 确认收货
const confirmReceipt = async () => {
  uni.showLoading({
    title: '确认收货中...'
  });
  
  try {
    await confirmMaterialOutbound(outboundDetail.value.id);
    
    // 刷新详情
    await fetchOutboundDetail();
    
    uni.showToast({
      title: '确认收货成功'
    });
  } catch (err) {
    console.error('确认收货失败', err);
    uni.showToast({
      title: '确认收货失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
    showConfirmReceiptDialog.value = false;
  }
};

onMounted(() => {
  // 加载出库申请详情
  fetchOutboundDetail();
});
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 40rpx;
}

// 状态卡片样式
.status-card {
  background-color: #fff;
  padding: 30rpx;
  display: flex;
  align-items: center;
  z-index: 10;
  
  .status-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    
    &.status-pending {
      background-color: #ff9800;
    }
    
    &.status-approved {
      background-color: #2196f3;
    }
    
    &.status-shipped {
      background-color: #8f1911;
    }
    
    &.status-completed {
      background-color: #4caf50;
    }
    
    &.status-canceled {
      background-color: #9e9e9e;
    }
  }
  
  .status-info {
    .status-text {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 10rpx;
      display: block;
    }
    
    .status-desc {
      font-size: 26rpx;
      color: #666;
    }
  }
}

// 状态卡片间隔
.status-spacer {
  height: 120rpx;
}

// 卡片通用样式
.card {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    border-left: 6rpx solid #8f1911;
    padding-left: 15rpx;
  }
}

// 物流卡片样式
.logistics-card {
  .logistics-info {
    position: relative;
    
    .info-item {
      display: flex;
      margin-bottom: 15rpx;
      
      .item-label {
        width: 150rpx;
        font-size: 28rpx;
        color: #666;
      }
      
      .item-value {
        flex: 1;
        font-size: 28rpx;
        color: #333;
      }
    }
    
    .copy-btn {
      position: absolute;
      right: 0;
      top: 0;
      padding: 10rpx 20rpx;
      background-color: #f0f0f0;
      border-radius: 30rpx;
      font-size: 24rpx;
      color: #666;
    }
  }
}

// 地址卡片样式
.address-card {
  .address-info {
    .address-header {
      display: flex;
      align-items: center;
      margin-bottom: 15rpx;
      
      .address-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-right: 20rpx;
      }
      
      .address-mobile {
        font-size: 28rpx;
        color: #666;
      }
    }
    
    .address-detail {
      font-size: 28rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

// 物料卡片样式
.material-card {
  .material-list {
    margin-bottom: 20rpx;
    
    .material-item {
      display: flex;
      align-items: center;
      padding: 15rpx 0;
      border-bottom: 1rpx solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      .material-image {
        width: 100rpx;
        height: 100rpx;
        border-radius: 8rpx;
        background-color: #f5f5f5;
        object-fit: cover;
      }
      
      .material-info {
        flex: 1;
        margin-left: 20rpx;
        
        .material-name {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 8rpx;
          display: block;
        }
        
        .material-id {
          font-size: 24rpx;
          color: #999;
        }
      }
      
      .material-quantity {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
  
  .material-total {
    font-size: 26rpx;
    color: #666;
    text-align: right;
  }
}

// 备注卡片样式
.remark-card {
  .remark-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.5;
  }
}

// 订单信息卡片样式
.order-card {
  .order-info {
    .info-item {
      display: flex;
      margin-bottom: 15rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .item-label {
        width: 150rpx;
        font-size: 28rpx;
        color: #666;
      }
      
      .item-value {
        flex: 1;
        font-size: 28rpx;
        color: #333;
      }
    }
  }
}

// 底部操作按钮
.bottom-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .action-btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 40rpx;
    font-size: 28rpx;
    margin: 0 20rpx;
    
    &.cancel-btn {
      background-color: #f0f0f0;
      color: #666;
    }
    
    &.confirm-btn {
      background-color: #8f1911;
      color: #fff;
    }
  }
}

// 底部安全区
.safe-area-bottom {
  height: 120rpx;
}

// 取消原因输入框
.cancel-reason {
  width: 100%;
  height: 100rpx;
  border: 1px solid #eee;
  border-radius: 8rpx;
  padding: 20rpx;
  margin-top: 20rpx;
  font-size: 28rpx;
}
</style> 