package com.example.sample.service;

import com.example.sample.common.CustomException;
import com.example.sample.common.ResponseCode;
import com.example.sample.domain.User;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.repository.MysqlUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MysqlService {

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
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "해당 사용자가 없습니다. id=" + id));
    }

    @Transactional
    public User update(Long id, UserRequestDto dto) {
        User user = mysqlUserRepository.findById(id)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "해당 사용자가 없습니다. id=" + id));
        user.update(dto.name(), dto.email());
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = mysqlUserRepository.findById(id)
                .orElseThrow(() -> new CustomException(ResponseCode.NOT_FOUND, "해당 사용자가 없습니다. id=" + id));
        Optional.ofNullable(user)
                .ifPresent(u -> {
                    mysqlUserRepository.deleteById(u.getId());
                });
    }

}
