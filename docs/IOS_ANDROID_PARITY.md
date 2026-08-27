# PingLet iOS parity contract

PingLet Android is the canonical product specification. iOS code must not change product rules or backend contracts without first tracing the corresponding Android implementation.

## Confirmed foundation behavior

- API base URL: `https://api.pinglet.ai`.
- A stable installation ID creates an anonymous session using platform, timezone, locale, and app version metadata.
- Access and refresh tokens are persisted securely. A request receives one retry after a `401`: refresh first, then create a new anonymous session only if refresh fails.
- Email OTP verification can return replacement credentials for an existing account. When it does, local account-scoped data is cleared before the new session is installed.
- Typed text uses `POST /api/v1/me/content`; public links use asynchronous ingestion through `POST /api/v1/me/ingestions`.
- The first public-link submission requires acceptance of the current Terms version. A server `TERMS_ACCEPTANCE_REQUIRED` response reopens the same consent gate.
- `ACCOUNT_REQUIRED` opens account connection. `UPGRADE_REQUIRED` and `SOCIAL_IMPORT_LIMIT` open the reverse-trial or paid-plan path according to server entitlement fields.
- External shares dismiss PingLet after the ingestion is queued. A manually opened add flow returns to its previous in-app destination.
- The processing queue refreshes every three seconds. Newly `READY` ingestions trigger a feed refresh once per ingestion ID.
- Entitlements, limits, trial eligibility, expiry, and paid-plan availability are server-authoritative.
- The reverse trial is seven days, requires no payment method, never starts a subscription, and falls back to Free.
- Paid purchase controls are hidden when `paidPlansEnabled` is false.

## Confirmed widget persistence

Each widget has an independent profile containing name, theme, content mode, catalog IDs, schedule, typography, text scale, spacing, opacity, manual-next state, and current displayed content. The default interval is 30 minutes. Slot calculation is epoch-based: `floor(timestamp / interval)` and the next boundary is the next exact interval.

## Required iOS equivalents

- Android DataStore account secrets -> Keychain.
- Android Room/DataStore widget cache -> App Group storage shared with WidgetKit and the Share Extension.
- Android share intent -> native Share Extension.
- Android WorkManager -> background `URLSession`, `BGTaskScheduler` where permitted, and foreground refresh.
- Google Play Billing -> StoreKit 2, with the same backend-verified entitlement outcome.

## Platform differences requiring explicit verification

WidgetKit controls timeline execution and does not guarantee an exact 30-minute refresh. iOS should precompute 30-minute timeline entries and request reloads after state changes, preserving ordering and content selection while documenting that display timing is system-budgeted.

## Not yet traced

Home, Library, Explore, detailed Settings, content-detail presentation, full widget selection/rendering, notifications, deletion UI, and StoreKit verification remain pending Android code-path review. They must not be inferred from this document.
