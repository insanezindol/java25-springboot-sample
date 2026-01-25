package com.example.sample.common;

import com.example.sample.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> notFoundException() {
        ResponseCode responseCode = ResponseCode.NOT_FOUND;
        return ResponseEntity.status(responseCode.getHttpStatus()).body(new ErrorResponse(responseCode));
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> validException(ValidationException e) {
        log.error("validException error", e);
        String message = e.getMessage();
        if (e instanceof ConstraintViolationException) {
            message = new ArrayList<>(((ConstraintViolationException) e).getConstraintViolations()).get(0).getMessage();
        }
        ResponseCode responseCode = ResponseCode.BAD_REQUEST;
        return ResponseEntity.status(responseCode.getHttpStatus()).body(new ErrorResponse(responseCode, message));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> common(Exception e) {
        log.error("INTERNAL_SERVER_ERROR", e);
        ResponseCode responseCode = ResponseCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(responseCode.getHttpStatus()).body(new ErrorResponse(responseCode, e.getMessage()));
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> optimisticLockException(OptimisticLockException e) {
        log.error("CUSTOM_EXCEPTION", e);
        ResponseCode responseCode = ResponseCode.DUPLICATE_REQUEST;
        return ResponseEntity.status(responseCode.getHttpStatus()).body(new ErrorResponse(responseCode, responseCode.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customException(CustomException e) {
        log.error("CUSTOM_EXCEPTION", e);
        return ResponseEntity.status(e.getErrorResponse().httpStatus()).body(e.getErrorResponse());
    }

}
