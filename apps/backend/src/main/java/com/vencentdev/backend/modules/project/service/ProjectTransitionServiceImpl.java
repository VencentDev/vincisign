package com.vencentdev.backend.modules.project.service;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ProjectTransitionException;
import com.vencentdev.backend.common.exception.ResourceNotFoundException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.evidence.service.EvidenceEventService;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionRequest;
import com.vencentdev.backend.modules.project.dto.ProjectTransitionResponse;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import com.vencentdev.backend.modules.project.enums.ProjectTransitionAction;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectTransitionServiceImpl implements ProjectTransitionService {

  private static final List<ProjectTransitionAction> REASON_REQUIRED_ACTIONS =
      List.of(
          ProjectTransitionAction.CANCEL,
          ProjectTransitionAction.DECLINE,
          ProjectTransitionAction.EXPIRE,
          ProjectTransitionAction.REJECT,
          ProjectTransitionAction.SUSPEND,
          ProjectTransitionAction.REVOKE);

  private final ProjectRepository projects;
  private final UserService users;
  private final EvidenceEventService evidenceEvents;

  public ProjectTransitionServiceImpl(
      ProjectRepository projects, UserService users, EvidenceEventService evidenceEvents) {
    this.projects = projects;
    this.users = users;
    this.evidenceEvents = evidenceEvents;
  }

  @Override
  @Transactional
  public ProjectTransitionResponse transition(
      UUID projectId, AuthenticatedUser principal, ProjectTransitionRequest request) {
    UUID actorUserId = users.resolveInternalId(principal);
    Project project =
        projects
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    requireOwner(project, actorUserId);

    ProjectTransitionAction action = ProjectTransitionAction.fromApiValue(request.action());
    if (action == null) {
      throw transitionError(
          "PROJECT_TRANSITION_INVALID_ACTION",
          "The requested project transition action is not supported.");
    }

    return apply(
        project,
        actorUserId,
        principal,
        action,
        request.expectedAggregateVersion(),
        request.reasonCode(),
        null,
        null);
  }

  @Override
  @Transactional
  public ProjectTransitionResponse recordDocumentUploaded(
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Document document,
      DocumentVersion version) {
    return apply(
        project,
        actorUserId,
        principal,
        ProjectTransitionAction.DOCUMENT_UPLOADED,
        project.getAggregateVersion(),
        null,
        document,
        version);
  }

  @Override
  @Transactional(readOnly = true)
  public void assertDocumentUploadAllowed(Project project) {
    if (ProjectTransitionRules.target(
            project.getStatus(),
            ProjectTransitionAction.DOCUMENT_UPLOADED,
            project.getSuspendedFromStatus())
        == null) {
      throw transitionError(
          "PROJECT_TRANSITION_NOT_ALLOWED",
          "The project cannot accept a document in its current state.");
    }
  }

  private ProjectTransitionResponse apply(
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      ProjectTransitionAction action,
      long expectedAggregateVersion,
      String reasonCode,
      Document document,
      DocumentVersion version) {
    if (project.getAggregateVersion() != expectedAggregateVersion) {
      throw transitionError(
          "PROJECT_VERSION_CONFLICT",
          "The project changed before this transition could be applied.");
    }

    ProjectStatus previousStatus = project.getStatus();
    ProjectStatus nextStatus =
        ProjectTransitionRules.target(
            previousStatus, action, project.getSuspendedFromStatus());
    if (nextStatus == null) {
      throw transitionError(
          "PROJECT_TRANSITION_NOT_ALLOWED",
          "The project cannot perform this transition from its current state.");
    }
    validateReason(action, reasonCode);

    String normalizedReasonCode = normalize(reasonCode);
    UUID transitionId = UUID.randomUUID();
    long previousAggregateVersion = project.getAggregateVersion();
    ProjectStatus nextSuspendedFromStatus = project.getSuspendedFromStatus();
    if (action == ProjectTransitionAction.SUSPEND) {
      nextSuspendedFromStatus = previousStatus;
    } else if (previousStatus == ProjectStatus.SUSPENDED || action == ProjectTransitionAction.RESUME) {
      nextSuspendedFromStatus = null;
    }
    project.transitionTo(nextStatus, nextSuspendedFromStatus);

    Project savedProject;
    try {
      savedProject = projects.saveAndFlush(project);
    } catch (OptimisticLockingFailureException exception) {
      throw transitionError(
          "PROJECT_VERSION_CONFLICT",
          "The project changed before this transition could be applied.");
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("transitionId", transitionId.toString());
    payload.put("transitionCommand", action.apiValue());
    payload.put("previousState", previousStatus.name());
    payload.put("newState", nextStatus.name());
    payload.put("previousAggregateVersion", previousAggregateVersion);
    payload.put("aggregateVersion", savedProject.getAggregateVersion());
    if (normalizedReasonCode != null) {
      payload.put("reasonCode", normalizedReasonCode);
    }
    if (action == ProjectTransitionAction.SUSPEND) {
      payload.put("suspendedFromState", previousStatus.name());
    }

    UUID evidenceEventId;
    if (document == null) {
      evidenceEventId =
          evidenceEvents.recordProjectEventWithId(
              action.eventType(),
              savedProject,
              actorUserId,
              principal,
              normalizedReasonCode,
              payload);
    } else {
      evidenceEventId =
          evidenceEvents.recordDocumentEventWithId(
              action.eventType(),
              savedProject,
              actorUserId,
              principal,
              document,
              version,
              normalizedReasonCode,
              payload);
    }

    return new ProjectTransitionResponse(
        transitionId,
        savedProject.getId(),
        previousStatus,
        savedProject.getStatus(),
        savedProject.getAggregateVersion(),
        action.apiValue(),
        normalizedReasonCode,
        List.of(evidenceEventId),
        new ArrayList<>());
  }

  private void requireOwner(Project project, UUID actorUserId) {
    if (!actorUserId.equals(project.getOwnerUserId())) {
      throw new ForbiddenException("You do not have access to this project.");
    }
  }

  private void validateReason(ProjectTransitionAction action, String reasonCode) {
    if (REASON_REQUIRED_ACTIONS.contains(action) && normalize(reasonCode) == null) {
      throw transitionError(
          "PROJECT_TRANSITION_REASON_REQUIRED",
          "A reason code is required for this project transition.");
    }
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private ProjectTransitionException transitionError(String code, String message) {
    HttpStatus status =
        ("PROJECT_TRANSITION_REASON_REQUIRED".equals(code)
                || "PROJECT_TRANSITION_INVALID_ACTION".equals(code))
            ? HttpStatus.BAD_REQUEST
            : HttpStatus.CONFLICT;
    return new ProjectTransitionException(status, code, message);
  }
}
