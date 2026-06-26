# 电脑销售系统

Spring Boot 4.0.7 (Java 21) + Vue 3 + MySQL 8 的电脑销售管理平台。双角色：客户自助注册下单，销售员(staff)管理商品/客户/订单。

## 项目结构

```
SpringBoot/   后端 (Spring Boot 4.0.7 + MyBatis + Lombok，Java 21)，入口 src/main/java/com/example/springboot/Application.java
vue3/         前端 (Vue 3 + Vite 8 + Element Plus + vue-router + Axios，JS 非 TS)
sql/          数据库脚本（按文件编号顺序执行）
docs/         技术文档（database.md / backend.md / frontend.md / sql-test.md / defense-guide.md + CourseDesignReport/ 课程设计报告）
```

⚠️ Spring Boot 4 将 `spring-boot-starter-web` 重命名为 `spring-boot-starter-webmvc`，测试依赖同理为 `spring-boot-starter-webmvc-test`。不要使用旧的 artifact ID。

## 开发命令

```bash
# 数据库初始化（必须按编号顺序；--default-character-set=utf8mb4 必加，否则中文乱码）
mysql -u root -proot --default-character-set=utf8mb4
source sql/01_schema.sql            # 建库 computer_sales_db + 9 表
source sql/02_indexes.sql           # 非聚簇索引
source sql/03_procedures.sql        # sp_create_order / sp_search_products / sp_get_customer_orders / sp_get_order_detail
source sql/04_status_procedures.sql # sp_update_order_status 订单状态机
source sql/05_test_data.sql         # 种子数据（含 __SEED_<明文>__ 占位密码）

# 后端（端口 8080；Maven 用 SpringBoot/settings.xml 阿里云镜像）
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml compile
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test                     # ⚠ 需先启动 MySQL
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test "-Dtest=ApplicationTests"

# 前端（http://localhost:5173；Node ^20.19.0 || >=22.12.0）
cd vue3; npm install; npm run dev
cd vue3; npm run build
```

后端含 `spring-boot-devtools`，开发时支持热重载。Maven 通过 `SpringBoot/settings.xml` 使用阿里云镜像加速依赖下载。

## 数据库（9 表，3NF，库名 computer_sales_db，账号 root/root）

权威定义见 `sql/01_schema.sql`。核心表：Customer / Staff / Product / Laptop_Detail / Desktop_Detail / Spare_Part_Detail / Desktop_Composition / Sales_Order / Order_Detail。

关键陷阱：

- **悲观锁防超卖**：下单 `SELECT stock ... FOR UPDATE`，整事务封装在 `sp_create_order`；入参 `p_items` 为 JSON 数组 `[{"product_id":1,"quantity":2}]`
- **存储过程状态码**：`sp_create_order` → 0成功/1数量不合法/2商品不存在/3库存不足/4系统异常；`sp_update_order_status` → 0成功/1非法状态流转/2订单不存在/3系统异常
- ⚠ **MySQL 陷阱**：`SELECT ... INTO` 查不到行时变量不会置 NULL，过程内必须用 `ROW_COUNT() = 0` 判断存在性
- 订单取消/退货会回补库存；所有表含 `created_at` / `updated_at` 审计字段

## 后端约定

- **业务逻辑走存储过程**，不直接拼复杂 SQL。MyBatis Mapper 调用范式：`@Select("{call sp_xxx(#{p,mode=IN,jdbcType=...},#{o,mode=OUT,jdbcType=...})}")` + `@Options(statementType=StatementType.CALLABLE)`，参数用 `Map<String,Object>`。范例见 `mapper/OrderMapper.java`
- **JSON 输出 SNAKE_CASE**（`spring.jackson.property-naming-strategy=SNAKE_CASE`）：后端返回 `product_id`/`order_no` 等下划线 key
- **Spring Boot 4 不自动配置 ObjectMapper**：依赖 `config/JacksonConfig.java` 显式 Bean（SNAKE_CASE + JavaTimeModule），删除该配置会导致 JSON 命名与日期序列化失效
- MyBatis `map-underscore-to-camel-case=true`：DB 下划线列自动映射到实体驼峰字段
- **鉴权**：JWT Bearer token（24h，密钥 `app.jwt.secret`）。`JwtAuthFilter` 解析后写入 `AuthContext`（ThreadLocal）。控制器用 `AuthContext.require()` 校验登录、`requireStaff()` 校验销售员。⚠ `JwtAuthFilter` 必须在 finally 中 `AuthContext.clear()`，防止线程池复用串号
- **权限规则（易错）**：
  - **下单**：客户下单用 token 的 customer_id（忽略 body，防伪造）；仅销售员代客下单才用 body 的 customer_id
  - **查订单**：客户只能查自己的订单；staff 可查全部
  - **状态流转**：staff 可 ship/cancel/return，但**不能代付款**（pay 抛 403）；客户只能对**自己的**订单执行 pay/cancel
  - **商品 CRUD、客户管理**：仅 staff
  - **销售看板统计**（`GET /api/stats/overview`）：仅 staff（`StatsController.requireStaff()`）
- **DataInitializer**（`CommandLineRunner` @Order(1)）：启动时扫描 Customer/Staff 表中 `__SEED_<明文>__` 占位符，提取明文替换为 BCrypt 哈希；若 Staff 表为空则插入默认 admin/admin（山田小姐）
- 全局异常：`BizException(code,msg)` 的 code（400-599）直接作为 HTTP 状态码，响应体 `{code,message}`
- CORS 放行 `http://localhost:5173` / `127.0.0.1:5173`

## 默认测试账号

- 客户：phone `13800138001` / 密码 `123456`（另有 `13900139002`/`123456`、`18903503653`(佐佐木大叔)/`159951`）
- 销售员：username `admin`（山田小姐）/ 密码 `admin`
- 端点：`POST /api/auth/login/customer`、`POST /api/auth/login/staff`、`POST /api/auth/register`、`GET /api/auth/me`、`PUT /api/auth/me`、`PUT /api/auth/me/password`

## 前端约定

- **默认暗色模式**：`main.js` 入口 `document.documentElement.classList.add('dark')`，`main.css`（含 CSS 变量定义 + Element Plus 暗色覆盖）在 `main.js` 中 import
- Axios 单例 baseURL `/api`，请求拦截器注入 `Authorization: Bearer <token>`；响应 401 自动清 token 跳 `/login`（`src/api/index.js`）
- **Vite 代理**：`/api` → `http://localhost:8080`（`vite.config.js`）。前端用相对路径 `/api/*`，后端必须运行在 8080
- 路径别名 `@` → `vue3/src`
- vue-router：`/login` 公开，其余需 token；`meta.staff: true`（商品/客户管理）仅 staff 可进
- **下单在 Dashboard.vue 内完成**：商品列表 + 购物车抽屉（el-drawer），staff 额外显示客户选择
- **订单操作按角色分**：staff 看到取消/发货/退货按钮；客户看到付款/取消按钮
- **账户设置**在 MainLayout.vue 用户下拉菜单中打开（el-dialog）
- 登录态存储在 `localStorage`（token/role/name/uid），无 Vuex/Pinia
- 无 lint / typecheck 命令（未配置 ESLint、Prettier、TypeScript）

## 分支策略

| 分支 | 说明 |
|------|------|
| `main` | 稳定发布版 |
| `dev` | 开发主分支 |
| `feat/*` | 功能特性分支 |
