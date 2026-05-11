package com.manage.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.manage.club.entity.Activity;
import com.manage.club.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import com.manage.club.entity.User;
public interface ActivityService extends IService<Activity> {
    // 活动列表（支持筛选：all/upcoming/ended）
    List<Activity> listByType(String type, Long userId);

    // 根据社团ID查询活动（含是否已报名状态）
    List<Activity> listByClubId(Long clubId, Long userId);

    // 活动报名
    boolean signup(Long userId, Long activityId);

    // 获取用户已报名的活动
    List<Activity> getSignupList(Long userId);
    // 取消报名
    boolean cancelSignup(Long userId, Long activityId);
    //用户报名
    boolean isSignedUp(Long userId, Long activityId);
    List<User> getSignupUsers(Long activityId);
    // ActivityService.java
    List<Map<String, Object>> getActivityHeatLast7Days();

}