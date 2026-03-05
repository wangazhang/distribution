<template>
  <view class="yh-image-grid" :style="containerStyle">
    <template v-if="loading">
      <view class="loading-container">
        <uni-load-more status="loading" />
      </view>
    </template>
    <template v-else-if="error">
      <view class="error-container">
        <text>加载失败，请重试</text>
        <button class="retry-btn" @tap="fetchConfig">重试</button>
      </view>
    </template>
    <template v-else>
      <block v-for="(row, rowIndex) in processedData" :key="rowIndex">
        <view 
          class="image-row" 
          :style="{
            marginBottom: rowIndex < processedData.length - 1 ? `${config.rowGap}rpx` : '0'
          }"
        >
          <view 
            v-for="(item, colIndex) in row" 
            :key="colIndex" 
            class="image-item"
            :style="{
              width: item.width,
              marginRight: colIndex < row.length - 1 ? `${config.columnGap}rpx` : '0'
            }"
            @tap="handleImageClick(item)"
          >
            <image 
              :src="item.src" 
              mode="widthFix" 
              :style="{
                borderRadius: `${config.borderRadius}rpx`,
                width: '100%'
              }"
            ></image>
          </view>
        </view>
      </block>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, defineProps, defineEmits } from 'vue';

const props = defineProps({
  // API配置URL或配置对象ID
  configId: {
    type: [String, Number],
    default: ''
  },
  // 是否自动加载配置
  autoLoad: {
    type: Boolean,
    default: true
  },
  // 预设的配置（可选，如果提供则不会从API加载）
  presetConfig: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['click', 'loaded', 'error']);

// 状态管理
const loading = ref(false);
const error = ref(false);
const config = ref({
  // 图片数据列表 [{src, link, params}, ...]
  data: [],
  // 布局类型: 'single'(一行一张), 'multiple'(一行多张)
  layout: 'single',
  // 一行多张时每行几个
  columns: 2,
  // 容器左右边距
  margin: 20,
  // 行间距
  rowGap: 20,
  // 列间距
  columnGap: 20,
  // 图片圆角
  borderRadius: 8
});

// 容器样式
const containerStyle = computed(() => {
  return {
    padding: `0 ${config.value.margin}rpx`
  };
});

// 处理数据，根据布局模式转换为二维数组
const processedData = computed(() => {
  if (config.value.layout === 'single') {
    // 一行一张
    return config.value.data.map(item => [{
      ...item,
      width: '100%'
    }]);
  } else {
    // 一行多张
    const result = [];
    const itemWidth = `${(100 - (config.value.columns - 1) * config.value.columnGap / (750 / 100)) / config.value.columns}%`;
    
    for (let i = 0; i < config.value.data.length; i += config.value.columns) {
      const row = [];
      for (let j = 0; j < config.value.columns; j++) {
        if (i + j < config.value.data.length) {
          row.push({
            ...config.value.data[i + j],
            width: itemWidth
          });
        }
      }
      result.push(row);
    }
    return result;
  }
});

// 从API获取配置
const fetchConfig = async () => {
  // 如果有预设配置，直接使用而不请求API
  if (props.presetConfig) {
    config.value = {
      ...config.value,
      ...props.presetConfig
    };
    emit('loaded', config.value);
    return;
  }
  
  // 如果没有配置ID，不执行请求
  if (!props.configId) {
    error.value = true;
    emit('error', new Error('未提供configId'));
    return;
  }
  
  loading.value = true;
  error.value = false;
  
  try {
    // 根据实际API调整请求地址和参数
    const response = await uni.request({
      url: `/api/components/image-grid/config/${props.configId}`,
      method: 'GET'
    });
    
    if (response.statusCode === 200 && response.data) {
      // 更新配置
      config.value = {
        ...config.value,
        ...response.data
      };
      emit('loaded', config.value);
    } else {
      throw new Error('获取配置失败');
    }
  } catch (err) {
    console.error('加载图片组件配置失败', err);
    error.value = true;
    emit('error', err);
  } finally {
    loading.value = false;
  }
};

// 初始化
onMounted(() => {
  if (props.autoLoad) {
    fetchConfig();
  }
});

// 处理图片点击事件
const handleImageClick = (item) => {
  if (!item.link) {
    emit('click', item);
    return;
  }
  
  try {
    // 处理振动反馈
    uni.vibrateShort({
      success: () => {
        console.log('点击震动成功');
      }
    });
    
    // 处理导航
    const params = item.params ? item.params : {};
    let urlParams = '';
    
    if (Object.keys(params).length > 0) {
      urlParams = '?' + Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join('&');
    }
    
    uni.navigateTo({
      url: `${item.link}${urlParams}`,
      fail: (err) => {
        console.error('页面跳转失败', err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
    
    // 向父组件发送点击事件
    emit('click', item);
  } catch (error) {
    console.error('处理点击事件失败', error);
  }
};

// 对外暴露方法
defineExpose({
  reload: fetchConfig,
  setConfig: (newConfig) => {
    config.value = {
      ...config.value,
      ...newConfig
    };
  }
});
</script>

<style lang="scss" scoped>
.yh-image-grid {
  width: 100%;
  box-sizing: border-box;
  
  .loading-container, .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 200rpx;
    padding: 40rpx 0;
  }
  
  .error-container {
    text {
      color: #999;
      margin-bottom: 20rpx;
    }
    
    .retry-btn {
      font-size: 28rpx;
      padding: 10rpx 30rpx;
      background-color: #f0f0f0;
      border-radius: 6rpx;
    }
  }
  
  .image-row {
    display: flex;
    width: 100%;
  }
  
  .image-item {
    box-sizing: border-box;
    
    image {
      display: block;
    }
  }
}
</style> 