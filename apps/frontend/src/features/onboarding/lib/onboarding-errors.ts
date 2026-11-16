import { ApiError } from '@/lib/api';

export function getApiErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return 'We could not save your onboarding choices. Please try again.';
  }

  if (error.status === 401) {
    return 'Your session has expired. Sign in again to continue onboarding.';
  }

  if (error.status === 429) {
    return 'Too many attempts. Please wait a moment and try again.';
  }

  if (isRecord(error.payload) && typeof error.payload.message === 'string') {
    return error.payload.message;
  }

  return 'We could not save your onboarding choices. Please try again.';
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
