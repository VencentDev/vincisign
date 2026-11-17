package com.vencentdev.backend.modules.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evidence_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EvidenceEvent {

  @Id
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "sequence_number", nullable = false, insertable = false, updatable = false)
  private Long sequenceNumber;

  @Column(name = "event_type", nullable = false, updatable = false, length = 120)
  private String eventType;

  @Column(name = "event_version", nullable = false, updatable = false)
  private int eventVersion;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt;

  @Column(nullable = false, updatable = false, length = 64)
  private String timezone;

  @Column(name = "organization_id", nullable = false, updatable = false)
  private UUID organizationId;

  @Column(name = "workspace_id", updatable = false)
  private UUID workspaceId;

  @Column(name = "project_id", nullable = false, updatable = false)
  private UUID projectId;

  @Column(name = "document_id", updatable = false)
  private UUID documentId;

  @Column(name = "document_version_id", updatable = false)
  private UUID documentVersionId;

  @Column(name = "participant_id", updatable = false)
  private UUID participantId;

  @Column(name = "actor_type", nullable = false, updatable = false, length = 32)
  private String actorType;

  @Column(name = "actor_user_id", updatable = false)
  private UUID actorUserId;

  @Column(name = "actor_role", updatable = false, length = 64)
  private String actorRole;

  @Column(name = "identity_level", updatable = false, length = 16)
  private String identityLevel;

  @Column(nullable = false, updatable = false, length = 32)
  private String source;

  @Column(name = "request_id", updatable = false, length = 255)
  private String requestId;

  @Column(name = "correlation_id", updatable = false, length = 255)
  private String correlationId;

  @Column(name = "idempotency_key", updatable = false, length = 255)
  private String idempotencyKey;

  @Column(name = "aggregate_version", updatable = false)
  private Long aggregateVersion;

  @Column(name = "policy_id", updatable = false, length = 120)
  private String policyId;

  @Column(name = "policy_version", updatable = false, length = 64)
  private String policyVersion;

  @Column(name = "reason_code", updatable = false, length = 120)
  private String reasonCode;

  @Column(name = "device_context_ref", updatable = false, length = 255)
  private String deviceContextRef;

  @Column(name = "previous_event_hash", updatable = false, length = 64)
  private String previousEventHash;

  @Column(name = "event_hash", nullable = false, updatable = false, length = 64)
  private String eventHash;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, updatable = false, columnDefinition = "jsonb")
  private Map<String, Object> payload;
}
