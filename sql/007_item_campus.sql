-- ============================================================================
--  007：商品新增 campus（校区）字段，用于首页 [全部] [上海路] [前湖] 筛选
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/007_item_campus.sql
-- ============================================================================

USE `campus_market`;

ALTER TABLE `item`
    ADD COLUMN `campus` VARCHAR(32) NOT NULL DEFAULT '前湖'
    COMMENT '所属校区：上海路 / 前湖'
    AFTER `category`;

-- 已有商品默认归到「前湖」，方便首页筛选演示
UPDATE `item` SET `campus` = '前湖' WHERE `campus` IS NULL OR `campus` = '';

CREATE INDEX `idx_item_campus_status` ON `item` (`campus`, `status`);
