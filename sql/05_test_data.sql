SET NAMES utf8mb4;
USE computer_sales_db;

-- ============================================================
-- 种子数据（单文件 · 按品类分区 · 纯追加幂等）
-- 精简范式（依赖 Product 的 UNIQUE(brand, model)）：
--   1) 商品主表：一条 INSERT IGNORE 批量插入，重复执行自动跳过
--   2) 详情子表：一条 INSERT ... SELECT，派生表(UNION ALL)按 brand+model
--      JOIN 回 Product 取 product_id，再 LEFT JOIN 子表防重
-- 重复执行本脚本不会产生重复数据，也不会清空已有客户/订单
-- 密码占位符 __SEED_<明文>__ 由后端 DataInitializer 启动时替换为 BCrypt 哈希
-- ============================================================


-- ============================================================
-- 一、账号（客户 + 销售员）
-- ============================================================

-- 客户（手机号为登录账号）
INSERT IGNORE INTO Customer (customer_name, phone, address, password_hash) VALUES
('张三',   '13800138001', '北京市海淀区中关村大街1号',   '__SEED_123456__'),
('李四',   '13900139002', '上海市浦东新区张江高科技园区', '__SEED_123456__'),
('王五',   '13700137003', '广州市天河区体育西路191号',   '__SEED_123456__'),
('赵六',   '13600136004', '深圳市南山区科技园南区',      '__SEED_123456__'),
('孙七',   '13500135005', '杭州市西湖区文三路269号',     '__SEED_123456__'),
('周八',   '13400134006', '成都市武侯区科华中路2号',     '__SEED_123456__'),
('佐佐木大叔', '18903503653', '应急管理大学',                '__SEED_159951__');

-- 销售员
INSERT IGNORE INTO Staff (username, password_hash, staff_name) VALUES
('admin',    '__SEED_admin__', '山田小姐'),
('liwei',    '__SEED_admin__', '李维'),
('zhangmin', '__SEED_admin__', '张敏');


-- ============================================================
-- 二、笔记本
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Lenovo', 'ThinkPad X1 Carbon',  8999.00,  50, '笔记本'),
('Apple',  'MacBook Pro 14',     14999.00,  30, '笔记本'),
('ASUS',   'ROG 魔霸7 Plus',     10999.00,  20, '笔记本'),
('Dell',   'XPS 15',              7999.00,  15, '笔记本'),
('HP',     'OMEN 16',             7499.00,  18, '笔记本'),
('HUAWEI', 'MateBook X Pro',      9999.00,  22, '笔记本'),
('Lenovo', '小新 Pro 16 2026',    6499.00,  20, '笔记本'),
('Lenovo', 'ThinkBook 16 2026',   5999.00,  18, '笔记本'),
('MSI',    '薄刃 16 AI Evo',      7999.00,  12, '笔记本'),
('MSI',    '泰坦 18 HX',         24999.00,   6, '笔记本'),
('Xiaomi', 'RedmiBook 14 2026',   3999.00,  25, '笔记本'),
('Xiaomi', 'Redmi G Pro 2026',    8999.00,  10, '笔记本'),
('Honor',  'MagicBook Pro 16 2026',6999.00, 15, '笔记本'),
('Razer',  '灵刃 14',            17999.00,   5, '笔记本'),
('Razer',  '灵刃 16',            22999.00,   4, '笔记本');

INSERT INTO Laptop_Detail (product_id, screen_size, cpu_model, gpu_model, weight)
SELECT p.product_id, d.screen_size, d.cpu_model, d.gpu_model, d.weight
FROM Product p
JOIN (
  SELECT 'Lenovo' AS brand, 'ThinkPad X1 Carbon' AS model, '14英寸 2.8K OLED' AS screen_size, 'Intel Ultra 7 155H' AS cpu_model, 'Intel Arc' AS gpu_model, '1.12kg' AS weight
  UNION ALL SELECT 'Apple',  'MacBook Pro 14',       '14.2英寸 Liquid Retina XDR', 'Apple M4 Pro',            '集成 20 核 GPU', '1.55kg'
  UNION ALL SELECT 'ASUS',   'ROG 魔霸7 Plus',       '17.3英寸 2.5K 240Hz',        'AMD Ryzen 9 7945HX',      'RTX 4080',       '3.0kg'
  UNION ALL SELECT 'Dell',   'XPS 15',               '15.6英寸 3.5K OLED',         'Intel Ultra 9 185H',      'RTX 4060',       '1.86kg'
  UNION ALL SELECT 'HP',     'OMEN 16',              '16.1英寸 2.5K 165Hz',        'Intel Core i7-13700H',    'RTX 4070',       '2.4kg'
  UNION ALL SELECT 'HUAWEI', 'MateBook X Pro',       '14.2英寸 3.1K 触控',         'Intel Core Ultra 9 185H', 'Intel Arc',      '1.26kg'
  UNION ALL SELECT 'Lenovo', '小新 Pro 16 2026',     '16英寸 2.5K 165Hz',          'Intel Core Ultra 7 255H', '集成 Arc',       '1.95kg'
  UNION ALL SELECT 'Lenovo', 'ThinkBook 16 2026',    '16英寸 2.5K 120Hz',          'Intel Core Ultra 5 225H', '集成 Arc',       '1.88kg'
  UNION ALL SELECT 'MSI',    '薄刃 16 AI Evo',       '16英寸 2.5K 165Hz',          'Intel Core Ultra 7 155H', '集成 Arc',       '1.9kg'
  UNION ALL SELECT 'MSI',    '泰坦 18 HX',           '18英寸 4K 120Hz Mini LED',   'Intel Core Ultra 9 285HX','RTX 4090',       '3.6kg'
  UNION ALL SELECT 'Xiaomi', 'RedmiBook 14 2026',    '14英寸 2.8K 120Hz',          'Intel Core Ultra 5 225H', '集成 Arc',       '1.35kg'
  UNION ALL SELECT 'Xiaomi', 'Redmi G Pro 2026',     '16英寸 2.5K 240Hz',          'Intel Core i9-14900HX',   'RTX 4070',       '2.6kg'
  UNION ALL SELECT 'Honor',  'MagicBook Pro 16 2026','16英寸 3.1K 165Hz',          'Intel Core Ultra 7 155H', '集成 Arc',       '1.79kg'
  UNION ALL SELECT 'Razer',  '灵刃 14',              '14英寸 QHD+ 240Hz',          'AMD Ryzen 9 8945HS',      'RTX 4070',       '1.84kg'
  UNION ALL SELECT 'Razer',  '灵刃 16',              '16英寸 QHD+ 240Hz OLED',     'Intel Core Ultra 9 185H', 'RTX 4080',       '2.5kg'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Laptop_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 三、台式机整机
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Lenovo', '天逸510S',       3999.00, 25, '台式机整机'),
('HP',     '暗影精灵8 台式', 6999.00, 10, '台式机整机'),
('ASUS',   'ROG 光魔G16CH',  8999.00,  8, '台式机整机'),
('Lenovo', 'GeekPro 2026',   5999.00, 12, '台式机整机'),
('Dell',   'OptiPlex 7020',  4999.00, 15, '台式机整机'),
('MSI',    'Infinite S3',    6499.00, 10, '台式机整机');

INSERT INTO Desktop_Detail (product_id, form_factor, cpu_desc, gpu_desc, ram_desc, storage_desc)
SELECT p.product_id, d.form_factor, d.cpu_desc, d.gpu_desc, d.ram_desc, d.storage_desc
FROM Product p
JOIN (
  SELECT 'Lenovo' AS brand, '天逸510S' AS model, 'M-ATX' AS form_factor, 'Intel i5-12400' AS cpu_desc, '集成 UHD 730' AS gpu_desc, '16GB DDR4' AS ram_desc, '512GB SSD' AS storage_desc
  UNION ALL SELECT 'HP',     '暗影精灵8 台式', 'ATX',   'Intel i7-12700F',    'RTX 3060',     '16GB DDR4', '1TB SSD'
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'ATX',   'Intel i9-14900KF',   'RTX 4080',     '32GB DDR5', '2TB NVMe + 2TB HDD'
  UNION ALL SELECT 'Lenovo', 'GeekPro 2026',   'M-ATX', 'Intel Core i7-14700','RTX 4060',     '16GB DDR5', '1TB NVMe'
  UNION ALL SELECT 'Dell',   'OptiPlex 7020',  'M-ATX', 'Intel Core i5-14500','集成 UHD 770', '16GB DDR5', '512GB SSD'
  UNION ALL SELECT 'MSI',    'Infinite S3',    'ATX',   'Intel Core i7-14700F','RTX 4070',    '32GB DDR5', '1TB NVMe + 2TB HDD'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Desktop_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 四、DIY配件 - CPU
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('AMD',   'Ryzen 7 7800X3D', 2599.00, 40, 'DIY配件'),
('AMD',   'Ryzen 9 7950X3D', 4999.00, 20, 'DIY配件'),
('AMD',   'Ryzen 9 7900X3D', 3299.00, 25, 'DIY配件'),
('AMD',   'Ryzen 9 7950X',   3799.00, 22, 'DIY配件'),
('AMD',   'Ryzen 9 7900X',   2899.00, 28, 'DIY配件'),
('AMD',   'Ryzen 7 7700X',   1899.00, 35, 'DIY配件'),
('AMD',   'Ryzen 5 7600X',   1399.00, 40, 'DIY配件'),
('AMD',   'Ryzen 9 9950X',   5299.00, 18, 'DIY配件'),
('AMD',   'Ryzen 9 9900X',   3499.00, 22, 'DIY配件'),
('AMD',   'Ryzen 7 9700X',   2299.00, 28, 'DIY配件'),
('AMD',   'Ryzen 5 9600X',   1699.00, 32, 'DIY配件'),
('Intel', 'Core i7-14700K',  2999.00, 30, 'DIY配件'),
('Intel', 'Core i5-14600K',  1899.00, 35, 'DIY配件'),
('Intel', 'Core i9-14900K',  4799.00, 25, 'DIY配件'),
('Intel', 'Core i9-13900K',  3999.00, 18, 'DIY配件'),
('Intel', 'Core i7-13700K',  2499.00, 25, 'DIY配件'),
('Intel', 'Core i5-13600K',  1799.00, 30, 'DIY配件'),
('Intel', 'Core i9-12900K',  2999.00, 15, 'DIY配件'),
('Intel', 'Core i7-12700K',  1899.00, 20, 'DIY配件'),
('Intel', 'Core i5-12600K',  1299.00, 28, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'AMD' AS brand, 'Ryzen 7 7800X3D' AS model, 'CPU' AS part_type, '8核16线程 / 4.2GHz基频 / AM5接口 / 120W TDP' AS specification
  UNION ALL SELECT 'AMD',   'Ryzen 9 7950X3D', 'CPU', '16核32线程 / 4.2GHz基频 / 128MB L3 / AM5 / 120W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 9 7900X3D', 'CPU', '12核24线程 / 4.4GHz基频 / 128MB L3 / AM5 / 120W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 9 7950X',   'CPU', '16核32线程 / 4.5GHz基频 / 64MB L3 / AM5 / 170W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 9 7900X',   'CPU', '12核24线程 / 4.7GHz基频 / 64MB L3 / AM5 / 170W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 7 7700X',   'CPU', '8核16线程 / 4.5GHz基频 / 32MB L3 / AM5 / 105W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 5 7600X',   'CPU', '6核12线程 / 4.7GHz基频 / 32MB L3 / AM5 / 105W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 9 9950X',   'CPU', '16核32线程 / 4.3GHz基频 / 64MB L3 / Zen5 / AM5 / 170W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 9 9900X',   'CPU', '12核24线程 / 4.4GHz基频 / 64MB L3 / Zen5 / AM5 / 120W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 7 9700X',   'CPU', '8核16线程 / 3.8GHz基频 / 32MB L3 / Zen5 / AM5 / 65W TDP'
  UNION ALL SELECT 'AMD',   'Ryzen 5 9600X',   'CPU', '6核12线程 / 3.9GHz基频 / 32MB L3 / Zen5 / AM5 / 65W TDP'
  UNION ALL SELECT 'Intel', 'Core i7-14700K',  'CPU', '20核28线程 / 3.4GHz基频 / 33MB L3 / LGA1700 / 253W TDP'
  UNION ALL SELECT 'Intel', 'Core i5-14600K',  'CPU', '14核20线程 / 3.5GHz基频 / 24MB L3 / LGA1700 / 181W TDP'
  UNION ALL SELECT 'Intel', 'Core i9-14900K',  'CPU', '24核32线程 / 3.2GHz基频 / 36MB L3 / LGA1700 / 253W TDP'
  UNION ALL SELECT 'Intel', 'Core i9-13900K',  'CPU', '24核32线程 / 3.0GHz基频 / 36MB L3 / LGA1700 / 253W TDP'
  UNION ALL SELECT 'Intel', 'Core i7-13700K',  'CPU', '16核24线程 / 3.4GHz基频 / 30MB L3 / LGA1700 / 253W TDP'
  UNION ALL SELECT 'Intel', 'Core i5-13600K',  'CPU', '14核20线程 / 3.5GHz基频 / 24MB L3 / LGA1700 / 181W TDP'
  UNION ALL SELECT 'Intel', 'Core i9-12900K',  'CPU', '16核24线程 / 3.2GHz基频 / 30MB L3 / LGA1700 / 241W TDP'
  UNION ALL SELECT 'Intel', 'Core i7-12700K',  'CPU', '12核20线程 / 3.6GHz基频 / 25MB L3 / LGA1700 / 180W TDP'
  UNION ALL SELECT 'Intel', 'Core i5-12600K',  'CPU', '10核16线程 / 3.6GHz基频 / 20MB L3 / LGA1700 / 150W TDP'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 五、DIY配件 - 显卡
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('NVIDIA', 'GeForce RTX 4070',           4799.00, 30, 'DIY配件'),
('ASUS',   'ROG STRIX RTX 4080',         8999.00, 12, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4090',          15999.00,  8, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4080 SUPER',     8999.00, 12, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4070 Ti SUPER',  6799.00, 15, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4070 Ti',        5999.00, 18, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4070 SUPER',     4999.00, 20, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4060 Ti',        3599.00, 25, 'DIY配件'),
('NVIDIA', 'GeForce RTX 4060',           2799.00, 35, 'DIY配件'),
('NVIDIA', 'GeForce RTX 3090',           8999.00, 10, 'DIY配件'),
('NVIDIA', 'GeForce RTX 3080',           5499.00, 12, 'DIY配件'),
('NVIDIA', 'GeForce RTX 3070',           3899.00, 15, 'DIY配件'),
('NVIDIA', 'GeForce RTX 3060',           2299.00, 30, 'DIY配件'),
('AMD',    'Radeon RX 7900 XTX',         8499.00, 10, 'DIY配件'),
('AMD',    'Radeon RX 7900 XT',          6799.00, 14, 'DIY配件'),
('AMD',    'Radeon RX 7800 XT',          4299.00, 18, 'DIY配件'),
('AMD',    'Radeon RX 7700 XT',          3499.00, 22, 'DIY配件'),
('AMD',    'Radeon RX 7600 XT',          2699.00, 28, 'DIY配件'),
('AMD',    'Radeon RX 6750 XT',          2299.00, 20, 'DIY配件'),
('AMD',    'Radeon RX 6700 XT',          2099.00, 25, 'DIY配件'),
('AMD',    'Radeon RX 6600 XT',          1899.00, 30, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'NVIDIA' AS brand, 'GeForce RTX 4070' AS model, '显卡' AS part_type, '12GB GDDR6X / 192-bit / PCIe 4.0 / 200W' AS specification
  UNION ALL SELECT 'ASUS',   'ROG STRIX RTX 4080',        '显卡', '16GB GDDR6X / 256-bit / PCIe 4.0 / 320W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4090',          '显卡', '24GB GDDR6X / 384-bit / PCIe 4.0 / 450W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4080 SUPER',    '显卡', '16GB GDDR6X / 256-bit / PCIe 4.0 / 320W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4070 Ti SUPER', '显卡', '16GB GDDR6X / 256-bit / PCIe 4.0 / 285W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4070 Ti',       '显卡', '12GB GDDR6X / 192-bit / PCIe 4.0 / 285W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4070 SUPER',    '显卡', '12GB GDDR6X / 192-bit / PCIe 4.0 / 220W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4060 Ti',       '显卡', '8GB GDDR6 / 128-bit / PCIe 4.0 / 160W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 4060',          '显卡', '8GB GDDR6 / 128-bit / PCIe 4.0 / 115W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 3090',          '显卡', '24GB GDDR6X / 384-bit / PCIe 4.0 / 350W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 3080',          '显卡', '10GB GDDR6X / 320-bit / PCIe 4.0 / 320W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 3070',          '显卡', '8GB GDDR6 / 256-bit / PCIe 4.0 / 220W'
  UNION ALL SELECT 'NVIDIA', 'GeForce RTX 3060',          '显卡', '12GB GDDR6 / 192-bit / PCIe 4.0 / 170W'
  UNION ALL SELECT 'AMD',    'Radeon RX 7900 XTX',        '显卡', '24GB GDDR6 / 384-bit / PCIe 4.0 / 355W'
  UNION ALL SELECT 'AMD',    'Radeon RX 7900 XT',         '显卡', '20GB GDDR6 / 320-bit / PCIe 4.0 / 315W'
  UNION ALL SELECT 'AMD',    'Radeon RX 7800 XT',         '显卡', '16GB GDDR6 / 256-bit / PCIe 4.0 / 263W'
  UNION ALL SELECT 'AMD',    'Radeon RX 7700 XT',         '显卡', '12GB GDDR6 / 192-bit / PCIe 4.0 / 245W'
  UNION ALL SELECT 'AMD',    'Radeon RX 7600 XT',         '显卡', '8GB GDDR6 / 128-bit / PCIe 4.0 / 190W'
  UNION ALL SELECT 'AMD',    'Radeon RX 6750 XT',         '显卡', '12GB GDDR6 / 192-bit / PCIe 4.0 / 250W'
  UNION ALL SELECT 'AMD',    'Radeon RX 6700 XT',         '显卡', '12GB GDDR6 / 192-bit / PCIe 4.0 / 230W'
  UNION ALL SELECT 'AMD',    'Radeon RX 6600 XT',         '显卡', '8GB GDDR6 / 128-bit / PCIe 4.0 / 160W'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 六、DIY配件 - 主板
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('ASUS',     'ROG STRIX B650-A',             1499.00, 35, 'DIY配件'),
('MSI',      'MAG B760M MORTAR',              999.00, 40, 'DIY配件'),
('ASUS',     'ROG STRIX Z790-A GAMING WIFI', 2699.00, 15, 'DIY配件'),
('ASUS',     'PRIME B760M-A',                 899.00, 30, 'DIY配件'),
('MSI',      'MAG Z790 TOMAHAWK WIFI',       2199.00, 18, 'DIY配件'),
('MSI',      'MAG B760M MORTAR WIFI II',     1299.00, 25, 'DIY配件'),
('Gigabyte', 'Z790 AORUS PRO X',             2399.00, 12, 'DIY配件'),
('Gigabyte', 'B760M DS3H AX',                 799.00, 35, 'DIY配件'),
('ASRock',   'Z790 Taichi',                  2999.00, 10, 'DIY配件'),
('ASRock',   'B760M-itx',                     999.00, 20, 'DIY配件'),
('ASUS',     'ROG STRIX Z890-A GAMING WIFI', 2999.00, 10, 'DIY配件'),
('MSI',      'MAG Z890 TOMAHAWK WIFI',       2499.00, 12, 'DIY配件'),
('Gigabyte', 'Z890 AORUS PRO',               2599.00, 10, 'DIY配件'),
('ASRock',   'Z890 Taichi',                  3299.00,  8, 'DIY配件'),
('ASUS',     'ROG CROSSHAIR X670E HERO',     5499.00,  6, 'DIY配件'),
('MSI',      'MPG X670E CARBON WIFI',        3199.00,  8, 'DIY配件'),
('Gigabyte', 'X670E AORUS PRO',              2799.00, 10, 'DIY配件'),
('ASUS',     'ROG STRIX X870E-E GAMING WIFI',4299.00,  6, 'DIY配件'),
('MSI',      'MPG X870E CARBON WIFI',        3499.00,  8, 'DIY配件'),
('Gigabyte', 'X870E AORUS PRO',              2999.00,  8, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'ASUS' AS brand, 'ROG STRIX B650-A' AS model, '主板' AS part_type, 'AM5 / ATX / 4×DDR5 / PCIe 5.0 / WiFi 6E' AS specification
  UNION ALL SELECT 'MSI',      'MAG B760M MORTAR',              '主板', 'LGA1700 / M-ATX / 2×DDR5 / PCIe 5.0 / 2.5G网口'
  UNION ALL SELECT 'ASUS',     'ROG STRIX Z790-A GAMING WIFI',  '主板', 'LGA1700 / ATX / 16+1供电 / DDR5 7800+ / 4×M.2 / PCIe 5.0 / WiFi 6E'
  UNION ALL SELECT 'ASUS',     'PRIME B760M-A',                 '主板', 'LGA1700 / M-ATX / 2×DDR5 / PCIe 4.0 / 2×M.2'
  UNION ALL SELECT 'MSI',      'MAG Z790 TOMAHAWK WIFI',        '主板', 'LGA1700 / ATX / 18+1+1供电 / DDR5 7200+ / 4×M.2 / WiFi 6E / 2.5G网口'
  UNION ALL SELECT 'MSI',      'MAG B760M MORTAR WIFI II',      '主板', 'LGA1700 / M-ATX / 2×DDR5 / PCIe 5.0 / WiFi 6E / 2.5G网口'
  UNION ALL SELECT 'Gigabyte', 'Z790 AORUS PRO X',              '主板', 'LGA1700 / ATX / 18+1+2供电 / DDR5 8000+ / 4×M.2 / PCIe 5.0 / WiFi 7'
  UNION ALL SELECT 'Gigabyte', 'B760M DS3H AX',                 '主板', 'LGA1700 / M-ATX / 2×DDR5 / PCIe 4.0 / WiFi 6 / 2×M.2'
  UNION ALL SELECT 'ASRock',   'Z790 Taichi',                   '主板', 'LGA1700 / ATX / 24+1+2供电 / DDR5 8000+ / 3×M.2 / PCIe 5.0 / WiFi 6E / 10G网口'
  UNION ALL SELECT 'ASRock',   'B760M-itx',                     '主板', 'LGA1700 / ITX / 2×DDR5 / PCIe 4.0 / WiFi 6E / 2×M.2'
  UNION ALL SELECT 'ASUS',     'ROG STRIX Z890-A GAMING WIFI',  '主板', 'LGA1851 / ATX / 18+1供电 / DDR5 8000+ / 5×M.2 / PCIe 5.0 / WiFi 7 / USB4'
  UNION ALL SELECT 'MSI',      'MAG Z890 TOMAHAWK WIFI',        '主板', 'LGA1851 / ATX / 18+1+1供电 / DDR5 7200+ / 4×M.2 / PCIe 5.0 / WiFi 7 / 5G网口'
  UNION ALL SELECT 'Gigabyte', 'Z890 AORUS PRO',                '主板', 'LGA1851 / ATX / 18+1+2供电 / DDR5 8800+ / 4×M.2 / PCIe 5.0 / WiFi 7'
  UNION ALL SELECT 'ASRock',   'Z890 Taichi',                   '主板', 'LGA1851 / ATX / 24+1+2供电 / DDR5 8800+ / 5×M.2 / PCIe 5.0 / WiFi 7 / 10G网口'
  UNION ALL SELECT 'ASUS',     'ROG CROSSHAIR X670E HERO',      '主板', 'AM5 / ATX / 20+2供电 / DDR5 6400+ / 4×M.2 / PCIe 5.0 / WiFi 6E / 10G网口'
  UNION ALL SELECT 'MSI',      'MPG X670E CARBON WIFI',         '主板', 'AM5 / ATX / 22+2供电 / DDR5 6600+ / 4×M.2 / PCIe 5.0 / WiFi 6E / 2.5G网口'
  UNION ALL SELECT 'Gigabyte', 'X670E AORUS PRO',               '主板', 'AM5 / ATX / 18+2+2供电 / DDR5 6400+ / 4×M.2 / PCIe 5.0 / WiFi 6E'
  UNION ALL SELECT 'ASUS',     'ROG STRIX X870E-E GAMING WIFI', '主板', 'AM5 / ATX / 18+2+2供电 / DDR5 8000+ / 5×M.2 / PCIe 5.0 / WiFi 7 / USB4'
  UNION ALL SELECT 'MSI',      'MPG X870E CARBON WIFI',         '主板', 'AM5 / ATX / 22+2供电 / DDR5 8000+ / 4×M.2 / PCIe 5.0 / WiFi 7 / 5G网口'
  UNION ALL SELECT 'Gigabyte', 'X870E AORUS PRO',               '主板', 'AM5 / ATX / 18+2+2供电 / DDR5 8000+ / 4×M.2 / PCIe 5.0 / WiFi 7 / USB4'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 七、DIY配件 - 内存
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Corsair', 'Vengeance DDR5 32GB',             899.00, 60, 'DIY配件'),
('G.SKILL', 'Trident Z5 RGB 64GB',            1799.00, 28, 'DIY配件'),
('Corsair', 'Vengeance DDR5 6000 32GB',        799.00, 50, 'DIY配件'),
('G.SKILL', 'Trident Z5 RGB DDR5 6000 32GB',   999.00, 40, 'DIY配件'),
('Kingston','FURY Beast DDR5 6000 32GB',       849.00, 45, 'DIY配件'),
('Crucial', 'Pro DDR5 6000 32GB',              699.00, 55, 'DIY配件'),
('Corsair', 'Vengeance DDR5 6400 32GB',        899.00, 40, 'DIY配件'),
('G.SKILL', 'Trident Z5 RGB DDR5 6400 32GB',  1149.00, 30, 'DIY配件'),
('Kingston','FURY Renegade DDR5 6400 32GB',   1099.00, 25, 'DIY配件'),
('G.SKILL', 'Trident Z5 RGB DDR5 7200 32GB',  1499.00, 20, 'DIY配件'),
('Kingston','FURY Renegade DDR5 7200 32GB',   1599.00, 18, 'DIY配件'),
('Corsair', 'Vengeance DDR5 6000 64GB',       1599.00, 18, 'DIY配件'),
('G.SKILL', 'Trident Z5 Neo RGB DDR5 6000 96GB',3299.00,10,'DIY配件'),
('Kingston','FURY Beast DDR5 5600 32GB',       599.00, 60, 'DIY配件'),
('Corsair', 'Vengeance LPX DDR4 3200 32GB',    399.00, 50, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'Corsair' AS brand, 'Vengeance DDR5 32GB' AS model, '内存' AS part_type, 'DDR5 6000MHz / CL30 / 2×16GB 套条 / XMP 3.0' AS specification
  UNION ALL SELECT 'G.SKILL', 'Trident Z5 RGB 64GB',           '内存', 'DDR5 6400MHz / CL32 / 2×32GB 套条 / EXPO / RGB'
  UNION ALL SELECT 'Corsair', 'Vengeance DDR5 6000 32GB',      '内存', 'DDR5 6000MHz / CL30 / 2×16GB / EXPO / XMP 3.0'
  UNION ALL SELECT 'G.SKILL', 'Trident Z5 RGB DDR5 6000 32GB', '内存', 'DDR5 6000MHz / CL30 / 2×16GB / EXPO / RGB'
  UNION ALL SELECT 'Kingston','FURY Beast DDR5 6000 32GB',     '内存', 'DDR5 6000MHz / CL36 / 2×16GB / XMP 3.0 / Intel优化'
  UNION ALL SELECT 'Crucial', 'Pro DDR5 6000 32GB',            '内存', 'DDR5 6000MHz / CL36 / 2×16GB / Plug&Play超频'
  UNION ALL SELECT 'Corsair', 'Vengeance DDR5 6400 32GB',      '内存', 'DDR5 6400MHz / CL32 / 2×16GB / XMP 3.0 / Intel优化'
  UNION ALL SELECT 'G.SKILL', 'Trident Z5 RGB DDR5 6400 32GB', '内存', 'DDR5 6400MHz / CL32 / 2×16GB / XMP 3.0 / RGB'
  UNION ALL SELECT 'Kingston','FURY Renegade DDR5 6400 32GB',  '内存', 'DDR5 6400MHz / CL32 / 2×16GB / XMP 3.0 / RGB / 低延迟'
  UNION ALL SELECT 'G.SKILL', 'Trident Z5 RGB DDR5 7200 32GB', '内存', 'DDR5 7200MHz / CL34 / 2×16GB / XMP 3.0 / 高频RGB'
  UNION ALL SELECT 'Kingston','FURY Renegade DDR5 7200 32GB',  '内存', 'DDR5 7200MHz / CL38 / 2×16GB / XMP 3.0 / RGB / Intel 14代优化'
  UNION ALL SELECT 'Corsair', 'Vengeance DDR5 6000 64GB',      '内存', 'DDR5 6000MHz / CL30 / 2×32GB / EXPO / XMP 3.0'
  UNION ALL SELECT 'G.SKILL', 'Trident Z5 Neo RGB DDR5 6000 96GB','内存', 'DDR5 6000MHz / CL30 / 2×48GB / EXPO / RGB / 大容量'
  UNION ALL SELECT 'Kingston','FURY Beast DDR5 5600 32GB',     '内存', 'DDR5 5600MHz / CL40 / 2×16GB / XMP 3.0 / 入门版'
  UNION ALL SELECT 'Corsair', 'Vengeance LPX DDR4 3200 32GB',  '内存', 'DDR4 3200MHz / CL16 / 2×16GB / XMP 2.0 / 经典DDR4'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 八、DIY配件 - 硬盘
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Samsung', '990 PRO 2TB NVMe',      1299.00, 45, 'DIY配件'),
('WD',      'Black SN850X 1TB',       799.00, 50, 'DIY配件'),
('Samsung', '990 PRO 1TB',            899.00, 50, 'DIY配件'),
('Samsung', '990 PRO 4TB',           2999.00, 15, 'DIY配件'),
('Samsung', '990 EVO 2TB',           1199.00, 30, 'DIY配件'),
('Samsung', '870 EVO 2TB SATA',       999.00, 40, 'DIY配件'),
('WD',      'BLACK SN850X 2TB',      1299.00, 35, 'DIY配件'),
('WD',      'BLACK SN850X 4TB',      2599.00, 12, 'DIY配件'),
('WD',      'BLUE SN580 2TB',         799.00, 45, 'DIY配件'),
('WD',      'Black SN770 1TB',        599.00, 50, 'DIY配件'),
('Crucial', 'T710 Gen5 2TB',         1899.00, 12, 'DIY配件'),
('Crucial', 'T700 Gen5 1TB',         1199.00, 15, 'DIY配件'),
('Crucial', 'P3 Plus 1TB',            499.00, 60, 'DIY配件'),
('Crucial', 'T500 Gen4 2TB',         1199.00, 25, 'DIY配件'),
('Seagate', 'FireCuda 530 2TB',      1499.00, 20, 'DIY配件'),
('Seagate', 'Barracuda 510 1TB',      599.00, 35, 'DIY配件'),
('Kingston','FURY Renegade NVMe 2TB',1099.00, 30, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'Samsung' AS brand, '990 PRO 2TB NVMe' AS model, '硬盘' AS part_type, 'M.2 NVMe PCIe 4.0 / 读7450 写6900 MB/s' AS specification
  UNION ALL SELECT 'WD',      'Black SN850X 1TB',     '硬盘', 'M.2 NVMe PCIe 4.0 / 读7300 写6300 MB/s'
  UNION ALL SELECT 'Samsung', '990 PRO 1TB',          '硬盘', 'M.2 NVMe PCIe 4.0 / 1TB / 读7450 写6900 MB/s / 1200 TBW'
  UNION ALL SELECT 'Samsung', '990 PRO 4TB',          '硬盘', 'M.2 NVMe PCIe 4.0 / 4TB / 读7450 写6900 MB/s / 2400 TBW'
  UNION ALL SELECT 'Samsung', '990 EVO 2TB',          '硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读5000 写4200 MB/s / 1200 TBW / 混合协议'
  UNION ALL SELECT 'Samsung', '870 EVO 2TB SATA',     '硬盘', '2.5寸 SATA / 2TB / 读560 写530 MB/s / 1200 TBW / V-NAND TLC'
  UNION ALL SELECT 'WD',      'BLACK SN850X 2TB',     '硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读7300 写6600 MB/s / 1200 TBW / 游戏'
  UNION ALL SELECT 'WD',      'BLACK SN850X 4TB',     '硬盘', 'M.2 NVMe PCIe 4.0 / 4TB / 读7300 写6600 MB/s / 2400 TBW'
  UNION ALL SELECT 'WD',      'BLUE SN580 2TB',       '硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读4150 写4150 MB/s / 900 TBW / 主流'
  UNION ALL SELECT 'WD',      'Black SN770 1TB',      '硬盘', 'M.2 NVMe PCIe 4.0 / 1TB / 读5150 写4900 MB/s / 600 TBW / 性价比'
  UNION ALL SELECT 'Crucial', 'T710 Gen5 2TB',        '硬盘', 'M.2 NVMe PCIe 5.0 / 2TB / 读14000 写12000 MB/s / 2400 TBW / Gen5旗舰'
  UNION ALL SELECT 'Crucial', 'T700 Gen5 1TB',        '硬盘', 'M.2 NVMe PCIe 5.0 / 1TB / 读12400 写11800 MB/s / 600 TBW / Gen5代'
  UNION ALL SELECT 'Crucial', 'P3 Plus 1TB',          '硬盘', 'M.2 NVMe PCIe 4.0 / 1TB / 读5000 写4200 MB/s / QLC / 220 TBW'
  UNION ALL SELECT 'Crucial', 'T500 Gen4 2TB',        '硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读7300 写6600 MB/s / 1200 TBW / TLC'
  UNION ALL SELECT 'Seagate', 'FireCuda 530 2TB',     '硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读7300 写6900 MB/s / 2550 TBW / 游戏'
  UNION ALL SELECT 'Seagate', 'Barracuda 510 1TB',    '硬盘', 'M.2 NVMe PCIe 3.0 / 1TB / 读3400 写3000 MB/s / 600 TBW'
  UNION ALL SELECT 'Kingston','FURY Renegade NVMe 2TB','硬盘', 'M.2 NVMe PCIe 4.0 / 2TB / 读7300 写6000 MB/s / 2000 TBW / 游戏'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 九、DIY配件 - 电源
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Corsair',     'RM850e 850W 金牌',        999.00, 35, 'DIY配件'),
('Seasonic',    'PRIME TX-1000 钛金',     2299.00, 15, 'DIY配件'),
('Seasonic',    'PRIME TX-1300 ATX 3.1',  2999.00, 10, 'DIY配件'),
('Seasonic',    'FOCUS GX-1000 ATX 3.1',  1799.00, 15, 'DIY配件'),
('Super Flower','LEADEX VII 1300W',       2599.00, 10, 'DIY配件'),
('Super Flower','LEADEX III GOLD 850W',    899.00, 25, 'DIY配件'),
('FSP',         'Hydro PTM X 1000W',      1399.00, 12, 'DIY配件'),
('Great Wall',  'X8 850W 金牌',            699.00, 30, 'DIY配件'),
('Segotep',     'GP 850W 金牌',            499.00, 40, 'DIY配件'),
('Antec',       'NEOECO 750W 金牌',        599.00, 35, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'Corsair' AS brand, 'RM850e 850W 金牌' AS model, '电源' AS part_type, '850W / 80Plus 金牌 / 全模组 / 日系电容' AS specification
  UNION ALL SELECT 'Seasonic',    'PRIME TX-1000 钛金',    '电源', '1000W / 80Plus 钛金 / 全模组 / 12V-2x6'
  UNION ALL SELECT 'Seasonic',    'PRIME TX-1300 ATX 3.1', '电源', '1300W / 80Plus 钛金 / 全模组 / ATX 3.1 / 原生 12V-2x6 / 日系电容'
  UNION ALL SELECT 'Seasonic',    'FOCUS GX-1000 ATX 3.1', '电源', '1000W / 80Plus 金牌 / 全模组 / ATX 3.1 / 12V-2x6 / 10年质保'
  UNION ALL SELECT 'Super Flower','LEADEX VII 1300W',      '电源', '1300W / 80Plus 钛金 / 全模组 / ATX 3.0 / 12VHPWR / ECO智慧温控'
  UNION ALL SELECT 'Super Flower','LEADEX III GOLD 850W',  '电源', '850W / 80Plus 金牌 / 全模组 / ATX 3.1 / 200%峰值功耗耐受'
  UNION ALL SELECT 'FSP',         'Hydro PTM X 1000W',     '电源', '1000W / 80Plus 钛金 / 全模组 / ATX 3.0 / 12VHPWR / 工业级'
  UNION ALL SELECT 'Great Wall',  'X8 850W 金牌',          '电源', '850W / 80Plus 金牌 / 全模组 / LLC+DC-DC / 5年质保'
  UNION ALL SELECT 'Segotep',     'GP 850W 金牌',          '电源', '850W / 80Plus 金牌 / 半模组 / 温控风扇 / 3年质保'
  UNION ALL SELECT 'Antec',       'NEOECO 750W 金牌',      '电源', '750W / 80Plus 金牌 / 全模组 / LLC+DC-DC / 静音风扇'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 十、DIY配件 - 机箱
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Lian Li',        'O11 Dynamic EVO',  899.00, 20, 'DIY配件'),
('NZXT',           'H7 Flow',          799.00, 30, 'DIY配件'),
('Phanteks',       'NV7 海景房',      1299.00, 12, 'DIY配件'),
('Phanteks',       'NV5',              899.00, 18, 'DIY配件'),
('CoolerMaster',   'HAF 700 EVO',     1999.00,  6, 'DIY配件'),
('CoolerMaster',   'Q300L M-ATX',      399.00, 30, 'DIY配件'),
('Fractal Design', 'North',            899.00, 15, 'DIY配件'),
('be quiet!',      'Pure Base 500DX',  599.00, 20, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'Lian Li' AS brand, 'O11 Dynamic EVO' AS model, '机箱' AS part_type, '双面玻璃 / 铝合金 / E-ATX 兼容 / 垂直风道' AS specification
  UNION ALL SELECT 'NZXT',           'H7 Flow',         '机箱', '钢化玻璃侧透 / ATX 兼容 / 前置3×120风扇位'
  UNION ALL SELECT 'Phanteks',       'NV7 海景房',      '机箱', '全视景海景房 / 无立柱双面玻璃 / EATX / 420mm水冷位 / ARGB双通道'
  UNION ALL SELECT 'Phanteks',       'NV5',             '机箱', '海景房设计 / 双面玻璃 / ATX / 360mm水冷位 / 模组化安装'
  UNION ALL SELECT 'CoolerMaster',   'HAF 700 EVO',     '机箱', '全塔 / E-ATX / 双面玻璃 / ARGB灯控 / 360mm×3水冷位 / 模组化'
  UNION ALL SELECT 'CoolerMaster',   'Q300L M-ATX',     '机箱', '紧凑M-ATX / 钢网面板 / 240mm水冷位 / 磁吸防尘网'
  UNION ALL SELECT 'Fractal Design', 'North',           '机箱', '木纹前面板 / ATX / 360mm水冷位 / 北欧极简设计'
  UNION ALL SELECT 'be quiet!',      'Pure Base 500DX', '机箱', 'ATX / 三面网孔散热 / 360mm水冷位 / 静音设计'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 十一、DIY配件 - 散热器
-- ============================================================
INSERT IGNORE INTO Product (brand, model, price, stock, category) VALUES
('Noctua',       'NH-D15 chromax',           799.00, 25, 'DIY配件'),
('EK',           '360 AIO D-RGB',           1299.00, 20, 'DIY配件'),
('Thermalright', 'Peerless Assassin 120 SE', 169.00, 60, 'DIY配件'),
('Thermalright', 'Frozen Vision 360 ARGB',   399.00, 35, 'DIY配件'),
('Thermalright', 'Frost Spirit 140 RGB',     269.00, 40, 'DIY配件'),
('Noctua',       'NH-U12A chromax',          899.00, 15, 'DIY配件'),
('Valkyrie',     'GL360 ARGB',               599.00, 25, 'DIY配件'),
('Valkyrie',     'DL-SX360 ARGB',            499.00, 28, 'DIY配件'),
('DeepCool',     'LT720 360mm',              499.00, 30, 'DIY配件'),
('DeepCool',     'AK500',                    199.00, 50, 'DIY配件'),
('CoolerMaster', 'Hyper 212 EVO',            199.00, 50, 'DIY配件');

INSERT INTO Spare_Part_Detail (product_id, part_type, specification)
SELECT p.product_id, d.part_type, d.specification
FROM Product p
JOIN (
  SELECT 'Noctua' AS brand, 'NH-D15 chromax' AS model, '散热器' AS part_type, '风冷 / 双塔6热管 / 165mm高度 / 含NT-H2硅脂' AS specification
  UNION ALL SELECT 'EK',           '360 AIO D-RGB',            '散热器', '360mm 一体水冷 / D5泵 / 三代冷头 / ARGB'
  UNION ALL SELECT 'Thermalright', 'Peerless Assassin 120 SE', '散热器', '风冷 / 双塔6热管 / 120mm双扇 / 155mm高度 / 性价比之王'
  UNION ALL SELECT 'Thermalright', 'Frozen Vision 360 ARGB',   '散热器', '360mm一体水冷 / ARGB / 三代冷头 / 高扬程泵 / AM5兼容'
  UNION ALL SELECT 'Thermalright', 'Frost Spirit 140 RGB',     '散热器', '风冷 / 双塔7热管 / 140mm风扇 / AGHP逆重力热管 / ARGB'
  UNION ALL SELECT 'Noctua',       'NH-U12A chromax',          '散热器', '风冷 / 单塔6热管 / 120mm NF-A12x25风扇 / 158mm高度 / 极致静音'
  UNION ALL SELECT 'Valkyrie',     'GL360 ARGB',               '散热器', '360mm一体水冷 / 第八代冷头 / ARGB无限镜 / 2.4寸LCD屏 / 高扬程泵'
  UNION ALL SELECT 'Valkyrie',     'DL-SX360 ARGB',            '散热器', '360mm一体水冷 / ARGB / 双扬程泵 / 智能温控 / LGA1851兼容'
  UNION ALL SELECT 'DeepCool',     'LT720 360mm',              '散热器', '360mm一体水冷 / ARGB / 第八代冷头 / 高流量泵 / 双360风扇'
  UNION ALL SELECT 'DeepCool',     'AK500',                    '散热器', '风冷 / 5热管单塔 / 120mm风扇 / 155mm高度 / 易安装'
  UNION ALL SELECT 'CoolerMaster', 'Hyper 212 EVO',            '散热器', '风冷 / 4热管 / 120mm风扇 / 经典入门款 / 159mm高度'
) d ON d.brand = p.brand AND d.model = p.model
LEFT JOIN Spare_Part_Detail x ON x.product_id = p.product_id
WHERE x.product_id IS NULL;


-- ============================================================
-- 十二、台式机组装配置（按 brand+model 关联整机与配件，幂等防重）
-- ============================================================
INSERT INTO Desktop_Composition (product_id, part_id, quantity)
SELECT pc.product_id, spd.part_id, c.quantity
FROM (
  SELECT 'Lenovo' AS pc_brand, '天逸510S' AS pc_model, 'AMD' AS pt_brand, 'Ryzen 7 7800X3D' AS pt_model, 1 AS quantity
  UNION ALL SELECT 'Lenovo', '天逸510S',       'ASUS',    'ROG STRIX B650-A',    1
  UNION ALL SELECT 'Lenovo', '天逸510S',       'Corsair', 'Vengeance DDR5 32GB', 2
  UNION ALL SELECT 'HP',     '暗影精灵8 台式', 'Intel',   'Core i9-14900K',      1
  UNION ALL SELECT 'HP',     '暗影精灵8 台式', 'NVIDIA',  'GeForce RTX 4070',    1
  UNION ALL SELECT 'HP',     '暗影精灵8 台式', 'G.SKILL', 'Trident Z5 RGB 64GB', 1
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'Intel',   'Core i9-14900K',      1
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'ASUS',    'ROG STRIX RTX 4080',  1
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'Samsung', '990 PRO 2TB NVMe',    1
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'Lian Li', 'O11 Dynamic EVO',     1
  UNION ALL SELECT 'ASUS',   'ROG 光魔G16CH',  'Noctua',  'NH-D15 chromax',      1
) c
JOIN Product pc ON pc.brand = c.pc_brand AND pc.model = c.pc_model
JOIN Product pt ON pt.brand = c.pt_brand AND pt.model = c.pt_model
JOIN Spare_Part_Detail spd ON spd.product_id = pt.product_id
LEFT JOIN Desktop_Composition dc ON dc.product_id = pc.product_id AND dc.part_id = spd.part_id
WHERE dc.composition_id IS NULL;


-- ============================================================
-- 十三、订单测试数据（覆盖全部 5 种状态，按 order_no 幂等防重）
--   说明：
--   1) order_no 用固定值 ORD-SEED-NN（非 UUID），重复执行靠 NOT EXISTS 跳过
--   2) 客户按 phone 关联、商品按 brand+model 关联，避免依赖自增 ID
--   3) total_amount 先占位 0，写完明细后由真实明细 SUM 回填，保证金额准确
--   4) 为保持脚本幂等，此处不扣减 Product 库存（演示订单关系用，非库存对账）
-- ============================================================

-- 13.1 订单主表（金额先置 0，稍后回填）
INSERT INTO Sales_Order
  (order_no, customer_id, order_date, total_amount, status, payment_method, payment_time, cancel_reason, cancel_time)
SELECT o.order_no, c.customer_id, o.order_date, 0, o.status, o.payment_method, o.payment_time, o.cancel_reason, o.cancel_time
FROM (
  SELECT 'ORD-SEED-01' AS order_no, '13800138001' AS phone, '2026-06-01 10:15:00' AS order_date, '待付款' AS status, NULL                 AS payment_method, NULL                  AS payment_time, NULL           AS cancel_reason, NULL                  AS cancel_time
  UNION ALL SELECT 'ORD-SEED-02', '13800138001', '2026-06-02 14:30:00', '已付款', '微信',     '2026-06-02 14:35:00', NULL,             NULL
  UNION ALL SELECT 'ORD-SEED-03', '13900139002', '2026-06-03 09:20:00', '已发货', '支付宝',   '2026-06-03 09:25:00', NULL,             NULL
  UNION ALL SELECT 'ORD-SEED-04', '13700137003', '2026-06-05 16:40:00', '已取消', NULL,       NULL,                  '客户改变主意',   '2026-06-05 17:00:00'
  UNION ALL SELECT 'ORD-SEED-05', '13600136004', '2026-06-08 11:05:00', '已退货', '银行卡',   '2026-06-08 11:10:00', NULL,             NULL
  UNION ALL SELECT 'ORD-SEED-06', '18903503653', '2026-06-10 20:30:00', '已付款', '货到付款', '2026-06-10 20:31:00', NULL,             NULL
  UNION ALL SELECT 'ORD-SEED-07', '13900139002', '2026-06-12 13:00:00', '已发货', '微信',     '2026-06-12 13:05:00', NULL,             NULL
  UNION ALL SELECT 'ORD-SEED-08', '13800138001', '2026-06-15 08:50:00', '待付款', NULL,       NULL,                  NULL,             NULL
) o
JOIN Customer c ON c.phone = o.phone
WHERE NOT EXISTS (SELECT 1 FROM Sales_Order so WHERE so.order_no = o.order_no);

-- 13.2 订单明细（unit_price 取 Product 当前价；按 order_id+product_id 防重）
INSERT INTO Order_Detail (order_id, product_id, quantity, unit_price)
SELECT so.order_id, p.product_id, d.quantity, p.price
FROM (
  SELECT 'ORD-SEED-01' AS order_no, 'Apple'        AS brand, 'MacBook Pro 14'             AS model, 1 AS quantity
  UNION ALL SELECT 'ORD-SEED-01', 'Corsair',      'Vengeance DDR5 32GB',          2
  UNION ALL SELECT 'ORD-SEED-02', 'Lenovo',       'ThinkPad X1 Carbon',           1
  UNION ALL SELECT 'ORD-SEED-03', 'AMD',          'Ryzen 7 7800X3D',              1
  UNION ALL SELECT 'ORD-SEED-03', 'NVIDIA',       'GeForce RTX 4070',             1
  UNION ALL SELECT 'ORD-SEED-03', 'Samsung',      '990 PRO 2TB NVMe',             1
  UNION ALL SELECT 'ORD-SEED-04', 'ASUS',         'ROG 魔霸7 Plus',               1
  UNION ALL SELECT 'ORD-SEED-05', 'Lenovo',       '天逸510S',                     2
  UNION ALL SELECT 'ORD-SEED-06', 'HP',           '暗影精灵8 台式',               1
  UNION ALL SELECT 'ORD-SEED-06', 'Thermalright', 'Peerless Assassin 120 SE',     1
  UNION ALL SELECT 'ORD-SEED-07', 'NVIDIA',       'GeForce RTX 4070',             2
  UNION ALL SELECT 'ORD-SEED-08', 'Lenovo',       '小新 Pro 16 2026',             1
  UNION ALL SELECT 'ORD-SEED-08', 'Corsair',      'Vengeance DDR5 6000 32GB',     1
) d
JOIN Sales_Order so ON so.order_no = d.order_no
JOIN Product p      ON p.brand = d.brand AND p.model = d.model
LEFT JOIN Order_Detail od ON od.order_id = so.order_id AND od.product_id = p.product_id
WHERE od.detail_id IS NULL;

-- 13.3 回填订单总金额 / 实付金额（幂等：基于真实明细聚合）
UPDATE Sales_Order so
JOIN (
  SELECT order_id, SUM(quantity * unit_price) AS amt
  FROM Order_Detail
  GROUP BY order_id
) t ON t.order_id = so.order_id
SET so.total_amount = t.amt,
    so.paid_amount  = CASE WHEN so.status IN ('已付款', '已发货', '已退货') THEN t.amt ELSE NULL END
WHERE so.order_no LIKE 'ORD-SEED-%';
