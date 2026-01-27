package com.example.sample.service;

import com.example.sample.dto.RedisUserDto;
import com.example.sample.repository.RedisUserRepository;
import com.google.gson.JsonObject;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    final String _ID = "USERID";
    final String _USERNAME = "USERNAME";
    final String _EMAIL = "EMAIL@EMAIL.COM";
    final int _AGE = 30;

    RedisService redisService;
    FixtureMonkey fixtureMonkey;

    @Mock
    RedisUserRepository redisUserRepository;

    @BeforeEach
    void setUp() {
        this.redisService = new RedisService(redisUserRepository);
        this.fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @Test
    @DisplayName("사용자 생성")
    void createUser() {
        // given
        final RedisUserDto redisUserDto = fixtureMonkey.giveMeBuilder(RedisUserDto.class)
                .set(javaGetter(RedisUserDto::id), _ID)
                .set(javaGetter(RedisUserDto::username), _USERNAME)
                .set(javaGetter(RedisUserDto::email), _EMAIL)
                .set(javaGetter(RedisUserDto::age), _AGE)
                .sample();

        // when
        doNothing().when(redisUserRepository).setValueWithTTL(any(), any(), anyLong(), any());
        RedisUserDto savedUser = redisService.createUser(redisUserDto);

        // then
        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(redisUserDto.id(), savedUser.id());
        Assertions.assertEquals(redisUserDto.username(), savedUser.username());
        Assertions.assertEquals(redisUserDto.email(), savedUser.email());
        Assertions.assertEquals(redisUserDto.age(), savedUser.age());
        verify(redisUserRepository, times(1)).setValueWithTTL(any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("사용자 ID 조회")
    void getUserById() {
        // given
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", _ID);
        jsonObject.addProperty("username", _USERNAME);
        jsonObject.addProperty("email", _EMAIL);
        jsonObject.addProperty("age", _AGE);

        // when
        when(redisUserRepository.getValue(any())).thenReturn(jsonObject);
        Object returnObject = redisService.getUserById(_ID);

        // then
        Assertions.assertNotNull(returnObject);
        verify(redisUserRepository, times(1)).getValue(any());
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // given
        final boolean deleteFlag = true;

        // when
        when(redisUserRepository.deleteValue(any())).thenReturn(deleteFlag);
        redisService.deleteUser(_ID);

        // then
        verify(redisUserRepository, times(1)).deleteValue(any());
    }

    @Test
    @DisplayName("키 패턴 조회")
    void searchKeys() {
        // given
        final String pattern = "*";
        final Set<String> result = Set.of("user:abc1", "user:abc2", "user:abc3");

        // when
        when(redisUserRepository.getKeys(any())).thenReturn(result);
        Set<String> keySet = redisService.searchKeys(pattern);

        // then
        Assertions.assertNotNull(keySet);
        Assertions.assertEquals(result.size(), keySet.size());
        verify(redisUserRepository, times(1)).getKeys(any());
    }

    @Test
    @DisplayName("최근상품 추가")
    void addToRecentItems() {
        // given
        final String userId = "abc1";
        final String itemId = "100";
        final List<Object> objects = List.of("user:abc1", "user:abc2", "user:abc3", "user:abc4", "user:abc5",
                "user:abc6", "user:abc7", "user:abc8", "user:abc9", "user:abc10", "user:abc11");

        // when
        when(redisUserRepository.leftPush(any(), any())).thenReturn(1L);
        when(redisUserRepository.getListRange(any(), anyLong(), anyLong())).thenReturn(objects);
        when(redisUserRepository.rightPop(any())).thenReturn(itemId);
        redisService.addToRecentItems(userId, itemId);

        // then
        verify(redisUserRepository, times(1)).leftPush(any(), any());
        verify(redisUserRepository, times(1)).getListRange(any(), anyLong(), anyLong());
        verify(redisUserRepository, times(1)).rightPop(any());
    }

    @Test
    @DisplayName("최근상품 조회")
    void getRecentItems() {
        // given
        final List<Object> objects = List.of("user:abc1", "user:abc2", "user:abc3", "user:abc4", "user:abc5");

        // when
        when(redisUserRepository.getListRange(any(), anyLong(), anyLong())).thenReturn(objects);
        List<Object> recentItems = redisService.getRecentItems(_ID);

        // then
        Assertions.assertNotNull(recentItems);
        Assertions.assertEquals(objects.size(), recentItems.size());
        verify(redisUserRepository, times(1)).getListRange(any(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("장바구니 추가")
    void addToCart() {
        // given
        final String userId = "abc1";
        final String[] itemIds = {"100", "200", "300"};

        // when
        when(redisUserRepository.addToSet(any(), any(), any(), any())).thenReturn(3L);
        redisService.addToCart(userId, itemIds);

        // then
        verify(redisUserRepository, times(1)).addToSet(any(), any(), any(), any());
    }

    @Test
    @DisplayName("장바구니 조회")
    void getCart() {
        // given
        final String userId = "abc1";
        final Set<Object> result = Set.of("user:abc1", "user:abc2", "user:abc3");

        // when
        when(redisUserRepository.getSet(any())).thenReturn(result);
        Set<Object> carts = redisService.getCart(userId);

        // then
        Assertions.assertNotNull(carts);
        Assertions.assertEquals(result.size(), carts.size());
        verify(redisUserRepository, times(1)).getSet(any());
    }

    @Test
    @DisplayName("장바구니 삭제")
    void removeFromCart() {
        // given
        final String userId = "abc1";
        final String[] itemIds = {"100", "200", "300"};

        // when
        when(redisUserRepository.removeFromSet(any(), any(), any(), any())).thenReturn(1L);
        redisService.removeFromCart(userId, itemIds);

        // then
        verify(redisUserRepository, times(1)).removeFromSet(any(), any(), any(), any());
    }

    @Test
    @DisplayName("조회수 증가")
    void incrementViewCount() {
        // given
        final String itemId = "abc1";
        final Long viewCount = 100L;

        // when
        when(redisUserRepository.increment(any())).thenReturn(viewCount);
        Long resultViewCount = redisService.incrementViewCount(itemId);

        // then
        Assertions.assertNotNull(resultViewCount);
        Assertions.assertEquals(viewCount, resultViewCount);
        verify(redisUserRepository, times(1)).increment(any());
    }

    @Test
    @DisplayName("조회수 가져오기")
    void getViewCount() {
        // given
        final String itemId = "abc1";
        final Long viewCount = 100L;

        // when
        when(redisUserRepository.getValue(any())).thenReturn(viewCount);
        Long resultViewCount = redisService.getViewCount(itemId);

        // then
        Assertions.assertNotNull(resultViewCount);
        Assertions.assertEquals(viewCount, resultViewCount);
        verify(redisUserRepository, times(1)).getValue(any());
    }

}