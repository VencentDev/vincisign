package com.vencentdev.backend.modules.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ProjectTransitionException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.evidence.service.EvidenceEventService;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionRequest;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionResponse;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectTransitionServiceTest {

  @Mock private ProjectRepository projects;
  @Mock private UserService users;
  @Mock private EvidenceEventService evidenceEvents;

  private ProjectTransitionServiceImpl service;
  private AuthenticatedUser principal;
  private UUID userId;
  private Project project;

  @BeforeEach
  void setUp() {
    service = new ProjectTransitionServiceImpl(projects, users, evidenceEvents);
    userId = UUID.randomUUID();
    principal = new AuthenticatedUser("external-owner", "owner@example.com", "Owner", Set.of("USER"));
    project =
        Project.builder()
            .id(UUID.randomUUID())
            .organizationId(UUID.randomUUID())
            .workspaceId(UUID.randomUUID())
            .ownerUserId(userId)
            .name("Transition project")
            .status(ProjectStatus.DOCUMENT_UPLOADED)
            .aggregateVersion(1)
            .build();
    when(users.resolveInternalId(principal)).thenReturn(userId);
    when(projects.findById(project.getId())).thenReturn(Optional.of(project));
    lenient()
        .when(projects.saveAndFlush(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    lenient()
        .when(evidenceEvents.recordProjectEventWithId(
            any(), any(), any(), any(), any(), any(Map.class)))
        .thenReturn(UUID.randomUUID());
  }

  @Test
  void appliesAllowedTransitionAndRecordsEvidence() {
    ProjectTransitionResponse response =
        service.transition(
            project.getId(),
            principal,
            new ProjectTransitionRequest("prepare", 1L, "SENDER_CONFIGURATION_COMPLETE"));

    assertThat(response.previousStatus()).isEqualTo(ProjectStatus.DOCUMENT_UPLOADED);
    assertThat(response.status()).isEqualTo(ProjectStatus.DOCUMENT_PREPARED);
    assertThat(response.aggregateVersion()).isEqualTo(1);
    assertThat(response.evidenceEventIds()).hasSize(1);
    assertThat(project.getStatus()).isEqualTo(ProjectStatus.DOCUMENT_PREPARED);

    ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
    verify(evidenceEvents)
        .recordProjectEventWithId(
            eq("project.document_prepared"),
            same(project),
            eq(userId),
            same(principal),
            eq("SENDER_CONFIGURATION_COMPLETE"),
            payload.capture());
    assertThat(payload.getValue())
        .containsEntry("previousState", "DOCUMENT_UPLOADED")
        .containsEntry("newState", "DOCUMENT_PREPARED")
        .containsEntry("transitionCommand", "prepare")
        .containsEntry("previousAggregateVersion", 1L);
  }

  @Test
  void rejectsTransitionFromTerminalStateWithStableCode() {
    project.transitionTo(ProjectStatus.COMPLETED, null);

    assertThatThrownBy(
            () ->
                service.transition(
                    project.getId(),
                    principal,
                    new ProjectTransitionRequest("prepare", 1L, null)))
        .isInstanceOf(ProjectTransitionException.class)
        .satisfies(
            exception ->
                assertThat(((ProjectTransitionException) exception).getCode())
                    .isEqualTo("PROJECT_TRANSITION_NOT_ALLOWED"));

    verify(projects, never()).saveAndFlush(any());
    verify(evidenceEvents, never())
        .recordProjectEventWithId(any(), any(), any(), any(), any(), any(Map.class));
  }

  @Test
  void rejectsStaleAggregateVersionBeforeMutation() {
    assertThatThrownBy(
            () ->
                service.transition(
                    project.getId(),
                    principal,
                    new ProjectTransitionRequest("prepare", 0L, null)))
        .isInstanceOf(ProjectTransitionException.class)
        .satisfies(
            exception ->
                assertThat(((ProjectTransitionException) exception).getCode())
                    .isEqualTo("PROJECT_VERSION_CONFLICT"));

    verify(projects, never()).saveAndFlush(any());
  }

  @Test
  void requiresReasonForCancellation() {
    project.transitionTo(ProjectStatus.SENT, null);

    assertThatThrownBy(
            () ->
                service.transition(
                    project.getId(),
                    principal,
                    new ProjectTransitionRequest("cancel", 1L, null)))
        .isInstanceOf(ProjectTransitionException.class)
        .satisfies(
            exception ->
                assertThat(((ProjectTransitionException) exception).getCode())
                    .isEqualTo("PROJECT_TRANSITION_REASON_REQUIRED"));

    verify(projects, never()).saveAndFlush(any());
  }

  @Test
  void rejectsNonOwnerWithoutMutatingProject() {
    UUID differentUserId = UUID.randomUUID();
    when(users.resolveInternalId(principal)).thenReturn(differentUserId);

    assertThatThrownBy(
            () ->
                service.transition(
                    project.getId(),
                    principal,
                    new ProjectTransitionRequest("prepare", 1L, null)))
        .isInstanceOf(ForbiddenException.class);

    verify(projects, never()).saveAndFlush(any());
  }

  @Test
  void suspendsAndResumesTheRecordedPriorState() {
    project.transitionTo(ProjectStatus.SENT, null);
    ProjectTransitionResponse suspended =
        service.transition(
            project.getId(),
            principal,
            new ProjectTransitionRequest("suspend", 1L, "SECURITY_REVIEW"));

    assertThat(suspended.status()).isEqualTo(ProjectStatus.SUSPENDED);
    assertThat(project.getSuspendedFromStatus()).isEqualTo(ProjectStatus.SENT);

    Project resumedProject =
        Project.builder()
            .id(project.getId())
            .organizationId(project.getOrganizationId())
            .workspaceId(project.getWorkspaceId())
            .ownerUserId(userId)
            .name(project.getName())
            .status(ProjectStatus.SUSPENDED)
            .suspendedFromStatus(ProjectStatus.SENT)
            .aggregateVersion(2)
            .build();
    when(projects.findById(project.getId())).thenReturn(Optional.of(resumedProject));

    ProjectTransitionResponse resumed =
        service.transition(
            project.getId(),
            principal,
            new ProjectTransitionRequest("resume", 2L, null));

    assertThat(resumed.status()).isEqualTo(ProjectStatus.SENT);
    assertThat(resumedProject.getSuspendedFromStatus()).isNull();
  }

}
