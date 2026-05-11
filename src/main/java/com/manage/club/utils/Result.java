package com.manage.club.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;  // 状态码：200成功，500失败，401未登录
    private String msg;    // 提示信息
    private T data;        // 响应数据

    // 1. 无参构造（必须有，否则Lombok/序列化会出问题）
    public Result() {}

    // 2. 全参构造（解决new Result<>(code, msg, data)报错）
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功（无数据）
    public static <T> Result<T> success() {
        return success(null);  // 修正：去掉JDK9+的data:写法
    }

    // 失败
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 未登录
    public static <T> Result<T> unAuth() {
        return new Result<>(401, "未登录", null);
    }
}