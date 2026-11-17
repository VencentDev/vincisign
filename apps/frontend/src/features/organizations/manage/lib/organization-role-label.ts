import type { OrganizationRole } from '@/features/organizations/manage/api/organizations.hooks';

export const assignableOrganizationRoles: OrganizationRole[] = [
  'ORGANIZATION_ADMINISTRATOR',
  'ORGANIZATION_MEMBER',
  'AUDITOR',
  'API_OPERATOR',
];

export function organizationRoleLabel(role: OrganizationRole) {
  return role
    .replace('ORGANIZATION_', '')
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export function organizationStatusLabel(status: string) {
  return status
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}
