<template>
  <s-layout title="变更记录" >
    <view class="page-container">
      <!-- 顶部筛选区域 -->
      <view class="filter-section" v-if="!materialId">
        <text class="filter-title">全部物料记录</text>
      </view>
      
      <view class="filter-section" v-else>
        <view class="material-info">
          <text class="material-name">{{ materialName }}</text>
          <text class="material-id">编号: {{ materialId }}</text>
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
          <view class="retry-btn" @tap="fetchRecords">
            <text>重新加载</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else-if="recordsList.length === 0" class="empty-container">
          <uni-icons type="info" size="30" color="#999"></uni-icons>
          <text class="empty-text">暂无变更记录</text>
        </view>

        <!-- 记录列表 -->
        <view v-else class="records-list">
          <view 
            v-for="(item, index) in recordsList" 
            :key="item.id" 
            class="record-item"
            :class="{ 'use-type': item.action_type === 1 }"
          >
            <!-- 时间线 -->
            <view class="timeline">
              <view class="timeline-dot" :class="{ 'green-dot': item.action_type === 0, 'red-dot': item.action_type === 1 }"></view>
              <view class="timeline-line" v-if="index !== recordsList.length - 1"></view>
            </view>
            
            <!-- 记录内容 -->
            <view class="record-content">
              <view class="record-header">
                <view class="record-type">
                  <text :class="{ 'use-text': item.action_type === 1, 'get-text': item.action_type === 0 }">
                    {{ item.action_type_text }}
                  </text>
                </view>
                <text class="record-date">{{ item.action_date }}</text>
              </view>
              
              <view class="record-body">
                <!-- 显示物料名称 (在查看全部记录时) -->
                <view class="material-row" v-if="!materialId">
                  <text class="label">物料名称</text>
                  <text class="value">{{ item.material_name }}</text>
                </view>
                
<!--                <view class="source-row">-->
<!--                  <text class="label">来源</text>-->
<!--                  <text class="value">{{ item.source_type_text }}</text>-->
<!--                </view>-->
                
                <view class="desc-row" v-if="item.source_desc">
                  <text class="label">描述</text>
                  <text class="value">{{ item.source_desc }}</text>
                </view>
                
                <view class="amount-row">
                  <text class="label">数量变化</text>
                  <view class="value">
                    <!-- 修正显示逻辑：actionType=0（获得）显示+，actionType=1（使用）显示- -->
                    <text>{{ item.before_amount }}</text>
                    <text :class="item.action_type === 0 ? 'plus-symbol' : 'minus-symbol'">
                      {{ item.action_type === 0 ? ' + ' : ' - ' }}
                    </text>
                    <text :class="item.action_type === 0 ? 'plus-symbol' : 'minus-symbol'">{{ item.amount }}</text>
                    <text> = </text>
                    <text class="final-amount">{{ item.after_amount }}</text>
                    <text> {{ item.unit || '件' }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 上拉加载更多 -->
        <uni-load-more 
          v-if="recordsList.length > 0" 
          :status="loadMoreStatus" 
          :contentText="loadingText"
          @clickLoadMore="loadMore"
        ></uni-load-more>

        <!-- 底部安全区 -->
        <view class="safe-area-bottom"></view>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue';
  import { getMaterialRecords, getMaterialList } from '@/yehu/api/mine';
  import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';

  // 物料ID (如果是查看特定物料的记录)
  const materialId = ref(null);
  const materialName = ref('');
  
  // 记录列表数据
  const recordsList = ref([]);
  const pagination = ref({
    pageNo: 1,
    pageSize: 10,
    total: 0
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
    contentnomore: '没有更多数据了'
  };



  // 格式化日期
  const formatDate = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
  };

  // 获取记录列表
  const fetchRecords = async (isRefresh = true) => {
    if (isRefresh) {
      loading.value = true;
      error.value = false;
      pagination.value.pageNo = 1;
      recordsList.value = [];
      console.log('开始刷新数据，materialId:', materialId.value);
    }

    try {
      const params = {
        pageNo: pagination.value.pageNo,
        pageSize: pagination.value.pageSize,
        materialId: materialId.value
      };
      
      console.log('请求参数:', params);

      const res = await getMaterialRecords(params);
      console.log('接口响应:', res);
      
      // 处理返回的数据
      if (res && res.data&& res.data.list && res.data.list.length > 0) {
        // 转换后端数据格式为前端所需格式
        const processedList = res.data.list.map(item => ({
          id: item.id,
          material_id: item.materialId,
          material_name: item.materialName,
          action_type: item.actionType, // 0-获得, 1-使用
          action_type_text: item.actionType === 0 ? '获得' : '使用',
          amount: item.amount,
          source_type: item.sourceType, // 来源类型
          source_type_text: item.sourceDesc,
          source_desc: item.sourceDesc || '',
          before_amount: item.beforeAmount,
          after_amount: item.afterAmount,
          unit: item.unit || '件',
          // 修正时间显示：使用createTime时间戳显示完整日期时间
          action_date: formatDate(item.createTime), // 使用原始时间戳显示精确到秒的时间
        }));
        
        if (isRefresh) {
          recordsList.value = processedList;
        } else {
          recordsList.value = [...recordsList.value, ...processedList];
        }
        
        pagination.value.total = res.data.total || 0;
        
        // 更新加载更多状态
        loadMoreStatus.value = recordsList.value.length >= pagination.value.total ? 'noMore' : 'more';
        console.log('加载状态更新为:', loadMoreStatus.value, '当前记录数:', recordsList.value.length, '总记录数:', pagination.value.total);
      } else {
        if (isRefresh) {
          recordsList.value = [];
        }
        loadMoreStatus.value = 'noMore';
        console.log('无更多数据');
      }
      
      console.log('记录列表加载完成', recordsList.value);
    } catch (err) {
      console.error('获取记录列表失败', err);
      error.value = true;
      errorMessage.value = err.message || '数据加载失败，请重试';
    } finally {
      loading.value = false;
      uni.stopPullDownRefresh();
    }
  };

  // 页面加载时获取物料ID参数
  onLoad((options) => {
    if (options.materialId) {
      materialId.value = options.materialId;
      // fetchMaterialInfo(materialId.value);
    }
    // 强制初始化加载
    setTimeout(() => {
      fetchRecords();
    }, 0);
  });

  // 页面下拉刷新
  onPullDownRefresh(async () => {
    console.log('触发下拉刷新');
    await fetchRecords(true);
  });

  // 加载更多数据
  const loadMore = () => {
    if (loading.value || loadMoreStatus.value === 'noMore') return;
    
    // 更新加载状态
    loadMoreStatus.value = 'loading';
    console.log('触发加载更多');
    
    // 增加页码
    pagination.value.pageNo += 1;
    
    // 加载下一页数据（传入false表示不刷新列表，而是追加）
    fetchRecords(false);
  };
  
  // 页面上拉触底
  onReachBottom(() => {
    console.log('触发上拉触底');
    if (recordsList.value.length > 0 && !loading.value && loadMoreStatus.value !== 'noMore') {
      loadMore();
    }
  });
</script>

<style scoped lang="scss">
.page-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  position: relative;
}

.filter-section {
  padding: 30rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
  
  .filter-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .material-info {
    display: flex;
    flex-direction: column;
    
    .material-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 10rpx;
    }
    
    .material-id {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.page-content {
  padding: 0 30rpx 30rpx;
  
  .records-list {
    .record-item {
      display: flex;
      margin-bottom: 20rpx;
      
      .timeline {
        position: relative;
        width: 40rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        
        .timeline-dot {
          width: 20rpx;
          height: 20rpx;
          border-radius: 50%;
          background-color: #8f1911;
          z-index: 2;
          margin-top: 40rpx;
          
          &.green-dot {
            background-color: #17a05d;
          }
          
          &.red-dot {
            background-color: #8f1911;
          }
        }
        
        .timeline-line {
          position: absolute;
          top: 50rpx;
          width: 2rpx;
          height: calc(100% - 20rpx);
          background-color: #ddd;
          z-index: 1;
        }
      }
      
      &.use-type {
        .timeline-dot {
          background-color: #4a4a4a;
        }
      }
      
      .record-content {
        flex: 1;
        background-color: #fff;
        border-radius: 16rpx;
        padding: 20rpx;
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
        
        .record-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16rpx;
          
          .record-type {
            .get-text {
              color: #17a05d;
              font-weight: 600;
              font-size: 30rpx;
            }
            
            .use-text {
              color: #8f1911;
              font-weight: 600;
              font-size: 30rpx;
            }
          }
          
          .record-date {
            font-size: 24rpx;
            color: #999;
          }
        }
        
        .record-body {
          .material-row,
          .source-row,
          .desc-row,
          .amount-row {
            display: flex;
            margin-bottom: 12rpx;
            
            .label {
              width: 140rpx;
              font-size: 26rpx;
              color: #999;
            }
            
            .value {
              flex: 1;
              font-size: 26rpx;
              color: #333;
            }
          }
          
          .amount-row {
            .value {
              font-weight: 600;
              display: flex;
              align-items: center;
              
              .plus-symbol {
                color: #17a05d;
                font-weight: bold;
              }
              
              .minus-symbol {
                color: #8f1911;
                font-weight: bold;
              }
              
              .final-amount {
                color: #333;
                font-weight: bold;
              }
            }
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
}
</style>
