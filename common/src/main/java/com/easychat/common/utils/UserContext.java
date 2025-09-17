package com.easychat.common.utils;

public class UserContext {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到ThreadLocal
     * @param userId 用户id
     */
    public static void setUser(String userId) {
        tl.set(userId);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户id
     */
    public static String getUser() {
        return tl.get();
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        tl.remove();
    }

    /**
     * 保存当前登录用户昵称到ThreadLocal
     * @param nickName 昵称
     */
    public static void setNickName(String nickName) {
        tl.set(nickName);
    }

    /**
     * 获取当前登录用户昵称
     * @return 昵称
     */
    public static String getNickName() {
        return tl.get();
    }

    /**
     * 移除当前登录用户昵称
     */
    public static void removeNickName() {
        tl.remove();
    }
}
