package com.launchstack.user.controller;

import com.launchstack.auth.security.AuthenticatedUserPrincipal;
import com.launchstack.common.response.ApiResponse;
import com.launchstack.user.dto.CurrentUserProfileResponse;
import com.launchstack.user.dto.UpdateCurrentUserRequest;
import com.launchstack.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@SecurityRequirement(name = "bearerAuth")
public class CurrentUserProfileController {

    private final UserService userService;

    public CurrentUserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CurrentUserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        CurrentUserProfileResponse profile = userService.getCurrentUserProfile(principal);
        return ResponseEntity.ok(ApiResponse.success("Current profile retrieved successfully.", profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CurrentUserProfileResponse>> updateCurrentUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateCurrentUserRequest request) {
        CurrentUserProfileResponse profile = userService.updateCurrentUserProfile(principal, request);
        return ResponseEntity.ok(ApiResponse.success("Current profile updated successfully.", profile));
    }
}
