package com.finance.manager.exception;

// thrown when something is not found in the db
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
