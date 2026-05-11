package com.manage.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_activity")
public class UserActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;      // 用户ID
    private Long activityId;  // 活动ID
    private LocalDateTime signupTime; // 报名时间
}