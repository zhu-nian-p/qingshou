-- ============================================================================
--  002：学号规则由「10 位」调整为「8-9 位纯数字」
-- ----------------------------------------------------------------------------
--  适用场景：已经用旧版 init.sql 建过库的环境（本次线上库就是这种）。
--  全新环境直接执行最新的 init.sql 即可，不需要跑本文件。
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/002_student_id_8_9.sql
-- ============================================================================

USE `campus_market`;

-- 1) 先移除旧的检查约束，否则它会把 8/9 位学号写库直接拒掉
ALTER TABLE `user` DROP CHECK `chk_user_student_id`;

-- 2) 修正历史测试数据（10 位 -> 9 位）。只动明确的测试账号，避免误伤真实数据
UPDATE `user`
SET `student_id` = '202400001'
WHERE `username` = 'alice01' AND `student_id` = '2024000001';

-- 3) 列类型放宽：CHAR(10) -> VARCHAR(20)，保留 NOT NULL 与唯一索引 uk_user_student_id
ALTER TABLE `user`
    MODIFY COLUMN `student_id` VARCHAR(20) NOT NULL
    COMMENT '学号，8-9 位纯数字，全局唯一，杜绝一人注册多个账号';

-- 4) 重新挂上按新规则校验的检查约束
ALTER TABLE `user`
    ADD CONSTRAINT `chk_user_student_id` CHECK (REGEXP_LIKE(`student_id`, '^[0-9]{8,9}$'));
