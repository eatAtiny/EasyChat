package com.easychat.user.userservice.constants;

import org.redisson.api.RedissonClient;

public class Constants {
    // token
    public static final Integer LENGTH_20 = 20;

    // checkCode
    public static final String REDIS_KEY_CHECK_CODE = "user:checkCode:";
    public static final Integer REDIS_KEY_CHECK_CODE_KEY_TIME = 60 * 10;
    public static final String CHECK_CODE_KEY = "checkCodeKey";

    // 错误信息
    public static final String ERROR_MSG_CHECK_CODE = "图片验证码错误";
    public static final String ERROR_MSG_USER_EXIST = "用户已存在";
    public static final String ERROR_MSG_USER_NOT_EXIST = "用户不存在";
    public static final String ERROR_MSG_PASSWORD_ERROR = "密码错误";
    public static final String ERROR_MSG_USER_DISABLE = "用户已禁用";
    public static final String ERROR_MSG_USER_LOGIN = "用户已登录";

    // 用户默认信息
    public static final Integer USER_DEFAULT_JOIN_TYPE = 1; // 0:直接加入 1:同意后加好友
    public static final Integer USER_DEFAULT_SEX = 0; // 0:女 1:男
    public static final Integer USER_DEFAULT_STATUS = 1; // 0:禁用 1:正常
    public static final String USER_DEFAULT_PERSONAL_SIGNATURE = "这个人很懒，没有留下什么~";
    public static final String USER_DEFAULT_AREA_NAME = "未知";
    public static final String USER_DEFAULT_AREA_CODE = "000000";

    // 靓号
    public static final Integer USER_BEAUTY_STATUS = 1; // 0:未使用 1:已使用

    // 用户状态
    public static final Integer USER_STATUS_DISABLE = 0; // 禁用
    public static final Integer USER_STATUS_NORMAL = 1; // 正常
}
