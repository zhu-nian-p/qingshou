-- ============================================================================
--  011：订单新增状态 4（买家已确认面交，待卖家确认完成）
--  双向确认：0(待面交) -> 4(待卖家确认完成) -> 1(已完成)
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/011_order_status_4.sql
-- ============================================================================

USE `campus_market`;

ALTER TABLE `order` DROP CHECK `chk_order_status`;
ALTER TABLE `order`
    ADD CONSTRAINT `chk_order_status` CHECK (`status` IN (0, 1, 2, 3, 4));

ALTER TABLE `order`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
    COMMENT '订单状态：0=待面交，1=已完成，2=已取消，3=待卖家确认，4=买家已确认面交、待卖家确认完成';
