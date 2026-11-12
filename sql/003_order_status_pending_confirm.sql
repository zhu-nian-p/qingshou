-- ============================================================================
--  003：订单新增「待卖家确认」状态
-- ----------------------------------------------------------------------------
--  状态机：3=待卖家确认 -> 0=待面交 -> 1=已完成
--                              └-> 2=已取消
--  适用场景：已经用旧版 init.sql 建过库的环境。全新环境直接执行最新 init.sql。
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/003_order_status_pending_confirm.sql
-- ============================================================================

USE `campus_market`;

-- 1) 先去掉旧约束（旧约束只允许 0/1/2）
ALTER TABLE `order` DROP CHECK `chk_order_status`;

-- 2) 更新字段注释，写明 3=待卖家确认
ALTER TABLE `order`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
    COMMENT '订单状态：0=待面交，1=已完成，2=已取消，3=待卖家确认';

-- 3) 重新挂上允许 3 的检查约束
ALTER TABLE `order`
    ADD CONSTRAINT `chk_order_status` CHECK (`status` IN (0, 1, 2, 3));
