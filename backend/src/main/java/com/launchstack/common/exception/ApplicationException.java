package com.launchstack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for application-specific exceptions that should map to a defined HTTP status.
 */
public abstract class ApplicationException extends RuntimeException {

    private final HttpStatus status;

    protected ApplicationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
