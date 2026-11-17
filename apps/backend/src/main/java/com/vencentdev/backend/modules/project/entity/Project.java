package com.vencentdev.backend.modules.project.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Project extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "owner_user_id", nullable = false)
  private UUID ownerUserId;

  @Column(nullable = false, length = 200)
  private String name;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ProjectStatus status = ProjectStatus.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(name = "suspended_from_status", length = 32)
  private ProjectStatus suspendedFromStatus;

  @Builder.Default
  @Column(name = "policy_id", nullable = false, length = 120)
  private String policyId = "DOC-POLICY";

  @Builder.Default
  @Column(name = "policy_version", nullable = false, length = 64)
  private String policyVersion = "0.2";

  @Version
  @Column(name = "aggregate_version", nullable = false)
  private long aggregateVersion;

  public void transitionTo(ProjectStatus nextStatus, ProjectStatus nextSuspendedFromStatus) {
    this.status = nextStatus;
    this.suspendedFromStatus = nextSuspendedFromStatus;
  }
}
