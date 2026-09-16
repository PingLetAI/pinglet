# App Store audit fixes — September 16, 2026

## Implemented locally

- Created a five-screenshot upload set for each iPhone size in `assets/app-store/premium/resubmission/`. Excludes the rejected trial promotion and the old screenshot advertising YouTube. Updated the premium generator and asset instructions to use this selection. Historical files remain outside the upload set.
- Removed the unsupported YouTube claim from the iOS input placeholder.
- Added a readable iOS consent sheet, also used by the share extension, naming OpenAI, the transmitted public-post data, processing purposes, reuse, and Explore publication. Includes privacy/terms links and cancellation. Updated Android’s dialog too.
- Versioned consent as `2026-09-16`. The backend now requires the specific version the client displayed; an empty acceptance request cannot accept a new disclosure. Import creation and background processing both enforce current consent.
- Generalized analysis uses fetched public-post content only. The server ignores legacy `contextText`, new clients stop submitting it, and workers exclude even previously queued `rawText` from AI input and catalog classification. Only analysis marked `public-post-v1` by the corrected worker is eligible for reuse, including reuse across users.
- Anonymous bootstrap cannot recover any existing account or reassign an existing device using an installation identifier. Updated clients start a separate guest session after a collision; users recover verified accounts through email verification. Refresh verifies device ownership. Temporary refresh errors preserve credentials. Session creation is serialized within each client process, and new guest sessions clear cached account data.
- Added required-reason UserDefaults privacy manifests to the app, share extension, and widget. All three bundles use version 1.0 (14). Updated XcodeGen’s source configuration to preserve the existing signing team and portrait orientation and explicitly generate the PingLet scheme.
- Updated website privacy copy to describe public-source analysis and explicit AI consent.

No App Store Connect changes, messages to Apple, deployment, or production data changes were performed.

## Deployment requirements

These changes require a new iOS binary. Build 13 does not contain the native fixes.

1. Coordinate release of iOS build 14, the updated Android client, the website, and both backend API and ingestion worker. Deploy the backend enforcement before enabling imports in the new clients. Older clients cannot accept the revised disclosure and will need to update before importing posts. Existing authenticated sessions remain valid; if credentials are lost or revoked, the user must verify email to recover an account.
2. Stop the old ingestion workers before switching traffic to the new release. An old worker must not continue processing jobs with user-supplied context or the old consent rules. Deploy the API and worker from the same revision. Jobs lacking current consent are marked failed with an instruction to review consent and import again.
3. No database schema migration is introduced by these fixes. The existing consent fields and ingestion analysis JSON carry the new versions. Normal production migration procedures still apply to any pre-existing unapplied migrations.
4. Publish the revised website policy with the release. Verify its production URLs from a device.
5. Review historical data using `backend/scripts/audit-legacy-analysis.sql` (read-only). The new reuse restriction prevents new copying of older analyses; it does not erase analysis already copied or excerpts already published. The script reports content IDs/counts, not private text. Decide remediation after inspecting affected records; no data is deleted automatically.
6. Upload only `premium/resubmission/6.9-inch/` and `premium/resubmission/6.5-inch/` images into the corresponding slots. Remove old screenshot 07 and screenshot 02 from every localization and custom listing where applicable. Compare the retained captures against the submitted build.

## Validation commands

Completed locally:

- Backend API and worker build passed; all 13 security regression tests passed.
- iOS Release simulator build passed for the app and both extensions; inspected all three built bundles for version 1.0 (14) and the privacy manifest.
- All three native session regression checks passed.
- Android `:app:compileDebugKotlin` passed. Existing SDK/plugin compatibility and deprecation warnings remain.
- Website production build passed.
- Screenshot dimensions, opacity, file selection, and checksums validated; shell syntax and `git diff --check` passed.

These checks do not replace signed-device/TestFlight or production integration testing.

```sh
cd backend
npm install
npx prisma generate
npm run test:security
```

The security suite exercises existing-account bootstrap rejection, device protection, concurrent bootstrap conflicts, guest creation, refresh ownership, consent-version enforcement, public-only cross-account reuse, and legacy worker jobs.

From the repository root:

```sh
bash ios/tests/run-session-regressions.sh
xcodegen generate --spec ios/project.yml
xcodebuild -project ios/PingLet.xcodeproj -scheme PingLet \
  -configuration Release -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath /private/tmp/pinglet-audit-derived CODE_SIGNING_ALLOWED=NO build
python3 assets/app-store/premium/prepare-resubmission.py
```

The native session regression harness runs the actual SessionManager/APIClient against intercepted requests and memory-only storage. It checks concurrent startup, installation collision recovery, and preservation of credentials on temporary refresh failures.

## Still requires release verification

- Signed archive validation and TestFlight testing, including iPad compatibility mode, import consent/cancel, email sign-in, deletion, share sheet, purchases/restore, and widget refresh.
- App Store privacy labels, age rating, reviewer access, subscription configuration, and rights to third-party content.
- App Store Connect screenshot replacement and resubmission.
- Production review of pre-existing analyses and catalog excerpts.

Privacy manifests declare required API reasons; they do not replace accurate App Store Connect data-collection disclosures. Apple documents the reasons used here in its [UserDefaults API guidance](https://developer.apple.com/documentation/bundleresources/app-privacy-configuration/nsprivacyaccessedapitypes/nsprivacyaccessedapitype).
