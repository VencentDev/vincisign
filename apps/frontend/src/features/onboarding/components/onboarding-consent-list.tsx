'use client';

import { Check } from 'lucide-react';

import { onboardingConsentLabels } from '@/features/onboarding/lib/onboarding';

import type {
  OnboardingConsent,
  OnboardingConsentType,
} from '@/features/onboarding/types/onboarding';

type OnboardingConsentListProps = {
  consents: OnboardingConsent[];
  selected: Partial<Record<OnboardingConsentType, boolean>>;
  onChange: (type: OnboardingConsentType, accepted: boolean) => void;
  disabled?: boolean;
};

export function OnboardingConsentList({
  consents,
  selected,
  onChange,
  disabled = false,
}: OnboardingConsentListProps) {
  return (
    <fieldset className="space-y-3" disabled={disabled}>
      <legend className="text-base font-semibold">Review and consent</legend>
      <p className="text-sm text-muted-foreground">
        Read each policy reference before accepting. Your choices are recorded with the policy
        version shown below.
      </p>
      <div className="space-y-3">
        {consents.map((consent) => {
          const checked = selected[consent.type] ?? false;

          return (
            <label
              key={consent.type}
              className={`block rounded-lg border p-4 transition-colors ${
                checked ? 'border-primary/50 bg-primary/5' : 'border-border bg-card'
              }`}
            >
              <span className="flex items-start gap-3">
                <input
                  type="checkbox"
                  checked={checked}
                  onChange={(event) => onChange(consent.type, event.target.checked)}
                  className="mt-1 size-4 shrink-0 accent-primary"
                />
                <span className="min-w-0 flex-1 space-y-2">
                  <span className="flex flex-wrap items-center gap-2">
                    <span className="font-medium">
                      {consent.title || onboardingConsentLabels[consent.type]}
                    </span>
                    <span className="rounded-full border px-2 py-0.5 font-mono text-[10px] uppercase tracking-wider text-muted-foreground">
                      {consent.required ? 'Required' : 'Optional'}
                    </span>
                    {checked ? <Check className="size-4 text-emerald-600" /> : null}
                  </span>
                  <span className="block text-sm leading-6 text-muted-foreground">
                    {consent.description}
                  </span>
                  <span className="flex flex-wrap gap-x-4 gap-y-1 font-mono text-[11px] text-muted-foreground">
                    <span>Reference: {consent.policyReference}</span>
                    <span>Version: {consent.policyVersion}</span>
                  </span>
                </span>
              </span>
            </label>
          );
        })}
      </div>
    </fieldset>
  );
}
