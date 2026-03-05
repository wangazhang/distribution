<template>
  <view 
    class="yh-tabbar" 
    :style="{ paddingBottom: safeAreaBottomHeight + 'px' }" 
    v-show="!isHidden"
  >
    <view class="tabbar-content">
      <view
        class="tabbar-item"
        v-for="(item, index) in tabbarList"
        :key="index"
        @click="handleTabClick(item.id, item.pagePath)"
      >
        <view class="tabbar-icon">
          <uni-icons
            :type="activeIndex === item.id ? item.activeIcon : item.icon"
            :color="activeIndex === item.id ? activeColor : color"
            size="26"
          ></uni-icons>
        </view>
        <view
          class="tabbar-text"
          :style="{
            color: activeIndex === item.id ? activeColor : color
          }"
        >{{ item.text }}</view>
      </view>
    </view>
  </view>
</template>

<script setup >
import { ref, onMounted, watch, computed } from 'vue';
import sheep from '@/sheep';

// 定义属性
const props = defineProps({
  activeColor: {
    type: String,
    default: '#FF5777'
  },
  color: {
    type: String,
    default: '#999999'
  },
  defaultActiveIndex: {
    type: Number,
    default: 0
  }
});

// 定义事件
const emits = defineEmits(['change']);

// 底部安全区高度
const safeAreaBottomHeight = ref(0);

// 当前激活的标签索引
const activeIndex = ref(props.defaultActiveIndex);

// 是否隐藏底部导航栏
const isHidden = computed(() => sheep.$store('modal').hideTabbar);

// 底部导航项
const tabbarList = [
  {
    id:0,
    pagePath: '/pages/home/index',
    text: '觅天下',
    icon: 'home',
    activeIcon: 'home-filled'
  },
  // {
  //   id:1,
  //   pagePath: '/pages/mall/index',
  //   text: '觅商城',
  //   icon: 'shop',
  //   activeIcon: 'shop-filled'
  // },
  {
    id:2,
    pagePath: '/pages/community/index',
    text: '觅社区',
    icon: 'map',
    activeIcon: 'map-filled'
  },
  {
    id:3,
    pagePath: '/pages/mine/index',
    text: '觅己',
    icon: 'person',
    activeIcon: 'person-filled'
  }
];

// 处理标签点击
const handleTabClick = (index, path) => {
  if (activeIndex.value === index) return;
  
  // 触发短震动反馈
  uni.vibrateShort({
    success: () => {
      console.log('震动成功');
    },
    fail: (err) => {
      console.log('震动失败', err);
    }
  });
  
  activeIndex.value = index;
  // 触发change事件，传递当前选中的索引
  emits('change', index);
};

onMounted(() => {
  // 获取底部安全区高度
  uni.getSystemInfo({
    success: (res) => {
      // @ts-ignore
      safeAreaBottomHeight.value = res.safeAreaInsets?.bottom || 0;
    }
  });
});
</script>

<style lang="scss" scoped>
.yh-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.05);
  z-index: 999;
  
  .tabbar-content {
    display: flex;
    align-items: center;
    height: 100rpx;
    
    .tabbar-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      
      .tabbar-icon {
        margin-bottom: 4rpx;
      }
      
      .tabbar-text {
        font-size: 24rpx;
        line-height: 1;
      }
    }
  }
}
</style> 