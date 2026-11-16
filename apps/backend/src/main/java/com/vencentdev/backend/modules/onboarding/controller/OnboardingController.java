package com.vencentdev.backend.modules.onboarding.controller;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.CurrentUser;
import com.vencentdev.backend.modules.onboarding.dto.CompleteOnboardingRequest;
import com.vencentdev.backend.modules.onboarding.dto.OnboardingStatusResponse;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import com.vencentdev.backend.modules.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

  private final OnboardingService onboarding;

  public OnboardingController(OnboardingService onboarding) {
    this.onboarding = onboarding;
  }

  @GetMapping("/status")
  public OnboardingStatusResponse status(@CurrentUser AuthenticatedUser user) {
    return onboarding.status(user);
  }

  @PostMapping("/complete")
  public OnboardingStatusResponse complete(
      @CurrentUser AuthenticatedUser user,
      @Valid @RequestBody CompleteOnboardingRequest request) {
    return onboarding.complete(user, request);
  }

  @PostMapping("/consents/{consentType}/withdraw")
  public OnboardingStatusResponse withdraw(
      @CurrentUser AuthenticatedUser user, @PathVariable OnboardingConsentType consentType) {
    return onboarding.withdraw(user, consentType);
  }
}
