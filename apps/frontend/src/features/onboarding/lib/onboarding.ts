import type {
  OnboardingConsentType,
  OnboardingStatus,
} from '@/features/onboarding/types/onboarding';

export const onboardingConsentLabels: Record<OnboardingConsentType, string> = {
  TERMS_OF_SERVICE: 'Terms of Service',
  PRIVACY_NOTICE: 'Privacy Notice',
  ELECTRONIC_RECORDS: 'Electronic records and signatures',
  PLATFORM_LIMITS_ACKNOWLEDGEMENT: 'Platform limits acknowledgement',
};

export function getSafeOnboardingCallbackUrl(value: string | null | undefined): string {
  if (!value || !value.startsWith('/') || value.startsWith('//')) {
    return '/todos';
  }

  if (value === '/onboarding' || value.startsWith('/onboarding?')) {
    return '/todos';
  }

  return value;
}

export function getOnboardingRedirectUrl(callbackUrl: string): string {
  const params = new URLSearchParams({ callbackUrl });
  return `/onboarding?${params.toString()}`;
}

export function getCompletionErrorMessage(status: OnboardingStatus): string {
  if (status.blockedCode) {
    return `Onboarding is currently blocked (${status.blockedCode}). Please resolve the blocker before continuing.`;
  }

  return 'Accept every required consent to complete onboarding.';
}
