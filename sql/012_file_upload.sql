-- ============================================================================
--  012：图片上传记录表（每用户最多 50 张配额）
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/012_file_upload.sql
-- ============================================================================

USE `campus_market`;

CREATE TABLE IF NOT EXISTS `file_upload` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '上传记录ID',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '上传用户ID，关联 user.id',
  `url`         VARCHAR(500)    NOT NULL                COMMENT '访问地址（如 /uploads/xxx.png）',
  `filename`    VARCHAR(255)    NOT NULL                COMMENT '存储文件名',
  `size`        BIGINT          NOT NULL DEFAULT 0      COMMENT '文件大小（字节）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_file_upload_user` (`user_id`, `create_time`),
  CONSTRAINT `fk_file_upload_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '图片上传记录：用户上传配额统计';
