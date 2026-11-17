package com.vencentdev.backend.modules.evidence.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EvidenceEventResponse(
    UUID id,
    Long sequenceNumber,
    String eventType,
    int eventVersion,
    Instant occurredAt,
    Instant recordedAt,
    String timezone,
    UUID organizationId,
    UUID workspaceId,
    UUID projectId,
    UUID documentId,
    UUID documentVersionId,
    UUID participantId,
    String actorType,
    UUID actorUserId,
    String actorRole,
    String identityLevel,
    String source,
    String requestId,
    String correlationId,
    String idempotencyKey,
    Long aggregateVersion,
    String policyId,
    String policyVersion,
    String reasonCode,
    String deviceContextRef,
    String previousEventHash,
    String eventHash,
    Map<String, Object> payload) {}
