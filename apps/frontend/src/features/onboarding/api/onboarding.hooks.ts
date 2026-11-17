'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSession } from 'next-auth/react';

import { clientApi } from '@/lib/api';
import { qk } from '@/lib/queryKeys';

import type {
  CompleteOnboardingRequest,
  OnboardingConsentType,
  OnboardingStatus,
} from '@/features/onboarding/types/onboarding';

export function useOnboardingStatus() {
  const { data: session } = useSession();

  return useQuery({
    queryKey: qk.onboarding.status(),
    queryFn: () => clientApi<OnboardingStatus>(session?.accessToken, '/api/v1/onboarding/status'),
    enabled: !!session?.accessToken,
  });
}

export function useCompleteOnboarding() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CompleteOnboardingRequest) =>
      clientApi<OnboardingStatus>(session?.accessToken, '/api/v1/onboarding/complete', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
    onSuccess: (status) => {
      queryClient.setQueryData(qk.onboarding.status(), status);
      void queryClient.invalidateQueries({ queryKey: qk.me() });
    },
  });
}

export function useWithdrawOnboardingConsent() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (consentType: OnboardingConsentType) =>
      clientApi<OnboardingStatus>(
        session?.accessToken,
        `/api/v1/onboarding/consents/${consentType}/withdraw`,
        { method: 'POST' },
      ),
    onSuccess: (status) => {
      queryClient.setQueryData(qk.onboarding.status(), status);
    },
  });
}
