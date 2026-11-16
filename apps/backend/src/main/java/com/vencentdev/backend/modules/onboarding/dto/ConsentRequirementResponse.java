package com.vencentdev.backend.modules.onboarding.dto;

import com.vencentdev.backend.modules.onboarding.enums.ConsentStatus;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import java.time.Instant;

public record ConsentRequirementResponse(
    OnboardingConsentType type,
    String policyId,
    String policyVersion,
    String policyReference,
    String title,
    String description,
    boolean required,
    ConsentStatus status,
    Instant acceptedAt) {}
