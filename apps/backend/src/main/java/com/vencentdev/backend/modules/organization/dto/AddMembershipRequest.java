package com.vencentdev.backend.modules.organization.dto;

import com.vencentdev.backend.modules.organization.enums.OrganizationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddMembershipRequest(
    @NotBlank(message = "Account email is required")
        @Email(message = "Account email must be valid")
        @Size(max = 255, message = "Account email must be at most 255 characters")
        String email,
    OrganizationRole role) {}
