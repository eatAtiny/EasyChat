package com.easychat.chat.kafka;

import com.easychat.chat.netty.ChannelContextUtils;
import com.easychat.common.entity.dto.MessageSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class SendMessageListener {

    private static final String SEND_MESSAGE_TOPIC = "send-message-topic";

    @Resource
    private ChannelContextUtils channelContextUtils;


    /**
     * 处理发送消息事件
     * @param record Kafka消息记录
     * @param acknowledgment 确认对象
     */
    @KafkaListener(topics = SEND_MESSAGE_TOPIC, groupId = "message_to_channel_topic")
    public void handleSendMessage(ConsumerRecord<String, MessageSendDTO> record, Acknowledgment acknowledgment) {
        try {
            MessageSendDTO event = record.value();
            if (event == null) {
                acknowledgment.acknowledge();
                return;
            }

            log.info("收到消息, event: {}", event);
            // 处理发送消息事件,将消息发往channel
            channelContextUtils.sendMessage(event);
        } catch (Exception e) {
            // 记录异常日志
            log.error("处理发送消息事件时出错: {}", e.getMessage(), e);
        } finally {
            // 确认消息已处理
            acknowledgment.acknowledge();
        }
    }
}