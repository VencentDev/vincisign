'use client';

import { Building2, UserRound } from 'lucide-react';

import type { OnboardingUserType } from '@/features/onboarding/types/onboarding';

type OnboardingUserTypeChoiceProps = {
  value: OnboardingUserType;
  onChange: (value: OnboardingUserType) => void;
  disabled?: boolean;
};

export function OnboardingUserTypeChoice({
  value,
  onChange,
  disabled = false,
}: OnboardingUserTypeChoiceProps) {
  return (
    <fieldset className="space-y-3" disabled={disabled}>
      <legend className="text-base font-semibold">How will you use VinciSign?</legend>
      <p className="text-sm text-muted-foreground">
        This sets the starting account context. You can still work with organizations and invite
        collaborators after setup.
      </p>
      <div className="grid gap-3 sm:grid-cols-2">
        <UserTypeOption
          value="INDIVIDUAL"
          selected={value === 'INDIVIDUAL'}
          title="Individual"
          description="Sign and manage documents for yourself."
          icon={<UserRound className="size-5" />}
          onChange={onChange}
        />
        <UserTypeOption
          value="ORGANIZATION"
          selected={value === 'ORGANIZATION'}
          title="Organization"
          description="Create or join a shared signing workspace."
          icon={<Building2 className="size-5" />}
          onChange={onChange}
        />
      </div>
    </fieldset>
  );
}

function UserTypeOption({
  value,
  selected,
  title,
  description,
  icon,
  onChange,
}: {
  value: OnboardingUserType;
  selected: boolean;
  title: string;
  description: string;
  icon: React.ReactNode;
  onChange: (value: OnboardingUserType) => void;
}) {
  return (
    <label
      className={`flex cursor-pointer gap-3 rounded-lg border p-4 transition-colors ${
        selected
          ? 'border-primary bg-primary/5 ring-1 ring-primary/30'
          : 'border-border bg-card hover:bg-muted/50'
      }`}
    >
      <input
        type="radio"
        name="onboarding-user-type"
        value={value}
        checked={selected}
        onChange={() => onChange(value)}
        className="mt-1 size-4 accent-primary"
      />
      <span className="flex min-w-0 flex-1 gap-3">
        <span className="mt-0.5 text-primary">{icon}</span>
        <span className="space-y-1">
          <span className="block font-medium">{title}</span>
          <span className="block text-sm text-muted-foreground">{description}</span>
        </span>
      </span>
    </label>
  );
}
