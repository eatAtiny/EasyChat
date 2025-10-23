package com.easychat.admin.kafka;

import com.easychat.common.entity.dto.MessageSendDTO;
import com.easychat.common.entity.kafka.GroupInfoMessage;
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
     * 发送ws消息主题
     */
    private static final String SEND_MESSAGE_TOPIC = "send-message-topic";
    /**
     * 发送ws消息
     * @param messageSendDTO 消息发送DTO
     */
     public void sendWsMessage(MessageSendDTO messageSendDTO) {
        kafkaTemplate.send(SEND_MESSAGE_TOPIC, messageSendDTO).addCallback(
                success -> {
                    // 发送成功，确认消息
                    log.info("发送成功");
                },
                failure -> {
                    // 发送失败，记录日志或进行其他处理
                    log.error("发送ws消息失败: {}", failure.getMessage());
                }
        );
    }
}