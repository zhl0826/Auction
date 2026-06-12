-- ============================================================
-- 拍卖管理后台 - 数据库初始化脚本
-- 数据库：MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS auction DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction;

-- ------------------------------------------------------------
-- 1. 买家/卖家用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '登录密码（明文，演示用）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    role        VARCHAR(20)  NOT NULL COMMENT 'buyer / seller',
    balance     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '账户余额',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status      VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'active / banned',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家/卖家用户';

-- ------------------------------------------------------------
-- 2. 拍卖管理员表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS auction_admin;
CREATE TABLE auction_admin (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50)  DEFAULT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT 'active / disabled',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拍卖管理员';

-- ------------------------------------------------------------
-- 3. 系统管理员表（账号固定写在 AuthController 演示，这里也建一张便于扩展）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50)  DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员';

-- ------------------------------------------------------------
-- 4. 商品（拍卖品）表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS goods;
CREATE TABLE goods (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL,
    type            VARCHAR(50)  NOT NULL COMMENT '数码/服饰/古董/书籍/其它',
    seller_id       BIGINT       NOT NULL COMMENT '卖家 user.id',
    start_price     DECIMAL(12,2) NOT NULL COMMENT '起拍价',
    current_price   DECIMAL(12,2) NOT NULL COMMENT '当前价（开拍后等于 start_price）',
    min_increment   DECIMAL(12,2) NOT NULL DEFAULT 10 COMMENT '最小加价幅度',
    cover           VARCHAR(500) DEFAULT NULL COMMENT '封面图 URL',
    description     TEXT         DEFAULT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/on_sale/sold/off_shelf/rejected',
    reject_reason   VARCHAR(500) DEFAULT NULL,
    end_at          DATETIME     DEFAULT NULL COMMENT '竞拍结束时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拍卖商品';

-- ------------------------------------------------------------
-- 5. 订单表（出价结束后生成；演示用，最小字段）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id    BIGINT NOT NULL,
    goods_title VARCHAR(200) DEFAULT NULL,
    buyer_id    BIGINT NOT NULL,
    seller_id   BIGINT NOT NULL,
    amount      DECIMAL(12,2) NOT NULL COMMENT '成交价',
    status      VARCHAR(20) NOT NULL DEFAULT 'paid' COMMENT 'paid/refunded/cancelled',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_buyer (buyer_id),
    INDEX idx_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ------------------------------------------------------------
-- 6. 出价（竞拍）记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS bid;
CREATE TABLE bid (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id    BIGINT NOT NULL,
    bidder_id   BIGINT NOT NULL COMMENT '出价人 user.id',
    price       DECIMAL(12,2) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_goods (goods_id),
    INDEX idx_bidder (bidder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出价记录';

-- ------------------------------------------------------------
-- 7. 售后表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS after_sale;
CREATE TABLE after_sale (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT NOT NULL,
    reason      VARCHAR(500) DEFAULT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/refunded/rejected',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申请';

-- ------------------------------------------------------------
-- 8. 系统参数表（单行，id=1）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    id              INT PRIMARY KEY,
    min_increment   DECIMAL(12,2) NOT NULL DEFAULT 10,
    fee_rate        DECIMAL(5,4)  NOT NULL DEFAULT 0.0200 COMMENT '手续费比例 0~1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

-- ============================================================
-- 初始数据
-- ============================================================

-- 拍卖管理员（密码 123456）
INSERT INTO auction_admin(username, password, nickname) VALUES
('auction01', '123456', '审核员-张'),
('auction02', '123456', '审核员-李');

-- 系统管理员（密码 123456，写死鉴权也走这张表）
INSERT INTO sys_admin(username, password, nickname) VALUES
('sysadmin', '123456', '超级管理员');

-- 买家/卖家（密码 123456）
INSERT INTO user(username, password, nickname, role, balance, status) VALUES
('buyer01',  '123456', '小明', 'buyer',  5000.00, 'active'),
('buyer02',  '123456', '小红', 'buyer',     0.00, 'banned'),
('seller01', '123456', '老王', 'seller', 1200.00, 'active'),
('seller02', '123456', '阿强', 'seller', 3000.00, 'active');

-- 商品
INSERT INTO goods(title, type, seller_id, start_price, current_price, min_increment, cover, status, end_at) VALUES
('索尼 A7M4 相机', '数码', 3, 8000,  8000,  50, 'https://picsum.photos/seed/g1/200/140', 'pending', '2026-06-20 20:00'),
('宋代茶盏',       '古董', 4, 1500,  1500, 100, 'https://picsum.photos/seed/g2/200/140', 'pending', '2026-06-18 20:00'),
('Nike Air Max',   '服饰', 3,  300,   480,  20, 'https://picsum.photos/seed/g3/200/140', 'on_sale', '2026-06-15 12:00'),
('iPhone 15 Pro',  '数码', 4, 6000,  7100,  50, 'https://picsum.photos/seed/g4/200/140', 'on_sale', '2026-06-13 21:00');

-- 订单（演示：iPhone 15 Pro 已成交给 buyer01）
INSERT INTO `order`(goods_id, goods_title, buyer_id, seller_id, amount, status) VALUES
(4, 'iPhone 15 Pro', 1, 4, 7100, 'paid');

-- 出价记录
INSERT INTO bid(goods_id, bidder_id, price) VALUES
(3, 1, 380),
(3, 2, 480),
(4, 1, 6800),
(4, 4, 7100);

-- 售后申请
INSERT INTO after_sale(order_id, reason) VALUES (1, '尺码不对');

-- 系统参数
INSERT INTO sys_config(id, min_increment, fee_rate) VALUES (1, 10, 0.0200);
