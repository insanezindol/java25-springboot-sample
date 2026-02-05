package com.example.sample.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisUserRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 단일 값 저장
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("Set key: {}, value: {}", key, value);
    }

    /**
     * 단일 값 저장 with TTL
     */
    public void setValueWithTTL(String key, Object value, long ttl, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl, timeUnit);
            log.info("Set key: {} with TTL: {} {}", key, ttl, timeUnit);
        } catch (JsonProcessingException e) {
            log.error("Error serializing value to JSON");
        }
    }

    /**
     * 값 조회
     */
    public Object getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        log.info("Get key: {}, value: {}", key, value);
        return value;
    }

    /**
     * 값 삭제
     */
    public Boolean deleteValue(String key) {
        Boolean result = redisTemplate.delete(key);
        log.info("Delete key: {}, success: {}", key, result);
        return result;
    }

    /**
     * 값 존재 여부 확인
     */
    public Boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        log.info("Key exists: {}, exists: {}", key, exists);
        return exists;
    }

    /**
     * TTL 설정
     */
    public Boolean setExpire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        log.info("Set expire key: {}, timeout: {} {}", key, timeout, unit);
        return result;
    }

    /**
     * TTL 조회
     */
    public Long getExpire(String key) {
        Long ttl = redisTemplate.getExpire(key);
        log.info("Get TTL key: {}, TTL: {}", key, ttl);
        return ttl;
    }

    // ========== Hash Operations ==========

    /**
     * Hash 저장
     */
    public void setHash(String key, Map<String, Object> hash) {
        redisTemplate.opsForHash().putAll(key, hash);
        log.info("Set hash key: {}, hash: {}", key, hash);
    }

    /**
     * Hash 필드 추가/수정
     */
    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
        log.info("Set hash field key: {}, field: {}, value: {}", key, field, value);
    }

    /**
     * Hash 조회
     */
    public Map<Object, Object> getHash(String key) {
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
        log.info("Get hash key: {}, hash: {}", key, hash);
        return hash;
    }

    /**
     * Hash 필드 조회
     */
    public Object getHashField(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        log.info("Get hash field key: {}, field: {}, value: {}", key, field, value);
        return value;
    }

    /**
     * Hash 필드 삭제
     */
    public Long deleteHashField(String key, String... fields) {
        Long result = redisTemplate.opsForHash().delete(key, (Object[]) fields);
        log.info("Delete hash fields key: {}, fields: {}, deleted: {}", key, fields, result);
        return result;
    }

    // ========== List Operations ==========

    /**
     * List 왼쪽 추가
     */
    public Long leftPush(String key, Object value) {
        Long size = redisTemplate.opsForList().leftPush(key, value);
        log.info("Left push key: {}, value: {}, size: {}", key, value, size);
        return size;
    }

    /**
     * List 오른쪽 추가
     */
    public Long rightPush(String key, Object value) {
        Long size = redisTemplate.opsForList().rightPush(key, value);
        log.info("Right push key: {}, value: {}, size: {}", key, value, size);
        return size;
    }

    /**
     * List 조회 (범위)
     */
    public List<Object> getListRange(String key, long start, long end) {
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        log.info("Get list range key: {}, start: {}, end: {}, size: {}",
                key, start, end, list != null ? list.size() : 0);
        return list;
    }

    /**
     * List 왼쪽 Pop
     */
    public Object leftPop(String key) {
        Object value = redisTemplate.opsForList().leftPop(key);
        log.info("Left pop key: {}, value: {}", key, value);
        return value;
    }

    /**
     * List 오른쪽 Pop
     */
    public Object rightPop(String key) {
        Object value = redisTemplate.opsForList().rightPop(key);
        log.info("Right pop key: {}, value: {}", key, value);
        return value;
    }

    // ========== Set Operations ==========

    /**
     * Set에 값 추가
     */
    public Long addToSet(String key, Object... values) {
        Long count = redisTemplate.opsForSet().add(key, values);
        log.info("Add to set key: {}, values: {}, added: {}", key, values, count);
        return count;
    }

    /**
     * Set 조회
     */
    public Set<Object> getSet(String key) {
        Set<Object> set = redisTemplate.opsForSet().members(key);
        log.info("Get set key: {}, size: {}", key, set != null ? set.size() : 0);
        return set;
    }

    /**
     * Set에서 값 삭제
     */
    public Long removeFromSet(String key, Object... values) {
        Long count = redisTemplate.opsForSet().remove(key, values);
        log.info("Remove from set key: {}, values: {}, removed: {}", key, values, count);
        return count;
    }

    // ========== Custom Operations ==========

    /**
     * 모든 키 조회 (패턴)
     */
    public Set<String> getKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        log.info("Get keys pattern: {}, count: {}", pattern, keys != null ? keys.size() : 0);
        return keys;
    }

    /**
     * 카운터 증가
     */
    public Long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        log.info("Increment key: {}, value: {}", key, value);
        return value;
    }

    /**
     * 카운터 감소
     */
    public Long decrement(String key) {
        Long value = redisTemplate.opsForValue().decrement(key);
        log.info("Decrement key: {}, value: {}", key, value);
        return value;
    }

}
