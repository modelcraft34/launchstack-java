package com.launchstack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception used when a request is syntactically valid but semantically invalid for the application.
 */
public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
