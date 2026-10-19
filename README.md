# 爱民医生（Aimin）
官网：[渣哥-官网](https://www.zha-ge.cn)

基于SpringAi+SpringCloud实现的，私有化部署的，多模态医疗AI应用实战项目！

**项目开源，无底线开源**
前后端代码 + 部署安装文档 + 重点讲解，统统开源！！！

## 项目介绍

项目名称：爱民医生（aimin）。项目是一个基于SpringCloud微服务架构的SpingAi医疗问诊实战项目，通过集成多种大模型，实现了一个完整的AI应用。项目采用前后端分离的开发模式，前端使用Vue3+Element Plus，后端使用SpringCloud+SpringBoot。

## 项目地址
[管理端：](https://gitee.com/dd_mark/aimin-admin-front)
[小程序端：](https://gitee.com/dd_mark/aimin-xcx-user)

## 技术栈

### AI相关
- **Spring AI:** 1.0.0-M8
- **Chat Model:** qwen2.5-1m:latest
- **Embeding Model:** gme-Qwen2-VL-2B-Instruct
- **Platform:** Ollama(下步体验vLLM)
- **VectorStore:** Milvus
- **下步集成并完成：** SpringAi MCP Server/Client

### 后端技术栈
- JDK 21
- Springboot 3
- SpringCloud 2024.0.0
- SpringCloudAlibaba 2023.0.1.0
- Nacos 2.4.3
- Sa-token 1.39.0
- Mybatis-plus 3.5.9
- Es 8.9.0
- Redis 7.0.11
- Rabbitmq 4.0.5
- Caffeine 3.1.8
- Mysql 8.0
- Canal 1.1.7
- MongoDB

### 前端技术栈
- Vue3
- Vite
- Element Plus
- Axios
- Pinia
- 小程序原生

### 其他
- 阿里云图形验证码
- 微信支付

## 项目架构

项目采用微服务架构，主要包含以下模块：
1. **后台管理服务（aimin-admin）** 后台管理服务
2. **网关服务(aimin-gateway)**: 负责请求的路由和过滤
3. **认证服务(aimin-auth)**: 处理用户认证和授权
4. **AI服务(aimin-ai)**: AI服务
5. **药品服务（aimin-drug）**：药品服务
6. **搜索服务（aimin-search）**：用户搜索服务
7. **数据同步（aimin-canal）**：数据同步服务
7. **通用组件(common)**: 通用组件父项目
   **通用组件包含：**
   - aimin-base: 基础组件
   - aimin-cache: 二级缓存自主实现组件
   - aimin-ds: 数据源组件
   - aimin-es: elasticsearch组件
   - aimin-file: 文件上传下载组件（目前只支持oss，之后集成minio）
   - aimin-generator: 代码生成器
   - aimin-mongo: mongoDB组件
   - aimin-mq: mq组件（目前只支持rabbitMQ,之后集成kafka）
   - aimin-redis: redis组件
   - aimin-satoken: satoken组件(已实现多账号认证)

## 项目部署

### 部署步骤

即将更新...

## 开发指南

### 本地开发环境搭建

即将更新...

### API文档
即将更新...

## 性能优化

本项目在性能方面做了以下优化：

1. 引入Redis缓存热点数据
2. 使用Nginx做负载均衡
4. 图片等静态资源使用CDN加速
5. 采用异步处理提高响应速度
   即将更新...

## 安全措施

即将更新...

## 许可证
即将更新...