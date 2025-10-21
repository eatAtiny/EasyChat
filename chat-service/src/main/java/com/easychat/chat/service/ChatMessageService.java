package com.easychat.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.po.ChatMessage;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ChatMessageService extends IService<ChatMessage> {
        /**
         * 保存消息
         *
         * @param chatMessage 消息
         * @return 消息发送DTO
         */
        MessageSendDTO saveMessage(ChatMessage chatMessage);

        /**
         * 保存消息文件
         *
         * @param userId    用户ID
         * @param messageId 消息ID
         * @param file      文件
         * @param cover     封面
         */
        void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

        /**
         * 下载文件
         *
         * @param messageId 消息ID
         * @param cover     是否显示封面
         * @return 文件
         */
        File downloadFile(Long messageId, Boolean cover);
}
