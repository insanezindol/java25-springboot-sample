package com.example.sample.service;

import com.example.sample.domain.User;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.repository.MysqlUserRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MysqlServiceTest {

    @InjectMocks
    MysqlService mysqlService;

    @Mock
    MysqlUserRepository mysqlUserRepository;

    FixtureMonkey fixtureMonkey;

    final Long _ID1 = 1L;
    final String _NAME = "홍길동";
    final String _NAME2 = "김둘리";
    final String _EMAIL = "test@test.com";

    @BeforeEach
    void setUp() {
        this.fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @Test
    @DisplayName("사용자 저장")
    void save() {
        // given
        User user = fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getId), _ID1)
                .set(javaGetter(User::getName), _NAME)
                .set(javaGetter(User::getEmail), _EMAIL)
                .sample();
        UserRequestDto userRequestDto = fixtureMonkey.giveMeBuilder(UserRequestDto.class)
                .set(javaGetter(UserRequestDto::name), _NAME)
                .set(javaGetter(UserRequestDto::email), _EMAIL)
                .sample();
        when(mysqlUserRepository.save(any())).thenReturn(user);

        // when
        Long savedUserId = mysqlService.save(userRequestDto);

        // then
        assertNotNull(savedUserId);
        assertEquals(savedUserId, _ID1);
        verify(mysqlUserRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("사용자 전체 검색")
    void findAll() {
        // given
        final int docSize = 5;
        List<User> userInfos = fixtureMonkey.giveMe(User.class, docSize);
        when(mysqlUserRepository.findAll()).thenReturn(userInfos);

        // when
        List<User> list = mysqlService.findAll();

        // then
        assertNotNull(list);
        assertEquals(docSize, list.size());
        verify(mysqlUserRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("특정 사용자 검색")
    void findOne() {
        // given
        User user = fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getId), _ID1)
                .set(javaGetter(User::getName), _NAME)
                .set(javaGetter(User::getEmail), _EMAIL)
                .sample();
        when(mysqlUserRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        User findedUser = mysqlService.findOne(_ID1);

        // then
        assertNotNull(user);
        assertEquals(_ID1, findedUser.getId());
        verify(mysqlUserRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("사용자 수정")
    void update() {
        // given
        User user = fixtureMonkey.giveMeBuilder(User.class)
                .set(javaGetter(User::getId), _ID1)
                .set(javaGetter(User::getName), _NAME)
                .set(javaGetter(User::getEmail), _EMAIL)
                .sample();
        UserRequestDto userRequestDto = fixtureMonkey.giveMeBuilder(UserRequestDto.class)
                .set(javaGetter(UserRequestDto::name), _NAME2)
                .set(javaGetter(UserRequestDto::email), _EMAIL)
                .sample();
        when(mysqlUserRepository.findById(any())).thenReturn(Optional.of(user));

        // when
        User savedUser = mysqlService.update(_ID1, userRequestDto);

        // then
        assertNotNull(savedUser);
        assertEquals(_NAME2, savedUser.getName());
        verify(mysqlUserRepository, times(1)).findById(any());
    }

    @Test
    @DisplayName("사용자 삭제")
    void delete() {
        // given
        User user = fixtureMonkey.giveMeOne(User.class);
        when(mysqlUserRepository.findById(any())).thenReturn(Optional.of(user));

        // when
        mysqlService.delete(_ID1);

        // then
        verify(mysqlUserRepository, times(1)).findById(any());
        verify(mysqlUserRepository, times(1)).deleteById(any());
    }

}