package com.vencentdev.backend.modules.organization.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.organization.enums.InvitationStatus;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
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
import lombok.Setter;

@Entity
@Table(name = "membership_invitations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class MembershipInvitation extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "workspace_id")
  private UUID workspaceId;

  @Column(nullable = false, length = 255)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private OrganizationRole role;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private InvitationStatus status = InvitationStatus.PENDING;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "invited_by_user_id", nullable = false)
  private UUID invitedByUserId;

  @Column(name = "accepted_by_user_id")
  private UUID acceptedByUserId;

  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;
}
