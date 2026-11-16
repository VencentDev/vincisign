import { Info } from 'lucide-react';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

type PlatformLimitsCardProps = {
  limits: string[];
};

export function PlatformLimitsCard({ limits }: PlatformLimitsCardProps) {
  return (
    <Card className="border-primary/20 bg-primary/[0.03]">
      <CardHeader className="pb-3">
        <div className="flex items-start gap-3">
          <div className="flex size-9 shrink-0 items-center justify-center rounded-md border bg-card">
            <Info className="size-4 text-primary" />
          </div>
          <div className="space-y-1">
            <CardTitle className="text-base">Know the platform limits</CardTitle>
            <CardDescription>
              VinciSign records document, signature, and consent evidence. It does not replace your
              own legal, regulatory, or professional review.
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="pt-0">
        {limits.length ? (
          <ul className="space-y-2 text-sm text-muted-foreground">
            {limits.map((limit) => (
              <li key={limit} className="flex gap-2">
                <span
                  aria-hidden="true"
                  className="mt-2 size-1.5 shrink-0 rounded-full bg-primary"
                />
                <span>{limit}</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm text-muted-foreground">
            The current platform limits will be shown here as they become available.
          </p>
        )}
      </CardContent>
    </Card>
  );
}
