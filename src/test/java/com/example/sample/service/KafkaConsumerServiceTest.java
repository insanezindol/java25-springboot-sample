package com.example.sample.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.example.sample.log.MemoryAppender;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    private static final String LOGGER_NAME = "com.example.sample.service";
    private static MemoryAppender memoryAppender;
    private KafkaConsumerService kafkaConsumerService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        kafkaConsumerService = new KafkaConsumerService(objectMapper);
        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logger.setLevel(Level.DEBUG);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        memoryAppender.start();
        logger.addAppender(memoryAppender);
    }

    @AfterEach
    public void cleanUp() {
        memoryAppender.reset();
        memoryAppender.stop();
    }

    @Test
    @DisplayName("정상적인 메시지 소비시 로그 출력")
    void consume() {
        // given
        final String testMessage = "{\"id\": 1, \"event\": \"TEST_EVENT\"}";

        // when
        kafkaConsumerService.consume(testMessage);

        // then
        Assertions.assertEquals(2, memoryAppender.getSize());
        Assertions.assertTrue(memoryAppender.contains("Consumed message", Level.INFO));
    }

    @Test
    @DisplayName("예외 발생시 에러 로그 출력")
    void consumeOnException() {
        // given
        final String testMessage = null;

        // when
        kafkaConsumerService.consume(testMessage);

        // then
        Assertions.assertEquals(2, memoryAppender.getSize());
        Assertions.assertTrue(memoryAppender.contains("Error processing message", Level.ERROR));
    }

}