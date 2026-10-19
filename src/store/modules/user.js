import { defineStore } from 'pinia';

import { login } from '@/api/auth';
import { getCurrentAdminInfo } from "@/api/system/admin";

/**
 * 用户状态存储
 */
export const useUserStore =  defineStore("user",{
    state: () => {
        return {
            isLogin: false,  // 登录状态
            tokenName: 'token', // token名称
            tokenValue: '', // token值
            permissions: [], // 用户权限列表
            captchaResult: false, // 验证码校验结果
            nickname: '管理员'
        }
    },
    getters: {
        getIsLogin: (state) => state.isLogin,  // 对外暴露的登录状态计算属性
        getTokenName: (state) => state.tokenName,  // 对外暴露的token名称计算属性
        getTokenValue: (state) => state.tokenValue,  // 对外暴露的token值计算属性
        getPermissions: (state) => state.permissions, // 对外暴露权限列表
    },
    actions: {
        setLoginState(loginState){
            this.isLogin = loginState;
        },
        setTokenName(tokenName){
            this.tokenName = tokenName;
        },
        setTokenValue(tokenValue){
            this.tokenValue = tokenValue;
        },
        setPermissions(permissions) {
            this.permissions = permissions;
        },
        clearLoginState(){
            this.isLogin = false;
            this.tokenValue = '';
            this.tokenName = 'token';
            this.permissions = [];
        },
        clearAll(){
            this.isLogin = false;
            this.tokenValue = '';
            this.tokenName = 'token';
            this.permissions = [];
        },
        // 检查是否有某个权限
        hasPermission(permission) {
            return this.permissions.includes('*:*:*') || this.permissions.includes(permission);
        },
        // 检查是否有指定权限中的任意一个
        hasAnyPermission(permissions) {
            return this.permissions.includes('*:*:*') || 
                permissions.some(permission => this.permissions.includes(permission));
        },
        async accountLogin(loginData) {
            const resp = await login(loginData);
            const data = resp?.data;
            this.captchaResult = !!data?.captchaVerifyResult;
            if (data?.permissions && data?.tokenValue && data?.tokenName) {
                this.setTokenName(data.tokenName);
                this.setTokenValue(data.tokenValue);
                this.setPermissions(data.permissions);
                this.setLoginState(true)
                await this.getAdminAllInfo();
                return true;
            }
            return false;
        },

        async getAdminAllInfo(){
            const res = await getCurrentAdminInfo();
            if(res.data){
                this.nickname = res.data.nickname;
            }
        }
    },
    persist: true
})

