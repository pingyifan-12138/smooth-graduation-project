package com.manage.club.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;       // 通知标题
    private String content;     // 通知内容
    private String type;        // 类型：system/activity/club
    private String typeName;    // 类型名称：系统/活动/社团
    private String publisher;   // 发布人
    private LocalDateTime publishTime; // 发布时间
    @TableField(exist = false)
    public Boolean isRead;// 是否已读（前端展示用）
}