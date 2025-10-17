package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.protobuf.DescriptorProtos;
import lombok.Data;

import java.io.Serializable;


/**
 * 会话用户
 */
@Data
@TableName("chat_session_user")
public class ChatSessionUser implements Serializable {


    /**
     * 用户ID
     */
    @TableId(type = IdType.INPUT)
    private String userId;

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    @TableField(exist = false)
    private String lastMessage;
    @TableField(exist = false)
    private Long lastReceiveTime;
    @TableField(exist = false)
    private Integer contactType;
    @TableField(exist = false)
    private Integer memberCount;


    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，联系人ID:" + (contactId == null ? "空" : contactId) + "，会话ID:" + (sessionId == null ? "空" : sessionId) +
                "，联系人名称:" + (contactName == null ? "空" : contactName);
    }
}