'use client';

import { Loader2, UserMinus } from 'lucide-react';

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
import { toast } from '@/components/ui/use-toast';
import {
  useRevokeMembership,
  useUpdateMembership,
} from '@/features/organizations/manage/api/organizations.hooks';
import {
  assignableOrganizationRoles,
  organizationRoleLabel,
  organizationStatusLabel,
} from '@/features/organizations/manage/lib/organization-role-label';

import type {
  Membership,
  OrganizationRole,
} from '@/features/organizations/manage/api/organizations.hooks';

export function MembershipList({
  organizationId,
  memberships,
}: {
  organizationId: string;
  memberships: Membership[];
}) {
  const updateMembership = useUpdateMembership();
  const revokeMembership = useRevokeMembership();

  async function changeRole(membershipId: string, role: OrganizationRole) {
    try {
      await updateMembership.mutateAsync({ organizationId, membershipId, role });
      toast({ title: 'Role updated' });
    } catch (error) {
      toast({
        title: 'Could not update role',
        description: error instanceof Error ? error.message : 'The server rejected this change.',
        variant: 'destructive',
      });
    }
  }

  async function revoke(membership: Membership) {
    if (!window.confirm(`Revoke access for ${membership.email}?`)) {
      return;
    }

    try {
      await revokeMembership.mutateAsync({ organizationId, membershipId: membership.id });
      toast({ title: 'Membership revoked' });
    } catch (error) {
      toast({
        title: 'Could not revoke membership',
        description: error instanceof Error ? error.message : 'The server rejected this change.',
        variant: 'destructive',
      });
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Members</CardTitle>
        <CardDescription>
          Organization access is enforced by the API. Owners cannot be reassigned or revoked.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {memberships.length === 0 ? (
          <p className="rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground">
            No memberships have been created yet.
          </p>
        ) : (
          <div className="space-y-3">
            {memberships.map((membership) => {
              const isOwner = membership.role === 'ORGANIZATION_OWNER';
              const isBusy = updateMembership.isPending || revokeMembership.isPending;

              return (
                <div
                  key={membership.id}
                  className="flex flex-col gap-3 rounded-md border p-3 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0">
                    <p className="break-all text-sm font-medium">{membership.email}</p>
                    <div className="mt-1 flex flex-wrap items-center gap-2">
                      <Badge variant={membership.status === 'ACTIVE' ? 'secondary' : 'outline'}>
                        {organizationStatusLabel(membership.status)}
                      </Badge>
                      {isOwner && <Badge>Owner</Badge>}
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                    {isOwner ? (
                      <span className="text-sm text-muted-foreground">Organization owner</span>
                    ) : (
                      <>
                        <Select
                          value={membership.role}
                          onValueChange={(value) =>
                            void changeRole(membership.id, value as OrganizationRole)
                          }
                          disabled={isBusy}
                        >
                          <SelectTrigger
                            className="w-full sm:w-52"
                            aria-label={`Role for ${membership.email}`}
                          >
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {assignableOrganizationRoles.map((role) => (
                              <SelectItem key={role} value={role}>
                                {organizationRoleLabel(role)}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => void revoke(membership)}
                          disabled={isBusy}
                        >
                          {revokeMembership.isPending ? (
                            <Loader2 className="mr-2 size-4 animate-spin" />
                          ) : (
                            <UserMinus className="mr-2 size-4" />
                          )}
                          Revoke
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
