package com.manage.club.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manage.club.dto.ClubAuditDTO;
import com.manage.club.dto.LoginDTO;
import com.manage.club.dto.NoticePublishDTO;
import com.manage.club.entity.Activity;
import com.manage.club.entity.Club;
import com.manage.club.entity.Notice;
import com.manage.club.entity.User;
import com.manage.club.service.*;
import com.manage.club.utils.JwtUtil;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对接前端：/admin/* 接口
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ClubService clubService;      // 注入社团服务

    @Autowired
    private ActivityService activityService; // 注入活动服务

    @Autowired
    private JwtUtil jwtUtil;                // 注入 JwtUtil

    @PostMapping("/login")
    public Result<Map<String, String>> adminLogin(@RequestBody LoginDTO dto) {
        try {
            String token = adminService.login(dto);
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private User getAdminUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("authHeader: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        System.out.println("userId: " + userId);
        if (userId == null) return null;
        User user = userService.getById(userId);
        System.out.println("user role: " + (user != null ? user.getRole() : "null"));
        if (user == null ||(user.getRole() != 1 && user.getRole() != 2)) {return null;
        }
        return user;
    }

    @GetMapping("/stats")
    public Result<Map<String, Integer>> getAdminStats(HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        return Result.success(adminService.getStats());
    }
    // 待审核社团列表
    @GetMapping("/club/list")
    public Result<Map<String, List<Club>>> getAuditClubList(@RequestParam Integer status) {
        List<Club> clubList = adminService.getAuditClubList();
        Map<String, List<Club>> data = new HashMap<>();
        data.put("list", clubList); // 前端用 res.data.list
        return Result.success(data);
    }

    // 所有社团列表（包括已通过和待审核）
    @GetMapping("/club/all")
    public Result<List<Club>> getAllClubs(HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        List<Club> clubs = clubService.list();
        return Result.success(clubs);
    }

    // 社团审核
    @PostMapping("/club/audit/{clubId}")
    public Result<Boolean> auditClub(@PathVariable Long clubId,
                                     @RequestBody ClubAuditDTO dto,
                                     HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        boolean success = adminService.auditClub(clubId, dto.getStatus(), dto.getReason());
        return success ? Result.success(true) : Result.error("审核失败");
    }

    // 获取所有活动
    @GetMapping("/activity/all")
    public Result<List<Activity>> getAllActivities(HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        List<Activity> activities = activityService.list();
        return Result.success(activities);
    }

    // 发布活动
    @PostMapping("/activity/add")
    public Result<Boolean> addActivity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 1. 权限校验
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }

        // 2. 提取参数
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String location = (String) params.get("location");
        String timeStr = (String) params.get("time");
        Integer maxPeople = (Integer) params.get("maxPeople");
        String clubName = (String) params.get("clubName");

        // 3. 非空校验
        if (title == null || content == null || location == null || timeStr == null || maxPeople == null || clubName == null) {
            return Result.error("参数不能为空");
        }

        // 4. 根据社团名称查询社团ID
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Club::getName, clubName)
                .eq(Club::getStatus, 1); // 只查已通过的社团
        Club club = clubService.getOne(wrapper);
        if (club == null) {
            return Result.error("社团不存在，请确认名称");
        }

        // 5. 解析时间字符串
        LocalDateTime time;
        try {
            time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return Result.error("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }

        // 6. 创建活动对象
        Activity activity = new Activity();
        activity.setTitle(title);
        activity.setContent(content);
        activity.setLocation(location);
        activity.setTime(time);
        activity.setMaxPeople(maxPeople);
        activity.setClubId(club.getId());
        activity.setCreateTime(LocalDateTime.now());
        activity.setStatus(1); // 默认上架

        // 7. 保存
        boolean success = activityService.save(activity);
        return success ? Result.success(true) : Result.error("发布失败");
    }
    // 删除活动
    @DeleteMapping("/activity/{activityId}")
    public Result<Boolean> deleteActivity(@PathVariable Long activityId,
                                          HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        boolean success = activityService.removeById(activityId);
        return success ? Result.success(true) : Result.error("删除失败");
    }
    // 获取所有通知
    @GetMapping("/notice/all")
    public Result<List<Notice>> getAllNotices(HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        List<Notice> notices = noticeService.list();
        return Result.success(notices);
    }

    // 发布通知（已有）
    @PostMapping("/notice/publish")
    public Result<Boolean> publishNotice(@RequestBody NoticePublishDTO dto,
                                         HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        boolean success = noticeService.publish(dto);
        return success ? Result.success(true) : Result.error("发布失败");
    }

    // 删除通知
    @DeleteMapping("/notice/{noticeId}")
    public Result<Boolean> deleteNotice(@PathVariable Long noticeId,
                                        HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        boolean success = noticeService.removeById(noticeId);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @GetMapping("/user/list")
    public Result<List<User>> getUserList(HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        List<User> users = userService.list();
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }

    // 启用/禁用用户
    @PutMapping("/user/{userId}/status")
    public Result<Boolean> updateUserStatus(@PathVariable Long userId,
                                            @RequestParam Integer status,
                                            HttpServletRequest request) {
        if (getAdminUser(request) == null) {
            return Result.error("无权限");
        }
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        boolean success = userService.updateById(user);
        return success ? Result.success(true) : Result.error("操作失败");
    }
    @GetMapping("/statistics/charts")
    public Result<Map<String, Object>> getStatisticsCharts(HttpServletRequest request) {
        if (getAdminUser(request) == null) return Result.error("无权限");
        Map<String, Object> result = new HashMap<>();
        // 用户增长（最近7天每天注册数）
        List<Map<String, Object>> userGrowth = userService.getUserGrowthLast7Days();
        // 活动热度（最近7天每天活动数）
        List<Map<String, Object>> activityHeat = activityService.getActivityHeatLast7Days();
        result.put("userGrowth", userGrowth);
        result.put("activityHeat", activityHeat);
        return Result.success(result);
    }
}