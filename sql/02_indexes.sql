-- ============================================================
-- 02 非聚簇（二级）索引
-- 主键已是聚簇索引，此处针对高频的 JOIN / WHERE / ORDER BY 列补建二级索引，
-- 以空间换查询时间。须在 01 建表之后执行。
-- ============================================================
USE computer_sales_db;

-- 订单明细表：加速 JOIN/WHERE 查询
CREATE INDEX idx_detail_order_id   ON Order_Detail(order_id);
CREATE INDEX idx_detail_product_id ON Order_Detail(product_id);

-- 商品表：加速分类筛选
CREATE INDEX idx_product_category ON Product(category);

-- 订单主表：加速按客户查询订单
CREATE INDEX idx_order_customer ON Sales_Order(customer_id);

-- 订单主表：加速按状态筛选
CREATE INDEX idx_order_status ON Sales_Order(status);

-- 订单主表：加速按下单时间排序
CREATE INDEX idx_order_date ON Sales_Order(order_date DESC);
