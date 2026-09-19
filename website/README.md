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
VITE_ANDROID_RELEASED=false
```

PingLet AI is live on the [App Store](https://apps.apple.com/us/app/pinglet-ai/id6806096564). The iOS link is defined in `src/App.tsx`; the old `VITE_IOS_RELEASED` and `VITE_APP_STORE_URL` variables are no longer used. Android remains marked coming soon unless explicitly enabled.

Both QR codes encode `https://pinglet.ai/get.html` (using `VITE_PUBLIC_SITE_URL`). Vercel redirects iOS user agents directly to the App Store and Android user agents to `/download?preview` on the website. The built `get.html` is a fallback for other hosts and iPads using a desktop user agent. Desktop visitors also get the download page.

The route must be deployed before scanning from a local preview can use it. QR codes never use a local IP address. When Android launches, change its destination in both Vercel configurations and `get.html` to the Google Play URL, and enable `VITE_ANDROID_RELEASED` for the website's Android button.

Older `/download` QR codes redirect iOS visitors to the App Store and leave Android visitors on the website while unreleased. Use `/download?preview` to inspect the page without redirecting.
