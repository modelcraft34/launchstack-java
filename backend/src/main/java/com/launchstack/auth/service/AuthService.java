package com.launchstack.auth.service;

import com.launchstack.auth.dto.CurrentUserResponse;
import com.launchstack.auth.dto.ForgotPasswordRequest;
import com.launchstack.auth.dto.LoginRequest;
import com.launchstack.auth.dto.LogoutRequest;
import com.launchstack.auth.dto.RefreshTokenRequest;
import com.launchstack.auth.dto.RegisterRequest;
import com.launchstack.auth.dto.ResendVerificationRequest;
import com.launchstack.auth.dto.ResetPasswordRequest;
import com.launchstack.auth.dto.TokenResponse;
import com.launchstack.auth.dto.VerifyEmailRequest;
import com.launchstack.auth.security.AuthenticatedUserPrincipal;
import com.launchstack.auth.token.EmailVerificationToken;
import com.launchstack.auth.token.EmailVerificationTokenRepository;
import com.launchstack.auth.token.JwtTokenService;
import com.launchstack.auth.token.PasswordResetToken;
import com.launchstack.auth.token.PasswordResetTokenRepository;
import com.launchstack.auth.token.RefreshToken;
import com.launchstack.auth.token.RefreshTokenRepository;
import com.launchstack.common.exception.BadRequestException;
import com.launchstack.common.exception.NotFoundException;
import com.launchstack.email.EmailService;
import com.launchstack.role.Role;
import com.launchstack.role.RoleRepository;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final long emailVerificationTokenExpirationSeconds;
    private final long passwordResetTokenExpirationSeconds;
    private final String appBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            EmailService emailService,
            @Value("${auth.email-verification-token-expiration:86400}") long emailVerificationTokenExpirationSeconds,
            @Value("${auth.password-reset-token-expiration:3600}") long passwordResetTokenExpirationSeconds,
            @Value("${app.base-url:http://localhost:4200}") String appBaseUrl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.emailService = emailService;
        this.emailVerificationTokenExpirationSeconds = emailVerificationTokenExpirationSeconds;
        this.passwordResetTokenExpirationSeconds = passwordResetTokenExpirationSeconds;
        this.appBaseUrl = appBaseUrl;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered.");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim());
        user.setEnabled(false);
        user.getRoles().add(getOrCreateRole(ROLE_USER));
        User savedUser = userRepository.save(user);

        EmailVerificationToken verificationToken = createEmailVerificationToken(savedUser);
        emailService.sendEmailVerificationEmail(
                savedUser.getEmail(),
                verificationToken.getToken(),
                buildLink("/verify-email", verificationToken.getToken()));
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(request.token().trim())
                .orElseThrow(() -> new BadRequestException("Email verification token is invalid."));

        if (verificationToken.isUsed()) {
            throw new BadRequestException("Email verification token has already been used.");
        }

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Email verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.markUsed(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);
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

    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        userRepository.findByEmail(normalizedEmail)
                .filter(user -> !user.isEnabled())
                .ifPresent(user -> {
                    EmailVerificationToken verificationToken = createEmailVerificationToken(user);
                    emailService.sendEmailVerificationEmail(
                            user.getEmail(),
                            verificationToken.getToken(),
                            buildLink("/verify-email", verificationToken.getToken()));
                });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        userRepository.findByEmail(normalizedEmail)
                .ifPresent(user -> {
                    PasswordResetToken resetToken = createPasswordResetToken(user);
                    emailService.sendPasswordResetEmail(
                            user.getEmail(),
                            resetToken.getToken(),
                            buildLink("/reset-password", resetToken.getToken()));
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token().trim())
                .orElseThrow(() -> new BadRequestException("Password reset token is invalid."));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Password reset token has already been used.");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.markUsed(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        revokeActiveRefreshTokens(user.getId());
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
                generateRandomTokenValue(),
                user,
                Instant.now().plusSeconds(jwtTokenService.getRefreshTokenExpirationSeconds()));
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        return new TokenResponse(
                accessToken,
                savedRefreshToken.getToken(),
                "Bearer",
                jwtTokenService.getAccessTokenExpirationSeconds());
    }

    private EmailVerificationToken createEmailVerificationToken(User user) {
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                generateRandomTokenValue(),
                user,
                Instant.now().plusSeconds(emailVerificationTokenExpirationSeconds));
        return emailVerificationTokenRepository.save(verificationToken);
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        PasswordResetToken resetToken = new PasswordResetToken(
                generateRandomTokenValue(),
                user,
                Instant.now().plusSeconds(passwordResetTokenExpirationSeconds));
        return passwordResetTokenRepository.save(resetToken);
    }

    private void revokeActiveRefreshTokens(Long userId) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        if (refreshTokens.isEmpty()) {
            return;
        }

        refreshTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(refreshTokens);
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateRandomTokenValue() {
        byte[] tokenBytes = new byte[48];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String buildLink(String path, String token) {
        String normalizedBaseUrl = appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length() - 1) : appBaseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBaseUrl + normalizedPath + "?token=" + token;
    }
}
