package com.easychat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.chat.mapper.ChatMessageMapper;
import com.easychat.chat.mapper.ChatSessionMapper;
import com.easychat.chat.netty.ChannelContextUtils;
import com.easychat.common.config.FileConfig;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.dto.SysSettingDTO;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.enums.MessageStatusEnum;
import com.easychat.common.entity.enums.ResponseCodeEnum;
import com.easychat.common.entity.po.ChatMessage;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.po.ChatSession;
import com.easychat.common.utils.*;
import com.easychat.chat.service.ChatMessageService;
import com.easychat.common.exception.BusinessException;
import jodd.util.ArraysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
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

    @Autowired
    private FileConfig fileConfig;


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

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (null == message) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!message.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验文件大小
        SysSettingDTO sysSettingDTO = redisComponet.getSysSetting();
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());
        if (!StringTools.isEmpty(fileSuffix) && ArraysUtil.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > Constants.FILE_SIZE_MB * sysSettingDTO.getMaxImageSize()) {
            return;
        } else if (!StringTools.isEmpty(fileSuffix) && ArraysUtil.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > Constants.FILE_SIZE_MB * sysSettingDTO.getMaxVideoSize()) {
            return;
        } else if (!StringTools.isEmpty(fileSuffix) &&
                !ArraysUtil.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                !ArraysUtil.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase()) &&
                file.getSize() > Constants.FILE_SIZE_MB * sysSettingDTO.getMaxFileSize()) {
            return;
        }
        // 构造文件名称与路径
        String fileName = file.getOriginalFilename();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;
        // 获取当前项目根目录
        String projectPath = System.getProperty("user.dir");
        // 构建完整的文件夹路径
        String messagePath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getMessageFolder();
        String month = DateUtil.format(new Date(), "yyyyMM");
        File folder = new File(messagePath + File.separator + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + File.separator + fileRealName);
        try {
            file.transferTo(uploadFile);
            if (cover != null) {
                cover.transferTo(new File(uploadFile.getPath() + Constants.COVER_IMAGE_SUFFIX));
            }
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BusinessException("文件上传失败");
        }
//        ChatMessage updateInfo = new ChatMessage();
//        updateInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
//        ChatMessageQuery messageQuery = new ChatMessageQuery();
//        messageQuery.setMessageId(messageId);
//        chatMessageMapper.updateByParam(updateInfo, messageQuery);

        chatMessageMapper.update(new ChatMessage(), new LambdaUpdateWrapper<ChatMessage>()
                .set(ChatMessage::getStatus, MessageStatusEnum.SENDED.getStatus())
                .eq(ChatMessage::getMessageId, messageId));



        MessageSendDTO messageSend = new MessageSendDTO();
        messageSend.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSend.setMessageId(message.getMessageId());
        messageSend.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSend.setContactId(message.getContactId());
        channelContextUtils.sendMessage(messageSend);
    }

    @Override
    public File downloadFile(Long messageId, Boolean cover) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        String contactId = message.getContactId();
        ContactTypeEnum contactTypeEnum = ContactTypeEnum.getByPrefix(contactId);
        if (ContactTypeEnum.USER.equals(contactTypeEnum) && !UserContext.getUser().equals(message.getContactId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
//        if (ContactTypeEnum.GROUP.equals(contactTypeEnum)) {
//            UserContactQuery userContactQuery = new UserContactQuery();
//            userContactQuery.setUserId(userInfoDto.getUserId());
//            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
//            userContactQuery.setContactId(contactId);
//            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
//            Integer contactCount = userContactMapper.selectCount(userContactQuery);
//            if (contactCount == 0) {
//                throw new BusinessException(ResponseCodeEnum.CODE_600);
//            }
//        }
        String projectPath = System.getProperty("user.dir");
        String month = DateUtil.format(new Date(message.getSendTime()), "yyyyMM");
        String messagePath = projectPath + File.separator + fileConfig.getFilePath() + File.separator + fileConfig.getMessageFolder();
        File folder = new File(messagePath + File.separator + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String fileName = message.getFileName();
        String fileExtName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtName;

        if (cover != null && cover) {
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + File.separator + fileRealName);
        if (!file.exists()) {
            log.error("文件不存在");
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        return file;
    }
}