package com.vencentdev.backend.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
    @NotBlank(message = "Invitation token is required") String token) {}
