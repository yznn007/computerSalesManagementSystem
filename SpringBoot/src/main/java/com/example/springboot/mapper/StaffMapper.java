package com.example.springboot.mapper;

import com.example.springboot.entity.Staff;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 销售员数据访问层。
 * 提供登录查询、种子密码替换，以及 DataInitializer 判断是否需创建默认 admin 的计数方法。
 */
public interface StaffMapper {

    /** 按主键查销售员 */
    @Select("SELECT * FROM Staff WHERE staff_id = #{id}")
    Staff findById(@Param("id") Integer id);

    /** 按用户名查销售员：登录依据（username 唯一） */
    @Select("SELECT * FROM Staff WHERE username = #{username}")
    Staff findByUsername(@Param("username") String username);

    /** 统计销售员总数：DataInitializer 用其判断是否需要插入默认 admin/admin */
    @Select("SELECT COUNT(*) FROM Staff")
    long countAll();

    /** 新增销售员，自增主键回填到 staffId */
    @Insert("INSERT INTO Staff (username, password_hash, staff_name) VALUES (#{username}, #{passwordHash}, #{staffName})")
    @Options(useGeneratedKeys = true, keyProperty = "staffId")
    int insert(Staff s);

    /** 更新销售员资料（不含密码） */
    @Update("UPDATE Staff SET username=#{username}, staff_name=#{staffName} WHERE staff_id = #{staffId}")
    int update(Staff s);

    /** 查出所有 __SEED_ 占位密码，供 DataInitializer 替换为真实哈希 */
    @Select("SELECT DISTINCT password_hash FROM Staff WHERE LEFT(password_hash, 7) = '__SEED_'")
    List<String> findSeedPasswords();

    /** 将某个种子占位密码批量替换为真实 BCrypt 哈希 */
    @Update("UPDATE Staff SET password_hash = #{hash} WHERE password_hash = #{seed}")
    int replaceSeedPasswords(@Param("seed") String seed, @Param("hash") String hash);

    /** 按销售员 id 更新密码哈希 */
    @Update("UPDATE Staff SET password_hash = #{hash} WHERE staff_id = #{id}")
    int updatePasswordHashById(@Param("id") Integer id, @Param("hash") String hash);
}
