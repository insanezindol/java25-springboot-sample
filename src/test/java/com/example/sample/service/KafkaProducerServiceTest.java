package com.example.sample.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @InjectMocks
    KafkaProducerService kafkaProducerService;

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @Mock // ObjectMapper도 Mock으로 선언해야 InjectMocks가 주입해줍니다.
    ObjectMapper objectMapper;

    private final String TEST_TOPIC = "user-events";

    @BeforeEach
    void setUp() {
        // @Value("${kafka.topic.event}") 필드에 테스트용 토픽명 주입
        ReflectionTestUtils.setField(kafkaProducerService, "eventTopic", TEST_TOPIC);
    }

    @Test
    @DisplayName("카프카 퍼블리쉬 테스트")
    void sendMessage_Success() throws JsonProcessingException {
        // given
        final Long userId = 100L;
        final String action = "working";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"userId\":100,\"action\":\"working\"}");

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


    @Test
    @DisplayName("JSON 직렬화 실패 시 RuntimeException 발생")
    void sendMessage_WhenJsonSerializationFails_ShouldThrowRuntimeException() throws JsonProcessingException {
        // given
        Long userId = 100L;
        String action = "working";

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON 변환 실패") {});

        // when & then
        assertThrows(RuntimeException.class, () -> {
            kafkaProducerService.sendMessage(userId, action);
        });

        // 로그 검증 (선택사항)
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("메시지 전송 성공 시 성공 로그 출력")
    void sendMessage_WhenSendSuccess_ShouldLogSuccess() throws JsonProcessingException {
        // given
        Long userId = 100L;
        String action = "working";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"success\":true}");

        // 성공적인 SendResult Mock 생성
        SendResult<String, Object> mockSendResult = mock(SendResult.class);
        RecordMetadata mockMetadata = mock(RecordMetadata.class);
        when(mockMetadata.offset()).thenReturn(12345L);
        when(mockSendResult.getRecordMetadata()).thenReturn(mockMetadata);

        CompletableFuture<SendResult<String, Object>> successfulFuture =
                CompletableFuture.completedFuture(mockSendResult);

        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString()))
                .thenReturn(successfulFuture);

        // when
        kafkaProducerService.sendMessage(userId, action);

        // then - 성공 로그가 출력되는지 검증
        verify(kafkaTemplate, times(1)).send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString());

        // 로그 검증을 위해 약간의 대기 (비동기 콜백 실행을 위해)
        try {
            Thread.sleep(100); // 콜백이 실행될 시간을 줌
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("메시지 전송 실패 시 에러 로그 출력")
    void sendMessage_WhenSendFails_ShouldLogError() throws JsonProcessingException {
        // given
        Long userId = 100L;
        String action = "working";

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"fail\":true}");

        // 실패한 Future 생성
        CompletableFuture<SendResult<String, Object>> failedFuture =
                new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("카프카 전송 실패"));

        when(kafkaTemplate.send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString()))
                .thenReturn(failedFuture);

        // when
        kafkaProducerService.sendMessage(userId, action);

        // then
        verify(kafkaTemplate, times(1)).send(eq(TEST_TOPIC), eq(String.valueOf(userId)), anyString());

        // 에러 로그 출력을 기다림
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}