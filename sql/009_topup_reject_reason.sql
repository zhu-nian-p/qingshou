-- ============================================================================
--  009：置顶申请增加拒绝原因 reject_reason
--  管理员拒绝申请时必填原因，用户可在「我发布的」看到未生效的原因。
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/009_topup_reject_reason.sql
-- ============================================================================

USE `campus_market`;

ALTER TABLE `top_up_apply`
    ADD COLUMN `reject_reason` VARCHAR(255) NULL
    COMMENT '拒绝原因（仅 status=2 时有值，管理员填写）'
    AFTER `status`;
