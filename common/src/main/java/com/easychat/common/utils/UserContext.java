package com.easychat.common.utils;

public class UserContext {
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> nickNameHolder = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到ThreadLocal
     * @param userId 用户id
     */
    public static void setUser(String userId) {
        userIdHolder.set(userId);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户id
     */
    public static String getUser() {
        return userIdHolder.get();
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        userIdHolder.remove();
    }

    /**
     * 保存当前登录用户昵称到ThreadLocal
     * @param nickName 昵称
     */
    public static void setNickName(String nickName) {
        nickNameHolder.set(nickName);
    }

    /**
     * 获取当前登录用户昵称
     * @return 昵称
     */
    public static String getNickName() {
        return nickNameHolder.get();
    }

    /**
     * 移除当前登录用户昵称
     */
    public static void removeNickName() {
        nickNameHolder.remove();
    }
}
