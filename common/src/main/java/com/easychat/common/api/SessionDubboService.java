package com.easychat.common.api;

import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.entity.po.ChatSessionUser;

public interface SessionDubboService {
    /**
     * 添加会话
     */
    boolean addSession(ChatSession chatSession);
     /**
     * 添加会话用户
     */
    boolean addSessionUser(ChatSessionUser chatSessionUser);

    /**
     * 添加聊天信息
     */
     boolean addChatMessage(ChatMessage chatMessage);
}
