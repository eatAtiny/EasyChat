package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;


/**
 * 会话用户
 */
@Data
public class ChatSessionUser implements Serializable {


    /**
     * 用户ID
     */
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
