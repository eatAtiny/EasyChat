package com.easychat.contact.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j

public class TestEvenListener {
    @KafkaListener(topics = "test_topic")
    public void testKafkaConsumer(String message) {
        log.info("testKafkaConsumer, message: {}", message);
    }
}
