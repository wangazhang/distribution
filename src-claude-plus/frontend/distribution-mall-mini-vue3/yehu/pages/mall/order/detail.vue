<template>
  <view class="order-detail-container">
    <!-- 自定义导航栏 -->
    <u-navbar
      title="订单详情"
      :background="{ background: '#ffffff' }"
      :border-bottom="true"
      back-icon-color="#333333"
    ></u-navbar>
    
    <!-- 订单状态 -->
    <view class="order-status" :class="{ 'status-canceled': orderDetail.status === 'CANCELED' }">
      <view class="status-icon">
        <u-icon 
          :name="getStatusIcon(orderDetail.status)"
          size="60" 
          :color="orderDetail.status === 'CANCELED' ? '#999999' : '#ffffff'"
        ></u-icon>
      </view>
      <view class="status-info">
        <view class="status-text">{{ orderDetail.statusText }}</view>
        <view class="status-desc">{{ getStatusDescription(orderDetail.status) }}</view>
      </view>
    </view>
    
    <!-- 收货信息 -->
    <view class="order-card address-card">
      <view class="card-header">
        <u-icon name="map-fill" size="36" color="#999999"></u-icon>
        <text>收货信息</text>
      </view>
      <view class="address-content">
        <view class="address-user">
          <text>{{ orderDetail.address?.name }}</text>
          <text>{{ orderDetail.address?.phone }}</text>
        </view>
        <view class="address-detail">
          {{ orderDetail.address?.province }}{{ orderDetail.address?.city }}{{ orderDetail.address?.district }}{{ orderDetail.address?.detail }}
        </view>
      </view>
    </view>
    
    <!-- 商品信息 -->
    <view class="order-card">
      <view class="card-header">
        <u-icon name="shopping-cart" size="36" color="#999999"></u-icon>
        <text>商品信息</text>
      </view>
      <view 
        class="product-item" 
        v-for="(product, index) in orderDetail.products" 
        :key="index"
      >
        <image class="product-image" :src="product.image" mode="aspectFill"></image>
        <view class="product-info">
          <view class="product-name">{{ product.name }}</view>
          <view class="product-spec">{{ product.spec }}</view>
          <view class="price-count">
            <text class="product-price">¥{{ product.price }}</text>
            <text class="product-count">x{{ product.count }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 订单信息 -->
    <view class="order-card">
      <view class="card-header">
        <u-icon name="file-text" size="36" color="#999999"></u-icon>
        <text>订单信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="info-label">订单编号</text>
          <view class="info-value copy-box">
            <text>{{ orderDetail.orderNo }}</text>
            <view class="copy-btn" @tap="copyOrderNo">复制</view>
          </view>
        </view>
        <view class="info-item">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ orderDetail.createTime }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.payTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ orderDetail.payTime }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.deliverTime">
          <text class="info-label">发货时间</text>
          <text class="info-value">{{ orderDetail.deliverTime }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.completeTime">
          <text class="info-label">完成时间</text>
          <text class="info-value">{{ orderDetail.completeTime }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">支付方式</text>
          <text class="info-value">{{ orderDetail.payType || '未支付' }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.remark">
          <text class="info-label">订单备注</text>
          <text class="info-value">{{ orderDetail.remark }}</text>
        </view>
      </view>
    </view>
    
    <!-- 金额信息 -->
    <view class="order-card">
      <view class="card-header">
        <u-icon name="rmb-circle" size="36" color="#999999"></u-icon>
        <text>金额明细</text>
      </view>
      <view class="price-list">
        <view class="price-item">
          <text class="price-label">商品金额</text>
          <text class="price-value">¥{{ orderDetail.goodsAmount }}</text>
        </view>
        <view class="price-item">
          <text class="price-label">运费</text>
          <text class="price-value">¥{{ orderDetail.freight }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.couponAmount">
          <text class="price-label">优惠券</text>
          <text class="price-value">-¥{{ orderDetail.couponAmount }}</text>
        </view>
        <view class="price-item total">
          <text class="price-label">实付金额</text>
          <text class="price-value">¥{{ orderDetail.totalAmount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 物流信息 -->
    <view class="order-card" v-if="orderDetail.logistics && orderDetail.status !== 'WAIT_PAY' && orderDetail.status !== 'CANCELED'">
      <view class="card-header">
        <u-icon name="car" size="36" color="#999999"></u-icon>
        <text>物流信息</text>
      </view>
      <view class="logistics-content">
        <view class="logistics-company">
          <text class="company-label">物流公司：</text>
          <text class="company-value">{{ orderDetail.logistics.company }}</text>
        </view>
        <view class="logistics-number">
          <text class="number-label">物流单号：</text>
          <text class="number-value">{{ orderDetail.logistics.number }}</text>
          <view class="copy-btn" @tap="copyTrackingNo">复制</view>
        </view>
        <view class="logistics-status">
          <view class="status-item">
            <view class="status-time">{{ orderDetail.logistics.updates[0].time }}</view>
            <view class="status-desc">{{ orderDetail.logistics.updates[0].desc }}</view>
          </view>
        </view>
        <view class="more-btn" @tap="viewLogistics(orderDetail.id)">查看物流详情</view>
      </view>
    </view>
    
    <!-- 底部按钮区域 -->
    <view class="order-actions">
      <template v-if="orderDetail.status === 'WAIT_PAY'">
        <view class="action-buttons">
          <view class="cancel-btn" @tap="cancelOrder(orderDetail.id)">取消订单</view>
          <view class="pay-btn" @tap="payOrder(orderDetail.id)">去支付</view>
        </view>
      </template>
      
      <template v-if="orderDetail.status === 'WAIT_DELIVER'">
        <view class="action-buttons">
          <view class="cancel-btn" @tap="contactService">联系客服</view>
        </view>
      </template>
      
      <template v-if="orderDetail.status === 'WAIT_RECEIVE'">
        <view class="action-buttons">
          <view class="cancel-btn" @tap="viewLogistics(orderDetail.id)">查看物流</view>
          <view class="pay-btn" @tap="confirmReceive(orderDetail.id)">确认收货</view>
        </view>
      </template>
      
      <template v-if="orderDetail.status === 'COMPLETED'">
        <view class="action-buttons">
          <view class="cancel-btn" @tap="deleteOrder(orderDetail.id)">删除订单</view>
          <view class="pay-btn" @tap="reviewOrder(orderDetail.id)" v-if="!orderDetail.isReviewed">评价</view>
          <view class="pay-btn" @tap="buyAgain(orderDetail.id)">再次购买</view>
        </view>
      </template>
      
      <template v-if="orderDetail.status === 'CANCELED'">
        <view class="action-buttons">
          <view class="cancel-btn" @tap="deleteOrder(orderDetail.id)">删除订单</view>
          <view class="pay-btn" @tap="buyAgain(orderDetail.id)">再次购买</view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup >
import { ref, reactive, onMounted } from 'vue';

// 订单ID
const orderId = ref('');

// 订单详情数据
const orderDetail = reactive({
  id: '',
  orderNo: '',
  status: 'WAIT_PAY',
  statusText: '待付款',
  createTime: '',
  payTime: '',
  deliverTime: '',
  completeTime: '',
  goodsAmount: '0.00',
  freight: '0.00',
  couponAmount: '0.00',
  totalAmount: '0.00',
  payType: '',
  remark: '',
  isReviewed: false,
  address: {
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: ''
  },
  products: [],
  logistics: null,
});

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case 'WAIT_PAY':
      return 'red-packet';
    case 'WAIT_DELIVER':
      return 'staff';
    case 'WAIT_RECEIVE':
      return 'car';
    case 'COMPLETED':
      return 'checkmark-circle';
    case 'CANCELED':
      return 'close-circle';
    default:
      return 'checkmark-circle';
  }
};

// 获取状态描述
const getStatusDescription = (status) => {
  switch (status) {
    case 'WAIT_PAY':
      return '请尽快完成支付';
    case 'WAIT_DELIVER':
      return '商家正在处理订单';
    case 'WAIT_RECEIVE':
      return '商品已发出，请注意查收';
    case 'COMPLETED':
      return '订单已完成';
    case 'CANCELED':
      return '订单已取消';
    default:
      return '';
  }
};

// 接收页面参数
const onLoad = (options) => {
  console.log('订单详情页面参数:', options);
  if (options.id) {
    orderId.value = options.id;
    loadOrderDetail(options.id);
  }
};

// 加载订单详情
const loadOrderDetail = (id) => {
  // 模拟接口调用，获取订单详情
  setTimeout(() => {
    // 根据订单ID匹配不同订单状态的数据
    switch (id) {
      case '1': // 待付款
        Object.assign(orderDetail, {
          id: '1',
          orderNo: '202403150001',
          status: 'WAIT_PAY',
          statusText: '待付款',
          createTime: '2024-03-15 14:30:00',
          goodsAmount: '2400.00',
          freight: '0.00',
          totalAmount: '2400.00',
          remark: '请尽快发货，谢谢',
          address: {
            name: '张三',
            phone: '138****1234',
            province: '上海市',
            city: '上海市',
            district: '浦东新区',
            detail: '张江高科技园区博云路2号'
          },
          products: [
            {
              id: '1',
              name: 'MEETHER 居家胶原美塑',
              spec: '1盒5支装',
              price: '2400.00',
              count: 1,
              image: 'https://cdn.example.com/static/pic/banner/index/1.png'
            }
          ]
        });
        break;
      case '2': // 待发货
        Object.assign(orderDetail, {
          id: '2',
          orderNo: '202403140002',
          status: 'WAIT_DELIVER',
          statusText: '待发货',
          createTime: '2024-03-14 10:15:00',
          payTime: '2024-03-14 10:20:30',
          goodsAmount: '4800.00',
          freight: '0.00',
          totalAmount: '4800.00',
          payType: '微信支付',
          address: {
            name: '李四',
            phone: '159****5678',
            province: '北京市',
            city: '北京市',
            district: '海淀区',
            detail: '中关村科技园区海淀大街甲28号'
          },
          products: [
            {
              id: '2',
              name: 'MEETHER 居家胶原美塑',
              spec: '1盒5支装',
              price: '2400.00',
              count: 2,
              image: 'https://cdn.example.com/static/pic/banner/index/2.png'
            }
          ]
        });
        break;
      case '3': // 待收货
        Object.assign(orderDetail, {
          id: '3',
          orderNo: '202403130003',
          status: 'WAIT_RECEIVE',
          statusText: '待收货',
          createTime: '2024-03-13 16:45:00',
          payTime: '2024-03-13 16:50:20',
          deliverTime: '2024-03-14 10:30:00',
          goodsAmount: '2400.00',
          freight: '0.00',
          totalAmount: '2400.00',
          payType: '微信支付',
          address: {
            name: '王五',
            phone: '139****4321',
            province: '广州市',
            city: '广州市',
            district: '天河区',
            detail: '天河路385号太古汇'
          },
          products: [
            {
              id: '3',
              name: 'MEETHER 居家胶原美塑',
              spec: '1盒5支装',
              price: '2400.00',
              count: 1,
              image: 'https://cdn.example.com/static/pic/banner/index/3.png'
            }
          ],
          logistics: {
            company: '顺丰速运',
            number: 'SF1234567890',
            updates: [
              {
                time: '2024-03-14 15:30:00',
                desc: '【广州市】快件已发出，预计明日送达'
              }
            ]
          }
        });
        break;
      case '4': // 已完成
        Object.assign(orderDetail, {
          id: '4',
          orderNo: '202403120004',
          status: 'COMPLETED',
          statusText: '已完成',
          createTime: '2024-03-12 09:20:00',
          payTime: '2024-03-12 09:25:10',
          deliverTime: '2024-03-12 14:30:00',
          completeTime: '2024-03-14 18:45:00',
          goodsAmount: '2400.00',
          freight: '0.00',
          totalAmount: '2400.00',
          payType: '微信支付',
          isReviewed: true,
          address: {
            name: '赵六',
            phone: '136****8765',
            province: '杭州市',
            city: '杭州市',
            district: '西湖区',
            detail: '西湖文化广场B座1203室'
          },
          products: [
            {
              id: '4',
              name: 'MEETHER 居家胶原美塑',
              spec: '1盒5支装',
              price: '2400.00',
              count: 1,
              image: 'https://cdn.example.com/static/pic/banner/index/4.png'
            }
          ],
          logistics: {
            company: '圆通速递',
            number: 'YT9876543210',
            updates: [
              {
                time: '2024-03-14 18:45:00',
                desc: '【杭州市】已签收，签收人：本人'
              }
            ]
          }
        });
        break;
      case '5': // 已取消
        Object.assign(orderDetail, {
          id: '5',
          orderNo: '202403110005',
          status: 'CANCELED',
          statusText: '已取消',
          createTime: '2024-03-11 18:05:00',
          goodsAmount: '2400.00',
          freight: '0.00',
          totalAmount: '2400.00',
          address: {
            name: '钱七',
            phone: '135****2468',
            province: '深圳市',
            city: '深圳市',
            district: '南山区',
            detail: '科技园南区T3栋502室'
          },
          products: [
            {
              id: '5',
              name: 'MEETHER 居家胶原美塑',
              spec: '1盒5支装',
              price: '2400.00',
              count: 1,
              image: 'https://cdn.example.com/static/pic/banner/index/1.png'
            }
          ]
        });
        break;
      default:
        break;
    }
  }, 500);
};

// 复制订单号
const copyOrderNo = () => {
  uni.setClipboardData({
    data: orderDetail.orderNo,
    success: () => {
      uni.showToast({
        title: '订单号已复制',
        icon: 'success'
      });
    }
  });
};

// 复制物流单号
const copyTrackingNo = () => {
  if (orderDetail.logistics && orderDetail.logistics.number) {
    uni.setClipboardData({
      data: orderDetail.logistics.number,
      success: () => {
        uni.showToast({
          title: '物流单号已复制',
          icon: 'success'
        });
      }
    });
  }
};

// 取消订单
const cancelOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        // 更新订单状态
        orderDetail.status = 'CANCELED';
        orderDetail.statusText = '已取消';
        
        uni.showToast({
          title: '订单已取消',
          icon: 'success'
        });
      }
    }
  });
};

// 去支付
const payOrder = (orderId) => {
  uni.navigateTo({
    url: `/pages/payment/index?orderId=${orderId}`
  });
};

// 查看物流
const viewLogistics = (orderId) => {
  uni.navigateTo({
    url: `/pages/logistics/index?orderId=${orderId}`
  });
};

// 确认收货
const confirmReceive = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确认已收到商品吗？',
    success: (res) => {
      if (res.confirm) {
        // 更新订单状态
        orderDetail.status = 'COMPLETED';
        orderDetail.statusText = '已完成';
        orderDetail.completeTime = new Date().toLocaleString();
        
        uni.showToast({
          title: '已确认收货',
          icon: 'success'
        });
      }
    }
  });
};

// 删除订单
const deleteOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该订单吗？删除后将无法恢复',
    success: (res) => {
      if (res.confirm) {
        // 删除订单，返回上一页
        uni.showToast({
          title: '订单已删除',
          icon: 'success',
          success: () => {
            setTimeout(() => {
              uni.navigateBack();
            }, 1000);
          }
        });
      }
    }
  });
};

// 评价订单
const reviewOrder = (orderId) => {
  uni.navigateTo({
    url: `/pages/review/index?orderId=${orderId}`
  });
};

// 再次购买
const buyAgain = (orderId) => {
  // 将商品添加到购物车
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  });
};

// 联系客服
const contactService = () => {
  uni.showToast({
    title: '联系客服功能暂未开放',
    icon: 'none'
  });
};

onMounted(() => {
  // 页面挂载后的初始化操作
});
</script>

<style lang="scss" scoped>
.order-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.order-status {
  display: flex;
  align-items: center;
  background-color: #c82436;
  padding: 30rpx;
  color: #ffffff;
  
  &.status-canceled {
    background-color: #999999;
  }
  
  .status-icon {
    margin-right: 30rpx;
  }
  
  .status-info {
    flex: 1;
    
    .status-text {
      font-size: 36rpx;
      font-weight: 600;
      margin-bottom: 10rpx;
    }
    
    .status-desc {
      font-size: 26rpx;
      opacity: 0.8;
    }
  }
}

.order-card {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin: 20rpx;
  overflow: hidden;
  
  .card-header {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    text {
      font-size: 30rpx;
      font-weight: 500;
      color: #333333;
      margin-left: 10rpx;
    }
  }
}

.address-card {
  .address-content {
    padding: 20rpx 30rpx;
    
    .address-user {
      display: flex;
      margin-bottom: 10rpx;
      
      text {
        font-size: 30rpx;
        color: #333333;
        margin-right: 20rpx;
      }
    }
    
    .address-detail {
      font-size: 28rpx;
      color: #666666;
      line-height: 1.5;
    }
  }
}

.product-item {
  display: flex;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .product-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 8rpx;
    margin-right: 20rpx;
    background-color: #f9f9f9;
  }
  
  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    
    .product-name {
      font-size: 28rpx;
      color: #333333;
      margin-bottom: 8rpx;
    }
    
    .product-spec {
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 10rpx;
    }
    
    .price-count {
      display: flex;
      justify-content: space-between;
      
      .product-price {
        font-size: 28rpx;
        color: #c82436;
        font-weight: 500;
      }
      
      .product-count {
        font-size: 26rpx;
        color: #999999;
      }
    }
  }
}

.info-list, .price-list {
  padding: 10rpx 30rpx;
  
  .info-item, .price-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    font-size: 28rpx;
    
    .info-label, .price-label {
      color: #666666;
    }
    
    .info-value, .price-value {
      color: #333333;
    }
    
    .copy-box {
      display: flex;
      align-items: center;
      
      .copy-btn {
        font-size: 24rpx;
        color: #3399ff;
        margin-left: 10rpx;
      }
    }
    
    &.total {
      margin-top: 10rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      
      .price-label, .price-value {
        font-size: 30rpx;
        font-weight: 500;
        color: #c82436;
      }
    }
  }
}

.logistics-content {
  padding: 20rpx 30rpx;
  
  .logistics-company, .logistics-number {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    margin-bottom: 16rpx;
    
    .company-label, .number-label {
      color: #666666;
    }
    
    .company-value, .number-value {
      color: #333333;
    }
    
    .copy-btn {
      font-size: 24rpx;
      color: #3399ff;
      margin-left: 10rpx;
    }
  }
  
  .logistics-status {
    padding: 20rpx 0;
    
    .status-item {
      position: relative;
      padding-left: 30rpx;
      
      &::before {
        content: '';
        position: absolute;
        left: 10rpx;
        top: 8rpx;
        width: 10rpx;
        height: 10rpx;
        background-color: #c82436;
        border-radius: 50%;
      }
      
      .status-time {
        font-size: 24rpx;
        color: #999999;
        margin-bottom: 6rpx;
      }
      
      .status-desc {
        font-size: 28rpx;
        color: #333333;
      }
    }
  }
  
  .more-btn {
    width: 100%;
    height: 70rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #666666;
    border-top: 1rpx solid #f0f0f0;
    margin-top: 10rpx;
    padding-top: 10rpx;
  }
}

.order-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 99;
  
  .action-buttons {
    display: flex;
    flex: 1;
    justify-content: flex-end;
    
    .cancel-btn, .pay-btn {
      min-width: 180rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      font-weight: 500;
      border-radius: 40rpx;
      margin-left: 20rpx;
    }
    
    .cancel-btn {
      background-color: rgba(200, 36, 54, 0.1);
      color: #c82436;
      border: 1rpx solid #c82436;
    }
    
    .pay-btn {
      background-color: #c82436;
      color: #ffffff;
    }
  }
}
</style> 