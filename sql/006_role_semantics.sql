-- ============================================================================
--  006：调整 role 语义：1=普通用户，2=管理员（原 0=普通，1=管理员）
-- ----------------------------------------------------------------------------
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/006_role_semantics.sql
-- ============================================================================

USE `campus_market`;

-- 1) 改默认值与注释
ALTER TABLE `user`
    MODIFY COLUMN `role` TINYINT NOT NULL DEFAULT 1
    COMMENT '角色：1=普通用户，2=管理员';

-- 2) 老数据迁移：原来的普通用户 0 -> 1
UPDATE `user` SET `role` = 1 WHERE `role` = 0;

-- 3) 原来的管理员 1 -> 2（先按用户名精确指定，避免误伤普通用户）
UPDATE `user` SET `role` = 2 WHERE `username` = 'admin01';

-- 4) 校验
SELECT CONCAT(username, ' | role=', role) AS result FROM `user` ORDER BY id;
