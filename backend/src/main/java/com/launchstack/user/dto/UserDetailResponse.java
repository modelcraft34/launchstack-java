package com.launchstack.user.dto;

import java.time.Instant;
import java.util.List;

public record UserDetailResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean accountNonLocked,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt) {
}
