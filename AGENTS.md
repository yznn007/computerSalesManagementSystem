# 电脑销售系统

## 版本与环境

| 项 | 值 |
|------|------|
| Spring Boot | 4.0.7 |
| Java | 21 |
| MyBatis | 4.0.1 |
| Vue | 3.5.x |
| Vite | 8.x |
| Node.js | >=22.12.0 |
| MySQL | 8.0.x |
| 构建工具 | Maven Wrapper (`mvnw`) |
| 数据库名 | `computer_sales_db` |

## 项目结构

```
├── SpringBoot/          # 后端 (Spring Boot + MyBatis)
│   ├── mvnw / mvnw.cmd  # Maven Wrapper（自动下载 Maven 3.9.16）
│   ├── pom.xml          # parent: spring-boot-starter-parent:4.0.7
│   └── src/main/java/com/example/springboot/
├── vue3/                # 前端 (Vue 3 + Vite)
│   └── package.json     # npm run dev / build / preview
├── sql/                 # 数据库脚本（已执行）
│   ├── 01_schema.sql    # 建库 + 建表
│   ├── 02_indexes.sql   # 非聚簇索引
│   ├── 03_procedures.sql# 存储过程 sp_create_order
│   └── 04_test_data.sql # 测试数据（2客户 + 11商品）
├── .agents/skills/      # skills.sh 安装的技能（OpenCode 自动发现）
└── AGENTS.md
```

## 开发命令

```bash
# 后端 - 构建 & 启动
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml compile     # 编译
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml spring-boot:run  # 启动
.\SpringBoot\mvnw.cmd -f .\SpringBoot\pom.xml test         # 运行测试

# 前端
cd vue3; npm install; npm run dev          # 安装依赖 + 启动 (默认 http://localhost:5173)
cd vue3; npm run build                     # 生产构建

# 数据库 - 连接 & 初始化
mysql -u root -proot --default-character-set=utf8mb4
# 初始化顺序: 01_schema.sql → 02_indexes.sql → 03_procedures.sql → 04_test_data.sql
```

> `--default-character-set=utf8mb4` 必须加，否则中文测试数据会乱码/报错。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Axios |
| 后端 | Java (Spring Boot) |
| 数据库 | MySQL 8.x (InnoDB 存储引擎) |

### UI 设计约定

- **暗夜主题**：纯黑背景 `#0a0a0a`，Element Plus 原生暗黑模式（`html class="dark"`）
- **暗金强调色**：`#C9A96E`，仅用于提交按钮、ASCII 横幅渐变、Element Plus 主色覆盖
- **等宽数字**：JetBrains Mono 用于订单号、金额、库存、时间
- **无边框设计**：内容浮于背景，靠排版而非装饰区分层级
- **顶栏**：48px 纯文字导航，`#262626` 底线分隔

## 数据库核心设计

### 表结构（7 表，符合 3NF）

**Customer（客户表）**
| 字段 | 类型 | 约束 |
|------|------|------|
| customer_id | INT | PK, AUTO_INCREMENT |
| customer_name | VARCHAR | NOT NULL |
| phone | CHAR(11) | UNIQUE, NOT NULL |
| address | VARCHAR | NOT NULL |

**Product（商品表）**
| 字段 | 类型 | 约束 |
|------|------|------|
| product_id | INT | PK, AUTO_INCREMENT |
| brand | VARCHAR | NOT NULL |
| model | VARCHAR | NOT NULL |
| price | DECIMAL(10,2) | NOT NULL |
| stock | INT | NOT NULL, CHECK(stock >= 0) |
| category | ENUM | '笔记本','台式机整机','DIY配件' |

**Sales_Order（订单主表）**
| 字段 | 类型 | 约束 |
|------|------|------|
| order_id | INT | PK, AUTO_INCREMENT |
| order_no | VARCHAR(50) | UNIQUE, NOT NULL |
| customer_id | INT | FK -> Customer |
| order_date | DATETIME | NOT NULL |
| total_amount | DECIMAL(10,2) | NOT NULL |
| status | ENUM | '待付款','已付款','已发货','已取消' |

**Order_Detail（订单明细表）**
| 字段 | 类型 | 约束 |
|------|------|------|
| detail_id | INT | PK, AUTO_INCREMENT |
| order_id | INT | FK -> Sales_Order(ON DELETE CASCADE) |
| product_id | INT | FK -> Product |
| quantity | INT | NOT NULL, > 0 |
| unit_price | DECIMAL(10,2) | NOT NULL |

**Laptop_Detail（笔记本详情，1:1 垂直拆分）**
| 字段 | 类型 | 约束 |
|------|------|------|
| laptop_id | INT | PK, AUTO_INCREMENT |
| product_id | INT | FK -> Product(ON DELETE CASCADE), UNIQUE |
| screen_size | VARCHAR(50) | — |
| cpu_model | VARCHAR(50) | — |
| gpu_model | VARCHAR(50) | — |
| weight | VARCHAR(20) | — |

**Spare_Part_Detail（DIY配件详情，多对一从属）**
| 字段 | 类型 | 约束 |
|------|------|------|
| part_id | INT | PK, AUTO_INCREMENT |
| product_id | INT | FK -> Product(ON DELETE CASCADE) |
| part_type | ENUM | 'CPU','显卡','主板','内存','硬盘','电源','机箱','散热器' |
| specification | VARCHAR(255) | — |

### 索引策略

- **聚簇索引**：所有主键自动建立，数据物理按主键顺序存储
- **唯一索引**：`Customer.phone` — 保证唯一性 + O(1) 查询
- **非聚簇索引**：`Order_Detail.order_id`、`Order_Detail.product_id` — 加速 JOIN/WHERE

### 事务隔离级别

`REPEATABLE READ`（InnoDB 默认），防止幻读。

### 并发控制（关键）

下单减库存时使用 **悲观锁**：
```sql
SELECT stock FROM Product WHERE product_id = ? FOR UPDATE;
```
- `FOR UPDATE` 加排他锁（X 锁），防止超卖
- 整个下单事务封装在存储过程 `sp_create_order` 中

### 日志策略

- `innodb_flush_log_at_trx_commit = 1` — 每次提交刷盘，保证持久性
- Undo Log — 回滚时恢复数据，保证原子性

## 后端架构约定

- **不直接拼接复杂 SQL**，业务逻辑通过调用 MySQL **存储过程** 执行
- 存储过程返回状态码：`0` 成功、`1` 库存不足、`2` 商品不存在、`3` 系统异常
- RESTful API 将状态码封装为 JSON 返回前端
- 数据源管理使用 HikariCP 连接池
- 使用 MyBatis（非 JPA），Mapper 接口 + 注解方式调用存储过程
- ⚠️ **MySQL 陷阱**：`SELECT ... INTO` 查不到行时变量不会设为 NULL，保持原默认值。`sp_create_order` 中必须用 `ROW_COUNT() = 0` 判断商品是否存在

## 前端交互约定

- 用户操作流程：选择客户 → 选择商品 → 输入数量 → 提交订单
- 使用 Axios 发送异步 POST 请求
- 结果通过弹窗（Notification/Message）反馈
- 下单成功后动态刷新库存列表
- ⚠️ `sp_create_order` 只支持单商品下单，前端购物清单模式需后端适配

## 实体关系（E-R）

```
Customer (1) ──→ (N) Sales_Order (1) ──→ (N) Order_Detail (N) ←── (1) Product
                                    Product (1) ──→ (1) Laptop_Detail
                                    Product (1) ──→ (N) Spare_Part_Detail
```

- Customer : Sales_Order = 1 : N
- Sales_Order : Product = M : N（通过 Order_Detail 拆解为两个 1:N）
- Product : Laptop_Detail = 1 : 1（笔记本通过 product_id 关联详情）
- Product : Spare_Part_Detail = 1 : N（一个配件商品只有一条规格）
