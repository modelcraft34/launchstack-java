package com.launchstack.common.validation;

/**
 * Standard validation error entry returned inside API error responses.
 *
 * @param field the request field or property that failed validation
 * @param message the validation message for the failed field
 */
public record ValidationError(String field, String message) {
}
