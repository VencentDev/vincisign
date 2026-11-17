import type { ReactNode } from 'react';
import { redirect } from 'next/navigation';

import { SessionErrorGate } from '@/components/session-error-gate';
import { getOnboardingRedirectUrl } from '@/features/onboarding/lib/onboarding';
import { getOnboardingStatusForGuard } from '@/features/onboarding/lib/onboarding-server';
import { auth } from '@/lib/auth';

export default async function AcceptInvitationLayout({ children }: { children: ReactNode }) {
  const session = await auth();

  if (!session) {
    redirect('/login?callbackUrl=/invitations/accept');
  }

  const onboarding = await getOnboardingStatusForGuard();
  if (onboarding && !onboarding.completed) {
    redirect(getOnboardingRedirectUrl('/invitations/accept'));
  }

  return (
    <SessionErrorGate>
      <div className="mx-auto w-full max-w-5xl">{children}</div>
    </SessionErrorGate>
  );
}
