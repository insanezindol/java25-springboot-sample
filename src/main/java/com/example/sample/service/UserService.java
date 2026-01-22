package com.example.sample.service;

import com.example.sample.domain.User;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.repository.MysqlUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final MysqlUserRepository mysqlUserRepository;

    @Transactional
    public Long save(UserRequestDto dto) {
        return mysqlUserRepository.save(dto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return mysqlUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findOne(Long id) {
        return mysqlUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));
    }

    @Transactional
    public void update(Long id, UserRequestDto dto) {
        User user = mysqlUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id=" + id));
        user.update(dto.name(), dto.email());
    }

    @Transactional
    public void delete(Long id) {
        mysqlUserRepository.deleteById(id);
    }

}
