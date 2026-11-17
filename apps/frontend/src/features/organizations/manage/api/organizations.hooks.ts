'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useSession } from 'next-auth/react';

import { clientApi } from '@/lib/api';
import { qk } from '@/lib/queryKeys';

import type { components, paths } from '@app/api-types';

export type Organization =
  paths['/api/v1/organizations']['get']['responses']['200']['content']['application/json'][number];
export type OrganizationRole = components['schemas']['OrganizationRole'];
export type Membership =
  paths['/api/v1/organizations/{organizationId}/memberships']['get']['responses']['200']['content']['application/json'][number];
export type Invitation =
  paths['/api/v1/organizations/{organizationId}/invitations']['get']['responses']['200']['content']['application/json'][number];
type CreateOrganizationBody =
  paths['/api/v1/organizations']['post']['requestBody']['content']['application/json'];
type CreateInvitationBody =
  paths['/api/v1/organizations/{organizationId}/invitations']['post']['requestBody']['content']['application/json'];

export function useOrganizations() {
  const { data: session } = useSession();

  return useQuery({
    queryKey: qk.organizations.all(),
    queryFn: () => clientApi<Organization[]>(session?.accessToken, '/api/v1/organizations'),
    enabled: !!session?.accessToken,
  });
}

export function useOrganizationMemberships(organizationId: string | undefined) {
  const { data: session } = useSession();

  return useQuery({
    queryKey: organizationId
      ? qk.organizations.memberships(organizationId)
      : ['organizations', 'none', 'memberships'],
    queryFn: () =>
      clientApi<Membership[]>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/memberships`,
      ),
    enabled: !!session?.accessToken && !!organizationId,
  });
}

export function useOrganizationInvitations(organizationId: string | undefined) {
  const { data: session } = useSession();

  return useQuery({
    queryKey: organizationId
      ? qk.organizations.invitations(organizationId)
      : ['organizations', 'none', 'invitations'],
    queryFn: () =>
      clientApi<Invitation[]>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/invitations`,
      ),
    enabled: !!session?.accessToken && !!organizationId,
  });
}

export function useCreateOrganization() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CreateOrganizationBody) =>
      clientApi<Organization>(session?.accessToken, '/api/v1/organizations', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: qk.organizations.all() }),
  });
}

export function useCreateInvitation() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      organizationId,
      body,
    }: {
      organizationId: string;
      body: CreateInvitationBody;
    }) =>
      clientApi<Invitation>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/invitations`,
        {
          method: 'POST',
          body: JSON.stringify(body),
        },
      ),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({
        queryKey: qk.organizations.invitations(variables.organizationId),
      }),
  });
}

export function useRevokeInvitation() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      organizationId,
      invitationId,
    }: {
      organizationId: string;
      invitationId: string;
    }) =>
      clientApi<void>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/invitations/${invitationId}`,
        { method: 'DELETE' },
      ),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({
        queryKey: qk.organizations.invitations(variables.organizationId),
      }),
  });
}

export function useUpdateMembership() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      organizationId,
      membershipId,
      role,
    }: {
      organizationId: string;
      membershipId: string;
      role: OrganizationRole;
    }) =>
      clientApi<Membership>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/memberships/${membershipId}`,
        {
          method: 'PATCH',
          body: JSON.stringify({ role }),
        },
      ),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({
        queryKey: qk.organizations.memberships(variables.organizationId),
      }),
  });
}

export function useRevokeMembership() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      organizationId,
      membershipId,
    }: {
      organizationId: string;
      membershipId: string;
    }) =>
      clientApi<void>(
        session?.accessToken,
        `/api/v1/organizations/${organizationId}/memberships/${membershipId}`,
        { method: 'DELETE' },
      ),
    onSuccess: (_data, variables) =>
      queryClient.invalidateQueries({
        queryKey: qk.organizations.memberships(variables.organizationId),
      }),
  });
}

export function useAcceptInvitation() {
  const { data: session } = useSession();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (token: string) =>
      clientApi<Membership>(session?.accessToken, '/api/v1/invitations/accept', {
        method: 'POST',
        body: JSON.stringify({ token }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: qk.organizations.all() }),
  });
}
