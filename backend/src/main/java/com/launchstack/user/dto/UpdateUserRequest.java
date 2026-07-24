package com.launchstack.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UpdateUserRequest(
        @NotBlank(message = "firstName is required")
        @Size(max = 100, message = "firstName must be at most 100 characters")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 100, message = "lastName must be at most 100 characters")
        String lastName,

        @NotEmpty(message = "roles must not be empty")
        Set<@NotBlank(message = "role must not be blank") String> roles,

        @NotNull(message = "enabled is required")
        Boolean enabled) {
}
