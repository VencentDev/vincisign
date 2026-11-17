'use client';

import { AlertCircle, CheckCircle2, Loader2, RefreshCw } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect, useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { getApiErrorMessage } from '@/features/onboarding/lib/onboarding-errors';
import {
  getCompletionErrorMessage,
  getSafeOnboardingCallbackUrl,
} from '@/features/onboarding/lib/onboarding';
import {
  useCompleteOnboarding,
  useOnboardingStatus,
} from '@/features/onboarding/api/onboarding.hooks';
import { OnboardingConsentList } from '@/features/onboarding/components/onboarding-consent-list';
import { PlatformLimitsCard } from '@/features/onboarding/components/platform-limits-card';
import { OnboardingUserTypeChoice } from '@/features/onboarding/components/onboarding-user-type-choice';
import type {
  OnboardingConsentType,
  OnboardingUserType,
} from '@/features/onboarding/types/onboarding';

export function OnboardingPageContent() {
  return (
    <Suspense fallback={<OnboardingLoadingState />}>
      <OnboardingForm />
    </Suspense>
  );
}

function OnboardingForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const callbackUrl = getSafeOnboardingCallbackUrl(searchParams.get('callbackUrl'));
  const onboarding = useOnboardingStatus();
  const completeOnboarding = useCompleteOnboarding();
  const [userType, setUserType] = useState<OnboardingUserType>('INDIVIDUAL');
  const [selectedConsents, setSelectedConsents] = useState<
    Partial<Record<OnboardingConsentType, boolean>>
  >({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);

  const requiredConsentsAccepted = useMemo(
    () =>
      onboarding.data?.requiredConsents
        .filter((consent) => consent.required)
        .every((consent) => selectedConsents[consent.type] === true) ?? false,
    [onboarding.data?.requiredConsents, selectedConsents],
  );

  useEffect(() => {
    if (!onboarding.data) {
      return;
    }

    setUserType(onboarding.data.userType);
    setSelectedConsents(
      Object.fromEntries(
        onboarding.data.requiredConsents.map((consent) => [
          consent.type,
          consent.status === 'ACCEPTED',
        ]),
      ),
    );
  }, [onboarding.data]);

  useEffect(() => {
    if (!onboarding.data?.completed || showSuccess) {
      return;
    }

    router.replace(callbackUrl);
  }, [callbackUrl, onboarding.data?.completed, router, showSuccess]);

  useEffect(() => {
    if (!showSuccess) {
      return;
    }

    const timeout = window.setTimeout(() => router.replace(callbackUrl), 900);
    return () => window.clearTimeout(timeout);
  }, [callbackUrl, router, showSuccess]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError(null);

    if (!requiredConsentsAccepted) {
      setSubmitError('Accept every required consent before continuing.');
      return;
    }

    try {
      const status = await completeOnboarding.mutateAsync({
        userType,
        consents: (onboarding.data?.requiredConsents ?? []).map((consent) => ({
          type: consent.type,
          accepted: selectedConsents[consent.type] === true,
        })),
      });

      if (!status.completed) {
        setSubmitError(getCompletionErrorMessage(status));
        return;
      }

      setShowSuccess(true);
    } catch (error) {
      setSubmitError(getApiErrorMessage(error));
    }
  }

  if (onboarding.isError) {
    return <OnboardingErrorState onRetry={() => void onboarding.refetch()} />;
  }

  if (onboarding.isLoading || !onboarding.data) {
    return <OnboardingLoadingState />;
  }

  if (showSuccess) {
    return <OnboardingSuccessState />;
  }

  return (
    <main className="mx-auto w-full max-w-3xl space-y-6">
      <div className="space-y-2">
        <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-accent-alt">
          Account setup
        </p>
        <h1 className="text-3xl font-bold tracking-tight">Set up your VinciSign account</h1>
        <p className="max-w-2xl text-sm leading-6 text-muted-foreground">
          Choose your starting account context and confirm the policies that govern your use of
          document signing and evidence features.
        </p>
      </div>

      {onboarding.data.blockedCode ? (
        <div
          role="alert"
          className="flex gap-3 rounded-lg border border-amber-500/30 bg-amber-500/10 p-4 text-sm text-amber-900 dark:text-amber-200"
        >
          <AlertCircle className="mt-0.5 size-4 shrink-0" />
          <span>
            Onboarding is currently blocked by the server. Reference:{' '}
            <span className="font-mono">{onboarding.data.blockedCode}</span>
          </span>
        </div>
      ) : null}

      <PlatformLimitsCard limits={onboarding.data.limits} />

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Before you begin</CardTitle>
          <CardDescription>
            Your selections and consent records are associated with this account and the policy
            versions displayed below.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-7" onSubmit={submit}>
            <OnboardingUserTypeChoice value={userType} onChange={setUserType} />
            <OnboardingConsentList
              consents={onboarding.data.requiredConsents}
              selected={selectedConsents}
              onChange={(type, accepted) => {
                setSelectedConsents((current) => ({ ...current, [type]: accepted }));
                setSubmitError(null);
              }}
            />

            {submitError ? (
              <p
                role="alert"
                className="rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive"
              >
                {submitError}
              </p>
            ) : null}

            <div className="flex flex-col gap-3 border-t pt-5 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-xs text-muted-foreground">
                You can review your consent records from the account settings once onboarding is
                complete.
              </p>
              <Button
                type="submit"
                disabled={completeOnboarding.isPending || !!onboarding.data.blockedCode}
              >
                {completeOnboarding.isPending ? (
                  <Loader2 className="mr-2 size-4 animate-spin" />
                ) : null}
                {completeOnboarding.isPending ? 'Saving setup…' : 'Complete setup'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}

function OnboardingLoadingState() {
  return (
    <div className="mx-auto flex min-h-[calc(100svh-10rem)] w-full max-w-3xl items-center justify-center">
      <div className="flex items-center gap-3 text-sm text-muted-foreground">
        <Loader2 className="size-5 animate-spin" />
        Loading onboarding…
      </div>
    </div>
  );
}

function OnboardingErrorState({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="mx-auto flex min-h-[calc(100svh-10rem)] w-full max-w-lg items-center">
      <Card className="w-full">
        <CardHeader>
          <CardTitle>Onboarding is unavailable</CardTitle>
          <CardDescription>
            We could not retrieve the policies and account options for this account.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Button type="button" variant="outline" onClick={onRetry}>
            <RefreshCw className="mr-2 size-4" />
            Try again
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

function OnboardingSuccessState() {
  return (
    <div className="mx-auto flex min-h-[calc(100svh-10rem)] w-full max-w-lg items-center">
      <Card className="w-full">
        <CardContent className="flex flex-col items-center gap-4 p-10 text-center">
          <CheckCircle2 className="size-12 text-emerald-600" />
          <div className="space-y-2">
            <h1 className="text-xl font-semibold">Setup complete</h1>
            <p className="text-sm text-muted-foreground">
              Your account is ready. We are taking you to your workspace.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
