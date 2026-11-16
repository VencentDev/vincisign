package com.vencentdev.backend.modules.document.repository;

import com.vencentdev.backend.modules.document.entity.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
  List<Document> findByProjectIdAndOrganizationIdOrderByCreatedAtAsc(
      UUID projectId, UUID organizationId);

  Optional<Document> findByIdAndProjectIdAndOrganizationId(
      UUID id, UUID projectId, UUID organizationId);

  Optional<Document> findByIdAndOrganizationId(UUID id, UUID organizationId);

  Optional<Document> findByWorkingVersionIdAndOrganizationId(
      UUID workingVersionId, UUID organizationId);
}
