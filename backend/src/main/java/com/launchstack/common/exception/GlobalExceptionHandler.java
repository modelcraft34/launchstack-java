package com.launchstack.common.exception;

import com.launchstack.common.response.ApiResponse;
import com.launchstack.common.validation.ValidationError;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed.", mapFieldErrors(exception.getBindingResult().getFieldErrors())));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed.", mapFieldErrors(exception.getBindingResult().getFieldErrors())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath() == null ? null : violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred."));
    }

    private List<ValidationError> mapFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Invalid value")))
                .toList();
    }
}
