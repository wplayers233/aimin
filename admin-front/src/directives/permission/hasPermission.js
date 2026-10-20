import { useUserStore } from '@/store'

/**
 * @desc v-permission 操作权限处理
 * @desc 使用 v-permission="['system:user:add']"
 */
function checkPermission(el, binding) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value && Array.isArray(value) && value.length) {
        // 使用 store 中的 hasAnyPermission 方法进行权限检查
        const hasPermission = userStore.hasAnyPermission(value)
        if (!hasPermission) {
            el.parentNode && el.parentNode.removeChild(el)
        }
    } else {
        throw new Error(`需要权限! 例如 v-permission="['system:user:add','system:user:edit']"`)
    }
}

const directive = {
    mounted(el, binding) {
        checkPermission(el, binding)
    },
    updated(el, binding) {
        checkPermission(el, binding)
    }
}

export default directive
