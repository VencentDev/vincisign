'use client';

import Link from 'next/link';
import { Building2, ChevronRight, Loader2, RefreshCw } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Skeleton } from '@/components/ui/skeleton';
import { CreateOrganizationForm } from '@/features/organizations/manage/components/create-organization-form';
import { InvitationPanel } from '@/features/organizations/manage/components/invitation-panel';
import { MembershipList } from '@/features/organizations/manage/components/membership-list';
import {
  useOrganizationInvitations,
  useOrganizationMemberships,
  useOrganizations,
} from '@/features/organizations/manage/api/organizations.hooks';

export function OrganizationsPageContent() {
  const organizations = useOrganizations();
  const [selectedOrganizationId, setSelectedOrganizationId] = useState<string>();
  const selectedOrganization = useMemo(
    () => organizations.data?.find((organization) => organization.id === selectedOrganizationId),
    [organizations.data, selectedOrganizationId],
  );
  const memberships = useOrganizationMemberships(selectedOrganization?.id);
  const invitations = useOrganizationInvitations(selectedOrganization?.id);

  useEffect(() => {
    if (!organizations.data?.length) {
      setSelectedOrganizationId(undefined);
      return;
    }

    if (
      !selectedOrganizationId ||
      !organizations.data.some((organization) => organization.id === selectedOrganizationId)
    ) {
      setSelectedOrganizationId(organizations.data[0].id);
    }
  }, [organizations.data, selectedOrganizationId]);

  if (organizations.isLoading) {
    return <OrganizationsSkeleton />;
  }

  if (organizations.error) {
    return (
      <div className="space-y-4">
        <PageHeading />
        <Card>
          <CardContent className="flex flex-col items-center gap-4 p-8 text-center">
            <p className="text-sm text-destructive">Could not load your organizations.</p>
            <Button type="button" variant="outline" onClick={() => void organizations.refetch()}>
              <RefreshCw className="mr-2 size-4" />
              Try again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!organizations.data?.length) {
    return (
      <div className="space-y-5">
        <PageHeading />
        <CreateOrganizationForm />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <PageHeading />

      <Card>
        <CardHeader>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <CardTitle className="text-lg">Organization workspace</CardTitle>
              <CardDescription>
                Choose the organization whose access you want to manage.
              </CardDescription>
            </div>
            <Button asChild variant="outline" size="sm">
              <Link href="/invitations/accept">
                Accept an invitation
                <ChevronRight className="ml-2 size-4" />
              </Link>
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <Select value={selectedOrganization?.id} onValueChange={setSelectedOrganizationId}>
            <SelectTrigger aria-label="Select organization">
              <SelectValue placeholder="Select organization" />
            </SelectTrigger>
            <SelectContent>
              {organizations.data.map((organization) => (
                <SelectItem key={organization.id} value={organization.id}>
                  {organization.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {selectedOrganization && (
            <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
              <Badge variant="outline">{selectedOrganization.jurisdictionProfile}</Badge>
              <span>
                {selectedOrganization.workspaces.length} workspace
                {selectedOrganization.workspaces.length === 1 ? '' : 's'}
              </span>
              {selectedOrganization.workspaces[0] && (
                <span>Default: {selectedOrganization.workspaces[0].name}</span>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {selectedOrganization && (
        <>
          {memberships.isLoading || invitations.isLoading ? (
            <SectionSkeleton />
          ) : memberships.error || invitations.error ? (
            <Card>
              <CardContent className="p-6 text-sm text-destructive">
                You can see the organization, but its access lists could not be loaded. The server
                may restrict this management action for your role.
              </CardContent>
            </Card>
          ) : (
            <>
              <MembershipList
                organizationId={selectedOrganization.id}
                memberships={memberships.data ?? []}
              />
              <InvitationPanel
                organizationId={selectedOrganization.id}
                invitations={invitations.data ?? []}
              />
            </>
          )}
        </>
      )}
    </div>
  );
}

function PageHeading() {
  return (
    <div className="flex items-start gap-3">
      <div className="rounded-md border bg-card p-2">
        <Building2 className="size-5 text-muted-foreground" />
      </div>
      <div>
        <h1 className="text-2xl font-semibold tracking-normal">Organizations</h1>
        <p className="text-sm text-muted-foreground">
          Manage tenant boundaries, members, roles, and invitation access.
        </p>
      </div>
    </div>
  );
}

function OrganizationsSkeleton() {
  return (
    <div className="space-y-5">
      <Skeleton className="h-14 w-full max-w-md" />
      <SectionSkeleton />
    </div>
  );
}

function SectionSkeleton() {
  return (
    <div className="space-y-5">
      <Skeleton className="h-40 w-full" />
      <Skeleton className="h-64 w-full" />
      <Skeleton className="h-64 w-full" />
      <Loader2 className="mx-auto size-4 animate-spin text-muted-foreground" />
    </div>
  );
}
