package com.easychat.contact.constant;

public class Constants {

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
    public static final String GROUP_INFO_SERVICE_ERROR = "获取群组信息失败，请稍后再试";
    public static final String USER_INFO_SERVICE_ERROR = "获取用户信息失败，请稍后再试";
    public static final String APPLY_INFO_EXIST = "申请已存在";
    public static final String GROUP_INFO_EXIST = "群聊已存在";



    // 群聊状态
    public static final Integer GROUP_STATUS_DISSOLUTION = 0;  // 解散

    // 联系人状态
    public static final Integer CONTACT_USER_STATUS_NOT_FRIEND = 0;  // 非好友
    public static final Integer CONTACT_USER_STATUS_FRIEND = 1;  // 好友
    public static final Integer CONTACT_USER_STATUS_DELETE_FRIEND = 2; // 已删除好友
    public static final Integer CONTACT_USER_STATUS_DELETED_BY_FRIEND = 3; // 被好友删除
    public static final Integer CONTACT_USER_STATUS_BLOCK_FRIEND = 4; // 已拉黑好友
    public static final Integer CONTACT_USER_STATUS_BLOCKED_BY_FRIEND = 5; // 被好友拉黑

    // 联系人申请权限
    public static final Integer CONTACT_APPLY_PERMISSION_DIRECT = 0; // 直接加入
    public static final Integer CONTACT_APPLY_PERMISSION_AUDIT = 1; // 需要审核

    // 好友/群聊申请状态
    public static final Integer CONTACT_APPLY_STATUS_PENDING = 0; // 待处理
    public static final Integer CONTACT_APPLY_STATUS_ACCEPTED = 1; // 已同意
    public static final Integer CONTACT_APPLY_STATUS_REJECTED = 2; // 已拒绝
    public static final Integer CONTACT_APPLY_STATUS_BLOCKED = 3; // 已拉黑
    // 头像上传路径
    public static final String AVATAR_PATH = "D:/avatar/";
    // 头像文件名后缀
    public static final String AVATAR_SUFFIX = ".jpg";
    // 头像缩略图上传路径
    public static final String AVATAR_COVER_PATH = "D:/avatar/cover/";
    // 头像缩略图文件名后缀
    public static final String AVATAR_COVER_SUFFIX = "_cover.jpg";
}