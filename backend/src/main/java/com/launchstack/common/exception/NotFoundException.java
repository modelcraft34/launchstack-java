package com.launchstack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception used when an application resource cannot be found.
 */
public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
