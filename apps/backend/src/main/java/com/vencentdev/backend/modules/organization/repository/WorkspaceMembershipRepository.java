package com.vencentdev.backend.modules.organization.repository;

import com.vencentdev.backend.modules.organization.entity.WorkspaceMembership;
import com.vencentdev.backend.modules.organization.enums.MembershipStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
  List<WorkspaceMembership> findByUserId(UUID userId);

  List<WorkspaceMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

  Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

  boolean existsByWorkspaceIdAndUserIdAndStatus(
      UUID workspaceId, UUID userId, MembershipStatus status);
}
