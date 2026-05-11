package com.manage.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.manage.club.dto.LoginDTO;
import com.manage.club.entity.Club;
import com.manage.club.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService extends IService<User> {

    // 管理员登录
    String login(LoginDTO dto);

    // 获取后台统计数据
    Map<String, Integer> getStats();

    // 获取待审核社团列表（status=0）
    List<Club> getAuditClubList();

    // 社团审核
    boolean auditClub(Long clubId, Integer status, String reason);
}