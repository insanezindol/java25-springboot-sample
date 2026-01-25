package com.example.sample.dto;

import com.example.sample.common.ResponseCode;
import jakarta.validation.constraints.NotNull;

public record ErrorResponse(int httpStatus, String errorCode, long timestamp, String message) {

    public ErrorResponse(@NotNull ResponseCode responseCode) {
        this(responseCode.getHttpStatus().value(), responseCode.getCode(), System.currentTimeMillis(), responseCode.getMessage());
    }

    public ErrorResponse(@NotNull ResponseCode responseCode, String message) {
        this(responseCode.getHttpStatus().value(), responseCode.getCode(), System.currentTimeMillis(), message);
    }

}
