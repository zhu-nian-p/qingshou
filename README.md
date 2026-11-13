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
├── frontend/
│   ├── login.html               登录 / 注册（学校邮箱验证码，60 秒倒计时）
│   ├── index.html               主应用（响应式：底部导航 / 桌面侧边栏）
│   ├── publish.html             发布闲置（图片上传）
│   ├── detail.html              商品详情（我想要 / 复制联系方式 / 直接下单）
│   └── admin.html               管理后台（置顶审核 / 用户封禁）
├── sql/                         增量迁移脚本（002 ~ 009）
├── docs/                        文档与截图
├── init.sql                     全量建库建表脚本
├── docker-compose.yml           MySQL + Redis
├── start-backend.cmd / .ps1     启动后端
├── stop-backend.cmd / .ps1      停止后端（释放 8080）
├── start-frontend.cmd           启动前端静态服务（5173）
├── build-backend.cmd            打包后端 jar
├── .env.local                   本地密钥（已 gitignore，不进版本库）
└── .tools/                      便携 JDK17 + Maven（已 gitignore）
```

## 快速启动（每次开发的标准步骤）

1. **启动 Docker Desktop**，等状态变成 Running
2. 在项目根目录启动依赖容器：
   ```powershell
   cd D:\campus-market
   docker compose up -d
   docker compose ps        # mysql / redis 两个容器都应为 healthy
   ```
   > 首次初始化会自动执行 `init.sql`；已有数据库升级时，按顺序执行 `sql/002 ~ 009` 的增量脚本。
3. **启动后端**：双击 `start-backend.cmd`
   - 成功标志：日志出现 `Tomcat started on port 8080` 和 `Started CampusMarketApplication`
   - 后端地址：http://localhost:8080
4. **启动前端**：双击 `start-frontend.cmd`（自动打开 http://localhost:5173/index.html）
   - 未登录会自动跳转到 `login.html`
   - 也可直接访问各页面（见下方「页面地址」）
5. **停止**：双击 `stop-backend.cmd` 停止后端（释放 8080），关闭前端命令行窗口释放 5173

> 后端日志在 `logs\startup-*.log`，启动失败时把最后 30 行发出来即可定位。
> `.env.local` 保存本机密钥（DeepSeek Key / 邮箱授权码），启动脚本会自动加载，不会进 git。

## 测试账号

| 角色 | 账号 | 密码 | 说明 |
|---|---|---|---|
| 卖家 | alice01 | 123456 | 已发布 4 件商品，信用分 120（演示高信誉标签） |
| 买家 | buyer01 | 123456 | 用于下单 / 完成交易 |
| 买家 | buyer02 | 123456 | 用于测并发/抢单 |
| 管理员 | admin01 | 123456 | role=2，唯一管理员；可置顶审核、封禁用户 |

> 角色字段 `user.role`：**1=普通用户，2=管理员**。只有 admin01 能进入管理后台。
> 注册新账号需要 `.edu.cn` 学校邮箱 + 邮箱验证码。

## 页面地址

| 页面 | 地址 | 说明 |
|---|---|---|
| 登录 / 注册 | http://localhost:5173/login.html | 未登录访问其他页面会自动跳这里 |
| 首页 | http://localhost:5173/index.html | 搜索、校区筛选、无限滚动 |
| 发布闲置 | http://localhost:5173/publish.html | 需登录 |
| 商品详情 | http://localhost:5173/detail.html?id=3 | `id` 为商品 ID |
| 管理后台 | http://localhost:5173/admin.html | 仅 admin01 可进 |

## 数据状态

仓库默认数据库为干净的演示数据：4 件在售商品（item 3 置顶中）、4 个测试账号、0 订单、0 置顶申请、0 评价。
所有业务流程（注册 → 发布 → 浏览 → 下单 → 卖家确认 → 面交完成/拒绝 → 评价 → 置顶申请 → 管理员审核）均可直接用上述账号跑通。

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
topup:                            # 阶梯定价：1天1元 / 7天5元 / 30天12元（后端计算，不信任前端金额）
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
| 商品 | `POST /api/item`、`GET /api/item/list?page=1&size=10&keyword=&category=&campus=&sort=`（返回 records + total）、`GET /api/item/{id}` |
| 订单 | `POST /api/order`、`POST /api/order/{id}/seller-confirm`、`POST /api/order/{id}/complete`、`POST /api/order/{id}/seller-reject`、`GET /api/order/buyer`、`GET /api/order/seller` |
| 评价 | `POST /api/review`、`GET /api/review/user/{userId}` |
| 上传 | `POST /api/upload`（form-data，字段 `file`） |
| AI | `POST /api/ai/generate-desc` |
| 置顶申请 | `POST /api/topup/apply`（body: `itemId` + `days`，金额后端按 1/7/30 天阶梯计算）、`GET /api/topup/my`（含 `rejectReason`） |
| 管理 | `POST /api/admin/items/top`、`POST /api/admin/items/{id}/untop`、`POST /api/admin/users/{id}/ban`、`POST /api/admin/users/{id}/unban`、`GET /api/admin/topup/pending`、`POST /api/admin/topup/{id}/approve`、`POST /api/admin/topup/{id}/reject`（body 必填 `reason`） |

所有接口统一返回 `{ "code": 200, "message": "...", "data": ... }`。

## 前端页面

| 页面 | 说明 |
|---|---|
| `login.html` | 登录 / 注册 Tab；注册含用户名、密码、真实姓名、学号、学校邮箱、验证码；「获取验证码」60 秒倒计时；成功后 token 存 localStorage 并跳转主应用 |
| `index.html` | 主应用，响应式：手机底部导航 + 悬浮发布，桌面左侧固定边栏；主页搜索 + 校区 Tab（全部/上海路/前湖）+ **滑到底部无限滚动**（触底加载下一页、加载中动画、「没有更多啦」提示、切 Tab/搜索自动重置）；商品卡片 2 列 / 4 列自适应，🔥 急售、🏅 高信誉标签；消息（模拟）；我的（资料 / 信用分 / 我发布的 / 我买到的 / 我卖出的 / 退出登录）；订单支持买家「确认完成交易」、卖家「同意面交 / 拒绝接单」；**点击卡片跳转 `detail.html?id=`** |
| `publish.html` | 发布闲置：标题、描述、价格、分类、校区、联系方式 + 图片上传（调 `/api/upload`，回显预览）；提交带 token 调 `/api/item`，成功后弹窗并返回首页 |
| `detail.html` | 商品详情：大图、标题、价格、描述、卖家信息（🏅 高信誉）；底部「我想要」按钮，登录后弹窗展示联系方式；复制按钮按内容动态显示「复制QQ号 / 复制微信号 / 一键复制」；支持弹窗内「直接下单」填写交易地点创建订单 |
| `admin.html` | 管理员后台（仅 admin01）：Tab 切换「置顶审核」（el-table 待审核申请：商品/申请人/天数/金额/时间，确认收款并置顶 / 拒绝）与「用户封禁」（el-table 全部用户：角色/信用分/状态 el-tag，封禁 / 解封） |

前端通过 `http://localhost:8080` 直连后端，CORS 已全开放。建议用 `start-frontend.cmd` 起静态服务（`python -m http.server 5173`），不要直接双击 HTML。

## 学校邮箱验证码注册

1. `POST /api/user/send-code`，body `{"email":"学号@xxx.edu.cn"}`：
   - 仅接受 `.edu.cn` 结尾邮箱
   - 同一邮箱 1 分钟内只能发一次（Redis 限流，超限 429）
   - 6 位随机验证码，Redis 存 5 分钟
   - 由 QQ SMTP 发送，标题「【青售】您的注册验证码」，发件人「青售官方团队」
2. `POST /api/user/register` 时带上 `schoolEmail` 和 `emailCode`：
   - 校验通过后立即删除 Redis 中的验证码（一次性使用）
   - 错误或过期返回 400「验证码错误或已过期」

## 后续优化路线图（按优先级）

1. **安全风控**：CORS 收敛到正式域名；接口按角色做方法级鉴权（Spring Security + `@PreAuthorize`）；登录/发码加 IP 维度限流与失败锁定；敏感操作（封禁、审核）写审计日志。
2. **性能与架构**：商品列表接入 Elasticsearch 或 MySQL 全文索引替代 `LIKE %keyword%`；图片迁移到对象存储（OSS/MinIO）+ CDN；热点列表加 Redis 缓存与缓存击穿保护；订单超时清理改为延迟队列（如 RocketMQ / Redis ZSet）。
3. **交易与资金安全**：置顶收款目前是「扫码 + 人工审核」，上线应接微信/支付宝企业支付并校验回调签名；订单引入支付流水表、幂等键与对账任务；平台抽佣与结算账本。
4. **用户体验**：前端从单文件拆成 Vite + Vue 工程（组件化、路由、状态管理）；商品详情补 SKU/多图轮播；消息模块接实时通知（WebSocket）；骨架屏与图片懒加载。
5. **运营与商业变现**：增加曝光位/推荐流竞价、卖家会员套餐、交易服务费；数据看板（DAU、成交额、置顶转化率）；优惠券与首单立减。
6. **可观测性**：接入 Prometheus + Grafana 监控 QPS/延迟/错误率；结构化日志 + TraceId；慢 SQL 告警；前端接入 Sentry。
7. **工程质量**：补 JUnit + MockMvc 接口测试与前端 E2E（Playwright）；CI 流水线（构建、测试、镜像推送）；数据库迁移工具（Flyway/Liquibase）替代手写 sql。
8. **合规与治理**：实名/学籍校验、内容审核（敏感词 + 图片鉴黄）、隐私政策与数据留存策略、未成年人保护、投诉举报与信用惩戒规则。
