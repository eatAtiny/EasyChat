package com.easychat.gateway.constant;

public class Constants {

    // 约定好的token
    public static final String REDIS_KEY_WS_TOKEN = "easychat:ws:token:";
    public static final String REDIS_KEY_WS_TOKEN_USERID = "easychat:ws:token:userid";
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat";
    public static final String REDIS_KEY_WS_ON_LINE_USER = "easychat:ws:online:";

    // 传递给下服务的信息
    public static final String USER_ID = "user_id"; // 用户id
    public static final String USER_NICK_NAME = "nick_name";  // 昵称


}
