package com.vencentdev.backend.modules.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vencentdev.backend.IntegrationTestBase;
import com.vencentdev.backend.common.exception.ProjectTransitionException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.evidence.entity.EvidenceEvent;
import com.vencentdev.backend.modules.evidence.repository.EvidenceEventRepository;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionRequest;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionResponse;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.project.service.ProjectTransitionService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

class ProjectTransitionIntegrationTest extends IntegrationTestBase {

  @Autowired private ProjectRepository projects;
  @Autowired private EvidenceEventRepository evidenceEvents;
  @Autowired private ProjectTransitionService transitions;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private MockMvc mockMvc;

  @Test
  void advancesProjectVersionAndPersistsTransitionEvidence() {
    Aggregate aggregate = createAggregate("prepare");

    ProjectTransitionResponse response =
        transitions.transition(
            aggregate.project().getId(),
            aggregate.principal(),
            new ProjectTransitionRequest("prepare", 0L, "SENDER_CONFIGURATION_COMPLETE"));

    assertThat(response.previousStatus()).isEqualTo(ProjectStatus.DOCUMENT_UPLOADED);
    assertThat(response.status()).isEqualTo(ProjectStatus.DOCUMENT_PREPARED);
    assertThat(response.aggregateVersion()).isEqualTo(1L);
    Project saved = projects.findById(aggregate.project().getId()).orElseThrow();
    assertThat(saved.getStatus()).isEqualTo(ProjectStatus.DOCUMENT_PREPARED);
    assertThat(saved.getAggregateVersion()).isEqualTo(1L);

    List<EvidenceEvent> events =
        evidenceEvents.findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(
            saved.getId(), saved.getOrganizationId());
    assertThat(events).hasSize(1);
    assertThat(events.getFirst().getEventType()).isEqualTo("project.document_prepared");
    assertThat(events.getFirst().getReasonCode()).isEqualTo("SENDER_CONFIGURATION_COMPLETE");
    assertThat(events.getFirst().getAggregateVersion()).isEqualTo(1L);
    assertThat(events.getFirst().getPayload())
        .containsEntry("previousState", "DOCUMENT_UPLOADED")
        .containsEntry("newState", "DOCUMENT_PREPARED")
        .containsEntry("previousAggregateVersion", 0)
        .containsEntry("aggregateVersion", 1);
  }

  @Test
  void rejectsStaleAndTerminalTransitionsWithoutChangingState() {
    Aggregate aggregate = createAggregate("stale");
    transitions.transition(
        aggregate.project().getId(),
        aggregate.principal(),
        new ProjectTransitionRequest("prepare", 0L, null));

    assertThatThrownBy(
            () ->
                transitions.transition(
                    aggregate.project().getId(),
                    aggregate.principal(),
                    new ProjectTransitionRequest("send", 0L, null)))
        .isInstanceOf(ProjectTransitionException.class)
        .satisfies(
            exception ->
                assertThat(((ProjectTransitionException) exception).getCode())
                    .isEqualTo("PROJECT_VERSION_CONFLICT"));

    Project saved = projects.findById(aggregate.project().getId()).orElseThrow();
    assertThat(saved.getStatus()).isEqualTo(ProjectStatus.DOCUMENT_PREPARED);
    assertThat(saved.getAggregateVersion()).isEqualTo(1L);

    Aggregate terminal = createAggregate("terminal", ProjectStatus.COMPLETED);
    assertThatThrownBy(
            () ->
                transitions.transition(
                    terminal.project().getId(),
                    terminal.principal(),
                    new ProjectTransitionRequest("prepare", 0L, null)))
        .isInstanceOf(ProjectTransitionException.class)
        .satisfies(
            exception ->
                assertThat(((ProjectTransitionException) exception).getCode())
                    .isEqualTo("PROJECT_TRANSITION_NOT_ALLOWED"));
  }

  @Test
  void storesPriorStateForSuspensionAndRestoresItOnResume() {
    Aggregate aggregate = createAggregate("suspend", ProjectStatus.SENT);

    ProjectTransitionResponse suspended =
        transitions.transition(
            aggregate.project().getId(),
            aggregate.principal(),
            new ProjectTransitionRequest("suspend", 0L, "SECURITY_REVIEW"));
    assertThat(suspended.status()).isEqualTo(ProjectStatus.SUSPENDED);

    Project suspendedProject = projects.findById(aggregate.project().getId()).orElseThrow();
    assertThat(suspendedProject.getSuspendedFromStatus()).isEqualTo(ProjectStatus.SENT);

    ProjectTransitionResponse resumed =
        transitions.transition(
            aggregate.project().getId(),
            aggregate.principal(),
            new ProjectTransitionRequest("resume", 1L, null));
    assertThat(resumed.status()).isEqualTo(ProjectStatus.SENT);
    Project resumedProject = projects.findById(aggregate.project().getId()).orElseThrow();
    assertThat(resumedProject.getSuspendedFromStatus()).isNull();
  }

  @Test
  void exposesNamedTransitionEndpoint() throws Exception {
    Aggregate aggregate = createAggregate("endpoint");

    mockMvc
        .perform(
            post("/api/v1/projects/{projectId}/transitions", aggregate.project().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"action\":\"prepare\",\"expectedAggregateVersion\":0,"
                        + "\"reasonCode\":\"SENDER_CONFIGURATION_COMPLETE\"}")
                .with(currentUser(aggregate.principal().subject())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.previousStatus").value("DOCUMENT_UPLOADED"))
        .andExpect(jsonPath("$.status").value("DOCUMENT_PREPARED"))
        .andExpect(jsonPath("$.aggregateVersion").value(1))
        .andExpect(jsonPath("$.evidenceEventIds[0]").isNotEmpty());
  }

  private Aggregate createAggregate(String suffix) {
    return createAggregate(suffix, ProjectStatus.DOCUMENT_UPLOADED);
  }

  private Aggregate createAggregate(String suffix, ProjectStatus status) {
    UUID userId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    UUID workspaceId = UUID.randomUUID();
    String unique = suffix + "-" + userId;

    jdbcTemplate.update(
        """
        INSERT INTO users (id, external_id, email, display_name, role, user_type,
                           identity_level, contact_verification_status)
        VALUES (?, ?, ?, ?, 'USER', 'INDIVIDUAL', 'I0', 'UNVERIFIED')
        """,
        userId,
        unique,
        unique + "@example.com",
        "Transition Test User");
    jdbcTemplate.update(
        "INSERT INTO organizations (id, name, owner_user_id) VALUES (?, ?, ?)",
        organizationId,
        "Transition Org " + unique,
        userId);
    jdbcTemplate.update(
        "INSERT INTO workspaces (id, organization_id, name, default_workspace) VALUES (?, ?, ?, TRUE)",
        workspaceId,
        organizationId,
        "Default " + unique);

    Project project =
        projects.saveAndFlush(
            Project.builder()
                .organizationId(organizationId)
                .workspaceId(workspaceId)
                .ownerUserId(userId)
                .name("Project " + unique)
                .status(status)
                .build());
    return new Aggregate(
        project,
        new AuthenticatedUser(unique, unique + "@example.com", "Transition Test User", Set.of("USER")));
  }

  private record Aggregate(Project project, AuthenticatedUser principal) {}

  private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
          .JwtRequestPostProcessor
      currentUser(String subject) {
    return jwt()
        .jwt(
            token ->
                token.subject(subject)
                    .claim("email", "jwt@example.com")
                    .claim("name", "JWT User"))
        .authorities(() -> "ROLE_USER");
  }
}
