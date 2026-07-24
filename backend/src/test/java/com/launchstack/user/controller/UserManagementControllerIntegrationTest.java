package com.launchstack.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchstack.auth.token.EmailVerificationTokenRepository;
import com.launchstack.auth.token.PasswordResetTokenRepository;
import com.launchstack.auth.token.RefreshTokenRepository;
import com.launchstack.role.Role;
import com.launchstack.role.RoleRepository;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import java.util.Map;
import java.util.Set;
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
class UserManagementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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
        ensureRole("ROLE_USER");
        ensureRole("ROLE_ADMIN");
    }

    @Test
    void unauthenticatedRequestToUsersReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void roleUserRequestToUsersReturns403() throws Exception {
        createUser("member@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String token = loginAndGetAccessToken("member@launchstack.dev", "Password123!");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleAdminRequestToUsersReturnsSuccess() throws Exception {
        createUser("admin@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        createUser("member@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String adminToken = loginAndGetAccessToken("admin@launchstack.dev", "Password123!");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10")
                        .param("email", "launchstack.dev")
                        .param("enabled", "true")
                        .param("role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleUserCanAccessMe() throws Exception {
        createUser("profile@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String token = loginAndGetAccessToken("profile@launchstack.dev", "Password123!");

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile@launchstack.dev"));
    }

    @Test
    void adminGetsUserById() throws Exception {
        User admin = createUser("admin-read@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        User member = createUser("member-read@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(get("/api/users/{id}", member.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void adminGetsMissingUserReturnsNotFound() throws Exception {
        User admin = createUser("admin-missing@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(get("/api/users/{id}", 99999)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User was not found."));
    }

    @Test
    void adminCreatesUserAndHashesPassword() throws Exception {
        User admin = createUser("admin-create@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "created@launchstack.dev",
                                "firstName", "Created",
                                "lastName", "User",
                                "password", "AdminSetPass123!",
                                "roles", Set.of("ROLE_USER"),
                                "enabled", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("created@launchstack.dev"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        User created = userRepository.findByEmail("created@launchstack.dev").orElseThrow();
        assertThat(passwordEncoder.matches("AdminSetPass123!", created.getPasswordHash())).isTrue();
    }

    @Test
    void adminCreateDuplicateEmailFails() throws Exception {
        User admin = createUser("admin-duplicate@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        createUser("duplicate@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "duplicate@launchstack.dev",
                                "firstName", "Dup",
                                "lastName", "User",
                                "password", "AdminSetPass123!",
                                "roles", Set.of("ROLE_USER"),
                                "enabled", true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already registered."));
    }

    @Test
    void adminUpdatesUser() throws Exception {
        User admin = createUser("admin-update@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        User member = createUser("member-update@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(put("/api/users/{id}", member.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Updated",
                                "lastName", "Person",
                                "roles", Set.of("ROLE_ADMIN", "ROLE_USER"),
                                "enabled", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"));

        User updated = userRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getRoles()).extracting(Role::getName).contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void adminUpdatesUserStatus() throws Exception {
        User admin = createUser("admin-status@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        User member = createUser("member-status@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");

        mockMvc.perform(patch("/api/users/{id}/status", member.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "enabled", false,
                                "accountNonLocked", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.accountNonLocked").value(false));
    }

    @Test
    void normalUserCannotUpdateAnotherUser() throws Exception {
        User actor = createUser("actor@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        User target = createUser("target@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String userToken = loginAndGetAccessToken(actor.getEmail(), "Password123!");

        mockMvc.perform(put("/api/users/{id}", target.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Blocked",
                                "lastName", "Attempt",
                                "roles", Set.of("ROLE_USER"),
                                "enabled", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListRolesAndUserCannot() throws Exception {
        User admin = createUser("roles-admin@launchstack.dev", "Password123!", Set.of("ROLE_ADMIN", "ROLE_USER"));
        User user = createUser("roles-user@launchstack.dev", "Password123!", Set.of("ROLE_USER"));

        String adminToken = loginAndGetAccessToken(admin.getEmail(), "Password123!");
        String userToken = loginAndGetAccessToken(user.getEmail(), "Password123!");

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").exists());

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void authMeEndpointStillWorks() throws Exception {
        createUser("auth-me@launchstack.dev", "Password123!", Set.of("ROLE_USER"));
        String token = loginAndGetAccessToken("auth-me@launchstack.dev", "Password123!");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("auth-me@launchstack.dev"));
    }

    @Test
    void accountLifecycleEndpointsRemainPublic() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "invalid"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "missing@launchstack.dev"))))
                .andExpect(status().isOk());
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private User createUser(String email, String password, Set<String> roles) {
        User user = new User(email, passwordEncoder.encode(password), "Test", "User");
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        roles.stream().map(this::ensureRole).forEach(role -> user.getRoles().add(role));
        return userRepository.save(user);
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }
}
