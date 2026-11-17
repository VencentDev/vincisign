package com.vencentdev.backend.modules.evidence.service;

import com.vencentdev.backend.common.exception.ForbiddenException;
import com.vencentdev.backend.common.exception.ResourceNotFoundException;
import com.vencentdev.backend.common.web.RequestIdFilter;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.document.entity.Document;
import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.evidence.dto.EvidenceEventResponse;
import com.vencentdev.backend.modules.evidence.entity.EvidenceEvent;
import com.vencentdev.backend.modules.evidence.repository.EvidenceEventRepository;
import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.repository.ProjectRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class EvidenceEventServiceImpl implements EvidenceEventService {

  private static final String ACTOR_TYPE_ACCOUNT = "ACCOUNT";
  private static final String SOURCE_USER = "USER";
  private static final String TIMEZONE = "Asia/Manila";
  private static final String EVENT_HASH_ALGORITHM = "SHA-256";
  private static final HexFormat HEX_FORMAT = HexFormat.of();

  private final EvidenceEventRepository events;
  private final ProjectRepository projects;
  private final UserService users;
  private final ObjectMapper objectMapper;

  public EvidenceEventServiceImpl(
      EvidenceEventRepository events,
      ProjectRepository projects,
      UserService users,
      ObjectMapper objectMapper) {
    this.events = events;
    this.projects = projects;
    this.users = users;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public void recordProjectEvent(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Map<String, Object> payload) {
    events.save(newEvent(eventType, project, actorUserId, principal, null, null, null, payload));
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordProjectEventInNewTransaction(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      String reasonCode,
      Map<String, Object> payload) {
    events.save(
        newEvent(eventType, project, actorUserId, principal, null, null, reasonCode, payload));
  }

  @Override
  @Transactional
  public void recordDocumentEvent(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Document document,
      DocumentVersion version,
      Map<String, Object> payload) {
    events.save(
        newEvent(
            eventType, project, actorUserId, principal, document, version, null, payload));
  }

  @Override
  @Transactional(readOnly = true)
  public List<EvidenceEventResponse> listProjectEvents(
      UUID projectId, AuthenticatedUser principal) {
    UUID actorUserId =
        users
            .findExistingInternalId(principal)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Project project =
        projects
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    if (!actorUserId.equals(project.getOwnerUserId())) {
      throw new ForbiddenException("You do not have access to this project.");
    }

    return events.findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(
            projectId, project.getOrganizationId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private EvidenceEvent newEvent(
      String eventType,
      Project project,
      UUID actorUserId,
      AuthenticatedUser principal,
      Document document,
      DocumentVersion version,
      String reasonCode,
      Map<String, Object> payload) {
    UUID eventId = UUID.randomUUID();
    Instant occurredAt = Instant.now();
    Instant recordedAt = Instant.now();
    String requestId = MDC.get(RequestIdFilter.MDC_TRACE_ID_KEY);
    String actorRole =
        principal.roles() == null
            ? "USER"
            : principal.roles().stream().sorted().findFirst().orElse("USER");
    String actorType = ACTOR_TYPE_ACCOUNT;
    String source = SOURCE_USER;
    Map<String, Object> payloadValue =
        payload == null || payload.isEmpty() ? Map.of() : new TreeMap<>(payload);
    String canonicalPayload = objectMapper.writeValueAsString(payloadValue);
    String eventHash =
        hash(
            eventId,
            eventType,
            occurredAt,
            recordedAt,
            project,
            document,
            version,
            actorUserId,
            actorType,
            actorRole,
            source,
            requestId,
            requestId,
            project.getAggregateVersion(),
            project.getPolicyId(),
            project.getPolicyVersion(),
            reasonCode,
            canonicalPayload);

    return EvidenceEvent.builder()
        .id(eventId)
        .eventType(eventType)
        .eventVersion(1)
        .occurredAt(occurredAt)
        .recordedAt(recordedAt)
        .timezone(TIMEZONE)
        .organizationId(project.getOrganizationId())
        .workspaceId(project.getWorkspaceId())
        .projectId(project.getId())
        .documentId(document == null ? null : document.getId())
        .documentVersionId(version == null ? null : version.getId())
        .actorType(actorType)
        .actorUserId(actorUserId)
        .actorRole(actorRole)
        .source(source)
        .requestId(requestId)
        .correlationId(requestId)
        .aggregateVersion(project.getAggregateVersion())
        .policyId(project.getPolicyId())
        .policyVersion(project.getPolicyVersion())
        .reasonCode(reasonCode)
        .eventHash(eventHash)
        .payload(payloadValue)
        .build();
  }

  private String hash(
      UUID eventId,
      String eventType,
      Instant occurredAt,
      Instant recordedAt,
      Project project,
      Document document,
      DocumentVersion version,
      UUID actorUserId,
      String actorType,
      String actorRole,
      String source,
      String requestId,
      String correlationId,
      long aggregateVersion,
      String policyId,
      String policyVersion,
      String reasonCode,
      String canonicalPayload) {
    String canonical =
        String.join(
            "|",
            eventId.toString(),
            eventType,
            "1",
            occurredAt.toString(),
            recordedAt.toString(),
            TIMEZONE,
            value(project.getOrganizationId()),
            value(project.getWorkspaceId()),
            value(project.getId()),
            document == null ? "" : document.getId().toString(),
            version == null ? "" : version.getId().toString(),
            actorType,
            actorUserId.toString(),
            value(actorRole),
            source,
            value(requestId),
            value(correlationId),
            Long.toString(aggregateVersion),
            value(policyId),
            value(policyVersion),
            value(reasonCode),
            canonicalPayload);
    try {
      return HEX_FORMAT.formatHex(
          MessageDigest.getInstance(EVENT_HASH_ALGORITHM)
              .digest(canonical.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is not available in the current Java runtime.", exception);
    }
  }

  private String value(String value) {
    return value == null ? "" : value;
  }

  private String value(UUID value) {
    return value == null ? "" : value.toString();
  }

  private EvidenceEventResponse toResponse(EvidenceEvent event) {
    return new EvidenceEventResponse(
        event.getId(),
        event.getSequenceNumber(),
        event.getEventType(),
        event.getEventVersion(),
        event.getOccurredAt(),
        event.getRecordedAt(),
        event.getTimezone(),
        event.getOrganizationId(),
        event.getWorkspaceId(),
        event.getProjectId(),
        event.getDocumentId(),
        event.getDocumentVersionId(),
        event.getParticipantId(),
        event.getActorType(),
        event.getActorUserId(),
        event.getActorRole(),
        event.getIdentityLevel(),
        event.getSource(),
        event.getRequestId(),
        event.getCorrelationId(),
        event.getIdempotencyKey(),
        event.getAggregateVersion(),
        event.getPolicyId(),
        event.getPolicyVersion(),
        event.getReasonCode(),
        event.getDeviceContextRef(),
        event.getPreviousEventHash(),
        event.getEventHash(),
        event.getPayload());
  }
}
