package com.example.sample.service;

import com.example.sample.dto.UserEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.event}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            log.info("Consumed message: {}", message);
            UserEventMessage userEventMessage = objectMapper.readValue(message, UserEventMessage.class);
            log.info("userEventMessage : {}", userEventMessage);
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

}
