-- ============================================================
-- 测试用 DDL + 种子（分号分隔，Testcontainers withInitScript 走 JDBC 可用）
-- 存储过程见 test-procedures.sql，密码占位符由 DataInitializer 替换
-- ============================================================

CREATE TABLE Customer (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(50)  NOT NULL,
    phone         VARCHAR(20)  NOT NULL UNIQUE,
    address       VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE Staff (
    staff_id      INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    staff_name    VARCHAR(50)  NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE Product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    brand      VARCHAR(50)    NOT NULL,
    model      VARCHAR(100)   NOT NULL,
    price      DECIMAL(10,2)  NOT NULL,
    stock      INT            NOT NULL DEFAULT 0,
    category   ENUM('笔记本', '台式机整机', 'DIY配件') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_brand_model UNIQUE (brand, model),
    CONSTRAINT chk_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE Laptop_Detail (
    laptop_id   INT AUTO_INCREMENT PRIMARY KEY,
    product_id  INT UNIQUE NOT NULL,
    screen_size VARCHAR(50),
    cpu_model   VARCHAR(50),
    gpu_model   VARCHAR(50),
    weight      VARCHAR(20),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_laptop_product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Desktop_Detail (
    desktop_id   INT AUTO_INCREMENT PRIMARY KEY,
    product_id   INT UNIQUE NOT NULL,
    form_factor  VARCHAR(50),
    cpu_desc     VARCHAR(100),
    gpu_desc     VARCHAR(100),
    ram_desc     VARCHAR(100),
    storage_desc VARCHAR(100),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_desktop_product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Spare_Part_Detail (
    part_id       INT AUTO_INCREMENT PRIMARY KEY,
    product_id    INT NOT NULL,
    part_type     ENUM('CPU', '显卡', '主板', '内存', '硬盘', '电源', '机箱', '散热器') NOT NULL,
    specification VARCHAR(255),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_spare_product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Desktop_Composition (
    composition_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id     INT NOT NULL,
    part_id        INT NOT NULL,
    quantity       INT NOT NULL DEFAULT 1,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_composition_product FOREIGN KEY (product_id) REFERENCES Product(product_id) ON DELETE CASCADE,
    CONSTRAINT fk_composition_part FOREIGN KEY (part_id) REFERENCES Spare_Part_Detail(part_id),
    CONSTRAINT chk_composition_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE Sales_Order (
    order_id       INT AUTO_INCREMENT PRIMARY KEY,
    order_no       VARCHAR(50)   NOT NULL UNIQUE,
    customer_id    INT           NOT NULL,
    order_date     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount   DECIMAL(10,2) NOT NULL,
    status         ENUM('待付款', '已付款', '已发货', '已取消', '已退货') NOT NULL DEFAULT '待付款',
    payment_method ENUM('微信', '支付宝', '银行卡', '货到付款'),
    payment_time   DATETIME,
    paid_amount    DECIMAL(10,2),
    cancel_reason  VARCHAR(200),
    cancel_time    DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
) ENGINE=InnoDB;

CREATE TABLE Order_Detail (
    detail_id  INT AUTO_INCREMENT PRIMARY KEY,
    order_id   INT           NOT NULL,
    product_id INT           NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_detail_order   FOREIGN KEY (order_id)   REFERENCES Sales_Order(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES Product(product_id),
    CONSTRAINT chk_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE INDEX idx_detail_order_id   ON Order_Detail(order_id);
CREATE INDEX idx_detail_product_id ON Order_Detail(product_id);
CREATE INDEX idx_product_category ON Product(category);
CREATE INDEX idx_order_customer ON Sales_Order(customer_id);
CREATE INDEX idx_order_status ON Sales_Order(status);
CREATE INDEX idx_order_date ON Sales_Order(order_date DESC);

-- 种子数据：密码占位符由 DataInitializer 启动时替换为 BCrypt
INSERT INTO Customer (customer_name, phone, address, password_hash) VALUES
('张三', '13800138001', '北京市海淀区中关村大街1号', '__SEED_123456__'),
('李四', '13900139002', '上海市浦东新区张江高科技园区', '__SEED_123456__');

INSERT INTO Staff (username, password_hash, staff_name) VALUES
('admin', '__SEED_admin__', '山田小姐');

INSERT INTO Product (brand, model, price, stock, category) VALUES
('Lenovo', 'ThinkPad X1 Carbon', 8999.00, 10, '笔记本'),
('Dell',   'OptiPlex 7090',      5999.00,  5, '台式机整机'),
('Intel',  'i9-14900K',          3999.00,  3, 'DIY配件');

INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight)
SELECT product_id, '14英寸 2.8K OLED', 'Intel Ultra 7 155H', 'Intel Arc', '1.12kg'
FROM Product WHERE brand='Lenovo' AND model='ThinkPad X1 Carbon';

INSERT INTO Desktop_Detail (product_id, form_factor, cpu_desc, gpu_desc, ram_desc, storage_desc)
SELECT product_id, '塔式', 'Intel i7-14700', 'Intel UHD 770', '32GB DDR5', '1TB NVMe'
FROM Product WHERE brand='Dell' AND model='OptiPlex 7090';

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT product_id, 'CPU', '24核32线程 / 6.0GHz / LGA1700'
FROM Product WHERE brand='Intel' AND model='i9-14900K';
