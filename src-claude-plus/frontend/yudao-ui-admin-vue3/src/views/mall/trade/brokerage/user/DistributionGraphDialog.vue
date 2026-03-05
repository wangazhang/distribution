<template>
  <el-dialog
    v-model="visible"
    title="分销关系图谱"
    width="90%"
    :before-close="handleClose"
    class="distribution-graph-dialog"
    destroy-on-close
    :z-index="2000"
    append-to-body
  >
    <!-- 工具栏 -->
    <div class="graph-toolbar">
      <div class="toolbar-left">
        <el-radio-group v-model="layoutType" @change="changeLayout">
          <el-radio-button value="tree">树形图</el-radio-button>
          <el-radio-button value="compactBox">紧凑树</el-radio-button>
          <el-radio-button value="mindmap">思维导图</el-radio-button>
        </el-radio-group>
        
        <el-divider direction="vertical" />
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户昵称"
          prefix-icon="Search"
          style="width: 200px;"
          @input="handleSearch"
          clearable
        />
      </div>
      
      <div class="toolbar-right">
        <el-button @click="refreshGraph" :icon="Refresh" :loading="loading">刷新数据</el-button>
        <el-button @click="fitView" :icon="FullScreen">适应画布</el-button>
        <el-button @click="resetView" :icon="Refresh">重置视图</el-button>
        <el-button @click="exportImage" :icon="Download">导出图片</el-button>
        <el-button @click="toggleFullscreen" :icon="FullScreen">
          {{ isFullscreen ? '退出全屏' : '全屏' }}
        </el-button>
      </div>
    </div>
    
    <!-- 图谱容器 -->
    <div
      ref="graphContainer"
      class="graph-container"
      :class="{ 'fullscreen': isFullscreen }"
    >
      <div v-if="loading" class="graph-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载分销关系图谱...</span>
      </div>

      <div v-else-if="!loading && !graphData" class="graph-empty">
        <el-icon class="empty-icon"><Warning /></el-icon>
        <div class="empty-text">
          <p>暂无团队关系数据</p>
          <p class="empty-desc">该用户可能还没有建立分销团队关系</p>
        </div>
      </div>
    </div>
    
    <!-- 图例和统计信息 -->
    <div class="graph-footer">
      <div class="graph-legend">
        <div class="legend-title">等级图例</div>
        <div class="legend-items">
          <div
            class="legend-item"
            v-for="level in levelDefinitions"
            :key="level.key"
          >
            <div 
              class="legend-color" 
              :style="{ backgroundColor: level.color }"
            ></div>
            <span>{{ level.name }}</span>
          </div>
        </div>
      </div>
      
      <div class="graph-stats">
        <div class="stats-title">团队统计</div>
        <div class="stats-items">
          <div class="stats-item">
            <span class="stats-label">总人数：</span>
            <span class="stats-value">{{ teamStats.totalCount }}</span>
          </div>
          <div class="stats-item">
            <span class="stats-label">层级数：</span>
            <span class="stats-value">{{ teamStats.levelCount }}</span>
          </div>
          <div class="stats-item">
            <span class="stats-label">团队佣金：</span>
            <span class="stats-value">¥{{ teamStats.totalCommission }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 节点详情弹窗 -->
    <el-dialog
      v-model="nodePopoverVisible"
      title="用户详情"
      width="400px"
      :show-close="true"
      :modal="false"
      :append-to-body="true"
      :z-index="2100"
      class="node-detail-dialog"
    >
      <div v-if="selectedNode" class="node-detail">
        <div class="node-header">
          <el-avatar :src="selectedNode.avatar" :size="50" />
          <div class="node-info">
            <div class="node-name">{{ selectedNode.nickname }}</div>
            <div class="node-level" :style="getLevelTagStyle(selectedNode.levelName)">
              {{ selectedNode.levelName || '普通用户' }}
            </div>
          </div>
        </div>
        <div class="node-stats">
          <div class="stat-row">
            <span>用户ID：</span>
            <span>{{ selectedNode.id }}</span>
          </div>
          <div class="stat-row">
            <span>下级人数：</span>
            <span>{{ selectedNode.childCount || 0 }}</span>
          </div>
          <div class="stat-row">
            <span>累计佣金：</span>
            <span class="commission-amount">¥{{ (selectedNode.totalCommission || 0).toFixed(2) }}</span>
          </div>
          <div class="stat-row">
            <span>注册时间：</span>
            <span>{{ formatDate(selectedNode.createTime) }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="openBrokerageDetail">佣金详情</el-button>
          <el-button @click="openMaterialDetail">物料详情</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 内置复用的弹窗，保障图谱内部也能直接打开 -->
    <BrokerageDetailDialog ref="graphBrokerageDetailDialogRef" />
    <MaterialDetailDialog ref="graphMaterialDetailDialogRef" />
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, onUnmounted, nextTick, watch, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { FullScreen, Refresh, Download, Loading, Warning } from '@element-plus/icons-vue'
import G6 from '@antv/g6'

import * as BrokerageUserApi from '@/api/mall/trade/brokerage/user'
import BrokerageDetailDialog from '@/views/mall/trade/brokerage/user/BrokerageDetailDialog.vue'
import MaterialDetailDialog from '@/views/mall/trade/brokerage/user/MaterialDetailDialog.vue'

defineOptions({ name: 'DistributionGraphDialog' })

// Props
interface Props {
  modelValue?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'node-click': [node: any]
  'open-brokerage-detail': [data: { userId: number; nickname: string }]
  'open-material-detail': [data: { userId: number; nickname: string }]
}>()

// 响应式数据
const visible = ref(false)
const loading = ref(false)
const isFullscreen = ref(false)
const layoutType = ref('tree')
const searchKeyword = ref('')
const graphContainer = ref<HTMLElement>()
const nodePopoverVisible = ref(false)
const selectedNode = ref<any>(null)
const graphBrokerageDetailDialogRef = ref()
const graphMaterialDetailDialogRef = ref()
const instance = getCurrentInstance()

/** 判断父组件是否监听指定事件 */
const toHandlerKey = (event: string) => {
  return (
    'on' + event.replace(/(?:^|-)(\w)/g, (_, c: string) => (c ? c.toUpperCase() : '')).replace(/-/g, '')
  )
}

const hasParentListener = (event: string) => {
  const props = instance?.vnode?.props ?? {}
  const handlerKey = toHandlerKey(event)
  return typeof props[handlerKey] === 'function'
}

/** 本地兜底打开佣金详情 */
const openBrokerageDetailFallback = (userId: number, nickname: string) => {
  if (graphBrokerageDetailDialogRef.value?.open) {
    graphBrokerageDetailDialogRef.value.open(userId, nickname)
  } else {
    console.warn('图谱内部未找到佣金详情弹窗引用')
  }
}

/** 本地兜底打开物料详情 */
const openMaterialDetailFallback = (userId: number, nickname: string) => {
  if (graphMaterialDetailDialogRef.value?.open) {
    graphMaterialDetailDialogRef.value.open(userId, nickname)
  } else {
    console.warn('图谱内部未找到物料详情弹窗引用')
  }
}

// 图谱实例
let graph: any = null
let graphData: any = null

// 当前用户信息（用于刷新）
const currentUser = ref<{ userId: number; nickname: string } | null>(null)

// 团队统计数据
const teamStats = reactive({
  totalCount: 0,
  levelCount: 0,
  totalCommission: 0
})

// 等级配置：统一维护颜色与关键词，避免图例与节点颜色不一致
const levelDefinitions = [
  {
    key: 'headquarters',
    name: '总公司',
    color: '#ff4d4f',
    keywords: ['总公司', '总部', '总院']
  },
  {
    key: 'branch',
    name: '分公司',
    color: '#fa8c16',
    keywords: ['分公司', '分部', '分院']
  },
  {
    key: 'aesthetician',
    name: '美学师',
    color: '#1890ff',
    keywords: ['美学师', '美学', '导师']
  },
  {
    key: 'svip',
    name: 'SVIP',
    color: '#52c41a',
    keywords: ['svip', '超级vip', '超级会员']
  },
  {
    key: 'vip',
    name: 'VIP',
    color: '#722ed1',
    keywords: ['vip', '贵宾']
  },
  {
    key: 'member',
    name: '会员',
    color: '#13c2c2',
    keywords: ['会员', '代理', '经理', '合伙人']
  },
  {
    key: 'guest',
    name: '游客',
    color: '#8c8c8c',
    keywords: ['游客', '普通用户', '用户', '散客']
  }
] as const

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    cleanup()
  }
})

/** 打开对话框 */
const open = async (userId: number, nickname: string) => {
  visible.value = true
  currentUser.value = { userId, nickname }
  await nextTick()
  await loadGraphData(userId, nickname)

  // 只有在数据加载成功时才初始化图谱
  if (graphData) {
    initGraph()
  }
}

/** 加载图谱数据 */
const loadGraphData = async (userId: number, nickname: string) => {
  try {
    loading.value = true

    // 调用真实API获取团队关系数据
    const response = await BrokerageUserApi.getTeamGraph(userId)

    // 检查返回数据结构
    if (!response || !response.rootNode) {
      throw new Error('返回数据格式不正确')
    }

    // 使用API返回的根节点数据
    graphData = response.rootNode

    // 使用API返回的统计信息，如果没有则计算
    if (response.totalCount && response.levelCount && response.totalCommission) {
      teamStats.totalCount = response.totalCount
      teamStats.levelCount = response.levelCount
      teamStats.totalCommission = response.totalCommission
    } else {
      // 如果API没有返回统计信息，则手动计算
      calculateStats(graphData)
    }

  } catch (error) {
    console.error('加载图谱数据失败:', error)

    // 根据错误类型显示不同的错误信息
    const errorMessage = (error as any)?.message || ''
    if (errorMessage.includes('404')) {
      ElMessage.error('未找到该用户的团队数据')
    } else if (errorMessage.includes('403')) {
      ElMessage.error('没有权限查看该用户的团队数据')
    } else if (errorMessage.includes('返回数据格式不正确')) {
      ElMessage.error('服务器返回数据格式错误，请联系管理员')
    } else {
      ElMessage.error('加载团队关系图谱失败，请稍后重试')
    }

    // 加载失败时使用空数据
    graphData = null
    teamStats.totalCount = 0
    teamStats.levelCount = 0
    teamStats.totalCommission = 0

  } finally {
    loading.value = false
  }
}



/** 计算统计信息 */
const calculateStats = (data: any) => {
  let totalCount = 0
  let maxLevel = 0
  let totalCommission = 0

  const traverse = (node: any, level: number) => {
    totalCount++
    maxLevel = Math.max(maxLevel, level)

    // 修复佣金计算逻辑：正确处理正负数
    const commission = node.totalCommission || 0
    // 佣金可能是正数（收入）或负数（扣除），需要按实际数值累加
    totalCommission += commission

    if (node.children) {
      node.children.forEach((child: any) => traverse(child, level + 1))
    }
  }

  traverse(data, 1)

  teamStats.totalCount = totalCount
  teamStats.levelCount = maxLevel
  // 后端已经返回元单位，无需再次转换
  teamStats.totalCommission = parseFloat(totalCommission.toFixed(2))
}

/** 初始化图谱 */
const initGraph = () => {
  if (!graphContainer.value || !graphData) return

  // 清理之前的图谱
  if (graph) {
    graph.destroy()
  }

  // 注册自定义节点
  registerCustomNode()

  // 创建图谱实例
  graph = new G6.TreeGraph({
    container: graphContainer.value,
    width: graphContainer.value.clientWidth,
    height: 600,
    modes: {
      default: [
        'drag-canvas',
        'zoom-canvas',
        'drag-node'
      ]
    },
    defaultNode: {
      type: 'user-card',
      size: [140, 80]
    },
    defaultEdge: {
      type: 'cubic-vertical',
      style: {
        stroke: '#A3B1BF',
        lineWidth: 2
      }
    },
    layout: {
      type: 'compactBox',
      direction: 'TB',
      getId: (d: any) => d.id,
      getHeight: () => 80,
      getWidth: () => 140,
      getVGap: () => 60,
      getHGap: () => 40
    }
  })

  // 绑定事件
  bindEvents()

  // 渲染数据
  graph.data(graphData)
  graph.render()
  graph.fitView()
  highlightCurrentUserNode()
}

/** 高亮并聚焦当前用户节点 */
const highlightCurrentUserNode = (options: { focus?: boolean } = {}) => {
  if (!graph || !currentUser.value) {
    return
  }
  const targetId = `user_${currentUser.value.userId}`
  const targetNode = graph.findById(targetId)
  if (!targetNode) {
    return
  }

  // 清除已有高亮状态
  graph.getNodes().forEach((node: any) => {
    graph.clearItemStates(node, 'highlight')
  })

  graph.setItemState(targetNode, 'highlight', true)

  if (options.focus !== false) {
    graph.focusItem(targetNode, true, {
      easing: 'easeCubic',
      duration: 400
    })
  }
}

/** 注册自定义节点 */
const registerCustomNode = () => {
  G6.registerNode('user-card', {
    draw(cfg: any, group: any) {
      const { nickname, avatar, levelName, totalCommission, childCount } = cfg

      // 获取等级颜色
      const levelColor = getLevelColor(levelName)

      // 主容器
      const rect = group.addShape('rect', {
        attrs: {
          x: -70,
          y: -40,
          width: 140,
          height: 80,
          radius: 8,
          fill: '#fff',
          stroke: levelColor,
          lineWidth: 2,
          shadowColor: 'rgba(0, 0, 0, 0.1)',
          shadowBlur: 4,
          shadowOffsetY: 2
        },
        name: 'main-rect'
      })

      // 头像
      if (avatar && avatar.trim() !== '') {
        // 创建圆形头像 - 使用自定义绘制方法
        const img = new Image()
        img.crossOrigin = 'anonymous'

        // 先添加占位圆圈
        const avatarCircle = group.addShape('circle', {
          attrs: {
            x: -48,
            y: -18,
            r: 12,
            fill: '#f0f0f0',
            stroke: '#d9d9d9',
            lineWidth: 1
          },
          name: 'avatar-placeholder'
        })

        img.onload = () => {
          // 图片加载成功后，创建圆形头像
          const canvas = document.createElement('canvas')
          const ctx = canvas.getContext('2d')
          const size = 24
          canvas.width = size
          canvas.height = size

          if (ctx) {
            // 绘制圆形裁剪路径
            ctx.beginPath()
            ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2)
            ctx.clip()

            // 绘制图片
            ctx.drawImage(img, 0, 0, size, size)
          }

          // 将canvas转换为dataURL
          const circularImageUrl = canvas.toDataURL()

          // 移除占位圆圈
          avatarCircle.remove()

          // 添加圆形头像
          group.addShape('image', {
            attrs: {
              x: -60,
              y: -30,
              width: 24,
              height: 24,
              img: circularImageUrl
            },
            name: 'avatar-image'
          })

          // 添加圆形边框
          group.addShape('circle', {
            attrs: {
              x: -48,
              y: -18,
              r: 12,
              fill: 'transparent',
              stroke: '#d9d9d9',
              lineWidth: 2
            },
            name: 'avatar-border'
          })
        }

        img.onerror = () => {
          console.warn('头像加载失败:', avatar)
          // 加载失败时显示默认图标
          avatarCircle.attr('fill', '#f0f0f0')
          group.addShape('text', {
            attrs: {
              x: -48,
              y: -14,
              text: '👤',
              fontSize: 14,
              fill: '#999',
              textAlign: 'center',
              textBaseline: 'middle'
            },
            name: 'default-avatar-icon'
          })
        }

        // 开始加载图片
        img.src = avatar
      } else {
        // 没有头像时的背景圆圈
        group.addShape('circle', {
          attrs: {
            x: -48,
            y: -18,
            r: 12,
            fill: '#f0f0f0',
            stroke: '#d9d9d9',
            lineWidth: 1
          },
          name: 'avatar-bg'
        })

        // 默认头像图标
        group.addShape('text', {
          attrs: {
            x: -48,
            y: -14,
            text: '👤',
            fontSize: 14,
            fill: '#999',
            textAlign: 'center',
            textBaseline: 'middle'
          },
          name: 'default-avatar-icon'
        })
      }

      // 昵称
      group.addShape('text', {
        attrs: {
          x: -30,
          y: -22,
          text: nickname || '未知用户',
          fontSize: 12,
          fill: '#333',
          fontWeight: 500,
          textAlign: 'left',
          textBaseline: 'middle'
        },
        name: 'nickname'
      })

      // 等级标签
      if (levelName) {
        group.addShape('rect', {
          attrs: {
            x: -30,
            y: -8,
            width: levelName.length * 8 + 8,
            height: 16,
            radius: 8,
            fill: levelColor,
            opacity: 0.1
          },
          name: 'level-bg'
        })

        group.addShape('text', {
          attrs: {
            x: -26,
            y: 0,
            text: levelName,
            fontSize: 10,
            fill: levelColor,
            textAlign: 'left',
            textBaseline: 'middle'
          },
          name: 'level-text'
        })
      }

      // 统计信息
      const statsText = `下级: ${childCount || 0} | 佣金: ¥${(totalCommission || 0).toFixed(2)}`
      group.addShape('text', {
        attrs: {
          x: 0,
          y: 20,
          text: statsText,
          fontSize: 10,
          fill: '#666',
          textAlign: 'center',
          textBaseline: 'middle'
        },
        name: 'stats'
      })

      return rect
    },

    setState(name: string, value: boolean, item: any) {
      const group = item.getContainer()
      const rect = group.find((e: any) => e.get('name') === 'main-rect')

      if (name === 'hover') {
        if (value) {
          rect.attr('shadowBlur', 8)
          rect.attr('shadowOffsetY', 4)
          rect.attr('stroke', '#1890ff')
          rect.attr('lineWidth', 3)
        } else {
          rect.attr('shadowBlur', 4)
          rect.attr('shadowOffsetY', 2)
          const model = item.getModel()
          rect.attr('stroke', getLevelColor(model.levelName))
          rect.attr('lineWidth', 2)
        }
      }

      if (name === 'highlight') {
        if (value) {
          rect.attr('fill', '#e6f7ff')
          rect.attr('stroke', '#1890ff')
          rect.attr('lineWidth', 3)
        } else {
          rect.attr('fill', '#fff')
          const model = item.getModel()
          rect.attr('stroke', getLevelColor(model.levelName))
          rect.attr('lineWidth', 2)
        }
      }
    }
  })
}

/** 获取等级颜色 */
const getLevelColor = (levelName: string) => {
  const defaultColor =
    levelDefinitions.find(definition => definition.key === 'guest')?.color ?? '#8c8c8c'

  if (!levelName) {
    return defaultColor
  }

  const normalizedName = levelName.toLowerCase()
  for (const definition of levelDefinitions) {
    const match = definition.keywords.some(keyword => normalizedName.includes(keyword.toLowerCase()))
    if (match) {
      return definition.color
    }
  }

  return defaultColor
}

/** 获取等级标签样式 */
const getLevelTagStyle = (levelName: string) => {
  const levelColor = getLevelColor(levelName)

  // 生成浅色背景（在原色基础上增加透明度）
  const getLightBackground = (color: string) => {
    // 将颜色转换为rgba格式，透明度为0.1
    const hex = color.replace('#', '')
    const r = parseInt(hex.substring(0, 2), 16)
    const g = parseInt(hex.substring(2, 4), 16)
    const b = parseInt(hex.substring(4, 6), 16)
    return `rgba(${r}, ${g}, ${b}, 0.1)`
  }

  return {
    backgroundColor: getLightBackground(levelColor),
    borderColor: levelColor,
    color: levelColor // 使用等级颜色作为文字颜色，确保一致性
  }
}

/** 绑定图谱事件 */
const bindEvents = () => {
  if (!graph) return

  // 节点点击事件
  graph.on('node:click', (e: any) => {
    const node = e.item
    const model = node.getModel()
    showNodeDetail(model)
  })

  // 节点悬浮事件
  graph.on('node:mouseenter', (e: any) => {
    const node = e.item
    graph.setItemState(node, 'hover', true)
  })

  graph.on('node:mouseleave', (e: any) => {
    const node = e.item
    graph.setItemState(node, 'hover', false)
  })

  // 画布点击事件（隐藏弹窗）
  graph.on('canvas:click', () => {
    hideNodeDetail()
  })
}

/** 显示节点详情 */
const showNodeDetail = (nodeData: any) => {
  selectedNode.value = nodeData
  nodePopoverVisible.value = true
}

/** 隐藏节点详情 */
const hideNodeDetail = () => {
  nodePopoverVisible.value = false
  selectedNode.value = null
}

/** 切换布局 */
const changeLayout = (type: string) => {
  if (!graph) return

  const layoutConfig = {
    tree: {
      type: 'dendrogram',
      direction: 'TB',
      nodeSep: 50,
      rankSep: 100
    },
    compactBox: {
      type: 'compactBox',
      direction: 'TB',
      getId: (d: any) => d.id,
      getHeight: () => 60,
      getWidth: () => 120,
      getVGap: () => 50,
      getHGap: () => 30
    },
    mindmap: {
      type: 'mindmap',
      direction: 'H',
      getHeight: () => 60,
      getWidth: () => 120,
      getVGap: () => 20,
      getHGap: () => 50
    }
  }

  graph.once('afterlayout', () => {
    highlightCurrentUserNode({ focus: false })
  })

  graph.updateLayout(layoutConfig[type] || layoutConfig.tree)
}

/** 搜索处理 */
const handleSearch = (keyword: string) => {
  if (!graph || !keyword.trim()) {
    // 清除高亮
    graph.getNodes().forEach((node: any) => {
      graph.clearItemStates(node)
    })
    highlightCurrentUserNode({ focus: false })
    return
  }

  // 搜索并高亮匹配的节点
  const nodes = graph.getNodes()
  nodes.forEach((node: any) => {
    const model = node.getModel()
    if (model.nickname && model.nickname.includes(keyword.trim())) {
      graph.setItemState(node, 'highlight', true)
    } else {
      graph.clearItemStates(node)
    }
  })
}

/** 适应画布 */
const fitView = () => {
  if (graph) {
    graph.fitView()
  }
}

/** 重置视图 */
const resetView = () => {
  if (graph) {
    graph.fitView()
    graph.zoomTo(1)
    searchKeyword.value = ''
    handleSearch('')
  }
}

/** 刷新图谱数据 */
const refreshGraph = async () => {
  if (!currentUser.value) {
    ElMessage.warning('无法刷新：缺少用户信息')
    return
  }

  try {
    // 保存当前的视图状态
    let currentZoom = 1
    let currentMatrix = null
    if (graph) {
      currentZoom = graph.getZoom()
      currentMatrix = graph.getGroup().getMatrix()
    }

    // 重新加载数据
    await loadGraphData(currentUser.value.userId, currentUser.value.nickname)

    if (graphData) {
      // 重新初始化图谱
      initGraph()

      // 恢复视图状态
      if (graph && currentMatrix) {
        try {
          graph.zoomTo(currentZoom)
          // 可以根据需要恢复平移状态
        } catch (error) {
          console.warn('恢复视图状态失败:', error)
          graph.fitView()
        }
      }

      ElMessage.success('图谱数据已刷新')
    }
  } catch (error) {
    console.error('刷新图谱失败:', error)
    ElMessage.error('刷新图谱失败，请稍后重试')
  }
}

/** 导出图片 */
const exportImage = () => {
  if (graph) {
    graph.downloadFullImage('分销关系图谱', 'image/png')
    ElMessage.success('图片导出成功')
  }
}

/** 切换全屏 */
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value

  nextTick(() => {
    if (graph) {
      graph.changeSize(
        graphContainer.value!.clientWidth,
        isFullscreen.value ? window.innerHeight - 200 : 600
      )
      graph.fitView()
    }
  })
}



/** 打开佣金详情 */
const openBrokerageDetail = () => {
  console.log('点击佣金详情按钮')
  if (selectedNode.value) {
    console.log('当前选中节点数据:', JSON.stringify(selectedNode.value))
    // 先缓存节点信息，避免关闭节点弹窗后数据被清空导致取不到用户信息
    const nodeInfo = selectedNode.value
    const rawId = String(nodeInfo.id ?? '')
    const userId = Number.parseInt(rawId.replace('user_', ''), 10)
    const nickname = nodeInfo.nickname
    if (Number.isNaN(userId)) {
      ElMessage.warning('无法获取选中用户编号，请刷新后重试')
      return
    }
    console.log('发送佣金详情事件，用户ID:', userId, '昵称:', nickname)

    // 先关闭节点详情弹窗，避免层级冲突
    console.log('关闭节点详情弹窗前 nodePopoverVisible:', nodePopoverVisible.value)
    hideNodeDetail()
    console.log('关闭节点详情弹窗后 nodePopoverVisible:', nodePopoverVisible.value)

    // 延迟一点时间再打开子弹窗，确保父弹窗状态稳定
    setTimeout(() => {
      console.log('延迟回调触发，准备 emit open-brokerage-detail')
      const payload = { userId, nickname }
      emit('open-brokerage-detail', payload)
      console.log('open-brokerage-detail 事件已发送')
      if (!hasParentListener('open-brokerage-detail')) {
        console.log('未检测到父组件监听 open-brokerage-detail，图谱内部直接打开弹窗')
        openBrokerageDetailFallback(userId, nickname)
      }
    }, 100)
  } else {
    console.error('selectedNode.value 为空')
  }
}

/** 打开物料详情 */
const openMaterialDetail = () => {
  console.log('点击物料详情按钮')
  if (selectedNode.value) {
    console.log('当前选中节点数据:', JSON.stringify(selectedNode.value))
    // 先缓存节点信息，避免关闭节点弹窗后数据被清空导致取不到用户信息
    const nodeInfo = selectedNode.value
    const rawId = String(nodeInfo.id ?? '')
    const userId = Number.parseInt(rawId.replace('user_', ''), 10)
    const nickname = nodeInfo.nickname
    if (Number.isNaN(userId)) {
      ElMessage.warning('无法获取选中用户编号，请刷新后重试')
      return
    }
    console.log('发送物料详情事件，用户ID:', userId, '昵称:', nickname)

    // 先关闭节点详情弹窗，避免层级冲突
    console.log('关闭节点详情弹窗前 nodePopoverVisible:', nodePopoverVisible.value)
    hideNodeDetail()
    console.log('关闭节点详情弹窗后 nodePopoverVisible:', nodePopoverVisible.value)

    // 延迟一点时间再打开子弹窗，确保父弹窗状态稳定
    setTimeout(() => {
      console.log('延迟回调触发，准备 emit open-material-detail')
      const payload = { userId, nickname }
      emit('open-material-detail', payload)
      console.log('open-material-detail 事件已发送')
      if (!hasParentListener('open-material-detail')) {
        console.log('未检测到父组件监听 open-material-detail，图谱内部直接打开弹窗')
        openMaterialDetailFallback(userId, nickname)
      }
    }, 100)
  } else {
    console.error('selectedNode.value 为空')
  }
}



/** 格式化日期 */
const formatDate = (date: string) => {
  if (!date) return '-'
  try {
    return date.split(' ')[0] // 直接取日期部分
  } catch (error) {
    return '-'
  }
}

/** 关闭对话框 */
const handleClose = () => {
  visible.value = false
}

/** 清理资源 */
const cleanup = () => {
  if (graph) {
    graph.destroy()
    graph = null
  }
  graphData = null
  selectedNode.value = null
  nodePopoverVisible.value = false
  currentUser.value = null
}

/** 组件卸载时清理 */
onUnmounted(() => {
  cleanup()
})

// 暴露方法
defineExpose({
  open
})
</script>

<style lang="scss" scoped>


// 响应式设计
@media (width <= 768px) {
  .graph-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .toolbar-left,
    .toolbar-right {
      justify-content: center;
    }
  }

  .graph-footer {
    flex-direction: column;
    gap: 16px;

    .graph-stats .stats-items {
      flex-direction: column;
      gap: 8px;
    }
  }
}

.distribution-graph-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.graph-toolbar {
  display: flex;
  padding: 16px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #e8e9eb;
  justify-content: space-between;
  align-items: center;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.graph-container {
  position: relative;
  height: 600px;
  overflow: hidden;
  background: #fff;

  &.fullscreen {
    height: calc(100vh - 200px);
  }

  .graph-loading {
    position: absolute;
    top: 50%;
    left: 50%;
    display: flex;
    font-size: 14px;
    color: #666;
    transform: translate(-50%, -50%);
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .el-icon {
      font-size: 24px;
      color: #1890ff;
    }
  }

  .graph-empty {
    position: absolute;
    top: 50%;
    left: 50%;
    display: flex;
    color: #999;
    text-align: center;
    transform: translate(-50%, -50%);
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .empty-icon {
      font-size: 48px;
      color: #d9d9d9;
    }

    .empty-text {
      p {
        margin: 0;
        font-size: 16px;
        color: #666;

        &:first-child {
          margin-bottom: 8px;
          font-weight: 500;
        }
      }

      .empty-desc {
        font-size: 14px;
        color: #999;
      }
    }
  }
}

.graph-footer {
  display: flex;
  padding: 16px 20px;
  background: #fafbfc;
  border-top: 1px solid #e8e9eb;
  justify-content: space-between;

  .graph-legend {
    .legend-title {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .legend-items {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: #666;

      .legend-color {
        width: 12px;
        height: 12px;
        border-radius: 2px;
      }
    }
  }

  .graph-stats {
    .stats-title {
      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      text-align: right;
    }

    .stats-items {
      display: flex;
      gap: 24px;
    }

    .stats-item {
      font-size: 12px;
      color: #666;

      .stats-label {
        color: #999;
      }

      .stats-value {
        font-weight: 500;
        color: #333;
      }
    }
  }
}

.node-detail-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
  }

  :deep(.el-dialog__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #e8e9eb;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 20px;
    border-top: 1px solid #e8e9eb;
  }
}

.node-detail {
  .node-header {
    display: flex;
    padding: 16px;
    margin-bottom: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    align-items: center;
    gap: 16px;

    .node-info {
      flex: 1;

      .node-name {
        margin-bottom: 6px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .node-level {
        display: inline-block;
        padding: 4px 12px;
        font-size: 12px;
        font-weight: 500;
        border: 1px solid;
        border-radius: 12px;

        /* 背景色、边框色、文字颜色通过内联样式动态设置 */
      }
    }
  }

  .node-stats {
    .stat-row {
      display: flex;
      padding: 8px 0;
      margin-bottom: 12px;
      font-size: 14px;
      border-bottom: 1px solid #f0f0f0;
      justify-content: space-between;
      align-items: center;

      &:last-child {
        margin-bottom: 0;
        border-bottom: none;
      }

      span:first-child {
        font-weight: 500;
        color: #666;
      }

      span:last-child {
        font-weight: 600;
        color: #333;
      }

      .commission-amount {
        font-size: 16px;
        font-weight: 600;
        color: #52c41a;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 16px 0;
}

/* 弹窗层级管理 */
.distribution-graph-dialog {
  z-index: 2000 !important;
}

.node-detail-dialog {
  z-index: 2100 !important;
}

/* 确保子弹窗在最上层 */
:deep(.el-dialog__wrapper) {
  z-index: 3000 !important;
}
</style>
