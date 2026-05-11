package com.manage.club.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manage.club.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 根据用户名查询用户
    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);
}