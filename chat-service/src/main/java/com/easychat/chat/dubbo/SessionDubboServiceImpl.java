package com.easychat.chat.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easychat.chat.netty.ChannelContextUtils;
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
    @Resource
    private ChannelContextUtils channelContextUtils;

    /**
     * 添加会话
     */
    @Override
    public boolean addSession(ChatSession chatSession) {
        // 按sessionId条件保存
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", chatSession.getSessionId());
        chatSessionService.saveOrUpdate(chatSession, queryWrapper);
        return true;
    }
     /**
      * 添加会话用户
      */
    @Override
    public boolean addSessionUser(ChatSessionUser chatSessionUser) {
        // 按userId和contactId联合主键条件保存
        QueryWrapper<ChatSessionUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", chatSessionUser.getUserId())
                   .eq("contact_id", chatSessionUser.getContactId());
        chatSessionUserService.saveOrUpdate(chatSessionUser, queryWrapper);
        return true;
    }

     /**
      * 更新会话用户
      * @param chatSessionUser 会话用户
      * @return 是否更新成功
      */
    @Override
    public boolean updateSessionUser(ChatSessionUser chatSessionUser) {
        // 按userId和contactId联合主键条件更新
        QueryWrapper<ChatSessionUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("contact_id", chatSessionUser.getContactId());
        chatSessionUserService.update(chatSessionUser, queryWrapper);
        return true;
    }

    /**
     * 添加聊天信息
     */
     @Override
    public ChatMessage addChatMessage(ChatMessage chatMessage) {
        chatMessageService.save(chatMessage);
        return chatMessage; // 返回包含自增ID的chatMessage对象
    }

    /**
     * 将新建群组添加进channelGroup
     */
    @Override
    public void addGroupToChannel(String userId, String groupId){
        channelContextUtils.addUserToGroup(userId, groupId);
    }
}