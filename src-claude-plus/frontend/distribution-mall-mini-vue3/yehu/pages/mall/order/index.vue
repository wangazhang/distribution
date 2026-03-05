<template>
  <view class="order-container">
    <!-- 固定区域包装器 -->
    <view class="fixed-wrapper">
      <!-- 自定义导航栏 -->
      <u-navbar
        title="我的订单"
        :background="{ background: '#ffffff' }"
        :border-bottom="true"
        back-icon-color="#333333"
      ></u-navbar>
      
      <!-- 筛选区域 -->
      <view class="filter-section">
        <!-- 日期筛选区域 -->
        <view class="date-filter">
          <!-- 顶部时间筛选和日期选择器 - 改为一行显示 -->
          <view class="filter-header">
            <view class="date-dropdown" @tap="toggleDateDropdown">
              <text>{{ selectedDateRange }}</text>
              <u-icon name="arrow-down" size="24" color="#333333"></u-icon>
            </view>
            
            <!-- 日期选择器 -->
            <view class="date-picker-box">
              <view class="date-picker" @tap="toggleStartDatePicker">
                <text class="date-placeholder" v-if="!startDate">年/月/日</text>
                <text v-else>{{ formatDate(startDate) }}</text>
                <u-icon name="calendar" size="24" color="#999999"></u-icon>
              </view>
              
              <text class="date-separator">-</text>
              
              <view class="date-picker" @tap="toggleEndDatePicker">
                <text class="date-placeholder" v-if="!endDate">年/月/日</text>
                <text v-else>{{ formatDate(endDate) }}</text>
                <u-icon name="calendar" size="24" color="#999999"></u-icon>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 搜索框 -->
        <view class="search-section">
          <view class="search-box">
            <u-icon name="search" size="36" color="#999999"></u-icon>
            <input 
              type="text" 
              class="search-input" 
              placeholder="商品名称/订单编号/收货人" 
              confirm-type="search"
              v-model="searchKeyword"
              @confirm="search"
            />
          </view>
          <view class="action-btns">
            <view class="search-btn" @tap="search">搜索</view>
            <u-icon name="eye-off" size="36" color="#333333" @tap="toggleHideStatus"></u-icon>
          </view>
        </view>
      </view>
      
      <!-- 订单状态栏 -->
      <view class="order-tabs">
        <view 
          class="tab-item" 
          v-for="(tab, index) in tabs" 
          :key="index"
          :class="{ active: currentTab === index }"
          @tap="switchTab(index)"
        >
          <text>{{ tab.name }}</text>
          <view class="tab-line" v-if="currentTab === index"></view>
        </view>
      </view>
    </view>
    
    <!-- 占位元素，防止内容被固定区域遮挡 -->
    <view class="fixed-placeholder"></view>
    
    <!-- 订单列表 -->
    <view class="order-list" v-if="filteredOrders.length > 0">
      <view class="order-item" v-for="(order, index) in filteredOrders" :key="index">
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="order-no">订单号：{{ order.orderNo }}</view>
          <view class="order-status" :class="order.statusClass">{{ order.statusText }}</view>
        </view>
        
        <!-- 订单商品 -->
        <view 
          class="order-product" 
          v-for="(product, pIndex) in order.products" 
          :key="pIndex"
          @tap="goToOrderDetail(order.id)"
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
        
        <!-- 订单汇总 -->
        <view class="order-summary">
          <view class="order-time">{{ order.createTime }}</view>
          <view class="order-total">
            <text>共{{ getTotalCount(order.products) }}件商品</text>
            <text>合计：</text>
            <text class="total-price">¥{{ order.totalAmount }}</text>
            <text>(含运费¥{{ order.freight }})</text>
          </view>
        </view>
        
        <!-- 订单按钮 -->
        <view class="order-actions">
          <template v-if="order.status === 'WAIT_PAY'">
            <u-button 
              type="default" 
              size="mini" 
              shape="circle"
              @click.stop="cancelOrder(order.id)"
            >取消订单</u-button>
            <u-button 
              type="primary" 
              size="mini" 
              shape="circle"
              @click.stop="payOrder(order.id)"
            >去支付</u-button>
          </template>
          
          <template v-if="order.status === 'WAIT_DELIVER'">
            <u-button 
              type="default" 
              size="mini" 
              shape="circle"
              @click.stop="viewLogistics(order.id)"
            >查看物流</u-button>
          </template>
          
          <template v-if="order.status === 'WAIT_RECEIVE'">
            <u-button 
              type="default" 
              size="mini" 
              shape="circle"
              @click.stop="viewLogistics(order.id)"
            >查看物流</u-button>
            <u-button 
              type="primary" 
              size="mini" 
              shape="circle"
              @click.stop="confirmReceive(order.id)"
            >确认收货</u-button>
          </template>
          
          <template v-if="order.status === 'COMPLETED'">
            <u-button 
              type="default" 
              size="mini" 
              shape="circle"
              @click.stop="deleteOrder(order.id)"
            >删除订单</u-button>
            <u-button 
              type="primary" 
              size="mini" 
              shape="circle"
              @click.stop="reviewOrder(order.id)"
            >评价</u-button>
          </template>
          
          <template v-if="order.status === 'CANCELED'">
            <u-button 
              type="default" 
              size="mini" 
              shape="circle"
              @click.stop="deleteOrder(order.id)"
            >删除订单</u-button>
          </template>
        </view>
      </view>
    </view>
    
    <!-- 空订单状态 -->
    <view class="empty-order" v-else>
      <u-empty text="暂无相关订单" mode="order"></u-empty>
    </view>
    
    <!-- 日期选择弹出框 -->
    <u-picker 
      mode="time" 
      v-model="showStartDatePicker" 
      :params="dateParams"
      @confirm="confirmStartDate"
    ></u-picker>
    
    <u-picker 
      mode="time" 
      v-model="showEndDatePicker" 
      :params="dateParams"
      @confirm="confirmEndDate"
    ></u-picker>
    
    <!-- 日期范围下拉菜单 -->
    <u-popup v-model="showDateDropdown" mode="bottom">
      <view class="date-dropdown-modal">
        <view class="modal-header">
          <text class="modal-title">时间筛选</text>
          <u-icon name="close" size="30" @tap="showDateDropdown = false"></u-icon>
        </view>
        
        <view class="modal-body">
          <view class="date-options">
            <view 
              class="date-option-item" 
              v-for="(item, index) in dateRanges" 
              :key="index"
              :class="{ active: selectedDateRangeIndex === index }"
              @tap="selectDateRange(item)"
            >
              <text>{{ item.name }}</text>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="reset-btn" @tap="resetDateRange">重置</view>
          <view class="confirm-btn" @tap="confirmDateRange">确认</view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup >
import { ref, computed, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

// 订单状态栏
const tabs = ref([
  { name: '全部', value: 'ALL' },
  { name: '待付款', value: 'WAIT_PAY' },
  { name: '待发货', value: 'WAIT_DELIVER' },
  { name: '待收货', value: 'WAIT_RECEIVE' },
  { name: '已完成', value: 'COMPLETED' }
]);

// 当前选中的标签
const currentTab = ref(0);

// 搜索关键词
const searchKeyword = ref('');

// 日期选择相关
const showStartDatePicker = ref(false);
const showEndDatePicker = ref(false);
const startDate = ref('');
const endDate = ref('');
const dateParams = {
  year: true,
  month: true,
  day: true,
  hour: false,
  minute: false,
  second: false
};

// 日期范围下拉菜单
const showDateDropdown = ref(false);
const selectedDateRangeIndex = ref(0);
const dateRanges = [
  { name: '全部时间', value: 'ALL' },
  { name: '今天', value: 'TODAY' },
  { name: '本周', value: 'THIS_WEEK' },
  { name: '本月', value: 'THIS_MONTH' },
  { name: '上月', value: 'LAST_MONTH' },
  { name: '今年', value: 'THIS_YEAR' },
  { name: '去年', value: 'LAST_YEAR' }
];

// 获取选中的日期范围名称
const selectedDateRange = computed(() => {
  return dateRanges[selectedDateRangeIndex.value].name;
});

// 订单列表数据
const orderList = ref([
  {
    id: '1',
    orderNo: '202403150001',
    status: 'WAIT_PAY',
    statusText: '待付款',
    statusClass: 'status-wait-pay',
    createTime: '2024-03-15 14:30',
    totalAmount: '2400.00',
    freight: '0.00',
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
  },
  {
    id: '2',
    orderNo: '202403140002',
    status: 'WAIT_DELIVER',
    statusText: '待发货',
    statusClass: 'status-wait-deliver',
    createTime: '2024-03-14 10:15',
    totalAmount: '4800.00',
    freight: '0.00',
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
  },
  {
    id: '3',
    orderNo: '202403130003',
    status: 'WAIT_RECEIVE',
    statusText: '待收货',
    statusClass: 'status-wait-receive',
    createTime: '2024-03-13 16:45',
    totalAmount: '2400.00',
    freight: '0.00',
    products: [
      {
        id: '3',
        name: 'MEETHER 居家胶原美塑',
        spec: '1盒5支装',
        price: '2400.00',
        count: 1,
        image: 'https://cdn.example.com/static/pic/banner/index/3.png'
      }
    ]
  },
  {
    id: '4',
    orderNo: '202403120004',
    status: 'COMPLETED',
    statusText: '已完成',
    statusClass: 'status-completed',
    createTime: '2024-03-12 09:20',
    totalAmount: '2400.00',
    freight: '0.00',
    products: [
      {
        id: '4',
        name: 'MEETHER 居家胶原美塑',
        spec: '1盒5支装',
        price: '2400.00',
        count: 1,
        image: 'https://cdn.example.com/static/pic/banner/index/4.png'
      }
    ]
  },
  {
    id: '5',
    orderNo: '202403110005',
    status: 'CANCELED',
    statusText: '已取消',
    statusClass: 'status-canceled',
    createTime: '2024-03-11 18:05',
    totalAmount: '2400.00',
    freight: '0.00',
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
  }
]);

// 过滤订单列表
const filterOrders = computed(() => {
  if (currentTab.value === 0) {
    return orderList.value;
  } else {
    const status = tabs.value[currentTab.value].value;
    return orderList.value.filter(order => order.status === status);
  }
});

// 根据搜索和日期过滤后的订单
const filteredOrders = computed(() => {
  let result = filterOrders.value;
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(order => {
      // 订单号搜索
      if (order.orderNo.toLowerCase().includes(keyword)) return true;
      
      // 商品名称搜索
      const hasMatchProduct = order.products.some(product => 
        product.name.toLowerCase().includes(keyword)
      );
      if (hasMatchProduct) return true;
      
      return false;
    });
  }
  
  // 日期筛选
  if (startDate.value && endDate.value) {
    const start = new Date(startDate.value).getTime();
    const end = new Date(endDate.value).getTime();
    
    result = result.filter(order => {
      const orderTime = new Date(order.createTime).getTime();
      return orderTime >= start && orderTime <= end;
    });
  }
  
  return result;
});

// 切换标签
const switchTab = (index) => {
  currentTab.value = index;
};

// 获取商品总数量
const getTotalCount = (products) => {
  return products.reduce((total, item) => total + item.count, 0);
};

// 跳转到订单详情
const goToOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/mall/order/detail?id=${orderId}`
  });
};

// 取消订单
const cancelOrder = (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        // 更新订单状态
        const order = orderList.value.find(item => item.id === orderId);
        if (order) {
          order.status = 'CANCELED';
          order.statusText = '已取消';
          order.statusClass = 'status-canceled';
        }
        
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
    url: `/pages/mall/payment/index?orderId=${orderId}`
  });
};

// 查看物流
const viewLogistics = (orderId) => {
  uni.navigateTo({
    url: `/pages/mall/logistics/index?orderId=${orderId}`
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
        const order = orderList.value.find(item => item.id === orderId);
        if (order) {
          order.status = 'COMPLETED';
          order.statusText = '已完成';
          order.statusClass = 'status-completed';
        }
        
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
        // 删除订单
        const index = orderList.value.findIndex(item => item.id === orderId);
        if (index !== -1) {
          orderList.value.splice(index, 1);
        }
        
        uni.showToast({
          title: '订单已删除',
          icon: 'success'
        });
      }
    }
  });
};

// 评价订单
const reviewOrder = (orderId) => {
  uni.navigateTo({
    url: `/pages/mall/review/index?orderId=${orderId}`
  });
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`;
};

// 显示开始日期选择器
const toggleStartDatePicker = () => {
  showStartDatePicker.value = true;
};

// 显示结束日期选择器
const toggleEndDatePicker = () => {
  showEndDatePicker.value = true;
};

// 确认开始日期
const confirmStartDate = (e) => {
  startDate.value = `${e.year}-${e.month}-${e.day}`;
};

// 确认结束日期
const confirmEndDate = (e) => {
  endDate.value = `${e.year}-${e.month}-${e.day}`;
};

// 切换日期下拉菜单
const toggleDateDropdown = () => {
  showDateDropdown.value = !showDateDropdown.value;
};

// 选择日期范围
const selectDateRange = (item) => {
  const index = dateRanges.findIndex(range => range.value === item.value);
  if (index !== -1) {
    selectedDateRangeIndex.value = index;
    
    // 根据选择设置日期范围
    const now = new Date();
    const end = formatDate(now.toISOString());
    
    switch (item.value) {
      case 'TODAY':
        now.setHours(0, 0, 0, 0);
        startDate.value = formatDate(now.toISOString());
        endDate.value = end;
        break;
      case 'THIS_WEEK':
        const startOfWeek = new Date(now);
        startOfWeek.setDate(now.getDate() - now.getDay());
        startDate.value = formatDate(startOfWeek.toISOString());
        endDate.value = end;
        break;
      case 'THIS_MONTH':
        now.setDate(1);
        startDate.value = formatDate(now.toISOString());
        endDate.value = end;
        break;
      case 'LAST_MONTH':
        now.setMonth(now.getMonth() - 1);
        now.setDate(1);
        startDate.value = formatDate(now.toISOString());
        endDate.value = end;
        break;
      case 'THIS_YEAR':
        now.setMonth(0, 1);
        startDate.value = formatDate(now.toISOString());
        endDate.value = end;
        break;
      case 'LAST_YEAR':
        now.setFullYear(now.getFullYear() - 1);
        startDate.value = formatDate(now.toISOString());
        endDate.value = end;
        break;
      default:
        startDate.value = '';
        endDate.value = '';
    }
  }
  
  showDateDropdown.value = false;
};

// 搜索订单
const search = () => {
  console.log('搜索关键词:', searchKeyword.value);
  // 搜索逻辑已在computed中处理
};

// 切换隐藏状态
const toggleHideStatus = () => {
  uni.showToast({
    title: '切换显示/隐藏订单',
    icon: 'none'
  });
};

// 重置日期范围
const resetDateRange = () => {
  selectedDateRangeIndex.value = 0;
  startDate.value = '';
  endDate.value = '';
};

// 确认日期范围选择
const confirmDateRange = () => {
  showDateDropdown.value = false;
};

// 接收页面参数并处理状态切换
onLoad((options) => {
  console.log('订单列表页面参数:', options);
  
  // 如果有状态参数，切换到对应标签
  if (options.status !== undefined) {
    const status = parseInt(options.status);
    // 将数字状态映射到对应的标签索引
    // 0-待付款，1-待发货，2-待收货，3-退款/售后
    switch(status) {
      case 0: // 待付款
        currentTab.value = 1; // 对应 tabs 中的待付款
        break;
      case 1: // 待发货
        currentTab.value = 2; // 对应 tabs 中的待发货 
        break;
      case 2: // 待收货
        currentTab.value = 3; // 对应 tabs 中的待收货
        break;
      case 3: // 退款/售后
        // 目前标签栏没有退款/售后，可以保持在全部订单
        currentTab.value = 0;
        break;
      default:
        currentTab.value = 0; // 默认全部订单
    }
  }
});

onMounted(() => {
  // 页面加载时可以从缓存或API获取订单列表
  // 不再需要手动调用onLoad
});
</script>

<style lang="scss" scoped>
.order-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.fixed-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #ffffff;
}

.fixed-placeholder {
  height: calc(var(--status-bar-height) + 44px + 240rpx);
  /* 计算高度: 状态栏高度 + 导航栏高度 + 筛选区域高度 + 订单状态栏高度 */
}

.filter-section {
  background-color: #ffffff;
  padding: 20rpx;
}

.date-filter {
  margin-bottom: 20rpx;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.date-dropdown {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: #333333;
  
  text {
    margin-right: 10rpx;
  }
}

.date-picker-box {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 70%;
  justify-content: flex-end;
}

.date-picker {
  flex: 1;
  height: 70rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333333;
  max-width: 200rpx;
  
  .date-placeholder {
    color: #999999;
  }
}

.date-separator {
  margin: 0 20rpx;
  color: #999999;
  font-size: 28rpx;
}

.search-section {
  display: flex;
  align-items: center;
}

.search-box {
  flex: 1;
  height: 60rpx;
  background-color: #f5f5f5;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  margin-right: 20rpx;
  
  .search-input {
    flex: 1;
    height: 80rpx;
    font-size: 28rpx;
    margin-left: 10rpx;
  }
}

.action-btns {
  display: flex;
  align-items: center;
  
  .search-btn {
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #FFA0B4;
    color: #ffffff;
    font-size: 28rpx;
    border-radius: 40rpx;
    padding: 0 40rpx;
    margin-right: 20rpx;
  }
}

.date-dropdown-modal {
  background-color: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
  
  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .modal-title {
      font-size: 32rpx;
      font-weight: 500;
      color: #333333;
    }
  }
  
  .modal-body {
    padding: 30rpx;
    
    .date-options {
      display: flex;
      flex-wrap: wrap;
      margin: 0 -10rpx;
      
      .date-option-item {
        width: calc(33.33% - 20rpx);
        height: 80rpx;
        margin: 10rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28rpx;
        color: #333333;
        background-color: #f5f5f5;
        border-radius: 8rpx;
        
        &.active {
          background-color: rgba(255, 160, 180, 0.1);
          color: #FFA0B4;
          border: 1rpx solid #FFA0B4;
        }
      }
    }
  }
  
  .modal-footer {
    display: flex;
    padding: 20rpx 30rpx 40rpx;
    
    .reset-btn, .confirm-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30rpx;
      border-radius: 40rpx;
    }
    
    .reset-btn {
      background-color: #f5f5f5;
      color: #666666;
      margin-right: 20rpx;
    }
    
    .confirm-btn {
      background-color: #FFA0B4;
      color: #ffffff;
    }
  }
}

.date-dropdown-menu {
  display: none; /* 隐藏原来的下拉菜单样式 */
}

.order-tabs {
  display: flex;
  background-color: #ffffff;
  height: 80rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    font-size: 28rpx;
    color: #666666;
    
    &.active {
      color: #FFA0B4;
      font-weight: 500;
      
      .tab-line {
        position: absolute;
        bottom: 0;
        width: 40rpx;
        height: 4rpx;
        background-color: #FFA0B4;
        border-radius: 2rpx;
      }
    }
  }
}

.order-list {
  padding: 20rpx;
}

.order-item {
  background-color: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
    .order-no {
      font-size: 26rpx;
      color: #666666;
    }
    
    .order-status {
      font-size: 26rpx;
      font-weight: 500;
      
      &.status-wait-pay {
        color: #ff9900;
      }
      
      &.status-wait-deliver {
        color: #3399ff;
      }
      
      &.status-wait-receive {
        color: #33cc99;
      }
      
      &.status-completed {
        color: #c82436;
      }
      
      &.status-canceled {
        color: #999999;
      }
    }
  }
  
  .order-product {
    display: flex;
    padding: 20rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    
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
          color: #FFA0B4;
          font-weight: 500;
        }
        
        .product-count {
          font-size: 26rpx;
          color: #999999;
        }
      }
    }
  }
  
  .order-summary {
    padding: 20rpx 30rpx;
    
    .order-time {
      font-size: 24rpx;
      color: #999999;
      margin-bottom: 10rpx;
    }
    
    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      font-size: 26rpx;
      color: #666666;
      
      text {
        margin-left: 10rpx;
      }
      
      .total-price {
        font-size: 30rpx;
        color: #FFA0B4;
        font-weight: 500;
      }
    }
  }
  
  .order-actions {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f0f0f0;
    
    :deep(.u-button) {
      margin-left: 20rpx;
      height: 60rpx;
    }
    
    :deep(.u-button--primary) {
      background-color: #FFA0B4;
      border-color: #FFA0B4;
    }
  }
}

.empty-order {
  padding: 200rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 