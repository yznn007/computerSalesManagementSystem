package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface StaffMapper {

    @Select("SELECT * FROM Staff WHERE staff_id = #{id}")
    Staff findById(@Param("id") Integer id);

    @Select("SELECT * FROM Staff WHERE username = #{username}")
    Staff findByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM Staff")
    long countAll();

    @Insert("INSERT INTO Staff (username, password_hash, staff_name) VALUES (#{username}, #{passwordHash}, #{staffName})")
    @Options(useGeneratedKeys = true, keyProperty = "staffId")
    int insert(Staff s);

    @Update("UPDATE Staff SET username=#{username}, staff_name=#{staffName} WHERE staff_id = #{staffId}")
    int update(Staff s);

    @Select("SELECT DISTINCT password_hash FROM Staff WHERE LEFT(password_hash, 7) = '__SEED_'")
    List<String> findSeedPasswords();

    @Update("UPDATE Staff SET password_hash = #{hash} WHERE password_hash = #{seed}")
    int replaceSeedPasswords(@Param("seed") String seed, @Param("hash") String hash);

    @Update("UPDATE Staff SET password_hash = #{hash} WHERE staff_id = #{id}")
    int updatePasswordHashById(@Param("id") Integer id, @Param("hash") String hash);
}
