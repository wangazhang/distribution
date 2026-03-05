<!-- header -->
<template>
  <el-header class="chat-header">
    <div class="title">
      {{ title }}
    </div>
    <div class="title-right">
      <slot></slot>
    </div>
  </el-header>
</template>

<script setup lang="ts">
// 设置组件属性
defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>

<style scoped lang="scss">
.chat-header {
  display: flex;
  width: 100%;
  padding: 0 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
  background-color: #ececec;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  .title {
    max-width: 220px;
    overflow: hidden;
    font-size: 20px;
    font-weight: bold;
    color: #3e3e3e;
  }

  .title-right {
    display: flex;
    flex-direction: row;
  }
}
</style>
