USE `auction`;
-- MySQL dump 10.13  Distrib 8.3.0, for Win64 (x86_64)
--
-- Host: localhost    Database: auction
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `after_sale`
--

DROP TABLE IF EXISTS `after_sale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `after_sale` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/refunded/rejected',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='售后申请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `after_sale`
--

LOCK TABLES `after_sale` WRITE;
/*!40000 ALTER TABLE `after_sale` DISABLE KEYS */;
INSERT INTO `after_sale` (`id`, `order_id`, `reason`, `status`, `created_at`) VALUES (2,7,'描述与商品不符','refunded','2026-06-23 20:30:21'),(3,1,'6','rejected','2026-06-23 20:34:13'),(6,8,'不合适','refunded','2026-06-23 21:43:09');
/*!40000 ALTER TABLE `after_sale` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auction_admin`
--

DROP TABLE IF EXISTS `auction_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT 'active / disabled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拍卖管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction_admin`
--

LOCK TABLES `auction_admin` WRITE;
/*!40000 ALTER TABLE `auction_admin` DISABLE KEYS */;
INSERT INTO `auction_admin` (`id`, `username`, `password`, `nickname`, `status`, `created_at`) VALUES (1,'auction01','123456','审核员-张','active','2026-06-12 09:22:59');
/*!40000 ALTER TABLE `auction_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bid`
--

DROP TABLE IF EXISTS `bid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bid` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `bidder_id` bigint NOT NULL COMMENT '出价人 user.id',
  `price` decimal(12,2) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT 'active/outbid/won/cancelled',
  `refunded` tinyint(1) NOT NULL DEFAULT '0' COMMENT '鏄?惁宸查?杩',
  PRIMARY KEY (`id`),
  KEY `idx_goods` (`goods_id`),
  KEY `idx_bidder` (`bidder_id`),
  KEY `idx_status` (`status`),
  KEY `idx_goods_status` (`goods_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='出价记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bid`
--

LOCK TABLES `bid` WRITE;
/*!40000 ALTER TABLE `bid` DISABLE KEYS */;
INSERT INTO `bid` (`id`, `goods_id`, `bidder_id`, `price`, `created_at`, `status`, `refunded`) VALUES (1,3,1,380.00,'2026-06-12 09:22:59','cancelled',1),(2,3,2,480.00,'2026-06-12 09:22:59','won',0),(3,4,1,6800.00,'2026-06-12 09:22:59','cancelled',1),(4,4,4,7100.00,'2026-06-12 09:22:59','won',0),(5,8,2,500.00,'2026-06-23 17:17:38','cancelled',1),(6,8,2,510.00,'2026-06-23 17:18:54','cancelled',1),(7,8,2,220.00,'2026-06-23 18:02:04','outbid',0),(8,8,2,240.00,'2026-06-23 18:02:35','cancelled',1),(9,8,2,500.00,'2026-06-23 18:10:25','cancelled',1),(10,8,2,500.00,'2026-06-23 18:10:40','cancelled',1),(11,7,2,60.00,'2026-06-23 18:46:18','outbid',1),(12,7,3,80.00,'2026-06-23 18:47:14','won',0),(13,5,3,6000.00,'2026-06-23 19:21:42','won',0),(14,6,2,50.00,'2026-06-23 19:31:32','outbid',1),(15,6,3,80.00,'2026-06-23 19:31:50','cancelled',1),(16,9,1,500.00,'2026-06-23 20:00:23','won',0),(17,10,3,2200.00,'2026-06-23 23:27:14','outbid',0),(18,10,3,2220.00,'2026-06-23 23:27:22','outbid',0),(19,10,3,2250.00,'2026-06-23 23:27:34','outbid',1),(20,10,4,3000.00,'2026-06-23 23:28:17','active',0);
/*!40000 ALTER TABLE `bid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bill`
--

DROP TABLE IF EXISTS `bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(30) NOT NULL COMMENT 'recharge/place_bid/bid_refund/cancel_refund/offshelf_refund/sold_income/sold_fee',
  `amount` decimal(12,2) NOT NULL COMMENT '正=收入,负=支出',
  `balance_after` decimal(12,2) NOT NULL COMMENT '变动后余额',
  `related_type` varchar(20) DEFAULT NULL COMMENT 'bid/goods/order/recharge',
  `related_id` bigint DEFAULT NULL,
  `memo` varchar(200) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户流水';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill`
--

LOCK TABLES `bill` WRITE;
/*!40000 ALTER TABLE `bill` DISABLE KEYS */;
INSERT INTO `bill` (`id`, `user_id`, `type`, `amount`, `balance_after`, `related_type`, `related_id`, `memo`, `created_at`) VALUES (1,3,'recharge',10.00,1130.00,'recharge',NULL,'充值','2026-06-23 19:19:21'),(2,1,'recharge',10.00,13500.00,'recharge',NULL,'充值','2026-06-23 19:21:10'),(3,3,'recharge',5000.00,6130.00,'recharge',NULL,'充值','2026-06-23 19:21:36'),(4,3,'place_bid',-6000.00,130.00,'bid',NULL,'首次出价: 6000.0 (商品: 5)','2026-06-23 19:21:42'),(8,1,'sold_income',5880.00,19380.00,'order',5,'卖出商品收入','2026-06-23 19:23:59'),(9,2,'place_bid',-50.00,1460.00,'bid',NULL,'首次出价: 50.0 (商品: 6)','2026-06-23 19:31:32'),(10,2,'bid_refund',50.00,1510.00,'bid',14,'出价退款 (商品: 6)','2026-06-23 19:31:50'),(11,3,'place_bid',-80.00,50.00,'bid',NULL,'首次出价: 80.0 (商品: 6)','2026-06-23 19:31:50'),(12,3,'bid_refund',80.00,130.00,'bid',15,'出价退款 (商品: 6)','2026-06-23 19:34:34'),(13,1,'place_bid',-500.00,18880.00,'bid',NULL,'首次出价: 500.0 (商品: 9)','2026-06-23 20:00:23'),(14,3,'sold_income',490.00,620.00,'order',9,'卖出商品收入','2026-06-23 20:00:46'),(15,3,'after_sale_refund',5880.00,6500.00,'after_sale',2,'售后退款 (订单: 7)','2026-06-23 20:30:35'),(16,1,'after_sale_payback',-5880.00,13000.00,'after_sale',2,'售后扣款 (订单: 7)','2026-06-23 20:30:35'),(19,1,'after_sale_refund',490.00,13490.00,'after_sale',6,'售后退款 (订单: 8)','2026-06-23 21:43:48'),(20,3,'after_sale_payback',-490.00,6010.00,'after_sale',6,'售后扣款 (订单: 8)','2026-06-23 21:43:48'),(21,3,'place_bid',-2200.00,3810.00,'bid',NULL,'首次出价: 2200.0 (商品: 10)','2026-06-23 23:27:14'),(22,3,'place_bid',-20.00,3790.00,'bid',NULL,'加价差额: 20.00 (商品: 10)','2026-06-23 23:27:22'),(23,3,'place_bid',-30.00,3760.00,'bid',NULL,'加价差额: 30.00 (商品: 10)','2026-06-23 23:27:34'),(24,3,'bid_refund',2250.00,6010.00,'bid',19,'出价退款 (商品: 10)','2026-06-23 23:28:17'),(25,4,'place_bid',-3000.00,0.00,'bid',NULL,'首次出价: 3000.0 (商品: 10)','2026-06-23 23:28:17');
/*!40000 ALTER TABLE `bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `type` varchar(50) NOT NULL COMMENT '数码/服饰/古董/书籍/其它',
  `seller_id` bigint NOT NULL COMMENT '卖家 user.id',
  `start_price` decimal(12,2) NOT NULL COMMENT '起拍价',
  `current_price` decimal(12,2) NOT NULL COMMENT '当前价（开拍后等于 start_price）',
  `min_increment` decimal(12,2) NOT NULL DEFAULT '10.00' COMMENT '最小加价幅度',
  `cover` varchar(500) DEFAULT NULL COMMENT '封面图 URL',
  `description` text,
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'pending/on_sale/sold/off_shelf/rejected',
  `reject_reason` varchar(500) DEFAULT NULL,
  `end_at` datetime DEFAULT NULL COMMENT '竞拍结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `current_bidder_id` bigint DEFAULT NULL COMMENT '褰撳墠鏈?珮鍑轰环浜',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拍卖商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods`
--

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
INSERT INTO `goods` (`id`, `title`, `type`, `seller_id`, `start_price`, `current_price`, `min_increment`, `cover`, `description`, `status`, `reject_reason`, `end_at`, `created_at`, `current_bidder_id`) VALUES (1,'索尼 A7M4 相机','数码',3,8000.00,8000.00,50.00,'https://picsum.photos/seed/g1/200/140',NULL,'pending',NULL,'2026-06-20 20:00:00','2026-06-12 09:22:59',NULL),(2,'宋代茶盏','古董',4,1500.00,1500.00,100.00,'https://picsum.photos/seed/g2/200/140',NULL,'off_shelf',NULL,'2026-06-18 20:00:00','2026-06-12 09:22:59',NULL),(3,'Nike Air Max','服饰',3,300.00,480.00,20.00,'https://picsum.photos/seed/g3/200/140',NULL,'sold',NULL,'2026-06-15 12:00:00','2026-06-12 09:22:59',NULL),(4,'iPhone 15 Pro','数码',4,6000.00,7100.00,50.00,'https://picsum.photos/seed/g4/200/140',NULL,'sold',NULL,'2026-06-13 21:00:00','2026-06-12 09:22:59',NULL),(5,'iphone 17','古董',1,5000.00,6000.00,20.00,'https://picsum.photos/seed/iphone17/400/300','17','sold',NULL,'2026-06-24 02:00:00','2026-06-23 16:23:58',3),(6,'test','数码',1,11.00,80.00,20.00,'','111','off_shelf',NULL,'2026-06-30 19:31:15','2026-06-23 16:26:02',3),(7,'1','数码',1,20.00,80.00,20.00,'','','sold',NULL,'2026-06-25 00:00:00','2026-06-23 17:13:37',3),(9,'Air pods','数码',3,60.00,500.00,20.00,'http://127.0.0.1:8080/uploads/202606/ad0d28ce125c4d08a531e904bb069a92.png','666','sold',NULL,'2026-06-24 00:00:00','2026-06-23 19:59:37',1),(10,'表','数码',1,2000.00,3000.00,20.00,'http://127.0.0.1:8080/uploads/202606/90f81ce09a3a4e4dab812543de5e6daa.png','1','on_sale',NULL,'2026-06-30 23:25:38','2026-06-23 23:24:48',4);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods_vector`
--

DROP TABLE IF EXISTS `goods_vector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_vector` (
  `goods_id` bigint NOT NULL,
  `embedding` json DEFAULT NULL COMMENT '向量嵌入',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods_vector`
--

LOCK TABLES `goods_vector` WRITE;
/*!40000 ALTER TABLE `goods_vector` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_vector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` bigint NOT NULL,
  `goods_title` varchar(200) DEFAULT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `amount` decimal(12,2) NOT NULL COMMENT '成交价',
  `status` varchar(20) NOT NULL DEFAULT 'paid' COMMENT 'paid/refunded/cancelled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '平台手续费',
  `after_sale_status` varchar(20) NOT NULL DEFAULT 'none' COMMENT 'none/pending/refunded/rejected',
  PRIMARY KEY (`id`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` (`id`, `goods_id`, `goods_title`, `buyer_id`, `seller_id`, `amount`, `status`, `created_at`, `fee`, `after_sale_status`) VALUES (1,4,'iPhone 15 Pro',1,4,7100.00,'paid','2026-06-12 09:22:59',142.00,'rejected'),(2,8,'bao',2,1,500.00,'cancelled','2026-06-23 17:17:38',0.00,'none'),(3,8,'bao',2,1,510.00,'cancelled','2026-06-23 17:18:54',0.00,'none'),(4,4,'iPhone 15 Pro',4,4,7100.00,'cancelled','2026-06-23 18:34:05',0.00,'none'),(5,3,'Nike Air Max',2,3,480.00,'paid','2026-06-23 18:34:05',9.60,'none'),(6,7,'1',3,1,80.00,'paid','2026-06-23 18:49:53',1.60,'none'),(7,5,'iphone 17',3,1,6000.00,'refunded','2026-06-23 19:23:59',120.00,'refunded'),(8,9,'Air pods',1,3,500.00,'refunded','2026-06-23 20:00:46',10.00,'refunded');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_admin`
--

DROP TABLE IF EXISTS `sys_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_admin`
--

LOCK TABLES `sys_admin` WRITE;
/*!40000 ALTER TABLE `sys_admin` DISABLE KEYS */;
INSERT INTO `sys_admin` (`id`, `username`, `password`, `nickname`, `created_at`) VALUES (1,'sysadmin','123456','超级管理员','2026-06-12 09:22:59');
/*!40000 ALTER TABLE `sys_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` int NOT NULL,
  `min_increment` decimal(12,2) NOT NULL DEFAULT '10.00',
  `fee_rate` decimal(5,4) NOT NULL DEFAULT '0.0200' COMMENT '手续费比例 0~1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` (`id`, `min_increment`, `fee_rate`) VALUES (1,20.00,0.0200);
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '登录密码（明文，演示用）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `role` varchar(20) NOT NULL DEFAULT '',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT 'active / banned',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='买家/卖家用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `role`, `balance`, `status`, `created_at`) VALUES (1,'user01','123456','小明','',13490.00,'active','2026-06-12 09:22:59'),(2,'user02','123456','小红','',1510.00,'active','2026-06-12 09:22:59'),(3,'user03','123456','老王','',6010.00,'active','2026-06-12 09:22:59'),(4,'user04','123456','阿强','',0.00,'banned','2026-06-12 09:22:59');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-30 13:57:44
