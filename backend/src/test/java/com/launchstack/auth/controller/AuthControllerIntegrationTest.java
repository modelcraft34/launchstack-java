package com.launchstack.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchstack.auth.token.EmailVerificationToken;
import com.launchstack.auth.token.EmailVerificationTokenRepository;
import com.launchstack.auth.token.PasswordResetToken;
import com.launchstack.auth.token.PasswordResetTokenRepository;
import com.launchstack.auth.token.RefreshToken;
import com.launchstack.auth.token.RefreshTokenRepository;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerCreatesVerificationTokenAndDisablesUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload("test@launchstack.dev"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify your email address."))
                .andExpect(jsonPath("$.timestamp").exists());

        User savedUser = userRepository.findByEmail("test@launchstack.dev").orElseThrow();
        assertThat(savedUser.isEnabled()).isFalse();
        assertThat(savedUser.getPasswordHash()).startsWith("$2");

        List<EmailVerificationToken> tokens = emailVerificationTokenRepository.findAllByUserId(savedUser.getId());
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).isUsed()).isFalse();
    }

    @Test
    void duplicateEmailFailure() throws Exception {
        registerUser("dupe@launchstack.dev");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload("dupe@launchstack.dev"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already registered."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void verifyEmailSuccess() throws Exception {
        registerUser("verify@launchstack.dev");
        User user = userRepository.findByEmail("verify@launchstack.dev").orElseThrow();
        EmailVerificationToken token = latestVerificationToken(user.getId());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token.getToken()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully."));

        EmailVerificationToken savedToken = emailVerificationTokenRepository.findByToken(token.getToken()).orElseThrow();
        User savedUser = userRepository.findByEmail("verify@launchstack.dev").orElseThrow();
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedToken.isUsed()).isTrue();
        assertThat(savedToken.getUsedAt()).isNotNull();
    }

    @Test
    void verifyEmailInvalidTokenFailure() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "bad-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification token is invalid."));
    }

    @Test
    void verifyEmailExpiredTokenFailure() throws Exception {
        registerUser("verify-expired@launchstack.dev");
        User user = userRepository.findByEmail("verify-expired@launchstack.dev").orElseThrow();
        EmailVerificationToken token = latestVerificationToken(user.getId());
        token.setExpiresAt(Instant.now().minusSeconds(1));
        emailVerificationTokenRepository.save(token);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token.getToken()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification token has expired."));
    }

    @Test
    void verifyEmailAlreadyUsedTokenFailure() throws Exception {
        registerUser("verify-used@launchstack.dev");
        User user = userRepository.findByEmail("verify-used@launchstack.dev").orElseThrow();
        EmailVerificationToken token = latestVerificationToken(user.getId());
        token.markUsed(Instant.now());
        emailVerificationTokenRepository.save(token);

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token.getToken()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification token has already been used."));
    }

    @Test
    void resendVerificationReturnsGenericSafeResponse() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "missing@launchstack.dev"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If the account exists, the requested email has been sent."));

        assertThat(emailVerificationTokenRepository.count()).isZero();
    }

    @Test
    void resendVerificationCreatesNewTokenWhenUserExistsAndUnverified() throws Exception {
        registerUser("resend@launchstack.dev");

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "resend@launchstack.dev"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User user = userRepository.findByEmail("resend@launchstack.dev").orElseThrow();
        assertThat(emailVerificationTokenRepository.findAllByUserId(user.getId())).hasSize(2);
    }

    @Test
    void loginSuccess() throws Exception {
        verifyUser("login@launchstack.dev");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "login@launchstack.dev",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());
    }

    @Test
    void loginInvalidPasswordFailure() throws Exception {
        verifyUser("wrong-pass@launchstack.dev");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "wrong-pass@launchstack.dev",
                                "password", "BadPassword123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void loginFailsForUnverifiedUser() throws Exception {
        registerUser("not-verified@launchstack.dev");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "not-verified@launchstack.dev",
                                "password", "Password123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User account is not active."));
    }

    @Test
    void refreshTokenSuccess() throws Exception {
        String refreshToken = loginAndGetRefreshToken("refresh@launchstack.dev");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    void refreshTokenInvalidFailure() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "invalid-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid."));
    }

    @Test
    void refreshTokenRevokedFailure() throws Exception {
        String refreshToken = loginAndGetRefreshToken("revoked@launchstack.dev");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked."));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        String refreshToken = loginAndGetRefreshToken("logout@launchstack.dev");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void forgotPasswordReturnsGenericSafeResponse() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "missing@launchstack.dev"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If the account exists, the requested email has been sent."));

        assertThat(passwordResetTokenRepository.count()).isZero();
    }

    @Test
    void forgotPasswordCreatesResetTokenWhenUserExists() throws Exception {
        verifyUser("forgot@launchstack.dev");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "forgot@launchstack.dev"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User user = userRepository.findByEmail("forgot@launchstack.dev").orElseThrow();
        assertThat(passwordResetTokenRepository.findAllByUserId(user.getId())).hasSize(1);
    }

    @Test
    void resetPasswordSuccessUpdatesHashAndRevokesRefreshTokens() throws Exception {
        String refreshToken = loginAndGetRefreshToken("reset-success@launchstack.dev");
        requestPasswordReset("reset-success@launchstack.dev");

        User beforeReset = userRepository.findByEmail("reset-success@launchstack.dev").orElseThrow();
        String previousHash = beforeReset.getPasswordHash();

        PasswordResetToken resetToken = latestResetToken(beforeReset.getId());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", resetToken.getToken(),
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully."));

        User afterReset = userRepository.findByEmail("reset-success@launchstack.dev").orElseThrow();
        assertThat(afterReset.getPasswordHash()).isNotEqualTo(previousHash);
        assertThat(passwordEncoder.matches("NewPassword123!", afterReset.getPasswordHash())).isTrue();

        PasswordResetToken usedToken = passwordResetTokenRepository.findByToken(resetToken.getToken()).orElseThrow();
        assertThat(usedToken.isUsed()).isTrue();

        RefreshToken revokedRefreshToken = refreshTokenRepository.findByToken(refreshToken).orElseThrow();
        assertThat(revokedRefreshToken.isRevoked()).isTrue();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "reset-success@launchstack.dev",
                                "password", "NewPassword123!"))))
                .andExpect(status().isOk());
    }

    @Test
    void resetPasswordInvalidTokenFailure() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "invalid-reset-token",
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Password reset token is invalid."));
    }

    @Test
    void resetPasswordExpiredTokenFailure() throws Exception {
        verifyUser("reset-expired@launchstack.dev");
        requestPasswordReset("reset-expired@launchstack.dev");

        User user = userRepository.findByEmail("reset-expired@launchstack.dev").orElseThrow();
        PasswordResetToken resetToken = latestResetToken(user.getId());
        resetToken.setExpiresAt(Instant.now().minusSeconds(1));
        passwordResetTokenRepository.save(resetToken);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", resetToken.getToken(),
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Password reset token has expired."));
    }

    @Test
    void resetPasswordUsedTokenFailure() throws Exception {
        verifyUser("reset-used@launchstack.dev");
        requestPasswordReset("reset-used@launchstack.dev");

        User user = userRepository.findByEmail("reset-used@launchstack.dev").orElseThrow();
        PasswordResetToken resetToken = latestResetToken(user.getId());
        resetToken.markUsed(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", resetToken.getToken(),
                                "newPassword", "NewPassword123!"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Password reset token has already been used."));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void meReturnsCurrentUserWhenAuthenticated() throws Exception {
        verifyUser("me@launchstack.dev");
        String accessToken = loginAndGetAccessToken("me@launchstack.dev", "Password123!");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me@launchstack.dev"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));
    }

    @Test
    void validationErrorsFollowStandardStructure() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors[0].field").isString())
                .andExpect(jsonPath("$.errors[0].message").isString());
    }

    private void registerUser(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email))))
                .andExpect(status().isCreated());
    }

    private void verifyUser(String email) throws Exception {
        registerUser(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        EmailVerificationToken token = latestVerificationToken(user.getId());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token.getToken()))))
                .andExpect(status().isOk());
    }

    private void requestPasswordReset(String email) throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk());
    }

    private String loginAndGetRefreshToken(String email) throws Exception {
        verifyUser(email);
        JsonNode data = loginAndGetTokenData(email, "Password123!");
        return data.path("refreshToken").asText();
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        JsonNode data = loginAndGetTokenData(email, password);
        return data.path("accessToken").asText();
    }

    private JsonNode loginAndGetTokenData(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private EmailVerificationToken latestVerificationToken(Long userId) {
        return emailVerificationTokenRepository.findAllByUserId(userId).stream()
                .max(Comparator.comparing(EmailVerificationToken::getCreatedAt))
                .orElseThrow();
    }

    private PasswordResetToken latestResetToken(Long userId) {
        return passwordResetTokenRepository.findAllByUserId(userId).stream()
                .max(Comparator.comparing(PasswordResetToken::getCreatedAt))
                .orElseThrow();
    }

    private Map<String, String> registerPayload(String email) {
        return Map.of(
                "email", email,
                "password", "Password123!",
                "firstName", "Jane",
                "lastName", "Doe");
    }
}
