export const qk = {
  me: () => ['me'] as const,
  onboarding: {
    status: () => ['onboarding', 'status'] as const,
  },
  organizations: {
    all: () => ['organizations'] as const,
    detail: (organizationId: string) => ['organizations', organizationId] as const,
    memberships: (organizationId: string) =>
      ['organizations', organizationId, 'memberships'] as const,
    invitations: (organizationId: string) =>
      ['organizations', organizationId, 'invitations'] as const,
  },
  todos: {
    all: () => ['todos'] as const,
    lists: () => ['todos', 'list'] as const,
    list: (filters: { status?: string; page?: number }) => ['todos', 'list', filters] as const,
    detail: (id: string) => ['todos', 'detail', id] as const,
  },
};
