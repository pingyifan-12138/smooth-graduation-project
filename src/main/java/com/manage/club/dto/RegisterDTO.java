package com.manage.club.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username; // 学号
    private String name;     // 真实姓名
    private String phone;    // 手机号
    private String password; // 密码
}