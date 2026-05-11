package com.manage.club.dto;


import lombok.Data;

@Data
public class LoginDTO {
    private String username; // 账号/学号
    private String password; // 密码
}