-- ============================================================================
--  013：举报表 report
--  举报商品 / 用户，管理员处理（下架商品或忽略）
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/013_report.sql
-- ============================================================================

USE `campus_market`;

CREATE TABLE IF NOT EXISTS `report` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `item_id`        BIGINT UNSIGNED NULL                    COMMENT '被举报商品ID（与 target_user_id 二选一）',
  `target_user_id` BIGINT UNSIGNED NULL                    COMMENT '被举报用户ID（与 item_id 二选一）',
  `reporter_id`    BIGINT UNSIGNED NOT NULL                COMMENT '举报人用户ID',
  `reason`         VARCHAR(500)    NOT NULL                COMMENT '举报原因/描述',
  `status`         TINYINT         NOT NULL DEFAULT 0      COMMENT '0=待处理，1=已处理',
  `handler_id`     BIGINT UNSIGNED NULL                    COMMENT '处理的管理员ID',
  `handle_time`    DATETIME        NULL                    COMMENT '处理时间',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`, `create_time`),
  KEY `idx_report_item` (`item_id`),
  KEY `idx_report_reporter` (`reporter_id`),
  CONSTRAINT `fk_report_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_user` FOREIGN KEY (`target_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_report_status` CHECK (`status` IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '举报表：用户举报商品/用户，管理员处理';
