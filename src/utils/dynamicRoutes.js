import {ElMessage} from 'element-plus'
import Layout from '@/layout/index.vue'
import {useRouteStore, useUserStore} from '@/store'

// 组件映射表
const componentMap = {
  // 布局组件
  'layout': Layout,
  
  // 页面组件
  'Dashboard': () => import('@/views/dashboard/index.vue'),
  'dashboard': () => import('@/views/dashboard/index.vue'),
  
  // 系统管理
  'system/user': () => import('@/views/system/admin/index.vue'),
  'system/role': () => import('@/views/system/role/index.vue'),
  'system/menu': () => import('@/views/system/menu/index.vue'),

  // 药品管理
  'drugs': Layout,
  'drugs/index': () => import('@/views/drugs/index.vue'),

  // 错误页面
  '404': () => import('@/views/error/404.vue')
}

/**
 * 将后端返回的路由数据转换为前端路由对象
 * @param {Array} asyncRoutes - 后端返回的路由数据
 * @returns {Array} - 处理后的路由数组
 */
export function convertToRoutes(asyncRoutes) {
  return asyncRoutes.map(route => {
    const {path, component, name, meta, children, redirect} = route

    // 检查组件是否存在于映射表中
    if (component && !componentMap[component]) {
      ElMessage.warning(`组件 ${component} 不存在，请检查路由配置`);
      console.warn(`Component ${component} not found in componentMap.`);
    }

    // 创建路由对象
    const currentRoute = {
      path,
      name,
      component: component ? (componentMap[component] || componentMap['404']) : componentMap['layout'],
      meta: meta || {}
    }

    // 如果有重定向，添加重定向属性
    if (redirect) {
      currentRoute.redirect = redirect
    }

    // 处理子路由
    if (children && children.length) {
      currentRoute.children = convertToRoutes(children)
    }

    return currentRoute
  })
}

/**
 * 动态添加路由
 * @param {Array} routes - 路由配置数组
 * @param {Object} router - Vue Router实例
 */
export function addDynamicRoutes(routes, router) {
  // 先清除现有的相同路由,避免重复添加
  const existingRoutes = router.getRoutes();
  const routeNames = routes.map(route => route.name);
  
  // 移除可能重复的路由(使用路由名称匹配)
  existingRoutes.forEach(route => {
    if (route.name && routeNames.includes(route.name)) {
      router.removeRoute(route.name);
    }
  });
  
  // 添加所有路由
  routes.forEach(route => {
    // 避免重复添加已存在的路由
    try {
      // 检查路由是否已存在
      const existRoute = router.hasRoute(route.name);
      if (existRoute) {
        console.warn(`Route ${route.name} already exists, skipping.`);
        return;
      }
      
      // 添加父路由
      router.addRoute({
        path: route.path,
        name: route.name,
        component: route.component,
        redirect: route.redirect,
        meta: route.meta || {}
      });

      // 添加子路由
      if (route.children && route.children.length) {
        route.children.forEach(child => {
          // 确保子路由名称唯一
          if (router.hasRoute(child.name)) {
            console.warn(`Route ${child.name} already exists, skipping.`);
            return;
          }
          
          router.addRoute(route.name, {
            path: child.path,
            name: child.name,
            component: child.component,
            meta: child.meta || {},
            redirect: child.redirect
          });
        });
      }
    } catch (error) {
      console.error(`Error adding route ${route.name}:`, error);
    }
  });
}

/**
 * 重置路由
 * @param {Object} router - Vue Router实例
 */
export function resetRouter(router) {
  const routeStore = useRouteStore();
  const dynamicRoutes = routeStore.dynamicRoutes || [];
  
  // 获取所有动态路由的名称
  const dynamicRouteNames = new Set();
  
  // 收集所有动态路由名称（包括父路由和子路由）
  dynamicRoutes.forEach(route => {
    if (route.name) {
      dynamicRouteNames.add(route.name);
    }
    
    // 收集子路由名称
    if (route.children && route.children.length) {
      route.children.forEach(child => {
        if (child.name) {
          dynamicRouteNames.add(child.name);
        }
      });
    }
  });
  
  // 获取当前路由中实际存在的路由
  const allRoutes = router.getRoutes();
  const routesToRemove = allRoutes
    .filter(route => route.name && dynamicRouteNames.has(route.name))
    .map(route => route.name);
  
  // 删除这些动态路由
  routesToRemove.forEach(name => {
    if (router.hasRoute(name)) {
      router.removeRoute(name);
    }
  });
  
  // 使用setTimeout确保路由移除完成后再清空状态
  setTimeout(() => {
    // 清空路由状态存储
    routeStore.clearAll();
  }, 0);
}

/**
 * 从routeStore中获取动态路由数据并加载到路由中
 * 不从后端请求数据，只使用store中已有的数据
 * @param {Object} router - Vue Router实例
 * @returns {Boolean} - 是否成功加载路由
 */
export function loadRoutesFromStore(router) {
  // 初始化store
  const routeStore = useRouteStore();
  const userStore = useUserStore();

  // 检查登录状态
  if (!userStore.isLogin) {
    return false;
  }

  // 从store中获取路由数据
  const routesData = routeStore.dynamicRoutes;

  // 检查数据是否存在且有效
  if (routesData.length === 0) {
    return false;
  }

  try {
    // 转换成路由对象
    const routes = convertToRoutes(routesData);
    // 添加到路由
    addDynamicRoutes(routes, router);
    // 更新加载状态
    routeStore.setIsLoad(true);
    return true;
  } catch (error) {
    return false;
  }
} 