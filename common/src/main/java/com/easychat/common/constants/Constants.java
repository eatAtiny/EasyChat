package com.easychat.common.constants;

import com.easychat.common.entity.enums.ContactTypeEnum;

public class Constants {
    public static final String ROBOT_ID = "Urobot";
    public static final String ROBOT_NAME = "Robot";

    public static final String ZERO_STR = "0";

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_11 = 11;
    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_30 = 30;

    public static final String SESSION_KEY = "session_key";

    public static final String FILE_FOLDER_FILE = "/file/";

    public static final String FILE_FOLDER_TEMP = "/temp/";

    public static final String FILE_FOLDER_TEMP_2 = "temp";

    public static final String FILE_FOLDER_IMAGE = "images/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String CHECK_CODE_KEY = "check_code_key";

    public static final String IMAGE_SUFFIX = ".png";

    public static final String COVER_IMAGE_SUFFIX = "_cover.png";

    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};

    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    public static final Long FILE_SIZE_MB = 1024 * 1024L;

    /**
     * redis key 相关
     */

    /**
     * 过期时间 1分钟
     */
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;


    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    /**
     * 过期时间 1天
     */
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;


    public static final Integer REDIS_KEY_TOKEN_EXPIRES = REDIS_KEY_EXPIRES_DAY * 2;


    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";
    public static final String REDIS_KEY_WS_TOKEN = "easychat:ws:token:";

    public static final String REDIS_KEY_WS_TOKEN_USERID = "easychat:ws:token:userid";

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat";

    public static final String REDIS_KEY_WS_ON_LINE_USER = "easychat:ws:online:";

    //用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT = "easychat:ws:user:contact:";

    //用户参与的会话列表
    public static final String REDIS_KEY_USER_SESSION = "easychat:ws:user:session:";

    public static final Long MILLISECOND_3DAYS_AGO = 3 * 24 * 60 * 60 * 1000L;

    public static final String ROBOT_UID = ContactTypeEnum.USER.getPrefix() + "robot";

    //系统设置
    public static final String REDIS_KEY_SYS_SETTING = "easychat:syssetting:";

    public static final String APP_UPDATE_FOLDER = "/app/";

    public static final String APP_NAME = "EasyChatSetup.";
    public static final String APP_EXE_SUFFIX = ".exe";

    //正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    //申请信息模板
    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    //自己退群
    public static final String out_group_TEMPLATE_self = "%s退出了群聊";

    //被管理员踢群
    public static final String out_group_TEMPLATE = "%s被管理员移出了群聊";

    // 传递给下服务的信息
    public static final String USER_ID = "user_id"; // 用户id
    public static final String USER_NICK_NAME = "nick_name";  // 昵称

    // checkCode
    public static final Integer REDIS_KEY_CHECK_CODE_KEY_TIME = 60 * 10;
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


    // 群聊默认信息
    public static final Integer GROUP_STATUS_NORMAL = 1;

    // 联系人ID正则表达式
    public static final String CONTACT_ID_REGEX = "^[GU][0-9a-fA-F]{11}$";

    // 错误信息
    public static final String GROUP_NOT_EXIST = "群聊不存在";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String GROUP_EXIST = "群聊已存在";
    public static final String GROUP_NUM_EXCEED = "群聊数量已达上限";
    public static final String GROUP_OWNER_ERROR = "群主信息错误";
    public static final String USER_NOT_IN_GROUP = "用户不在群聊中";
    public static final String GROUP_OP_TYPE_UNKNOWN = "群聊操作类型未知";
    public static final String GROUP_OWNER_CANT_LEAVE = "群主不能退出群聊";
    public static final String CONTACT_APPLY_INFO_ERROR = "申请信息错误";
    public static final String CONTACT_USER_STATUS_BLOCKED = "对方已将你拉黑";
    public static final String GROUP_FULL = "群聊已满";
    public static final String APPLY_NOT_EXIST = "申请不存在";
    public static final String ERROR_OPERATION = "违法操作";

    // 群聊状态
    public static final Integer GROUP_STATUS_DISSOLUTION = 0;  // 解散
}
