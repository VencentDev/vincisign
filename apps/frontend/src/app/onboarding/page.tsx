import type { Metadata } from 'next';

import { siteConfig } from '@/config/site';
import { OnboardingPageContent } from '@/features/onboarding/components/onboarding-page-content';

export const metadata: Metadata = {
  title: `Account setup - ${siteConfig.name}`,
  description: 'Choose your account context and review the VinciSign platform policies.',
};

export default function OnboardingPage() {
  return <OnboardingPageContent />;
}
