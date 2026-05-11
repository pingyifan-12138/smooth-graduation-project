package com.manage.club.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manage.club.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    // 根据社团ID查询活动
    @Select("select * from activity where club_id = #{clubId} and status = 1")
    List<Activity> selectByClubId(@Param("clubId") Long clubId);

    // 根据用户ID查询已报名的活动
    @Select("select a.* from activity a join user_activity ua on a.id = ua.activity_id where ua.user_id = #{userId}")
    List<Activity> selectSignupByUserId(@Param("userId") Long userId);

    // 筛选活动（all/upcoming/ended）
    List<Activity> selectByType(@Param("type") String type);
}