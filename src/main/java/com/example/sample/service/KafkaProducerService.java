package com.example.sample.service;

import com.example.sample.dto.UserEventMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("${kafka.topic.event}")
    private String eventTopic;

    public void sendMessage(Long userId, String action) {
        UserEventMessage message = new UserEventMessage(userId, action, Instant.now());
        String jsonMessage = null;
        try {
            jsonMessage = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Produce message: {}", jsonMessage);

        kafkaTemplate.send(eventTopic, String.valueOf(userId), jsonMessage)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully: offset {}", result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message", ex);
                    }
                });
    }
}
