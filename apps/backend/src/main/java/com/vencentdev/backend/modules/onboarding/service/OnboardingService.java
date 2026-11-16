package com.vencentdev.backend.modules.onboarding.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.onboarding.dto.CompleteOnboardingRequest;
import com.vencentdev.backend.modules.onboarding.dto.OnboardingStatusResponse;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;

public interface OnboardingService {
  OnboardingStatusResponse status(AuthenticatedUser principal);

  OnboardingStatusResponse complete(
      AuthenticatedUser principal, CompleteOnboardingRequest request);

  OnboardingStatusResponse withdraw(
      AuthenticatedUser principal, OnboardingConsentType consentType);
}
