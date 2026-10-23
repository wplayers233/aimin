<template>
  <div class="atlas-shell" :class="{ 'is-collapsed': isCollapse }">
    <aside class="rail">
      <div class="brand">
        <span class="mark">艾</span>
        <div v-if="!isCollapse" class="brand-text">
          <strong>aimin</strong>
          <em>CLINICAL ATLAS</em>
        </div>
      </div>

      <nav class="rail-menu">
        <template v-for="(route, i) in filteredRoutes" :key="route.path">
          <div v-if="route.children && route.children.length > 1" class="group">
            <span class="group-label">
              <span class="idx">{{ String(i + 1).padStart(2, '0') }}</span>
              <span v-if="!isCollapse">{{ route.meta?.title }}</span>
            </span>
            <button
              v-for="child in route.children"
              :key="child.path"
              class="item sub"
              :class="{ on: isActive(child.originalPath) }"
              @click="go(child.originalPath)"
            >
              <span class="bar"></span>
              <span v-if="!isCollapse" class="label">{{ child.meta?.title }}</span>
            </button>
          </div>

          <button
            v-else
            class="item"
            :class="{ on: isActive(singlePath(route)) }"
            :title="singleTitle(route)"
            @click="go(singlePath(route))"
          >
            <span class="bar"></span>
            <span class="idx">{{ String(i + 1).padStart(2, '0') }}</span>
            <span v-if="!isCollapse" class="label">{{ singleTitle(route) }}</span>
          </button>
        </template>
      </nav>

      <div class="rail-foot">
        <span class="avatar">{{ avatarText }}</span>
        <div v-if="!isCollapse" class="who">
          <b>{{ displayName }}</b>
          <span>ADMIN</span>
        </div>
        <button v-if="!isCollapse" class="logout" title="退出登录" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>

      <button class="collapse" :title="isCollapse ? '展开' : '收起'" @click="toggleSideBar">
        <el-icon><component :is="isCollapse ? 'ArrowRight' : 'ArrowLeft'" /></el-icon>
      </button>
    </aside>

    <div class="body">
      <header class="topbar">
        <div class="crumb">
          <span>工作台</span>
          <template v-for="(c, i) in crumbs" :key="i">
            <i class="sep"></i>
            <b>{{ c }}</b>
          </template>
        </div>
        <div class="tools">
          <span class="dot"></span>
          <span class="num">{{ clock }}</span>
          <span class="env">LOCAL · DEMO</span>
        </div>
      </header>

      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { resetRouter } from '@/utils/dynamicRoutes'
import { logout } from '@/api/auth'
import { useUserStore } from '@/store'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}

const isActive = (path) => !!path && (route.path === path || route.path.startsWith(path + '/'))
const singlePath = (r) => r.children?.[0]?.originalPath || r.path
const singleTitle = (r) => r.children?.[0]?.meta?.title || r.meta?.title || ''

const crumbs = computed(() => {
  const titles = route.matched
    .map((r) => r.meta?.title)
    .filter((t) => typeof t === 'string' && t.trim())
    .filter((t, i, arr) => !arr.slice(i + 1).some((next) => next !== t && next.startsWith(t)))
  return [...new Set(titles)]
})

const displayName = computed(() => userStore.nickname || '管理员')

const avatarText = computed(() => {
  const name = (userStore.nickname || 'A').trim()
  return name.slice(0, 1).toUpperCase()
})

let menuItemsCache = null
const filteredRoutes = computed(() => {
  const allRoutes = router.getRoutes()
  if (menuItemsCache && menuItemsCache.routeCount === allRoutes.length) {
    return menuItemsCache.items
  }
  const topLevelRoutes = allRoutes.filter((r) => {
    if (r.meta?.hidden === true) return false
    return r.path.split('/').filter(Boolean).length === 1
  })
  const items = topLevelRoutes.map((topRoute) => {
    const menuItem = { ...topRoute, children: [] }
    if (!menuItem.meta) menuItem.meta = {}
    menuItem.children = allRoutes
      .filter((r) => {
        if (!r.path.startsWith(topRoute.path + '/')) return false
        if (r.meta?.showInMenu === false) return false
        return !r.path.slice(topRoute.path.length + 1).includes('/')
      })
      .map((child) => ({ ...child, originalPath: child.path }))
    return menuItem
  })
  menuItemsCache = { routeCount: allRoutes.length, items }
  return items
})

function go(path) {
  if (!path) return
  if (route.path !== path) router.push(path)
}

const clock = ref('')
let timer = null
const tick = () => {
  const d = new Date()
  clock.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
onMounted(() => {
  tick()
  timer = setInterval(tick, 30000)
})
onUnmounted(() => timer && clearInterval(timer))

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
.atlas-shell {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
}

.rail {
  width: 196px;
  flex: 0 0 196px;
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  padding: 24px 0 18px;
  transition: width 0.35s var(--ease), flex-basis 0.35s var(--ease);
  position: relative;

  .is-collapsed & {
    width: 72px;
    flex-basis: 72px;
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 20px 26px;

  .mark {
    width: 30px;
    height: 30px;
    flex: 0 0 30px;
    border: 1px solid var(--ink);
    border-radius: 50%;
    display: grid;
    place-items: center;
    font-family: var(--serif);
    font-size: 13px;
  }

  .brand-text strong {
    display: block;
    font-family: var(--serif);
    font-size: 17px;
    font-weight: 500;
    letter-spacing: 0.01em;
    line-height: 1.2;
  }

  .brand-text em {
    display: block;
    font-style: normal;
    font-family: var(--mono);
    font-size: 8.5px;
    letter-spacing: 0.2em;
    color: var(--ink-3);
    margin-top: 3px;
  }
}

.rail-menu {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;

  &::-webkit-scrollbar {
    width: 0;
  }
}

.group {
  margin-bottom: 6px;
}

.group-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px 6px;
  font-family: var(--mono);
  font-size: 9.5px;
  letter-spacing: 0.16em;
  color: var(--ink-3);
  text-transform: uppercase;
}

.item {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 20px;
  color: var(--ink-2);
  font-size: 13px;
  text-align: left;
  transition: color 0.25s, background 0.25s;

  .bar {
    position: absolute;
    left: 0;
    top: 50%;
    width: 2px;
    height: 0;
    background: var(--ochre);
    transform: translateY(-50%);
    transition: height 0.3s var(--ease);
  }

  .idx {
    font-family: var(--mono);
    font-size: 9.5px;
    letter-spacing: 0.1em;
    color: var(--ink-3);
    min-width: 16px;
  }

  .label {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &:hover {
    color: var(--ink);
    background: rgba(22, 32, 46, 0.025);
  }

  &.on {
    color: var(--ink);
    background: rgba(42, 74, 107, 0.055);

    .bar {
      height: 22px;
    }

    .idx {
      color: var(--ochre);
    }
  }

  &.sub {
    padding-left: 48px;
    font-size: 12.5px;

    .is-collapsed & {
      padding-left: 24px;
    }
  }
}

.rail-foot {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px 0;
  border-top: 1px solid var(--line);
  margin-top: 8px;

  .avatar {
    width: 28px;
    height: 28px;
    flex: 0 0 28px;
    border-radius: 50%;
    background: var(--paper-3);
    display: grid;
    place-items: center;
    font-family: var(--mono);
    font-size: 11px;
    color: var(--ink-2);
  }

  .who {
    flex: 1;
    min-width: 0;

    b {
      display: block;
      font-weight: 500;
      font-size: 12.5px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    span {
      display: block;
      font-family: var(--mono);
      font-size: 8.5px;
      letter-spacing: 0.16em;
      color: var(--ink-3);
    }
  }

  .logout {
    color: var(--ink-3);
    padding: 4px;
    transition: color 0.2s;
    &:hover {
      color: var(--ochre);
    }
  }
}

.collapse {
  position: absolute;
  right: -11px;
  top: 74px;
  width: 22px;
  height: 22px;
  border: 1px solid var(--line);
  background: var(--paper);
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--ink-3);
  font-size: 11px;
  z-index: 3;
  transition: color 0.2s, border-color 0.2s;

  &:hover {
    color: var(--ink);
    border-color: var(--ink);
  }
}

.body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 62px;
  flex: 0 0 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36px;
  border-bottom: 1px solid var(--line);
}

.crumb {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12px;
  color: var(--ink-3);

  b {
    color: var(--ink);
    font-weight: 500;
  }

  .sep {
    width: 14px;
    height: 1px;
    background: var(--line);
  }
}

.tools {
  display: flex;
  align-items: center;
  gap: 16px;
  font-family: var(--mono);
  font-size: 10.5px;
  letter-spacing: 0.1em;
  color: var(--ink-3);

  .dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    background: var(--sage);
    box-shadow: 0 0 0 3px rgba(110, 143, 123, 0.16);
  }

  .env {
    border-left: 1px solid var(--line);
    padding-left: 14px;
  }
}

.app-main {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 32px 36px 52px;
  min-height: 0;
}

@media (max-width: 900px) {
  .rail {
    width: 72px;
    flex-basis: 72px;
  }
  .brand-text,
  .item .label,
  .item .idx,
  .group-label span,
  .rail-foot .who,
  .rail-foot .logout {
    display: none;
  }
  .app-main {
    padding: 24px 20px 40px;
  }
  .topbar {
    padding: 0 20px;
  }
}
</style>
