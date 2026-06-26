-- ============================================================
-- 03 业务存储过程（下单 / 搜索 / 客户订单 / 订单详情）
-- 复杂业务逻辑下沉到存储过程，事务与悲观锁在库内闭环；
-- 后端 MyBatis 以 CALLABLE 方式调用，OUT 参数回传状态码。须在 01/02 之后执行。
-- ============================================================
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

    -- 任何 SQL 异常：回滚事务并以状态码 4（系统异常）返回，保证不留下半成品订单
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status = 4;
        SET o_order_no = '';
    END;

    -- 整个下单流程（校验 → 锁库存 → 建单 → 扣减）封装在单个事务内，保证原子性
    START TRANSACTION;

    -- 检查 JSON 是否合法且非空
    IF p_items IS NULL OR JSON_LENGTH(p_items) = 0 THEN
        ROLLBACK;
        SET o_status = 1;
        SET o_order_no = '';
        LEAVE sp_label;
    END IF;

    SET v_total = JSON_LENGTH(p_items);

    -- 第一轮：校验所有商品的数量合法性（无锁、快速失败，避免无谓加锁后再回滚）
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        -- JSON_EXTRACT 取出后用 JSON_UNQUOTE 去掉引号再隐式转 INT
        SET v_quantity = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));
        IF v_quantity <= 0 THEN
            ROLLBACK;
            SET o_status = 1;
            SET o_order_no = '';
            LEAVE sp_label;
        END IF;
        SET v_idx = v_idx + 1;
    END WHILE;

    -- 第二轮：逐条 SELECT ... FOR UPDATE 加悲观锁（行级 X 锁），在锁内校验库存与商品存在性
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].product_id')));
        SET v_quantity   = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));

        -- FOR UPDATE 锁住该商品行直到事务结束，杜绝并发下单造成的超卖
        SELECT stock, price INTO v_current_stock, v_unit_price
        FROM Product
        WHERE product_id = v_product_id
        FOR UPDATE;

        -- ⚠ MySQL 陷阱：SELECT ... INTO 查不到行时既不报错也不置 NULL，
        --   必须用 ROW_COUNT() = 0 判断商品是否存在
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

        -- 累加金额（数量 × 锁内读到的单价），作为订单总额
        SET v_line_total = v_line_total + (v_quantity * v_unit_price);
        SET v_idx = v_idx + 1;
    END WHILE;

    -- 校验通过：用 UUID 去横线生成全局唯一业务单号
    SET v_order_no = CONCAT('ORD', REPLACE(UUID(), '-', ''));

    INSERT INTO Sales_Order (order_no, customer_id, total_amount, order_date, status)
    VALUES (v_order_no, p_customer_id, v_line_total, NOW(), '待付款');

    SET v_order_id = LAST_INSERT_ID(); -- 取刚插入订单的自增主键，供明细外键引用

    -- 第三轮：扣减库存 + 写入明细
    SET v_idx = 0;
    WHILE v_idx < v_total DO
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].product_id')));
        SET v_quantity   = JSON_UNQUOTE(JSON_EXTRACT(p_items, CONCAT('$[', v_idx, '].quantity')));

        -- 重新逐条取单价：循环变量在第二轮结束后只剩最后一件的值，这里按当前商品重取
        SELECT price INTO v_unit_price
        FROM Product
        WHERE product_id = v_product_id;

        UPDATE Product
        SET stock = stock - v_quantity
        WHERE product_id = v_product_id;

        -- 写入明细并保存下单时的单价快照（unit_price），后续商品调价不影响历史订单
        INSERT INTO Order_Detail (order_id, product_id, quantity, unit_price)
        VALUES (v_order_id, v_product_id, v_quantity, v_unit_price);

        SET v_idx = v_idx + 1;
    END WHILE;

    COMMIT;
    SET o_status = 0;
    SET o_order_no = v_order_no;
END sp_label //


-- ============================================================
-- 搜索商品（支持关键词 + 分类筛选）
-- ============================================================
DROP PROCEDURE IF EXISTS sp_search_products;

CREATE PROCEDURE sp_search_products(
    IN p_keyword  VARCHAR(100),
    IN p_category VARCHAR(20)
)
BEGIN
    -- 一次性 LEFT JOIN 三张详情子表，按 category 决定哪组字段有值；
    -- 关键词对品牌/型号模糊匹配，分类精确匹配，入参为 NULL 时该条件不生效
    SELECT p.*,
           ld.screen_size, ld.cpu_model, ld.gpu_model, ld.weight,
           dd.form_factor, dd.cpu_desc, dd.gpu_desc, dd.ram_desc, dd.storage_desc,
           spd.part_type, spd.specification
    FROM Product p
    LEFT JOIN Laptop_Detail ld      ON p.product_id = ld.product_id
    LEFT JOIN Desktop_Detail dd     ON p.product_id = dd.product_id
    LEFT JOIN Spare_Part_Detail spd ON p.product_id = spd.product_id
    WHERE (p_keyword  IS NULL OR p.brand LIKE CONCAT('%', p_keyword, '%') OR p.model LIKE CONCAT('%', p_keyword, '%'))
      AND (p_category IS NULL OR p.category = p_category)
    ORDER BY p.product_id;
END //


-- ============================================================
-- 查询客户订单列表
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_customer_orders;

CREATE PROCEDURE sp_get_customer_orders(
    IN p_customer_id INT
)
BEGIN
    SELECT *
    FROM Sales_Order
    WHERE customer_id = p_customer_id
    ORDER BY order_date DESC;
END //


-- ============================================================
-- 查询订单详情（含商品信息）
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_order_detail;

CREATE PROCEDURE sp_get_order_detail(
    IN p_order_id INT
)
BEGIN
    SELECT od.*, p.brand, p.model, p.category
    FROM Order_Detail od
    JOIN Product p ON od.product_id = p.product_id
    WHERE od.order_id = p_order_id;
END //

DELIMITER ;
