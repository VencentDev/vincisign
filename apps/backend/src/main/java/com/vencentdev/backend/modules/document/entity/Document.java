package com.vencentdev.backend.modules.document.entity;

import com.vencentdev.backend.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Document extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "original_version_id")
  private UUID originalVersionId;

  @Column(name = "working_version_id")
  private UUID workingVersionId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "original_media_type", nullable = false, length = 127)
  private String originalMediaType;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;
}
