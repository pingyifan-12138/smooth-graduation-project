package com.manage.club.controller;
import com.manage.club.entity.Activity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manage.club.entity.Club;
import com.manage.club.entity.User;
import com.manage.club.entity.UserActivity;
import com.manage.club.service.ActivityService;
import com.manage.club.service.ClubService;
import com.manage.club.service.UserService;
import com.manage.club.utils.JwtUtil;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ActivityService activityService;

    /**
     * 从请求头中获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = userService.getOne(wrapper);

        if (existUser == null) {
            return Result.error("用户名不存在");
        }
        if (!existUser.getPassword().equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(existUser.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", existUser);
        return Result.success(map);
    }

    // 注册
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User exist = userService.getOne(wrapper);
        if (exist != null) {
            return Result.error("用户名已存在");
        }
        user.setRole(0);
        user.setStatus(0);
        user.setAuditStatus(1);
        userService.save(user);
        return Result.success(true);
    }

    // 获取当前用户信息
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    // 获取用户统计数据（社团数、活动数、通知数）
    @GetMapping("/stats")
    public Result<Map<String, Integer>> stats(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        Map<String, Integer> stats = new HashMap<>();
        int clubCount = clubService.getJoinList(userId).size();
        int activityCount = activityService.getSignupList(userId).size();
        int noticeCount = 0; // 如需实现可扩展
        stats.put("clubCount", clubCount);
        stats.put("activityCount", activityCount);
        stats.put("noticeCount", noticeCount);
        return Result.success(stats);
    }

    // 获取用户已加入的社团列表
    @GetMapping("/my-clubs")
    public Result<List<Club>> myClubs(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        List<Club> clubs = clubService.getJoinList(userId);
        return Result.success(clubs);
    }

    // 获取用户已报名的活动列表
    @GetMapping("/my-activities")
    public Result<List<Activity>> myActivities(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        List<Activity> activities = activityService.getSignupList(userId);
        return Result.success(activities);
    }

    // 上传头像
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        String avatarUrl = userService.uploadAvatar(userId, file);
        if (avatarUrl == null) {
            return Result.error("上传失败");
        }
        return Result.success(avatarUrl);
    }

    @PutMapping("/updateProfile")
    public Result<Boolean> updateProfile(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        String nickname = (String) params.get("nickname");
        String phone = (String) params.get("phone");
        String school = (String) params.get("school");
        User user = new User();
        user.setId(userId);
        if (nickname != null) user.setNickname(nickname);
        if (phone != null) user.setPhone(phone);
        if (school != null) user.setSchool(school);
        boolean success = userService.updateById(user);
        return success ? Result.success(true) : Result.error("更新失败");
    }
}