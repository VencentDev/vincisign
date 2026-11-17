package com.vencentdev.backend.modules.onboarding.service;

import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import java.util.List;

public final class OnboardingPolicyCatalog {

  private static final List<PolicyDefinition> REQUIRED_POLICIES =
      List.of(
          new PolicyDefinition(
              OnboardingConsentType.TERMS_OF_SERVICE,
              "LEG-001",
              "engineering-draft-0.1",
              "LEGAL-OPS-INVENTORY-0.1#LEG-001",
              "Terms of Service",
              "I understand the account, organization, document, and platform-use terms presented by VinciSign."),
          new PolicyDefinition(
              OnboardingConsentType.PRIVACY_NOTICE,
              "LEG-002",
              "engineering-draft-0.1",
              "LEGAL-OPS-INVENTORY-0.1#LEG-002",
              "Privacy Notice",
              "I have read the notice explaining the personal data VinciSign records, why it is used, and how policy versions are preserved."),
          new PolicyDefinition(
              OnboardingConsentType.ELECTRONIC_RECORDS,
              "LEG-003",
              "engineering-draft-0.1",
              "LEGAL-OPS-INVENTORY-0.1#LEG-003",
              "Electronic Records Consent",
              "I consent to receive, access, and use applicable records electronically through VinciSign."),
          new PolicyDefinition(
              OnboardingConsentType.PLATFORM_LIMITS_ACKNOWLEDGEMENT,
              "PLATFORM-LIMITS",
              "engineering-draft-0.1",
              "SIGNATURE-IDENTITY-0.1#limits",
              "Platform Limits",
              "I understand that VinciSign records workflow and evidence; it does not provide legal advice or independently determine legal validity, identity, or notarial eligibility."));

  private OnboardingPolicyCatalog() {}

  public static List<PolicyDefinition> required() {
    return REQUIRED_POLICIES;
  }

  public record PolicyDefinition(
      OnboardingConsentType type,
      String policyId,
      String policyVersion,
      String policyReference,
      String title,
      String description) {}
}
