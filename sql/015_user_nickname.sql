-- ============================================================================
--  015：账号体系重构
--  user.username（登录用户名） -> user.nickname（昵称，仅用于前端展示）
--  登录改为「学号 + 密码」，昵称不再唯一、不参与登录
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/015_user_nickname.sql
-- ============================================================================

USE `campus_market`;

-- 1. 去掉用户名唯一索引
ALTER TABLE `user` DROP INDEX `uk_user_username`;

-- 2. 字段改名
ALTER TABLE `user`
    CHANGE COLUMN `username` `nickname` VARCHAR(50) NOT NULL
    COMMENT '昵称，用于前端展示；不参与登录，允许重复';

-- 3. 昵称只做普通索引，方便后台搜索
ALTER TABLE `user` ADD KEY `idx_user_nickname` (`nickname`);
