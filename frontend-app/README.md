# 青售 · 前端工程（Vite + Vue3）

由原来的单文件 `frontend/*.html` 重构而来，标准 Vite + Vue 3 SFC 工程。

## 运行命令

要求：Node.js ≥ 18（推荐 20/22），npm 或 pnpm 均可。

```bash
cd frontend-app
npm install
npm run dev
```

启动后打开：http://localhost:5174

生产构建：

```bash
npm run build      # 产物在 dist/
npm run preview    # 本地预览构建产物
```

> 使用 pnpm 的同学：`pnpm install` 后如果提示 `Ignored build scripts: esbuild`，执行一次 `pnpm approve-builds`（或 `pnpm rebuild esbuild`）即可；本仓库已用 `pnpm-workspace.yaml` 声明允许 esbuild 构建脚本。

## 开发代理

`vite.config.js` 已配置：

| 路径 | 代理目标 |
|---|---|
| `/api` | http://localhost:8080 |
| `/uploads` | http://localhost:8080 |

因此本地开发不需要改 CORS；接口地址默认走同源代理（`.env.development` 中 `VITE_API_BASE=` 为空）。
如需直连后端，把 `.env.development` 改成 `VITE_API_BASE=http://localhost:8080`。

## 目录结构

```
frontend-app/
├── index.html
├── package.json
├── vite.config.js
├── .env.development
└── src/
    ├── main.js                 # 挂载 Vue / Pinia / Router / Element Plus
    ├── App.vue                 # 只渲染 <router-view/>
    ├── api/http.js             # fetch 封装（token 注入、401 统一处理）
    ├── stores/user.js          # Pinia：token / 用户资料 / 登录登出
    ├── router/index.js         # 路由与鉴权守卫
    ├── styles/base.css         # 全局主题与布局样式
    ├── utils/                  # 图片、格式化、剪贴板工具
    ├── components/
    │   ├── AppShell.vue        # 桌面侧边栏 + 移动端顶栏/底部导航
    │   ├── ItemCard.vue        # 商品卡片
    │   ├── SkeletonGrid.vue    # 骨架屏
    │   ├── TopUpDialog.vue     # 申请置顶
    │   ├── EditItemDialog.vue  # 编辑商品
    │   └── ReviewDialog.vue    # 评价卖家
    └── views/
        ├── LoginView.vue       # 登录 / 注册 / 忘记密码
        ├── HomeView.vue        # 首页：搜索、校区、无限滚动
        ├── MessagesView.vue    # 消息
        ├── ProfileView.vue     # 我的：发布/买到/卖出、编辑下架、评价
        ├── PublishView.vue     # 发布闲置
        ├── DetailView.vue      # 商品详情 + 卖家评价
        └── AdminView.vue       # 管理后台
```

## 路由

| 路径 | 页面 | 权限 |
|---|---|---|
| `/login` | 登录 / 注册 | 游客 |
| `/` | 首页 | 需登录 |
| `/messages` | 消息 | 需登录 |
| `/profile` | 我的 | 需登录 |
| `/publish` | 发布闲置 | 需登录 |
| `/detail/:id` | 商品详情 | 公开 |
| `/admin` | 管理后台 | 仅管理员（role=2） |

旧的 `frontend/*.html` 保留作回退，切换稳定后可删除。
