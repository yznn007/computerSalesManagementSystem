package com.example.springboot.mapper;

import com.example.springboot.entity.OrderDetail;
import com.example.springboot.entity.SalesOrder;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

public interface OrderMapper {

    /**
     * 调用 sp_create_order 存储过程。
     * params: customerId(IN), items(IN,JSON字符串), status(OUT), orderNo(OUT)
     */
    @Select("{call sp_create_order(" +
            "#{customerId,mode=IN,jdbcType=INTEGER}," +
            "#{items,mode=IN,jdbcType=VARCHAR}," +
            "#{status,mode=OUT,jdbcType=INTEGER}," +
            "#{orderNo,mode=OUT,jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callCreateOrder(Map<String, Object> params);

    /**
     * 调用 sp_update_order_status 存储过程。
     * params: orderId(IN), action(IN), paymentMethod(IN), cancelReason(IN), status(OUT), message(OUT)
     */
    @Select("{call sp_update_order_status(" +
            "#{orderId,mode=IN,jdbcType=INTEGER}," +
            "#{action,mode=IN,jdbcType=VARCHAR}," +
            "#{paymentMethod,mode=IN,jdbcType=VARCHAR}," +
            "#{cancelReason,mode=IN,jdbcType=VARCHAR}," +
            "#{status,mode=OUT,jdbcType=INTEGER}," +
            "#{message,mode=OUT,jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callUpdateStatus(Map<String, Object> params);

    @Select("<script>" +
            "SELECT o.*, c.customer_name FROM Sales_Order o " +
            "JOIN Customer c ON o.customer_id = c.customer_id " +
            "<where> " +
            "  <if test='status != null and status != \"\"'>AND o.status = #{status}</if> " +
            "  <if test='customerId != null'>AND o.customer_id = #{customerId}</if> " +
            "</where> " +
            "ORDER BY o.order_date DESC" +
            "</script>")
    List<SalesOrder> findAll(@Param("status") String status, @Param("customerId") Integer customerId);

    @Select("SELECT o.*, c.customer_name FROM Sales_Order o " +
            "JOIN Customer c ON o.customer_id = c.customer_id " +
            "WHERE o.order_id = #{id}")
    SalesOrder findById(@Param("id") Integer id);

    @Select("SELECT od.*, p.brand, p.model, p.category FROM Order_Detail od " +
            "JOIN Product p ON od.product_id = p.product_id " +
            "WHERE od.order_id = #{orderId} ORDER BY od.detail_id")
    List<OrderDetail> findDetailsByOrderId(@Param("orderId") Integer orderId);
}
