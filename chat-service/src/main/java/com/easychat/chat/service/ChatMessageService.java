package com.easychat.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.po.ChatMessage;

public interface ChatMessageService extends IService<ChatMessage> {
        /**
         * 保存消息
         *
         * @param chatMessage 消息
         * @return 消息发送DTO
         */
        MessageSendDTO saveMessage(ChatMessage chatMessage);
}
