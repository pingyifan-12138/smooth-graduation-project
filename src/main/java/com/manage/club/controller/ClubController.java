package com.manage.club.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.manage.club.entity.Activity;
import com.manage.club.entity.Club;
import com.manage.club.entity.User;
import com.manage.club.mapper.UserClubMapper;
import com.manage.club.service.ActivityService;
import com.manage.club.service.ClubService;
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
@RequestMapping("/club")
public class ClubController {

    @Autowired
    private ClubService clubService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserClubMapper userClubMapper;

    private Long getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    //社团列表
    @GetMapping("/list")
    public Result<List<Club>> getClubList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        IPage<Club> page = clubService.listByKeyword(pageNum, pageSize, keyword);
        return Result.success(page.getRecords());
    }

    // 社团详情（包含是否已加入）
    @GetMapping("/detail/{clubId}")
    public Result<Club> getClubDetail(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = getUserId(request);
        Club club = clubService.getDetail(clubId, userId);
        if (club == null) {
            return Result.error("社团不存在");
        }
        return Result.success(club);
    }

    // 加入社团
    @PostMapping("/join")
    public Result<Boolean> joinClub(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long clubId = params.get("clubId");
        Long userId = getUserId(request);
        boolean success = clubService.joinClub(userId, clubId);
        return success ? Result.success(true) : Result.error("加入失败或已加入");
    }

    // 退出社团
    @PostMapping("/quit")
    public Result<Boolean> quitClub(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long clubId = params.get("clubId");
        Long userId = getUserId(request);
        boolean success = clubService.quitClub(userId, clubId);
        return success ? Result.success(true) : Result.error("退出失败");
    }

    // 上传社团相册图片（单张）
    @PostMapping("/uploadAlbum")
    public Result<String> uploadAlbum(@RequestParam("file") MultipartFile file) {
        System.out.println("=== updateClub 被调用 ===");
        String imageUrl = fileUtil.uploadImage(file);
        return imageUrl != null ? Result.success(imageUrl) : Result.error("上传失败");
    }

    // 上传社团图片
    @PostMapping("/updateImages")
    public Result<Boolean> updateImages(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        System.out.println("=== updateClub 被调用 ===");
        Long clubId = Long.valueOf(params.get("clubId").toString());
        String images = (String) params.get("images");
        Long userId = getUserId(request);
        if (!clubService.isClubAdmin(userId, clubId)) {
            return Result.error("无权限");
        }
        Club club = new Club();
        club.setId(clubId);
        club.setImages(images);
        boolean success = clubService.updateById(club);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    // 创建社团申请
    @PostMapping("/add")
    public Result<Boolean> addClub(@RequestBody Club club, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            if (userId == null) {
                return Result.error("未登录");
            }
            club.setCreatorId(userId);
            club.setStatus(0);               // 待审核
            club.setCreateTime(LocalDateTime.now());
            boolean success = clubService.save(club);
            return success ? Result.success(true) : Result.error("创建失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器错误：" + e.getMessage());
        }


    }


    // 获取当前用户管理的社团列表（供前端展示）
    @GetMapping("/managed")
    public Result<List<Club>> getManagedClubs(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        List<Club> clubs = clubService.getManagedClubs(userId);
        return Result.success(clubs);
    }

    // 社团管理员发布活动（接口路径 /club/activity/add）
    @PostMapping("/activity/add")
    public Result<Boolean> addActivityByClubAdmin(@RequestBody Activity activity, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        // 校验是否为该社团管理员
        if (!clubService.isClubAdmin(userId, activity.getClubId())) {
            return Result.error("无权限发布此社团的活动");
        }
        activity.setCreateTime(LocalDateTime.now());
        activity.setStatus(1); // 默认上架
        boolean success = activityService.save(activity);
        return success ? Result.success(true) : Result.error("发布失败");
    }

    // 获取社团成员列表（包括待审核和已加入）
    @GetMapping("/{clubId}/members")
    public Result<List<User>> getMembers(@PathVariable Long clubId, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        // 校验是否为该社团管理员
        if (!clubService.isClubAdmin(userId, clubId)) {
            return Result.error("无权限");
        }
        // 通过注入的实例调用方法
        List<User> members = userClubMapper.selectMembersByClubId(clubId);
        return Result.success(members);
    }
    // 审核入社申请
    @PostMapping("/join/audit")
    public Result<Boolean> auditJoin(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long clubId = Long.valueOf(params.get("clubId").toString());
        Long targetUserId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString()); // 1-通过, 2-拒绝
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");
        if (!clubService.isClubAdmin(userId, clubId)) {
            return Result.error("无权限");
        }
        boolean success = clubService.updateJoinStatus(clubId, targetUserId, status);
        return success ? Result.success(true) : Result.error("操作失败");
    }
    // 更新社团信息
    @PutMapping("/update")
    public Result<Boolean> updateClub(@RequestBody Club club, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) return Result.error("未登录");

        Club oldClub = clubService.getById(club.getId());
        if (oldClub == null) return Result.error("社团不存在");

        // 打印日志（可选）
        System.out.println("=== 更新社团 ===");
        System.out.println("社团ID: " + club.getId());
        System.out.println("images字段内容: " + club.getImages());
        System.out.println("images字段长度: " + (club.getImages() == null ? 0 : club.getImages().length()));

        oldClub.setName(club.getName());
        oldClub.setDesc(club.getDesc());
        oldClub.setLogo(club.getLogo());
        oldClub.setImages(club.getImages());

        try {
            boolean success = clubService.updateById(oldClub);
            System.out.println("更新结果: " + success);
            return success ? Result.success(true) : Result.error("更新失败，数据库无变化");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新异常: " + e.getMessage());
        }
    }
    // 上传社团 Logo（单张图片）
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUtil.uploadImage(file);
        if (imageUrl == null) {
            return Result.error("上传失败");
        }
        return Result.success(imageUrl);
    }
}