package com.easychat.contact.kafka;

import com.easychat.common.entity.dto.MessageSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaMessageService {

    /**
     * 客户端消息主题
     */
    private static final String MESSAGE_TO_CHANNEL_TOPIC = "send-message-topic";


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessageToChannel(MessageSendDTO messageSendDTO){
        kafkaTemplate.send(MESSAGE_TO_CHANNEL_TOPIC, messageSendDTO).addCallback(
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
