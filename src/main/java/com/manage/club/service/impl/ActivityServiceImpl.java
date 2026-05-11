package com.manage.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manage.club.entity.Activity;
import com.manage.club.entity.UserActivity;
import com.manage.club.mapper.ActivityMapper;
import com.manage.club.mapper.UserActivityMapper;
import com.manage.club.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.manage.club.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private UserActivityMapper userActivityMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;   // 注入 JdbcTemplate，用于原生 SQL 插入

    @Override
    public List<Activity> listByType(String type, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (!"all".equals(type)) {
            wrapper.eq(Activity::getStatus, type);
        }
        Page<Activity> page = this.page(new Page<>(1, 100), wrapper);
        return page.getRecords();
    }

    @Override
    public List<Activity> listByClubId(Long clubId, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getClubId, clubId);
        Page<Activity> page = this.page(new Page<>(1, 100), wrapper);
        return page.getRecords();
    }
    @Override
    public boolean signup(Long userId, Long activityId) {
        if (userId == null || activityId == null) return false;

        // 检查是否已报名
        LambdaQueryWrapper<UserActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivity::getUserId, userId)
                .eq(UserActivity::getActivityId, activityId);
        if (userActivityMapper.selectCount(wrapper) > 0) {
            return false;
        }

        // 获取活动信息，检查人数上限
        Activity activity = this.getById(activityId);
        if (activity == null) return false;
        // 统计当前报名人数
        long currentCount = userActivityMapper.selectCount(
                new LambdaQueryWrapper<UserActivity>().eq(UserActivity::getActivityId, activityId)
        );
        if (currentCount >= activity.getMaxPeople()) {
            return false;  // 人数已满
        }

        // 插入报名记录
        String sql = "INSERT INTO user_activity (user_id, activity_id, signup_time) VALUES (?, ?, NOW())";
        int rows = jdbcTemplate.update(sql, userId, activityId);
        return rows > 0;
    }

    @Override
    public List<Activity> getSignupList(Long userId) {
        LambdaQueryWrapper<UserActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivity::getUserId, userId);
        List<UserActivity> userActivities = userActivityMapper.selectList(wrapper);

        List<Long> activityIds = userActivities.stream()
                .map(UserActivity::getActivityId)
                .collect(Collectors.toList());

        if (activityIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.listByIds(activityIds);
    }
    @Override
    public boolean cancelSignup(Long userId, Long activityId) {
        // 删除 user_activity 表中的报名记录
        LambdaQueryWrapper<UserActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivity::getUserId, userId)
                .eq(UserActivity::getActivityId, activityId);
        return userActivityMapper.delete(wrapper) > 0;
    }
    @Override
    public boolean isSignedUp(Long userId, Long activityId) {
        LambdaQueryWrapper<UserActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivity::getUserId, userId)
                .eq(UserActivity::getActivityId, activityId);
        return userActivityMapper.selectCount(wrapper) > 0;
    }
    @Override
    public List<User> getSignupUsers(Long activityId) {
        return userActivityMapper.selectUsersByActivityId(activityId);
    }
    @Override
    public List<Map<String, Object>> getActivityHeatLast7Days() {
        String sql = "SELECT DATE(create_time) as date, COUNT(*) as count FROM activity WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) GROUP BY DATE(create_time)";
        return jdbcTemplate.queryForList(sql);
    }
}