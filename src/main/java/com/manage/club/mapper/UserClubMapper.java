package com.manage.club.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manage.club.entity.User;
import com.manage.club.entity.UserClub;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserClubMapper extends BaseMapper<UserClub> {
    // 判断用户是否已加入社团
    @Select("select count(*) from user_club where user_id = #{userId} and club_id = #{clubId}")
    int isJoined(@Param("userId") Long userId, @Param("clubId") Long clubId);

    // 根据用户ID和社团ID删除关联
    @Delete("DELETE FROM user_club WHERE user_id = #{userId} AND club_id = #{clubId}")
    int deleteByUserIdAndClubId(@Param("userId") Long userId, @Param("clubId") Long clubId);

    // 查询待审核入社申请的用户列表
    @Select("SELECT u.* FROM user u INNER JOIN user_club uc ON u.id = uc.user_id WHERE uc.club_id = #{clubId} AND uc.status = 0")
    List<User> selectJoinRequests(@Param("clubId") Long clubId);
    // 查询社团成员（关联 user 表，并获取 user_club 的 status）
    @Select("SELECT u.*, uc.status as join_status FROM user u " +
            "INNER JOIN user_club uc ON u.id = uc.user_id " +
            "WHERE uc.club_id = #{clubId}")
    List<User> selectMembersByClubId(@Param("clubId") Long clubId);
}