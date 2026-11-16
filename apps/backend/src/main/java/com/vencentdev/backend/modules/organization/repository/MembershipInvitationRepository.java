package com.vencentdev.backend.modules.organization.repository;

import com.vencentdev.backend.modules.organization.entity.MembershipInvitation;
import com.vencentdev.backend.modules.organization.enums.InvitationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipInvitationRepository extends JpaRepository<MembershipInvitation, UUID> {
  Optional<MembershipInvitation> findByTokenHash(String tokenHash);

  Optional<MembershipInvitation> findByIdAndOrganizationId(UUID id, UUID organizationId);

  List<MembershipInvitation> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
      UUID organizationId, InvitationStatus status);

  boolean existsByOrganizationIdAndEmailIgnoreCaseAndWorkspaceIdAndStatus(
      UUID organizationId, String email, UUID workspaceId, InvitationStatus status);
}
