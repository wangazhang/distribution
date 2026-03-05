<template>
  <s-layout title="出库申请记录">
    <view class="list-container">
      <!-- 顶部筛选栏 -->
      <su-sticky bgColor="#fff">
        <view class="filter-bar">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index" 
            class="filter-item" 
            :class="{ 'active': activeTab === index }"
            @tap="switchTab(index)"
          >
            <text>{{ tab.name }}</text>
            <view v-if="activeTab === index" class="active-line"></view>
          </view>
        </view>
      </su-sticky>

      <!-- 列表内容 -->
      <view class="list-content">
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <uni-load-more status="loading"></uni-load-more>
        </view>
        
        <!-- 空状态 -->
        <view v-else-if="outboundList.length === 0" class="empty-container">
          <uni-icons type="info" size="30" color="#999"></uni-icons>
          <text class="empty-text">暂无{{ tabs[activeTab].name }}的出库申请</text>
          <view class="create-btn" @tap="navigateToCreate">
            申请出库
          </view>
        </view>
        
        <!-- 列表数据 -->
        <view v-else class="outbound-list">
          <view 
            v-for="item in outboundList" 
            :key="item.id" 
            class="outbound-item"
            @tap="navigateToDetail(item.id)"
          >
            <view class="item-header">
              <text class="outbound-no">单号: {{ item.outboundNo }}</text>
              <text class="outbound-status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </text>
            </view>
            
            <view class="item-content">
              <!-- 物料信息区域 - 简洁展示 -->
              <view class="material-summary">
                <!-- 物料图片展示 -->
                <view class="material-images" v-if="item.materialImages && item.materialImages.length > 0">
                  <view class="image-wrapper" v-for="(imgUrl, index) in item.materialImages.slice(0, 3)" :key="index">
                    <image 
                      :src="imgUrl" 
                      class="material-image" 
                      mode="aspectFill"
                      @error="handleImageError($event, item, index)"
                    ></image>
                  </view>
                  <view class="material-image-more" v-if="item.itemCount > 3">
                    +{{ item.itemCount - 3 }}
                  </view>
                </view>
                
                <view class="material-info">
                  <text class="summary-text">
                    共 {{ item.itemCount }} 种物料，总计 {{ item.totalQuantity }} 件
                  </text>
                  
                  <!-- 显示主要物料名称 -->
                  <text v-if="item.materialNames && item.materialNames.length > 0" class="material-names">
                    {{ item.materialNames.join('、') }}{{ item.itemCount > item.materialNames.length ? '等' : '' }}
                  </text>
                </view>
              </view>
              
              <!-- 收货人信息区域  -->
              <view class="receiver-info">
                <view class="receiver-header">
                  <text class="receiver-name">{{ item.address.name }}</text>
                  <view class="mobile-wrapper">
                    <uni-icons type="location" size="16" color="#666"></uni-icons>
                    <text class="address-text">{{ item.address.province }}{{ item.address.city }}{{ item.address.district }}</text>
                  </view>
                  <view class="phone-wrapper">
                    <uni-icons type="phone" size="16" color="#666"></uni-icons>
                    <text class="receiver-mobile">{{ item.address.mobile }}</text>
                  </view>
                </view>
              </view>
            </view>
            
            <view class="item-footer">
              <view class="footer-left">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="create-time">{{ formatDate(item.createTime) }}</text>
              </view>
              
              <view class="action-buttons">
                <!-- 待审核状态可取消 -->
                <view 
                  v-if="item.status === 0" 
                  class="action-btn cancel-btn"
                  @tap.stop="showCancelConfirm(item)"
                >
                  取消申请
                </view>
                
                <!-- 已发货状态可确认收货 -->
                <view 
                  v-if="item.status === 2" 
                  class="action-btn confirm-btn"
                  @tap.stop="showConfirmReceiptConfirm(item)"
                >
                  确认收货
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 加载更多 -->
        <uni-load-more 
          v-if="outboundList.length > 0" 
          :status="loadMoreStatus" 
          :content-text="loadingText"
          @clickLoadMore="loadMore"
        ></uni-load-more>
      </view>
      
      <!-- 底部安全区 -->
      <view class="safe-area-bottom"></view>
      
      <!-- 悬浮按钮 -->
      <view class="float-btn" @tap="navigateToCreate">
        <uni-icons type="plusempty" size="24" color="#fff"></uni-icons>
      </view>
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
    'uni-load-more': () => import('@/uni_modules/uni-load-more/components/uni-load-more/uni-load-more.vue'),
    'su-dialog': () => import('@/sheep/ui/su-dialog/su-dialog.vue'),
    'su-sticky': () => import('@/sheep/ui/su-sticky/su-sticky.vue')
  }
};
</script>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getMaterialOutboundPage, cancelMaterialOutbound, confirmMaterialOutbound } from '@/yehu/api/mine/material-outbound';
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { resetPagination } from '@/sheep/util';

// 筛选标签
const tabs = [
  { name: '全部', status: null },
  { name: '待审核', status: 0 },
  { name: '待发货', status: 1 },
  { name: '已发货', status: 2 },
  { name: '已完成', status: 3 }
];
const activeTab = ref(0);

// 列表数据
const outboundList = ref([]);
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0
});

// 加载状态
const loading = ref(true);
const loadMoreStatus = ref('more');

// 加载文本配置
const loadingText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了'
};

// 取消申请相关
const showCancelDialog = ref(false);
const currentOutbound = ref(null);
const cancelReason = ref('');

// 确认收货相关
const showConfirmReceiptDialog = ref(false);

// 重置分页数据
const resetPageData = () => {
  resetPagination(pagination.value);
  outboundList.value = [];
  loadMoreStatus.value = 'more';
};

// 切换标签
const switchTab = (index) => {
  if (activeTab.value === index) return;
  
  activeTab.value = index;
  // 重置分页并重新加载数据
  resetPageData();
  fetchOutboundList(true);
};

// 获取出库申请列表
const fetchOutboundList = async (isRefresh = true) => {
  if (isRefresh) {
    loading.value = true;
  }
  
  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
    };
    
    // 如果选择了特定状态，则添加状态筛选
    if (tabs[activeTab.value].status !== null) {
      params.status = tabs[activeTab.value].status;
    }
    
    loadMoreStatus.value = 'loading';
    const res = await getMaterialOutboundPage(params);
    console.log('获取出库申请列表响应:', res);
    
    if (res && res.data && res.data.list) {
      // 打印第一条数据的结构，帮助调试
      if (res.data.list.length > 0) {
        console.log('第一条出库申请数据结构:', JSON.stringify(res.data.list[0]));
      }
      
      const processedList = res.data.list.map(item => {
        // 后端已经计算好了物料统计信息，直接使用
        // itemCount: 物料种类数
        // totalQuantity: 物料总数量
        // materialImages: 物料图片列表（最多3张）
        // materialNames: 物料名称列表（最多3个）

        return {
          id: item.id,
          outboundNo: item.outboundNo,
          status: item.status,
          createTime: item.createTime,
          // 直接使用后端返回的统计信息
          itemCount: item.itemCount || 0,
          totalQuantity: item.totalQuantity || 0,
          materialNames: item.materialNames || [],
          materialImages: item.materialImages || [],
          address: {
            name: item.receiverName || '',
            mobile: item.receiverMobile || '',
            province: item.receiverProvince || '',
            city: item.receiverCity || '',
            district: item.receiverDistrict || '',
            detail: item.receiverDetailAddress || ''
          }
        };
      });
      
      if (isRefresh) {
        outboundList.value = processedList;
      } else {
        outboundList.value = [...outboundList.value, ...processedList];
      }
      
      pagination.value.total = res.data.total || 0;
      
      // 更新加载更多状态
      loadMoreStatus.value = outboundList.value.length >= pagination.value.total ? 'noMore' : 'more';
    } else {
      if (isRefresh) {
        outboundList.value = [];
      }
      loadMoreStatus.value = 'noMore';
    }
  } catch (err) {
    console.error('获取出库申请列表失败', err);
    uni.showToast({
      title: '获取列表失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

// 加载更多
const loadMore = async () => {
  if (loadMoreStatus.value === 'noMore' || loadMoreStatus.value === 'loading') return;
  
  pagination.value.pageNo += 1;
  await fetchOutboundList(false);
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
const formatAddressSimple = (address) => {
  if (!address) return '';
  return `${address.name} ${address.mobile} ${address.province}${address.city}${address.district}${address.detail}`;
};

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  
  // 年月日
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  
  // 时分
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  
  return `申请时间: ${year}-${month}-${day} ${hour}:${minute}`;
};

// 导航到创建页面
const navigateToCreate = () => {
  uni.navigateTo({
    url: '/yehu/pages/mine/material/outbound/index'
  });
};

// 导航到详情页面
const navigateToDetail = (id) => {
  uni.navigateTo({
    url: `/yehu/pages/mine/material/outbound/detail?id=${id}`
  });
};

// 显示取消确认弹窗
const showCancelConfirm = (item) => {
  currentOutbound.value = item;
  cancelReason.value = '';
  showCancelDialog.value = true;
};

// 关闭取消确认弹窗
const closeCancelPopup = () => {
  showCancelDialog.value = false;
};

// 确认取消申请
const confirmCancel = async () => {
  if (!currentOutbound.value) return;
  
  uni.showLoading({
    title: '取消申请中...'
  });
  
  try {
    await cancelMaterialOutbound({
      id: currentOutbound.value.id,
      cancelReason: cancelReason.value
    });
    
    // 刷新列表
    resetPageData();
    await fetchOutboundList(true);
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
const showConfirmReceiptConfirm = (item) => {
  currentOutbound.value = item;
  showConfirmReceiptDialog.value = true;
};

// 关闭确认收货弹窗
const closeConfirmReceiptPopup = () => {
  showConfirmReceiptDialog.value = false;
};

// 确认收货
const confirmReceipt = async () => {
  if (!currentOutbound.value) return;
  
  uni.showLoading({
    title: '确认收货中...'
  });
  
  try {
    await confirmMaterialOutbound(currentOutbound.value.id);
    
    // 刷新列表
    resetPageData();
    await fetchOutboundList(true);
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

// 处理下拉刷新
const handlePullDownRefresh = async () => {
  resetPageData();
  await fetchOutboundList(true);
  
  // 延迟关闭下拉刷新状态
  setTimeout(() => {
    uni.stopPullDownRefresh();
  }, 800);
};

// 处理上拉加载更多
const handleReachBottom = () => {
  if (outboundList.value.length > 0 && !loading.value && loadMoreStatus.value === 'more') {
    loadMore();
  }
};

// 页面下拉刷新
onPullDownRefresh(() => {
  handlePullDownRefresh();
});

// 页面上拉触底
onReachBottom(() => {
  handleReachBottom();
});

// 图片加载错误处理函数
const handleImageError = (event, item, index) => {
  console.error(`图片加载错误: 索引${index}`, event);
  // 替换为默认图片
  if (item.materialImages && item.materialImages.length > index) {
    // 使用Vue的响应式API直接修改数组元素
    item.materialImages[index] = '/static/images/default-product.png';
  }
};

onMounted(() => {
  // 加载出库申请列表
  fetchOutboundList();
});
</script>

<style lang="scss" scoped>
.list-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  position: relative;
  padding-bottom: 40rpx;
}

// 筛选栏样式
.filter-bar {
  display: flex;
  background-color: #fff;
  padding: 20rpx 0;
  position: relative;
  z-index: 10;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  
  .filter-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    
    text {
      font-size: 28rpx;
      color: #666;
      padding: 10rpx 0;
    }
    
    &.active {
      text {
        color: #8f1911;
        font-weight: 500;
      }
      
      .active-line {
        width: 40rpx;
        height: 4rpx;
        background-color: #8f1911;
        position: absolute;
        bottom: 0;
      }
    }
  }
}

// 列表内容样式
.list-content {
  padding: 20rpx;
}

// 出库申请项样式
.outbound-list {
  .outbound-item {
    background-color: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    padding: 24rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
    
    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 16rpx;
      border-bottom: 1rpx solid #f0f0f0;
      margin-bottom: 16rpx;
      
      .outbound-no {
        font-size: 26rpx;
        color: #666;
      }
      
      .outbound-status {
        font-size: 26rpx;
        font-weight: 500;
        color: #999999;
        
        &.status-pending {
          color: #faad14;
        }
      }
    }
    
    .item-content {
      padding: 0 0 16rpx 0;
      
      .material-summary {
        display: flex;
        margin-bottom: 20rpx;
        padding-bottom: 16rpx;
        align-items: flex-start;
        border-bottom: 1rpx solid #f0f0f0;
        
        .material-images {
          position: relative;
          margin-right: 20rpx;
          width: 240rpx;
          height: 120rpx;
          flex-shrink: 0;
          
          .image-wrapper {
            position: absolute;
            width: 120rpx;
            height: 120rpx;
            top: 0;
            border-radius: 8rpx;
            border: 1rpx solid #f0f0f0;
            background-color: #fff;
            overflow: hidden;
            box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
            z-index: 3;
            
            &:nth-child(1) {
              left: 0;
              z-index: 3;
            }
            
            &:nth-child(2) {
              left: 40rpx;
              z-index: 2;
            }
            
            &:nth-child(3) {
              left: 80rpx;
              z-index: 1;
            }
          }
          
          .material-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
          
          .material-image-more {
            position: absolute;
            top: 0;
            left: 120rpx;
            width: 120rpx;
            height: 120rpx;
            border-radius: 8rpx;
            background-color: rgba(0, 0, 0, 0.6);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24rpx;
            font-weight: 500;
            z-index: 0;
          }
        }
        
        .material-info {
          flex: 1;
          padding-top: 10rpx;
          
          .summary-text {
            font-size: 28rpx;
            color: #333;
            margin-bottom: 12rpx;
            display: block;
          }
          
          .material-names {
            font-size: 26rpx;
            color: #666;
            display: block;
            word-break: break-all;
          }
        }
      }
      
      .receiver-info {
        padding: 10rpx 20rpx;
        font-size: 24rpx;
        color: #666;
        background-color: #f8f8f8;
        // border-radius: 12rpx;
        // margin-top: 16rpx;
        
        .receiver-header {
          display: flex;
          align-items: center;
          // margin-bottom: 12rpx;
          justify-content: space-between;
          flex-wrap: wrap;
          
          .receiver-name {
            font-size: 26rpx;
            color: #333;
            font-weight: 500;
            margin-right: 0;
            max-width: 140rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .mobile-wrapper {
            display: flex;
            align-items: center;
            flex: 1;
            margin: 0rpx 20rpx 0 5rpx;
            min-width: 0; // 确保flex子项可以正确收缩
            
            .uni-icons {
              margin-right: 6rpx;
              flex-shrink: 0;
            }
            
            .address-text {
              margin-left: 6rpx;
              line-height: 1.4;
              flex: 1;
              font-size: 24rpx;
              color: #666;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              min-width: 0; // 确保文本可以正确收缩
            }
          }
          
          .phone-wrapper {
            display: flex;
            align-items: center;
            width: 200rpx;
            justify-content: flex-end;
            
            .uni-icons {
              margin-right: 6rpx;
              flex-shrink: 0;
            }
            
            .receiver-mobile {
              font-size: 26rpx;
              color: #666;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              max-width: 160rpx; // 限制手机号最大宽度
            }
          }
        }
        
        .receiver-address {
          display: flex;
          align-items: flex-start;
          
          .uni-icons {
            margin-top: 4rpx;
            flex-shrink: 0;
          }
          
          .address-text {
            margin-left: 6rpx;
            line-height: 1.4;
            flex: 1;
          }
        }
      }
    }
    
    .item-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16rpx;
      border-top: 1rpx solid #f0f0f0;
      
      .footer-left {
        display: flex;
        align-items: center;
      }
      
      .create-time {
        font-size: 24rpx;
        color: #999;
        margin-left: 10rpx;
      }
      
      .action-buttons {
        display: flex;
        
        .action-btn {
          padding: 10rpx 24rpx;
          border-radius: 30rpx;
          font-size: 24rpx;
          margin-left: 16rpx;
          
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
    }
  }
}

// 加载和空状态样式
.loading-container, .empty-container {
  padding: 100rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
    margin-bottom: 30rpx;
  }
  
  .create-btn {
    padding: 16rpx 40rpx;
    background-color: #8f1911;
    color: #fff;
    border-radius: 30rpx;
    font-size: 28rpx;
  }
}

// 底部安全区
.safe-area-bottom {
  height: 120rpx;
}

// 悬浮按钮
.float-btn {
  position: fixed;
  right: 40rpx;
  bottom: 100rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #8f1911;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(143, 25, 17, 0.3);
  z-index: 100;
}

// 取消原因输入框
.cancel-reason {
  width: 100%;
  height: 160rpx;
  padding: 20rpx;
  font-size: 28rpx;
  background-color: #f8f8f8;
  border-radius: 8rpx;
  box-sizing: border-box;
  margin-top: 20rpx;
}
</style> 