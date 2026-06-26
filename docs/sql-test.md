# 电脑销售管理系统 — SQL 直连测试手册

> 不依赖后端 / 前端，直接用 `mysql` 客户端逐条执行 SQL 验证数据库（建表、约束、存储过程、状态机、悲观锁、索引、统计）。
>
> 目标库：`computer_sales_db`（MySQL 8.x，InnoDB，utf8mb4，账号 `root/root`）。

---

## 0. 连接与初始化

```bash
# ⚠ --default-character-set=utf8mb4 必加，否则中文乱码
mysql -u root -proot --default-character-set=utf8mb4
```

```sql
-- 按编号顺序执行建库脚本（首次或需要重置时）
SOURCE sql/01_schema.sql;            -- 建库 + 9 表
SOURCE sql/02_indexes.sql;           -- 二级索引
SOURCE sql/03_procedures.sql;        -- 下单/搜索/订单查询过程
SOURCE sql/04_status_procedures.sql; -- 订单状态机过程
SOURCE sql/05_test_data.sql;         -- 种子数据

USE computer_sales_db;
```

> ⚠ `05_test_data.sql` 中密码为 `__SEED_<明文>__` 占位符，需后端 `DataInitializer` 启动时替换为 BCrypt 哈希；纯 SQL 测试不影响业务表数据。

---

## 1. 结构核对（DDL）

```sql
-- 列出全部 9 张表
SHOW TABLES;

-- 查看列定义与注释
DESC Customer;
SHOW FULL COLUMNS FROM Order_Detail;

-- 查看外键约束
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'computer_sales_db' AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 查看索引
SHOW INDEX FROM Sales_Order;

-- 查看已创建的存储过程
SHOW PROCEDURE STATUS WHERE Db = 'computer_sales_db';
```

---

## 2. 基础查询（SELECT）

```sql
-- 各表行数
SELECT 'Customer' t, COUNT(*) n FROM Customer
UNION ALL SELECT 'Staff',    COUNT(*) FROM Staff
UNION ALL SELECT 'Product',  COUNT(*) FROM Product
UNION ALL SELECT 'Sales_Order',  COUNT(*) FROM Sales_Order
UNION ALL SELECT 'Order_Detail', COUNT(*) FROM Order_Detail;

-- 按分类统计商品数与均价
SELECT category, COUNT(*) AS cnt, ROUND(AVG(price),2) AS avg_price, SUM(stock) AS total_stock
FROM Product GROUP BY category;

-- 笔记本详情（一对一 JOIN）
SELECT p.product_id, p.brand, p.model, p.price, ld.cpu_model, ld.gpu_model
FROM Product p JOIN Laptop_Detail ld ON p.product_id = ld.product_id;

-- 台式机 BOM（整机 → 配件多对多）
SELECT p.model AS 整机, sp.part_type, sp2.model AS 配件, dc.quantity
FROM Desktop_Composition dc
JOIN Product p   ON dc.product_id = p.product_id
JOIN Spare_Part_Detail sp ON dc.part_id = sp.part_id
JOIN Product sp2 ON sp.product_id = sp2.product_id;

-- 价格区间筛选 + 排序 + 分页
SELECT product_id, brand, model, price FROM Product
WHERE price BETWEEN 5000 AND 10000
ORDER BY price DESC LIMIT 5 OFFSET 0;

-- 模糊搜索
SELECT product_id, brand, model FROM Product WHERE brand LIKE '%Lenovo%' OR model LIKE '%Pro%';
```

---

## 3. 增删改（INSERT / UPDATE / DELETE）

```sql
-- 新增客户
INSERT INTO Customer (customer_name, phone, address, password_hash)
VALUES ('测试用户', '13000000000', '测试地址', 'plain_for_test');
SELECT * FROM Customer WHERE phone = '13000000000';

-- 新增商品 + 笔记本详情（一对一）
INSERT INTO Product (brand, model, price, stock, category)
VALUES ('TestBrand', 'TestModel-X', 6666.00, 100, '笔记本');
SET @pid = LAST_INSERT_ID();
INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight)
VALUES (@pid, '15.6英寸', 'Test CPU', 'Test GPU', '1.5kg');

-- 更新（改价 / 改库存 / 改地址）
UPDATE Product SET price = 5999.00, stock = 80 WHERE product_id = @pid;
UPDATE Customer SET address = '更新后的地址' WHERE phone = '13000000000';

-- 验证 updated_at 自动刷新（应晚于 created_at）
SELECT created_at, updated_at FROM Product WHERE product_id = @pid;

-- 删除（CASCADE：删 Product 会连带删 Laptop_Detail）
DELETE FROM Product WHERE product_id = @pid;
SELECT COUNT(*) AS should_be_0 FROM Laptop_Detail WHERE product_id = @pid;
DELETE FROM Customer WHERE phone = '13000000000';
```

---

## 4. 约束测试（应全部报错 / 被拒绝）

```sql
-- 4.1 UNIQUE：手机号重复 → 报 Duplicate entry
INSERT INTO Customer (customer_name, phone, address, password_hash)
VALUES ('重复手机', '13800138001', '地址', 'x');

-- 4.2 UNIQUE：商品 brand+model 组合重复 → 报错
INSERT INTO Product (brand, model, price, stock, category)
VALUES ('Lenovo', 'ThinkPad X1 Carbon', 1.00, 1, '笔记本');

-- 4.3 CHECK：库存为负 → 违反 chk_stock
INSERT INTO Product (brand, model, price, stock, category)
VALUES ('Neg', 'Stock', 10.00, -5, '笔记本');

-- 4.4 ENUM：非法分类 → 报错（Data truncated）
INSERT INTO Product (brand, model, price, stock, category)
VALUES ('Bad', 'Cat', 10.00, 1, '平板');

-- 4.5 外键：订单引用不存在客户 → 报 foreign key constraint fails
INSERT INTO Sales_Order (order_no, customer_id, total_amount)
VALUES ('ORDTEST', 999999, 100.00);

-- 4.6 CHECK：订单明细数量 <= 0 → 违反 chk_quantity
INSERT INTO Order_Detail (order_id, product_id, quantity, unit_price)
VALUES (1, 1, 0, 100.00);

-- 4.7 NOT NULL：缺收货地址 → 报错
INSERT INTO Customer (customer_name, phone, password_hash)
VALUES ('无地址', '13011110000', 'x');
```

---

## 5. 存储过程 — sp_create_order（下单）

> 状态码：`0` 成功 / `1` 数量不合法 / `2` 商品不存在 / `3` 库存不足 / `4` 系统异常。
> 入参 `p_items` 为 JSON 数组 `[{"product_id":N,"quantity":N}]`。

```sql
-- 先取一个种子客户与商品 id（避免硬编码自增值）
SELECT customer_id INTO @cid FROM Customer WHERE phone='13800138001';
SELECT product_id, stock INTO @p1, @stock1 FROM Product WHERE brand='Lenovo' AND model='ThinkPad X1 Carbon';

-- 5.1 正常下单（单商品）→ 期望 @st=0，返回单号
SET @items = CONCAT('[{"product_id":', @p1, ',"quantity":2}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT @st AS status, @no AS order_no;      -- status 应为 0
SELECT stock FROM Product WHERE product_id=@p1;  -- 库存应减 2

-- 5.2 多商品下单 → 期望 @st=0
SELECT product_id INTO @p2 FROM Product WHERE brand='Apple' AND model='MacBook Pro 14';
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":1},{"product_id":',@p2,',"quantity":1}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT @st AS status, @no AS order_no;

-- 5.3 数量不合法（quantity<=0）→ 期望 @st=1，不建单
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":0}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT @st AS status;     -- 应为 1

-- 5.4 商品不存在 → 期望 @st=2
CALL sp_create_order(@cid, '[{"product_id":999999,"quantity":1}]', @st, @no);
SELECT @st AS status;     -- 应为 2

-- 5.5 库存不足 → 期望 @st=3
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":99999}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT @st AS status;     -- 应为 3

-- 5.6 空数组 → 期望 @st=1
CALL sp_create_order(@cid, '[]', @st, @no);
SELECT @st AS status;     -- 应为 1

-- 5.7 验证单价快照：明细 unit_price = 下单时 Product.price
SELECT od.*, p.price AS current_price
FROM Order_Detail od JOIN Product p ON od.product_id=p.product_id
WHERE od.order_id = (SELECT MAX(order_id) FROM Sales_Order);
```

---

## 6. 存储过程 — 查询类

```sql
-- 6.1 商品搜索（关键词 + 分类，NULL 表示不筛选）
CALL sp_search_products('Lenovo', NULL);     -- 按品牌
CALL sp_search_products(NULL, '笔记本');      -- 按分类
CALL sp_search_products('Pro', '笔记本');     -- 组合
CALL sp_search_products(NULL, NULL);          -- 全部

-- 6.2 客户订单列表（按下单时间倒序）
CALL sp_get_customer_orders(@cid);

-- 6.3 订单详情（含商品信息）
SELECT MAX(order_id) INTO @oid FROM Sales_Order;
CALL sp_get_order_detail(@oid);
```

---

## 7. 存储过程 — sp_update_order_status（订单状态机）

> `p_action`：`pay` | `ship` | `cancel` | `return`。
> 状态码：`0` 成功 / `1` 非法状态流转 / `2` 订单不存在 / `3` 系统异常。
> 合法流转：`待付款 →pay→ 已付款 →ship→ 已发货 →return→ 已退货`；`待付款 →cancel→ 已取消`。
> 取消 / 退货会**回补库存**。

```sql
-- 准备：新建一张待付款订单用于流转
SELECT customer_id INTO @cid FROM Customer WHERE phone='13800138001';
SELECT product_id INTO @p1 FROM Product WHERE brand='Dell' AND model='XPS 15';
SELECT stock INTO @stock_before FROM Product WHERE product_id=@p1;
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":3}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT order_id INTO @oid FROM Sales_Order WHERE order_no=@no;

-- 7.1 付款：待付款 → 已付款（期望 @st=0，@msg='已付款'）
CALL sp_update_order_status(@oid, 'pay', '微信', NULL, @st, @msg);
SELECT @st, @msg;
SELECT status, payment_method, payment_time, paid_amount FROM Sales_Order WHERE order_id=@oid;

-- 7.2 发货：已付款 → 已发货（期望 @st=0）
CALL sp_update_order_status(@oid, 'ship', NULL, NULL, @st, @msg);
SELECT @st, @msg;

-- 7.3 退货：已发货 → 已退货（期望 @st=0，库存回补）
CALL sp_update_order_status(@oid, 'return', NULL, NULL, @st, @msg);
SELECT @st, @msg;
SELECT @stock_before AS before, stock AS after FROM Product WHERE product_id=@p1;  -- after 应等于 before

-- 7.4 非法流转：对已退货订单再付款（期望 @st=1）
CALL sp_update_order_status(@oid, 'pay', '微信', NULL, @st, @msg);
SELECT @st, @msg;     -- @st=1，@msg 含"不可付款"

-- 7.5 订单不存在（期望 @st=2）
CALL sp_update_order_status(999999, 'pay', '微信', NULL, @st, @msg);
SELECT @st, @msg;     -- @st=2

-- 7.6 取消流程：新建待付款订单 → cancel（期望 @st=0，库存回补）
SELECT stock INTO @sb FROM Product WHERE product_id=@p1;
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":2}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT order_id INTO @oid2 FROM Sales_Order WHERE order_no=@no;
CALL sp_update_order_status(@oid2, 'cancel', NULL, '不想要了', @st, @msg);
SELECT @st, @msg;
SELECT cancel_reason, cancel_time, status FROM Sales_Order WHERE order_id=@oid2;
SELECT @sb AS before, stock AS after FROM Product WHERE product_id=@p1;  -- 应相等

-- 7.7 非法流转：待付款直接发货（期望 @st=1）
SET @items = CONCAT('[{"product_id":',@p1,',"quantity":1}]');
CALL sp_create_order(@cid, @items, @st, @no);
SELECT order_id INTO @oid3 FROM Sales_Order WHERE order_no=@no;
CALL sp_update_order_status(@oid3, 'ship', NULL, NULL, @st, @msg);
SELECT @st, @msg;     -- @st=1，"不可发货"

-- 7.8 未知动作（期望 @st=1，@msg='未知操作'）
CALL sp_update_order_status(@oid3, 'foo', NULL, NULL, @st, @msg);
SELECT @st, @msg;
```

---

## 8. 悲观锁 / 防超卖验证（需开两个会话）

> `sp_create_order` 内 `SELECT stock ... FOR UPDATE` 对商品行加行级排他锁，并发下单不会超卖。

```sql
-- 【会话 A】手动模拟过程内的加锁逻辑
SELECT product_id INTO @p FROM Product WHERE brand='Razer' AND model='灵刃 16';  -- 库存仅 4
START TRANSACTION;
SELECT stock FROM Product WHERE product_id=@p FOR UPDATE;   -- 持锁，暂不提交
```

```sql
-- 【会话 B】同一时刻执行（会阻塞，直到会话 A 提交/回滚）
START TRANSACTION;
SELECT stock FROM Product WHERE product_id = (SELECT product_id FROM Product WHERE brand='Razer' AND model='灵刃 16') FOR UPDATE;
-- 观察：此查询被阻塞 → 证明行锁生效
```

```sql
-- 【会话 A】释放锁
COMMIT;   -- 会话 B 立即解除阻塞
```

```sql
-- 8.1 并发超卖功能性验证：连续下单直到库存耗尽，再下一单应返回 @st=3
SELECT customer_id INTO @cid FROM Customer WHERE phone='13800138001';
SELECT product_id, stock INTO @p, @s FROM Product WHERE brand='Razer' AND model='灵刃 16';
SET @items = CONCAT('[{"product_id":',@p,',"quantity":',@s,'}]');
CALL sp_create_order(@cid, @items, @st, @no);   -- 买空，@st=0
SELECT stock FROM Product WHERE product_id=@p;  -- 应为 0
CALL sp_create_order(@cid, CONCAT('[{"product_id":',@p,',"quantity":1}]'), @st, @no);
SELECT @st;   -- 应为 3（库存不足）
```

---

## 9. 事务与回滚

```sql
-- 9.1 手动事务回滚：扣库存后回滚，库存应复原
SELECT product_id, stock INTO @p, @s0 FROM Product WHERE brand='HP' AND model='OMEN 16';
START TRANSACTION;
UPDATE Product SET stock = stock - 5 WHERE product_id=@p;
SELECT stock AS in_tx FROM Product WHERE product_id=@p;   -- 已减 5
ROLLBACK;
SELECT @s0 AS before, stock AS after FROM Product WHERE product_id=@p;  -- after 应等于 before

-- 9.2 SAVEPOINT 部分回滚
START TRANSACTION;
UPDATE Product SET price = price + 100 WHERE product_id=@p;
SAVEPOINT sp1;
UPDATE Product SET price = price + 999 WHERE product_id=@p;
ROLLBACK TO sp1;     -- 撤销 +999，保留 +100
COMMIT;
```

---

## 10. 索引验证（EXPLAIN）

```sql
-- 10.1 分类筛选走 idx_product_category
EXPLAIN SELECT * FROM Product WHERE category='笔记本';

-- 10.2 按客户查订单走 idx_order_customer
EXPLAIN SELECT * FROM Sales_Order WHERE customer_id=1;

-- 10.3 按状态筛选走 idx_order_status
EXPLAIN SELECT * FROM Sales_Order WHERE status='待付款';

-- 10.4 明细 JOIN 走 idx_detail_order_id
EXPLAIN SELECT * FROM Order_Detail WHERE order_id=1;

-- 观察 key 列是否命中对应索引，type 应为 ref / range 而非 ALL
```

---

## 11. 统计分析（销售看板类查询）

```sql
-- 11.1 订单状态分布
SELECT status, COUNT(*) AS cnt, SUM(total_amount) AS amount
FROM Sales_Order GROUP BY status;

-- 11.2 销售额（仅计已付款及之后状态）
SELECT SUM(total_amount) AS gmv
FROM Sales_Order WHERE status IN ('已付款','已发货','已退货');

-- 11.3 热销商品 TOP5（按销量）
SELECT p.brand, p.model, SUM(od.quantity) AS sold
FROM Order_Detail od JOIN Product p ON od.product_id=p.product_id
GROUP BY p.product_id ORDER BY sold DESC LIMIT 5;

-- 11.4 客户消费排行
SELECT c.customer_name, COUNT(o.order_id) AS orders, IFNULL(SUM(o.total_amount),0) AS spent
FROM Customer c LEFT JOIN Sales_Order o ON c.customer_id=o.customer_id
GROUP BY c.customer_id ORDER BY spent DESC;

-- 11.5 低库存预警（库存 < 10）
SELECT product_id, brand, model, stock FROM Product WHERE stock < 10 ORDER BY stock;

-- 11.6 按日统计下单量
SELECT DATE(order_date) AS day, COUNT(*) AS orders, SUM(total_amount) AS amount
FROM Sales_Order GROUP BY DATE(order_date) ORDER BY day DESC;
```

---

## 12. 级联删除验证

```sql
-- 删订单 → 明细随 ON DELETE CASCADE 一起删除
SELECT order_id INTO @oid FROM Sales_Order ORDER BY order_id DESC LIMIT 1;
SELECT COUNT(*) AS detail_before FROM Order_Detail WHERE order_id=@oid;
DELETE FROM Sales_Order WHERE order_id=@oid;
SELECT COUNT(*) AS detail_after_should_be_0 FROM Order_Detail WHERE order_id=@oid;
```

---

## 13. 清理测试数据（可选）

```sql
-- 删除本手册产生的测试订单 / 客户（按需调整）
DELETE FROM Sales_Order WHERE customer_id=(SELECT customer_id FROM Customer WHERE phone='13800138001');
DELETE FROM Customer WHERE phone IN ('13000000000','13011110000');
DELETE FROM Product WHERE brand='TestBrand';

-- 或彻底重置：重新执行 01~05 脚本（见第 0 节）
```

---

## 附录：预期状态码速查

| 过程 | 状态码 | 含义 |
|------|--------|------|
| `sp_create_order` | 0 | 成功 |
| | 1 | 数量不合法 / 空数组 |
| | 2 | 商品不存在 |
| | 3 | 库存不足 |
| | 4 | 系统异常 |
| `sp_update_order_status` | 0 | 成功（`o_message` 回传目标状态） |
| | 1 | 非法状态流转 / 未知动作 |
| | 2 | 订单不存在 |
| | 3 | 系统异常 |

**订单状态机**

```
待付款 ──pay──→ 已付款 ──ship──→ 已发货 ──return──→ 已退货
   │                                （回补库存）
   └──cancel──→ 已取消（回补库存）
```
