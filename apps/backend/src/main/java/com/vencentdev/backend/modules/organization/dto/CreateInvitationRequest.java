package com.vencentdev.backend.modules.organization.dto;

import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateInvitationRequest(
    @NotBlank(message = "Invitation email is required")
        @Email(message = "Invitation email must be valid")
        @Size(max = 255, message = "Invitation email must be at most 255 characters")
        String email,
    @NotNull(message = "Invitation role is required") OrganizationRole role,
    UUID workspaceId) {}
