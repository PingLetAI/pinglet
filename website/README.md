# PingLet website

React, TypeScript, Vite, and Tailwind landing site for PingLet.

```bash
npm install
npm run dev
```

Production build:

```bash
npm run build
```

## Vercel

The repository supports both Vercel configurations:

- Repository root: the root `vercel.json` builds `website/` explicitly.
- Root Directory `website`: `website/vercel.json` builds the current directory.

Both configurations rewrite client-side routes such as `/download` to the React
entry point. Using the repository root is recommended for this monorepo.

Set these environment variables in the Vercel project:

```text
VITE_PUBLIC_SITE_URL=https://pinglet.ai
VITE_APP_STORE_URL=
```
