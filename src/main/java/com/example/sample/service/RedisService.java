package com.example.sample.service;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.repository.RedisUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisUserRepository redisUserRepository;

    // ========== User 관련 메서드 ==========

    public RedisUserDto createUser(String username, String email, int age) {
        String userId = "user_" + System.currentTimeMillis();

        RedisUserDto user = RedisUserDto.builder()
                .id(userId)
                .username(username)
                .email(email)
                .age(age)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        redisUserRepository.saveUser(user);
        return user;
    }

    public RedisUserDto getUserById(String userId) {
        return redisUserRepository.getUser(userId);
    }

    public void deleteUser(String userId) {
        String key = "user:" + userId;
        redisUserRepository.deleteValue(key);
    }

    // ========== 공통 메서드 ==========

    public void setCache(String key, Object value, long ttlMinutes) {
        redisUserRepository.setValueWithTTL(key, value, ttlMinutes, java.util.concurrent.TimeUnit.MINUTES);
    }

    public Object getCache(String key) {
        return redisUserRepository.getValue(key);
    }

    public void deleteCache(String key) {
        redisUserRepository.deleteValue(key);
    }

    public boolean exists(String key) {
        return redisUserRepository.hasKey(key);
    }

    public Set<String> searchKeys(String pattern) {
        return redisUserRepository.getKeys(pattern);
    }

    // ========== List 예제 ==========

    public void addToRecentItems(String userId, String itemId) {
        String key = "recent:" + userId;
        // 최근 10개 항목만 유지
        redisUserRepository.leftPush(key, itemId);

        // 리스트 크기 제한 (10개)
        int size = redisUserRepository.getListRange(key, 0, -1).size();
        if (size > 10) {
            redisUserRepository.rightPop(key);
        }
    }

    public List<Object> getRecentItems(String userId) {
        String key = "recent:" + userId;
        return redisUserRepository.getListRange(key, 0, 9);
    }

    // ========== Set 예제 ==========

    public void addToCart(String userId, String... itemIds) {
        String key = "cart:" + userId;
        redisUserRepository.addToSet(key, (Object[]) itemIds);
    }

    public Set<Object> getCart(String userId) {
        String key = "cart:" + userId;
        return redisUserRepository.getSet(key);
    }

    public void removeFromCart(String userId, String... itemIds) {
        String key = "cart:" + userId;
        redisUserRepository.removeFromSet(key, (Object[]) itemIds);
    }

    // ========== Counter 예제 ==========

    public Long incrementViewCount(String itemId) {
        String key = "views:" + itemId;
        return redisUserRepository.increment(key);
    }

    public Long getViewCount(String itemId) {
        String key = "views:" + itemId;
        Object value = redisUserRepository.getValue(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }
}
