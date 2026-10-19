<template>
  <div class="app-shell">
    <!-- 左侧导航 -->
    <aside class="sidebar-container" :class="{ 'is-collapsed': isCollapse }">
      <div class="brand-row">
        <BrandLogo :size="isCollapse ? 32 : 34" :show-text="!isCollapse" subtitle="医疗诊断工作台" />
      </div>

      <div class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpeneds"
          class="el-menu-vertical"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#424245"
          active-text-color="#0A84FF"
          :unique-opened="false"
          router
        >
          <template v-for="route in filteredRoutes" :key="route.path">
            <!-- 只有一个子页面时直接渲染成一级菜单，避免多一层无意义的展开 -->
            <el-menu-item
              v-if="route.children && route.children.length === 1"
              :index="route.children[0].originalPath"
            >
              <el-icon v-if="route.meta?.icon || route.children[0].meta?.icon">
                <component :is="route.meta?.icon || route.children[0].meta?.icon" />
              </el-icon>
              <span>{{ route.children[0].meta?.title || route.meta?.title }}</span>
            </el-menu-item>

            <el-sub-menu
              v-else-if="route.children && route.children.length && route.meta?.showInMenu !== false"
              :index="route.path"
            >
              <template #title>
                <el-icon v-if="route.meta?.icon">
                  <component :is="route.meta.icon" />
                </el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>

              <template v-for="childRoute in route.children" :key="childRoute.path">
                <el-menu-item
                  v-if="childRoute.meta?.showInMenu !== false"
                  :index="childRoute.originalPath"
                >
                  <el-icon v-if="childRoute.meta?.icon">
                    <component :is="childRoute.meta.icon" />
                  </el-icon>
                  <span>{{ childRoute.meta?.title }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>

            <el-menu-item
              v-else-if="route.meta?.showInMenu !== false"
              :index="route.path"
            >
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="sidebar-footer">
        <el-dropdown trigger="click" placement="top-start" popper-class="sidebar-user-menu">
          <div class="user-pill">
            <el-avatar :size="32" class="user-avatar">{{ avatarText }}</el-avatar>
            <div v-show="!isCollapse" class="user-meta">
              <span class="user-name">{{ userStore.nickname || '管理员' }}</span>
              <span class="user-role">系统管理员</span>
            </div>
            <el-icon v-show="!isCollapse" class="user-caret"><CaretBottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Lock /></el-icon>修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-container">
      <header class="navbar">
        <div class="nav-left">
          <button class="icon-btn" type="button" title="折叠菜单" @click="toggleSideBar">
            <el-icon :size="18">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </button>
          <div class="crumbs">
            <span class="crumb-root">工作台</span>
            <template v-for="crumb in crumbs" :key="crumb">
              <el-icon class="crumb-sep"><ArrowRight /></el-icon>
              <span class="crumb">{{ crumb }}</span>
            </template>
          </div>
        </div>

        <div class="nav-right">
          <el-button class="quick-btn" round @click="router.push('/agent')">
            <el-icon><ChatDotRound /></el-icon>
            新建问诊
          </el-button>
        </div>
      </header>

      <main class="app-main">
        <div class="app-main-inner">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CaretBottom, ArrowRight, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { resetRouter } from '@/utils/dynamicRoutes'
import { logout } from '@/api/auth'
import { useUserStore } from '@/store'
import BrandLogo from '@/components/BrandLogo.vue'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}

const activeMenu = computed(() => route.path)

const defaultOpeneds = ref([])
watch(
  () => route.path,
  (newPath) => {
    const parentPath = '/' + newPath.split('/')[1]
    if (parentPath && parentPath !== '/' && !defaultOpeneds.value.includes(parentPath)) {
      defaultOpeneds.value.push(parentPath)
    }
  },
  { immediate: true }
)

// 面包屑：取路由链上有标题的层级，去掉重复
const crumbs = computed(() => {
  const titles = route.matched
    .map((r) => r.meta?.title)
    .filter((t) => typeof t === 'string' && t.trim())
    // 父级标题被更具体的子级标题包含时只保留子级，例如“知识库 > 知识库管理”
    .filter((t, i, arr) => !arr.slice(i + 1).some((next) => next !== t && next.startsWith(t)))
  return [...new Set(titles)]
})

const avatarText = computed(() => {
  const name = (userStore.nickname || 'A').trim()
  return name.slice(0, 1).toUpperCase()
})

// 菜单项缓存
let menuItemsCache = null

const filteredRoutes = computed(() => {
  const allRoutes = router.getRoutes()

  if (menuItemsCache && menuItemsCache.routeCount === allRoutes.length) {
    return menuItemsCache.items
  }

  const topLevelRoutes = allRoutes.filter((route) => {
    if (route.meta?.hidden === true) return false
    const pathSegments = route.path.split('/').filter(Boolean)
    return pathSegments.length === 1
  })

  const menuItems = topLevelRoutes.map((topRoute) => {
    const menuItem = { ...topRoute, children: [] }
    if (!menuItem.meta) menuItem.meta = {}

    menuItem.children = allRoutes
      .filter((route) => {
        if (!route.path.startsWith(topRoute.path + '/')) return false
        if (route.meta?.showInMenu === false) return false
        const remainingPath = route.path.slice(topRoute.path.length + 1)
        return !remainingPath.includes('/')
      })
      .map((childRoute) => ({ ...childRoute, originalPath: childRoute.path }))

    return menuItem
  })

  menuItemsCache = { routeCount: allRoutes.length, items: menuItems }
  return menuItems
})

function handleLogout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await logout()
      userStore.clearLoginState()
      resetRouter(router)
      router.push('/login')
      ElMessage.success('退出成功')
    })
    .catch(() => {})
}
</script>

<style scoped lang="scss">
.app-shell {
  display: flex;
  height: 100vh;
  width: 100%;
  background:
    radial-gradient(900px 420px at 88% -8%, rgba(10, 132, 255, 0.08), transparent 60%),
    radial-gradient(700px 380px at 6% 108%, rgba(56, 189, 248, 0.08), transparent 62%),
    var(--aimin-bg);
}

// ---------------- 侧栏 ----------------
.sidebar-container {
  width: 228px;
  flex-shrink: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(22px) saturate(1.5);
  -webkit-backdrop-filter: blur(22px) saturate(1.5);
  border-right: 1px solid var(--aimin-border);
  transition: width 0.28s var(--aimin-ease);
  z-index: 2;

  &.is-collapsed {
    width: 76px;
  }
}

.brand-row {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  flex-shrink: 0;
}

.is-collapsed .brand-row {
  justify-content: center;
  padding: 0;
}

.menu-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0 8px;
}

.el-menu-vertical {
  border-right: none;
  background: transparent;
  width: 100%;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    margin: 2px 12px;
    border-radius: 10px;
    height: 42px;
    line-height: 42px;
    font-size: 13.5px;
    transition: background 0.18s ease, color 0.18s ease;
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background: rgba(15, 23, 42, 0.045);
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, rgba(10, 132, 255, 0.14), rgba(56, 189, 248, 0.08));
    color: var(--aimin-accent);
    font-weight: 600;
    box-shadow: inset 2px 0 0 var(--aimin-accent);
  }

  :deep(.el-menu) {
    background: transparent;
  }
}

.is-collapsed .el-menu-vertical {
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    margin: 2px 8px;
    border-radius: 10px;
  }
}

.sidebar-footer {
  padding: 10px;
  border-top: 1px solid var(--aimin-border);
  flex-shrink: 0;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.18s ease;
  outline: none;

  &:hover {
    background: rgba(15, 23, 42, 0.045);
  }
}

.user-avatar {
  background: var(--aimin-gradient-brand);
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  flex-shrink: 0;
}

.user-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--aimin-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: var(--aimin-text-tertiary);
}

.user-caret {
  color: var(--aimin-text-tertiary);
  font-size: 12px;
}

.is-collapsed .user-pill {
  justify-content: center;
  padding: 8px 0;
}

// ---------------- 主体 ----------------
.main-container {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px 0 14px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(20px) saturate(1.6);
  -webkit-backdrop-filter: blur(20px) saturate(1.6);
  border-bottom: 1px solid var(--aimin-border);
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--aimin-text-secondary);
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;

  &:hover {
    background: rgba(15, 23, 42, 0.05);
    color: var(--aimin-text);
  }
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--aimin-text-tertiary);
}

.crumb-root {
  color: var(--aimin-text-tertiary);
}

.crumb {
  color: var(--aimin-text);
  font-weight: 600;
}

.crumb-sep {
  font-size: 11px;
  color: #c7c7cc;
}

.quick-btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(10, 132, 255, 0.25);
  background: rgba(10, 132, 255, 0.07);
  color: var(--aimin-accent);
  font-weight: 600;

  &:hover {
    background: rgba(10, 132, 255, 0.12);
    border-color: rgba(10, 132, 255, 0.35);
  }
}

.app-main {
  flex: 1;
  overflow: auto;
  padding: 20px 24px 28px;
}

.app-main-inner {
  max-width: 1440px;
  margin: 0 auto;
  min-height: 100%;
}
</style>
