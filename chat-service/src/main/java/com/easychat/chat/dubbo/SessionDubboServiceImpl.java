package com.easychat.chat.dubbo;

import com.easychat.chat.service.ChatMessageService;
import com.easychat.chat.service.ChatSessionService;
import com.easychat.chat.service.ChatSessionUserService;
import com.easychat.common.api.SessionDubboService;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.entity.po.ChatSessionUser;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@DubboService
@Service
public class SessionDubboServiceImpl implements SessionDubboService {

    @Resource
    private ChatSessionService chatSessionService;
    @Resource
    private ChatSessionUserService chatSessionUserService;
    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 添加会话
     */
    @Override
    public boolean addSession(ChatSession chatSession) {
        chatSessionService.saveOrUpdate(chatSession);
        return true;
    }
     /**
      * 添加会话用户
      */
    @Override
    public boolean addSessionUser(ChatSessionUser chatSessionUser) {
        chatSessionUserService.saveOrUpdate(chatSessionUser);
        return true;
    }

    /**
     * 添加聊天信息
     */
     @Override
    public boolean addChatMessage(ChatMessage chatMessage) {
        chatMessageService.save(chatMessage);
        return true;
    }
}
