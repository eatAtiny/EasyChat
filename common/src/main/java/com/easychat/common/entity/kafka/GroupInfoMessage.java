package com.easychat.common.entity.kafka;

import lombok.Data;

import java.io.Serializable;

/**
 * 群组信息变更事件
 * 当groupinfo数据库发生增加或修改时，发送此事件给contactservice
 */
@Data
public class GroupInfoMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件类型：CREATE(创建)、UPDATE(更新)、DELETE(删除)、MEMBER_CHANGE(成员变更)、USER_LEAVE(用户退出)、DISSOLUTION(解散)
     */
    private EventType eventType;
    
    /**
     * 群组ID
     */
    private String groupId;
    
    /**
     * 群名称
     */
    private String groupName;
    
    /**
     * 群主ID
     */
    private String groupOwnerId;

    /**
     * 群公告
     */
    private String groupNotice;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        CREATE, UPDATE, DELETE, MEMBER_CHANGE, USER_LEAVE, DISSOLUTION
    }
}