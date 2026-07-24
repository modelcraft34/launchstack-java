package com.launchstack.user.dto;

import jakarta.validation.constraints.AssertTrue;

public record UpdateUserStatusRequest(
        Boolean enabled,
        Boolean accountNonLocked) {

    @AssertTrue(message = "at least one status field must be provided")
    public boolean hasAtLeastOneStatusField() {
        return enabled != null || accountNonLocked != null;
    }
}
