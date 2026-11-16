package com.vencentdev.backend.modules.organization.dto;

import java.util.List;
import java.util.UUID;

public record OrganizationResponse(
    UUID id, String name, String jurisdictionProfile, List<WorkspaceResponse> workspaces) {}
