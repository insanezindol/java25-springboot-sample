package com.example.sample.controller;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/users")
    public ResponseEntity<RedisUserDto> createUser(@RequestBody RedisUserDto userDto) {
        RedisUserDto createdUser = redisService.createUser(
                userDto.username(),
                userDto.email(),
                userDto.age()
        );
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<RedisUserDto> getUser(@PathVariable String userId) {
        RedisUserDto user = redisService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        redisService.deleteUser(userId);
        return ResponseEntity.ok("사용자가 삭제되었습니다: " + userId);
    }

    // ========== List/Set API ==========

    @PostMapping("/users/{userId}/recent")
    public ResponseEntity<String> addRecentItem(
            @PathVariable String userId,
            @RequestParam String itemId) {
        redisService.addToRecentItems(userId, itemId);
        return ResponseEntity.ok("최근 목록에 추가되었습니다");
    }

    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<List<Object>> getRecentItems(@PathVariable String userId) {
        List<Object> items = redisService.getRecentItems(userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/users/{userId}/cart")
    public ResponseEntity<String> addToCart(
            @PathVariable String userId,
            @RequestBody List<String> itemIds) {
        redisService.addToCart(userId, itemIds.toArray(new String[0]));
        return ResponseEntity.ok("장바구니에 추가되었습니다");
    }

    @GetMapping("/users/{userId}/cart")
    public ResponseEntity<Set<Object>> getCart(@PathVariable String userId) {
        Set<Object> cart = redisService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // ========== Counter API ==========

    @PostMapping("/items/{itemId}/view")
    public ResponseEntity<Long> incrementViewCount(@PathVariable String itemId) {
        Long count = redisService.incrementViewCount(itemId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/items/{itemId}/views")
    public ResponseEntity<Long> getViewCount(@PathVariable String itemId) {
        Long count = redisService.getViewCount(itemId);
        return ResponseEntity.ok(count);
    }

    // ========== 관리자 API ==========

    @GetMapping("/keys")
    public ResponseEntity<Set<String>> searchKeys(@RequestParam(required = false) String pattern) {
        String searchPattern = pattern != null ? pattern : "*";
        Set<String> keys = redisService.searchKeys(searchPattern);
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = Map.of(
                "service", "Redis CRUD Sample API",
                "status", "running",
                "timestamp", java.time.LocalDateTime.now()
        );
        return ResponseEntity.ok(info);
    }

}