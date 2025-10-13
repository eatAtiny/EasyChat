package com.easychat.contact.kafka;

import com.easychat.common.entity.kafka.UserInfoMessage;
import com.easychat.common.entity.po.ContactUserInfo;
import com.easychat.contact.mapper.ContactUserInfoMapper;
import lombok.extern.slf4j.Slf4j;
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