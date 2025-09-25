package com.easychat.common.entity.kafka;

import lombok.Data;

import java.io.Serializable;
/**
 * 用户信息变更事件
 * 当user_info数据库发生增加或修改时，发送此事件给contact-service和其他需要同步用户信息的服务
 */
@Data
public class UserInfoMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件类型：CREATE(创建)、UPDATE(更新)、DELETE(删除)
     */
    private EventType eventType;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 昵称
     */
    private String nickName;

    /**
     * 个性签名
     */
    private String personalSignature;

    /**
     * 性别 0:女 1:男
     */
    private Integer sex;

    /**
     * 省份
     */
    private String areaName;

    /**
     * 城市
     */
    private String areaCode;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        CREATE, UPDATE,  DELETE
    }
}