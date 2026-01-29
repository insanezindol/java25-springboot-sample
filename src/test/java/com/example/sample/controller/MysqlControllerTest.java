package com.example.sample.controller;

import com.example.sample.domain.User;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.service.MysqlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MysqlControllerTest {

    @Mock
    private MysqlService mysqlService; // 가짜 객체 생성

    @InjectMocks
    private MysqlController mysqlController; // Mock 객체를 주입받는 Controller

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(mysqlController).build();
        this.objectMapper = new ObjectMapper();
        this.fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void findAllUser() throws Exception {
        // given
        List<User> users = List.of(
                new User(1L, "홍길동", "hong@example.com"),
                new User(2L, "김철수", "kim@example.com")
        );

        given(mysqlService.findAll()).willReturn(users);

        // when & then
        mockMvc.perform(get("/api/v1/mysql")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("홍길동"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("김철수"));

        verify(mysqlService, times(1)).findAll();
    }

    @Test
    @DisplayName("사용자 상세 조회 성공")
    void findByUserId() throws Exception {
        // given
        Long userId = 1L;
        User user = new User(userId, "홍길동", "hong@example.com");

        given(mysqlService.findOne(userId)).willReturn(user);

        // when & then
        mockMvc.perform(get("/api/v1/mysql/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"));

        verify(mysqlService, times(1)).findOne(userId);
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser() throws Exception {
        // given
        UserRequestDto requestDto = fixtureMonkey.giveMeBuilder(UserRequestDto.class)
                .set("name", "테스트사용자")
                .set("email", "test@example.com")
                .sample();

        Long savedId = 100L;

        given(mysqlService.save(any(UserRequestDto.class))).willReturn(savedId);

        // when & then
        mockMvc.perform(post("/api/v1/mysql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(mysqlService, times(1)).save(any(UserRequestDto.class));
    }

    @Test
    @DisplayName("사용자 수정 성공")
    void updateUser() throws Exception {
        // given
        Long userId = 1L;
        UserRequestDto requestDto = new UserRequestDto("홍길동 수정", "hong.updated@example.com");
        User updatedUser = new User(userId, "홍길동 수정", "hong.updated@example.com");

        given(mysqlService.update(eq(userId), any(UserRequestDto.class))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/api/v1/mysql/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("홍길동 수정"))
                .andExpect(jsonPath("$.email").value("hong.updated@example.com"));

        verify(mysqlService, times(1)).update(eq(userId), any(UserRequestDto.class));
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser() throws Exception {
        // given
        Long userId = 1L;

        doNothing().when(mysqlService).delete(userId);

        // when & then
        mockMvc.perform(delete("/api/v1/mysql/{id}", userId))
                .andExpect(status().isNoContent());

        verify(mysqlService, times(1)).delete(userId);
    }

}