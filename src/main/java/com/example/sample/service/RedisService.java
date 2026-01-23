package com.example.sample.service;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.repository.RedisUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final static String _REDIS_USER_PREFIX = "user:";
    private final RedisUserRepository redisUserRepository;

    // ========== User 관련 메서드 ==========
    public RedisUserDto createUser(RedisUserDto user) {
        RedisUserDto savedUser = RedisUserDto.builder()
                .id(user.id())
                .username(user.username())
                .email(user.email())
                .age(user.age())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String key = _REDIS_USER_PREFIX + savedUser.id();
        redisUserRepository.setValueWithTTL(key, savedUser, 1L, TimeUnit.HOURS);
        return savedUser;
    }

    public Object getUserById(String userId) {
        return redisUserRepository.getValue(_REDIS_USER_PREFIX + userId);
    }

    public void deleteUser(String userId) {
        redisUserRepository.deleteValue(_REDIS_USER_PREFIX + userId);
    }

    // ========== 공통 메서드 ==========

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
