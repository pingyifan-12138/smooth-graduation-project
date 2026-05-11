package com.manage.club.dto;

import lombok.Data;

@Data
public class ClubAuditDTO {
    private Integer status;  // 1-通过，2-拒绝
    private String reason;   // 拒绝原因（状态为2时必填）
}