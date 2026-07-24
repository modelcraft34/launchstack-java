package com.launchstack.user.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean accountNonLocked,
        List<String> roles) {
}
