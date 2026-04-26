package com.wesjou.keymanager.exception;

public class AuthenticatedUserNotFoundException extends RuntimeException {
    public AuthenticatedUserNotFoundException() {
        super("Authenticated user not found");
    }
}
