package com.vencentdev.backend.modules.onboarding.dto;

import com.vencentdev.backend.modules.user.enums.UserType;
import java.util.List;

public record OnboardingStatusResponse(
    boolean completed,
    UserType userType,
    String blockedCode,
    List<String> limits,
    List<ConsentRequirementResponse> requiredConsents) {}
