package com.manage.club.controller;

import com.manage.club.entity.Activity;
import com.manage.club.entity.User;
import com.manage.club.service.ActivityService;
import com.manage.club.service.ClubService;
import com.manage.club.service.UserService;
import com.manage.club.utils.JwtUtil;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/clubadmin")
public class ClubAdminController {

    @Autowired
    private ClubService clubService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    /**
     * 从请求头中解析用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    private boolean isClubAdmin(Long userId, Long clubId) {
        return clubService.isClubAdmin(userId, clubId);
    }

    // 获取某个活动的报名列表（仅社团管理员可查看）
    @GetMapping("/activity/{activityId}/signups")
    public Result<List<User>> getActivitySignups(@PathVariable Long activityId, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        Activity activity = activityService.getById(activityId);
        if (activity == null) return Result.error("活动不存在");
        if (!isClubAdmin(userId, activity.getClubId())) return Result.error("无权限");
        List<User> users = activityService.getSignupUsers(activityId);
        return Result.success(users);
    }
}