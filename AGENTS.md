# 电脑销售系统

Spring Boot 4.0.7 (Java 21) + Vue 3 + MySQL 8 的电脑销售管理平台。双角色：客户自助注册下单，销售员(staff)管理商品/客户/订单。

## 项目结构

```
SpringBoot/   后端 (Spring Boot 4.0.7 + MyBatis，Java 21)，入口 src/main/java/com/example/springboot/Application.java
vue3/         前端 (Vue 3 + Vite + Element Plus + vue-router + Axios)
sql/          数据库脚本（按文件编号顺序执行，见下）
docs/         数据库技术文档
er-diagram.*  E-R 图（draw.io 源 + PNG 预览）
```

## 开发命令

```bash
# 数据库初始化（必须按编号顺序；--default-character-set=utf8mb4 必加，否则中文乱码/报错）
mysql -u root -proot --default-character-set=utf8mb4
source sql/01_schema.sql            # 建库 computer_sales_db + 9 表
source sql/02_indexes.sql           # 非聚簇索引
source sql/03_procedures.sql        # sp_create_order / sp_search_products / sp_get_customer_orders / sp_get_order_detail
source sql/04_test_data.sql         # 种子数据（含 __SEED_*__ 占位密码，由后端启动时替换）
source sql/05_status_procedures.sql # sp_update_order_status 订单状态机（付款/发货/取消/退货）

# 后端（端口 8080；Maven 已配 SpringBoot/settings.xml 阿里云镜像）
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml compile
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test                     # ⚠ 需先启动 MySQL：唯一测试是 @SpringBootTest contextLoads，会加载完整上下文并触发 DataInitializer
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test "-Dtest=ApplicationTests"   # 跑单个测试

# 前端（默认 http://localhost:5173；Node ^20.19.0 || >=22.12.0）
cd vue3; npm install; npm run dev
cd vue3; npm run build
```

## 数据库（9 表，3NF，库名 computer_sales_db，账号 root/root）

权威定义见 `sql/01_schema.sql`。表清单：

| 表 | 说明 | 关键关系 |
|----|------|----------|
| Customer | 客户（phone 为登录账号且 UNIQUE，password_hash 存 BCrypt） | 1:N Sales_Order |
| Staff | 销售员账号（username UNIQUE + password_hash BCrypt） | 管理端登录角色 |
| Product | 商品（category ENUM: 笔记本/台式机整机/DIY配件，CHECK stock>=0） | 1:1 Laptop/Desktop_Detail，1:N Spare_Part_Detail |
| Laptop_Detail / Desktop_Detail | 笔记本/台式机详情（与 Product 1:1 垂直拆分，ON DELETE CASCADE） | — |
| Spare_Part_Detail | DIY配件规格（part_type ENUM: CPU/显卡/主板/内存/硬盘/电源/机箱/散热器） | 1:N Desktop_Composition |
| Desktop_Composition | 台式机组装配置（product_id→台式机整机，part_id→配件，quantity>0） | N:1 Desktop_Detail / Spare_Part_Detail |
| Sales_Order | 订单主表（status ENUM: 待付款/已付款/已发货/已取消/已退货；payment_method ENUM） | N:1 Customer，1:N Order_Detail |
| Order_Detail | 订单明细（ON DELETE CASCADE 跟随订单，quantity>0） | N:1 Product |

关键设计 / 陷阱：

- **悲观锁防超卖**：下单 `SELECT stock ... FOR UPDATE`，整事务封装在 `sp_create_order`；入参 `p_items` 为 JSON 数组 `[{"product_id":1,"quantity":2}]`
- **存储过程状态码**：`sp_create_order` → 0成功 / 1数量不合法 / 2商品不存在 / 3库存不足 / 4系统异常；`sp_update_order_status` → 0成功 / 1非法状态流转 / 2订单不存在 / 3系统异常（action: `pay`|`ship`|`cancel`|`return`，cancel 与 return 会 JOIN 回补库存）
- ⚠ **MySQL 陷阱**：`SELECT ... INTO` 查不到行时变量不会置 NULL，保持原默认值。过程内必须用 `ROW_COUNT() = 0` 判断商品/订单是否存在
- 事务隔离 REPEATABLE READ（InnoDB 默认）；所有表含 created_at / updated_at 审计字段

## 后端约定

- **业务逻辑走存储过程**，不直接拼复杂 SQL。MyBatis Mapper 注解调用范式：`@Select("{call sp_xxx(#{p,mode=IN,jdbcType=...},#{o,mode=OUT,jdbcType=...})}")` + `@Options(statementType=StatementType.CALLABLE)`，参数用 `Map<String,Object>`（IN/OUT 用 mode 区分）。范例见 `mapper/OrderMapper.java` 的 `callCreateOrder` / `callUpdateStatus`
- **JSON 输出 SNAKE_CASE**（`spring.jackson.property-naming-strategy=SNAKE_CASE`）：后端返回 `product_id`/`order_no` 等下划线 key，前端按此取值，勿用驼峰
- MyBatis `map-underscore-to-camel-case=true`：DB 下划线列自动映射到实体驼峰字段
- **鉴权**：JWT Bearer token（24h，密钥在 `application.properties` 的 `app.jwt.secret`）。`JwtAuthFilter` 解析后写入 `AuthContext`（ThreadLocal）。两种角色常量在 `security/AuthContext.java`：`customer` / `staff`。控制器用 `AuthContext.require()` 登录校验、`requireStaff()` 销售员校验
- **权限规则（易错）**：客户下单用 token 中的 customer_id（忽略 body，防伪造）；仅销售员代客下单才用 body 的 customer_id。客户只能查自己的订单；商品 CRUD、订单状态流转、客户管理仅 staff
- **DataInitializer**（`CommandLineRunner` @Order(1)）：启动时把种子占位符 `__SEED_123456__` / `__SEED_ADMIN123__` 替换为 BCrypt 哈希；若 Staff 表为空则插入默认 admin/admin123
- 全局异常：`BizException(code,msg)` 的 code（400-599）直接作为 HTTP 状态码，响应体 `{code,message}`（见 `common/GlobalExceptionHandler.java`）
- CORS 放行 `http://localhost:5173` / `127.0.0.1:5173`（`config/WebConfig.java`）

## 默认测试账号

- 客户：phone `13800138001` / 密码 `123456`（另有 `13900139002` / `123456`）
- 销售员：username `admin` / 密码 `admin123`
- 登录端点：`POST /api/auth/login/customer`、`POST /api/auth/login/staff`、`POST /api/auth/register`、`GET /api/auth/me`

## 前端约定

- Axios 单例 baseURL `/api`，请求拦截器加 `Authorization: Bearer <localStorage.token>`；响应 401 自动清 token 跳 `/login`（`src/api/index.js`）
- **Vite 代理**：`/api` → `http://localhost:8080`（`vite.config.js`）。故前端用相对路径 `/api/*`，后端必须运行在 8080
- 路径别名 `@` → `vue3/src`
- vue-router：`/login` 公开，其余路由需 token；`meta.staff: true` 的路由（商品/客户管理）仅 staff 可进（`src/router/index.js`）
- 下单流程：选客户（staff 代选）→ 选商品 → 输数量 → 提交；结果用 ElMessage 弹窗反馈，下单成功后刷新库存

## 备注

- `README.md` 中 "Spring Boot 3.x" 为过期描述，实际版本以 `SpringBoot/pom.xml` 的 `4.0.7` 为准
