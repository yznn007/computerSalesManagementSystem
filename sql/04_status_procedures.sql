-- ============================================================
-- 04 订单状态机存储过程
-- 单独成文件，须在 03 之后执行。状态流转的合法性校验、字段更新与库存回补
-- 全部在库内一个事务中完成，后端只传动作（pay/ship/cancel/return）。
-- ============================================================
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

    -- 任何 SQL 异常：回滚并以状态码 3（系统异常）返回
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status  = 3;
        SET o_message = '系统异常';
    END;

    START TRANSACTION;

    -- 加 FOR UPDATE 锁住订单行，读取当前状态，防止并发流转造成状态错乱
    SELECT status INTO v_current_status
    FROM Sales_Order
    WHERE order_id = p_order_id
    FOR UPDATE;

    -- ⚠ MySQL 陷阱：SELECT ... INTO 无匹配行不会报错，必须用 ROW_COUNT() 判断订单是否存在
    IF ROW_COUNT() = 0 THEN
        ROLLBACK;
        SET o_status  = 2;
        SET o_message = '订单不存在';
        LEAVE sp_label;
    END IF;

    -- 状态机：逐个动作校验"当前状态是否允许该流转"，非法则返回状态码 1 并附原因
    CASE p_action
        WHEN 'pay' THEN
            IF v_current_status <> '待付款' THEN          -- 仅待付款可付款
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可付款');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已付款';
        WHEN 'ship' THEN
            IF v_current_status <> '已付款' THEN          -- 仅已付款可发货
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可发货');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已发货';
        WHEN 'cancel' THEN
            IF v_current_status <> '待付款' THEN          -- 仅待付款可取消（已付款须走退货）
                ROLLBACK;
                SET o_status = 1;
                SET o_message = CONCAT('当前状态[', v_current_status, ']不可取消');
                LEAVE sp_label;
            END IF;
            SET v_target_status = '已取消';
        WHEN 'return' THEN
            IF v_current_status <> '已发货' THEN          -- 仅已发货可退货
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

    -- 按动作分支更新订单：付款补记支付信息、取消补记取消原因，发货/退货仅改状态
    IF p_action = 'pay' THEN
        UPDATE Sales_Order
        SET status         = v_target_status,
            payment_method = p_payment_method,
            payment_time   = NOW(),
            paid_amount    = total_amount        -- 一次性全额付款，实付额=订单总额
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

    -- 取消/退货：一次性 JOIN 明细回补库存（按各明细数量加回 Product.stock）
    IF p_action IN ('cancel', 'return') THEN
        UPDATE Product p
        JOIN Order_Detail od ON p.product_id = od.product_id
        SET p.stock = p.stock + od.quantity
        WHERE od.order_id = p_order_id;
    END IF;

    COMMIT;
    SET o_status  = 0;
    SET o_message = v_target_status;     -- 成功时 o_message 回传目标状态
END sp_label //

DELIMITER ;
