package com.easychat.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.chat.mapper.ChatMessageMapper;
import com.easychat.chat.mapper.ChatSessionMapper;
import com.easychat.chat.netty.ChannelContextUtils;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.enums.MessageStatusEnum;
import com.easychat.common.entity.enums.ResponseCodeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.utils.CopyTools;
import com.easychat.common.utils.RedisComponet;
import com.easychat.chat.service.ChatMessageService;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.StringTools;
import jodd.util.ArraysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private RedisComponet redisComponet;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChannelContextUtils channelContextUtils;


    @Override
    public MessageSendDTO saveMessage(ChatMessage chatMessage) {
        //不是机器人回复，判断好友状态
        if (!Constants.ROBOT_UID.equals(chatMessage.getSendUserId())) {
            List<String> contactList = redisComponet.getUserContactList(chatMessage.getSendUserId());
            if (!contactList.contains(chatMessage.getContactId())) {
                ContactTypeEnum contactTypeEnum = ContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (ContactTypeEnum.USER == contactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }
        String sessionId = null;
        String sendUserId = chatMessage.getSendUserId();
        String contactId = chatMessage.getContactId();
        Long curTime = System.currentTimeMillis();
        ContactTypeEnum contactTypeEnum = ContactTypeEnum.getByPrefix(contactId);
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        String lastMessage = chatMessage.getMessageContent();
        String messageContent = StringTools.resetMessageContent(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        if (ArraysUtil.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.GROUP_CREATE.getType(),
                MessageTypeEnum.ADD_FRIEND.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()
        }, messageTypeEnum.getType())) {
            if (ContactTypeEnum.USER == contactTypeEnum) {
                sessionId = StringTools.getChatSessionId4User(new String[]{sendUserId, contactId});
                if (Objects.equals(sendUserId, Constants.ROBOT_ID))
                    sessionId = Constants.ROBOT_ID + contactId;
                if (contactId.equals(Constants.ROBOT_ID))
                    sessionId = contactId + sendUserId;
            } else {
                sessionId = StringTools.getChatSessionId4Group(contactId);
            }
            //更新会话消息
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(messageContent);
            if (ContactTypeEnum.GROUP == contactTypeEnum && !MessageTypeEnum.GROUP_CREATE.getType().equals(messageTypeEnum.getType())) {
                chatSession.setLastMessage(chatMessage.getSendUserNickName() + "：" + messageContent);
            }
            lastMessage = chatSession.getLastMessage();
            //如果是媒体文件
            chatSession.setLastReceiveTime(curTime);
            chatSessionMapper.updateById(chatSession);
            //记录消息消息表
            chatMessage.setSessionId(sessionId);
            chatMessage.setSendUserId(sendUserId);
            chatMessage.setSendUserNickName(chatMessage.getSendUserNickName());
            chatMessage.setSendTime(curTime);
            chatMessage.setContactType(contactTypeEnum.getStatus());
            chatMessage.setStatus(status);
            chatMessageMapper.insert(chatMessage);
        }
        MessageSendDTO messageSend = CopyTools.copy(chatMessage, MessageSendDTO.class);
        messageSend.setLastMessage(lastMessage);
        if (Constants.ROBOT_UID.equals(contactId)) {
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setSendUserId(Constants.ROBOT_ID);
            robotChatMessage.setSendUserNickName(Constants.ROBOT_NAME);
            robotChatMessage.setContactId(sendUserId);
            //这里可以对接Ai 根据输入的信息做出回答
            robotChatMessage.setMessageContent("我只是一个机器人无法识别你的消息");
            robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(robotChatMessage);
        } else {
            channelContextUtils.sendMessage(messageSend);
        }
        return messageSend;
    }
}
