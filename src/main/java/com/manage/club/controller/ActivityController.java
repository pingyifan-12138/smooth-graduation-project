package com.manage.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manage.club.entity.Activity;
import com.manage.club.entity.UserClub;
import com.manage.club.mapper.UserClubMapper;
import com.manage.club.service.ActivityService;
import com.manage.club.utils.FileUtil;
import com.manage.club.utils.JwtUtil;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private ActivityService activityService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserClubMapper userClubMapper; // 注入 Mapper 用于权限校验
    private Long getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    // 活动列表
    @GetMapping("/list")
    public Result<List<Activity>> getActivityList(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Long clubId,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Activity> activities;
        if (clubId != null) {
            activities = activityService.listByClubId(clubId, userId);
        } else {
            activities = activityService.listByType(type, userId);
        }
        return Result.success(activities);
    }

    // 报名
    @PostMapping("/signup")
    public Result<Boolean> signup(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long activityId = params.get("activityId");
        Long userId = getUserId(request);
        boolean success = activityService.signup(userId, activityId);
        return success ? Result.success(true) : Result.error("已报名该活动");
    }

    // 取消报名
    @PostMapping("/cancel")
    public Result<Boolean> cancelSignup(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long activityId = params.get("activityId");
        if (userId == null || activityId == null) {
            return Result.error("参数错误");
        }
        boolean success = activityService.cancelSignup(userId, activityId);
        return success ? Result.success(true) : Result.error("取消失败");
    }

    // 社团管理员发布活动
    @PostMapping("/clubAdmin/add")
    public Result<Boolean> addActivityByClubAdmin(@RequestBody Activity activity, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        // 检查用户是否是该社团的管理员（is_admin = 1）
        LambdaQueryWrapper<UserClub> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserClub::getUserId, userId)
                .eq(UserClub::getClubId, activity.getClubId())
                .eq(UserClub::getIsAdmin, 1);
        boolean isAdmin = userClubMapper.selectCount(wrapper) > 0;
        if (!isAdmin) {
            return Result.error("无权限发布此社团的活动");
        }
        activity.setCreateTime(LocalDateTime.now());
        activity.setStatus(1);
        boolean success = activityService.save(activity);
        return success ? Result.success(true) : Result.error("发布失败");
    }
    @GetMapping("/detail")
    public Result<Activity> getActivityDetail(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        Activity activity = activityService.getById(id);
        if (activity == null) return Result.error("活动不存在");
        boolean isSignedUp = activityService.isSignedUp(userId, id);
        activity.setIsSignedUp(isSignedUp);
        if (userId != null) {
            activity.setIsSignedUp(activityService.isSignedUp(userId, id));
        } else {
            activity.setIsSignedUp(false);
        }
        return Result.success(activity);

    }
    @PostMapping("/uploadCover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUtil.uploadImage(file);
        if (imageUrl == null) return Result.error("上传失败");
        return Result.success(imageUrl);
    }
}