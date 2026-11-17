'use client';

import Link from 'next/link';
import { CheckCircle2, Loader2, Ticket } from 'lucide-react';
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from '@/components/ui/use-toast';
import { useAcceptInvitation } from '@/features/organizations/manage/api/organizations.hooks';

export function AcceptInvitationPageContent() {
  const searchParams = useSearchParams();
  const [token, setToken] = useState('');
  const [acceptedOrganizationId, setAcceptedOrganizationId] = useState<string>();
  const acceptInvitation = useAcceptInvitation();

  useEffect(() => {
    const queryToken = searchParams.get('token');
    if (queryToken) {
      setToken(queryToken);
    }
  }, [searchParams]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedToken = token.trim();

    if (!normalizedToken) {
      toast({ title: 'Invitation token is required', variant: 'destructive' });
      return;
    }

    try {
      const membership = await acceptInvitation.mutateAsync(normalizedToken);
      setAcceptedOrganizationId(membership.organizationId);
      toast({ title: 'Invitation accepted' });
    } catch (error) {
      toast({
        title: 'Could not accept invitation',
        description:
          error instanceof Error
            ? error.message
            : 'The token may be expired, revoked, or intended for another account.',
        variant: 'destructive',
      });
    }
  }

  return (
    <div className="mx-auto flex min-h-[calc(100svh-8rem)] w-full max-w-lg items-center">
      <Card className="w-full">
        <CardHeader>
          <div className="mb-2 flex size-10 items-center justify-center rounded-md border bg-card">
            {acceptedOrganizationId ? (
              <CheckCircle2 className="size-5 text-emerald-600" />
            ) : (
              <Ticket className="size-5 text-muted-foreground" />
            )}
          </div>
          <CardTitle>
            {acceptedOrganizationId ? 'Invitation accepted' : 'Accept invitation'}
          </CardTitle>
          <CardDescription>
            {acceptedOrganizationId
              ? 'Your account now has access to the organization.'
              : 'Paste the one-time invitation token shared by the organization administrator.'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {acceptedOrganizationId ? (
            <Button asChild className="w-full">
              <Link href="/organizations">Open organizations</Link>
            </Button>
          ) : (
            <form className="space-y-4" onSubmit={submit}>
              <div className="space-y-2">
                <Label htmlFor="invitation-token">Invitation token</Label>
                <Input
                  id="invitation-token"
                  value={token}
                  onChange={(event) => setToken(event.target.value)}
                  placeholder="Paste token"
                  autoComplete="off"
                  disabled={acceptInvitation.isPending}
                />
              </div>
              <Button type="submit" className="w-full" disabled={acceptInvitation.isPending}>
                {acceptInvitation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
                Accept invitation
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
