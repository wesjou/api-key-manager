package com.wesjou.keymanager.exception;

public class InvalidScopeException extends RuntimeException {
    public InvalidScopeException() {
        super("Scopes must not be empty");
    }
}
