# 电脑销售管理系统 — 数据库技术文档

## 1. 概述

本文档描述电脑销售管理系统的数据库设计，涵盖表结构、关系、索引、存储过程及测试数据。

- **数据库**：MySQL 8.x（InnoDB 存储引擎）
- **事务隔离级别**：REPEATABLE READ
- **字符集**：utf8mb4 / utf8mb4_unicode_ci

---

## 2. 实体关系

```
Customer (1) ──→ (N) Sales_Order (1) ──→ (N) Order_Detail (N) ←── (1) Product
                                    Product (1) ──→ (1) Laptop_Detail
                                    Product (1) ──→ (1) Desktop_Detail
                                    Product (1) ──→ (N) Spare_Part_Detail
                              Desktop_Detail (1) ──→ (N) Desktop_Composition (N) ←── (1) Spare_Part_Detail
```

---

## 3. 表结构

### 3.1 Customer — 客户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| customer_id | INT | PK, AUTO_INCREMENT | 客户编号 |
| customer_name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | UNIQUE, NOT NULL | 手机号 |
| address | VARCHAR(200) | NOT NULL | 收货地址 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.2 Product — 商品表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| product_id | INT | PK, AUTO_INCREMENT | 商品编号 |
| brand | VARCHAR(50) | NOT NULL | 品牌 |
| model | VARCHAR(100) | NOT NULL | 型号 |
| price | DECIMAL(10,2) | NOT NULL | 售价 |
| stock | INT | NOT NULL, CHECK(stock >= 0) | 库存量 |
| category | ENUM('笔记本','台式机整机','DIY配件') | NOT NULL | 商品分类 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.3 Laptop_Detail — 笔记本详情

与 Product 一对一垂直拆分。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| laptop_id | INT | PK, AUTO_INCREMENT | 详情编号 |
| product_id | INT | FK → Product, UNIQUE | 商品编号 |
| screen_size | VARCHAR(50) | | 屏幕尺寸 |
| cpu_model | VARCHAR(50) | | CPU 型号 |
| gpu_model | VARCHAR(50) | | GPU 型号 |
| weight | VARCHAR(20) | | 重量 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.4 Desktop_Detail — 台式机整机详情

与 Product 一对一垂直拆分。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| desktop_id | INT | PK, AUTO_INCREMENT | 详情编号 |
| product_id | INT | FK → Product, UNIQUE | 商品编号 |
| form_factor | VARCHAR(50) | | 机箱类型 |
| cpu_desc | VARCHAR(100) | | 处理器描述 |
| gpu_desc | VARCHAR(100) | | 显卡描述 |
| ram_desc | VARCHAR(100) | | 内存描述 |
| storage_desc | VARCHAR(100) | | 存储描述 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.5 Spare_Part_Detail — DIY 配件详情

与 Product 一对多从属。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| part_id | INT | PK, AUTO_INCREMENT | 配件编号 |
| product_id | INT | FK → Product | 商品编号 |
| part_type | ENUM('CPU','显卡','主板','内存','硬盘','电源','机箱','散热器') | NOT NULL | 配件类型 |
| specification | VARCHAR(255) | | 规格参数 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.6 Desktop_Composition — 台式机组装配置

连接台式机与配件，记录每台台式机由哪些配件组成、各多少个。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| composition_id | INT | PK, AUTO_INCREMENT | 配置编号 |
| product_id | INT | FK → Product | 台式机整机商品编号 |
| part_id | INT | FK → Spare_Part_Detail | 配件编号 |
| quantity | INT | NOT NULL, CHECK(> 0) | 数量 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.7 Sales_Order — 订单主表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| order_id | INT | PK, AUTO_INCREMENT | 订单编号 |
| order_no | VARCHAR(50) | UNIQUE, NOT NULL | 业务单号 |
| customer_id | INT | FK → Customer | 客户编号 |
| order_date | DATETIME | NOT NULL | 下单时间 |
| total_amount | DECIMAL(10,2) | NOT NULL | 订单总金额 |
| status | ENUM('待付款','已付款','已发货','已取消','已退货') | NOT NULL | 订单状态 |
| payment_method | ENUM('微信','支付宝','银行卡','货到付款') | | 支付方式 |
| payment_time | DATETIME | | 支付时间 |
| paid_amount | DECIMAL(10,2) | | 实付金额 |
| cancel_reason | VARCHAR(200) | | 取消原因 |
| cancel_time | DATETIME | | 取消时间 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 3.8 Order_Detail — 订单明细表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| detail_id | INT | PK, AUTO_INCREMENT | 明细编号 |
| order_id | INT | FK → Sales_Order (CASCADE) | 订单编号 |
| product_id | INT | FK → Product | 商品编号 |
| quantity | INT | NOT NULL, CHECK(> 0) | 购买数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 购买时单价 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

---

## 4. 关系总结

| 左表 | 右表 | 基数 | 外键字段 | 说明 |
|------|------|:----:|----------|------|
| Customer | Sales_Order | 1:N | customer_id | 一个客户可有多笔订单 |
| Sales_Order | Order_Detail | 1:N | order_id (CASCADE) | 一笔订单包含多个明细项 |
| Order_Detail | Product | N:1 | product_id | 多个订单明细可指向同一商品 |
| Product | Laptop_Detail | 1:1 | product_id (UNIQUE) | 一件笔记本商品对应一条配置记录 |
| Product | Desktop_Detail | 1:1 | product_id (UNIQUE) | 一件台式机商品对应一条配置记录 |
| Product | Spare_Part_Detail | 1:N | product_id | 一件 DIY 配件商品对应一条规格 |
| Desktop_Detail | Desktop_Composition | 1:N | product_id | 一台台式机对应多条组装配置 |
| Desktop_Composition | Spare_Part_Detail | N:1 | part_id | 多条配置可引用同一配件 |
| Product | Desktop_Composition | 1:N | product_id | 一件商品可作为台式机参与组装 |
| Spare_Part_Detail | Desktop_Composition | 1:N | part_id | 一个配件可被多台台式机使用 |

---

## 5. 索引策略

| 索引类型 | 所属表 | 字段 | 作用 |
|----------|--------|------|------|
| 聚簇索引（PK） | 所有表 | 主键 | 物理存储顺序 |
| 唯一索引 | Customer | phone | 手机号唯一性 + O(1) 查询 |
| 非聚簇索引 | Order_Detail | order_id | 加速 JOIN |
| 非聚簇索引 | Order_Detail | product_id | 加速 JOIN |
| 非聚簇索引 | Product | category | 加速分类筛选 |
| 非聚簇索引 | Sales_Order | customer_id | 按客户查询订单 |
| 非聚簇索引 | Sales_Order | status | 按状态筛选订单 |
| 非聚簇索引 | Sales_Order | order_date DESC | 按下单时间排序 |

---

## 6. 并发控制

下单时使用**悲观锁**防止超卖：

```sql
SELECT stock FROM Product WHERE product_id = ? FOR UPDATE;
```

- `FOR UPDATE` 对目标商品行施加排他锁（X 锁）
- 事务提交后释放锁
- 整个下单流程封装在存储过程 `sp_create_order` 中

日志策略：
- `innodb_flush_log_at_trx_commit = 1`（每次提交刷盘）
- Undo Log 保证原子性与回滚

---

## 7. 存储过程

### 7.1 sp_create_order — 创建订单

支持一次下单多件商品（JSON 数组入参）。

```sql
CALL sp_create_order(
    1,                                    -- p_customer_id
    '[{"product_id": 1, "quantity": 2},
      {"product_id": 7, "quantity": 1}]', -- p_items
    @status,
    @order_no
);
```

**状态码：**

| 状态码 | 含义 |
|:------:|------|
| 0 | 成功 |
| 1 | 数量不合法（≤ 0 或空数组） |
| 2 | 商品不存在 |
| 3 | 库存不足 |
| 4 | 系统异常 |

### 7.2 sp_search_products — 搜索商品

按关键词（品牌/型号）和分类筛选商品，同时返回各分类子表的详情。

```sql
CALL sp_search_products('ThinkPad', '笔记本');
```

### 7.3 sp_get_customer_orders — 客户订单列表

```sql
CALL sp_get_customer_orders(1);
```

### 7.4 sp_get_order_detail — 订单明细

```sql
CALL sp_get_order_detail(1);
```

---

## 8. 测试数据

| 类型 | 条数 | 说明 |
|------|:----:|------|
| 客户 | 2 | 张三（北京）、李四（上海） |
| 商品 | 11 | 笔记本 4 / 台式机整机 2 / DIY 配件 5 |
| 笔记本详情 | 4 | ThinkPad / MacBook / ROG / XPS |
| 台式机详情 | 2 | 天逸510S / 暗影精灵8 |
| 配件详情 | 5 | CPU / 显卡 / 主板 / 内存 / 硬盘 |
| 组装配置 | 5 | 天逸510S 配 3 种配件 + 暗影精灵8 配 2 种配件 |

---

## 9. 初始化顺序

```bash
mysql -u root -proot --default-character-set=utf8mb4

source sql/01_schema.sql    # 建库 + 建表
source sql/02_indexes.sql   # 非聚簇索引
source sql/03_procedures.sql # 存储过程
source sql/04_test_data.sql # 测试数据
```

> `--default-character-set=utf8mb4` 必须指定，否则中文数据会乱码。
