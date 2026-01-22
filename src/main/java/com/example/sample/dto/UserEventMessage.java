package com.example.sample.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserEventMessage(
        Long userId,
        String action,
        Instant timestamp
) {

}
