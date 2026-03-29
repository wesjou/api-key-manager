package com.wesjou.keymanager.exception;

public class ApiKeyGenerationException extends RuntimeException{
    public ApiKeyGenerationException() {
        super("Invalid or missing authentication credentials");
    }
}
