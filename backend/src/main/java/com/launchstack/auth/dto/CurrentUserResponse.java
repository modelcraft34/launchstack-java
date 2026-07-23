package com.launchstack.auth.dto;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        List<String> roles) {
}
