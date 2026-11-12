-- ============================================================================
--  004：用户表新增 role 字段（0=普通用户，1=管理员），并创建开发用管理员账号
-- ----------------------------------------------------------------------------
--  适用场景：已经用旧版 init.sql 建过库的环境。全新环境直接执行最新 init.sql。
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/004_user_role.sql
-- ============================================================================

USE `campus_market`;

-- 1) 新增角色字段（1=普通用户，2=管理员）
ALTER TABLE `user`
    ADD COLUMN `role` TINYINT NOT NULL DEFAULT 1
    COMMENT '角色：1=普通用户，2=管理员（管理员才能置顶商品、封禁用户）'
    AFTER `status`;

-- 2) 开发环境管理员账号：admin01 / 123456
--    密文是 BCrypt("123456")，与测试账号 alice01 一致。生产环境请删掉这段或换成强密码。
INSERT INTO `user` (`username`, `password`, `real_name`, `student_id`, `school_email`, `status`, `credit_score`, `role`)
SELECT 'admin01',
       '$2a$10$pfaEnzXknUxLwFtMK5s9fOBH1bsdIx2tMIy6qR5qS8PrCSTAEoz2',
       '管理员',
       '20240000',
       'admin@campus.edu',
       0,
       100,
       2
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin01');
