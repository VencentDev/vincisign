package com.vencentdev.backend.modules.organization.dto;

import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMembershipRequest(
    @NotNull(message = "Membership role is required") OrganizationRole role) {}
