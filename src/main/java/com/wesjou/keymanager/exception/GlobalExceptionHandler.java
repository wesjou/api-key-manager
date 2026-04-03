package com.wesjou.keymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorEnvelope handleUserNotFound(UserNotFoundException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(ApiKeyGenerationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorEnvelope handleApiKeyGeneration(ApiKeyGenerationException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorEnvelope handleApiKeyNotFound(ApiKeyNotFoundException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(BadApiKeyException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorEnvelope handleBadApiKey(BadApiKeyException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(),
                LocalDateTime.now()));
    }
}
