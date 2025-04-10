import { defineStore } from 'pinia';
import { getRouters } from '@/api/system/router';

/**
 * 路由状态存储
 */
export const useRouteStore =  defineStore("route",{
    state: () => {
        return {
            isLoad: false,  // 微信端登录状态，只检查token
            dynamicRoutes: [], // token名称

        }
    },
    getters: {
        getIsLoad: (state) => state.isLoad,  // 对外暴露的登录状态计算属性
        getDynamicRoutes: (state) => state.dynamicRoutes, // 对外暴露的token名称计算属性
    },
    actions: {
        setDynamicRoutes(routes) {
            this.dynamicRoutes = routes;
        },
        setIsLoad(isLoad){
            this.isLoad = isLoad;
        },
        clearAll(){
            this.isLoad = false;
            this.dynamicRoutes = [];
        },
        async getUserRouters(){
            const resp = await getRouters();
            if (resp && resp?.data) {
                this.dynamicRoutes = resp.data;
                return true;
            }
            return false;
        }
    },
    persist: true
})

