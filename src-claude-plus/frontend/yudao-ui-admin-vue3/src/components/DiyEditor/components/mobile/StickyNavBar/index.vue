<template>
  <div class="sticky-navbar-container">
    <!-- 吸顶导航栏 -->
    <div class="sticky-navbar" :style="navbarStyle">
      <div class="nav-items">
        <div
          v-for="(item, index) in contentSections"
          :key="index"
          class="nav-item"
          :class="{ 
            active: index === activeIndex,
            'auto-width': contentSections.length <= 5 // 少于等于5个时采用等宽布局
          }"
          :style="contentSections.length <= 5 ? 'flex: 1' : ''"
          @click="activeIndex = index"
        >
          <span
            class="nav-text"
            :style="{
              color: index === activeIndex ? (formData?.activeColor || '#8F1911') : (formData?.textColor || '#333333')
            }"
          >
            {{ item.title || `导航${index + 1}` }}
          </span>
          <div
            v-if="showUnderline && index === activeIndex"
            class="nav-underline"
            :style="{ backgroundColor: formData?.underlineColor || '#8F1911' }"
          ></div>
        </div>
      </div>
    </div>
    
    <!-- 内容区域预览 -->
    <div class="content-sections">
      <div v-for="(section, index) in contentSections" :key="index" class="content-section">
        <div class="section-header">
          <span class="section-anchor">锚点ID: {{ section.anchorId }}</span>
        </div>
        
        <div v-if="section.components && section.components.length === 0" class="empty-components">
          <i class="el-icon-info"></i>
          <span>该区域暂无组件，请在属性面板中添加</span>
        </div>
        
        <div v-else-if="section.components && section.components.length > 0" class="component-list">
          <div v-for="(component, cIndex) in section.components" :key="cIndex" class="component-item">
            <div class="component-header">
              <i class="el-icon-picture-outline"></i>
              <span>{{ getComponentName(component.type) }}</span>
            </div>
            <!-- 组件预览 -->
            <div class="component-preview">
              <template v-if="component.type && resolveComponentName(component.type)">
                <!-- 基础组件 -->
                <SearchBar v-if="component.type === 'SearchBar'" :property="component.property" />
                <NoticeBar v-else-if="component.type === 'NoticeBar'" :property="component.property" />
                <MenuSwiper v-else-if="component.type === 'MenuSwiper'" :property="component.property" />
                <MenuList v-else-if="component.type === 'MenuList'" :property="component.property" />
                <MenuGrid v-else-if="component.type === 'MenuGrid'" :property="component.property" />
                <Popover v-else-if="component.type === 'Popover'" :property="component.property" />
                <FloatingActionButton v-else-if="component.type === 'FloatingActionButton'" :property="component.property" />
                
                <!-- 图文组件 -->
                <ImageBar v-else-if="component.type === 'ImageBar'" :property="component.property" />
                <Carousel v-else-if="component.type === 'Carousel'" :property="component.property" />
                <TitleBar v-else-if="component.type === 'TitleBar'" :property="component.property" />
                <MagicCube v-else-if="component.type === 'MagicCube'" :property="component.property" />
                <VideoPlayer v-else-if="component.type === 'VideoPlayer'" :property="component.property" />
                <Divider v-else-if="component.type === 'Divider'" :property="component.property" />
                <HotZone v-else-if="component.type === 'HotZone'" :property="component.property" />
                
                <!-- 商品组件 -->
                <ProductCard v-else-if="component.type === 'ProductCard'" :property="component.property" />
                <ProductList v-else-if="component.type === 'ProductList'" :property="component.property" />
                
                <!-- 营销组件 -->
                <PromotionCombination v-else-if="component.type === 'PromotionCombination'" :property="component.property" />
                <PromotionSeckill v-else-if="component.type === 'PromotionSeckill'" :property="component.property" />
                <PromotionPoint v-else-if="component.type === 'PromotionPoint'" :property="component.property" />
                <CouponCard v-else-if="component.type === 'CouponCard'" :property="component.property" />
                <PromotionArticle v-else-if="component.type === 'PromotionArticle'" :property="component.property" />
                
                <!-- 用户组件 -->
                <UserCard v-else-if="component.type === 'UserCard'" :property="component.property" />
                <UserOrder v-else-if="component.type === 'UserOrder'" :property="component.property" />
                <UserWallet v-else-if="component.type === 'UserWallet'" :property="component.property" />
                <UserCoupon v-else-if="component.type === 'UserCoupon'" :property="component.property" />
              </template>
              <div v-else class="component-placeholder">
                <i class="el-icon-picture"></i>
                <span>组件预览不可用</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { StickyNavBarProperty } from './config'
import { useVModel } from '@vueuse/core'
import { ref, computed } from 'vue'
import { componentConfigs, components } from '@/components/DiyEditor/components/mobile'

// 导入所有可能需要的组件（只导入存在的组件）
import SearchBar from '../SearchBar/index.vue'
import NoticeBar from '../NoticeBar/index.vue'
import MenuSwiper from '../MenuSwiper/index.vue'
import MenuList from '../MenuList/index.vue'
import MenuGrid from '../MenuGrid/index.vue'
import Popover from '../Popover/index.vue'
import FloatingActionButton from '../FloatingActionButton/index.vue'
import ImageBar from '../ImageBar/index.vue'
import Carousel from '../Carousel/index.vue'
import TitleBar from '../TitleBar/index.vue'
import MagicCube from '../MagicCube/index.vue'
import VideoPlayer from '../VideoPlayer/index.vue'
import Divider from '../Divider/index.vue'
import HotZone from '../HotZone/index.vue'
import ProductCard from '../ProductCard/index.vue'
import ProductList from '../ProductList/index.vue'
import PromotionCombination from '../PromotionCombination/index.vue'
import PromotionSeckill from '../PromotionSeckill/index.vue'
import PromotionPoint from '../PromotionPoint/index.vue'
import CouponCard from '../CouponCard/index.vue'
import PromotionArticle from '../PromotionArticle/index.vue'
import UserCard from '../UserCard/index.vue'
import UserOrder from '../UserOrder/index.vue'
import UserWallet from '../UserWallet/index.vue'
import UserCoupon from '../UserCoupon/index.vue'

defineOptions({ name: 'StickyNavBar' })

// 修改props定义，支持property和modelValue两种方式
const props = defineProps({
  // 兼容旧版property方式
  property: {
    type: Object as () => StickyNavBarProperty,
    default: () => ({})
  },
  // 新版modelValue方式
  modelValue: {
    type: Object as () => StickyNavBarProperty,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue'])
const formData = useVModel(props, 'modelValue', emit)

// 当前激活的导航索引
const activeIndex = ref(0)

// 计算属性：获取内容区域数据
const contentSections = computed(() => {
  // 优先使用modelValue方式的数据
  if (formData.value?.contentSections?.length > 0) {
    return formData.value.contentSections
  }
  // 其次使用property方式的数据
  if (props.property?.contentSections?.length > 0) {
    return props.property.contentSections
  }
  // 兼容旧版list数据
  if (formData.value && 'list' in formData.value && Array.isArray(formData.value.list) && formData.value.list.length > 0) {
    return formData.value.list.map(item => ({
      id: `nav-${Math.random().toString(36).substring(2, 10)}`,
      title: item.text,
      anchorId: item.anchorId,
      sectionTitle: item.text,
      components: []
    }))
  }
  if (props.property && 'list' in props.property && Array.isArray(props.property.list) && props.property.list.length > 0) {
    return props.property.list.map(item => ({
      id: `nav-${Math.random().toString(36).substring(2, 10)}`,
      title: item.text,
      anchorId: item.anchorId,
      sectionTitle: item.text,
      components: []
    }))
  }
  // 默认返回空数组
  return []
})

// 是否显示下划线
const showUnderline = computed(() => {
  return formData.value?.showUnderline ?? props.property?.showUnderline ?? true
})

// 导航栏样式
const navbarStyle = computed(() => {
  // 从formData或property中获取样式数据
  const data = formData.value || props.property || {}
  const style = data.style || {}
  
  return {
    backgroundColor: data.bgColor || '#ffffff',
    paddingTop: `${style.paddingTop || 10}px`,
    paddingRight: `${style.paddingRight || 0}px`,
    paddingBottom: `${style.paddingBottom || 10}px`,
    paddingLeft: `${style.paddingLeft || 0}px`,
    marginTop: `${style.marginTop || 0}px`,
    marginRight: `${style.marginRight || 0}px`,
    marginBottom: `${style.marginBottom || 0}px`,
    marginLeft: `${style.marginLeft || 0}px`,
    borderRadius: `${style.borderRadius || 0}px`,
  }
})

// 获取组件名称
const getComponentName = (componentType: string) => {
  const config = componentConfigs[componentType]
  return config?.name || componentType
}

// 解析组件名称
const resolveComponentName = (type: string) => {
  return components[type] || null
}
</script>

<style lang="scss" scoped>
.sticky-navbar-container {
  display: flex;
  width: 100%;
  flex-direction: column;
}

.sticky-navbar {
  width: 100%;
  overflow-x: auto; /* 添加横向滚动 */
  box-shadow: 0 1px 6px rgb(0 0 0 / 5%);
  box-sizing: border-box;
  
  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  
  .nav-items {
    display: flex;
    min-width: 100%; /* 确保内容填充容器 */
    padding: 0 12px;
    box-sizing: border-box;
  }

  .nav-item {
    position: relative;
    display: inline-flex;
    min-width: 80px; /* 设置最小宽度 */
    padding: 8px 12px;
    text-align: center;
    cursor: pointer;
    justify-content: center;
    
    &.auto-width {
      /* 当导航项较少时使用等宽布局 */
      flex: 1;
    }
    
    .nav-text {
      font-size: 14px;
      white-space: nowrap;
      transition: color 0.3s;
    }

    .nav-underline {
      position: absolute;
      bottom: 0;
      left: 50%;
      width: 20px;
      height: 3px;
      border-radius: 1.5px;
      transform: translateX(-50%);
      transition: all 0.3s;
    }
    
    &.active {
      font-weight: bold;
    }
  }
}

.content-sections {
  margin-top: 10px;
  
  .content-section {
    margin-bottom: 10px;
    overflow: hidden;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background-color: #f5f7fa;
      border-bottom: 1px dashed #dcdfe6;
      
      .section-anchor {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .empty-components {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px 0;
      color: #909399;
      
      i {
        margin-bottom: 8px;
        font-size: 20px;
      }
      
      span {
        font-size: 12px;
      }
    }
    
    .component-list {
      padding: 10px;
      
      .component-item {
        margin-bottom: 12px;
        overflow: hidden;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .component-header {
          display: flex;
          align-items: center;
          padding: 8px 12px;
          background-color: #f5f7fa;
          border-bottom: 1px solid #ebeef5;
          
          i {
            margin-right: 8px;
            color: #909399;
          }
          
          span {
            font-size: 12px;
            font-weight: 500;
          }
        }
        
        .component-preview {
          min-height: 60px;
          padding: 10px;
          background-color: #fff;
          
          .component-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 60px;
            color: #c0c4cc;
            
            i {
              margin-bottom: 8px;
              font-size: 24px;
            }
            
            span {
              font-size: 12px;
            }
          }
        }
      }
    }
  }
}
</style> 