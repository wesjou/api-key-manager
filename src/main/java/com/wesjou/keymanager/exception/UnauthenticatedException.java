package com.wesjou.keymanager.exception;

public class UnauthenticatedException extends RuntimeException{
    public UnauthenticatedException() {
        super("Invalid or missing authentication credentials");
    }
}
