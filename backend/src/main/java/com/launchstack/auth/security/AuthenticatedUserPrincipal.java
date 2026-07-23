package com.launchstack.auth.security;

public record AuthenticatedUserPrincipal(Long userId, String email) {
}
