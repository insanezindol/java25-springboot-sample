package com.example.sample.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @InjectMocks
    KafkaProducerService kafkaProducerService;

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    private final String TEST_TOPIC = "user-events";

    @BeforeEach
    void setUp() {
        // @Value("${kafka.topic.event}") 필드에 테스트용 토픽명 주입
        ReflectionTestUtils.setField(kafkaProducerService, "eventTopic", TEST_TOPIC);
    }

    @Test
    @DisplayName("카프카 퍼블리쉬 성공 테스트")
    void sendMessage_Success() {
        // given
        final Long userId = 100L;
        final String action = "working";

        // 가상 스레드 환경에서도 비동기 콜백이 실행되도록 완료된 Future를 생성
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mock(SendResult.class));

        // 파라미터 개수에 맞춰 send(topic, key, data) 매칭
        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString()))
                .thenReturn(future);

        // when
        kafkaProducerService.sendMessage(userId, action);

        // then
        // kafkaTemplate.send 가 정확한 파라미터로 호출되었는지 검증
        verify(kafkaTemplate, times(1)).send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString());
    }

}