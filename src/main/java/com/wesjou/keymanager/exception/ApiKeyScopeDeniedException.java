package com.wesjou.keymanager.exception;

public class ApiKeyScopeDeniedException extends RuntimeException {
    public ApiKeyScopeDeniedException() {
        super("User scope access is not allowed");
    }
}
