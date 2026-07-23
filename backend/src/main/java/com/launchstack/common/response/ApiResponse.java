package com.launchstack.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Standard response wrapper for backend API responses.
 *
 * <p>Successful responses can carry data, while error responses can carry a message and
 * structured validation details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;
    private final List<?> errors;

    private ApiResponse(boolean success, String message, T data, Instant timestamp, List<?> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Request completed successfully.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now(), null);
    }

    public static ApiResponse<Void> error(String message) {
        return error(message, null);
    }

    public static ApiResponse<Void> error(String message, List<?> errors) {
        return new ApiResponse<>(false, message, null, Instant.now(), errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<?> getErrors() {
        return errors;
    }
}
