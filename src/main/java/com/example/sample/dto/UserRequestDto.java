package com.example.sample.dto;

import com.example.sample.domain.User;
import lombok.Builder;

@Builder
public record UserRequestDto(String name, String email) {
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }
}
