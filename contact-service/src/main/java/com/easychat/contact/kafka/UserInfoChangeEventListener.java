package com.easychat.contact.kafka;

import cn.hutool.core.date.DateTime;
import com.alibaba.nacos.shaded.org.checkerframework.checker.units.qual.C;
import com.easychat.common.api.SessionDubboService;
import com.easychat.common.constants.Constants;
import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.enums.ContactStatusEnum;
import com.easychat.common.entity.enums.ContactTypeEnum;
import com.easychat.common.entity.enums.MessageStatusEnum;
import com.easychat.common.entity.enums.MessageTypeEnum;
import com.easychat.common.entity.kafka.UserInfoMessage;
import com.easychat.common.entity.po.*;
import com.easychat.common.utils.StringTools;
import com.easychat.common.utils.UserContext;
import com.easychat.contact.mapper.ContactMapper;
import com.easychat.contact.mapper.ContactUserInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * 用户信息变更事件监听器
 * 用于接收来自user-service的用户信息变更事件
 * 处理用户创建和更新事件，实现user-service和contact-service之间的用户数据同步
 */
@Component
@Slf4j
public class UserInfoChangeEventListener {

    @Autowired
    private ContactUserInfoMapper contactUserInfoMapper;

    @Autowired
    private ContactMapper contactMapper;

    @DubboReference(check = false)
    private SessionDubboService sessionDubboService;

    @Autowired
    private KafkaMessageService kafkaMessageService;
    
    /**
     * 用户信息变更主题
     */
    private static final String USER_INFO_CHANGE_TOPIC = "user_info_change_topic";

    /**
     * 处理用户信息变更事件
     * @param record Kafka消息记录
     * @param acknowledgment 消息确认
     */
    @KafkaListener(topics = USER_INFO_CHANGE_TOPIC, groupId = "contact-service-group")
    public void handleUserInfoChange(ConsumerRecord<String, UserInfoMessage> record, Acknowledgment acknowledgment) {
        try {
            UserInfoMessage event = record.value();
            if (event == null) {
                log.warn("Received null user info event");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing user info event: userId={}, eventType={}", event.getUserId(), event.getEventType());
            
            // 处理不同类型的事件
            switch (event.getEventType()) {
                case CREATE:
                    handleUserCreate(event);
                    break;
                case UPDATE:
                    handleUserUpdate(event);
                    break;
                case DELETE:
                    handleUserDelete(event);
                    break;
                default:
                    // 未知事件类型，记录日志
                    log.warn("Received unknown event type: {}", event.getEventType());
                    break;
            }
        } catch (Exception e) {
            // 记录异常信息
            log.error("Error processing user info change event: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            // 手动确认消息消费
            acknowledgment.acknowledge();
        }
    }

    /**
     * 处理用户创建事件
     * 根据系统设计，contact-service不直接保存用户信息，而是通过UserClient从user-service获取
     * 因此用户创建事件主要用于记录日志和触发可能的初始化操作
     */
    private void handleUserCreate(UserInfoMessage event) {
        ContactUserInfo contactUserInfo = new ContactUserInfo();
        BeanUtils.copyProperties(event, contactUserInfo);
        contactUserInfoMapper.insert(contactUserInfo);

        // 用户创建时默认添加机器人好友
        Contact contact = new Contact();
        contact.setUserId(contactUserInfo.getUserId());
        contact.setContactId(Constants.ROBOT_ID);
        contact.setContactType(ContactTypeEnum.USER.getStatus());
        contact.setCreateTime(DateTime.now());
        contact.setStatus(ContactStatusEnum.FRIEND.getStatus());
        contact.setLastUpdateTime(DateTime.now());
        contactMapper.insert(contact);

        String sessionId = Constants.ROBOT_ID + UserContext.getUser();
        // 创建会话
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(System.currentTimeMillis());
        chatSession.setLastMessage("欢迎使用EasyChat");
        sessionDubboService.addSession(chatSession);

        // 4.4 创建用户会话
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setSessionId(sessionId);
        chatSessionUser.setUserId(contact.getUserId());
        chatSessionUser.setContactId(Constants.ROBOT_ID);
        chatSessionUser.setContactName(Constants.ROBOT_NAME);
        sessionDubboService.addSessionUser(chatSessionUser);

        // 4.5 增加聊天消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
        chatMessage.setMessageContent("欢迎使用EasyChat");
        chatMessage.setSendUserId(Constants.ROBOT_ID);
        chatMessage.setSendUserNickName(Constants.ROBOT_NAME);
        chatMessage.setSendTime(System.currentTimeMillis());
        chatMessage.setContactId(contact.getUserId());
        chatMessage.setContactType(contact.getContactType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        // 添加消息到数据库，接收返回的包含自增ID的chatMessage对象
        chatMessage = sessionDubboService.addChatMessage(chatMessage);
    }

    /**
     * 处理用户更新事件
     * 根据系统设计，contact-service不直接保存用户信息，而是通过UserClient从user-service获取
     * 因此用户更新事件主要用于记录日志和触发可能的缓存刷新操作
     */
    private void handleUserUpdate(UserInfoMessage event) {
        ContactUserInfo contactUserInfo = contactUserInfoMapper.selectById(event.getUserId());
        if (contactUserInfo != null) {
            BeanUtils.copyProperties(event, contactUserInfo);
            contactUserInfoMapper.updateById(contactUserInfo);
        }
    }

    /**
     * 处理用户删除事件
     * 根据系统设计，contact-service不直接保存用户信息，而是通过UserClient从user-service获取
     * 因此用户删除事件主要用于记录日志和触发可能的清理操作
     */
    private void handleUserDelete(UserInfoMessage event) {
        contactUserInfoMapper.deleteById(event.getUserId());
    }
}