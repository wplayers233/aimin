import { defineStore } from 'pinia';

/**
 * 路由状态存储
 */
export const useAppStore =  defineStore("app",{
    state: () => {
        return {
            loading: false,  // loading状态
        }
    },
    getters: {
        getLoading: (state) => state.loading,  // 获取loading状态

    },
    actions: {
        setLoading(status) {
            this.loading = status;
        },
        clearAll(){
            this.isLoad = false;
        }
    }
})

