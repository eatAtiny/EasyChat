package com.easychat.chat.controller;

import com.easychat.chat.service.ChatMessageService;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.dto.TokenUserInfoDTO;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.enums.ResponseCodeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.UserContext;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RequestMapping("/chat")
@RestController
public class ChatController extends BaseController {

    @Autowired
    private ChatMessageService chatMessageService;


    @PostMapping("/sendMessage")
    public ResponseVO sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageType);
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, messageType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSendUserId(UserContext.getUser());
        chatMessage.setSendUserNickName(UserContext.getNickName());
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);
        MessageSendDTO messageSendDTO = chatMessageService.saveMessage(chatMessage);
        return getSuccessResponseVO(messageSendDTO);
    }
}