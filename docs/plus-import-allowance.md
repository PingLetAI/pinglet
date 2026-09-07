# Plus AI import allowance

The backend is the authoritative source for the Plus monthly AI import limit.
Configure `PLUS_MONTHLY_AI_IMPORT_LIMIT` in `backend/.env.production`. When unset,
the single backend default is 100. Values must be positive safe integers.

The entitlement response provides:

- `socialImportLimit`: the current user's enforced allowance.
- `plusMonthlyImportLimit`: the Plus allowance, including for Free and guest users.

iOS and Android paywalls use `plusMonthlyImportLimit`, not a hardcoded number.
They can use `socialImportLimit` for an already-Plus account on an older backend;
otherwise they show non-numeric benefit text until configuration is available.
Quota error messages also use the same server values.

The allowance applies to monthly subscribers, annual subscribers, and active
Plus reverse trials. Free and guest limits, existing usage, the monthly counting
window, duplicate-link handling, and subscription prices are unchanged.

## Deployment

Deploy the backend before distributing the updated apps. For environment-only
changes, recreate the backend and worker containers. Clients receive the updated
values on their next entitlement fetch. No database migration is needed.

Future allowance changes do not require app releases once users have installed
the API-driven paywalls. Older app builds can still contain the former fixed copy.

## Store assets

The illustrative settings screenshot source and generator avoid embedding a
numeric quota. Existing exported PNGs, real-device captures, and metadata already
uploaded to App Store Connect or Google Play do not update automatically. Replace
any that show the old allowance before publishing. Do not recreate subscription
products or change prices just to change the import allowance.
