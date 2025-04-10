<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="药品管理" name="drug-list">
        <drug-list />
      </el-tab-pane>
      <el-tab-pane label="药品分类" name="drug-category">
        <drug-category />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DrugList from './drug-list/index.vue'
import DrugCategory from './drug-category/index.vue'

const activeTab = ref('drug-list')
</script>

<style scoped>
.app-container {
  height: 100%;
}

:deep(.el-tabs__content) {
  height: calc(100% - 40px);
  overflow: auto;
}

:deep(.el-tab-pane) {
  height: 100%;
}
</style> 