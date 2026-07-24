package com.launchstack.user.dto;

import java.util.List;

public record CurrentUserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        List<String> roles) {
}
