package com.wesjou.keymanager.exception;

public class BadApiKeyException extends RuntimeException {
    public BadApiKeyException() {
        super("Invalid API key");
    }
}
