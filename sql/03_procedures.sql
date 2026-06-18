USE computer_sales_db;

DELIMITER //

-- ============================================================
-- 创建订单（支持多商品 JSON 入参）
-- 状态码：0 成功 / 1 数量不合法 / 2 商品不存在 / 3 库存不足 / 4 系统异常
-- ============================================================
DROP PROCEDURE IF EXISTS sp_create_order;

CREATE PROCEDURE sp_create_order(
    IN  p_customer_id INT,
    IN  p_items       JSON,
    OUT o_status      INT,
    OUT o_order_no    VARCHAR(50)
)
sp_label: BEGIN
    DECLARE v_idx          INT DEFAULT 0;
    DECLARE v_total        INT;
    DECLARE v_product_id   INT;
    DECLARE v_quantity     INT;
    DECLARE v_unit_price   DECIMAL(10,2);
    DECLARE v_current_stock INT;
    DECLARE v_line_total   DECIMAL(10,2) DEFAULT 0;
    DECLARE v_order_no     VARCHAR(50);
    DECLARE v_order_id     INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status = 4;
        SET o_order_no = '';
    END;

    START TRANSACTION;

    -- 检查 JSON 是否合法且非空
    IF p_items IS NULL OR JSON_LENGTH(p_items) = 0 THEN
        ROLLBACK;
        SET o_status = 1;
        SET o_order_no = '';
        LEAVE sp_label;
    END IF;

    SET v_total = JSON_LENGTH(p_items);

    -- 第一轮：校验所有商品的数量合法性
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        SET v_quantity = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));
        IF v_quantity <= 0 THEN
            ROLLBACK;
            SET o_status = 1;
            SET o_order_no = '';
            LEAVE sp_label;
        END IF;
        SET v_idx = v_idx + 1;
    END WHILE;

    -- 第二轮：逐条加悲观锁，校验库存与商品存在性
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].product_id')));
        SET v_quantity   = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));

        SELECT stock, price INTO v_current_stock, v_unit_price
        FROM Product
        WHERE product_id = v_product_id
        FOR UPDATE;

        IF ROW_COUNT() = 0 THEN
            ROLLBACK;
            SET o_status = 2;
            SET o_order_no = '';
            LEAVE sp_label;
        END IF;

        IF v_current_stock < v_quantity THEN
            ROLLBACK;
            SET o_status = 3;
            SET o_order_no = '';
            LEAVE sp_label;
        END IF;

        SET v_line_total = v_line_total + (v_quantity * v_unit_price);
        SET v_idx = v_idx + 1;
    END WHILE;

    -- 校验通过：生成单号
    SET v_order_no = CONCAT('ORD', REPLACE(UUID(), '-', ''));

    INSERT INTO Sales_Order (order_no, customer_id, total_amount, order_date, status)
    VALUES (v_order_no, p_customer_id, v_line_total, NOW(), '待付款');

    SET v_order_id = LAST_INSERT_ID();

    -- 第三轮：扣减库存 + 写入明细
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].product_id')));
        SET v_quantity   = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));

        SELECT price INTO v_unit_price
        FROM Product
        WHERE product_id = v_product_id;

        UPDATE Product
        SET stock = stock - v_quantity
        WHERE product_id = v_product_id;

        INSERT INTO Order_Detail (order_id, product_id, quantity, unit_price)
        VALUES (v_order_id, v_product_id, v_quantity, v_unit_price);

        SET v_idx = v_idx + 1;
    END WHILE;

    COMMIT;
    SET o_status = 0;
    SET o_order_no = v_order_no;
END sp_label //

DELIMITER ;
