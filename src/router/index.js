import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store'


const INDEX_PATH = '/index';
const LOGIN_PATH = '/login';
const NOT_FOUND_PATH = '/404';


NProgress.configure({
  easing: 'ease', // 动画方式
  speed: 500, // 递增进度条的速度
  showSpinner: false, // 是否显示圆圈加载
  trickleSpeed: 200, // 自动递增间隔
  minimum: 0.3, // 初始化时的最小百分比
})

// 基础路由 - 不需要权限验证的路由
const constantRoutes = [
  {
    path: "/",
    redirect: 'index',
    name: "Index",
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: "index",
        component: () => import('@/views/dashboard/index.vue'),
        name: "Dashboard",
        meta: {
          title: "首页",
          icon: "Monitor",
          hidden: false
        },
      },
    ]
  },
 /* {
    path: "/drugs",
    name: "Drugs",
    component: () => import('@/layout/index.vue'),
    meta: {
      title: '药品管理',
      hidden: false
    },
    children: [
      {
        path: 'index',
        name: 'DrugsIndex',
        component: () => import('@/views/drugs/index.vue'),
        meta: {
          title: '药品管理',
          hidden: false
        }
      },
    ]
  },*/




  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      hidden: true
    }
  },
  // 添加404页面路由
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
      hidden: true,

    }
  },
  // 添加通配符路由作为兜底
  {
    path: '/:pathMatch(.*)*',
    name: 'CatchAll',
    redirect: '/404',
    meta: {
      hidden: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

/**
 * 处理登录页重定向
 * 已登录用户访问登录页时，重定向到首页
 */
function handleLoginRedirect(to, from, next) {
  const userStateStore = useUserStore();
  const isLoggedIn = userStateStore.getIsLogin;
  
  // 如果已登录且访问登录页，重定向到首页
  if (to.path === LOGIN_PATH && isLoggedIn) {
    next(INDEX_PATH)
    return true;
  }
  
  // 如果未登录且访问非公共页面，重定向到登录页
  const isPublicPage = to.path === LOGIN_PATH || to.path === NOT_FOUND_PATH;
  // 添加对根路径的处理
  if ((to.path === '/' || (!isPublicPage && !isLoggedIn))) {
    next({ path: LOGIN_PATH, query: { redirect: to.fullPath } })
    return true;
  }
  
  return false;
}

/**
 * 设置页面标题
 */
function setDocumentTitle(to) {
  document.title = to.meta.title ? `${to.meta.title} - ElementAdmin` : 'ElementAdmin'
}

// 全局前置守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  // 设置页面标题
  setDocumentTitle(to);
  // 执行登录重定向逻辑
  if (handleLoginRedirect(to, from, next)) return;

  next();
})

router.onError(() => {
  NProgress.done()
})
router.afterEach(() => {
  NProgress.done()
})


export default router
