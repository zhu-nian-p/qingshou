-- ============================================================================
--  校园二手交易平台 —— 数据库初始化脚本
--  目标库：campus_market
--  适用版本：MySQL 8.0.16+（依赖 CHECK 约束与 utf8mb4_0900_ai_ci 排序规则）
-- ----------------------------------------------------------------------------
--  执行方式：
--    1) docker compose 首次启动时，由 /docker-entrypoint-initdb.d/01-init.sql 自动执行；
--    2) 手动导入：mysql -h127.0.0.1 -P3306 -uroot -p123456 < init.sql
--  本脚本可重复执行（建库建表均带 IF NOT EXISTS），不会破坏已有数据。
-- ----------------------------------------------------------------------------
--  表关系概览：
--    user  1 --- N  item        （seller_id：卖家发布的商品）
--    user  1 --- N  order       （buyer_id 买家 / seller_id 卖家）
--    item  1 --- N  order       （item_id：一件商品对应一笔成交订单）
--    order 1 --- N  review      （order_id：买卖双方针对同一订单互评）
--    user  1 --- N  review      （reviewer_id 评价人 / target_user_id 被评价人）
--  约定：所有金额为 DECIMAL，所有时间统一东八区，状态字段统一 TINYINT 小整数。
-- ============================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS `campus_market`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `campus_market`;


-- ============================================================================
-- 1. 用户表 user
--    买家与卖家共用一张表，通过 order 表中的角色字段区分。
-- ============================================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键，自增',
  `username`      VARCHAR(50)     NOT NULL                COMMENT '登录用户名，全局唯一，建议 4-20 位字母/数字/下划线',
  `password`      VARCHAR(100)    NOT NULL                COMMENT '密码密文，使用 BCrypt 加密后入库（BCrypt 固定 60 字符，长度预留到 100 以便换算法）',
  `real_name`     VARCHAR(50)     NOT NULL                COMMENT '真实姓名，用于实名认证与线下面对面交易的双方核验',
  `student_id`    VARCHAR(20)     NOT NULL                COMMENT '学号，8-9 位纯数字，全局唯一，杜绝一人注册多个账号',
  `school_email`  VARCHAR(100)    DEFAULT NULL            COMMENT '校园邮箱，可用于学生身份验证、找回密码、交易通知',
  `status`        TINYINT         NOT NULL DEFAULT 0      COMMENT '账号状态：0=正常，1=已封禁（封禁后禁止登录、发布商品与下单）',
  `role`          TINYINT         NOT NULL DEFAULT 1      COMMENT '角色：1=普通用户，2=管理员（管理员才能置顶商品、封禁用户）',
  `credit_score`  INT             NOT NULL DEFAULT 100    COMMENT '信用分，初始 100：交易完成加分，爽约/违规扣分，低于阈值可限制交易',
  `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间，任何字段被修改时自动刷新',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username`   (`username`)    COMMENT '用户名唯一索引：登录账号不可重复',
  UNIQUE KEY `uk_user_student_id` (`student_id`)  COMMENT '学号唯一索引：同一学号只能注册一次',
  UNIQUE KEY `uk_user_school_email` (`school_email`) COMMENT '学校邮箱唯一索引：防止同一邮箱注册多个账号、重置密码串号',
  KEY `idx_user_status`           (`status`)       COMMENT '后台按状态筛选（如列出封禁用户）',
  CONSTRAINT `chk_user_student_id` CHECK (REGEXP_LIKE(`student_id`, '^[0-9]{8,9}$')),
  CONSTRAINT `chk_user_status`     CHECK (`status` IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表：平台注册用户（买家/卖家同表）';


-- ============================================================================
-- 2. 商品表 item
--    一件商品同一时刻只应该有一笔「待面交」订单，该约束由业务层加锁保证
--    （MySQL 不支持带 WHERE 的部分唯一索引，故不在库层用 UNIQUE 表达）。
-- ============================================================================
CREATE TABLE IF NOT EXISTS `item` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID，主键，自增',
  `seller_id`        BIGINT UNSIGNED NOT NULL                COMMENT '卖家用户ID，关联 user.id，标识商品归属',
  `title`            VARCHAR(100)    NOT NULL                COMMENT '商品标题，列表页展示，建议 10-30 字',
  `description`      TEXT            DEFAULT NULL            COMMENT '商品详细描述：成色、入手渠道、使用时长、瑕疵等长文本',
  `price`            DECIMAL(10,2)   NOT NULL DEFAULT 0.00   COMMENT '售价（单位：元），DECIMAL 保存两位小数，避免浮点精度误差',
  `image_url`        VARCHAR(500)    DEFAULT NULL            COMMENT '商品主图 URL；如需多图，可另建 item_image 表或在此存 JSON 数组',
  `category`         VARCHAR(32)     NOT NULL DEFAULT '其他' COMMENT '商品分类：如 数码、书籍、生活用品、运动户外、其他（前端固定枚举）',
  `campus`           VARCHAR(32)     NOT NULL DEFAULT '前湖' COMMENT '所属校区：上海路 / 前湖',
  `contact_info`     VARCHAR(100)    DEFAULT NULL            COMMENT '卖家联系方式（微信号 / QQ 号等），纯文本，不做格式校验',
  `status`           TINYINT         NOT NULL DEFAULT 0      COMMENT '商品状态：0=在售，1=已预订，2=已售出，3=违规下架',
  `is_top`           TINYINT         NOT NULL DEFAULT 0      COMMENT '是否置顶：0=普通，1=置顶（列表排序优先）',
  `top_expire_time`  DATETIME        DEFAULT NULL            COMMENT '置顶到期时间；到期后定时任务将其改回 is_top=0，NULL 表示从未置顶',
  `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_seller`          (`seller_id`)                  COMMENT '查询某卖家发布的所有商品',
  KEY `idx_item_category_status` (`category`, `status`)         COMMENT '按分类筛选在售商品（首页分类入口）',
  KEY `idx_item_campus_status`   (`campus`, `status`)           COMMENT '按校区筛选在售商品（首页校区 Tab）',
  KEY `idx_item_status_create`   (`status`, `create_time`)      COMMENT '在售商品按发布时间倒序分页',
  KEY `idx_item_top`             (`is_top`, `top_expire_time`)  COMMENT '定时任务扫描已过期的置顶商品',
  CONSTRAINT `fk_item_seller`  FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_item_status` CHECK (`status` IN (0, 1, 2, 3)),
  CONSTRAINT `chk_item_is_top` CHECK (`is_top` IN (0, 1)),
  CONSTRAINT `chk_item_price`  CHECK (`price` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '商品表：校园二手交易的商品信息';


-- ============================================================================
-- 3. 订单表 order
--    注意：order 是 MySQL 保留字，建表与查询时必须使用反引号 `order`。
-- ============================================================================
CREATE TABLE IF NOT EXISTS `order` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键，自增',
  `item_id`        BIGINT UNSIGNED NOT NULL                COMMENT '商品ID，关联 item.id',
  `buyer_id`       BIGINT UNSIGNED NOT NULL                COMMENT '买家用户ID，关联 user.id',
  `seller_id`      BIGINT UNSIGNED NOT NULL                COMMENT '卖家用户ID，关联 user.id；冗余存储，避免查订单时再联表 item',
  `trade_location` VARCHAR(200)    NOT NULL                COMMENT '线下交易地点，纯文本（如「三教门口」「图书馆一楼大厅」），不做经纬度地理编码',
  `status`         TINYINT         NOT NULL DEFAULT 0      COMMENT '订单状态：0=待面交，1=已完成，2=已取消，3=待卖家确认，4=买家已确认面交、待卖家确认完成',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间（状态变更时间可据此推断）',
  PRIMARY KEY (`id`),
  KEY `idx_order_item`          (`item_id`)                   COMMENT '按商品查订单，用于判断商品是否已被下单',
  KEY `idx_order_buyer_status`  (`buyer_id`, `status`)        COMMENT '买家「我的订单」按状态筛选',
  KEY `idx_order_seller_status` (`seller_id`, `status`)       COMMENT '卖家「我卖出的」按状态筛选',
  KEY `idx_order_create_time`   (`create_time`)               COMMENT '后台按时间范围统计订单',
  CONSTRAINT `fk_order_item`   FOREIGN KEY (`item_id`)   REFERENCES `item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_buyer`  FOREIGN KEY (`buyer_id`)  REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_order_status` CHECK (`status` IN (0, 1, 2, 3, 4))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '订单表：面交交易订单（表名为保留字，SQL 中需写作 `order`）';


-- ============================================================================
-- 4. 评价表 review
--    一笔订单最多产生两条评价：买家评卖家、卖家评买家各一条。
-- ============================================================================
CREATE TABLE IF NOT EXISTS `review` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID，主键，自增',
  `order_id`       BIGINT UNSIGNED NOT NULL                COMMENT '关联订单ID，评价必须挂在真实成交订单上',
  `reviewer_id`    BIGINT UNSIGNED NOT NULL                COMMENT '评价人用户ID，关联 user.id',
  `target_user_id` BIGINT UNSIGNED NOT NULL                COMMENT '被评价人用户ID，关联 user.id；用于汇总个人信用与口碑',
  `rating`         TINYINT         NOT NULL                COMMENT '评分：1~5 星（1 星最差，5 星最好），参与信用分计算',
  `content`        VARCHAR(500)    DEFAULT NULL            COMMENT '评价内容，纯文本，建议限制 500 字以内避免刷屏',
  `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_order_reviewer` (`order_id`, `reviewer_id`) COMMENT '同一订单同一评价人只能评价一次，防重复提交',
  KEY `idx_review_target_user` (`target_user_id`, `create_time`)    COMMENT '按被评价人查询其收到的评价（个人主页口碑列表）',
  KEY `idx_review_reviewer`    (`reviewer_id`)                      COMMENT '按评价人查询其发出的评价',
  CONSTRAINT `fk_review_order`  FOREIGN KEY (`order_id`)       REFERENCES `order` (`id`) ON DELETE CASCADE  ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`)  REFERENCES `user` (`id`)  ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_target`   FOREIGN KEY (`target_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_review_rating`  CHECK (`rating` BETWEEN 1 AND 5),
  CONSTRAINT `chk_review_not_self` CHECK (`reviewer_id` <> `target_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '评价表：订单完成后的买卖双方互评';


-- ============================================================================
-- 5. 置顶申请表 top_up_apply
--    阶梯定价：1 天 1 元 / 7 天 5 元 / 30 天 12 元；金额由后端计算。
--    用户提交申请 -> 管理员人工审核（扫码付款后）-> 通过后商品置顶。
-- ============================================================================
CREATE TABLE IF NOT EXISTS `top_up_apply` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '置顶申请ID，主键，自增',
  `item_id`     BIGINT UNSIGNED NOT NULL                COMMENT '要置顶的商品ID，关联 item.id',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '申请人（必须是商品所有者），关联 user.id',
  `days`        INT             NOT NULL                COMMENT '置顶天数，只允许 1 / 7 / 30',
  `amount`      DECIMAL(10,2)   NOT NULL                COMMENT '应付金额（元），由后端按阶梯价计算，不信任前端',
  `status`      TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0=待审核，1=已通过（已置顶），2=已拒绝',
  `reject_reason` VARCHAR(255)  NULL                    COMMENT '拒绝原因（仅 status=2 时有值，管理员填写）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_topup_apply_item`   (`item_id`)                  COMMENT '按商品查申请',
  KEY `idx_topup_apply_user`   (`user_id`, `create_time`)   COMMENT '我的置顶申请',
  KEY `idx_topup_apply_status` (`status`, `create_time`)    COMMENT '管理员待审核列表',
  CONSTRAINT `fk_topup_apply_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE  ON UPDATE RESTRICT,
  CONSTRAINT `fk_topup_apply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_topup_apply_status` CHECK (`status` IN (0, 1, 2)),
  CONSTRAINT `chk_topup_apply_days`   CHECK (`days` IN (1, 7, 30)),
  CONSTRAINT `chk_topup_apply_amount` CHECK (`amount` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '置顶申请表：阶梯定价申请 -> 管理员审核 -> 商品置顶';


-- ============================================================================
-- 6. 上传记录表 file_upload
--    用于限制每个用户最多上传 50 张图片，防止刷接口把服务器磁盘打满。
-- ============================================================================
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

-- ============================================================================
-- 7. 建表结果自检：列出本库中已创建的表及注释
-- ============================================================================
SELECT TABLE_NAME AS `已创建表`, TABLE_COMMENT AS `说明`
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'campus_market'
ORDER BY TABLE_NAME;
