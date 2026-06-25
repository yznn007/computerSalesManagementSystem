-- 每个测试方法前重置数据（@Sql 执行，纯 JDBC 支持分号分隔）
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE Order_Detail;
TRUNCATE TABLE Sales_Order;
SET FOREIGN_KEY_CHECKS=1;
-- 复位商品库存到种子值（product_id 1/2/3 初始库存 10/5/3）
UPDATE Product SET stock = CASE product_id
    WHEN 1 THEN 10
    WHEN 2 THEN 5
    WHEN 3 THEN 3
    ELSE stock
END WHERE product_id IN (1,2,3);