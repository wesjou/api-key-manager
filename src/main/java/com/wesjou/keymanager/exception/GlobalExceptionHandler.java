package com.wesjou.keymanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorEnvelope handleUnauthenticated(UnauthenticatedException ex) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorEnvelope handleInvalidEmail(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();

        if (errorMessage != null && !errorMessage.isEmpty()) {
            errorMessage = errorMessage.substring(0, 1).toUpperCase() + errorMessage.substring(1);
        }

        return new ErrorEnvelope(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,
                LocalDateTime.now()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorEnvelope handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(ApiKeyAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorEnvelope handleApiKeyAccessDenied(ApiKeyAccessDeniedException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorEnvelope handleAuthenticatedUserNotFound(AuthenticatedUserNotFoundException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(),
                LocalDateTime.now()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    ErrorEnvelope handleRateLimitExceeded(RateLimitExceededException ex) {
        return new ErrorEnvelope(new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage(),
                LocalDateTime.now()));
    }
}
