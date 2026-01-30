package com.example.sample.dto;

import lombok.Builder;

@Builder
public record RedisUserDto(
        String id,
        String username,
        String email,
        int age
) {

}