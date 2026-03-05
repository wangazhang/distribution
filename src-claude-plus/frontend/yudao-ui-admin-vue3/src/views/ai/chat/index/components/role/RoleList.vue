<template>
  <div class="card-list" ref="tabsRef" @scroll="handleTabsScroll">
    <div class="card-item" v-for="role in roleList" :key="role.id">
      <el-card class="card" body-class="card-body">
        <!-- 更多操作 -->
        <div class="more-container" v-if="showMore">
          <el-dropdown @command="handleMoreClick">
            <span class="el-dropdown-link">
              <el-button type="text">
                <el-icon><More /></el-icon>
              </el-button>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="['edit', role]">
                  <Icon icon="ep:edit" color="#787878" />编辑
                </el-dropdown-item>
                <el-dropdown-item :command="['delete', role]" style="color: red">
                  <Icon icon="ep:delete" color="red" />删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <!-- 角色信息 -->
        <div>
          <img class="avatar" :src="role.avatar" />
        </div>
        <div class="right-container">
          <div class="content-container">
            <div class="title">{{ role.name }}</div>
            <div class="description">{{ role.description }}</div>
          </div>
          <div class="btn-container">
            <el-button type="primary" size="small" @click="handleUseClick(role)">使用</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatRoleVO } from '@/api/ai/model/chatRole'
import { PropType, ref } from 'vue'
import { More } from '@element-plus/icons-vue'

const tabsRef = ref<any>() // tabs ref

// 定义属性
const props = defineProps({
  loading: {
    type: Boolean,
    required: true
  },
  roleList: {
    type: Array as PropType<ChatRoleVO[]>,
    required: true
  },
  showMore: {
    type: Boolean,
    required: false,
    default: false
  }
})

// 定义钩子
const emits = defineEmits(['onDelete', 'onEdit', 'onUse', 'onPage'])

/** 操作：编辑、删除 */
const handleMoreClick = async (data) => {
  const type = data[0]
  const role = data[1]
  if (type === 'delete') {
    emits('onDelete', role)
  } else {
    emits('onEdit', role)
  }
}

/** 选中 */
const handleUseClick = (role) => {
  emits('onUse', role)
}

/** 滚动 */
const handleTabsScroll = async () => {
  if (tabsRef.value) {
    const { scrollTop, scrollHeight, clientHeight } = tabsRef.value
    if (scrollTop + clientHeight >= scrollHeight - 20 && !props.loading) {
      await emits('onPage')
    }
  }
}
</script>

<style lang="scss">
// 重写 card 组件 body 样式
.card-body {
  position: relative;
  display: flex;
  width: 240px;
  max-width: 240px;
  padding: 15px 15px 10px;
  flex-direction: row;
  justify-content: flex-start;
}
</style>
<style scoped lang="scss">
// 卡片列表
.card-list {
  position: relative;
  display: flex;
  height: 100%;
  padding: 0 25px;
  padding-bottom: 140px;
  overflow: auto;
  flex-flow: row wrap;
  align-items: start;
  place-content: flex-start start;

  .card {
    position: relative;
    display: inline-block;
    margin-right: 20px;
    margin-bottom: 20px;
    border-radius: 10px;

    .more-container {
      position: absolute;
      top: 0;
      right: 12px;
    }

    .avatar {
      width: 40px;
      height: 40px;
      overflow: hidden;
      border-radius: 10px;
    }

    .right-container {
      width: 100%;
      margin-left: 10px;
      //height: 100px;

      .content-container {
        height: 85px;

        .title {
          font-size: 18px;
          font-weight: bold;
          color: #3e3e3e;
        }

        .description {
          margin-top: 10px;
          font-size: 14px;
          color: #6a6a6a;
        }
      }

      .btn-container {
        display: flex;
        flex-direction: row-reverse;
        margin-top: 2px;
      }
    }
  }
}
</style>
