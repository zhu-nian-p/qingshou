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
│   ├── login.html               旧版单文件前端（保留作回退）
│   ├── index.html
│   ├── publish.html
│   ├── detail.html
│   └── admin.html
├── frontend-app/                新版 Vite + Vue3 工程（推荐）
│   ├── src/
│   │   ├── components/          组件：AppShell / ItemCard / 弹窗等
│   │   ├── views/               页面：Login / Home / Profile / Publish / Detail / Admin
│   │   ├── router/              Vue Router + 鉴权守卫
│   │   ├── stores/              Pinia（用户 Token / 资料）
│   │   ├── api/                 fetch 封装（token 注入、401 处理）
│   │   └── styles/              全局主题样式
│   ├── vite.config.js           dev 端口 5174 + /api、/uploads 代理
│   └── README.md                前端工程运行说明
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
4. **启动前端（二选一）**
   - **推荐：Vite 工程版**（工程化、组件化、Vue Router + Pinia）
     ```powershell
     cd D:\campus-market\frontend-app
     npm install        # 首次
     npm run dev        # http://localhost:5174
     ```
   - 旧版静态页：双击 `start-frontend.cmd`（http://localhost:5173/index.html）
5. **停止**：双击 `stop-backend.cmd` 停止后端（释放 8080），前端在命令行 Ctrl+C 停止（释放 5174 / 5173）

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

新版 Vite 工程（推荐，端口 5174）：

| 页面 | 地址 | 说明 |
|---|---|---|
| 登录 / 注册 | http://localhost:5174/login | 未登录访问其他页面会自动跳这里 |
| 首页 | http://localhost:5174/ | 搜索、校区筛选、骨架屏、无限滚动 |
| 消息 | http://localhost:5174/messages | 模拟通知 |
| 我的 | http://localhost:5174/profile | 我发布的 / 我买到的 / 我卖出的 |
| 发布闲置 | http://localhost:5174/publish | 需登录 |
| 商品详情 | http://localhost:5174/detail/3 | `3` 为商品 ID |
| 管理后台 | http://localhost:5174/admin | 仅 admin01 可进 |

旧版静态页（端口 5173）：`/login.html`、`/index.html`、`/publish.html`、`/detail.html?id=3`、`/admin.html`。

## 前端工程化说明（Vite + Vue3）

- **组件拆分**：`AppShell`（侧边栏/底部导航）、`ItemCard`、`SkeletonGrid`、`TopUpDialog`、`EditItemDialog`、`ReviewDialog`
- **路由**：Vue Router（`/login`、`/`、`/messages`、`/profile`、`/publish`、`/detail/:id`、`/admin`），带登录/管理员守卫
- **状态**：Pinia `useUserStore` 管理 token、用户资料、登录登出，并持久化到 localStorage
- **接口层**：`src/api/http.js` 统一注入 token，401 自动清理登录态并跳转登录页
- **开发代理**：Vite 把 `/api`、`/uploads` 代理到 `http://localhost:8080`，无需处理 CORS
- 详细命令与目录见 `frontend-app/README.md`

## 数据状态

仓库默认数据库为干净的演示数据：4 件在售商品（item 3 置顶中）、4 个测试账号、0 订单、0 置顶申请、0 评价。
所有业务流程（注册 → 发布 → 浏览 → 下单 → 卖家确认 → 面交完成/拒绝 → 评价 → 置顶申请 → 管理员审核）均可直接用上述账号跑通。

## Redis 商品列表缓存

首页 `/api/item/list` 已接入 Redis 缓存（2 核 4G 部署不引入 Elasticsearch，搜索仍走 MySQL）：

- **缓存 key**：`qingshou:item:list:{md5(版本号|keyword|category|campus|sort|page|size)}`
- **TTL**：正常结果 5 分钟 + 随机 0~60 秒（防雪崩）；空结果固定 60 秒（防穿透）
- **失效策略**：发布、编辑、下架、下单、完成交易、卖家拒单、超时取消、管理员置顶/取消置顶、置顶审核通过等写操作都会执行 `INCR qingshou:item:list:version`，所有旧 key 立即逻辑失效
- **降级**：Redis 异常时自动回退 MySQL 查询，接口不报错

实测结果：首次请求回源并生成缓存（TTL≈346s），二次请求命中；空结果缓存 TTL≈59s；编辑商品后版本号自增、列表立即返回新价格。

## WebSocket 订单实时通知

- **连接地址**：`ws://localhost:8080/ws?token={JWT}`（浏览器 WebSocket 无法带 Header，token 放查询参数）
- **握手鉴权**：校验 JWT、用户存在且未被封禁；无 token / 过期直接 401
- **会话管理**：按 userId 维护连接集合，同一账号多端在线都会收到；心跳 `ping/pong`
- **推送时机**：买家下单成功后，通过 `@TransactionalEventListener(AFTER_COMMIT)` 在事务提交后推送，订单回滚不会误报
- **消息体**：
  ```json
  {"type":"NEW_ORDER","title":"青售提示","message":"有新订单啦，请及时确认！","orderId":20,"itemId":3,"itemTitle":"iPad 第 9 代 64G"}
  ```
- **前端行为**：`frontend-app` 登录后自动连接，断线 5 秒重连，退出登录自动关闭；收到后 `ElNotification` 弹出「有新订单啦，请及时确认！」。旧版 `frontend/index.html` 也内联了同样的连接逻辑。
- **生产部署**：Nginx 需要转发 WebSocket 升级：
  ```nginx
  location /ws {
      proxy_pass http://127.0.0.1:8080;
      proxy_http_version 1.1;
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection "upgrade";
      proxy_read_timeout 3600s;
  }
  ```

> 实测：alice01 登录后建立连接（后端日志 `WebSocket 连接建立: userId=3`），buyer01 下单后前端收到并弹出「青售提示 / 有新订单啦，请及时确认！」。

## 接口限流 @RateLimit

自定义注解 + AOP + Redis 实现固定窗口限流，防止刷接口：

```java
@RateLimit(time = 60, count = 5)   // 60 秒内最多 5 次
@PostMapping
public Result<?> publish(...) { ... }
```

- **实现**：`RateLimitAspect` 用 Lua 脚本执行 `INCR` + 首次 `EXPIRE`（原子操作），key 为
  `qingshou:rate:{Controller}.{method}:{用户ID 或 IP}`
- **限流维度**：已登录按 `userId`，未登录按客户端 IP（支持 `X-Forwarded-For`）
- **超限响应**：HTTP/业务码 429，提示「手速太快啦，请稍后再试」
- **降级**：Redis 异常时放行（fail-open），避免缓存故障拖垮业务
- **已接入**：`POST /api/item`（发布商品）、`POST /api/user/send-code`（发送验证码），均为 60 秒 5 次

实测：连续第 6 次发布/发码均返回 `code=429, message=手速太快啦，请稍后再试`；Redis 中对应 key `count=6`、TTL 正常递减。

## 管理后台数据看板

`GET /api/admin/stats`（仅管理员）返回：

```json
{
  "totalUsers": 4,
  "todayNewUsers": 0,
  "onSaleItems": 4,
  "todayPublishedItems": 1,
  "completedAmount": 50.00,
  "trend": [{"date":"2026-11-08","label":"11-08","count":0}, "... 共 7 天"]
}
```

- `completedAmount`：已完成订单（status=1）关联 `item.price` 求和
- `trend`：近 7 天每天发布的商品数（含今天，日期升序）
- 前端 `admin` 页面顶部用 5 张卡片展示，数字通过 `AnimatedNumber` 组件做滚动动画（requestAnimationFrame + easeOutCubic）
- 数据看板下方用 ECharts 绘制近 7 天发布趋势折线图（渐变面积、平滑曲线、自适应 resize）
- Vite 版 `AdminView.vue` 与旧版 `frontend/admin.html` 均已接入

实测：今日发布 1 件、完成 1 笔 50 元订单后，接口返回 `todayPublishedItems=1`、`completedAmount=50.00`，趋势图中 11-12 为 4、11-14 为 1。

## AI 生成商品文案

发布页（Vite `PublishView.vue` 与旧版 `publish.html`）在「商品描述」右上角提供 **✨ AI帮我写文案** 按钮：

1. 取【商品标题】+【分类】拼成关键词（最多 50 字）
2. 调用 `POST /api/ai/generate-desc`（需登录）
3. 请求期间按钮显示「正在生成中...」并禁用，防止重复点击
4. 返回后自动填入描述框，并提示「已根据标题和分类生成文案」
5. 未配置 `DEEPSEEK_API_KEY` 时后端返回「模拟文案」，前端会提示 mock 状态
6. 标题为空时不会发请求，直接提示先填写标题

![AI 文案](<docs/screenshots/publish-ai.png>)

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
| 用户 | `POST /api/user/send-code`（学校邮箱验证码）、`POST /api/user/register`（需 `emailCode`）、`POST /api/user/login`、`POST /api/user/send-reset-code`（忘记密码发码）、`POST /api/user/reset-password`（验证码重置）、`GET /api/user/me` |
| 个人中心 | `GET /api/user/me/items`、`GET /api/user/me/trades` |
| 商品 | `POST /api/item`、`GET /api/item/list?page=1&size=10&keyword=&category=&campus=&sort=`（返回 records + total）、`GET /api/item/{id}`、`PUT /api/item/{id}`（卖家编辑）、`POST /api/item/{id}/offline`（卖家下架） |
| 订单 | `POST /api/order`、`POST /api/order/{id}/seller-confirm`、`POST /api/order/{id}/complete`、`POST /api/order/{id}/seller-reject`、`GET /api/order/buyer`、`GET /api/order/seller` |
| 评价 | `POST /api/review`（订单完成后买家评价）、`GET /api/review/user/{userId}`（匿名可看，详情页展示）、`GET /api/review/mine`（我发出的评价） |
| 上传 | `POST /api/upload`（form-data，字段 `file`） |
| AI | `POST /api/ai/generate-desc` |
| 置顶申请 | `POST /api/topup/apply`（body: `itemId` + `days`，金额后端按 1/7/30 天阶梯计算）、`GET /api/topup/my`（含 `rejectReason`） |
| 管理 | `GET /api/admin/stats`（数据看板）、`POST /api/admin/items/top`、`POST /api/admin/items/{id}/untop`、`POST /api/admin/users/{id}/ban`、`POST /api/admin/users/{id}/unban`、`GET /api/admin/topup/pending`、`POST /api/admin/topup/{id}/approve`、`POST /api/admin/topup/{id}/reject`（body 必填 `reason`） |

所有接口统一返回 `{ "code": 200, "message": "...", "data": ... }`。

## 前端页面

| 页面 | 说明 |
|---|---|
| `login.html` | 登录 / 注册 Tab；注册含学校邮箱验证码（60 秒倒计时）；**忘记密码**弹窗（邮箱验证码重置，60 秒倒计时）；成功后 token 存 localStorage 并跳转主应用 |
| `index.html` | 主应用，响应式：手机底部导航 + 悬浮发布，桌面左侧固定边栏；主页搜索 + 校区 Tab + **骨架屏** + 无限滚动；商品卡片 🔥 急售、🏅 高信誉；我的（资料 / 信用分 / 我发布的 **含编辑、下架** / 我买到的 **含确认完成、评价卖家** / 我卖出的 **含同意/拒绝** / 退出登录）；**点击卡片跳转 `detail.html?id=`** |
| `publish.html` | 发布闲置：标题、描述、价格、分类、校区、联系方式 + 图片上传（调 `/api/upload`，回显预览）；提交带 token 调 `/api/item`，成功后弹窗并返回首页 |
| `detail.html` | 商品详情：大图、标题、价格、描述、卖家信息（🏅 高信誉）；底部「我想要」按钮，登录后弹窗展示联系方式；复制按钮按内容动态显示「复制QQ号 / 复制微信号 / 一键复制」；支持弹窗内「直接下单」；**展示卖家评价（平均分 + 评价列表）** |
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
