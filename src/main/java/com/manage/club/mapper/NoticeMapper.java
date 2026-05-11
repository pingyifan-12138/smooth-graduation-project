package com.manage.club.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manage.club.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {


    // 根据类型查询通知
    // List<Notice> selectByType(@Param("type") String type);

    // 标记已读
    // int markAsRead(@Param("noticeId") Long noticeId);

    // 查询未读数量
    // int selectUnreadCount();
}