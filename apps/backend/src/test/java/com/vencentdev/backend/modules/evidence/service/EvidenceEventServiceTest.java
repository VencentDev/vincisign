package com.vencentdev.backend.modules.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionRole;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import com.vencentdev.backend.modules.evidence.entity.EvidenceEvent;
import com.vencentdev.backend.modules.evidence.repository.EvidenceEventRepository;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EvidenceEventServiceTest {

  @Mock private EvidenceEventRepository events;
  @Mock private ProjectRepository projects;
  @Mock private UserService users;

  private EvidenceEventServiceImpl service;
  private AuthenticatedUser principal;
  private UUID userId;
  private Project project;
  private Document document;
  private DocumentVersion version;

  @BeforeEach
  void setUp() {
    service = new EvidenceEventServiceImpl(events, projects, users, new ObjectMapper());
    userId = UUID.randomUUID();
    principal = new AuthenticatedUser("external-owner", "owner@example.com", "Owner", Set.of("USER"));
    project =
        Project.builder()
            .id(UUID.randomUUID())
            .organizationId(UUID.randomUUID())
            .workspaceId(UUID.randomUUID())
            .ownerUserId(userId)
            .name("Evidence project")
            .build();
    document =
        Document.builder()
            .id(UUID.randomUUID())
            .projectId(project.getId())
            .organizationId(project.getOrganizationId())
            .name("signed.pdf")
            .originalFileName("signed.pdf")
            .originalMediaType("application/pdf")
            .createdByUserId(userId)
            .build();
    version =
        DocumentVersion.builder()
            .id(UUID.randomUUID())
            .documentId(document.getId())
            .organizationId(project.getOrganizationId())
            .versionNumber(1)
            .role(DocumentVersionRole.ORIGINAL)
            .status(DocumentVersionStatus.READY)
            .originalFileName("signed.pdf")
            .mediaType("application/pdf")
            .sizeBytes(128)
            .fingerprint(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
            .build();
  }

  @AfterEach
  void clearTrace() {
    MDC.clear();
  }

  @Test
  void recordsHashedEventWithActorTraceAndPayload() {
    MDC.put("traceId", "request-123");

    service.recordDocumentEvent(
        "document.fingerprinted",
        project,
        userId,
        principal,
        document,
        version,
        java.util.Map.of("algorithm", "SHA-256", "fingerprint", version.getFingerprint()));

    ArgumentCaptor<EvidenceEvent> captor = ArgumentCaptor.forClass(EvidenceEvent.class);
    verify(events).save(captor.capture());
    EvidenceEvent event = captor.getValue();

    assertThat(event.getEventType()).isEqualTo("document.fingerprinted");
    assertThat(event.getProjectId()).isEqualTo(project.getId());
    assertThat(event.getDocumentId()).isEqualTo(document.getId());
    assertThat(event.getDocumentVersionId()).isEqualTo(version.getId());
    assertThat(event.getActorUserId()).isEqualTo(userId);
    assertThat(event.getRequestId()).isEqualTo("request-123");
    assertThat(event.getCorrelationId()).isEqualTo("request-123");
    assertThat(event.getPayload()).containsEntry("algorithm", "SHA-256");
    assertThat(event.getEventHash()).matches("[0-9a-f]{64}");
  }

  @Test
  void listsEventsOnlyForProjectOwner() {
    when(users.findExistingInternalId(principal)).thenReturn(Optional.of(userId));
    when(projects.findById(project.getId())).thenReturn(Optional.of(project));
    EvidenceEvent event =
        EvidenceEvent.builder()
            .id(UUID.randomUUID())
            .sequenceNumber(1L)
            .eventType("document.uploaded")
            .eventVersion(1)
            .occurredAt(java.time.Instant.now())
            .recordedAt(java.time.Instant.now())
            .timezone("Asia/Manila")
            .organizationId(project.getOrganizationId())
            .workspaceId(project.getWorkspaceId())
            .projectId(project.getId())
            .actorType("ACCOUNT")
            .actorUserId(userId)
            .source("USER")
            .eventHash("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef")
            .payload(java.util.Map.of())
            .build();
    when(events.findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(
            project.getId(), project.getOrganizationId()))
        .thenReturn(List.of(event));

    assertThat(service.listProjectEvents(project.getId(), principal))
        .singleElement()
        .satisfies(response -> assertThat(response.sequenceNumber()).isEqualTo(1L));
  }

  @Test
  void rejectsEventReadsForNonOwner() {
    UUID differentUserId = UUID.randomUUID();
    when(users.findExistingInternalId(principal)).thenReturn(Optional.of(differentUserId));
    when(projects.findById(project.getId())).thenReturn(Optional.of(project));

    assertThatThrownBy(() -> service.listProjectEvents(project.getId(), principal))
        .isInstanceOf(ForbiddenException.class);

    verify(events, never())
        .findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(any(), any());
  }
}
