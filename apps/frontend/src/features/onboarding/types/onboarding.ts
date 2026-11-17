export type OnboardingUserType = 'INDIVIDUAL' | 'ORGANIZATION';

export type OnboardingConsentType =
  | 'TERMS_OF_SERVICE'
  | 'PRIVACY_NOTICE'
  | 'ELECTRONIC_RECORDS'
  | 'PLATFORM_LIMITS_ACKNOWLEDGEMENT';

export type OnboardingConsentStatus = 'ACCEPTED' | 'DECLINED' | 'WITHDRAWN' | null;

export type OnboardingConsent = {
  type: OnboardingConsentType;
  policyId: string;
  policyVersion: string;
  policyReference: string;
  title: string;
  description: string;
  required: boolean;
  status: OnboardingConsentStatus;
  acceptedAt: string | null;
};

export type OnboardingStatus = {
  completed: boolean;
  userType: OnboardingUserType;
  blockedCode: string | null;
  limits: string[];
  requiredConsents: OnboardingConsent[];
};

export type CompleteOnboardingRequest = {
  userType: OnboardingUserType;
  consents: Array<{
    type: OnboardingConsentType;
    accepted: boolean;
  }>;
};
