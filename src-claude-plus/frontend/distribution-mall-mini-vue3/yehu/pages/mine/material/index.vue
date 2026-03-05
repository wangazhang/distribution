<template>
  <s-layout title="我的物料">
    <view class="page-container">
      <!-- 页面顶部操作栏 -->
      <view class="action-header">
        <view class="title-section">
          <text class="page-title">物料管理</text>
        </view>
        <view class="action-buttons">
          <view class="record-link" @tap="navigateToRecords()">
            <uni-icons type="list" size="18" color="#666"></uni-icons>
            <text>变更记录</text>
          </view>
          <!-- <view class="outbound-link" @tap="navigateToOutbound()">
            <uni-icons type="paperplane" size="18" color="#666"></uni-icons>
            <text>申请出库</text>
          </view> -->
        </view>
      </view>

      <view class="page-content">
        <!-- 加载状态 -->
        <view v-if="loading" class="loading-container">
          <uni-load-more status="loading" :contentText="loadingText"></uni-load-more>
          <text class="loading-text">数据加载中...</text>
        </view>

        <!-- 错误状态 -->
        <view v-else-if="error" class="error-container">
          <uni-icons type="error" size="30" color="#8f1911"></uni-icons>
          <text class="error-text">{{ errorMessage }}</text>
          <view class="retry-btn" @tap="fetchMaterialList">
            <text>重新加载</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else-if="materialList.length === 0" class="empty-container">
          <uni-icons type="info" size="30" color="#999"></uni-icons>
          <text class="empty-text">暂无物料数据</text>
        </view>

        <!-- 物料列表 -->
        <view v-else class="material-list">
          <view
            v-for="item in materialList"
            :key="item.id"
            class="material-card"
            :class="{ 'low-stock': item.stock < 5 }"
            @tap="navigateToRecords(item.id)"
          >
            <view class="card-header">
              <view class="material-name">{{ item.name }}</view>
              <view class="stock-indicator" :class="{ warning: item.stock < 5 }">
                <uni-icons
                  v-if="item.stock < 5"
                  type="notification-filled"
                  size="16"
                  color="#8f1911"
                ></uni-icons>
                <text>余量: {{ item.stock }} {{ item.unit }}</text>
              </view>
            </view>
            <view class="card-content">
              <image class="material-image" :src="item.image" mode="aspectFill"></image>
              <view class="material-info">
                <text class="material-id">编号: {{ item.id }}</text>
              </view>
            </view>
            
            <!-- 卡片操作区域 - 同时展示补货与转化能力 -->
            <view class="card-actions single-action" @tap.stop>
              <view class="right-buttons">
                <view v-if="item.supportConvert" class="action-btn" @tap="handleConvert(item)">
                  <text>转产品</text>
                </view>
                <view
                  v-if="isRestockSupported(item)"
                  class="action-btn"
                  @tap="handleRestock(item)"
                >
                  <text>补货</text>
                </view>
                <view
                  v-if="!item.supportConvert && !isRestockSupported(item)"
                  class="action-btn disabled"
                >
                  <text>暂无操作</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 上拉加载更多 -->
        <uni-load-more
          v-if="materialList.length > 0"
          :status="loadMoreStatus"
          :contentText="loadingText"
          @clickLoadMore="loadMore"
        ></uni-load-more>

        <!-- 底部安全区 -->
        <view class="safe-area-bottom"></view>

        <!-- 添加出库管理入口到物料管理页面 -->
        <view class="function-card" @tap="navigateToOutboundList">
          <view class="card-icon">
            <uni-icons type="upload-filled" size="30" color="#8f1911"></uni-icons>
          </view>
          <view class="card-content">
            <text class="card-title">出库管理</text>
            <text class="card-desc">查看和管理物料出库申请</text>
          </view>
          <view class="card-arrow">
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 补货/物料转化弹窗内容模板 - 使用条件渲染 -->
    <view v-if="showRestockModal" class="restock-modal-mask" @tap="closeRestockModal">
      <view class="restock-modal" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isConvertMode ? `转${convertTargetLabel}` : '补货' }}</text>
          <view class="close-btn" @tap="closeRestockModal">×</view>
        </view>
        
        <view v-if="loadingPrice" class="modal-loading">
          <uni-load-more status="loading" :contentText="loadingText"></uni-load-more>
          <text class="loading-text">价格加载中...</text>
        </view>
        
        <view v-else-if="priceError" class="modal-error">
          <uni-icons type="error" size="30" color="#8f1911"></uni-icons>
          <text class="error-text">{{ priceErrorMessage }}</text>
          <view class="retry-btn" @tap="getRestockPrice(currentMaterial.id)">
            <text>重新加载</text>
          </view>
        </view>
        
        <view v-else class="modal-content">
          <view class="product-info">
            <image class="product-image" :src="restockPrice.productImage" mode="aspectFill"></image>
            <view class="product-details">
              <text class="product-name">{{ restockPrice.productName }}</text>
              <text class="product-price">{{ (restockPrice.restockPrice / 100).toFixed(2) }}元/件</text>
            </view>
          </view>
          
          <view class="quantity-selector">
            <text class="quantity-label">{{ isConvertMode ? '转化数量' : '补货数量' }}</text>
            <view class="quantity-control">
              <view class="quantity-btn" @tap="decreaseQuantity">
                <text>-</text>
              </view>
              <input 
                class="quantity-input" 
                type="number" 
                v-model="restockQuantity" 
                @input="validateQuantity"
              />
              <view class="quantity-btn" @tap="increaseQuantity">
                <text>+</text>
              </view>
            </view>
          </view>
          
          <view class="total-amount">
            <text class="total-label">总金额</text>
            <text class="total-value">¥{{ ((restockPrice.restockPrice * restockQuantity) / 100).toFixed(2) }}</text>
          </view>
          
          <!-- 转化库存提示 -->
          <view v-if="showMaxStockWarning" class="stock-warning">
            <uni-icons type="notification-filled" size="16" color="#8f1911"></uni-icons>
            <text>最多只能转化{{ maxConvertQuantity }}{{ currentMaterial.unit }}</text>
          </view>
        </view>
        
        <view class="modal-footer">
          <view class="modal-btn cancel-btn" @tap="closeRestockModal">取消</view>
          <view class="modal-btn confirm-btn" :class="{ disabled: !canSubmit }" @tap="submitRestockOrder">
            {{ isConvertMode ? '确认转化' : '确认下单' }}
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
import { ref, onMounted, computed, nextTick } from 'vue';
import { getMaterialList, getMaterialBalance } from '@/yehu/api/mine';
import { getMaterialRestockPrice, createMaterialRestockOrder } from '@/yehu/api/mine/material';
import { BIZ_TYPE } from '@/yehu/api/mine/mb-order';
import sheep from '@/sheep';
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';

// 物料列表数据
const materialList = ref([]);
const pagination = ref({
  pageNo: 1,
  pageSize: 10,
  total: 0,
});

// 加载状态
const loading = ref(true);
const error = ref(false);
const errorMessage = ref('数据加载失败，请重试');
const loadMoreStatus = ref('more');

// loading文本配置
const loadingText = {
  contentdown: '上拉显示更多',
  contentrefresh: '正在加载...',
  contentnomore: '没有更多数据了',
};

// 获取物料列表
const fetchMaterialList = async (isRefresh = true) => {
  if (isRefresh) {
    loading.value = true;
    error.value = false;
    pagination.value.pageNo = 1;
  }

  try {
    const params = {
      pageNo: pagination.value.pageNo,
      pageSize: pagination.value.pageSize,
    };

    const res = await getMaterialList(params);
    console.log("******getMaterialList", res);
    // 调试数据结构
    if (res && res.data && res.data.list && res.data.list.length > 0) {
      console.log("第一条物料数据示例:", res.data.list[0]);
    }
    
    // 处理返回的数据
    if (res && res.data && res.data.list) {
      // 转换后端数据为前端所需格式
      const processedList = res.data.list.map((item) => {
        console.log("处理物料项:", item);
        
        // 尝试不同的可能的字段名
        const materialId = item.materialId || item.id || '';
        const materialName = item.materialName || item.name || '未命名物料';
        const materialImage = item.materialImage || item.image || '';
        const balance = item.balance || item.stock || item.availableBalance || 0;
        const unit = item.unit || '件';

        // 保留分享ID
        const shareId = item.shareId || materialId;
        
        const materialType = item.materialType ?? item.type ?? null;
        const supportConvert = item.supportConvert === true && (item.convertPrice !== undefined && item.convertPrice !== null);
        const supportRestock = item.supportRestock !== undefined ? !!item.supportRestock : true;
        return {
          id: materialId,
          name: materialName,
          image: materialImage || 'https://cdn.example.com/static/pic/product/1.png',
          stock: balance,
          unit: unit,
          shareId: shareId,
          materialType,
          supportConvert,
          supportRestock,
          convertPrice: item.convertPrice ?? null,
          convertTargetSpuId: item.convertTargetSpuId ?? item.convertedSpuId ?? null,
          convertTargetName: item.convertTargetName || item.targetName || null,
        };
      });

      if (isRefresh) {
        materialList.value = processedList;
      } else {
        materialList.value = [...materialList.value, ...processedList];
      }
      
      // 打印处理后的物料列表第一项用于调试
      if(materialList.value.length > 0) {
        console.log("处理后的第一个物料项:", materialList.value[0]);
      }

      pagination.value.total = res.data.total || 0;

      // 更新加载更多状态
      loadMoreStatus.value =
        materialList.value.length >= pagination.value.total ? 'noMore' : 'more';
    } else {
      if (isRefresh) {
        materialList.value = [];
      }
      loadMoreStatus.value = 'noMore';
    }

    console.log('物料列表加载完成', materialList.value);
  } catch (err) {
    console.error('获取物料列表失败', err);
    error.value = true;
    errorMessage.value = err.message || '数据加载失败，请重试';
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
};

// 加载更多
const loadMore = async () => {
  if (loadMoreStatus.value === 'noMore') return;

  loadMoreStatus.value = 'loading';
  pagination.value.pageNo += 1;
  await fetchMaterialList(false);
};

// 补货/物料转化相关
const isConvertMode = computed(() => currentMaterial.value?.supportConvert === true);
const convertTargetLabel = computed(() => {
  if (!isConvertMode.value) {
    return '';
  }
  return currentMaterial.value?.convertTargetName || '目标物料';
});

// 补货相关
const showRestockModal = ref(false);
const currentMaterial = ref(null);
const restockPrice = ref({});
const restockQuantity = ref(1);
const loadingPrice = ref(false);
const priceError = ref(false);
const priceErrorMessage = ref('获取价格失败，请重试');

// 最大可转化数量（针对转化产品）
const maxConvertQuantity = computed(() => {
  return isConvertMode.value && currentMaterial.value ? currentMaterial.value.stock : Infinity;
});

// 是否显示最大库存警告
const showMaxStockWarning = computed(() => {
  return isConvertMode.value && restockQuantity.value > maxConvertQuantity.value;
});

const isRestockSupported = (item) => {
  if (!item) return false;
  return item.supportRestock !== false;
};

// 是否可以提交订单
const canSubmit = computed(() => {
  // 对于转化产品，需要检查转化数量是否超过库存
  if (isConvertMode.value) {
    return !loadingPrice.value && 
           !priceError.value && 
           restockQuantity.value > 0 && 
           restockQuantity.value <= maxConvertQuantity.value && 
           restockPrice.value && 
           restockPrice.value.restockPrice;
  }
  
  // 对于普通补货，维持原有逻辑
  return !loadingPrice.value && 
         !priceError.value && 
         restockQuantity.value > 0 && 
         restockPrice.value && 
         restockPrice.value.restockPrice;
});

// 处理补货/转转化产品
const resolveRestockProductId = (item) => {
  if (!item) return undefined;
  return item.refillId || item.shareId || item.convertTargetSpuId || item.materialId || item.id;
};

const resolveConvertSourceProductId = (item) => {
  if (!item) return undefined;
  return item.refillId || item.shareId || item.materialId || item.id;
};

const handleRestock = async (item) => {
  if (!item) return
  const convertFlow = item.supportConvert === true
  if (!convertFlow && !isRestockSupported(item)) {
    return
  }
  currentMaterial.value = item

  // 重置状态
  loadingPrice.value = true
  priceError.value = false
  restockQuantity.value = 1

  // 显示弹窗
  showRestockModal.value = true

  // 获取补货/转化价格
  await getRestockPrice(resolveRestockProductId(item))
};

// 处理转转化产品 - 复用补货处理函数
const handleConvert = async (item) => {
  if (!item?.supportConvert) {
    return;
  }
  await handleRestock(item);
};

// 获取补货/转转化价格
const getRestockPrice = async (productId) => {
  loadingPrice.value = true;
  priceError.value = false;
  if (!isConvertMode.value && !productId) {
    priceError.value = true;
    priceErrorMessage.value = '未配置补货商品';
    loadingPrice.value = false;
    return;
  }

  if (isConvertMode.value) {
    const price = currentMaterial.value?.convertPrice;
    if (price === undefined || price === null) {
      priceError.value = true;
      priceErrorMessage.value = '未配置转化价格';
      loadingPrice.value = false;
      return;
    }
    restockPrice.value = {
      restockPrice: price,
      productName: currentMaterial.value?.convertTargetName || currentMaterial.value?.name || '物料转化',
      productImage: currentMaterial.value?.image,
    };
    loadingPrice.value = false;
    return;
  }
  
  try {
    const res = await getMaterialRestockPrice(productId);
    console.log(`获取${isConvertMode.value ? '转化' : '补货'}价格成功:`, res);
    
    if (res && res.data) {
      restockPrice.value = res.data;
    } else {
      throw new Error('获取价格数据失败');
    }
  } catch (err) {
    console.error(`获取${isConvertMode.value ? '转化' : '补货'}价格失败`, err);
    priceError.value = true;
    priceErrorMessage.value = err.message || '获取价格失败，请重试';
  } finally {
    loadingPrice.value = false;
  }
};

// 增加数量
const increaseQuantity = () => {
  // 对于转化产品，不能超过最大库存量
  if (isConvertMode.value && restockQuantity.value >= maxConvertQuantity.value) {
    uni.showToast({
      title: `最多只能转化${maxConvertQuantity.value}${currentMaterial.value.unit}`,
      icon: 'none',
      duration: 2000
    });
    return;
  }
  
  restockQuantity.value++;
  validateQuantity();
};

// 减少数量
const decreaseQuantity = () => {
  if (restockQuantity.value > 1) {
    restockQuantity.value--;
  }
  validateQuantity();
};

// 验证数量输入
const validateQuantity = () => {
  let val = parseInt(restockQuantity.value);
  
  if (isNaN(val) || val < 1) {
    restockQuantity.value = 1;
  } else {
    // 对于转化产品，数量不能超过库存余量
    if (isConvertMode.value && val > maxConvertQuantity.value) {
      restockQuantity.value = maxConvertQuantity.value;
      uni.showToast({
        title: `最多只能转化${maxConvertQuantity.value}${currentMaterial.value.unit}`,
        icon: 'none',
        duration: 2000
      });
    } else {
      restockQuantity.value = val;
    }
  }
};

// 关闭补货/转转化产品弹窗
const closeRestockModal = () => {
  showRestockModal.value = false;
};

// 提交补货/转转化产品订单
const submitRestockOrder = async () => {
  if (!canSubmit.value) return;
  
  // 再次验证物料转化数量不超过库存
  if (isConvertMode.value && restockQuantity.value > currentMaterial.value.stock) {
    uni.showToast({
      title: `转化数量不能超过当前库存：${currentMaterial.value.stock}${currentMaterial.value.unit}`,
      icon: 'none',
      duration: 2000
    });
    return;
  }
  
  uni.showLoading({
    title: `提交${isConvertMode.value ? '转化' : '补货'}订单中...`
  });
  
  try {
    const productId = isConvertMode.value
      ? resolveConvertSourceProductId(currentMaterial.value)
      : resolveRestockProductId(currentMaterial.value);
    if (!productId) {
      uni.showToast({
        title: isConvertMode.value ? '未配置可转化物料' : '未配置补货商品',
        icon: 'none',
        duration: 2000
      });
      uni.hideLoading();
      return;
    }
    const orderData = {
      productId,
      quantity: restockQuantity.value,
      bizType: isConvertMode.value ? BIZ_TYPE.MATERIAL_CONVERT : BIZ_TYPE.RESTOCK,
    };
    
    console.log(`准备提交${isConvertMode.value ? '物料转化' : '补货'}订单数据:`, orderData);
    const res = await createMaterialRestockOrder(orderData);
    console.log(`创建${isConvertMode.value ? '物料转化' : '补货'}订单响应:`, res);
    
    // 处理不同的响应格式情况
    if (res.code === 0) {
      // 成功情况 - 考虑到响应可能是 {code: 0, data: xxx, msg: ""}
      closeRestockModal();
      
      // 提取订单ID - 根据实际返回格式调整
      const orderId = res.data;
      
      // 如果有订单ID，跳转支付
      if (orderId) {
        // 直接跳转到收银台
        uni.navigateTo({
          url: `/pages/pay/index?id=${orderId}&orderType=${isConvertMode.value ? 'material_convert' : 'material_restock'}`
        });
      } else {
        // 订单创建成功但没有返回ID的情况
        uni.showToast({
          title: '订单已创建，无需支付',
          icon: 'success'
        });
      }
      
      // 刷新物料列表
      setTimeout(() => {
        fetchMaterialList(true);
      }, 3000);
    } else {
      // 处理业务错误码情况
      throw new Error(res.msg || '创建订单失败');
    }
  } catch (err) {
    console.error(`提交${isConvertMode.value ? '物料转化' : '补货'}订单失败`, err);
    uni.showToast({
      title: err.message || '提交订单失败，请重试',
      icon: 'none',
      duration: 2000
    });
  } finally {
    uni.hideLoading();
  }
};

// 跳转到变更记录页面
const navigateToRecords = (materialId) => {
  let url = '/yehu/pages/mine/material/record/index';

  // 如果传入了物料ID，则将其作为参数传递
  if (materialId) {
    url += `?materialId=${materialId}`;
  }

  uni.navigateTo({
    url,
  });
};

// 跳转到物料出库页面
const navigateToOutbound = () => {
  uni.navigateTo({
    url: '/yehu/pages/mine/material/outbound/index'
  });
};

// 在script部分添加导航方法
const navigateToOutboundList = () => {
  uni.navigateTo({
    url: '/yehu/pages/mine/material/outbound/list'
  });
};

// 页面下拉刷新
onPullDownRefresh(async () => {
  console.log('触发下拉刷新');
  await fetchMaterialList(true);
});

// 页面上拉触底
onReachBottom(() => {
  if (materialList.value.length > 0 && !loading.value && loadMoreStatus.value !== 'noMore') {
    loadMore();
  }
});

onMounted(() => {
  // 加载物料列表
  fetchMaterialList();
});
</script>

<style scoped lang="scss">
  .page-container {
    min-height: 100vh;
    background-color: #f8f8f8;
    position: relative;
    padding-top: 0 !important; /* 移除顶部内边距，由s-layout处理 */
    padding-bottom: 180rpx !important;
  }

  .action-header {
    position: sticky !important; /* 使用sticky而不是fixed */
    top: 0 !important; /* 相对于s-layout的内容区域顶部 */
    left: 0 !important;
    right: 0 !important;
    z-index: 90 !important;
    background-color: #fff;
    padding: 30rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .title-section {
      .page-title {
        font-size: 34rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .action-buttons {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .record-link {
        display: flex;
        align-items: center;
        
        text {
          color: #666;
          font-size: 26rpx;
          margin-left: 8rpx;
        }
      }
    }
  }

  .page-content {
    padding: 30rpx;

    .material-list {
      .material-card {
        margin-bottom: 40rpx;
        background: #fff;
        border-radius: 20rpx;
        overflow: hidden;
        box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &.low-stock {
          border: 2rpx solid #8f1911;
        }

        &:hover {
          transform: translateY(-4rpx);
          box-shadow: 0 12rpx 24rpx rgba(0, 0, 0, 0.12);
        }

        .card-header {
          padding: 20rpx 30rpx;
          display: flex;
          justify-content: space-between;
          align-items: center;
          border-bottom: 1rpx solid #f0f0f0;

          .material-name {
            flex: 1;
            max-width: 80%;
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            margin-right: 20rpx;
          }

          .stock-indicator {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            font-size: 26rpx;
            color: #666;

            &.warning {
              color: #8f1911;
              font-weight: 500;

              uni-icons {
                margin-right: 8rpx;
              }
            }
          }
        }

        .card-content {
          padding: 30rpx;
          display: flex;

          .material-image {
            width: 180rpx;
            height: 180rpx;
            border-radius: 12rpx;
            background-color: #f5f5f5;
            object-fit: cover;
          }

          .material-info {
            flex: 1;
            margin-left: 30rpx;
            display: flex;
            flex-direction: column;
            justify-content: center;
            
            .material-id {
              font-size: 24rpx;
              color: #999;
              margin-bottom: 10rpx;
            }


          }
        }

        .card-actions {
          padding: 20rpx 30rpx;
          display: flex;
          border-top: 1rpx solid #f0f0f0;
          justify-content: flex-end;
          
          .right-buttons {
            display: flex;
            justify-content: flex-end;
          }

          .action-btn {
            padding: 10rpx 30rpx;
            border-radius: 40rpx;
            font-size: 25rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-left: 20rpx;
            background-color: transparent;
            color: #8f1911;
            border: 1rpx solid #8f1911;
            transition: all 0.3s ease;
            
            &:first-child {
              margin-left: 0;
            }
            
            &:active {
              background-color: rgba(143, 25, 17, 0.1);
            }

            &.disabled {
              color: #c0c0c0;
              border-color: #e0e0e0;
              background-color: #f5f5f5;
              pointer-events: none;
            }
          }
        }
      }
    }

    // 加载和错误状态样式
    .loading-container,
    .error-container,
    .empty-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 100rpx 0;

      .loading-text,
      .error-text,
      .empty-text {
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

    .safe-area-bottom {
      height: 40rpx;
    }

    // 添加出库管理入口到物料管理页面
    .function-card {
      position: fixed !important;
      left: 30rpx !important;
      right: 30rpx !important;
      bottom: 40rpx !important;
      z-index: 99 !important;
      margin-top: 0 !important;
      background-color: #fff;
      border-radius: 20rpx;
      overflow: hidden;
      box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      padding: 30rpx;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .card-icon {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        border-radius: 12rpx;
        margin-right: 20rpx;
      }

      .card-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .card-title {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 12rpx;
        }

        .card-desc {
          font-size: 26rpx;
          color: #666;
        }
      }

      .card-arrow {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        border-radius: 12rpx;
      }
    }
  }
  
  // 补货弹窗样式 - 使用自定义模态框
  .restock-modal-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .restock-modal {
    width: 650rpx;
    background-color: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    
    .modal-header {
      padding: 30rpx;
      text-align: center;
      border-bottom: 1rpx solid #f0f0f0;
      position: relative;
      
      .modal-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
      
      .close-btn {
        position: absolute;
        right: 30rpx;
        top: 30rpx;
        font-size: 40rpx;
        color: #999;
        line-height: 1;
      }
    }
    
    .modal-content {
      padding: 30rpx;
      
      .product-info {
        display: flex;
        margin-bottom: 40rpx;
        
        .product-image {
          width: 160rpx;
          height: 160rpx;
          border-radius: 12rpx;
          background-color: #f5f5f5;
          object-fit: cover;
        }
        
        .product-details {
          flex: 1;
          margin-left: 20rpx;
          display: flex;
          flex-direction: column;
          justify-content: center;
          
          .product-name {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
            margin-bottom: 12rpx;
          }
          
          .product-price {
            font-size: 32rpx;
            color: #8f1911;
            font-weight: 600;
          }
        }
      }
      
      .quantity-selector {
        margin-bottom: 40rpx;
        
        .quantity-label {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 16rpx;
          display: block;
        }
        
        .quantity-control {
          display: flex;
          align-items: center;
          border: 1rpx solid #e6e6e6;
          border-radius: 8rpx;
          overflow: hidden;
          width: 240rpx;
          
          .quantity-btn {
            width: 80rpx;
            height: 80rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f8f8f8;
            font-size: 32rpx;
          }
          
          .quantity-input {
            flex: 1;
            height: 80rpx;
            text-align: center;
            font-size: 28rpx;
            border-left: 1rpx solid #e6e6e6;
            border-right: 1rpx solid #e6e6e6;
          }
        }
      }
      
      .total-amount {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 20rpx;
        border-top: 1rpx solid #f0f0f0;
        
        .total-label {
          font-size: 28rpx;
          color: #333;
        }
        
        .total-value {
          font-size: 36rpx;
          color: #8f1911;
          font-weight: 600;
        }
      }
      
      // 添加转化产品库存提示样式
      .stock-warning {
        margin-top: 10rpx;
        font-size: 24rpx;
        color: #8f1911;
        display: flex;
        align-items: center;
        
        uni-icons {
          margin-right: 8rpx;
        }
      }
    }
    
    .modal-loading,
    .modal-error {
      padding: 60rpx 30rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .loading-text,
      .error-text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #999;
      }
      
      .retry-btn {
        margin-top: 20rpx;
        padding: 12rpx 30rpx;
        background-color: #8f1911;
        color: #fff;
        border-radius: 30rpx;
        font-size: 26rpx;
      }
    }
    
    .modal-footer {
      display: flex;
      border-top: 1rpx solid #f0f0f0;
      
      .modal-btn {
        flex: 1;
        height: 100rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 30rpx;
        
        &.cancel-btn {
          color: #666;
          background-color: #f8f8f8;
        }
        
        &.confirm-btn {
          color: #fff;
          background-color: #8f1911;
          
          &.disabled {
            opacity: 0.6;
          }
        }
      }
    }
  }
</style>
