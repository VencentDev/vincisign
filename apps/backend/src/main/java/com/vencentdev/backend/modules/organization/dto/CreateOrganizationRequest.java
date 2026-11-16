package com.vencentdev.backend.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
    @NotBlank(message = "Organization name is required")
        @Size(max = 160, message = "Organization name must be at most 160 characters")
        String name) {}
