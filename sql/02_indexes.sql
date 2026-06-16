USE computer_sales_db;

-- 订单明细表：加速 JOIN/WHERE 查询
CREATE INDEX idx_detail_order_id   ON Order_Detail(order_id);
CREATE INDEX idx_detail_product_id ON Order_Detail(product_id);

-- 商品表：加速分类筛选
CREATE INDEX idx_product_category ON Product(category);
