package com.manage.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manage.club.dto.NoticePublishDTO;
import com.manage.club.entity.Notice;
import com.manage.club.entity.UserNoticeRead;
import com.manage.club.mapper.NoticeMapper;
import com.manage.club.mapper.UserNoticeReadMapper;
import com.manage.club.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private UserNoticeReadMapper userNoticeReadMapper;

    @Override
    public boolean publish(NoticePublishDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setTypeName(dto.getTypeName());
        notice.setPublisher(dto.getPublisher());
        notice.setPublishTime(LocalDateTime.now());
        return save(notice);
    }

    @Override
    public List<Notice> listByType(String type, Long userId) {
        // 查询所有符合类型的通知
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (!"all".equals(type)) {
            wrapper.eq(Notice::getType, type);
        }
        List<Notice> notices = list(wrapper);

        // 如果用户未登录，则所有通知均为未读
        if (userId == null) {
            notices.forEach(n -> n.setIsRead(false));
            return notices;
        }

        // 查询用户已读的通知ID
        LambdaQueryWrapper<UserNoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(UserNoticeRead::getUserId, userId)
                .select(UserNoticeRead::getNoticeId);
        List<Long> readIds = userNoticeReadMapper.selectList(readWrapper)
                .stream()
                .map(UserNoticeRead::getNoticeId)
                .collect(Collectors.toList());

        // 设置每个通知的已读状态
        for (Notice notice : notices) {
            notice.setIsRead(readIds.contains(notice.getId()));
        }
        return notices;
    }

    @Override
    public Notice getDetail(Long noticeId) {
        return getById(noticeId);
    }

    @Override
    public boolean markAsRead(Long userId, Long noticeId) {
        // 检查是否已经标记过
        LambdaQueryWrapper<UserNoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoticeRead::getUserId, userId)
                .eq(UserNoticeRead::getNoticeId, noticeId);
        if (userNoticeReadMapper.selectCount(wrapper) > 0) {
            return true; // 已标记过，视为成功
        }

        // 插入阅读记录
        UserNoticeRead record = new UserNoticeRead();
        record.setUserId(userId);
        record.setNoticeId(noticeId);
        record.setReadTime(LocalDateTime.now());
        return userNoticeReadMapper.insert(record) > 0;
    }
}