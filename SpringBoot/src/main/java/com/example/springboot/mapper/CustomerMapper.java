package com.example.springboot.mapper;

import com.example.springboot.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 客户数据访问层。
 * 含基础 CRUD、登录查询（按手机号），以及配合 DataInitializer 的种子密码替换方法。
 */
public interface CustomerMapper {

    /** 客户列表（按 id 升序），供销售员管理界面使用 */
    @Select("SELECT * FROM Customer ORDER BY customer_id")
    List<Customer> findAll();

    /** 按主键查客户 */
    @Select("SELECT * FROM Customer WHERE customer_id = #{id}")
    Customer findById(@Param("id") Integer id);

    /** 按手机号查客户：登录与注册查重的依据（手机号唯一） */
    @Select("SELECT * FROM Customer WHERE phone = #{phone}")
    Customer findByPhone(@Param("phone") String phone);

    /** 新增客户，useGeneratedKeys 将自增主键回填到 customerId */
    @Insert("INSERT INTO Customer (customer_name, phone, address, password_hash) " +
            "VALUES (#{customerName}, #{phone}, #{address}, #{passwordHash})")
    @Options(useGeneratedKeys = true, keyProperty = "customerId")
    int insert(Customer c);

    /** 更新客户资料（不含密码，密码走单独方法） */
    @Update("UPDATE Customer SET customer_name=#{customerName}, phone=#{phone}, address=#{address} " +
            "WHERE customer_id = #{customerId}")
    int update(Customer c);

    /** 删除客户 */
    @Delete("DELETE FROM Customer WHERE customer_id = #{id}")
    int deleteById(@Param("id") Integer id);

    /** 查出所有 __SEED_ 占位密码（去重），供 DataInitializer 启动时识别需替换的明文 */
    @Select("SELECT DISTINCT password_hash FROM Customer WHERE LEFT(password_hash, 7) = '__SEED_'")
    List<String> findSeedPasswords();

    /** 将某个种子占位密码批量替换为真实 BCrypt 哈希 */
    @Update("UPDATE Customer SET password_hash = #{hash} WHERE password_hash = #{seed}")
    int replaceSeedPasswords(@Param("seed") String seed, @Param("hash") String hash);

    /** 按客户 id 更新密码哈希（修改密码用） */
    @Update("UPDATE Customer SET password_hash = #{hash} WHERE customer_id = #{id}")
    int updatePasswordHashById(@Param("id") Integer id, @Param("hash") String hash);
}
