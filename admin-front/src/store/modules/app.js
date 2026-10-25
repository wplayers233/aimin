import { defineStore } from 'pinia';
import { getStatus } from '@/api/agent';

/**
 * 应用全局状态：loading 与 agent 运行状态（演示模式徽标的单一数据源）
 */
export const useAppStore =  defineStore("app",{
    state: () => {
        return {
            loading: false,  // loading状态
            agentStatus: null,  // agent 运行状态 { mock, docs, chunks }；null = 尚未获取或后端未连接
        }
    },
    getters: {
        getLoading: (state) => state.loading,  // 获取loading状态
        // 是否演示模式（未配置 LLM Key）；未获取到状态时视为未知（false，只显示 LOCAL）
        isDemoMode: (state) => !!state.agentStatus?.mock,

    },
    actions: {
        setLoading(status) {
            this.loading = status;
        },
        clearAll(){
            this.isLoad = false;
        },
        // 获取 agent 运行状态；已有结果且不强制时直接复用，避免 layout 与页面重复请求
        async loadAgentStatus(force = false) {
            if (this.agentStatus && !force) return this.agentStatus
            try {
                const res = await getStatus()
                const data = res?.data ?? res
                if (data && typeof data === 'object') this.agentStatus = data
            } catch (e) {
                // 后端未连接时保持 null，徽标与首页各自呈现未知态
            }
            return this.agentStatus
        },
    }
})
