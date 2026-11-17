package com.vencentdev.backend.modules.document.repository;

import com.vencentdev.backend.modules.document.entity.DocumentVersion;
import com.vencentdev.backend.modules.document.enums.DocumentVersionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
  List<DocumentVersion> findByDocumentIdAndOrganizationIdOrderByVersionNumberAsc(
      UUID documentId, UUID organizationId);

  Optional<DocumentVersion> findByDocumentIdAndVersionNumberAndOrganizationId(
      UUID documentId, int versionNumber, UUID organizationId);

  Optional<DocumentVersion> findTopByDocumentIdAndOrganizationIdOrderByVersionNumberDesc(
      UUID documentId, UUID organizationId);

  Optional<DocumentVersion> findByIdAndDocumentIdAndOrganizationId(
      UUID id, UUID documentId, UUID organizationId);

  Optional<DocumentVersion> findByIdAndOrganizationId(UUID id, UUID organizationId);

  List<DocumentVersion> findByDocumentIdAndStatusAndOrganizationIdOrderByVersionNumberAsc(
      UUID documentId, DocumentVersionStatus status, UUID organizationId);

  boolean existsByDocumentIdAndVersionNumberAndOrganizationId(
      UUID documentId, int versionNumber, UUID organizationId);
}
