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
     * 更新会话用户
     * @param chatSessionUser 会话用户
     * @return 是否更新成功
     */
    boolean updateSessionUser(ChatSessionUser chatSessionUser);

     /**
     * 更新会话
     * @param chatSession 会话
     */
    void updateSession(ChatSession chatSession);

    /**
     * 添加聊天信息
     * @param chatMessage 聊天消息对象
     * @return 包含自增ID的聊天消息对象
     */
     ChatMessage addChatMessage(ChatMessage chatMessage);

    /**
     * 将用户添加进channelGroup
     */
    void addGroupToChannel(String userId, String groupId);

    /**
     * 关闭用户通道
     */
    void closeUserChannel(String userId);
}