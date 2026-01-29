package com.example.sample.controller;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Tag(name = "사용자 관리 API(redis)", description = "사용자 관리 redis CRUD")
@Slf4j
@RestController
@RequestMapping("/api/v1/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @Operation(summary = "사용자 상세 조회", description = "사용자 상세를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable String userId) {
        Object user = redisService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "사용자 생성", description = "사용자를 생성합니다.")
    @PostMapping("/users")
    public ResponseEntity<RedisUserDto> createUser(@RequestBody RedisUserDto userDto) {
        RedisUserDto createdUser = redisService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        redisService.deleteUser(userId);
        return ResponseEntity.ok("사용자가 삭제되었습니다: " + userId);
    }

    // ========== List/Set API ==========

    @Operation(summary = "사용자 최근 목록 조회", description = "사용자 최근 목록을 조회합니다.")
    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<List<Object>> getRecentItems(@PathVariable String userId) {
        List<Object> items = redisService.getRecentItems(userId);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "사용자 최근 목록 추가", description = "사용자 최근 목록을 추가합니다.")
    @PostMapping("/users/{userId}/recent")
    public ResponseEntity<String> addRecentItem(
            @PathVariable String userId,
            @RequestParam String itemId) {
        redisService.addToRecentItems(userId, itemId);
        return ResponseEntity.ok("최근 목록에 추가되었습니다");
    }

    @Operation(summary = "사용자 장바구니 조회", description = "사용자 장바구니를 조회합니다.")
    @GetMapping("/users/{userId}/cart")
    public ResponseEntity<Set<Object>> getCart(@PathVariable String userId) {
        Set<Object> cart = redisService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "사용자 장바구니 추가", description = "사용자 장바구니를 추가합니다.")
    @PostMapping("/users/{userId}/cart")
    public ResponseEntity<String> addToCart(
            @PathVariable String userId,
            @RequestBody List<String> itemIds) {
        redisService.addToCart(userId, itemIds.toArray(new String[0]));
        return ResponseEntity.ok("장바구니에 추가되었습니다");
    }

    // ========== Counter API ==========

    @Operation(summary = "상품 조회수 조회", description = "상품 조회수를 조회합니다.")
    @GetMapping("/items/{itemId}/views")
    public ResponseEntity<Long> getViewCount(@PathVariable String itemId) {
        Long count = redisService.getViewCount(itemId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "상품 조회수 증가", description = "상품 조회수를 증가합니다.")
    @PostMapping("/items/{itemId}/view")
    public ResponseEntity<Long> incrementViewCount(@PathVariable String itemId) {
        Long count = redisService.incrementViewCount(itemId);
        return ResponseEntity.ok(count);
    }

    // ========== 관리자 API ==========

    @Operation(summary = "redis 전체 키 목록 조회", description = "redis 전체 키 목록을 조회합니다.")
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> searchKeys(@RequestParam(required = false) String pattern) {
        String searchPattern = pattern != null ? pattern : "*";
        Set<String> keys = redisService.searchKeys(searchPattern);
        return ResponseEntity.ok(keys);
    }

}