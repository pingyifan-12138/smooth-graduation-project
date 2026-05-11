package com.manage.club.controller;

import com.manage.club.entity.Notice;
import com.manage.club.service.NoticeService;
import com.manage.club.utils.JwtUtil;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 对接前端：/notice/* 接口
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JwtUtil jwtUtil; // 注入 JwtUtil

    /**
     * 从请求头中解析用户ID
     * @param request HttpServletRequest
     * @return 用户ID，如果未登录或解析失败返回null
     */
    private Long getUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }

    // 通知列表（支持筛选）：GET /notice/list
    @GetMapping("/list")
    public Result<List<Notice>> getNoticeList(@RequestParam String type, HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Notice> notices = noticeService.listByType(type, userId);
        return Result.success(notices);
    }

    // 通知详情：GET /notice/detail/{noticeId}
    @GetMapping("/detail/{noticeId}")
    public Result<Notice> getNoticeDetail(@PathVariable Long noticeId) {
        return Result.success(noticeService.getDetail(noticeId));
    }

    // 标记已读：POST /notice/read
    @PostMapping("/read")
    public Result<Boolean> markAsRead(@RequestBody Map<String, Long> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long noticeId = params.get("noticeId");
        if (userId == null || noticeId == null) {
            return Result.error("参数错误");
        }
        boolean success = noticeService.markAsRead(userId, noticeId);
        return success ? Result.success(true) : Result.error("标记失败");
    }
}