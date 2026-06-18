USE computer_sales_db;

DELIMITER //

-- ============================================================
-- 订单状态流转（付款/发货/取消/退货，取消与退货回补库存）
-- p_action: 'pay' | 'ship' | 'cancel' | 'return'
-- 状态码：0 成功 / 1 状态不允许(非法流转) / 2 订单不存在 / 3 系统异常
-- o_message: 成功时返回目标状态，失败时返回原因
-- ============================================================
DROP PROCEDURE IF EXISTS sp_update_order_status;

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

    -- 取消/退货：一次性 JOIN 回补库存
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

DELIMITER ;
