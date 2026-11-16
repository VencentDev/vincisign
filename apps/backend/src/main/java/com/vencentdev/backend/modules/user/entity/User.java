package com.vencentdev.backend.modules.user.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.user.enums.ContactVerificationStatus;
import com.vencentdev.backend.modules.user.enums.IdentityLevel;
import com.vencentdev.backend.modules.user.enums.Role;
import com.vencentdev.backend.modules.user.enums.UserType;
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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "external_id", nullable = false, unique = true)
  private String externalId;

  @Column(unique = true)
  private String email;

  @Column(name = "display_name", length = 120)
  private String displayName;

  @Column(name = "password_hash", length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  private UserType userType;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "identity_level", nullable = false, length = 2)
  private IdentityLevel identityLevel = IdentityLevel.I0;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "contact_verification_status", nullable = false, length = 32)
  private ContactVerificationStatus contactVerificationStatus =
      ContactVerificationStatus.UNVERIFIED;

  @Column(name = "contact_verified_at")
  private Instant contactVerifiedAt;
}
