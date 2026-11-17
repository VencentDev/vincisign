package com.vencentdev.backend.modules.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProjectTransitionRequest(
    @NotBlank(message = "Transition action is required")
        @Size(max = 64, message = "Transition action must be at most 64 characters")
        String action,
    @NotNull(message = "Expected aggregate version is required")
        @PositiveOrZero(message = "Expected aggregate version must not be negative")
        Long expectedAggregateVersion,
    @Size(max = 80, message = "Reason code must be at most 80 characters") String reasonCode) {}
