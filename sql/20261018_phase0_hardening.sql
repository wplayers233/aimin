-- =============================================================
-- Phase 0 数据层加固（2026-10-18）
-- 项目没有 Flyway/Liquibase，这里是手工执行脚本：
--   新库先用 aimin2025_05_10_01.sql 初始化，再执行本文件；
--   已有库直接执行本文件即可（幂等性：唯一索引重复创建会报错，可先检查）。
-- 执行：mysql -uroot -p aimin < 20261018_phase0_hardening.sql
-- =============================================================
SET NAMES utf8mb4;

-- 1) 管理员用户名/手机号唯一
--    service 层 isExists 是“先查后写”，并发下仍可能插入重复；唯一索引兜底。
--    重复值场景由 AdminFacadeService 捕获 DuplicateKeyException 转成友好提示。
--    注意：逻辑删除的行同样占用唯一键，已删除账号的用户名/手机号不能再次注册。
ALTER TABLE `t_admin`
    ADD UNIQUE INDEX `uk_username` (`username`) USING BTREE,
    ADD UNIQUE INDEX `uk_phone` (`phone`) USING BTREE;

-- 2) 药品管理页已下线（前端路由与页面删除），清理菜单权限与角色关联
DELETE FROM `t_role_permission` WHERE `perm_id` IN (24, 25, 28);
DELETE FROM `t_permission` WHERE `id` IN (24, 25, 28);
