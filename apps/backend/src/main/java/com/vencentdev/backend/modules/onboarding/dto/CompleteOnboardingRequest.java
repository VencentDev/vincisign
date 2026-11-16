package com.vencentdev.backend.modules.onboarding.dto;

import com.vencentdev.backend.modules.user.enums.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompleteOnboardingRequest(
    @NotNull(message = "Account type is required") UserType userType,
    @NotEmpty(message = "Onboarding consent decisions are required")
        List<@Valid OnboardingConsentDecision> consents) {}
