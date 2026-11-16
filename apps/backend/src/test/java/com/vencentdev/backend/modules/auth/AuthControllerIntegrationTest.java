package com.vencentdev.backend.modules.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vencentdev.backend.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;

  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void meRejectsUnauthenticatedRequests() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsCurrentUserFromJwt() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/auth/me")
                .with(jwt().jwt(token -> token.subject("user-1").claim("email", "u@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("u@example.com"));
  }

  @Test
  void signupCreatesLocalAccountAndIssuesSessionToken() throws Exception {
    String email = "manual-" + java.util.UUID.randomUUID() + "@example.com";

    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"correct horse battery staple","displayName":"Manual User"}
                        """
                            .formatted(email)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.role").value("USER"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String accessToken = objectMapper.readTree(response).get("accessToken").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));

    Integer passwordHashCount =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM users WHERE email = ? AND password_hash IS NOT NULL",
            Integer.class,
            email);
    org.assertj.core.api.Assertions.assertThat(passwordHashCount).isEqualTo(1);
  }

  @Test
  void loginAcceptsCorrectPasswordAndRejectsIncorrectPassword() throws Exception {
    String email = "login-" + java.util.UUID.randomUUID() + "@example.com";
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s","password":"correct horse battery staple","displayName":"Login User"}
                    """
                        .formatted(email)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s","password":"wrong password"}
                    """
                        .formatted(email)))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s","password":"correct horse battery staple"}
                    """
                        .formatted(email)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.user.email").value(email));
  }

  @Test
  void signupRejectsDuplicateEmail() throws Exception {
    String email = "duplicate-" + java.util.UUID.randomUUID() + "@example.com";
    String body =
        """
        {"email":"%s","password":"correct horse battery staple","displayName":"Duplicate User"}
        """
            .formatted(email);

    mockMvc
        .perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
    mockMvc
        .perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
  }

  @Test
  void adminEndpointRejectsUserRole() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/ping")
                .with(
                    jwt()
                        .jwt(token -> token.subject("user-1").claim("email", "u@example.com"))
                        .authorities(() -> "ROLE_USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminEndpointAcceptsAdminRole() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/admin/ping")
                .with(
                    jwt()
                        .jwt(token -> token.subject("admin-1").claim("email", "a@example.com"))
                        .authorities(() -> "ROLE_ADMIN")))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));
  }
}
