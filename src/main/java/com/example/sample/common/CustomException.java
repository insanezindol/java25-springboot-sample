package com.example.sample.common;

import com.example.sample.dto.ErrorResponse;

public class CustomException extends RuntimeException {

    private final ErrorResponse errorResponse;

    public CustomException(ResponseCode responseCode, String customMessage) {
        this.errorResponse = new ErrorResponse(responseCode, customMessage);
    }

    public CustomException(ResponseCode responseCode) {
        this.errorResponse = new ErrorResponse(responseCode);
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

}
