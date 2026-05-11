package com.manage.club.controller;

import com.manage.club.entity.Activity;
import com.manage.club.entity.Club;
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
import java.util.Map;

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

    /**
     * 获取管理的社团列表
     * GET /clubadmin/my-clubs
     */
    @GetMapping("/my-clubs")
    public Result<List<Club>> getMyManagedClubs(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        List<Club> clubs = clubService.getManagedClubs(userId);
        return Result.success(clubs);
    }

    /**
     * 社团管理员发布活动
     * POST /clubadmin/activity/add
     */
    @PostMapping("/activity/add")
    public Result<Boolean> addActivity(@RequestBody Activity activity, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        // 校验是否为该社团管理员
        if (!isClubAdmin(userId, activity.getClubId())) {
            return Result.error("无权限发布此社团的活动");
        }

        activity.setCreateTime(java.time.LocalDateTime.now());
        activity.setStatus(1); // 默认上架

        boolean success = activityService.save(activity);
        return success ? Result.success(true) : Result.error("发布失败");
    }

    /**
     * 更新社团信息（名称、简介、Logo、相册）
     * PUT /clubadmin/club/update
     */
    @PutMapping("/club/update")
    public Result<Boolean> updateClub(@RequestBody Club club, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        Club oldClub = clubService.getById(club.getId());
        if (oldClub == null) {
            return Result.error("社团不存在");
        }

        // 权限校验：必须是该社团的管理员
        if (!isClubAdmin(userId, club.getId())) {
            return Result.error("无权限操作此社团");
        }

        oldClub.setName(club.getName());
        oldClub.setDesc(club.getDesc());
        oldClub.setLogo(club.getLogo());
        oldClub.setImages(club.getImages());

        try {
            boolean success = clubService.updateById(oldClub);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新异常: " + e.getMessage());
        }
    }

    /**
     * 更新社团相册
     * PUT /clubadmin/club/update-images
     */
    @PutMapping("/club/update-images")
    public Result<Boolean> updateImages(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        Long clubId = Long.valueOf(params.get("clubId").toString());
        String images = (String) params.get("images");

        // 权限校验
        if (!isClubAdmin(userId, clubId)) {
            return Result.error("无权限操作此社团");
        }

        Club club = new Club();
        club.setId(clubId);
        club.setImages(images);
        boolean success = clubService.updateById(club);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 审核入社申请
     * POST /clubadmin/join/audit
     */
    @PostMapping("/join/audit")
    public Result<Boolean> auditJoin(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }

        Long clubId = Long.valueOf(params.get("clubId").toString());
        Long targetUserId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString()); // 1-通过, 2-拒绝

        // 权限校验
        if (!isClubAdmin(userId, clubId)) {
            return Result.error("无权限审核此社团的申请");
        }

        boolean success = clubService.updateJoinStatus(clubId, targetUserId, status);
        return success ? Result.success(true) : Result.error("操作失败");
    }
}
