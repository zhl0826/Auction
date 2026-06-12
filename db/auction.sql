/*
 Navicat Premium Dump SQL

 Source Server         : 1
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : auction

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 12/06/2026 17:30:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for after_sale
-- ----------------------------
DROP TABLE IF EXISTS `after_sale`;
CREATE TABLE `after_sale`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/refunded/rejected',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '售后申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of after_sale
-- ----------------------------
INSERT INTO `after_sale` VALUES (1, 1, '尺码不对', 'pending', '2026-06-12 15:18:07');

-- ----------------------------
-- Table structure for auction_admin
-- ----------------------------
DROP TABLE IF EXISTS `auction_admin`;
CREATE TABLE `auction_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT 'active / disabled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖管理员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of auction_admin
-- ----------------------------
INSERT INTO `auction_admin` VALUES (1, 'auction01', '123456', '审核员-张', 'active', '2026-06-12 15:18:07');
INSERT INTO `auction_admin` VALUES (2, 'auction02', '123456', '审核员-李', 'active', '2026-06-12 15:18:07');

-- ----------------------------
-- Table structure for bid
-- ----------------------------
DROP TABLE IF EXISTS `bid`;
CREATE TABLE `bid`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `bidder_id` bigint NOT NULL COMMENT '出价人 user.id',
  `price` decimal(12, 2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods`(`goods_id` ASC) USING BTREE,
  INDEX `idx_bidder`(`bidder_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出价记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bid
-- ----------------------------
INSERT INTO `bid` VALUES (1, 3, 1, 380.00, '2026-06-12 15:18:07');
INSERT INTO `bid` VALUES (2, 3, 2, 480.00, '2026-06-12 15:18:07');
INSERT INTO `bid` VALUES (3, 4, 1, 6800.00, '2026-06-12 15:18:07');
INSERT INTO `bid` VALUES (4, 4, 4, 7100.00, '2026-06-12 15:18:07');
INSERT INTO `bid` VALUES (5, 3, 5, 500.00, '2026-06-12 17:10:10');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数码/服饰/古董/书籍/其它',
  `seller_id` bigint NOT NULL COMMENT '卖家 user.id',
  `start_price` decimal(12, 2) NOT NULL COMMENT '起拍价',
  `current_price` decimal(12, 2) NOT NULL COMMENT '当前价（开拍后等于 start_price）',
  `min_increment` decimal(12, 2) NOT NULL DEFAULT 10.00 COMMENT '最小加价幅度',
  `cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图 URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/on_sale/sold/off_shelf/rejected',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `end_at` datetime NULL DEFAULT NULL COMMENT '竞拍结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_seller`(`seller_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拍卖商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '索尼 A7M4 相机', '数码', 3, 8000.00, 8000.00, 50.00, 'https://picsum.photos/seed/g1/200/140', NULL, 'pending', NULL, '2026-06-20 20:00:00', '2026-06-12 15:18:07');
INSERT INTO `goods` VALUES (2, '宋代茶盏', '古董', 4, 1500.00, 1500.00, 100.00, 'https://picsum.photos/seed/g2/200/140', NULL, 'on_sale', NULL, '2026-06-18 20:00:00', '2026-06-12 15:18:07');
INSERT INTO `goods` VALUES (3, 'Nike Air Max', '服饰', 3, 300.00, 500.00, 20.00, 'https://picsum.photos/seed/g3/200/140', NULL, 'on_sale', NULL, '2026-06-15 12:00:00', '2026-06-12 15:18:07');
INSERT INTO `goods` VALUES (4, 'iPhone 15 Pro', '数码', 4, 6000.00, 7100.00, 50.00, 'https://picsum.photos/seed/g4/200/140', NULL, 'on_sale', NULL, '2026-06-13 21:00:00', '2026-06-12 15:18:07');
INSERT INTO `goods` VALUES (5, '玩偶', '收藏品', 5, 100.00, 100.00, 10.00, 'http://tmp/J2cHuTORXWN0d4720ac59c6720223db45716a752a093.png', '一个玩偶', 'on_sale', NULL, '2026-06-15 02:03:00', '2026-06-12 17:01:26');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `goods_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `amount` decimal(12, 2) NOT NULL COMMENT '成交价',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'paid' COMMENT 'paid/refunded/cancelled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_buyer`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller`(`seller_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 4, 'iPhone 15 Pro', 1, 4, 7100.00, 'paid', '2026-06-12 15:18:07');
INSERT INTO `order` VALUES (2, 3, 'Nike Air Max', 5, 3, 500.00, 'completed', '2026-06-12 17:10:10');

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统管理员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'sysadmin', '123456', '超级管理员', '2026-06-12 15:18:07');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` int NOT NULL,
  `min_increment` decimal(12, 2) NOT NULL DEFAULT 10.00,
  `fee_rate` decimal(5, 4) NOT NULL DEFAULT 0.0200 COMMENT '手续费比例 0~1',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 10.00, 0.0200);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录密码（明文，演示用）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'buyer / seller',
  `balance` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT 'active / banned',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '买家/卖家用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'buyer01', '123456', '小明', 'buyer', 5000.00, NULL, 'active', '2026-06-12 15:18:07');
INSERT INTO `user` VALUES (2, 'buyer02', '123456', '小红', 'buyer', 0.00, NULL, 'banned', '2026-06-12 15:18:07');
INSERT INTO `user` VALUES (3, 'seller01', '123456', '老王', 'seller', 1700.00, NULL, 'active', '2026-06-12 15:18:07');
INSERT INTO `user` VALUES (4, 'seller02', '123456', '阿强', 'seller', 3000.00, NULL, 'active', '2026-06-12 15:18:07');
INSERT INTO `user` VALUES (5, 'passerby', '123456', 'passerby', 'buyer', 0.00, NULL, 'active', '2026-06-12 16:53:52');

SET FOREIGN_KEY_CHECKS = 1;
