package com.example.springboot.mapper;

import com.example.springboot.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CustomerMapper {

    @Select("SELECT * FROM Customer ORDER BY customer_id")
    List<Customer> findAll();

    @Select("SELECT * FROM Customer WHERE customer_id = #{id}")
    Customer findById(@Param("id") Integer id);

    @Select("SELECT * FROM Customer WHERE phone = #{phone}")
    Customer findByPhone(@Param("phone") String phone);

    @Insert("INSERT INTO Customer (customer_name, phone, address, password_hash) " +
            "VALUES (#{customerName}, #{phone}, #{address}, #{passwordHash})")
    @Options(useGeneratedKeys = true, keyProperty = "customerId")
    int insert(Customer c);

    @Update("UPDATE Customer SET customer_name=#{customerName}, phone=#{phone}, address=#{address} " +
            "WHERE customer_id = #{customerId}")
    int update(Customer c);

    @Delete("DELETE FROM Customer WHERE customer_id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Select("SELECT DISTINCT password_hash FROM Customer WHERE LEFT(password_hash, 7) = '__SEED_'")
    List<String> findSeedPasswords();

    @Update("UPDATE Customer SET password_hash = #{hash} WHERE password_hash = #{seed}")
    int replaceSeedPasswords(@Param("seed") String seed, @Param("hash") String hash);

    @Update("UPDATE Customer SET password_hash = #{hash} WHERE customer_id = #{id}")
    int updatePasswordHashById(@Param("id") Integer id, @Param("hash") String hash);
}
