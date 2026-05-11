package com.manage.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.manage.club.dto.LoginDTO;
import com.manage.club.dto.RegisterDTO;
import com.manage.club.entity.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    // 用户登录
    Map<String, Object> login(LoginDTO dto);

    // 用户注册
    boolean register(RegisterDTO dto);

    // 根据用户名查询用户
    User getByUsername(String username);

    // 上传头像
    String uploadAvatar(Long userId, MultipartFile file);

    // 获取用户统计数据（已加入社团/已报名活动/未读通知）
    Map<String, Integer> getStats(Long userId);

    // 实现示例（MySQL）
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM user WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY DATE(create_time)")
    List<Map<String, Object>> getUserGrowthLast7Days();
}