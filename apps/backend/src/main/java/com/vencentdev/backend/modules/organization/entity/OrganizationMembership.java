package com.vencentdev.backend.modules.organization.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.organization.enums.MembershipStatus;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organization_memberships")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class OrganizationMembership extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private OrganizationRole role = OrganizationRole.ORGANIZATION_MEMBER;
}
