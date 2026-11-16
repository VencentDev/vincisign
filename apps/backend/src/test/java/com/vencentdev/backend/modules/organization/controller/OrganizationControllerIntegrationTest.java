package com.vencentdev.backend.modules.organization.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vencentdev.backend.IntegrationTestBase;
import com.vencentdev.backend.modules.organization.entity.MembershipInvitation;
import com.vencentdev.backend.modules.organization.repository.MembershipInvitationRepository;
import com.vencentdev.backend.modules.organization.repository.OrganizationMembershipRepository;
import com.vencentdev.backend.modules.organization.repository.OrganizationRepository;
import com.vencentdev.backend.modules.organization.repository.WorkspaceMembershipRepository;
import com.vencentdev.backend.modules.organization.repository.WorkspaceRepository;
import com.vencentdev.backend.modules.onboarding.entity.ConsentRecord;
import com.vencentdev.backend.modules.onboarding.enums.ConsentStatus;
import com.vencentdev.backend.modules.onboarding.repository.ConsentRecordRepository;
import com.vencentdev.backend.modules.onboarding.service.OnboardingPolicyCatalog;
import com.vencentdev.backend.modules.user.entity.User;
import com.vencentdev.backend.modules.user.enums.ContactVerificationStatus;
import com.vencentdev.backend.modules.user.enums.IdentityLevel;
import com.vencentdev.backend.modules.user.enums.Role;
import com.vencentdev.backend.modules.user.enums.UserType;
import com.vencentdev.backend.modules.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class OrganizationControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private MockMvc mockMvc;
  @Autowired private OrganizationRepository organizations;
  @Autowired private WorkspaceRepository workspaces;
  @Autowired private OrganizationMembershipRepository organizationMemberships;
  @Autowired private WorkspaceMembershipRepository workspaceMemberships;
  @Autowired private MembershipInvitationRepository invitations;
  @Autowired private UserRepository users;
  @Autowired private ConsentRecordRepository consentRecords;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void cleanOrganizations() {
    invitations.deleteAll();
    workspaceMemberships.deleteAll();
    organizationMemberships.deleteAll();
    workspaces.deleteAll();
    organizations.deleteAll();
  }

  @Test
  void createOrganizationCreatesDefaultWorkspaceAndOwnerMembership() throws Exception {
    createOrganization("organization-owner", "Acme PH");

    mockMvc
        .perform(get("/api/v1/organizations").with(currentUser("organization-owner")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Acme PH"))
        .andExpect(jsonPath("$[0].jurisdictionProfile").value("PH"))
        .andExpect(jsonPath("$[0].workspaces.length()").value(1))
        .andExpect(jsonPath("$[0].workspaces[0].name").value("Default"))
        .andExpect(jsonPath("$[0].workspaces[0].defaultWorkspace").value(true));
  }

  @Test
  void organizationDetailsAreHiddenFromNonMembers() throws Exception {
    JsonNode organization = createOrganization("organization-owner", "Private Org");

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organization.get("id").asText())
                .with(currentUser("different-account")))
        .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanCreateAndReadWorkspaces() throws Exception {
    JsonNode organization = createOrganization("workspace-owner", "Workspace Org");
    String organizationId = organization.get("id").asText();

    MvcResult createWorkspace =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/workspaces")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Legal\"}")
                    .with(currentUser("workspace-owner")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Legal"))
            .andExpect(jsonPath("$.defaultWorkspace").value(false))
            .andReturn();
    String workspaceId =
        objectMapper
            .readTree(createWorkspace.getResponse().getContentAsString())
            .get("id")
            .asText();

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId + "/workspaces")
                .with(currentUser("workspace-owner")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId + "/workspaces/" + workspaceId)
                .with(currentUser("workspace-owner")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Legal"));
  }

  @Test
  void nonMemberCannotListOrCreateWorkspaces() throws Exception {
    JsonNode organization = createOrganization("workspace-owner-2", "Scoped Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId + "/workspaces")
                .with(currentUser("non-member")))
        .andExpect(status().isNotFound());
    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Unauthorized\"}")
                .with(currentUser("non-member")))
        .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanAddExistingAccountToOrganizationAndDefaultWorkspace() throws Exception {
    JsonNode organization = createOrganization("membership-owner", "Membership Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").with(currentUser("membership-member")))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"membership-member@example.com\"}")
                .with(currentUser("membership-owner")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("membership-member@example.com"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId).with(currentUser("membership-member")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workspaces.length()").value(1));
  }

  @Test
  void ownerCanGrantExistingMemberAccessToAnotherWorkspace() throws Exception {
    JsonNode organization = createOrganization("workspace-owner-3", "Membership Workspace Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").with(currentUser("workspace-member")))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"workspace-member@example.com\"}")
                .with(currentUser("workspace-owner-3")))
        .andExpect(status().isCreated());

    MvcResult createWorkspace =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/workspaces")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Review\"}")
                    .with(currentUser("workspace-owner-3")))
            .andExpect(status().isCreated())
            .andReturn();
    String workspaceId =
        objectMapper
            .readTree(createWorkspace.getResponse().getContentAsString())
            .get("id")
            .asText();

    mockMvc
        .perform(
            post("/api/v1/organizations/"
                    + organizationId
                    + "/workspaces/"
                    + workspaceId
                    + "/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"workspace-member@example.com\"}")
                .with(currentUser("workspace-owner-3")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.workspaceId").value(workspaceId));

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId + "/workspaces/" + workspaceId)
                .with(currentUser("workspace-member")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Review"));
  }

  @Test
  void workspaceIdsCannotBeUsedAcrossOrganizations() throws Exception {
    JsonNode firstOrganization = createOrganization("first-owner", "First Org");
    JsonNode secondOrganization = createOrganization("second-owner", "Second Org");
    String firstOrganizationId = firstOrganization.get("id").asText();
    String secondOrganizationId = secondOrganization.get("id").asText();
    String firstWorkspaceId = firstOrganization.get("workspaces").get(0).get("id").asText();

    mockMvc
        .perform(
            get("/api/v1/organizations/" + secondOrganizationId + "/workspaces/" + firstWorkspaceId)
                .with(currentUser("second-owner")))
        .andExpect(status().isNotFound());
  }

  @Test
  void duplicateWorkspaceNamesAreRejectedWithinAnOrganization() throws Exception {
    JsonNode organization = createOrganization("duplicate-owner", "Duplicate Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Legal\"}")
                .with(currentUser("duplicate-owner")))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/workspaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" legal \"}")
                .with(currentUser("duplicate-owner")))
        .andExpect(status().isConflict());
  }

  @Test
  void nonOwnerCannotAddOrganizationMembers() throws Exception {
    JsonNode organization = createOrganization("member-owner", "Member Org");
    String organizationId = organization.get("id").asText();
    mockMvc
        .perform(get("/api/v1/auth/me").with(currentUser("member-account")))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"member-account@example.com\"}")
                .with(currentUser("member-account")))
        .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanInviteAccountAndAccountCanAcceptInvitation() throws Exception {
    JsonNode organization = createOrganization("invitation-owner", "Invitation Org");
    String organizationId = organization.get("id").asText();

    MvcResult invitationResult =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/invitations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"invited-account@example.com\","
                            + "\"role\":\"ORGANIZATION_MEMBER\"}")
                    .with(currentUser("invitation-owner")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.role").value("ORGANIZATION_MEMBER"))
            .andReturn();
    JsonNode invitation =
        objectMapper.readTree(invitationResult.getResponse().getContentAsString());

    mockMvc
        .perform(
            post("/api/v1/invitations/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + invitation.get("token").asText() + "\"}")
                .with(currentUser("invited-account")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.role").value("ORGANIZATION_MEMBER"));

    mockMvc
        .perform(
            get("/api/v1/organizations/" + organizationId).with(currentUser("invited-account")))
        .andExpect(status().isOk());
  }

  @Test
  void revokedInvitationCannotBeAccepted() throws Exception {
    JsonNode organization = createOrganization("revoke-invitation-owner", "Revoke Invitation Org");
    String organizationId = organization.get("id").asText();

    MvcResult invitationResult =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/invitations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"revoked-invitee@example.com\","
                            + "\"role\":\"ORGANIZATION_MEMBER\"}")
                    .with(currentUser("revoke-invitation-owner")))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode invitation =
        objectMapper.readTree(invitationResult.getResponse().getContentAsString());

    mockMvc
        .perform(
            delete(
                    "/api/v1/organizations/"
                        + organizationId
                        + "/invitations/"
                        + invitation.get("id").asText())
                .with(currentUser("revoke-invitation-owner")))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/invitations/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + invitation.get("token").asText() + "\"}")
                .with(currentUser("revoked-invitee")))
        .andExpect(status().isConflict());
  }

  @Test
  void expiredInvitationCannotBeAccepted() throws Exception {
    JsonNode organization =
        createOrganization("expired-invitation-owner", "Expired Invitation Org");
    String organizationId = organization.get("id").asText();

    MvcResult invitationResult =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/invitations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"expired-invitee@example.com\","
                            + "\"role\":\"ORGANIZATION_MEMBER\"}")
                    .with(currentUser("expired-invitation-owner")))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode invitation =
        objectMapper.readTree(invitationResult.getResponse().getContentAsString());
    MembershipInvitation storedInvitation =
        invitations.findById(UUID.fromString(invitation.get("id").asText())).orElseThrow();
    storedInvitation.setExpiresAt(Instant.now().minusSeconds(1));
    invitations.save(storedInvitation);

    mockMvc
        .perform(
            post("/api/v1/invitations/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + invitation.get("token").asText() + "\"}")
                .with(currentUser("expired-invitee")))
        .andExpect(status().isConflict());
  }

  @Test
  void ownerCanChangeRoleAndRevokeMembership() throws Exception {
    JsonNode organization = createOrganization("role-owner", "Role Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").with(currentUser("role-member")))
        .andExpect(status().isOk());
    MvcResult membershipResult =
        mockMvc
            .perform(
                post("/api/v1/organizations/" + organizationId + "/memberships")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"email\":\"role-member@example.com\","
                            + "\"role\":\"ORGANIZATION_MEMBER\"}")
                    .with(currentUser("role-owner")))
            .andExpect(status().isCreated())
            .andReturn();
    String membershipId =
        objectMapper
            .readTree(membershipResult.getResponse().getContentAsString())
            .get("id")
            .asText();

    mockMvc
        .perform(
            patch("/api/v1/organizations/" + organizationId + "/memberships/" + membershipId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ORGANIZATION_ADMINISTRATOR\"}")
                .with(currentUser("role-owner")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ORGANIZATION_ADMINISTRATOR"));

    mockMvc
        .perform(
            delete("/api/v1/organizations/" + organizationId + "/memberships/" + membershipId)
                .with(currentUser("role-owner")))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(get("/api/v1/organizations/" + organizationId).with(currentUser("role-member")))
        .andExpect(status().isNotFound());
  }

  @Test
  void administratorCanInviteButCannotRevokeOwner() throws Exception {
    JsonNode organization = createOrganization("administrator-owner", "Administrator Org");
    String organizationId = organization.get("id").asText();

    mockMvc
        .perform(get("/api/v1/auth/me").with(currentUser("administrator-account")))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"administrator-account@example.com\","
                        + "\"role\":\"ORGANIZATION_ADMINISTRATOR\"}")
                .with(currentUser("administrator-owner")))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/organizations/" + organizationId + "/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"administrator-invitee@example.com\","
                        + "\"role\":\"ORGANIZATION_MEMBER\"}")
                .with(currentUser("administrator-account")))
        .andExpect(status().isCreated());

    MvcResult membershipsResult =
        mockMvc
            .perform(
                get("/api/v1/organizations/" + organizationId + "/memberships")
                    .with(currentUser("administrator-account")))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode memberships =
        objectMapper.readTree(membershipsResult.getResponse().getContentAsString());
    String ownerMembershipId =
        memberships.get(0).get("role").asText().equals("ORGANIZATION_OWNER")
            ? memberships.get(0).get("id").asText()
            : memberships.get(1).get("id").asText();

    mockMvc
        .perform(
            delete("/api/v1/organizations/" + organizationId + "/memberships/" + ownerMembershipId)
                .with(currentUser("administrator-account")))
        .andExpect(status().isForbidden());
  }

  @Test
  void roleCatalogExposesOrganizationAndProjectPermissions() throws Exception {
    mockMvc
        .perform(get("/api/v1/roles").with(currentUser("role-catalog-user")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.role == 'ORGANIZATION_OWNER')].permissions").isNotEmpty())
        .andExpect(jsonPath("$[?(@.role == 'SIGNER')].permissions").isNotEmpty())
        .andExpect(jsonPath("$[?(@.role == 'NOTARY_SUPERVISOR')].permissions").isNotEmpty());
  }

  @Test
  void unauthenticatedOrganizationRequestsAreRejected() throws Exception {
    mockMvc.perform(get("/api/v1/organizations")).andExpect(status().isUnauthorized());
  }

  private JsonNode createOrganization(String subject, String name) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/organizations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"" + name + "\"}")
                    .with(currentUser(subject)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
          .JwtRequestPostProcessor
      currentUser(String subject) {
    ensureOnboarded(subject);
    return jwt()
        .jwt(
            token ->
                token
                    .subject(subject)
                    .claim("email", subject + "@example.com")
                    .claim("name", subject));
  }

  private void ensureOnboarded(String subject) {
    User account =
        users
            .findByExternalId(subject)
            .orElseGet(
                () ->
                    users.save(
                        User.builder()
                            .externalId(subject)
                            .email(subject + "@example.com")
                            .displayName(subject)
                            .role(Role.USER)
                            .userType(UserType.INDIVIDUAL)
                            .identityLevel(IdentityLevel.I0)
                            .contactVerificationStatus(ContactVerificationStatus.UNVERIFIED)
                            .build()));
    if (account.getOnboardingCompletedAt() != null) {
      return;
    }

    Instant decidedAt = Instant.now();
    OnboardingPolicyCatalog.required()
        .forEach(
            policy ->
                consentRecords.save(
                    ConsentRecord.builder()
                        .userId(account.getId())
                        .consentType(policy.type())
                        .policyId(policy.policyId())
                        .policyVersion(policy.policyVersion())
                        .policyReference(policy.policyReference())
                        .consentText(policy.description())
                        .status(ConsentStatus.ACCEPTED)
                        .decidedAt(decidedAt)
                        .actorUserId(account.getId())
                        .build()));
    account.setOnboardingCompletedAt(decidedAt);
    users.save(account);
  }
}
