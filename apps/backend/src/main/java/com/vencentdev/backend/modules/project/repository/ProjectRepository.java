package com.vencentdev.backend.modules.project.repository;

import com.vencentdev.backend.modules.project.entity.Project;
import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
  List<Project> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

  List<Project> findByWorkspaceIdAndOrganizationIdOrderByCreatedAtDesc(
      UUID workspaceId, UUID organizationId);

  Optional<Project> findByIdAndOrganizationId(UUID id, UUID organizationId);

  List<Project> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
      UUID organizationId, ProjectStatus status);
}
