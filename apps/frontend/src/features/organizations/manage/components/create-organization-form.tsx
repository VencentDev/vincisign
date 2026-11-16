'use client';

import { Loader2, Plus } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from '@/components/ui/use-toast';
import { useCreateOrganization } from '@/features/organizations/manage/api/organizations.hooks';

export function CreateOrganizationForm() {
  const [name, setName] = useState('');
  const createOrganization = useCreateOrganization();

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedName = name.trim();

    if (!normalizedName) {
      toast({
        title: 'Organization name is required',
        variant: 'destructive',
      });
      return;
    }

    try {
      await createOrganization.mutateAsync({ name: normalizedName });
      setName('');
      toast({ title: 'Organization created' });
    } catch (error) {
      toast({
        title: 'Could not create organization',
        description: error instanceof Error ? error.message : 'Please try again.',
        variant: 'destructive',
      });
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Create an organization</CardTitle>
        <CardDescription>
          Start a new tenant boundary with a Philippines jurisdiction profile and default workspace.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="flex flex-col gap-3 sm:flex-row sm:items-end" onSubmit={submit}>
          <div className="flex-1 space-y-2">
            <Label htmlFor="organization-name">Organization name</Label>
            <Input
              id="organization-name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Acme Philippines"
              maxLength={160}
              disabled={createOrganization.isPending}
            />
          </div>
          <Button type="submit" disabled={createOrganization.isPending}>
            {createOrganization.isPending ? (
              <Loader2 className="mr-2 size-4 animate-spin" />
            ) : (
              <Plus className="mr-2 size-4" />
            )}
            Create
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
