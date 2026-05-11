package com.manage.club.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;  // 学号/账号
    private String password;  // 密码（MD5加密）
    private String name;      // 真实姓名
    private String phone;     // 手机号
    private String avatarUrl; // 头像URL
    private String nickname;  // 昵称
    private String school;    // 学校
    private Integer role;     // 角色：0-学生，1-管理员
    private Integer status;   // 状态：0-正常，1-禁用
    private Integer auditStatus; // 审核状态：0-待审核，1-通过，2-拒绝
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
