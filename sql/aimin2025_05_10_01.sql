/*
 Navicat Premium Data Transfer

 Source Server         : aliyun
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : rm-2zeu4x1450e1g7xl75o.mysql.rds.aliyuncs.com:3306
 Source Schema         : aimin

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 10/05/2025 15:33:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_admin
-- ----------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码（BCrypt）',
  `nickname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `gender` bit(1) NULL DEFAULT NULL COMMENT '性别',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（唯一）',
  `status` bit(1) NOT NULL DEFAULT b'1' COMMENT '账号状态（0-禁用，1-启用）',
  `create_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `dept_id` int(0) NULL DEFAULT NULL COMMENT '部门ID',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除字段',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_admin
-- ----------------------------
INSERT INTO `t_admin` VALUES (1, 'admin', '$2a$10$5prN1xVO4jG/.XFVz1w4Xuae8eTzCAg7qZ/16TlnZSk..9/yAvKwG', '123', b'0', '15826279057', b'1', '2025-03-05 20:22:34', '2025-03-30 03:08:48', 1, b'0');
INSERT INTO `t_admin` VALUES (18, 'admin1', '$2a$10$5SJC7GkNOc7xftBWAwcg1u17JWkgKFgSpO4j.sTstwm3DzQvwZb4W', 'admin1', b'0', '18612344321', b'1', '2025-03-26 18:51:35', '2025-03-26 18:51:35', 21, b'0');
INSERT INTO `t_admin` VALUES (19, 'admin2', '$2a$10$IGLbkUZgQjaob5RTUpMGAOMenkrJf/TUaPGHj44avkoYfR/YRFXPO', 'admin2', b'1', '13811111111', b'1', '2025-03-26 18:59:45', '2025-04-07 16:46:40', 5, b'0');
INSERT INTO `t_admin` VALUES (20, 'admin3', '$2a$10$kGXpSwp5A54jdPi6nIRCtuV3QCfEApZspKx6Q7WBhNamx1to2fVpy', 'admin3', b'1', '18777777777', b'1', '2025-03-30 21:09:39', '2025-03-30 21:09:39', 21, b'0');
INSERT INTO `t_admin` VALUES (21, 'admin4', '$2a$10$vF73r5Aqb0O/oitbEB4ixuVixDCEmkggaUFlWoQT3vY5/3R5cqZrG', 'admin4', b'1', '13888888888', b'1', '2025-03-30 21:28:49', '2025-03-30 21:28:49', 21, b'0');
INSERT INTO `t_admin` VALUES (22, 'admin7', '$2a$10$oPfJF3OUAlV6GZm7ozbMVuIa7us4GE3.KR02CvSgXMARrZQRtsbcS', 'admin7', b'0', '13444444444', b'1', '2025-04-03 05:53:43', '2025-04-07 17:08:56', 20, b'0');

-- ----------------------------
-- Table structure for t_admin_department
-- ----------------------------
DROP TABLE IF EXISTS `t_admin_department`;
CREATE TABLE `t_admin_department`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` int(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `dept_id` int(0) UNSIGNED NOT NULL COMMENT '部门ID',
  `is_primary` tinyint(0) NULL DEFAULT 0 COMMENT '是否主部门（0-否，1-是）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除字段',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dept`(`admin_id`, `dept_id`) USING BTREE,
  INDEX `dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户部门关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_admin_department
-- ----------------------------

-- ----------------------------
-- Table structure for t_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `t_admin_role`;
CREATE TABLE `t_admin_role`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` int(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` int(0) UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT ' 逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`admin_id`, `role_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_admin_role
-- ----------------------------
INSERT INTO `t_admin_role` VALUES (1, 1, 1, '2025-03-15 16:09:57', b'0');
INSERT INTO `t_admin_role` VALUES (3, 8, 3, '2025-03-18 07:06:26', b'1');
INSERT INTO `t_admin_role` VALUES (4, 9, 4, '2025-03-18 12:53:05', b'1');
INSERT INTO `t_admin_role` VALUES (5, 9, 5, '2025-03-18 12:53:05', b'1');
INSERT INTO `t_admin_role` VALUES (6, 18, 3, '2025-03-30 21:43:14', b'0');
INSERT INTO `t_admin_role` VALUES (7, 19, 4, '2025-03-30 21:43:48', b'1');
INSERT INTO `t_admin_role` VALUES (8, 19, 22, '2025-03-30 22:34:14', b'1');
INSERT INTO `t_admin_role` VALUES (9, 19, 3, '2025-04-07 16:46:40', b'0');
INSERT INTO `t_admin_role` VALUES (10, 22, 3, '2025-04-07 17:08:56', b'0');

-- ----------------------------
-- Table structure for t_department
-- ----------------------------
DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `parent_id` int(0) NOT NULL DEFAULT 0 COMMENT '上级部门ID',
  `ancestors` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '祖级列表',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sort` int(0) NOT NULL DEFAULT 999 COMMENT '排序',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态（1：启用；2：禁用）',
  `is_system` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否为系统内置数据',
  `create_user` int(0) NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_user` int(0) NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name_parent_id`(`name`, `parent_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user`) USING BTREE,
  INDEX `idx_update_user`(`update_user`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_department
-- ----------------------------
INSERT INTO `t_department` VALUES (1, '集团总部', 21, '21', '集团核心管理部门', 1, 1, b'1', 1001, '2025-04-07 17:05:02', NULL, '2025-04-07 17:05:02', b'0');
INSERT INTO `t_department` VALUES (2, '技术中心', 21, '21', '负责技术研发', 2, 1, b'0', 1002, '2025-04-07 17:05:03', NULL, '2025-04-07 17:05:03', b'0');
INSERT INTO `t_department` VALUES (3, '人力资源部', 21, '21', '人事管理', 3, 1, b'0', 1003, '2025-04-07 17:05:04', NULL, '2025-04-07 17:05:04', b'0');
INSERT INTO `t_department` VALUES (4, '财务中心', 21, '12', '资金管理', 4, 1, b'0', 1004, '2025-04-07 17:05:05', NULL, '2025-04-07 17:05:05', b'0');
INSERT INTO `t_department` VALUES (5, '市场部', 21, '21', '市场拓展', 5, 2, b'0', 1005, '2025-04-07 17:05:30', NULL, '2025-04-07 17:05:30', b'0');
INSERT INTO `t_department` VALUES (6, '总裁办公室', 1, '21,1', '高管支持部门', 1, 1, b'0', 1001, '2025-04-07 17:05:09', NULL, '2025-04-07 17:05:09', b'0');
INSERT INTO `t_department` VALUES (7, '前端开发部', 2, '21,2', '前端技术研发', 2, 1, b'0', 1002, '2025-04-07 17:05:18', NULL, '2025-04-07 17:05:18', b'0');
INSERT INTO `t_department` VALUES (8, '招聘管理部', 3, '21,3', '人才招聘', 1, 1, b'0', 1003, '2025-04-07 17:05:28', NULL, '2025-04-07 17:05:28', b'0');
INSERT INTO `t_department` VALUES (9, '预算管理部', 4, '21,4', '财务预算编制', 1, 1, b'0', 1004, '2025-04-07 17:05:34', NULL, '2025-04-07 17:05:34', b'0');
INSERT INTO `t_department` VALUES (10, '品牌推广部', 5, '21,5', '品牌建设', 1, 2, b'0', 1005, '2025-04-07 17:05:38', NULL, '2025-04-07 17:05:38', b'0');
INSERT INTO `t_department` VALUES (11, '后端开发部', 2, '21,2', '服务端开发', 3, 1, b'0', 1002, '2025-04-07 17:05:42', NULL, '2025-04-07 17:05:42', b'0');
INSERT INTO `t_department` VALUES (12, '培训发展部', 3, '21,3', '员工培训', 2, 1, b'0', 1003, '2025-04-07 17:05:45', NULL, '2025-04-07 17:05:45', b'0');
INSERT INTO `t_department` VALUES (13, '会计核算部', 4, '21,4', '财务核算', 2, 1, b'0', 1004, '2025-04-07 17:05:51', NULL, '2025-04-07 17:05:51', b'0');
INSERT INTO `t_department` VALUES (14, '国际业务部', 5, '21,5', '海外市场拓展', 2, 2, b'0', 1005, '2025-04-07 17:05:55', NULL, '2025-04-07 17:05:55', b'0');
INSERT INTO `t_department` VALUES (15, '数据中台部', 2, '21,2', '数据体系建设', 4, 1, b'0', 1002, '2025-04-07 17:05:58', NULL, '2025-04-07 17:05:58', b'0');
INSERT INTO `t_department` VALUES (16, '战略决策组', 6, '21,1,6', '战略规划', 1, 1, b'0', 1001, '2025-04-07 17:06:01', NULL, '2025-04-07 17:06:01', b'0');
INSERT INTO `t_department` VALUES (17, 'UI设计组', 7, '21,2,7', '界面设计', 1, 1, b'0', 1002, '2025-04-07 17:06:04', NULL, '2025-04-07 17:06:04', b'0');
INSERT INTO `t_department` VALUES (18, '校园招聘组', 8, '21,3,8', '校招管理', 1, 1, b'0', 1003, '2025-04-07 17:06:07', NULL, '2025-04-07 17:06:07', b'0');
INSERT INTO `t_department` VALUES (19, '成本控制组', 9, '21,4,9', '成本核算', 1, 1, b'0', 1004, '2025-04-07 17:06:10', NULL, '2025-04-07 17:06:10', b'0');
INSERT INTO `t_department` VALUES (20, '海外市场组', 10, '21,5,10', '欧美市场拓展', 1, 2, b'0', 1005, '2025-04-07 17:06:18', NULL, '2025-04-07 17:06:18', b'0');
INSERT INTO `t_department` VALUES (21, 'xxx有限公司', 0, '', 'xxx有限公司', 999, 1, b'0', 1, '2025-03-21 01:02:46', NULL, '2025-03-21 01:02:46', b'0');

-- ----------------------------
-- Table structure for t_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_dict`;
CREATE TABLE `t_dict`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型名称',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典值',
  `sort_order` int(0) NULL DEFAULT 0 COMMENT '排序',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 1:启用 0:禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_code`(`type_code`, `dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通用字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_dict
-- ----------------------------
INSERT INTO `t_dict` VALUES (1, 'MEDICAL_CATEGORY', '医疗分类', 'INTERNAL_MEDICINE', '内科', NULL, 1, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (2, 'INTERNAL_MEDICINE', '内科子类', 'CARDIOVASCULAR', '心血管疾病', NULL, 1, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (3, 'INTERNAL_MEDICINE', '内科子类', 'RESPIRATORY', '呼吸系统疾病', NULL, 2, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (4, 'INTERNAL_MEDICINE', '内科子类', 'DIGESTIVE', '消化系统疾病', NULL, 3, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (5, 'INTERNAL_MEDICINE', '内科子类', 'ENDOCRINE', '内分泌疾病', NULL, 4, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (6, 'INTERNAL_MEDICINE', '内科子类', 'NEUROLOGICAL', '神经系统疾病', NULL, 5, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (7, 'MEDICAL_CATEGORY', '医疗分类', 'SURGERY', '外科', NULL, 2, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (8, 'SURGERY', '外科子类', 'ORTHOPEDICS', '骨科疾病', NULL, 1, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (9, 'SURGERY', '外科子类', 'GENERAL_SURGERY', '普通外科疾病', NULL, 2, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (10, 'SURGERY', '外科子类', 'BURN', '烧伤', NULL, 3, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (11, 'SURGERY', '外科子类', 'TRAUMA', '创伤', NULL, 4, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (12, 'SURGERY', '外科子类', 'PLASTIC_SURGERY', '整形外科', NULL, 5, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (13, 'MEDICAL_CATEGORY', '医疗分类', 'DERMATOLOGY', '皮肤科', NULL, 3, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (14, 'MEDICAL_CATEGORY', '医疗分类', 'OPHTHALMOLOGY', '眼科', NULL, 4, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (15, 'MEDICAL_CATEGORY', '医疗分类', 'GYNECOLOGY', '妇科', NULL, 5, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (16, 'MEDICAL_CATEGORY', '医疗分类', 'PEDIATRICS', '儿科', NULL, 6, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (17, 'MEDICAL_CATEGORY', '医疗分类', 'INFECTIOUS_DISEASE', '传染病', NULL, 7, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (18, 'MEDICAL_CATEGORY', '医疗分类', 'PSYCHIATRY', '精神心理科', NULL, 8, 1, NULL, '2024-12-30 15:33:11', '2024-12-30 15:33:11');
INSERT INTO `t_dict` VALUES (19, 'SHELF_LIFE_UNIT', '时间单位', 'DAY', '日', NULL, 1, 1, NULL, '2024-12-31 02:13:22', '2024-12-31 02:13:47');
INSERT INTO `t_dict` VALUES (20, 'SHELF_LIFE_UNIT', '时间单位', 'MONTH', '月', NULL, 2, 1, NULL, '2024-12-31 02:13:45', '2024-12-31 02:13:50');
INSERT INTO `t_dict` VALUES (21, 'SHELF_LIFE_UNIT', '时间单位', 'YEAR', '年', NULL, 3, 1, NULL, '2024-12-31 02:14:07', '2024-12-31 02:14:07');

-- ----------------------------
-- Table structure for t_doctor
-- ----------------------------
DROP TABLE IF EXISTS `t_doctor`;
CREATE TABLE `t_doctor`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生头像URL',
  `dept_id` int(0) NULL DEFAULT NULL COMMENT '所属科室ID',
  `hospital_id` int(0) NULL DEFAULT NULL COMMENT '所属医院ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `auth_status` tinyint(0) NULL DEFAULT 0 COMMENT '认证状态: 0-未认证, 1-已认证',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `consultation_count` int(0) NULL DEFAULT 0 COMMENT '诊疗人次',
  `consultation_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '诊疗费用',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医生简介',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_doctor
-- ----------------------------

-- ----------------------------
-- Table structure for t_drug
-- ----------------------------
DROP TABLE IF EXISTS `t_drug`;
CREATE TABLE `t_drug`  (
  `drug_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '药品ID，主键，自增',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '药品名称，例如“阿莫西林胶囊”',
  `generic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '药品通用名，药品的标准学名',
  `category_id` int(0) NOT NULL COMMENT '分类ID，与 t_drug_categories 表关联',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌名称，例如“辉瑞”',
  `manufacturer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产厂家，例如“华东制药”',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '药品描述，详细说明药品的用途和特点',
  `dosage_form` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '剂型，例如“片剂”、“胶囊”、“注射液”',
  `strength` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格，例如“500mg”、“100单位/ml”',
  `price` decimal(10, 2) NOT NULL COMMENT '售价，单位为人民币元',
  `discount_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣价，可为空',
  `stock_quantity` int(0) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sku` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库存单位编号（SKU），唯一',
  `bar_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条形码，唯一',
  `prescription` bit(1) NULL DEFAULT b'0' COMMENT '是否需要处方，TRUE 表示需要，FALSE 表示不需要',
  `shelf_life` int(0) NULL DEFAULT NULL COMMENT '保质期长度',
  `shelf_life_unit` int(3) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '保质期单位，对应枚举类',
  `storage_instructions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '储存说明，例如“避光保存”或“冷藏保存”',
  `create_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '药品创建时间，默认当前时间',
  `update_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '药品更新时间，自动更新为当前时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '是否逻辑删除，TRUE 表示已删除，FALSE 表示未删除',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '上下架状态',
  `creator_id` int(0) NULL DEFAULT NULL COMMENT '创建者ID',
  PRIMARY KEY (`drug_id`) USING BTREE,
  UNIQUE INDEX `sku`(`sku`) USING BTREE,
  UNIQUE INDEX `bar_code`(`bar_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品表，用于存储具体药品的信息和库存状态' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_drug
-- ----------------------------
INSERT INTO `t_drug` VALUES (1, '阿莫西林1', '阿莫西林', 5, '通用品牌', '华东制药', '用于治疗细菌感染的抗生素', '胶囊', '500mg', 12.50, NULL, 200, 'SKU001', 'BAR001', b'1', 20251231, 019, '阴凉干燥处保存', '2024-12-28 19:07:28', '2025-04-28 23:50:01', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (2, '布洛芬', '布洛芬', 4, '芬必得', '扬子江药业', '缓解疼痛和炎症', '片剂', '200mg', 8.00, 6.50, 150, 'SKU002', 'BAR002', b'0', 20260115, 019, '避免阳光直射', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (3, '维生素C', '抗坏血酸', 3, '善存', '拜耳', '增强免疫力', '片剂', '500mg', 20.00, 18.00, 300, 'SKU003', 'BAR003', b'0', 20241001, 019, '室温保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (4, '洛索洛芬钠片', '洛索洛芬钠', 4, '乐松', '第一三共', '非甾体类抗炎药，用于缓解疼痛', '片剂', '60mg', 15.00, 12.50, 180, 'SKU004', 'BAR004', b'1', 20250630, 019, '放在阴凉干燥处', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (5, '氯雷他定', '氯雷他定', 6, '开瑞坦', '默沙东', '用于缓解过敏症状', '片剂', '10mg', 25.00, 22.00, 200, 'SKU005', 'BAR005', b'0', 20260910, 019, '25°C以下保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (6, '止咳糖浆', '右美沙芬', 21, '止咳灵', '广州白云山', '缓解咳嗽症状', '糖浆', '100ml', 18.50, 15.00, 100, 'SKU006', 'BAR006', b'0', 20250320, NULL, '使用前摇匀', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (7, '多种维生素片', '多种维生素', 3, '善存', '拜耳', '每日维生素补充', '片剂', '每日一片', 30.00, 28.00, 500, 'SKU007', 'BAR007', b'0', 20260515, NULL, '原包装保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (8, '阿司匹林', '乙酰水杨酸', 4, '拜耳', '拜耳药业', '缓解疼痛和预防血栓', '片剂', '100mg', 10.00, NULL, 250, 'SKU008', 'BAR008', b'0', 20241231, NULL, '干燥处保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (9, '抗酸药片', '碳酸钙', 19, '达喜', '扬子江药业', '缓解胃酸过多和烧心', '咀嚼片', '500mg', 15.00, 13.00, 300, 'SKU009', 'BAR009', b'0', 20250430, NULL, '室温保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (10, '益生菌胶囊', '乳酸菌', 8, '益生菌+', '蒙牛', '支持消化系统健康', '胶囊', '100亿CFU', 60.00, 55.00, 200, 'SKU010', 'BAR010', b'0', 20240915, NULL, '开封后需冷藏', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (11, '护眼滴眼液', '羧甲基纤维素', 25, '润洁', '扬子江药业', '缓解眼疲劳', '液体', '10ml', 22.00, 20.00, 100, 'SKU011', 'BAR011', b'0', 20240630, NULL, '避免冷冻', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (12, '儿童咳嗽糖浆', '右美沙芬', 30, '小儿止咳糖浆', '葵花药业', '专为儿童设计的止咳药物', '糖浆', '100ml', 18.00, 16.00, 150, 'SKU012', 'BAR012', b'0', 20251101, NULL, '避免阳光直射', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (13, '铁片', '硫酸亚铁', 18, '铁之缘', '华润三九', '用于治疗缺铁性贫血', '片剂', '325mg', 25.00, 22.50, 300, 'SKU013', 'BAR013', b'0', 20250820, NULL, '避免受潮', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (14, '鱼油胶囊', '欧米伽-3', 9, '深海鱼油', '汤臣倍健', '支持心脏健康', '胶囊', '1000mg', 75.00, 68.00, 300, 'SKU014', 'BAR014', b'0', 20251101, NULL, '避免阳光直射', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (15, '胰岛素注射液', '甘精胰岛素', 20, '来得时', '赛诺菲', '长效胰岛素，用于糖尿病治疗', '注射液', '100单位/ml', 120.00, NULL, 80, 'SKU015', 'BAR015', b'1', 20241215, NULL, '冷藏保存，避免冷冻', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', NULL, NULL);
INSERT INTO `t_drug` VALUES (16, '儿童感冒颗粒', '对乙酰氨基酚', 15, '小儿感冒颗粒', '同仁堂', '缓解儿童感冒症状', '颗粒', '每袋5g', 28.00, 25.00, 250, 'SKU016', 'BAR016', b'0', 20250228, 019, '干燥处保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (17, '金银花颗粒', '金银花提取物', 14, '清热解毒颗粒', '北京同仁堂', '清热解毒，缓解炎症', '颗粒', '每袋5g', 35.00, 30.00, 150, 'SKU017', 'BAR017', b'0', 20240830, 019, '阴凉干燥处保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (18, '钙片', '柠檬酸钙', 27, '钙尔奇', '辉瑞制药', '支持骨骼健康', '片剂', '600mg', 40.00, 35.00, 400, 'SKU018', 'BAR018', b'0', 20251015, 019, '避免受潮', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (19, '锌含片', '葡萄糖酸锌', 18, '锌补充剂', '华北制药', '增强免疫力', '含片', '15mg', 28.00, 25.00, 180, 'SKU019', 'BAR019', b'0', 20251231, 019, '室温保存', '2024-12-28 19:07:28', '2025-04-01 14:01:57', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (20, '孕妇叶酸片', '叶酸', 29, '孕优叶酸', '汤臣倍健', '支持孕期健康', '片剂', '400mg', 30.00, 28.00, 150, 'SKU020', 'BAR020', b'0', 20240630, 019, '避免阳光直射', '2024-12-28 19:07:28', '2025-04-02 20:35:10', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (21, '维生素C咀嚼片', '维生素C', 30, '善存', '惠氏制药', '增强免疫力，抗氧化', '咀嚼片', '500mg', 25.00, 22.00, 200, 'SKU021', 'BAR021', b'0', 20251231, 019, '避光干燥保存', '2024-12-29 10:15:00', '2025-04-02 09:30:00', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (22, '111111', '123', 4, '123', '123', '12312', '123', '123', 0.05, NULL, 0, NULL, NULL, b'1', 4, 001, '123', '2025-04-27 22:57:11', '2025-04-28 23:55:26', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (23, '2222', '2222', 1, '222', '222222', '22222', '222', '222', 0.10, NULL, 4, NULL, NULL, b'1', 3, 001, '22222', '2025-04-29 17:01:22', '2025-04-29 17:01:22', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (24, '333', '3333', 1, '333', '333', '33333', '33', '333', 0.03, NULL, 4, NULL, NULL, b'0', 4, 001, '33333', '2025-04-29 17:22:43', '2025-04-29 17:22:43', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (25, '444', '444', 1, '444', '44444', '4444', '444', '444', 0.04, NULL, 3, NULL, NULL, b'0', 3, 001, '4444', '2025-04-29 17:32:12', '2025-04-29 17:32:12', b'0', 0, NULL);
INSERT INTO `t_drug` VALUES (26, '55555', '55', 1, '55', '555', '5555', '555', '555555', 0.02, NULL, 4, NULL, NULL, b'0', 3, 001, '555', '2025-04-29 17:40:29', '2025-04-29 17:54:13', b'1', 0, NULL);

-- ----------------------------
-- Table structure for t_drug_category
-- ----------------------------
DROP TABLE IF EXISTS `t_drug_category`;
CREATE TABLE `t_drug_category`  (
  `category_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键，自增',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称，例如“抗生素”、“维生素”',
  `parent_id` int(0) NULL DEFAULT 0 COMMENT '父级分类ID，用于支持多级分类',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分类描述，详细说明该分类的用途或特点',
  `create_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '分类创建时间，默认当前时间',
  `update_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '分类更新时间，自动更新为当前时间',
  `deleted` bit(1) NULL DEFAULT b'0' COMMENT '逻辑删除字段',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品分类表，用于存储药品的分类信息，支持多级分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_drug_category
-- ----------------------------
INSERT INTO `t_drug_category` VALUES (1, '处方药', 0, '需要医生处方的药品', '2024-12-28 19:07:28', '2025-04-09 23:30:07', b'0');
INSERT INTO `t_drug_category` VALUES (2, '非处方药', 0, '无需处方即可购买的药品', '2024-12-28 19:07:28', '2025-04-09 23:30:07', b'0');
INSERT INTO `t_drug_category` VALUES (3, '维生素与补充剂', 0, '健康补充剂与维生素', '2024-12-28 19:07:28', '2025-04-09 23:30:08', b'0');
INSERT INTO `t_drug_category` VALUES (4, '止痛药', 1, '用于缓解疼痛的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:12', b'0');
INSERT INTO `t_drug_category` VALUES (5, '抗生素', 1, '用于治疗感染的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:13', b'0');
INSERT INTO `t_drug_category` VALUES (6, '过敏药', 2, '用于治疗过敏的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:13', b'0');
INSERT INTO `t_drug_category` VALUES (7, '感冒和流感药物', 2, '缓解感冒和流感症状的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:14', b'0');
INSERT INTO `t_drug_category` VALUES (8, '消化健康', 3, '改善消化健康的补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:14', b'0');
INSERT INTO `t_drug_category` VALUES (9, '心脏健康', 3, '支持心血管健康的补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:15', b'0');
INSERT INTO `t_drug_category` VALUES (10, '皮肤健康', 0, '治疗皮肤问题的药物', '2024-12-28 19:07:28', '2025-04-09 23:30:09', b'0');
INSERT INTO `t_drug_category` VALUES (11, '眼部护理', 0, '用于眼睛健康和治疗的产品', '2024-12-28 19:07:28', '2025-04-09 23:30:10', b'0');
INSERT INTO `t_drug_category` VALUES (12, '体重管理', 0, '支持体重控制的补充剂', '2024-12-28 19:07:28', '2025-04-09 23:30:10', b'0');
INSERT INTO `t_drug_category` VALUES (13, '助眠药物', 2, '帮助改善睡眠问题的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:17', b'0');
INSERT INTO `t_drug_category` VALUES (14, '草本补充剂', 3, '基于天然草本的补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:17', b'0');
INSERT INTO `t_drug_category` VALUES (15, '儿童健康', 0, '专为儿童设计的药品', '2024-12-28 19:07:28', '2025-04-09 23:30:11', b'0');
INSERT INTO `t_drug_category` VALUES (16, '女性健康', 0, '专注于女性健康需求的产品', '2024-12-28 19:07:28', '2025-04-09 23:30:11', b'0');
INSERT INTO `t_drug_category` VALUES (17, '男性健康', 0, '专注于男性健康需求的产品', '2024-12-28 19:07:28', '2025-04-09 23:30:13', b'0');
INSERT INTO `t_drug_category` VALUES (18, '免疫增强剂', 3, '增强免疫力的补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:20', b'0');
INSERT INTO `t_drug_category` VALUES (19, '胃酸中和剂', 2, '治疗胃酸过多和烧心的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:21', b'0');
INSERT INTO `t_drug_category` VALUES (20, '糖尿病护理', 0, '用于糖尿病管理的产品', '2024-12-28 19:07:28', '2025-04-09 23:30:15', b'0');
INSERT INTO `t_drug_category` VALUES (21, '止咳药', 7, '用于缓解咳嗽的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:22', b'0');
INSERT INTO `t_drug_category` VALUES (22, '抗炎药', 1, '用于减少炎症的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:22', b'0');
INSERT INTO `t_drug_category` VALUES (23, '营养补充剂', 3, '支持全面健康的营养补充品', '2024-12-28 19:07:28', '2025-04-03 03:21:23', b'0');
INSERT INTO `t_drug_category` VALUES (24, '抗衰老产品', 10, '用于改善衰老症状的皮肤药物', '2024-12-28 19:07:28', '2025-04-03 03:21:24', b'0');
INSERT INTO `t_drug_category` VALUES (25, '眼药水', 11, '缓解干眼症和眼疲劳', '2024-12-28 19:07:28', '2025-04-03 03:21:25', b'0');
INSERT INTO `t_drug_category` VALUES (26, '哮喘药物', 1, '用于治疗哮喘的药品', '2024-12-28 19:07:28', '2025-04-03 03:21:25', b'0');
INSERT INTO `t_drug_category` VALUES (27, '骨骼健康', 3, '增强骨骼的健康补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:26', b'0');
INSERT INTO `t_drug_category` VALUES (28, '肝脏健康', 3, '支持肝脏功能的补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:26', b'0');
INSERT INTO `t_drug_category` VALUES (29, '孕期补充剂', 16, '专为孕妇设计的营养补充剂', '2024-12-28 19:07:28', '2025-04-03 03:21:27', b'0');
INSERT INTO `t_drug_category` VALUES (30, '儿童止咳糖浆', 15, '缓解儿童咳嗽的药物', '2024-12-28 19:07:28', '2025-04-03 03:21:28', b'0');
INSERT INTO `t_drug_category` VALUES (31, '测试分类', 29, '', '2025-04-10 02:24:04', '2025-04-10 02:24:04', NULL);
INSERT INTO `t_drug_category` VALUES (32, '测试分类2', 16, '', '2025-04-10 02:46:06', '2025-04-10 02:46:06', b'0');
INSERT INTO `t_drug_category` VALUES (33, '新增分类', 0, '', '2025-04-10 02:47:03', '2025-04-10 02:47:03', b'0');

-- ----------------------------
-- Table structure for t_drug_img
-- ----------------------------
DROP TABLE IF EXISTS `t_drug_img`;
CREATE TABLE `t_drug_img`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `drug_id` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '关联药品ID（逻辑关联）',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片存储路径',
  `current_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传后修改过的图片名称',
  `original_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原来的名称',
  `detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片说明',
  `is_main` tinyint(1) NULL DEFAULT 0 COMMENT '是否主图（0否 1是）',
  `sort` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '排序权重',
  `upload_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '上传时间',
  `update_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `upload_user` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '上传人',
  `file_size` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '文件大小（KB）',
  `file_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件格式',
  `storage_type` tinyint(0) NULL DEFAULT NULL COMMENT '存储类型1oss/2minio/3local',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '药品图片表（无外键约束版）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_drug_img
-- ----------------------------
INSERT INTO `t_drug_img` VALUES (13, NULL, 'drug/2025/04/22/2bc7a508bd034954b6e7ad7c77485f8e.jpg', '2bc7a508bd034954b6e7ad7c77485f8e.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-22 10:36:57', '2025-04-22 10:36:57', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (14, NULL, 'drug/2025/04/22/f4cdbbbe72864249875a5dbfdcc356a2.png', 'f4cdbbbe72864249875a5dbfdcc356a2.png', '微信图片_20250215161819.png', NULL, 1, 0, '2025-04-22 10:41:34', '2025-04-22 10:41:34', NULL, 14, 'png', NULL);
INSERT INTO `t_drug_img` VALUES (15, NULL, 'drug/2025/04/22/a29829029ebd475fb0858ce908f1267d.jpg', 'a29829029ebd475fb0858ce908f1267d.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-22 10:43:11', '2025-04-22 10:43:11', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (18, 1, 'drug/2025/04/22/b767a83559d941f897d58247be265be3.png', 'b767a83559d941f897d58247be265be3.png', '微信图片_20250215161819.png', NULL, 1, 0, '2025-04-22 13:29:12', '2025-04-22 13:29:53', NULL, 14, 'png', NULL);
INSERT INTO `t_drug_img` VALUES (19, NULL, 'drug/2025/04/22/9be2923cd9294a698f3254eee8154368.jpg', '9be2923cd9294a698f3254eee8154368.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-22 13:31:17', '2025-04-22 13:31:17', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (20, NULL, 'drug/2025/04/22/e5cabb3be52e48de8da0da125132db72.PNG', 'e5cabb3be52e48de8da0da125132db72.PNG', '捕获.PNG', NULL, 1, 0, '2025-04-22 13:34:41', '2025-04-22 13:34:41', NULL, 31, 'PNG', NULL);
INSERT INTO `t_drug_img` VALUES (21, NULL, 'drug/2025/04/22/4ed0a0dd5d3842f98ed3f3dab8e1de45.PNG', '4ed0a0dd5d3842f98ed3f3dab8e1de45.PNG', '捕获.PNG', NULL, 1, 0, '2025-04-22 13:55:35', '2025-04-22 13:55:35', NULL, 31, 'PNG', NULL);
INSERT INTO `t_drug_img` VALUES (22, NULL, 'drug/2025/04/22/3bc582338225415c9930a8b179b4a005.PNG', '3bc582338225415c9930a8b179b4a005.PNG', '捕获.PNG', NULL, 1, 0, '2025-04-22 14:22:53', '2025-04-22 14:22:53', NULL, 31, 'PNG', NULL);
INSERT INTO `t_drug_img` VALUES (23, NULL, 'drug/2025/04/22/be6e306ace8d4d37b0538bce858478d9.jpg', 'be6e306ace8d4d37b0538bce858478d9.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-22 14:37:04', '2025-04-22 14:37:04', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (26, 2, 'drug/2025/04/22/00169898eedf4c22a243b8ec69b85d87.jpg', '00169898eedf4c22a243b8ec69b85d87.jpg', '渣哥头像.jpg', NULL, 0, 0, '2025-04-22 16:57:31', '2025-04-22 16:57:42', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (27, NULL, 'drug/2025/04/27/d1d5034f55f6437186ca2d971a959f29.jpg', 'd1d5034f55f6437186ca2d971a959f29.jpg', '渣哥头像.jpg', NULL, 0, 0, '2025-04-27 22:54:06', '2025-04-27 22:54:06', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (28, 22, 'drug/2025/04/29/9261528348e34ddcafa1112111162d29.jpg', NULL, NULL, NULL, 1, 0, '2025-04-29 15:40:20', '2025-04-29 15:40:20', NULL, NULL, NULL, NULL);
INSERT INTO `t_drug_img` VALUES (29, 22, 'drug/2025/04/29/9261528348e34ddcafa1112111162d29.jpg', NULL, NULL, NULL, 1, 0, '2025-04-29 16:38:03', '2025-04-29 16:38:03', NULL, NULL, NULL, NULL);
INSERT INTO `t_drug_img` VALUES (30, 22, 'drug/2025/04/29/9261528348e34ddcafa1112111162d29.jpg', NULL, NULL, NULL, 1, 0, '2025-04-29 16:40:09', '2025-04-29 16:40:09', NULL, NULL, NULL, NULL);
INSERT INTO `t_drug_img` VALUES (31, 22, 'drug/2025/04/29/9261528348e34ddcafa1112111162d29.jpg', NULL, NULL, NULL, 1, 0, '2025-04-29 16:40:09', '2025-04-29 16:40:09', NULL, NULL, NULL, NULL);
INSERT INTO `t_drug_img` VALUES (32, NULL, 'drug/2025/04/29/aca4ff04c238467db40747476ef29302.jpg', 'aca4ff04c238467db40747476ef29302.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-29 17:01:22', '2025-04-29 17:01:22', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (33, 23, 'drug/2025/04/29/cb21b4f86309472191eed4ad7b353c0d.jpg', 'cb21b4f86309472191eed4ad7b353c0d.jpg', '渣哥头像.jpg', NULL, 1, 4, '2025-04-29 17:18:03', '2025-04-29 17:18:03', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (34, NULL, 'drug/2025/04/29/9c3e8c153a7a4ad3a1130e725cc0a497.jpg', '9c3e8c153a7a4ad3a1130e725cc0a497.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-29 17:22:43', '2025-04-29 17:22:43', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (35, NULL, 'drug/2025/04/29/57cd44842b5e4591a95eb7d5cfff777c.jpg', '57cd44842b5e4591a95eb7d5cfff777c.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-29 17:32:12', '2025-04-29 17:32:12', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (36, 24, 'drug/2025/04/29/a344604b9ed843a3877ed49b4673a001.jpg', 'a344604b9ed843a3877ed49b4673a001.jpg', '渣哥头像.jpg', NULL, 1, 6, '2025-04-29 17:35:18', '2025-04-29 17:35:18', NULL, 14, 'jpg', NULL);
INSERT INTO `t_drug_img` VALUES (37, NULL, 'drug/2025/04/29/dbbd27cde3874b2fa21e3cbd97b3662c.jpg', 'dbbd27cde3874b2fa21e3cbd97b3662c.jpg', '渣哥头像.jpg', NULL, 1, 0, '2025-04-29 17:40:45', '2025-04-29 17:40:45', NULL, 14, 'jpg', NULL);

-- ----------------------------
-- Table structure for t_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `t_evaluation`;
CREATE TABLE `t_evaluation`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `doctor_id` int(0) NOT NULL COMMENT '医生ID',
  `patient_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '患者电话(脱敏)',
  `rating` decimal(2, 1) NOT NULL COMMENT '评分',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `evaluation_date` date NULL DEFAULT NULL COMMENT '评价日期',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_evaluation
-- ----------------------------

-- ----------------------------
-- Table structure for t_hospital
-- ----------------------------
DROP TABLE IF EXISTS `t_hospital`;
CREATE TABLE `t_hospital`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医院名称',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医院等级',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医院地址',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '医院简介',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医院类别：综合医院/专业医院',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医院信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_hospital
-- ----------------------------

-- ----------------------------
-- Table structure for t_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_permission`;
CREATE TABLE `t_permission`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `perm_type` tinyint(0) NOT NULL COMMENT '权限类型（1-目录，2-菜单，3-按钮）',
  `perm_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限唯一标识（如:user:add）',
  `parent_id` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '父权限ID',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由地址',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由名称',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `hidden` bit(1) NULL DEFAULT b'0' COMMENT '是否隐藏（0-否，1-是）',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'vue组件路径',
  `sort` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_permission
-- ----------------------------
INSERT INTO `t_permission` VALUES (1, '系统管理', 1, 'system', 0, '/system', 'System', 'Setting', b'0', 'layout', 3, 1, '2025-03-15 16:20:38', '2025-04-01 22:18:34', NULL);
INSERT INTO `t_permission` VALUES (2, '用户管理', 2, 'system:user', 1, '/system/user', 'SystemUser', 'User', b'0', 'system/user', 4, 1, '2025-03-15 16:21:12', '2025-03-28 04:15:13', '');
INSERT INTO `t_permission` VALUES (3, '角色管理', 2, 'system:role', 1, '/system/role', 'SystemRole', 'UserFilled', b'0', 'system/role', 5, 1, '2025-03-15 16:52:34', '2025-03-28 12:57:10', '');
INSERT INTO `t_permission` VALUES (4, '菜单管理', 2, 'system:menu3', 1, '/system/menu', 'SystemMenu', 'Menu', b'0', 'system/menu', 6, 1, '2025-03-15 16:58:15', '2025-03-28 23:37:16', '');
INSERT INTO `t_permission` VALUES (8, '新增', 3, 'system:user:add', 2, '', NULL, '', b'0', '', 999, 1, '2025-03-29 02:18:55', '2025-03-29 02:18:55', '');
INSERT INTO `t_permission` VALUES (11, '删除', 3, 'system:user:delete', 2, '', NULL, '', b'0', '', 0, 1, '2025-03-30 04:08:34', '2025-03-30 04:08:55', '');
INSERT INTO `t_permission` VALUES (12, '修改', 3, 'system:user:edit', 2, '', NULL, '', b'0', '', 0, 1, '2025-03-30 04:11:04', '2025-03-30 04:11:04', '');
INSERT INTO `t_permission` VALUES (13, '列表', 3, 'system:user:list', 2, '', NULL, '', b'0', '', 0, 1, '2025-03-30 04:14:26', '2025-03-30 04:14:26', '');
INSERT INTO `t_permission` VALUES (14, '导出', 3, 'system:user:export', 2, '', NULL, '', b'0', '', 0, 1, '2025-03-30 04:14:49', '2025-03-30 04:14:49', '');
INSERT INTO `t_permission` VALUES (15, '修改权限', 3, 'system:role:updatePermission', 3, '', NULL, '', b'0', '', 0, 1, '2025-03-31 02:58:16', '2025-03-31 03:18:08', '');
INSERT INTO `t_permission` VALUES (16, '删除', 3, 'system:role:delete', 3, '', NULL, '', b'0', '', 0, 1, '2025-03-31 02:59:06', '2025-03-31 02:59:06', '');
INSERT INTO `t_permission` VALUES (17, '修改', 3, 'system:role:edit', 3, '', NULL, '', b'0', '', 0, 1, '2025-03-31 02:59:54', '2025-03-31 02:59:54', '');
INSERT INTO `t_permission` VALUES (18, '新增', 3, 'system:role:add', 3, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:02:20', '2025-03-31 03:02:20', '');
INSERT INTO `t_permission` VALUES (19, '列表', 3, 'system:role:list', 3, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:03:23', '2025-03-31 03:03:23', '');
INSERT INTO `t_permission` VALUES (20, '新增', 3, 'system:menu:add', 4, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:30:03', '2025-03-31 03:30:03', '');
INSERT INTO `t_permission` VALUES (21, '修改', 3, 'system:menu:edit', 4, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:30:53', '2025-03-31 03:30:53', '');
INSERT INTO `t_permission` VALUES (22, '删除', 3, 'system:menu:delete', 4, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:31:20', '2025-03-31 03:31:20', '');
INSERT INTO `t_permission` VALUES (23, '列表', 3, 'system:menu:list', 4, '', NULL, '', b'0', '', 0, 1, '2025-03-31 03:32:04', '2025-03-31 03:32:04', '');
INSERT INTO `t_permission` VALUES (24, '药品管理', 1, 'drug', 0, '/drugs', 'Drugs', 'Refrigerator', b'0', 'layout', 8, 1, '2025-04-01 22:18:35', '2025-04-01 23:51:21', '');
INSERT INTO `t_permission` VALUES (25, '药品管理', 2, 'drugs:index', 24, '/drugs/index', 'DrugsIndex', 'FirstAidKit', b'0', 'drugs/index', 0, 1, '2025-04-01 23:19:42', '2025-04-24 22:36:36', '');
INSERT INTO `t_permission` VALUES (26, '运营管理', 1, 'operations', 0, '/operations', 'Operations', 'BrushFilled', b'0', 'layout', 5, 1, '2025-04-22 17:10:30', '2025-04-22 18:16:01', '');
INSERT INTO `t_permission` VALUES (27, '小程序管理', 2, 'operations:xcx', 26, '/operations/xcx', 'Xcx', 'Message', b'0', 'operations/xcx', 2, 1, '2025-04-22 17:13:02', '2025-04-22 18:16:09', '');
INSERT INTO `t_permission` VALUES (28, '分类管理', 2, 'drugs:category', 24, '/drugs/category', 'DrugsCategory', 'CopyDocument', b'0', 'drugs/category', 1, 1, '2025-04-24 23:03:45', '2025-04-25 13:09:50', '');
INSERT INTO `t_permission` VALUES (29, '医生管理', 1, 'doctor', 0, '/doctor', 'Doctor', 'FirstAidKit', b'0', 'layout', 1, 1, '2025-04-24 23:09:33', '2025-04-25 18:26:15', '');
INSERT INTO `t_permission` VALUES (30, '医生管理', 2, 'doctor:index', 29, '/doctor/index', 'DoctorIndex', 'CirclePlusFilled', b'0', 'doctor/index', 0, 1, '2025-04-24 23:26:46', '2025-04-25 13:14:23', '');
INSERT INTO `t_permission` VALUES (31, '医院管理', 2, 'doctor:hospital', 29, '/doctor/hospital', 'DoctorHospital', 'Files', b'0', 'doctor/hospital', 2, 1, '2025-04-24 23:31:37', '2025-04-25 18:26:20', '');

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称（唯一）',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `create_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`role_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO `t_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限，可以管理所有功能和数据', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (3, '管理员', 'ADMIN', '一般管理员', 1, '2025-03-12 23:07:11', '2025-03-31 03:45:28');
INSERT INTO `t_role` VALUES (4, '财务管理员', 'FINANCE_ADMIN', '负责系统中的财务数据管理和财务报表查看', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (5, '用户管理员', 'USER_ADMIN', '负责用户账号的管理、审核和维护', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (6, '运营专员', 'OPERATIONS', '负责日常运营工作，包括活动策划和执行', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (11, '开发人员', 'DEVELOPER', '系统开发和维护人员，拥有特定的开发权限', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (13, '人事专员', 'HR_SPECIALIST', '负责员工管理和人事相关事务', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (15, '商品管理员', 'PRODUCT_MANAGER', '负责商品上架、定价和库存管理', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (16, '订单处理员', 'ORDER_MANAGER', '负责订单处理和物流跟踪', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (17, '售后服务员', 'AFTER_SALES', '负责产品售后服务和退换货处理', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');
INSERT INTO `t_role` VALUES (20, 'API接口管理员', 'API_ADMIN', '负责管理和维护系统对外API接口', 1, '2025-03-12 23:07:11', '2025-03-15 02:36:30');

-- ----------------------------
-- Table structure for t_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission`;
CREATE TABLE `t_role_permission`  (
  `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` int(0) UNSIGNED NOT NULL COMMENT '角色ID',
  `perm_id` int(0) UNSIGNED NOT NULL COMMENT '权限ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id`, `perm_id`) USING BTREE,
  INDEX `idx_perm_id`(`perm_id`) USING BTREE,
  CONSTRAINT `t_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `t_role_permission_ibfk_2` FOREIGN KEY (`perm_id`) REFERENCES `t_permission` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_role_permission
-- ----------------------------
INSERT INTO `t_role_permission` VALUES (160, 3, 1, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (161, 3, 2, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (162, 3, 3, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (163, 3, 16, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (164, 3, 17, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (165, 3, 18, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (166, 3, 19, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (167, 3, 4, '2025-04-09 04:41:15');
INSERT INTO `t_role_permission` VALUES (218, 1, 29, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (219, 1, 30, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (220, 1, 31, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (221, 1, 1, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (222, 1, 2, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (223, 1, 11, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (224, 1, 12, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (225, 1, 13, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (226, 1, 14, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (227, 1, 8, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (228, 1, 3, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (229, 1, 15, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (230, 1, 16, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (231, 1, 17, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (232, 1, 18, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (233, 1, 19, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (234, 1, 4, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (235, 1, 20, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (236, 1, 21, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (237, 1, 22, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (238, 1, 23, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (239, 1, 26, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (240, 1, 27, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (241, 1, 24, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (242, 1, 25, '2025-04-24 23:33:38');
INSERT INTO `t_role_permission` VALUES (243, 1, 28, '2025-04-24 23:33:38');

-- ----------------------------
-- Table structure for t_service
-- ----------------------------
DROP TABLE IF EXISTS `t_service`;
CREATE TABLE `t_service`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `consultation_type` tinyint(0) NOT NULL COMMENT '问诊类型：AI/人工',
  `doctor_id` int(0) NULL DEFAULT NULL COMMENT '医生ID，AI问诊可为空',
  `order_id` bigint(0) NOT NULL COMMENT '关联订单ID',
  `user_id` bigint(0) NOT NULL COMMENT '患者用户ID',
  `status` tinyint(0) NOT NULL COMMENT '问诊状态：0待接诊/1进行中/2已完成/3已取消/4已超时',
  `symptom_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '症状描述',
  `service_duration` int(0) NULL DEFAULT NULL COMMENT '服务时长(分钟)',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '问诊开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '问诊结束时间',
  `diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '诊断结果',
  `treatment_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '治疗建议',
  `prescription_id` bigint(0) NULL DEFAULT NULL COMMENT '关联处方ID',
  `evaluation_id` decimal(2, 1) NULL DEFAULT NULL COMMENT '评价id',
  `cancel_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '问诊服务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_service
-- ----------------------------

-- ----------------------------
-- Table structure for t_user_account
-- ----------------------------
DROP TABLE IF EXISTS `t_user_account`;
CREATE TABLE `t_user_account`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识（自增主键）',
  `openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信用户唯一标识（开放平台）',
  `unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信开放平台统一用户标识',
  `nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户手机号（11位数字）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像URL地址',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '记录创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '记录最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uni_phone`(`phone`) USING BTREE,
  INDEX `idx_openid`(`openid`(20)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户基本信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user_account
-- ----------------------------
INSERT INTO `t_user_account` VALUES (5, 'oupX-5A28Ub3QcYYSXpDDwhiU318', NULL, NULL, NULL, NULL, '2025-01-23 18:17:35', '2025-01-23 18:17:35');

SET FOREIGN_KEY_CHECKS = 1;
