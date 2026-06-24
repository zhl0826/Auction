# 后端启动

## 1. 建库
见 \../db/init.sql\

## 2. 修改密码
打开 \src/main/resources/application.yml\，把 \spring.datasource.password\ 改成你自己的 MySQL 密码。

## 3. 启动
用 IDE（IntelliJ IDEA）打开本目录，右键 \AuctionAdminApplication\ 运行；
或者命令行：
\\\
mvn spring-boot:run
\\\

启动后访问 \http://localhost:8080/api/sys/users\ 应返回 JSON。
