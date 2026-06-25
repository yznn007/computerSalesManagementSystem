DROP PROCEDURE IF EXISTS sp_create_order //

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

    IF p_items IS NULL OR JSON_LENGTH(p_items) = 0 THEN
        ROLLBACK;
        SET o_status = 1;
        SET o_order_no = '';
        LEAVE sp_label;
    END IF;

    SET v_total = JSON_LENGTH(p_items);

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

    SET v_order_no = CONCAT('ORD', REPLACE(UUID(), '-', ''));

    INSERT INTO Sales_Order (order_no, customer_id, total_amount, order_date, status)
    VALUES (v_order_no, p_customer_id, v_line_total, NOW(), '待付款');

    SET v_order_id = LAST_INSERT_ID();

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


DROP PROCEDURE IF EXISTS sp_update_order_status //

CREATE PROCEDURE sp_update_order_status(
    IN  p_order_id       INT,
    IN  p_action         VARCHAR(20),
    IN  p_payment_method VARCHAR(20),
    IN  p_cancel_reason  VARCHAR(200),
    OUT o_status         INT,
    OUT o_message        VARCHAR(100)
)
sp_label: BEGIN
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_target_status  VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status  = 3;
        SET o_message = '系统异常';
    END;

    START TRANSACTION;

    SELECT status INTO v_current_status
    FROM Sales_Order
    WHERE order_id = p_order_id
    FOR UPDATE;

    IF ROW_COUNT() = 0 THEN
        ROLLBACK;
        SET o_status  = 2;
        SET o_message = '订单不存在';
        LEAVE sp_label;
    END IF;

    CASE p_action
        WHEN 'pay' THEN
            IF v_current_status <> '待付款' THEN
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可付款');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已付款';
        WHEN 'ship' THEN
            IF v_current_status <> '已付款' THEN
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可发货');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已发货';
        WHEN 'cancel' THEN
            IF v_current_status <> '待付款' THEN
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可取消');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已取消';
        WHEN 'return' THEN
            IF v_current_status <> '已发货' THEN
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可退货');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已退货';
        ELSE
            ROLLBACK;
            SET o_status = 1;
            SET o_message = '未知操作';
            LEAVE sp_label;
    END CASE;

    IF p_action = 'pay' THEN
        UPDATE Sales_Order
        SET status         = v_target_status,
            payment_method = p_payment_method,
            payment_time   = NOW(),
            paid_amount    = total_amount
        WHERE order_id = p_order_id;
    ELSEIF p_action = 'cancel' THEN
        UPDATE Sales_Order
        SET status        = v_target_status,
            cancel_reason = p_cancel_reason,
            cancel_time   = NOW()
        WHERE order_id = p_order_id;
    ELSE
        UPDATE Sales_Order
        SET status = v_target_status
        WHERE order_id = p_order_id;
    END IF;

    IF p_action IN ('cancel', 'return') THEN
        UPDATE Product p
        JOIN Order_Detail od ON p.product_id = od.product_id
        SET p.stock = p.stock + od.quantity
        WHERE od.order_id = p_order_id;
    END IF;

    COMMIT;
    SET o_status  = 0;
    SET o_message = v_target_status;
END sp_label //
