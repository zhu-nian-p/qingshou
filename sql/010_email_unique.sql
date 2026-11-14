-- ============================================================================
--  010：学校邮箱唯一索引（防止一邮箱多账号 / 重置密码串号）
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/010_email_unique.sql
--  注意：若库中已存在重复邮箱，ALTER 会失败，需先人工合并/清理重复账号。
-- ============================================================================

USE `campus_market`;

ALTER TABLE `user`
    DROP INDEX `idx_user_school_email`,
    ADD UNIQUE KEY `uk_user_school_email` (`school_email`);
