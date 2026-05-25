package com.finance.manager.exception;

// thrown when trying to create something that already exists (like duplicate email)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
