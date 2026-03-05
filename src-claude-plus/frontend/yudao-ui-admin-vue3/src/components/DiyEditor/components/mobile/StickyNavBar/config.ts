import { DiyComponent } from '@/components/DiyEditor/util'

// 吸顶导航属性
export interface StickyNavBarProperty {
  // 基础配置
  bgColor: string
  activeColor: string
  textColor: string
  showUnderline: boolean
  underlineColor: string
  
  // 内容区域配置
  contentSections: StickyNavContentSection[]
  
  // 样式设置
  style: {
    // 外边距
    margin: number
    marginTop: number
    marginRight: number
    marginBottom: number
    marginLeft: number
    // 内边距
    padding: number
    paddingTop: number
    paddingRight: number
    paddingBottom: number
    paddingLeft: number
    // 边框圆角
    borderRadius: number
  }
  
  // 高级设置
  settings: {
    // 是否启用吸顶效果
    enableSticky: boolean
    // 吸顶时的偏移量（顶部距离）
    stickyOffset: number
    // 是否显示指示器
    showIndicator: boolean
    // 是否滚动时自动更新激活项
    autoUpdateActive: boolean
    // 滚动动画时长(ms)
    scrollDuration: number
  }
}

// 吸顶导航内容区域
export interface StickyNavContentSection {
  id: string;
  title: string;
  anchorId: string;
  components: StickyNavSectionComponent[];
}

// 内容区域组件
export interface StickyNavSectionComponent {
  // 组件类型
  type: string
  // 组件属性
  property: any
}

// 定义组件
export const component = {
  id: 'StickyNavBar',
  name: '吸顶导航',
  icon: 'tabler:layout-navbar-expand',
  position: 'center',
  property: {
    bgColor: '#ffffff',
    activeColor: '#8F1911',
    textColor: '#333333',
    showUnderline: true,
    underlineColor: '#8F1911',
    
    contentSections: [
      {
        id: 'nav-1',
        title: '推荐',
        anchorId: 'section-1',
        components: []
      },
      {
        id: 'nav-2',
        title: '热门',
        anchorId: 'section-2',
        components: []
      },
      {
        id: 'nav-3',
        title: '新品',
        anchorId: 'section-3',
        components: []
      },
      {
        id: 'nav-4',
        title: '活动',
        anchorId: 'section-4',
        components: []
      }
    ],
    
    style: {
      margin: 0,
      marginTop: 0,
      marginRight: 0,
      marginBottom: 0,
      marginLeft: 0,
      padding: 0,
      paddingTop: 10,
      paddingRight: 0,
      paddingBottom: 10,
      paddingLeft: 0,
      borderRadius: 0
    },
    
    settings: {
      enableSticky: true,
      stickyOffset: 0,
      showIndicator: true,
      autoUpdateActive: true,
      scrollDuration: 300
    }
  }
} as DiyComponent<StickyNavBarProperty> 