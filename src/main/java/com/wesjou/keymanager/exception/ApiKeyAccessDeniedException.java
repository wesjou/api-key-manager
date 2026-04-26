package com.wesjou.keymanager.exception;

public class ApiKeyAccessDeniedException extends RuntimeException {
    public ApiKeyAccessDeniedException() {
        super("User not allowed to access this resource");
    }
}
