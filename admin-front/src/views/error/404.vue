<template>
  <div class="not-found">
    <h1>404</h1>
    <h2>页面不存在</h2>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 50px;
}

h1 {
  font-size: 80px;
  color: #409EFF;
  margin-bottom: 20px;
}

h2 {
  font-size: 24px;
  color: #606266;
  margin-bottom: 30px;
}
</style> 