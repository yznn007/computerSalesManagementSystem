# 电脑销售管理系统 — 后端技术文档

## 1. 概述

后端为单体 Spring Boot 应用，提供 RESTful JSON API，鉴权基于 JWT Bearer token，业务逻辑通过 MyBatis 注解调用 MySQL 存储过程实现。

| 项 | 值 |
|----|----|
| 框架 | Spring Boot 4.0.7（`spring-boot-starter-webmvc`） |
| 语言 | Java 21 |
| ORM | MyBatis Spring Boot Starter 4.0.1（纯注解，无 XML） |
| 数据库 | MySQL 8.x（InnoDB），库名 `computer_sales_db`，账号 `root/root` |
| 鉴权 | JJWT 0.12.6 + spring-security-crypto（BCrypt） |
| 工具 | Lombok、jakarta.validation、jackson-datatype-jsr310 |
| 端口 | 8080 |
| 入口 | `src/main/java/com/example/springboot/Application.java` |

入口类标注 `@SpringBootApplication` 与 `@MapperScan("com.example.springboot.mapper")`，自动扫描 mapper 接口。

---

## 2. 项目结构

```
com.example.springboot
├── Application.java          # 启动入口（@MapperScan）
├── common/                   # 通用层
│   ├── BizException.java     # 业务异常，携带 HTTP code
│   ├── ApiError.java         # 统一错误响应体 {code, message}
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice 全局异常处理
├── config/                   # 配置层
│   ├── JacksonConfig.java    # 显式 ObjectMapper（SNAKE_CASE + JavaTimeModule）
│   ├── PasswordEncoderConfig.java   # BCryptPasswordEncoder Bean
│   ├── FilterConfig.java     # 注册 JwtAuthFilter（url-pattern=/*, order=1）
│   └── WebConfig.java        # CORS 配置
├── controller/               # 表现层（4 个）
│   ├── AuthController.java   # /api/auth
│   ├── ProductController.java# /api/products
│   ├── CustomerController.java # /api/customers
│   └── OrderController.java  # /api/orders
├── service/                  # 业务层（4 个）
├── mapper/                   # 数据访问层（4 个 MyBatis 接口）
├── entity/                   # 实体（与 DB 表对应，Lombok @Data）
├── dto/                      # 请求/响应 DTO（含 jakarta.validation 注解）
│   ├── LoginRequest / LoginResponse / RegisterRequest
│   ├── UpdateProfileRequest / ChangePasswordRequest
│   ├── ProductUpsertRequest / CustomerUpsertRequest
│   ├── OrderCreateRequest / StatusUpdateRequest
├── security/                 # 鉴权
│   ├── JwtUtil.java          # JWT 生成/解析
│   ├── JwtAuthFilter.java    # OncePerRequestFilter，解析 token 注入 AuthContext
│   └── AuthContext.java      # ThreadLocal 持有当前用户
└── init/
    └── DataInitializer.java  # CommandLineRunner @Order(1)，启动时替换种子密码
```

---

## 3. 配置

### 3.1 application.properties 关键项

| 配置 | 值 | 说明 |
|------|----|------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/computer_sales_db?...serverTimezone=Asia/Shanghai` | 数据源 |
| `spring.datasource.username/password` | `root` / `root` | 数据库账号 |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` | DB 下划线列 → 实体驼峰字段自动映射 |
| `spring.jackson.property-naming-strategy` | `SNAKE_CASE` | JSON 输出下划线 key（`product_id`、`order_no`） |
| `app.jwt.secret` | Base64 编码字符串 | JWT 签名密钥（生产环境应更换） |
| `app.jwt.expire-hours` | `24` | token 有效期 24 小时 |
| `server.port` | `8080` | 服务端口 |

### 3.2 JacksonConfig

> ⚠ Spring Boot 4 的 `webmvc` starter 不再自动配置 `ObjectMapper`，故本工程手动定义 `@Bean ObjectMapper`。

- 命名策略：`PropertyNamingStrategies.SNAKE_CASE`
- 注册 `JavaTimeModule` 处理 `LocalDateTime`
- 关闭 `WRITE_DATES_AS_TIMESTAMPS`（日期输出为字符串而非时间戳）

**重要约定**：后端实体字段为驼峰（`productId`、`orderNo`、`customerName`），但 JSON 响应统一输出下划线（`product_id`、`order_no`、`customer_name`）。前端必须按下划线 key 取值。

### 3.3 WebConfig — CORS

- 放行源：`http://localhost:5173`、`http://127.0.0.1:5173`（前端 Vite 默认端口）
- 方法：GET / POST / PUT / DELETE / OPTIONS
- `exposedHeaders`: `Authorization`；`allowCredentials`: true；`maxAge`: 3600

### 3.4 PasswordEncoderConfig

`BCryptPasswordEncoder` 单例，用于密码哈希与校验。

---

## 4. 鉴权机制

### 4.1 JWT 生命周期

`JwtUtil`：

- `@PostConstruct` 将 `app.jwt.secret` Base64 解码为字节，不足 32 字节则补 `'0'`，构造 HMAC-SHA 密钥
- `generate(id, role, name)`：`subject = id`，自定义 claim `role`、`name`，`issuedAt` + `expiration`（now + 24h），签名
- `parse(token)`：验签并返回 `Claims`

JWT payload 含三个字段：用户 id（subject）、角色（`customer` / `staff`）、姓名。

### 4.2 过滤器链路

```
请求 → JwtAuthFilter（/*, order=1） → Controller → AuthContext 校验
```

`JwtAuthFilter` 继承 `OncePerRequestFilter`：

1. 读取 `Authorization: Bearer <token>` 头
2. 解析成功 → `AuthContext.set(new CurrentUser(id, role, name))`
3. 解析失败 → 忽略异常，保持匿名（由后续 `require()` 拦截）
4. `try { chain.doFilter } finally { AuthContext.clear() }` —— 确保线程池复用前清理 ThreadLocal

### 4.3 AuthContext

ThreadLocal 持有 `CurrentUser(Long id, String role, String name)` record。

| 方法 | 行为 |
|------|------|
| `require()` | 无登录用户抛 `BizException(401, "未登录")` |
| `requireStaff()` | 先 `require()`，再校验 role == `"staff"`，否则抛 `BizException(403, "需要销售员权限")` |

角色常量：`ROLE_CUSTOMER = "customer"`、`ROLE_STAFF = "staff"`。

### 4.4 DataInitializer — 种子密码替换

`CommandLineRunner` 标注 `@Order(1)`，启动时执行：

1. 扫描 `Customer` 表中所有自描述占位符 `__SEED_<明文>__`（`LEFT(password_hash,7)='__SEED_'`），提取明文并替换为对应 `BCrypt(明文)`，如 `__SEED_123456__`→`BCrypt("123456")`、`__SEED_159951__`→`BCrypt("159951")`
2. 若 `Staff` 表为空 → 插入默认账号 `admin` / `admin` / 山田小姐
3. 否则按同样规则替换 `Staff` 表的种子占位符，如 `__SEED_admin__`→`BCrypt("admin")`、`__SEED_admin123__`→`BCrypt("admin123")`

> 占位符自带明文（`__SEED_<明文>__`），由 `sql/05_test_data.sql` 写入，后端启动时统一提取明文并替换为真实哈希，避免在 SQL 脚本中硬编码 BCrypt。新增任意账号只需写占位符，无需改 Java。

---

## 5. 分层架构与请求流转

```
HTTP 请求
  └─ JwtAuthFilter（解析 token → AuthContext）
       └─ Controller（@RestController，校验权限、参数 @Valid）
            └─ Service（业务规则、状态码映射、事务编排）
                 └─ Mapper（MyBatis 注解 SQL / 存储过程调用）
                      └─ MySQL（InnoDB，存储过程封装核心事务）
```

- Controller 只做权限校验与参数转发，不含业务逻辑
- Service 承担业务规则校验、存储过程 OUT 参数读取与状态码翻译
- 复杂业务（下单、状态流转）封装在数据库存储过程，Service 仅做调用与结果映射

---

## 6. REST API 接口清单

所有路径前缀 `/api`。鉴权列：`—` 公开、`登录` 需任意登录、`staff` 需销售员。

### 6.1 鉴权 — AuthController `/api/auth`

| 方法 | 路径 | 鉴权 | 入参 | 返回 | 说明 |
|------|------|:----:|------|------|------|
| POST | `/register` | — | `{customerName, phone, address, password}` | void | 客户自助注册；phone 须 11 位数字且唯一 |
| POST | `/login/customer` | — | `{account, password}` | `{token, role, id, name}` | 手机号登录 |
| POST | `/login/staff` | — | `{account, password}` | `{token, role, id, name}` | 用户名登录 |
| GET | `/me` | 登录 | — | `Customer` 或 `Staff` 完整对象 | 当前登录用户详细信息 |
| PUT | `/me` | 登录 | `UpdateProfileRequest` | `Customer` 或 `Staff` | 修改基本信息（见下） |
| PUT | `/me/password` | 登录 | `{oldPassword, newPassword}` | void | 修改密码；原密码错误抛 400 |

`UpdateProfileRequest` 字段按角色区分：

- 客户：`customerName`、`phone`（变更需查重）、`address`
- 销售员：`staffName`、`username`（变更需查重）

### 6.2 商品 — ProductController `/api/products`

| 方法 | 路径 | 鉴权 | 入参 | 返回 | 说明 |
|------|------|:----:|------|------|------|
| GET | `/?category=` | 登录 | query `category`（可选） | `List<Product>` | 按分类筛选商品 |
| GET | `/{id}` | 登录 | path `id` | `ProductDetail` | 商品详情；台式机整机附带 `composition` 组装配置 |
| POST | `/` | staff | `ProductUpsertRequest` | `Product` | 新增商品（按分类写入对应详情子表） |
| PUT | `/{id}` | staff | `ProductUpsertRequest` | `Product` | 编辑商品；**不允许修改分类**（详情表结构差异） |
| DELETE | `/{id}` | staff | path `id` | void | 删除；被订单明细或台式机配置引用时拒绝 |

`ProductUpsertRequest` 字段（按分类填对应详情）：

- 公共：`brand, model, price, stock, category`（分类 ∈ 笔记本/台式机整机/DIY配件）
- 笔记本：`screenSize, cpuModel, gpuModel, weight`
- 台式机整机：`formFactor, cpuDesc, gpuDesc, ramDesc, storageDesc`
- DIY配件：`partType, specification`

### 6.3 客户 — CustomerController `/api/customers`

| 方法 | 路径 | 鉴权 | 入参 | 返回 | 说明 |
|------|------|:----:|------|------|------|
| GET | `/` | staff | — | `List<Customer>` | 客户列表 |
| GET | `/{id}` | staff | path `id` | `Customer` | 单个客户 |
| POST | `/` | staff | `CustomerUpsertRequest` | `Customer` | 新增；默认密码 `123456`；手机号唯一 |
| PUT | `/{id}` | staff | `CustomerUpsertRequest` | `Customer` | 编辑；手机号变更需查重 |
| DELETE | `/{id}` | staff | path `id` | void | 删除；存在关联订单时拒绝 |

`CustomerUpsertRequest`：`customerName, phone(11位), address`（不含密码，新增时由服务端设默认密码）。

### 6.4 订单 — OrderController `/api/orders`

| 方法 | 路径 | 鉴权 | 入参 | 返回 | 说明 |
|------|------|:----:|------|------|------|
| POST | `/` | 登录 | `OrderCreateRequest` | `{status:0, order_no}` | 下单（见下） |
| GET | `/?status=` | 登录 | query `status` | `List<SalesOrder>` | staff 看全部（可按状态筛）；客户只看自己 |
| GET | `/{id}` | 登录 | path `id` | `{order, items}` | 订单详情；客户只能查自己的订单 |
| PUT | `/{id}/status` | 登录 | `StatusUpdateRequest` | void | 状态流转（角色限制见下） |

**订单状态流转权限（易错点）**：

- **销售员**：可发货(`ship`)、取消(`cancel`)、退货(`return`)，但**不能代客户付款**（`pay` 抛 403）
- **客户**：仅可对**自己的订单**执行付款(`pay`)和取消(`cancel`)，不能发货或退货

**下单权限规则（易错点）**：

- 客户下单：控制器用 token 中的 `id` 作为 `customerId`，**忽略请求体中的 `customerId`**（防伪造）
- 销售员代客下单：使用请求体中的 `customerId`，控制器校验非空
- 即同一个端点，角色决定 `customerId` 来源

`OrderCreateRequest`：`customerId`（客户下单不传）、`items: [{productId, quantity(≥1)}]`（非空）。

`StatusUpdateRequest`：`action`（`pay`/`ship`/`cancel`/`return`）、`paymentMethod`（pay 必填）、`cancelReason`（cancel 必填）。

---

## 7. 数据访问层

### 7.1 MyBatis 注解模式

全部 mapper 使用注解（`@Select` / `@Insert` / `@Update` / `@Delete`），无 XML 映射文件。动态 SQL 用 `<script>` 标签包裹（如 `ProductMapper.findAll`、`OrderMapper.findAll` 的条件拼接）。

实体字段驼峰 ↔ 数据库列下划线，由 `map-underscore-to-camel-case=true` 自动映射。`@Options(useGeneratedKeys=true, keyProperty="...")` 回填自增主键。

### 7.2 存储过程调用约定

业务逻辑（下单、状态流转）通过存储过程实现，Mapper 调用范式：

```java
@Select("{call sp_xxx(" +
        "#{in1,mode=IN,jdbcType=INTEGER}," +
        "#{out1,mode=OUT,jdbcType=INTEGER})}")
@Options(statementType = StatementType.CALLABLE)
void callXxx(Map<String, Object> params);
```

- 参数统一用 `Map<String, Object>`，`mode=IN` / `mode=OUT` 区分方向
- OUT 参数调用后从同一 Map 读取（`params.get("out1")`）
- 范例见 `OrderMapper.callCreateOrder` / `callUpdateStatus`

`OrderService` 在调用前将 items 列表序列化为 JSON 字符串传入存储过程，调用后读取 `status` 与 `orderNo` / `message` 并翻译为业务异常。

### 7.3 商品详情查询

`ProductMapper.findDetailById` 通过三表 `LEFT JOIN`（Laptop_Detail / Desktop_Detail / Spare_Part_Detail）一次性取回商品基础字段与三类详情字段，映射到 `ProductDetail` 实体（未匹配的详情字段为 null）。台式机整机额外调用 `findCompositionByProductId` 取组装配置。

---

## 8. 业务规则要点

### 8.1 商品分类

- 三选一：`笔记本` / `台式机整机` / `DIY配件`
- 新增时按分类写入对应详情子表
- 编辑时**禁止修改分类**（子表结构不同，改分类需先删后建）
- 删除时若被 `Order_Detail` 或 `Desktop_Composition` 引用，抛 `DataIntegrityViolationException` → 转为业务异常"该商品存在订单明细或被台式机配置引用，无法删除"

### 8.2 订单状态流转

由存储过程 `sp_update_order_status` 实现（详见数据库技术文档 §7.5）。`OrderService.updateStatus` 的处理：

- 前置校验 `action` ∈ `pay`/`ship`/`cancel`/`return`；`pay` 必填 `paymentMethod`；`cancel` 必填 `cancelReason`
- 调用存储过程后读取 `status`：`0` 成功；`2`（订单不存在）→ 抛 `BizException(404)`；其余 → 抛 `BizException(400)`，message 取自存储过程 `o_message`

### 8.3 下单

由存储过程 `sp_create_order` 实现（悲观锁防超卖，详见数据库技术文档 §7.1）。`OrderService.create` 的处理：

- 将 `items` 序列化为 JSON 字符串传入
- 读取 `status`：`0` 成功返回 `{status:0, order_no}`；`1`→"商品数量不合法"；`2`→"商品不存在"；`3`→"库存不足"；`4`→"系统异常"

### 8.4 客户管理

- 销售员新增客户时**不传密码**，服务端固定设为 `BCrypt("123456")`
- 手机号变更需查重

---

## 9. 全局异常处理

`GlobalExceptionHandler`（`@RestControllerAdvice`）：

| 异常 | HTTP 状态 | 响应体 |
|------|:---------:|--------|
| `BizException` | `code`（400–599 直接作 HTTP 码，否则 400） | `{code, message}` |
| `MethodArgumentNotValidException` | 400 | `{code:400, message:"字段: 默认消息"}`（首个错误） |
| 其他 `Exception` | 500 | `{code:500, message:"服务器内部错误"}` |

业务代码通过 `new BizException(message)`（默认 400）或 `new BizException(code, message)` 控制响应状态码与消息。

---

## 10. 默认测试账号

| 角色 | 账号 | 密码 | 登录端点 |
|------|------|------|----------|
| 客户 | 手机号 `13800138001` | `123456` | `POST /api/auth/login/customer` |
| 客户 | 手机号 `13900139002` | `123456` | `POST /api/auth/login/customer` |
| 客户 | 手机号 `18903503653`（石盛辰） | `159951` | `POST /api/auth/login/customer` |
| 销售员 | 用户名 `admin`（山田小姐） | `admin` | `POST /api/auth/login/staff` |

---

## 11. 开发与测试命令

```bash
# 编译（Maven 已配 SpringBoot/settings.xml 阿里云镜像）
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml compile

# 运行（端口 8080，需先启动 MySQL 并完成 sql/ 脚本初始化）
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run

# 全量测试
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test

# 单个测试
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test "-Dtest=ApplicationTests"
```

> ⚠ 测试需先启动 MySQL：唯一测试为 `ApplicationTests.contextLoads`（`@SpringBootTest`），会加载完整 Spring 上下文并触发 `DataInitializer` 执行种子密码替换。

---

## 12. 已知约束与陷阱

- **JSON 命名**：响应统一 SNAKE_CASE 下划线 key，前端按下划线取值，勿用驼峰
- **下单 customer_id 来源**：客户下单强制取 token.id，请求体 `customerId` 被忽略；销售员代客下单才用请求体 `customerId`
- **订单状态流转权限分角色**：staff 可 ship/cancel/return 不可 pay；客户仅可对自己订单 pay/cancel
- **商品分类不可改**：编辑商品时改分类会抛业务异常
- **存储过程 OUT 参数读取**：MyBatis 调用后须从原传入的 Map 中 `get` OUT 参数，`null` 兜底为系统异常状态码
- **MySQL `SELECT ... INTO` 陷阱**：查不到行时变量不会置 NULL，存储过程内须用 `ROW_COUNT() = 0` 判断存在性（详见数据库技术文档）
- **Spring Boot 4 不自动配置 ObjectMapper**：依赖 `JacksonConfig` 显式 Bean，删除该配置会导致 JSON 命名策略与日期序列化失效
- **ThreadLocal 清理**：`JwtAuthFilter` 必须在 finally 中 `AuthContext.clear()`，否则线程池复用导致用户身份串号
- **JWT 密钥**：`application.properties` 中的 `app.jwt.secret` 为示例值，生产环境必须更换
