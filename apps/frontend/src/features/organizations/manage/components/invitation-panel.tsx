'use client';

import { Check, Clipboard, Loader2, Mail, XCircle } from 'lucide-react';
import { useState } from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from '@/components/ui/use-toast';
import {
  useCreateInvitation,
  useRevokeInvitation,
} from '@/features/organizations/manage/api/organizations.hooks';
import {
  assignableOrganizationRoles,
  organizationRoleLabel,
  organizationStatusLabel,
} from '@/features/organizations/manage/lib/organization-role-label';

import type {
  Invitation,
  OrganizationRole,
} from '@/features/organizations/manage/api/organizations.hooks';

export function InvitationPanel({
  organizationId,
  invitations,
}: {
  organizationId: string;
  invitations: Invitation[];
}) {
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<OrganizationRole>('ORGANIZATION_MEMBER');
  const [createdToken, setCreatedToken] = useState<string | null>(null);
  const createInvitation = useCreateInvitation();
  const revokeInvitation = useRevokeInvitation();

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedEmail = email.trim();

    if (!normalizedEmail) {
      toast({ title: 'Invitee email is required', variant: 'destructive' });
      return;
    }

    try {
      const invitation = await createInvitation.mutateAsync({
        organizationId,
        body: { email: normalizedEmail, role },
      });
      setEmail('');
      setCreatedToken(invitation.token ?? null);
      toast({ title: 'Invitation created' });
    } catch (error) {
      toast({
        title: 'Could not create invitation',
        description: error instanceof Error ? error.message : 'Please try again.',
        variant: 'destructive',
      });
    }
  }

  async function revoke(invitation: Invitation) {
    if (!window.confirm(`Revoke the invitation for ${invitation.email}?`)) {
      return;
    }

    try {
      await revokeInvitation.mutateAsync({ organizationId, invitationId: invitation.id });
      toast({ title: 'Invitation revoked' });
    } catch (error) {
      toast({
        title: 'Could not revoke invitation',
        description: error instanceof Error ? error.message : 'The server rejected this change.',
        variant: 'destructive',
      });
    }
  }

  async function copyToken() {
    if (!createdToken) {
      return;
    }

    await navigator.clipboard.writeText(createdToken);
    toast({ title: 'Token copied' });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Invitations</CardTitle>
        <CardDescription>
          The API returns the invitation token once. Share it securely with the invited account.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-5">
        <form
          className="grid gap-3 sm:grid-cols-[minmax(0,1fr)_13rem_auto] sm:items-end"
          onSubmit={submit}
        >
          <div className="space-y-2">
            <Label htmlFor="invitation-email">Email address</Label>
            <Input
              id="invitation-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="member@company.com"
              maxLength={255}
              disabled={createInvitation.isPending}
            />
          </div>
          <div className="space-y-2">
            <Label>Organization role</Label>
            <Select
              value={role}
              onValueChange={(value) => setRole(value as OrganizationRole)}
              disabled={createInvitation.isPending}
            >
              <SelectTrigger aria-label="Invitation role">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {assignableOrganizationRoles.map((roleOption) => (
                  <SelectItem key={roleOption} value={roleOption}>
                    {organizationRoleLabel(roleOption)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <Button type="submit" disabled={createInvitation.isPending}>
            {createInvitation.isPending ? (
              <Loader2 className="mr-2 size-4 animate-spin" />
            ) : (
              <Mail className="mr-2 size-4" />
            )}
            Invite
          </Button>
        </form>

        {createdToken && (
          <div className="rounded-md border border-amber-500/40 bg-amber-500/10 p-4">
            <div className="flex items-start gap-3">
              <Check className="mt-0.5 size-4 shrink-0 text-amber-700 dark:text-amber-300" />
              <div className="min-w-0 flex-1 space-y-2">
                <p className="text-sm font-medium">One-time invitation token</p>
                <p className="break-all font-mono text-xs text-muted-foreground">{createdToken}</p>
                <Button type="button" variant="outline" size="sm" onClick={() => void copyToken()}>
                  <Clipboard className="mr-2 size-4" />
                  Copy token
                </Button>
              </div>
            </div>
          </div>
        )}

        <div className="space-y-3">
          {invitations.length === 0 ? (
            <p className="rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground">
              No invitations have been created yet.
            </p>
          ) : (
            invitations.map((invitation) => (
              <div
                key={invitation.id}
                className="flex flex-col gap-3 rounded-md border p-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div className="min-w-0">
                  <p className="break-all text-sm font-medium">{invitation.email}</p>
                  <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                    <Badge variant={invitation.status === 'PENDING' ? 'outline' : 'secondary'}>
                      {organizationStatusLabel(invitation.status)}
                    </Badge>
                    <span>{organizationRoleLabel(invitation.role)}</span>
                    <span>Expires {new Date(invitation.expiresAt).toLocaleDateString()}</span>
                  </div>
                </div>
                {invitation.status === 'PENDING' && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => void revoke(invitation)}
                    disabled={revokeInvitation.isPending}
                  >
                    {revokeInvitation.isPending ? (
                      <Loader2 className="mr-2 size-4 animate-spin" />
                    ) : (
                      <XCircle className="mr-2 size-4" />
                    )}
                    Revoke
                  </Button>
                )}
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}
