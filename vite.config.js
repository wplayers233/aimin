import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5174,
    proxy: {
      // 代理所有 /api 开头的请求
      '/api': {
        target: 'http://localhost:8080', // 修改为您实际的后端服务地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 代理所有 /public 开头的请求
      '/public': {
        target: 'http://localhost:8080', // 修改为您实际的后端服务地址
        changeOrigin: true
      }
    }
  }
})
