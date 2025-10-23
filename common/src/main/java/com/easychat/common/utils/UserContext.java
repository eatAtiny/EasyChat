package com.easychat.common.utils;

import org.apache.dubbo.rpc.RpcContext;

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

    /**
     * 从Dubbo上下文获取用户ID（用于Dubbo服务提供者端）
     * @return 用户ID，如果不存在返回null
     */
    public static String getUserFromDubbo() {
        // 优先从ThreadLocal获取
        String userId = getUser();
        if (userId != null) {
            return userId;
        }
        
        // 尝试从Dubbo上下文获取（Dubbo 3.x版本）
        try {
            // 方式1：从Invocation获取
            String dubboUserId = RpcContext.getServiceContext().getAttachment("userId");
            if (dubboUserId != null) {
                return dubboUserId;
            }
            
            // 方式2：从ServerAttachment获取
            dubboUserId = RpcContext.getServerAttachment().getAttachment("userId");
            if (dubboUserId != null) {
                return dubboUserId;
            }
        } catch (Exception e) {
            // 忽略异常，继续尝试其他方式
        }
        
        return null;
    }

    /**
     * 从Dubbo上下文获取用户昵称（用于Dubbo服务提供者端）
     * @return 用户昵称，如果不存在返回null
     */
    public static String getNickNameFromDubbo() {
        // 优先从ThreadLocal获取
        String nickName = getNickName();
        if (nickName != null) {
            return nickName;
        }
        
        // 尝试从Dubbo上下文获取（Dubbo 3.x版本）
        try {
            // 方式1：从Invocation获取
            String dubboNickName = RpcContext.getServiceContext().getAttachment("nickName");
            if (dubboNickName != null) {
                return dubboNickName;
            }
            
            // 方式2：从ServerAttachment获取
            dubboNickName = RpcContext.getServerAttachment().getAttachment("nickName");
            if (dubboNickName != null) {
                return dubboNickName;
            }
        } catch (Exception e) {
            // 忽略异常，继续尝试其他方式
        }
        
        return null;
    }

    /**
     * 安全获取用户ID，优先从ThreadLocal获取，其次从Dubbo上下文获取
     * @return 用户ID，如果都不存在返回null
     */
    public static String getUserIdSafely() {
        String userId = getUser();
        if (userId == null) {
            userId = getUserFromDubbo();
        }
        return userId;
    }

    /**
     * 安全获取用户昵称，优先从ThreadLocal获取，其次从Dubbo上下文获取
     * @return 用户昵称，如果都不存在返回null
     */
    public static String getNickNameSafely() {
        String nickName = getNickName();
        if (nickName == null) {
            nickName = getNickNameFromDubbo();
        }
        return nickName;
    }
}