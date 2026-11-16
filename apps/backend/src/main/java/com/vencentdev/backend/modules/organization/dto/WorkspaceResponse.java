package com.vencentdev.backend.modules.organization.dto;

import java.util.UUID;

public record WorkspaceResponse(
    UUID id, UUID organizationId, String name, boolean defaultWorkspace) {}
