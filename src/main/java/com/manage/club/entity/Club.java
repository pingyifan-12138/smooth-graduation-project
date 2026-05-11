package com.manage.club.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("club")
public class Club {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;// 社团名称
    @TableField("`desc`")
    private String desc;        // 社团描述
    private String logo;        // 社团Logo
    private String images;      // 社团相册（URL逗号分隔）
    private Long creatorId;// 创建者ID
    @TableField("`status`")
    private Integer status;     // 审核状态：0-待审核，1-通过，2-拒绝
    private String auditReason; // 拒绝原因
    private LocalDateTime createTime;
    // 非数据库字段：是否已加入
    @TableField(exist = false)
    private Boolean isJoined;
    @TableField(exist = false)
    private String creatorName;
}