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

Import the repository directly into Vercel. The root `vercel.json` installs and
builds this workspace and rewrites client-side routes such as `/download` to the
React entry point.

Set these environment variables in the Vercel project:

```text
VITE_PUBLIC_SITE_URL=https://pinglet.ai
VITE_APP_STORE_URL=
```
