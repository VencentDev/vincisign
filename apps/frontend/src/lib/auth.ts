import NextAuth from 'next-auth';
import Credentials from 'next-auth/providers/credentials';
import GitHub from 'next-auth/providers/github';
import Google from 'next-auth/providers/google';

export const protectedHomePath = '/todos';

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  'http://localhost:8080';

const githubProvider =
  process.env.AUTH_GITHUB_ID && process.env.AUTH_GITHUB_SECRET
    ? GitHub({
        clientId: process.env.AUTH_GITHUB_ID,
        clientSecret: process.env.AUTH_GITHUB_SECRET,
        authorization: {
          params: {
            scope: 'read:user user:email',
          },
        },
      })
    : null;

export const { handlers, auth, signIn, signOut } = NextAuth({
  providers: [
    Google({
      clientId: process.env.AUTH_GOOGLE_ID!,
      clientSecret: process.env.AUTH_GOOGLE_SECRET!,
      authorization: {
        params: {
          scope: 'openid profile email',
          prompt: 'consent',
          access_type: 'offline',
          response_type: 'code',
        },
      },
    }),
    ...(githubProvider ? [githubProvider] : []),
    Credentials({
      id: 'credentials',
      name: 'Email and password',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        const email = typeof credentials?.email === 'string' ? credentials.email.trim() : '';
        const password = typeof credentials?.password === 'string' ? credentials.password : '';

        if (!email || !password) {
          return null;
        }

        const response = await fetch(`${apiBaseUrl}/api/v1/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password }),
          cache: 'no-store',
        });

        if (!response.ok) {
          return null;
        }

        const payload: unknown = await response.json();
        if (!isRecord(payload) || typeof payload.accessToken !== 'string') {
          return null;
        }

        const profile = isRecord(payload.user) ? payload.user : payload;
        const userEmail = typeof profile.email === 'string' ? profile.email : email;
        const userId = typeof profile.id === 'string' ? profile.id : userEmail;
        const displayName =
          typeof profile.name === 'string'
            ? profile.name
            : typeof profile.displayName === 'string'
              ? profile.displayName
              : userEmail;

        return {
          id: userId,
          email: userEmail,
          name: displayName,
          accessToken: payload.accessToken,
          provider: 'credentials',
        };
      },
    }),
  ],
  pages: {
    signIn: '/login',
  },
  session: { strategy: 'jwt' },
  callbacks: {
    async jwt({ token, account, user }) {
      if (account) {
        token.accessToken = account.access_token ?? token.accessToken;
        token.provider = account.provider;
      }

      if (user?.accessToken) {
        token.accessToken = user.accessToken;
        token.provider = user.provider ?? 'credentials';
      }

      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken;
      session.provider = token.provider;
      return session;
    },
  },
});

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
