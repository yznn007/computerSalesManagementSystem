# 电脑销售管理系统 — 前端技术文档

## 1. 概述

前端为单页应用（SPA），基于 Vue 3 组合式 API，使用 Vite 构建，Element Plus 提供组件库，vue-router 管理路由，Axios 与后端 8080 端口通信。支持双角色（客户 / 销售员）界面差异化渲染。

| 项 | 值 |
|----|----|
| 框架 | Vue 3.5.32（组合式 API，`<script setup>`） |
| 构建 | Vite 8.0.8 + @vitejs/plugin-vue 6 |
| UI 库 | Element Plus 2.14.2 + @element-plus/icons-vue |
| 路由 | vue-router 4.6.4（HTML5 history 模式） |
| HTTP | Axios 1.18 |
| Node 版本 | `^20.19.0` 或 `>=22.12.0`（见 `package.json` engines） |
| 开发端口 | 5173 |
| 构建产物 | `vue3/dist` |

---

## 2. 项目结构

```
vue3/
├── index.html               # HTML 模板（title: One click, one PC）
├── vite.config.js           # Vite 配置（别名 @ + 代理）
├── package.json
└── src/
    ├── main.js              # 应用入口（挂载、注册插件、暗色模式）
    ├── App.vue              # 根组件（仅 <router-view />）
    ├── api/index.js         # Axios 单例 + 全部 API 函数
    ├── router/index.js      # 路由表 + 全局守卫
    ├── layout/MainLayout.vue # 顶栏导航布局
    ├── views/               # 7 个页面视图
    │   ├── Login.vue
    │   ├── Dashboard.vue
    │   ├── OrderCreate.vue
    │   ├── OrderList.vue
    │   ├── ProductList.vue
    │   ├── CustomerList.vue
    │   └── SalesReport.vue   # 占位页，未挂载到路由
    ├── components/          # 组件目录（当前为空）
    └── assets/
        ├── main.css         # Element Plus 暗色覆盖 + 全局组件样式
        └── reset.css        # CSS Reset + 字体引入 + CSS 变量定义
```

---

## 3. 应用入口与全局配置

### 3.1 main.js

```js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

document.documentElement.classList.add('dark')  // 全局暗色模式

const app = createApp(App)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)  // 全局注册所有图标组件
}
app.mount('#app')
```

要点：

- 全局启用暗色模式（`<html class="dark">`），配合 Element Plus 暗色变量表
- Element Plus 中文语言包 `zhCn`
- **所有** `@element-plus/icons-vue` 图标作为全局组件注册（视图中直接用 `<DataAnalysis />` 等标签名）

### 3.2 App.vue

仅含 `<router-view />`，所有页面由路由驱动。

### 3.3 全局样式加载

`main.js` 引入了 Element Plus 的 `index.css` 与暗色 `dark/css-vars.css`。`src/assets/main.css`（含 `reset.css` 与自定义 CSS 变量、Element Plus 暗色覆盖）**当前未被任何模块 import 引入**——详见 §10 已知问题。

---

## 4. Vite 配置

```js
// vite.config.js
resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } }
server: { proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } }
```

| 配置 | 说明 |
|------|------|
| 路径别名 `@` | 指向 `vue3/src`，import 时可用 `@/api` |
| 代理 `/api` | 转发至 `http://localhost:8080`，`changeOrigin: true` |

> **代理是前后端联调的关键**：前端用相对路径 `/api/*`，由 Vite dev server 代理到后端 8080，故后端必须运行在 8080。CORS 由后端 `WebConfig` 放行 5173 端口配合。

---

## 5. 路由与权限守卫

### 5.1 路由表

| 路径 | 视图 | meta | 说明 |
|------|------|------|------|
| `/login` | `Login.vue` | — | 公开登录页 |
| `/` | `MainLayout.vue` | redirect `/dashboard` | 布局壳，子路由嵌套 |
| `/dashboard` | `Dashboard.vue` | `{title:'主页'}` | 商品浏览 |
| `/order-create` | `OrderCreate.vue` | `{title:'下单'}` | 下单 |
| `/products` | `ProductList.vue` | `{title:'商品', staff:true}` | 商品管理（仅 staff） |
| `/orders` | `OrderList.vue` | `{title:'订单'}` | 订单 |
| `/customers` | `CustomerList.vue` | `{title:'客户', staff:true}` | 客户管理（仅 staff） |

> `SalesReport.vue` 存在但未在路由表注册，当前不可访问。

### 5.2 全局守卫

```js
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') return '/login'              // 未登录 → 登录页
  if (token && to.path === '/login') return '/dashboard'           // 已登录访问登录页 → 主页
  if (to.meta.staff && localStorage.getItem('role') !== 'staff') return '/dashboard' // 非staff访问staff页 → 主页
})
```

权限判断基于 `localStorage.role`，值由登录响应写入。

---

## 6. HTTP 层

### 6.1 Axios 实例

`src/api/index.js` 创建单例：

```js
const api = axios.create({ baseURL: '/api', timeout: 10000 })

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    ElMessage.error(msg)                              // 统一错误提示
    if (err.response?.status === 401) {               // 401 自动登出
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('name')
      if (location.pathname !== '/login') location.href = '/login'
    }
    return Promise.reject(err)
  }
)
```

要点：

- `baseURL: '/api'`，所有请求走 Vite 代理
- 请求拦截自动注入 `Authorization: Bearer <token>`
- 响应拦截统一用 `ElMessage.error` 弹错误（取 `err.response.data.message`，对应后端 `ApiError.message`）
- 401 自动清除登录态并跳转 `/login`

### 6.2 API 函数清单

| 函数 | 方法 / 路径 | 说明 |
|------|-------------|------|
| `register(data)` | POST `/auth/register` | 客户注册 |
| `loginCustomer(data)` | POST `/auth/login/customer` | 客户登录 |
| `loginStaff(data)` | POST `/auth/login/staff` | 销售员登录 |
| `me()` | GET `/auth/me` | 当前用户 |
| `getCustomers()` | GET `/customers` | 客户列表 |
| `addCustomer(data)` | POST `/customers` | 新增客户 |
| `updateCustomer(id, data)` | PUT `/customers/{id}` | 编辑客户 |
| `deleteCustomer(id)` | DELETE `/customers/{id}` | 删除客户 |
| `getProducts(category)` | GET `/products?category=` | 商品列表 |
| `getProductDetail(id)` | GET `/products/{id}` | 商品详情 |
| `createProduct(data)` | POST `/products` | 新增商品 |
| `updateProduct(id, data)` | PUT `/products/{id}` | 编辑商品 |
| `deleteProduct(id)` | DELETE `/products/{id}` | 删除商品 |
| `createOrder(data)` | POST `/orders` | 下单 |
| `getOrders(params)` | GET `/orders` | 订单列表（params 可含 status） |
| `getOrderDetail(id)` | GET `/orders/{id}` | 订单详情 |
| `updateOrderStatus(id, payload)` | PUT `/orders/{id}/status` | 状态流转 |

### 6.3 字段命名约定

前端全程使用 **snake_case** 字段名（`product_id`、`order_no`、`customer_name`、`screen_size`、`total_amount` 等），与后端 Jackson `SNAKE_CASE` 输出对齐。新增/编辑表单提交的字段名同样用 snake_case（如 `customer_name`、`screen_size`），与后端 DTO 的驼峰字段经 Jackson 反序列化时自动转换。

---

## 7. 布局与导航

`MainLayout.vue`：顶栏 + 主内容区。

- 顶栏左侧 ASCII Logo（链接 `/dashboard`）
- 导航项：主页、下单、商品（仅 staff 可见）、订单、客户（仅 staff 可见）
- 右侧：当前用户名、登出按钮、实时时钟
- 导航项的 staff 显隐由 `role === 'staff'`（读 localStorage）控制，与路由守卫双重保障
- 登出：清除 `token/role/name/uid` 后跳 `/login`

主内容区 `<router-view />`，最大宽度 1280px 居中。

---

## 8. 页面视图

### 8.1 Login.vue — 登录/注册

- 客户登录 / 销售员登录 tab 切换
- 登录成功写入 `localStorage`：`token`、`role`、`name`、`uid`，跳 `/dashboard`
- 客户模式下提供注册入口（`el-dialog` 弹窗），注册字段 `customer_name/phone/address/password`

### 8.2 Dashboard.vue — 商品浏览主页

- ASCII Logo + 搜索框 + 分类 tab（全部/笔记本/台式机整机/DIY配件）+ 子分类下拉
- 商品列表行点击弹 `el-dialog` 显示详情（调 `getProductDetail`，按 `screen_size`/`cpu_model`/`part_type` 等字段判断分类展示）

### 8.3 OrderCreate.vue — 下单

- 左右双栏：左侧商品面板（搜索 + 分类 tab + 商品列表），右侧购物车
- 销售员额外显示**客户选择**（`el-select` filterable，调 `getCustomers`）；客户角色无此栏，下单时 `customer_id` 由后端从 token 注入
- 购物车支持数量调整（上限库存）、移除、合计
- 提交载荷 `{items:[{product_id, quantity}], customer_id?}`；成功（`res.data.status === 0`）后弹单号并刷新库存

### 8.4 OrderList.vue — 订单列表

- 状态筛选 tab（全部/待付款/已付款/已发货/已取消/已退货）
- 表格展示订单号、客户、金额、时间、状态
- staff 可执行：详情、付款（弹窗选支付方式）、取消（弹窗填原因）、发货（确认框）、退货（确认框）
- 客户仅可查看详情
- 状态流转按钮按订单状态动态显隐（如"待付款"才显示付款/取消按钮）

### 8.5 ProductList.vue — 商品管理（staff）

- 分类 tab + 新增/编辑/删除按钮（仅 staff 可见操作列）
- 新增/编辑弹窗按分类动态渲染详情字段表单（笔记本/台式机整机/DIY配件）
- 编辑时**分类选择框禁用**（后端不允许改分类）
- 详情弹窗展示对应分类详情字段；台式机整机额外展示组装配置 `composition`

### 8.6 CustomerList.vue — 客户管理（staff）

- 表格展示编号、姓名、手机号、地址
- 新增/编辑/删除，表单含 `el-form` 校验规则（姓名必填、手机号 11 位、地址必填）
- 新增客户不传密码，后端设默认 `123456`

### 8.7 SalesReport.vue — 销售报表（占位）

占位页"功能即将上线"，**未挂载到路由**，当前不可访问。

---

## 9. 本地存储与状态

前端无 Vuex/Pinia，登录态与用户信息持久化在 `localStorage`：

| key | 内容 | 写入时机 | 清除时机 |
|-----|------|----------|----------|
| `token` | JWT | 登录成功 | 登出 / 401 响应 |
| `role` | `customer` / `staff` | 登录成功 | 登出 / 401 响应 |
| `name` | 用户姓名 | 登录成功 | 登出 / 401 响应 |
| `uid` | 用户 id | 登录成功 | 登出 |

视图内通过 `computed(() => localStorage.getItem('role'))` 读取角色实现差异化渲染。因 `localStorage` 非响应式，角色在会话内不变；登录/登出通过路由跳转重新挂载视图刷新。

---

## 10. 样式体系与已知问题

### 10.1 设计风格

全局采用**暗色极简**风格：

- 暗色背景（`#0a0a0a` 系）、灰阶文字层级
- 等宽字体（`--font-mono`，JetBrains Mono / Fira Mono）用于数字、单号、价格
- 无圆角卡片、紧凑表格、扁平按钮（详见 `assets/main.css` 对 Element Plus 的覆盖）

### 10.2 CSS 变量

`assets/reset.css` 定义全局 CSS 变量（如 `--font-mono`、`--font-sans`、`--text-primary`、`--text-secondary`、`--text-tertiary`、`--bg-hover`、`--border-dim` 等），视图 `<style scoped>` 中大量引用。

### 10.3 ⚠ 已知问题：main.css 未被引入

`src/assets/main.css`（含 `reset.css` 的 `@import`、CSS 变量定义、Element Plus 暗色组件覆盖）**当前未在 `main.js` 或任何模块中 `import`**，`index.html` 也未 `<link>`。这导致：

- 视图中 `var(--font-mono)`、`var(--text-primary)` 等自定义变量**回退到浏览器默认值**（等宽字体、文字颜色等未按设计生效）
- `assets/main.css` 中对 Element Plus 暗色组件的覆盖样式未生效

修复方式：在 `main.js` 中追加 `import './assets/main.css'`（或在 `App.vue` 中引入）。本文档记录此现状，便于后续维护者定位样式异常。

---

## 11. 开发与构建命令

```bash
# 安装依赖（Node ^20.19.0 || >=22.12.0）
cd vue3
npm install

# 开发服务器（默认 http://localhost:5173，需后端运行在 8080）
npm run dev

# 生产构建（产物 dist/）
npm run build

# 预览构建产物
npm run preview
```

---

## 12. 前后端协作约定速查

| 约定 | 前端做法 | 后端对应 |
|------|----------|----------|
| 接口前缀 | 相对路径 `/api/*` | `@RequestMapping("/api/...")` + Vite 代理 |
| 字段命名 | snake_case（`product_id`） | Jackson `SNAKE_CASE` 输出 |
| 鉴权头 | `Authorization: Bearer <token>` | `JwtAuthFilter` 解析 |
| 错误处理 | 读 `err.response.data.message` 弹 ElMessage | `ApiError {code, message}` |
| 401 处理 | 清登录态跳 `/login` | `BizException(401)` 或 token 过期 |
| 角色判断 | `localStorage.role === 'staff'` | JWT claim `role` |
| 下单 customer_id | 客户不传、staff 传 | 客户取 token.id、staff 取 body |
| 商品分类不可改 | 编辑时分类框禁用 | `ProductService.update` 校验拒绝 |
