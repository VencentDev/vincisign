package com.vencentdev.backend.modules.onboarding.dto;

import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import jakarta.validation.constraints.NotNull;

public record OnboardingConsentDecision(
    @NotNull(message = "Consent type is required") OnboardingConsentType type, boolean accepted) {}
