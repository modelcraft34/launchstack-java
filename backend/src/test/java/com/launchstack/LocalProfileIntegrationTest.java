package com.launchstack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchstack.auth.token.RefreshTokenRepository;
import com.launchstack.user.entity.User;
import com.launchstack.user.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "SEED_ADMIN_EMAIL=admin@launchstack.local",
        "SEED_ADMIN_PASSWORD=Password123!"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalProfileIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.findByEmail("local-user@launchstack.dev").ifPresent(userRepository::delete);
    }

    @Test
    void h2ConsoleIsAvailableInLocalProfile() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/h2-console", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("SAMEORIGIN");
        assertThat(response.getBody()).contains("H2 Console");
    }

    @Test
    void localProfileCanExerciseAuthEndpointsAgainstH2() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "local-user@launchstack.dev",
                                "password", "Password123!",
                                "firstName", "Local",
                                "lastName", "Tester"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode registerBody = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String refreshToken = registerBody.path("data").path("refreshToken").asText();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "local-user@launchstack.dev",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("local-user@launchstack.dev"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token has been revoked."));
    }

    @Test
    void seedAdminCredentialsWorkWithLocalProfile() {
        User seededAdmin = userRepository.findByEmail("admin@launchstack.local").orElseGet(() -> {
            throw new AssertionError("Expected local profile admin seed user to exist");
        });

        assertThat(seededAdmin.isEnabled()).isTrue();
        assertThat(seededAdmin.getRoles())
                .extracting(role -> role.getName())
                .contains("ROLE_ADMIN", "ROLE_USER");
    }
}
