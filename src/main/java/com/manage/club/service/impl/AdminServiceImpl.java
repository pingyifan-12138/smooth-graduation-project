package com.manage.club.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manage.club.dto.LoginDTO;
import com.manage.club.entity.User;
import com.manage.club.entity.Club;
import com.manage.club.mapper.UserMapper;
import com.manage.club.mapper.ClubMapper;
import com.manage.club.mapper.ActivityMapper;
import com.manage.club.mapper.NoticeMapper;
import com.manage.club.service.AdminService;
import com.manage.club.service.ClubService;
import com.manage.club.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<UserMapper, User> implements AdminService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ClubService clubService;
    @Autowired
    private ClubMapper clubMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private NoticeMapper noticeMapper;

    private static final String SALT = "clubManage2024";

    @Override
    public String login(LoginDTO dto) {
        // 1. 查询管理员（role=1）
        User admin = userMapper.selectByUsername(dto.getUsername());
        if (admin == null || admin.getRole() != 1&& admin.getRole() != 2) {
            throw new RuntimeException("账号不存在或无权限");
        }
        // 2. 校验密码
        String encryptPwd = DigestUtils.md5DigestAsHex((dto.getPassword() + SALT).getBytes());
        if (!encryptPwd.equals(admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 3. 生成Token
        return jwtUtil.generateToken(admin.getId());
    }

    @Override
    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        // 用户总数
        stats.put("userCount", Math.toIntExact(count()));
        // 社团总数
        stats.put("clubCount", Math.toIntExact(clubMapper.selectCount(null)));
        // 活动总数
        stats.put("activityCount", Math.toIntExact(activityMapper.selectCount(null)));
        // 通知总数
        stats.put("noticeCount", Math.toIntExact(noticeMapper.selectCount(null)));
        return stats;
    }

    @Override
    public List<Club> getAuditClubList() {
        return clubService.getAuditListWithCreator();
    }
    @Override
    public boolean auditClub(Long clubId, Integer status, String reason) {
        Club club = new Club();
        club.setId(clubId);
        club.setStatus(status);
        club.setAuditReason(reason);
        return clubService.updateById(club);
    }
}