package com.example.sample.controller;

import com.example.sample.domain.User;
import com.example.sample.service.KafkaProducerService;
import com.example.sample.service.MysqlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KafkaControllerTest {

    @Mock
    private KafkaProducerService kafkaProducerService; // 가짜 객체 생성

    @InjectMocks
    private KafkaController kafkaController; // Mock 객체를 주입받는 Controller

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(kafkaController).build();
    }

    @Test
    void publish() throws Exception {
        // given
        Long userId = 1L;
        String action = "TEST-ACTION";

        doNothing().when(kafkaProducerService).sendMessage(userId, action);

        // when & then

        mockMvc.perform(post("/api/v1/kafka/publish")
                .param("userId", String.valueOf(userId))
                .param("action", action)
                .contentType(MediaType.APPLICATION_JSON)
                .accept("text/plain;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().string("Message published"));

        verify(kafkaProducerService, times(1)).sendMessage(any(), any());
    }
}