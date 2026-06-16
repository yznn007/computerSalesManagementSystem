SET NAMES utf8mb4;
USE computer_sales_db;

-- 客户数据（兼任操作员/销售员）
INSERT INTO Customer (customer_name, phone, address) VALUES
('张三', '13800138001', '北京市海淀区中关村大街1号'),
('李四', '13900139002', '上海市浦东新区张江高科技园区');

-- 商品数据（含分类）
INSERT INTO Product (brand, model, price, stock, category) VALUES
('Lenovo', 'ThinkPad X1 Carbon', 8999.00, 50, '笔记本'),
('Apple', 'MacBook Pro 14', 14999.00, 30, '笔记本'),
('ASUS', 'ROG 魔霸7 Plus', 10999.00, 20, '笔记本'),
('Dell', 'XPS 15', 7999.00, 15, '笔记本'),
('Lenovo', '天逸510S', 3999.00, 25, '台式机整机'),
('HP', '暗影精灵8 台式', 6999.00, 10, '台式机整机'),
('AMD', 'Ryzen 7 7800X3D', 2599.00, 40, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4070', 4799.00, 30, 'DIY配件'),
('ASUS', 'ROG STRIX B650-A', 1499.00, 35, 'DIY配件'),
('Corsair', 'Vengeance DDR5 32GB', 899.00, 60, 'DIY配件'),
('Samsung', '990 PRO 2TB NVMe', 1299.00, 45, 'DIY配件');

-- 笔记本详情
INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight) VALUES
(1, '14英寸 2.8K OLED', 'Intel Ultra 7 155H', 'Intel Arc', '1.12kg'),
(2, '14.2英寸 Liquid Retina XDR', 'Apple M4 Pro', '集成 20 核 GPU', '1.55kg'),
(3, '17.3英寸 2.5K 240Hz', 'AMD Ryzen 9 7945HX', 'RTX 4080', '3.0kg'),
(4, '15.6英寸 3.5K OLED', 'Intel Ultra 9 185H', 'RTX 4060', '1.86kg');

-- 台式机整机详情（也存为商品记录，此处无子表，brand/model已注明配置）
-- 注：5,6 为台式机整机，配置信息在 Product.brand/model 中体现

-- DIY配件详情
INSERT INTO Spare_Part_Detail (product_id, part_type, specification) VALUES
(7, 'CPU',       '8核16线程 / 4.2GHz基频 / AM5接口 / 120W TDP'),
(8, '显卡',      '12GB GDDR6X / 192-bit / PCIe 4.0 / 200W'),
(9, '主板',      'AM5 / ATX / 4×DDR5 / PCIe 5.0 / WiFi 6E'),
(10, '内存',     'DDR5 6000MHz / CL30 / 2×16GB 套条 / XMP 3.0'),
(11, '硬盘',     'M.2 NVMe PCIe 4.0 / 读7450MB/s 写6900MB/s');
