package com.wesjou.keymanager.exception;

public class InvalidApiKeyException extends RuntimeException{
    public InvalidApiKeyException() {
        super("API key not found");
    }
}
