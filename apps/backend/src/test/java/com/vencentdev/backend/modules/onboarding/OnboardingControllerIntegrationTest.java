package com.vencentdev.backend.modules.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vencentdev.backend.IntegrationTestBase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

class OnboardingControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void newAccountMustCompleteOnboardingConsents() throws Exception {
    String email = "onboarding-status-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(get("/api/v1/onboarding/status").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.blockedCode").value("CONSENT_REQUIRED"))
        .andExpect(jsonPath("$.userType").value("INDIVIDUAL"))
        .andExpect(jsonPath("$.requiredConsents").isArray());
  }

  @Test
  void incompleteAccountCannotUseProtectedBusinessEndpoints() throws Exception {
    String email = "onboarding-gate-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(get("/api/v1/todos").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/api/v1/organizations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Blocked Workspace\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void completingOnboardingStoresAuditableConsentsAndAccountType() throws Exception {
    String email = "onboarding-complete-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(
            post("/api/v1/onboarding/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionBody("ORGANIZATION", true)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(true))
        .andExpect(jsonPath("$.blockedCode").doesNotExist())
        .andExpect(jsonPath("$.userType").value("ORGANIZATION"));

    Integer consentCount =
        jdbcTemplate.queryForObject(
            """
            SELECT count(*)
            FROM consent_records record
            JOIN users user_account ON user_account.id = record.user_id
            WHERE user_account.email = ? AND record.status = 'ACCEPTED'
            """,
            Integer.class,
            email);
    org.assertj.core.api.Assertions.assertThat(consentCount).isEqualTo(4);

    mockMvc
        .perform(get("/api/v1/accounts/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userType").value("ORGANIZATION"));
  }

  @Test
  void declinedConsentIsRecordedAndPreventsCompletion() throws Exception {
    String email = "onboarding-declined-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(
            post("/api/v1/onboarding/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"userType":"INDIVIDUAL","consents":[
                      {"type":"TERMS_OF_SERVICE","accepted":false},
                      {"type":"PRIVACY_NOTICE","accepted":true},
                      {"type":"ELECTRONIC_RECORDS","accepted":true},
                      {"type":"PLATFORM_LIMITS_ACKNOWLEDGEMENT","accepted":true}
                    ]}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.blockedCode").value("CONSENT_DECLINED"));

    Integer declinedCount =
        jdbcTemplate.queryForObject(
            """
            SELECT count(*)
            FROM consent_records record
            JOIN users user_account ON user_account.id = record.user_id
            WHERE user_account.email = ? AND record.status = 'DECLINED'
            """,
            Integer.class,
            email);
    org.assertj.core.api.Assertions.assertThat(declinedCount).isEqualTo(1);
  }

  @Test
  void withdrawingAcceptedConsentReopensOnboarding() throws Exception {
    String email = "onboarding-withdraw-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(
            post("/api/v1/onboarding/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(completionBody("INDIVIDUAL", true)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(true));

    mockMvc
        .perform(
            post("/api/v1/onboarding/consents/PRIVACY_NOTICE/withdraw")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(false))
        .andExpect(jsonPath("$.blockedCode").value("CONSENT_DECLINED"))
        .andExpect(jsonPath("$.requiredConsents[1].status").value("WITHDRAWN"));
  }

  @Test
  void decliningConsentCannotBeWithdrawnAsAccepted() throws Exception {
    String email = "withdraw-declined-" + UUID.randomUUID() + "@example.com";
    String token = signup(email);

    mockMvc
        .perform(
            post("/api/v1/onboarding/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"userType":"INDIVIDUAL","consents":[
                      {"type":"TERMS_OF_SERVICE","accepted":false}
                    ]}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.blockedCode").value("CONSENT_DECLINED"));

    mockMvc
        .perform(
            post("/api/v1/onboarding/consents/TERMS_OF_SERVICE/withdraw")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest());
  }

  private String signup(String email) throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"correct horse battery staple","displayName":"Onboarding User"}
                        """
                            .formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("accessToken").asText();
  }

  private String completionBody(String userType, boolean accepted) {
    return """
        {"userType":"%s","consents":[
          {"type":"TERMS_OF_SERVICE","accepted":%s},
          {"type":"PRIVACY_NOTICE","accepted":%s},
          {"type":"ELECTRONIC_RECORDS","accepted":%s},
          {"type":"PLATFORM_LIMITS_ACKNOWLEDGEMENT","accepted":%s}
        ]}
        """
        .formatted(userType, accepted, accepted, accepted, accepted);
  }
}
