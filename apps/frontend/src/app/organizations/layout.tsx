import type { ReactNode } from 'react';
import { redirect } from 'next/navigation';

import { SessionErrorGate } from '@/components/session-error-gate';
import { auth } from '@/lib/auth';

export default async function OrganizationsLayout({ children }: { children: ReactNode }) {
  const session = await auth();

  if (!session) {
    redirect('/login?callbackUrl=/organizations');
  }

  return (
    <SessionErrorGate>
      <div className="mx-auto w-full max-w-5xl">{children}</div>
    </SessionErrorGate>
  );
}
