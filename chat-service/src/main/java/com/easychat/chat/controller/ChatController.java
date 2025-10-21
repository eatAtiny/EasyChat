package com.easychat.chat.controller;

import com.easychat.chat.service.ChatMessageService;
import com.easychat.common.advice.BaseController;
import com.easychat.common.config.FileConfig;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.enums.ResponseCodeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.exception.BusinessException;
import com.easychat.common.utils.StringTools;
import com.easychat.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RequestMapping("/chat")
@RestController
@Slf4j
public class ChatController extends BaseController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private FileConfig fileConfig;


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


    @RequestMapping("uploadFile")
    public ResponseVO uploadFile(HttpServletRequest request,
                                 @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 @NotNull MultipartFile cover) {

        chatMessageService.saveMessageFile(UserContext.getUser(), messageId, file, cover);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("downloadFile")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,
                             @NotEmpty String fileId,
                             @NotNull Boolean showCover) throws Exception {
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = null;
            // 获取当前项目根目录
            String projectPath = System.getProperty("user.dir");
            if (!StringTools.isNumber(fileId)) {
                // 构建avatar文件夹路径
                String avatarPath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getAvatarFolder() + File.separator + fileId + Constants.IMAGE_SUFFIX;
                if (showCover) {
                    avatarPath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getAvatarFolder() + File.separator + "cover" + File.separator + fileId + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
            } else {
                file = chatMessageService.downloadFile(Long.parseLong(fileId), showCover);
            }
            response.setContentType("application/x-msdownload; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;");
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }
}