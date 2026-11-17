package com.vencentdev.backend.modules.project.dto;

import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import java.util.List;
import java.util.UUID;

public record ProjectTransitionResponse(
    UUID transitionId,
    UUID projectId,
    ProjectStatus previousStatus,
    ProjectStatus status,
    long aggregateVersion,
    String action,
    String reasonCode,
    List<UUID> evidenceEventIds,
    List<String> pendingWork) {}
