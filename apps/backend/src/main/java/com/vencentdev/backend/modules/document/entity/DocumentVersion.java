package com.vencentdev.backend.modules.document.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import com.vencentdev.backend.modules.document.enums.DocumentVersionRole;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
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
@Table(name = "document_versions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class DocumentVersion extends AuditableEntity {

  public static final String FINGERPRINT_ALGORITHM_SHA_256 = "SHA-256";

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "document_id", nullable = false)
  private UUID documentId;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "predecessor_version_id")
  private UUID predecessorVersionId;

  @Column(name = "version_number", nullable = false)
  private int versionNumber;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "version_role", nullable = false, length = 32)
  private DocumentVersionRole role = DocumentVersionRole.ORIGINAL;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private DocumentVersionStatus status = DocumentVersionStatus.RECEIVED;

  @Builder.Default
  @Column(name = "immutable_version", nullable = false)
  private boolean immutable = false;

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "media_type", nullable = false, length = 127)
  private String mediaType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "storage_bucket", length = 120)
  private String storageBucket;

  @Column(name = "storage_object_key", length = 1024)
  private String storageObjectKey;

  @Builder.Default
  @Column(name = "fingerprint_algorithm", nullable = false, length = 32)
  private String fingerprintAlgorithm = FINGERPRINT_ALGORITHM_SHA_256;

  @Column(length = 64)
  private String fingerprint;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;
}
