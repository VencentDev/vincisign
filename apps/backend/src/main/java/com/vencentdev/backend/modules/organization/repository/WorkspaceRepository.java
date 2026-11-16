package com.vencentdev.backend.modules.organization.repository;

import com.vencentdev.backend.modules.organization.entity.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
  List<Workspace> findByOrganizationIdOrderByCreatedAtAsc(UUID organizationId);

  boolean existsByOrganizationIdAndNameIgnoreCase(UUID organizationId, String name);

  Optional<Workspace> findByOrganizationIdAndDefaultWorkspaceTrue(UUID organizationId);
}
