USE computer_sales_db;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_create_order;

CREATE PROCEDURE sp_create_order(
    IN  p_customer_id INT,
    IN  p_product_id  INT,
    IN  p_quantity    INT,
    OUT o_status      INT,
    OUT o_order_no    VARCHAR(50)
)
sp_label: BEGIN
    DECLARE v_current_stock INT DEFAULT 0;
    DECLARE v_unit_price    DECIMAL(10,2) DEFAULT 0;
    DECLARE v_order_no      VARCHAR(50);
    DECLARE v_order_id      INT;

    -- 异常捕获：任何SQL错误自动回滚
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status = 3;
        SET o_order_no = '';
    END;

    START TRANSACTION;

    -- 数量校验：必须为正整数
    IF p_quantity <= 0 THEN
        ROLLBACK;
        SET o_status = 1;
        SET o_order_no = '';
        LEAVE sp_label;
    END IF;

    -- 悲观锁：锁定商品行，同时读取库存与单价（防止价格篡改）
    SELECT stock, price INTO v_current_stock, v_unit_price
    FROM Product
    WHERE product_id = p_product_id
    FOR UPDATE;

    -- 商品不存在（SELECT ... INTO 查不到行时变量保持默认值 0，ROW_COUNT() = 0 表示未命中）
    IF ROW_COUNT() = 0 THEN
        ROLLBACK;
        SET o_status = 2;
        SET o_order_no = '';
    -- 库存不足
    ELSEIF v_current_stock < p_quantity THEN
        ROLLBACK;
        SET o_status = 1;
        SET o_order_no = '';
    -- 校验通过：扣减库存 → 生成单号 → 写入订单
    ELSE
        SET v_order_no = CONCAT('ORD', REPLACE(UUID(), '-', ''));

        UPDATE Product
        SET stock = stock - p_quantity
        WHERE product_id = p_product_id;

        INSERT INTO Sales_Order (order_no, customer_id, total_amount, order_date, status)
        VALUES (v_order_no, p_customer_id, p_quantity * v_unit_price, NOW(), '待付款');

        SET v_order_id = LAST_INSERT_ID();

        INSERT INTO Order_Detail (order_id, product_id, quantity, unit_price)
        VALUES (v_order_id, p_product_id, p_quantity, v_unit_price);

        COMMIT;
        SET o_status = 0;
        SET o_order_no = v_order_no;
    END IF;
END sp_label //

DELIMITER ;
