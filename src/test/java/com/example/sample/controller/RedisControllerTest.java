package com.example.sample.controller;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RedisControllerTest {

    @Mock
    private RedisService redisService; // 가짜 객체 생성

    @InjectMocks
    private RedisController redisController; // Mock 객체를 주입받는 Controller

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(redisController).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("사용자 상세 조회 성공")
    void getUser() throws Exception {
        // given
        String userId = "user123";
        Map<String, Object> userData = Map.of(
                "id", userId,
                "username", "홍길동",
                "email", "hong@example.com",
                "age", 30
        );

        given(redisService.getUserById(userId)).willReturn(userData);

        // when & then
        mockMvc.perform(get("/api/v1/redis/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(redisService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("사용자 상세 조회 실패")
    void getUserNotFound() throws Exception {
        // given
        String userId = "user123";

        given(redisService.getUserById(userId)).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/redis/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(redisService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser() throws Exception {
        // given
        RedisUserDto requestDto = RedisUserDto.builder()
                .id("user123")
                .username("홍길동")
                .email("hong@example.com")
                .age(30)
                .build();

        given(redisService.createUser(any(RedisUserDto.class))).willReturn(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/redis/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user123"))
                .andExpect(jsonPath("$.username").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(redisService, times(1)).createUser(any(RedisUserDto.class));
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser() throws Exception {
        // given
        String userId = "user123";

        // when & then
        mockMvc.perform(delete("/api/v1/redis/users/{userId}", userId)
                        .contentType("text/plain;charset=UTF-8")
                        .accept("text/plain;charset=UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("사용자가 삭제되었습니다: " + userId));

        verify(redisService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("사용자 최근 목록 조회 성공")
    void getRecentItems() throws Exception {
        // given
        String userId = "user123";
        List<Object> recentItems = Arrays.asList("item1", "item2", "item3");

        given(redisService.getRecentItems(userId)).willReturn(recentItems);

        // when & then
        mockMvc.perform(get("/api/v1/redis/users/{userId}/recent", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("item1"))
                .andExpect(jsonPath("$[1]").value("item2"))
                .andExpect(jsonPath("$[2]").value("item3"));

        verify(redisService, times(1)).getRecentItems(userId);
    }

    @Test
    @DisplayName("사용자 최근 목록 추가 성공")
    void addRecentItem() throws Exception {
        // given
        String userId = "user123";
        String itemId = "item456";

        // when & then
        mockMvc.perform(post("/api/v1/redis/users/{userId}/recent", userId)
                        .param("itemId", itemId)
                        .contentType("text/plain;charset=UTF-8")
                        .accept("text/plain;charset=UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("최근 목록에 추가되었습니다"));

        verify(redisService, times(1)).addToRecentItems(userId, itemId);
    }

    @Test
    @DisplayName("사용자 장바구니 조회 성공")
    void getCart() throws Exception {
        // given
        String userId = "user123";
        Set<Object> cartItems = new HashSet<>(Arrays.asList("product1", "product2", "product3"));

        given(redisService.getCart(userId)).willReturn(cartItems);

        // when & then
        mockMvc.perform(get("/api/v1/redis/users/{userId}/cart", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(redisService, times(1)).getCart(userId);
    }

    @Test
    @DisplayName("사용자 장바구니 추가 성공")
    void addToCart() throws Exception {
        // given
        String userId = "user123";
        List<String> itemIds = Arrays.asList("product1", "product2");

        // when & then
        mockMvc.perform(post("/api/v1/redis/users/{userId}/cart", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("text/plain;charset=UTF-8")
                        .content(objectMapper.writeValueAsString(itemIds)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string("장바구니에 추가되었습니다"));

        verify(redisService, times(1)).addToCart(eq(userId), eq(itemIds.toArray(new String[0])));
    }

    @Test
    @DisplayName("상품 조회수 조회 성공")
    void getViewCount() throws Exception {
        // given
        String itemId = "product123";
        Long viewCount = 150L;

        given(redisService.getViewCount(itemId)).willReturn(viewCount);

        // when & then
        mockMvc.perform(get("/api/v1/redis/items/{itemId}/views", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));

        verify(redisService, times(1)).getViewCount(itemId);
    }

    @Test
    @DisplayName("상품 조회수 증가 성공")
    void incrementViewCount() throws Exception {
        // given
        String itemId = "product123";
        Long incrementedCount = 151L;

        given(redisService.incrementViewCount(itemId)).willReturn(incrementedCount);

        // when & then
        mockMvc.perform(post("/api/v1/redis/items/{itemId}/view", itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("151"));

        verify(redisService, times(1)).incrementViewCount(itemId);
    }

    @Test
    @DisplayName("Redis 전체 키 목록 조회 성공")
    void searchKeys() throws Exception {
        // given
        String pattern = "user:*";
        Set<String> keys = new HashSet<>(Arrays.asList("user:123", "user:456", "user:789"));

        given(redisService.searchKeys(pattern)).willReturn(keys);

        // when & then
        mockMvc.perform(get("/api/v1/redis/keys")
                        .param("pattern", pattern)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").isString());

        verify(redisService, times(1)).searchKeys(pattern);
    }

    @Test
    @DisplayName("Redis 전체 키 목록 조회 성공 - 기본 패턴")
    void getRedisInfo() throws Exception {
        // given
        Set<String> keys = new HashSet<>(Arrays.asList("user:123", "item:456", "cart:789"));

        given(redisService.searchKeys("*")).willReturn(keys);

        // when & then
        mockMvc.perform(get("/api/v1/redis/keys")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(redisService, times(1)).searchKeys("*");
    }

}