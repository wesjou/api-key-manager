package com.wesjou.keymanager.exception;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException() {
        super("API key not found");
    }
}
