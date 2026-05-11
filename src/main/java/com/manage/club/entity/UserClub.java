package com.manage.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_club")
public class UserClub {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;    // 用户ID
    private Long clubId;    // 社团ID
    private LocalDateTime joinTime; // 加入时间
    private Integer isAdmin;  // 0-普通成员，1-管理员
    private Integer status;  // 0待审核，1已加入，2已拒绝
}