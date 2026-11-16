package com.vencentdev.backend.modules.onboarding.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.onboarding.enums.ConsentStatus;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consent_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ConsentRecord extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "consent_type", nullable = false, length = 64)
  private OnboardingConsentType consentType;

  @Column(name = "policy_id", nullable = false, length = 120)
  private String policyId;

  @Column(name = "policy_version", nullable = false, length = 64)
  private String policyVersion;

  @Column(name = "policy_reference", nullable = false, length = 255)
  private String policyReference;

  @Column(name = "consent_text", nullable = false, columnDefinition = "TEXT")
  private String consentText;

  @Builder.Default
  @Column(nullable = false, length = 16)
  private String locale = "en-PH";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ConsentStatus status;

  @Column(name = "decided_at", nullable = false)
  private Instant decidedAt;

  @Column(name = "withdrawn_at")
  private Instant withdrawnAt;

  @Column(name = "actor_user_id", nullable = false)
  private UUID actorUserId;
}
