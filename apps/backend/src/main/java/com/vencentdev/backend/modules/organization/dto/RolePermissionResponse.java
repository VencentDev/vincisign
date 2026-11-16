package com.vencentdev.backend.modules.organization.dto;

import java.util.List;

public record RolePermissionResponse(String scope, String role, List<String> permissions) {}
