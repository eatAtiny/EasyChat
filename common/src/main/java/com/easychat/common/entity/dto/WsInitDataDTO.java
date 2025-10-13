package com.easychat.common.entity.dto;

import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSessionUser;
import lombok.Data;

import java.util.List;

@Data
public class WsInitDataDTO {
    /**
     * 会话列表
     */
    private List<ChatSessionUser> chatSessionList;
    /**
     * 聊天消息列表
     */
    private List<ChatMessage> chatMessageList;
    /**
     * 好友申请数量
     */
    private Integer applyCount;
}
