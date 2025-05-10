<template>
  <div class="dashboard-container">
    <!-- 左侧菜单栏 -->
    <div class="sidebar-container">
      <div class="logo-container">
        <img src="/logo.png" width="50%" alt="logo">
      </div>
      <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpeneds"
          class="el-menu-vertical"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :unique-opened="false"
          :collapse-transition="false"
          router
      >
        <!-- 动态渲染菜单 -->
        <template v-for="route in filteredRoutes" :key="route.path">
          <!-- 如果有子路由且不是隐藏的 -->
          <el-sub-menu v-if="route.children && route.children.length && route.meta?.showInMenu !== false" :index="route.path">
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            
            <!-- 渲染子菜单 -->
            <template v-for="childRoute in route.children" :key="childRoute.path">
              <el-menu-item 
                :index="childRoute.originalPath"
                v-if="childRoute.meta?.showInMenu !== false"
              >
                <el-icon v-if="childRoute.meta?.icon">
                  <component :is="childRoute.meta.icon" />
                </el-icon>
                <span>{{ childRoute.meta?.title }}</span>
              </el-menu-item>
            </template>
          </el-sub-menu>
          
          <!-- 如果没有子路由且不是隐藏的 -->
          <el-menu-item v-else-if="route.meta?.showInMenu !== false" :index="route.path">
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="hamburger-container" @click="toggleSideBar">
          <el-icon :size="20">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
        </div>
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-wrapper">
              <el-avatar :size="30" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="user-name">{{userStore.nickname}}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CaretBottom } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { resetRouter } from '@/utils/dynamicRoutes'
import { logout } from '@/api/auth'
import { useUserStore } from '@/store'
const userStore = useUserStore();

const router = useRouter()
const route = useRoute()

// 控制菜单折叠状态
const isCollapse = ref(false)
const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}

// 获取当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 默认展开的菜单
const defaultOpeneds = ref([])

// 监听路由变化，自动展开当前路由所在的菜单
watch(() => route.path, (newPath) => {
  // 找出当前路径的父级路径
  const parentPath = '/' + newPath.split('/')[1]
  if (parentPath && parentPath !== '/' && !defaultOpeneds.value.includes(parentPath)) {
    defaultOpeneds.value.push(parentPath)
  }
}, { immediate: true })

// 菜单项缓存
let menuItemsCache = null;

// 过滤后的路由，只包含一级路由
const filteredRoutes = computed(() => {
  // 获取所有路由
  const allRoutes = router.getRoutes();

  if (menuItemsCache && menuItemsCache.routeCount === allRoutes.length) {
    return menuItemsCache.items;
  }
  
  // 1. 提取顶级路由（不包括特殊路由）
  const topLevelRoutes = allRoutes.filter(route => {
    // 排除特殊路由和隐藏路由
    if (route.meta?.hidden === true) {
      return false;
    }
    // 只保留顶级路由（路径格式为 /xxx）
    const pathSegments = route.path.split('/').filter(Boolean);
    return pathSegments.length === 1;
  });
  
  // 2. 构建菜单项
  const menuItems = topLevelRoutes.map(topRoute => {
    // 创建顶级菜单项
    const menuItem = {
      ...topRoute,
      children: []
    };
    
    // 确保meta对象存在
    if (!menuItem.meta) {
      menuItem.meta = {};
    }
    
    // 3. 查找并添加子菜单项
    menuItem.children = allRoutes
      .filter(route => {
        // 路径必须以顶级路由路径开头，但不等于顶级路由路径
        if (!route.path.startsWith(topRoute.path + '/')) {
          return false;
        }
        
        // 排除隐藏的菜单项
        if (route.meta?.showInMenu === false) {
          return false;
        }
        
        // 只取直接子路由，不包括更深层次的路由
        const remainingPath = route.path.slice(topRoute.path.length + 1);
        return !remainingPath.includes('/');
      })
      .map(childRoute => ({
        ...childRoute,
        // 存储原始路径用于导航
        originalPath: childRoute.path
      }));
    
    return menuItem;
  });
  
  // 缓存结果
  menuItemsCache = {
    routeCount: allRoutes.length,
    items: menuItems
  };
  
  return menuItems;
})


function handleLogout() {
  const userStore = useUserStore();

  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {

    await logout();
    // 1. 清除登录状态
    userStore.clearLoginState();
    
    // 3. 重置路由（只清除动态路由，保留基础路由）
    resetRouter(router);
    
    // 4. 跳转到登录页
    router.push('/login');
    
    ElMessage.success('退出成功');
  }).catch(() => {});
}
</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;
  width: v-bind('isCollapse ? "64px" : "210px"');
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 10px 0;
  background: #fff;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 210px;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.hamburger-container {
  cursor: pointer;
}

.right-menu {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  margin: 0 5px;
}

.app-main {
  flex: 1;
  padding: 15px;
  overflow: auto;
}
</style> 