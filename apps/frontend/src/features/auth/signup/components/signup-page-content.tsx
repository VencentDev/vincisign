'use client';

import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { signIn } from 'next-auth/react';
import { type FormEvent, Suspense, useState } from 'react';

import { siteConfig } from '@/config/site';
import { AuroraPanel } from '@/features/auth/shared/components/aurora-panel';
import { OAuthButtons } from '@/features/auth/shared/components/oauth-buttons';

export function SignupPageContent() {
  return (
    <Suspense fallback={<SignupShell />}>
      <SignupContent />
    </Suspense>
  );
}

function SignupContent() {
  const searchParams = useSearchParams();
  const callbackUrl = searchParams.get('callbackUrl') ?? '/todos';

  return <SignupShell callbackUrl={callbackUrl} />;
}

function SignupShell({ callbackUrl }: { callbackUrl?: string }) {
  const [error, setError] = useState<string | null>(null);
  const [isPending, setIsPending] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsPending(true);

    const formData = new FormData(event.currentTarget);
    const firstName = String(formData.get('first-name') ?? '').trim();
    const lastName = String(formData.get('last-name') ?? '').trim();
    const email = String(formData.get('signup-email') ?? '').trim();
    const password = String(formData.get('signup-password') ?? '');
    const confirmPassword = String(formData.get('confirm-password') ?? '');

    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      setError('Complete all fields to create your account.');
      setIsPending(false);
      return;
    }

    if (password.length < 8) {
      setError('Your password must be at least 8 characters.');
      setIsPending(false);
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      setIsPending(false);
      return;
    }

    try {
      const apiBaseUrl =
        process.env.NEXT_PUBLIC_API_BASE ??
        process.env.NEXT_PUBLIC_API_BASE_URL ??
        'http://localhost:8080';
      const response = await fetch(`${apiBaseUrl}/api/v1/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          password,
          displayName: `${firstName} ${lastName}`,
        }),
      });

      if (!response.ok) {
        setError('Unable to create your account. Check your details and try again.');
        setIsPending(false);
        return;
      }

      const result = await signIn('credentials', {
        email,
        password,
        redirect: false,
        redirectTo: callbackUrl,
      });

      if (!result || result.error) {
        setError('Your account was created, but automatic sign-in failed. Please sign in.');
        setIsPending(false);
        return;
      }

      window.location.assign(result.url ?? '/todos');
    } catch {
      setError('Unable to connect to the server. Try again.');
      setIsPending(false);
    }
  }

  return (
    <main className="relative -mx-4 -my-6 flex min-h-[calc(100svh-3.5rem)] max-w-[100vw] items-center overflow-x-clip overflow-y-hidden bg-background px-4 py-6 text-foreground selection:bg-accent-alt/30 md:py-8">
      <BackgroundOrbs />
      <section className="relative z-10 mx-auto flex min-h-[560px] w-full max-w-[min(56rem,calc(100vw-2rem))] animate-float-in flex-col-reverse overflow-hidden rounded-xl border border-border bg-card shadow-2xl md:flex-row">
        <div className="flex flex-1 items-center p-8 md:p-12">
          <form className="w-full space-y-6" onSubmit={handleSubmit}>
            <div className="space-y-2">
              <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-accent-alt">
                Workspace setup
              </p>
              <h1 className="text-3xl font-bold tracking-tight">Create account</h1>
              <p className="text-sm text-muted-foreground">
                Create your account to send, sign, and verify documents.
              </p>
            </div>

            <div className="space-y-4">
              <div className="grid gap-3 sm:grid-cols-2">
                <Field
                  id="first-name"
                  label="First Name"
                  placeholder="John"
                  autoComplete="given-name"
                />
                <Field
                  id="last-name"
                  label="Last Name"
                  placeholder="Doe"
                  autoComplete="family-name"
                />
              </div>
              <Field
                id="signup-email"
                label="Email"
                type="email"
                placeholder="hello@world.com"
                autoComplete="email"
              />
              <div className="grid gap-3 sm:grid-cols-2">
                <Field
                  id="signup-password"
                  label="Secret"
                  type="password"
                  placeholder="••••"
                  autoComplete="new-password"
                />
                <Field
                  id="confirm-password"
                  label="Confirm"
                  type="password"
                  placeholder="••••"
                  autoComplete="new-password"
                />
              </div>
            </div>

            {error ? (
              <p
                role="alert"
                className="rounded-lg border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive"
              >
                {error}
              </p>
            ) : null}

            <button
              type="submit"
              disabled={isPending}
              className="w-full rounded-lg bg-gradient-to-r from-accent to-accent-alt py-2.5 font-semibold text-white transition-all duration-300 hover:opacity-90 hover:shadow-lg hover:shadow-accent/20 active:scale-[0.98]"
            >
              {isPending ? 'Creating account…' : 'Create account'}
            </button>

            <AuthDivider label="Or continue with" />
            <OAuthButtons callbackUrl={callbackUrl} />

            <p className="text-center text-sm text-muted-foreground">
              Already have an account?{' '}
              <Link
                href="/login"
                className="text-accent-alt underline-offset-4 transition-colors hover:text-accent hover:underline decoration-accent-alt/40"
              >
                Sign in
              </Link>
            </p>
          </form>
        </div>
        <AuroraPanel
          variant="signup"
          title={`${siteConfig.name} Identity`}
          subtitle="Start signing securely"
        />
      </section>
    </main>
  );
}

function Field({
  id,
  label,
  type = 'text',
  placeholder,
  autoComplete,
}: {
  id: string;
  label: string;
  type?: string;
  placeholder: string;
  autoComplete?: string;
}) {
  return (
    <div className="space-y-1.5">
      <label
        htmlFor={id}
        className="font-mono text-[11px] uppercase tracking-wider text-muted-foreground"
      >
        {label}
      </label>
      <input
        id={id}
        name={id}
        type={type}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className="field-input"
      />
    </div>
  );
}

function AuthDivider({ label }: { label: string }) {
  return (
    <div className="relative">
      <div className="absolute inset-0 flex items-center">
        <span className="w-full border-t border-border" />
      </div>
      <div className="relative flex justify-center">
        <span className="bg-card px-3 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          {label}
        </span>
      </div>
    </div>
  );
}

function BackgroundOrbs() {
  return (
    <>
      <span className="absolute left-8 top-16 size-56 rounded-full bg-accent-alt/20 blur-3xl" />
      <span className="absolute bottom-6 left-1/2 size-80 -translate-x-1/2 rounded-full bg-accent/15 blur-3xl" />
      <span className="absolute right-16 top-1/3 size-40 rounded-full bg-cyan-400/10 blur-3xl" />
    </>
  );
}
