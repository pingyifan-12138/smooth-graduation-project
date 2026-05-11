package com.manage.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.manage.club.dto.NoticePublishDTO;
import com.manage.club.entity.Notice;

import java.util.List;

public interface NoticeService extends IService<Notice> {
    boolean publish(NoticePublishDTO dto);
    List<Notice> listByType(String type, Long userId);   // 增加 userId
    Notice getDetail(Long noticeId);
    boolean markAsRead(Long userId, Long noticeId);      // 增加 userId
}