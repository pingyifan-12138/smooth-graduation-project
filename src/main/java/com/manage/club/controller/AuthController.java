package com.manage.club.controller;


import com.manage.club.dto.LoginDTO;
import com.manage.club.dto.RegisterDTO;
import com.manage.club.service.UserService;
import com.manage.club.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    // 用户登录：POST /auth/login
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        try {
            Map<String, Object> data = userService.login(dto);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 用户注册：POST /auth/register
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody RegisterDTO dto) {
        boolean success = userService.register(dto);
        return success ? Result.success(true) : Result.error("注册失败，用户名已存在");
    }
}