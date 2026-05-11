package com.manage.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manage.club.entity.User;          // 添加这一行
import com.manage.club.entity.UserActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserActivityMapper extends BaseMapper<UserActivity> {
    // 判断用户是否已报名活动
    @Select("select count(*) from user_activity where user_id = #{userId} and activity_id = #{activityId}")
    int isSignedUp(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 根据活动ID查询报名用户列表
    @Select("SELECT u.* FROM user u INNER JOIN user_activity ua ON u.id = ua.user_id WHERE ua.activity_id = #{activityId}")
    List<User> selectUsersByActivityId(@Param("activityId") Long activityId);
}