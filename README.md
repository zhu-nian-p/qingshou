# 青售 · 校园二手交易平台

一个面向校园的二手交易平台：Spring Boot 3 + MySQL 8 + Redis，前端为单文件 Vue3 + Element Plus。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5.16、MyBatis-Plus 3.5.18、JWT（jjwt 0.12）、BCrypt |
| 数据库 | MySQL 8.0（Docker）、Redis 7（Docker） |
| 邮件 | QQ 邮箱 SMTP（smtp.qq.com:465 + SSL），用于学校邮箱验证码注册 |
| 前端 | Vue 3 + Element Plus（CDN 引入，单文件 `frontend/index.html`） |
| AI | DeepSeek Chat Completions（可选，未配置 Key 时返回模拟文案） |

## 目录结构

```
campus-market/
├── backend/                     Spring Boot 后端
│   ├── src/main/java/com/campus/market/
│   │   ├── user/                用户：注册、登录、个人中心
│   │   ├── item/                商品：发布、列表搜索、详情
│   │   ├── order/               订单 + 超时取消定时任务
│   │   ├── review/              评价（买家评卖家，联动信用分）
│   │   ├── topup/               置顶购买（申请/审核）
│   │   ├── admin/               管理员：置顶、封禁
│   │   ├── ai/                  DeepSeek 文案生成
│   │   ├── file/                图片上传
│   │   ├── security/            JWT 拦截器、上下文
│   │   └── common/              统一返回、异常处理、敏感词
│   └── src/main/resources/application.yml
├── frontend/index.html          商品首页（单文件）
├── sql/                         增量迁移脚本（002 ~ 006）
├── docs/                        文档与截图
├── init.sql                     全量建库建表脚本
├── docker-compose.yml           MySQL + Redis
├── start-backend.cmd / .ps1     启动后端
├── stop-backend.cmd / .ps1      停止后端（释放 8080）
├── start-frontend.cmd           启动前端静态服务（5173）
├── .env.local                   本地密钥（已 gitignore，不进版本库）
└── .tools/                      便携 JDK17 + Maven（已 gitignore）
```

## 快速启动（每次开发的标准步骤）

1. **启动 Docker Desktop**，等状态变成 Running
2. 确认容器已起（首次或重启电脑后）：
   ```powershell
   cd D:\campus-market
   docker compose up -d
   docker compose ps        # 两个容器都应为 healthy
   ```
3. **启动后端**：双击 `start-backend.cmd`
   看到 `Tomcat started on port 8080` 和 `Started CampusMarketApplication` 即成功
4. **启动前端**：双击 `start-frontend.cmd`（自动打开 http://localhost:5173/index.html）
5. 关闭时：先关闭前端窗口，再双击 `stop-backend.cmd` 停止后端

> 后端日志在 `logs\startup-*.log`，启动失败时把最后 30 行发出来即可定位。

## 测试账号

| 角色 | 账号 | 密码 | 说明 |
|---|---|---|---|
| 卖家 | alice01 | 123456 | 已发布多件商品 |
| 买家 | buyer01 | 123456 | |
| 买家 | buyer02 | 123456 | 测并发/抢单 |
| 管理员 | admin01 | 123456 | role=2，可置顶、封禁 |

> 角色字段 `user.role`：**1=普通用户，2=管理员**。

## 关键配置

`backend/src/main/resources/application.yml`：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: ${MAIL_USERNAME:}      # 发件邮箱
    password: ${MAIL_AUTH_CODE:}     # QQ 邮箱 SMTP 授权码（不是登录密码）

deepseek:
  api-key: ${DEEPSEEK_API_KEY:}   # 从环境变量读取，启动脚本会加载 .env.local
topup:
  price-per-day: 1.00             # 置顶单价（元/天）
order:
  timeout-hours: 24               # 待卖家确认超过 24 小时自动取消
  timeout-cron: "0 */10 * * * *"  # 每 10 分钟扫描一次
```

`.env.local`（项目根目录，本机私有，已在 .gitignore）：
```
DEEPSEEK_API_KEY=sk-xxxx
MAIL_USERNAME=你的QQ邮箱@qq.com
MAIL_AUTH_CODE=你的SMTP授权码
```

## 接口速查

| 模块 | 接口 |
|---|---|
| 用户 | `POST /api/user/send-code`（学校邮箱验证码）、`POST /api/user/register`（需 `emailCode`）、`POST /api/user/login`、`GET /api/user/me` |
| 个人中心 | `GET /api/user/me/items`、`GET /api/user/me/trades` |
| 商品 | `POST /api/item`、`GET /api/item/list`、`GET /api/item/{id}` |
| 订单 | `POST /api/order`、`POST /api/order/{id}/seller-confirm`、`POST /api/order/{id}/complete`、`POST /api/order/{id}/seller-reject`、`GET /api/order/buyer`、`GET /api/order/seller` |
| 评价 | `POST /api/review`、`GET /api/review/user/{userId}` |
| 上传 | `POST /api/upload`（form-data，字段 `file`） |
| AI | `POST /api/ai/generate-desc` |
| 置顶购买 | `POST /api/topup/apply`、`GET /api/topup/my` |
| 管理 | `POST /api/admin/items/top`、`POST /api/admin/items/{id}/untop`、`POST /api/admin/users/{id}/ban`、`POST /api/admin/users/{id}/unban`、`POST /api/admin/topup/{id}/approve`、`POST /api/admin/topup/{id}/reject` |

所有接口统一返回 `{ "code": 200, "message": "...", "data": ... }`。

## 学校邮箱验证码注册

1. `POST /api/user/send-code`，body `{"email":"学号@xxx.edu.cn"}`：
   - 仅接受 `.edu.cn` 结尾邮箱
   - 同一邮箱 1 分钟内只能发一次（Redis 限流，超限 429）
   - 6 位随机验证码，Redis 存 5 分钟
   - 由 QQ SMTP 发送，标题「【青售】您的注册验证码」，发件人「青售官方团队」
2. `POST /api/user/register` 时带上 `schoolEmail` 和 `emailCode`：
   - 校验通过后立即删除 Redis 中的验证码（一次性使用）
   - 错误或过期返回 400「验证码错误或已过期」
