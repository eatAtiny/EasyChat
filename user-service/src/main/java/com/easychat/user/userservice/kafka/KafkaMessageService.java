package com.easychat.user.userservice.kafka;

import com.easychat.common.entity.kafka.UserInfoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka消息发送服务
 * 负责向Kafka发送各类事件消息
 */
@Service
@Slf4j
public class KafkaMessageService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 用户信息变更主题
     */
    private static final String USER_INFO_CHANGE_TOPIC = "user_info_change_topic";

    /**
     * 发送用户信息变更事件
     * @param event 用户信息变更事件
     */
    public void sendUserInfoChangeEvent(UserInfoMessage event) {
        kafkaTemplate.send(USER_INFO_CHANGE_TOPIC, event.getUserId(), event).addCallback(
                success -> {
                    // 发送成功，确认消息
                    log.info("发送用户信息变更事件成功: userId={}", event.getUserId());
                },
                failure -> {
                    // 发送失败，记录日志或进行其他处理
                    log.error("发送用户信息变更事件失败: {}", failure.getMessage());
                }
        );
    }

    /**
     * 通用发送方法
     * @param topic 主题
     * @param key 消息键
     * @param value 消息值
     */
    public void send(String topic, String key, Object value) {
        kafkaTemplate.send(topic, key, value).addCallback(
                success -> {
                    // 发送成功，确认消息
                    log.info("发送成功");
                },
                failure -> {
                    // 发送失败，记录日志或进行其他处理
                    log.error("发送消息失败: {}", failure.getMessage());
                }
        );
    }
}