package com.manage.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manage.club.entity.Club;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClubMapper extends BaseMapper<Club> {
    // 分页查询社团（支持关键词搜索）
    IPage<Club> selectByKeyword(Page<Club> page, @Param("keyword") String keyword);

    // 查询待审核社团（status=0）
    @Select("select * from club where status = 0")
    List<Club> selectAuditList();

    // 根据用户ID查询已加入的社团
    @Select("select c.* from club c join user_club uc on c.id = uc.club_id where uc.user_id = #{userId}")
    List<Club> selectJoinByUserId(@Param("userId") Long userId);

    // 根据社团ID查询社团名称
    @Select("select name from club where id = #{clubId}")
    String selectClubNameById(@Param("clubId") Long clubId);
    // 新增：查询用户管理的社团（is_admin=1）
    @Select("SELECT c.* FROM club c INNER JOIN user_club uc ON c.id = uc.club_id WHERE uc.user_id = #{userId} AND uc.is_admin = 1")
    List<Club> selectManagedClubs(@Param("userId") Long userId);
    @Select("SELECT c.*, u.name as creator_name FROM club c LEFT JOIN user u ON c.creator_id = u.id WHERE c.status = 0")
    List<Club> selectAuditListWithCreator();
}