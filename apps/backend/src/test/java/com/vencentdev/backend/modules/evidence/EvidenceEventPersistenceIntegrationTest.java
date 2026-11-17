package com.vencentdev.backend.modules.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vencentdev.backend.IntegrationTestBase;
import com.vencentdev.backend.modules.evidence.entity.EvidenceEvent;
import com.vencentdev.backend.modules.evidence.repository.EvidenceEventRepository;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class EvidenceEventPersistenceIntegrationTest extends IntegrationTestBase {

  private static final String EVENT_HASH =
      "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

  @Autowired private EvidenceEventRepository events;
  @Autowired private ProjectRepository projects;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void persistsJsonPayloadWithDatabaseSequenceAndRejectsMutation() {
    Project project = createProject();
    UUID eventId = UUID.randomUUID();
    EvidenceEvent event =
        EvidenceEvent.builder()
            .id(eventId)
            .eventType("document.uploaded")
            .eventVersion(1)
            .occurredAt(Instant.now())
            .recordedAt(Instant.now())
            .timezone("Asia/Manila")
            .organizationId(project.getOrganizationId())
            .workspaceId(project.getWorkspaceId())
            .projectId(project.getId())
            .actorType("ACCOUNT")
            .actorUserId(project.getOwnerUserId())
            .actorRole("USER")
            .source("USER")
            .eventHash(EVENT_HASH)
            .payload(java.util.Map.of("sizeBytes", 128))
            .build();

    events.saveAndFlush(event);

    EvidenceEvent persisted = events.findById(eventId).orElseThrow();
    assertThat(persisted.getSequenceNumber()).isPositive();
    assertThat(persisted.getPayload()).containsEntry("sizeBytes", 128);
    assertThat(
            events.findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(
                project.getId(), project.getOrganizationId()))
        .extracting(EvidenceEvent::getId)
        .containsExactly(eventId);

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "UPDATE evidence_events SET reason_code = 'tampered' WHERE id = ?", eventId))
        .hasMessageContaining("evidence events are append-only");
    assertThatThrownBy(
            () -> jdbcTemplate.update("DELETE FROM evidence_events WHERE id = ?", eventId))
        .hasMessageContaining("evidence events are append-only");
  }

  private Project createProject() {
    UUID userId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    UUID workspaceId = UUID.randomUUID();
    String unique = "evidence-" + userId;

    jdbcTemplate.update(
        """
        INSERT INTO users (id, external_id, email, display_name, role, user_type,
                           identity_level, contact_verification_status)
        VALUES (?, ?, ?, ?, 'USER', 'INDIVIDUAL', 'I0', 'UNVERIFIED')
        """,
        userId,
        unique,
        unique + "@example.com",
        "Evidence Test User");
    jdbcTemplate.update(
        "INSERT INTO organizations (id, name, owner_user_id) VALUES (?, ?, ?)",
        organizationId,
        "Evidence Org " + unique,
        userId);
    jdbcTemplate.update(
        "INSERT INTO workspaces (id, organization_id, name, default_workspace) VALUES (?, ?, ?, TRUE)",
        workspaceId,
        organizationId,
        "Default " + unique);

    return projects.saveAndFlush(
        Project.builder()
            .organizationId(organizationId)
            .workspaceId(workspaceId)
            .ownerUserId(userId)
            .name("Evidence Project " + unique)
            .build());
  }
}
