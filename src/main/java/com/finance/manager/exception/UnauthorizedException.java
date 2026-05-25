package com.finance.manager.exception;

// thrown when user tries to do something they shouldn't
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
