DROP DATABASE IF EXISTS computer_sales_db;

CREATE DATABASE computer_sales_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE computer_sales_db;

-- 客户表（兼任操作员/销售员，登录账户与客户一对一绑定）
CREATE TABLE Customer (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '客户编号',
    customer_name VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone         CHAR(11)     NOT NULL UNIQUE COMMENT '手机号',
    address       VARCHAR(200) NOT NULL COMMENT '收货地址'
) ENGINE=InnoDB COMMENT='客户/操作员表';

-- 商品表（增加分类字段）
CREATE TABLE Product (
    product_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品编号',
    brand      VARCHAR(50)    NOT NULL COMMENT '品牌',
    model      VARCHAR(100)   NOT NULL COMMENT '型号',
    price      DECIMAL(10,2)  NOT NULL COMMENT '售价',
    stock      INT            NOT NULL DEFAULT 0 COMMENT '库存量',
    category   ENUM('笔记本', '台式机整机', 'DIY配件') NOT NULL COMMENT '商品分类',
    CONSTRAINT chk_stock CHECK (stock >= 0)
) ENGINE=InnoDB COMMENT='商品表';

-- 笔记本详情表（与 Product 一对一垂直拆分）
CREATE TABLE Laptop_Detail (
    laptop_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '笔记本详情编号',
    product_id  INT UNIQUE NOT NULL COMMENT '商品编号',
    screen_size VARCHAR(50)  COMMENT '屏幕尺寸',
    cpu_model   VARCHAR(50)  COMMENT '处理器型号',
    gpu_model   VARCHAR(50)  COMMENT '显卡型号',
    weight      VARCHAR(20)  COMMENT '重量',
    CONSTRAINT fk_laptop_product FOREIGN KEY (product_id)
        REFERENCES Product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='笔记本详情表';

-- DIY配件详情表（与 Product 多对一从属）
CREATE TABLE Spare_Part_Detail (
    part_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '配件编号',
    product_id   INT NOT NULL COMMENT '商品编号',
    part_type    ENUM('CPU', '显卡', '主板', '内存', '硬盘', '电源', '机箱', '散热器') NOT NULL COMMENT '配件类型',
    specification VARCHAR(255) COMMENT '规格参数',
    CONSTRAINT fk_spare_product FOREIGN KEY (product_id)
        REFERENCES Product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='DIY配件详情表';

-- 订单主表（增加业务单号 order_no）
CREATE TABLE Sales_Order (
    order_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单编号',
    order_no     VARCHAR(50)   NOT NULL UNIQUE COMMENT '业务单号',
    customer_id  INT           NOT NULL COMMENT '客户编号',
    order_date   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status       ENUM('待付款', '已付款', '已发货', '已取消') NOT NULL DEFAULT '待付款' COMMENT '订单状态',
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
) ENGINE=InnoDB COMMENT='订单主表';

-- 订单明细表（外键级联删除）
CREATE TABLE Order_Detail (
    detail_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '明细编号',
    order_id   INT           NOT NULL COMMENT '订单编号',
    product_id INT           NOT NULL COMMENT '商品编号',
    quantity   INT           NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
    CONSTRAINT fk_detail_order   FOREIGN KEY (order_id)   REFERENCES Sales_Order(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES Product(product_id),
    CONSTRAINT chk_quantity CHECK (quantity > 0)
) ENGINE=InnoDB COMMENT='订单明细表';
