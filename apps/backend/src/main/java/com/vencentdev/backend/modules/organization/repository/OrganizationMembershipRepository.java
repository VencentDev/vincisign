package com.vencentdev.backend.modules.organization.repository;

import com.vencentdev.backend.modules.organization.entity.OrganizationMembership;
import com.vencentdev.backend.modules.organization.enums.MembershipStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMembershipRepository
    extends JpaRepository<OrganizationMembership, UUID> {
  List<OrganizationMembership> findByOrganizationId(UUID organizationId);

  List<OrganizationMembership> findByUserIdAndStatusOrderByCreatedAtAsc(
      UUID userId, MembershipStatus status);

  Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

  boolean existsByOrganizationIdAndUserIdAndStatus(
      UUID organizationId, UUID userId, MembershipStatus status);
}
