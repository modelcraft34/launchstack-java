package com.launchstack.auth.service;

import com.launchstack.auth.dto.CurrentUserResponse;
import com.launchstack.auth.dto.LoginRequest;
import com.launchstack.auth.dto.LogoutRequest;
import com.launchstack.auth.dto.RefreshTokenRequest;
import com.launchstack.auth.dto.RegisterRequest;
import com.launchstack.auth.dto.TokenResponse;
import com.launchstack.auth.security.AuthenticatedUserPrincipal;
import com.launchstack.auth.token.JwtTokenService;
import com.launchstack.auth.token.RefreshToken;
import com.launchstack.auth.token.RefreshTokenRepository;
import com.launchstack.common.exception.BadRequestException;
import com.launchstack.common.exception.NotFoundException;
import com.launchstack.role.Role;
import com.launchstack.role.RoleRepository;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.security.SecureRandom;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim());
        user.getRoles().add(getOrCreateRole(ROLE_USER));
        User savedUser = userRepository.save(user);

        return issueTokenPair(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password.");
        }

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new BadRequestException("User account is not active.");
        }

        return issueTokenPair(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken().trim())
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid."));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new BadRequestException("User account is not active.");
        }

        return new TokenResponse(
                jwtTokenService.generateAccessToken(user),
                refreshToken.getToken(),
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken().trim())
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid."));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(AuthenticatedUserPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new NotFoundException("User was not found."));

        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), roles);
    }

    @Transactional
    public void seedRolesAndAdmin(String adminEmail, String adminPassword) {
        Role userRole = getOrCreateRole(ROLE_USER);
        Role adminRole = getOrCreateRole(ROLE_ADMIN);

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        String normalizedEmail = normalizeEmail(adminEmail);
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        User admin = new User(
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                "Admin",
                "User");
        admin.getRoles().addAll(Set.of(userRole, adminRole));
        userRepository.save(admin);
    }

    private TokenResponse issueTokenPair(User user) {
        String accessToken = jwtTokenService.generateAccessToken(user);
        RefreshToken refreshToken = new RefreshToken(
                generateRefreshTokenValue(),
                user,
                Instant.now().plusSeconds(jwtTokenService.getRefreshTokenExpirationSeconds()));
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        return new TokenResponse(
                accessToken,
                savedRefreshToken.getToken(),
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds());
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateRefreshTokenValue() {
        byte[] tokenBytes = new byte[48];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
