package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface StaffMapper {

    @Select("SELECT * FROM Staff ORDER BY staff_id")
    List<Staff> findAll();

    @Select("SELECT * FROM Staff WHERE username = #{username}")
    Staff findByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM Staff")
    long countAll();

    @Insert("INSERT INTO Staff (username, password_hash, staff_name) VALUES (#{username}, #{passwordHash}, #{staffName})")
    @Options(useGeneratedKeys = true, keyProperty = "staffId")
    int insert(Staff s);

    @Update("UPDATE Staff SET username=#{username}, staff_name=#{staffName} WHERE staff_id = #{staffId}")
    int update(Staff s);

    @Delete("DELETE FROM Staff WHERE staff_id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Update("UPDATE Staff SET password_hash = #{hash} WHERE password_hash = #{seed}")
    int replaceSeedPasswords(@Param("seed") String seed, @Param("hash") String hash);
}
