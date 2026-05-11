package com.manage.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manage.club.dto.LoginDTO;
import com.manage.club.dto.RegisterDTO;
import com.manage.club.entity.User;
import com.manage.club.mapper.UserMapper;
import com.manage.club.service.ActivityService;
import com.manage.club.service.ClubService;
import com.manage.club.service.UserService;
import com.manage.club.utils.FileUtil;
import com.manage.club.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClubService clubService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private FileUtil fileUtil;  // 注入文件工具类
    @Autowired
    private JdbcTemplate jdbcTemplate;
    // 1. 登录
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("token", jwtUtil.generateToken(user.getId()));
        map.put("user", user);
        return map;
    }

    // 2. 注册
    @Override
    public boolean register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());   // 实际应加密，这里保持原样（可后续优化）
        user.setRole(0);
        user.setStatus(0);
        user.setAuditStatus(1);

        return userMapper.insert(user) > 0;
    }

    // 3. 根据用户名查询
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    // 4. 上传头像（完整实现）
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 保存文件，获取相对路径（如 /uploads/xxx.jpg）
        String relativePath = fileUtil.uploadImage(file);
        if (relativePath == null) {
            return null;
        }

        // 2. 更新数据库中的 avatarUrl 字段
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(relativePath);
        userMapper.updateById(user);

        // 3. 返回相对路径（前端会拼接 baseUrl 使用）
        return relativePath;
    }

    // 5. 获取用户统计数据（真实统计）
    @Override
    public Map<String, Integer> getStats(Long userId) {
        Map<String, Integer> stats = new HashMap<>();

        // 获取用户已加入的社团数量
        int clubCount = clubService.getJoinList(userId).size();

        // 获取用户已报名的活动数量
        int activityCount = activityService.getSignupList(userId).size();

        // 未读通知数量（如需实现可扩展，暂返回0）
        int noticeCount = 0;

        stats.put("clubCount", clubCount);
        stats.put("activityCount", activityCount);
        stats.put("noticeCount", noticeCount);

        return stats;
    }
    @Override
    public List<Map<String, Object>> getUserGrowthLast7Days() {
        String sql = "SELECT DATE(create_time) as date, COUNT(*) as count FROM user WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY DATE(create_time)";
        return jdbcTemplate.queryForList(sql);
    }
}