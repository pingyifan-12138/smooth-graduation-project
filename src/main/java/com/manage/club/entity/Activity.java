package com.manage.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long clubId;        // 社团ID
    private Integer status;     // 状态：0-下架，1-上架
    private String title;       // 活动标题
    private String coverImage;  // 活动封面图
    private String images;      // 活动相册（URL逗号分隔）
    private String content;     // 活动内容
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time; // 活动时间
    private String location;    // 活动地点
    private Integer maxPeople;  // 最大人数
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Boolean isSignedUp;

    @TableField(exist = false)
    private String clubName;
    @TableField(exist = false)
    private String activityName;
}