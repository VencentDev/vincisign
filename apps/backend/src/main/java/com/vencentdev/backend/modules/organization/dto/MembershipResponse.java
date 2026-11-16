package com.vencentdev.backend.modules.organization.dto;

import com.vencentdev.backend.modules.organization.enums.MembershipStatus;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import java.util.UUID;

public record MembershipResponse(
    UUID id,
    UUID organizationId,
    UUID workspaceId,
    UUID userId,
    String email,
    MembershipStatus status,
    OrganizationRole role) {}
