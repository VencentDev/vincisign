import { serverApi } from '@/lib/api';

import type { OnboardingStatus } from '@/features/onboarding/types/onboarding';

export async function getOnboardingStatusForGuard(): Promise<OnboardingStatus | null> {
  try {
    return await serverApi<OnboardingStatus>('/api/v1/onboarding/status');
  } catch {
    // Do not lock a signed-in user out of the app when the status service is temporarily unavailable.
    return null;
  }
}
