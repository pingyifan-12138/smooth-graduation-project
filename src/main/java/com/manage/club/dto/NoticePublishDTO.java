package com.manage.club.dto;

import lombok.Data;

@Data
public class NoticePublishDTO {
    private String title;       // 通知标题
    private String content;     // 通知内容
    private String type;        // 类型：system/activity/club
    private String typeName;    // 类型名称：系统/活动/社团
    private String publisher;   // 发布人
}