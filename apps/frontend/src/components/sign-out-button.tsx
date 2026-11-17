'use client';

import { LogOut } from 'lucide-react';
import { signOut, useSession } from 'next-auth/react';

import { Button } from '@/components/ui/button';

export function SignOutButton() {
  const { data: session } = useSession();

  async function handleSignOut() {
    if (session?.accessToken) {
      const apiBaseUrl =
        process.env.NEXT_PUBLIC_API_BASE ??
        process.env.NEXT_PUBLIC_API_BASE_URL ??
        'http://localhost:8080';
      await fetch(`${apiBaseUrl}/api/v1/auth/logout`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${session.accessToken}` },
      }).catch(() => undefined);
    }

    await signOut({ redirectTo: '/login' });
  }

  return (
    <Button
      type="button"
      variant="outline"
      size="sm"
      className="gap-2"
      onClick={() => void handleSignOut()}
    >
      <LogOut className="size-4" aria-hidden="true" />
      <span>Sign out</span>
    </Button>
  );
}
