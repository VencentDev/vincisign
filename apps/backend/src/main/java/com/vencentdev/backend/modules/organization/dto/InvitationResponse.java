package com.vencentdev.backend.modules.organization.dto;

import com.vencentdev.backend.modules.organization.enums.InvitationStatus;
import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
    UUID id,
    UUID organizationId,
    UUID workspaceId,
    String email,
    OrganizationRole role,
    InvitationStatus status,
    Instant expiresAt,
    String token) {}
