# 电脑销售管理系统

基于 Spring Boot + Vue 3 的电脑销售管理平台，支持笔记本、台式机整机、DIY 配件的销售与库存管理。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Axios |
| 后端 | Java 21 + Spring Boot 4.0.7 + MyBatis |
| 鉴权 | JWT Bearer token（双角色：customer / staff） |
| 数据库 | MySQL 8.x (InnoDB) |

## 项目结构

```
├── SpringBoot/               # 后端：Java Spring Boot 4.0.7 应用，提供 RESTful API 接口
│   ├── pom.xml                #   Maven 项目配置（依赖管理：Spring Boot、MyBatis、JWT、BCrypt 等）
│   ├── settings.xml           #   Maven 镜像配置（使用阿里云仓库，国内下载依赖更快）
│   ├── mvnw.cmd               #   Maven 包装器脚本（Windows，无需单独安装 Maven）
│   └── src/main/java/com/example/springboot/
│       ├── Application.java   #   启动入口（@SpringBootApplication + @MapperScan）
│       ├── controller/        #   控制器层：接收前端 HTTP 请求，校验权限后转发给 Service
│       ├── service/           #   业务层：处理业务逻辑，调用 Mapper（数据访问）和存储过程
│       ├── mapper/            #   数据访问层：MyBatis 接口，通过注解调用 SQL 或存储过程
│       ├── entity/            #   实体类：与数据库表一一对应的 Java 对象（Lombok @Data）
│       ├── dto/               #   数据传输对象：定义前端传来的请求参数和后端返回的响应格式
│       ├── config/            #   配置类：Jackson（JSON命名）、CORS（跨域）、密码加密器、过滤器注册
│       ├── security/          #   安全模块：JWT 生成/解析、鉴权过滤器、当前用户上下文（ThreadLocal）
│       ├── common/            #   通用模块：自定义业务异常 BizException、全局异常处理器
│       └── init/              #   初始化：DataInitializer 启动时替换种子密码为 BCrypt 哈希
│
├── vue3/                      # 前端：Vue 3 + Vite 8 单页应用，Element Plus 组件库
│   ├── package.json           #   Node 项目配置（依赖：Vue3、Element Plus、Axios、vue-router）
│   ├── vite.config.js         #   Vite 构建配置（路径别名 @ → src、开发服务器代理 /api → 8080）
│   ├── index.html             #   HTML 模板（浏览器访问的入口页面）
│   └── src/
│       ├── main.js            #   应用入口：创建 Vue 实例、注册 Element Plus/路由/图标、启用暗色模式
│       ├── App.vue            #   根组件（整个页面的壳，实际内容由路由切换）
│       ├── api/index.js       #   Axios 封装：统一请求前缀 /api、自动携带 JWT token、401 自动跳转登录
│       ├── router/index.js    #   路由配置：页面路径映射 + 全局守卫（未登录跳登录页、权限不足跳主页）
│       ├── layout/            #   布局组件：MainLayout.vue 提供顶栏导航 + 用户下拉菜单（设置/登出）
│       ├── views/             #   页面视图：Login、Dashboard（主页+购物车下单）、OrderList、ProductList、CustomerList
│       ├── components/        #   可复用组件目录（当前为空，可扩展）
│       └── assets/            #   静态资源：main.css（暗色主题覆盖、CSS 变量）、reset.css（基础样式重置）
│
├── sql/                       # 数据库脚本（必须按 01→05 顺序执行）
│   ├── 01_schema.sql          #   建库 + 建表：computer_sales_db 数据库，9 张业务表
│   ├── 02_indexes.sql         #   索引创建：加速分类筛选、JOIN 查询、按时间排序
│   ├── 03_procedures.sql      #   存储过程：下单、搜索商品、查询订单（核心业务逻辑在数据库中实现）
│   ├── 04_status_procedures.sql # 订单状态机存储过程：付款/发货/取消/退货（取消和退货会自动回补库存）
│   └── 05_test_data.sql       #   测试种子数据：多个客户/销售员账号 + 全品类商品（含密码占位符 __SEED_xxx__）
│
├── docs/                      # 技术文档
│   ├── database.md            #   数据库设计文档（表结构、关系、索引、存储过程详解）
│   ├── backend.md             #   后端技术文档（项目结构、API 接口清单、鉴权流程、业务规则）
│   ├── frontend.md            #   前端技术文档（路由守卫、HTTP 层封装、页面组件说明、样式体系）
│   └── defense-guide.md       #   答辩指南（系统概述、业务流程、架构设计思路，适合答辩参考）★
│
├── er-diagram.drawio          # E-R 图源文件（可用 draw.io 打开编辑）
├── er-diagram.png             # E-R 图预览（答辩时可放入 PPT）
├── AGENTS.md                  # AI 开发指南（供 OpenCode 等 AI 助手使用，含常见陷阱与约定）
└── README.md                  # 本文件：项目说明
```

## 快速开始

```bash
# 数据库初始化
mysql -u root -proot --default-character-set=utf8mb4
source sql/01_schema.sql
source sql/02_indexes.sql
source sql/03_procedures.sql
source sql/04_status_procedures.sql   # 订单状态机（付款/发货/取消/退货）
source sql/05_test_data.sql           # 种子数据（账号 + 全品类商品）

# 后端启动
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run

# 前端启动
cd vue3
npm install
npm run dev
```

> `--default-character-set=utf8mb4` 必须指定，否则中文数据会乱码。

## 分支策略

| 分支 | 说明 |
|------|------|
| `main` | 稳定发布版 |
| `dev` | 开发主分支 |
| `feat/*` | 功能特性分支 |

## 数据库设计

9 张表，符合 3NF，关键设计：

- **双角色账号**：`Customer`（手机号登录，自助下单）与 `Staff`（销售员，管理商品/客户/订单），密码均存 BCrypt 哈希
- **垂直拆分**：Product 与 Laptop_Detail / Desktop_Detail 1:1 拆分
- **悲观锁**：下单时 `SELECT ... FOR UPDATE` 防止超卖
- **存储过程**：`sp_create_order` 支持 JSON 多商品下单；`sp_update_order_status` 实现订单状态机（付款/发货/取消/退货，取消与退货回补库存）
- **审计字段**：所有表含 `created_at` / `updated_at`

技术文档：

- [数据库技术文档](docs/database.md)
- [后端技术文档](docs/backend.md)
- [前端技术文档](docs/frontend.md)
- [答辩指南](docs/defense-guide.md) ★ 推荐阅读，用通俗语言讲解系统全貌
