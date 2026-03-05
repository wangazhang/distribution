<template>
  <div class="folder-tree-container">
    <!-- 工具栏 -->
    <div class="folder-toolbar">
      <el-button
        type="primary"
        :icon="Plus"
        @click="handleCreateFolder"
        size="small"
      >
        新建文件夹
      </el-button>
      <el-button
        :icon="Refresh"
        @click="refreshTree"
        size="small"
      >
        刷新
      </el-button>
    </div>

    <!-- 文件夹树 -->
    <el-tree
      ref="treeRef"
      :data="folderTreeData"
      :props="treeProps"
      :expand-on-click-node="false"
      :highlight-current="true"
      :current-node-key="currentFolderId"
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      node-key="id"
      default-expand-all
      draggable
      @node-click="handleNodeClick"
      @node-drag-start="handleDragStart"
      @node-drag-end="handleDragEnd"
      @node-drop="handleDrop"
      class="folder-tree"
    >
      <template #default="{ data }">
        <div class="folder-node" :class="{'is-current': currentFolderId === data.id}">
          <!-- 文件夹图标和名称 -->
          <div class="folder-info">
            <Icon
              :icon="data.permissionType === 2 ? 'ep:lock' : 'ep:folder'"
              :class="{'private-folder': data.permissionType === 2}"
            />
            <span class="folder-name">{{ data.name }}</span>
            <span class="folder-count">({{ data.imageCount || 0 }})</span>
          </div>

          <!-- 操作按钮 -->
          <div class="folder-actions">
            <el-button
              v-if="data.id !== 0"
              text
              :icon="Edit"
              @click.stop="handleEditFolder(data)"
              size="small"
              title="重命名"
            />
            <el-button
              text
              :icon="Plus"
              @click.stop="handleCreateSubFolder(data)"
              size="small"
              title="新建子文件夹"
            />
            <el-button
              v-if="data.id !== 0"
              text
              :icon="Delete"
              @click.stop="handleDeleteFolder(data)"
              size="small"
              title="删除"
              class="delete-btn"
            />
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted } from 'vue'
import { Edit, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElTree, type FormInstance, type FormRules } from 'element-plus'
import * as ImageFolderApi from '../../../../api/infra/imageFolder/index'
import { ImageFolderVO, ImageFolderCreateReqVO, ImageFolderUpdateReqVO } from '../../../../api/infra/imageFolder/index'

// 扩展ImageFolderVO接口以包含children属性
interface FolderVO extends Omit<ImageFolderVO, 'children'> {
  children?: FolderVO[]
}

// Props
interface Props {
  folderTree: FolderVO[]
  currentFolderId?: number | null
}

const props = withDefaults(defineProps<Props>(), {
  folderTree: () => [],
  currentFolderId: null
})

// Emits
interface Emits {
  (e: 'folder-select', folder: FolderVO): void
  (e: 'folder-create', data: any): void
  (e: 'folder-update', folder: FolderVO): void
  (e: 'folder-delete', folder: FolderVO): void
  (e: 'folder-move', data: any): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const treeRef = ref<InstanceType<typeof ElTree>>()

// 计算属性
const folderTreeData = computed(() => props.folderTree)

// 树形配置
const treeProps = {
  children: 'children',
  label: 'name',
  value: 'id'
}

// 方法
const refreshTree = () => {
  // 父组件负责数据加载，这里只需要触发刷新事件
  emit('folder-change')
}

// 树节点点击
const handleNodeClick = (data: FolderVO) => {
  emit('folder-select', data)
}

// 拖拽相关
const allowDrag = (draggingNode: any) => {
  // 根目录不能拖拽
  return draggingNode.data?.id !== 0
}

const allowDrop = (draggingNode: any, dropNode: any, type: string) => {
  // 不能拖拽到自己的子节点
  if (draggingNode.data.id === dropNode.data.id) return false

  // 不能拖拽根目录
  if (draggingNode.data.id === 0) return false

  // 只允许作为子节点拖拽
  return type === 'inner'
}

const handleDragStart = (node: any) => {
  console.log('开始拖拽:', node.data.name)
}

const handleDragEnd = () => {
  console.log('拖拽结束')
}

const handleDrop = async (draggingNode: any, dropNode: any, dropType: string) => {
  if (dropType === 'inner' && draggingNode?.data && dropNode?.data) {
    try {
      emit('folder-move', {
        id: draggingNode.data.id,
        targetParentId: dropNode.data.id
      })
    } catch (error) {
      console.error('移动文件夹失败:', error)
    }
  }
}

// 新建文件夹
const handleCreateFolder = () => {
  emit('folder-create', {
    parentId: null,
    permissionType: 1
  })
}

// 新建子文件夹
const handleCreateSubFolder = (parentFolder: FolderVO) => {
  emit('folder-create', {
    parentId: parentFolder.id,
    permissionType: 1
  })
}

// 编辑文件夹
const handleEditFolder = (folder: FolderVO) => {
  emit('folder-update', folder)
}

// 删除文件夹
const handleDeleteFolder = async (folder: FolderVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件夹"${folder.name}"吗？删除后无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    emit('folder-delete', folder)
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除文件夹取消:', error)
    }
  }
}

// 设置当前节点
const setCurrentNode = (folderId: number | null) => {
  nextTick(() => {
    if (treeRef.value && folderId) {
      treeRef.value.setCurrentKey(folderId)
    }
  })
}

// 暴露方法给父组件
defineExpose({
  refreshTree,
  setCurrentNode
})

// 监听当前文件夹ID变化
watch(() => props.currentFolderId, (newId) => {
  setCurrentNode(newId)
}, { immediate: true })
</script>

<style scoped>


/* 响应式设计 */
@media (width <= 768px) {
  .folder-actions {
    opacity: 1;
  }

  .folder-count {
    display: none;
  }
}

.folder-tree-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 4px;
}

.folder-toolbar {
  display: flex;
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
  gap: 8px;
}

.folder-tree {
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}

.folder-node {
  display: flex;
  width: 100%;
  min-height: 36px;
  padding: 6px 0;
  transition: all 0.2s ease;
  justify-content: space-between;
  align-items: center;
}

.folder-node.is-current {
  background-color: #ecf5ff;
  border-radius: 4px;
}

.folder-node:hover {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.folder-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  gap: 6px;
}

.folder-info .icon {
  font-size: 16px;
  color: #409eff;
  flex-shrink: 0;
}

.folder-info .private-folder {
  color: #e6a23c;
}

.folder-name {
  overflow: hidden;
  font-size: 14px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.folder-count {
  margin-left: 4px;
  font-size: 12px;
  color: #909399;
}

.folder-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.folder-node:hover .folder-actions {
  opacity: 1;
}

.delete-btn {
  color: #f56c6c !important;
}

.delete-btn:hover {
  background-color: #fef0f0 !important;
}

/* Element Plus 样式覆盖 */
:deep(.el-tree-node__content) {
  padding: 0 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

:deep(.el-tree-node__content:hover) {
  background-color: transparent;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: transparent;
}

:deep(.el-button--small) {
  padding: 4px 8px;
}

:deep(.el-button--text) {
  padding: 4px;
}
</style>