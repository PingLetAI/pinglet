# PingLet Public Launch Checklist

This checklist tracks the work required before making PingLet publicly accessible through Google Play. The current decision is to wait until Google Play subscriptions are configured and tested before publishing.

## 1. Resolve launch blockers

### Account deletion

- [ ] Add an authenticated backend endpoint that permanently deletes an account.
- [ ] Delete or anonymize associated personal content, imports, favorites, devices, sessions, tokens, events, and entitlement records.
- [ ] Add a clearly visible **Delete account and data** action under Android account settings.
- [ ] Require confirmation and appropriate reauthentication before deletion.
- [ ] Add an external deletion page at `https://pinglet.ai/delete-account`.
- [ ] Allow users to request deletion from the website without reinstalling the app.
- [ ] Add the external deletion URL to the Google Play Data Safety form.
- [ ] Document any legally required data retention in the Privacy Policy.

Google Play requires an in-app deletion path and an external deletion resource whenever an app supports account creation, even when creating an account is optional.

Reference: <https://support.google.com/googleplay/android-developer/answer/13327111>

### Explore and public content safety

PingLet Explore displays content derived from user-submitted public links. Treat this as user-generated content for launch compliance.

- [ ] Require acceptance of Terms of Use before the first public-link submission.
- [ ] Define prohibited and objectionable content in the Terms of Use.
- [ ] Add **Report content** to Explore and content-detail screens.
- [ ] Allow users to hide or block a source or creator.
- [ ] Store reports in the backend with status, reason, reporter, and timestamps.
- [ ] Add an administrator moderation queue.
- [ ] Define moderation response targets and escalation procedures.
- [ ] Filter sexual, violent, hateful, abusive, illegal, and otherwise restricted content by default.
- [ ] Prevent paid promotion from encouraging objectionable content.
- [ ] Document catalog eligibility and removal policies.

Fastest alternative for the first release:

- [ ] Keep all user submissions private.
- [ ] Disable cross-user Explore content until reporting, blocking, and moderation are complete.

Reference: <https://support.google.com/googleplay/android-developer/answer/9876937>

## 2. Complete Google Play subscriptions

### Payments profile and product

- [ ] Complete the Google Play merchant/payments profile.
- [ ] Confirm the payments profile belongs to the correct legal entity.
- [ ] Create the subscription product ID `pinglet_plus`.
- [ ] Add a monthly base plan at USD 1.99.
- [ ] Add an annual base plan at USD 14.99.
- [ ] Configure regional availability and local prices.
- [ ] Activate both base plans.
- [ ] Ensure benefit descriptions exactly match functionality in the app.
- [ ] Clearly disclose renewal frequency and automatic renewal.
- [ ] Confirm Google Play subscription pause, grace period, and account-hold settings.

### Billing implementation

- [ ] Query the `pinglet_plus` product successfully from a Play-installed build.
- [ ] Confirm monthly and annual offers map to the correct base-plan IDs.
- [ ] Launch the Google Play purchase sheet successfully.
- [ ] Send purchase tokens to the PingLet backend.
- [ ] Validate purchases with Google Play on the backend.
- [ ] Acknowledge purchases after successful backend verification.
- [ ] Store purchase status, product ID, expiry, and renewal state.
- [ ] Handle active, canceled, expired, grace-period, paused, and account-hold states.
- [ ] Implement **Restore purchases**.
- [ ] Confirm entitlements synchronize across reinstalls and devices.
- [ ] Confirm expired subscriptions return to Free without deleting saved content.
- [ ] Configure Real-time Developer Notifications before scaling.
- [ ] Test purchase replacement and duplicate notification handling.

### Reverse trial

- [ ] Keep **Try PingLet Plus - 7 Days Free** separate from Google Play billing.
- [ ] Require a verified email account.
- [ ] Permit only one reverse trial per PingLet account.
- [ ] Require no card or payment method.
- [ ] Create no automatic Google Play subscription.
- [ ] Return the account to Free automatically at expiration.
- [ ] Preserve saved content and widget configuration after expiration.
- [ ] Show clear trial status and remaining time.
- [ ] Show an ending-soon notice without implying an automatic charge.
- [ ] Confirm paid entitlement takes precedence over trial entitlement.

## 3. Finish the release code

- [ ] Commit all approved Home, widget-refresh, trial, website, and backend changes.
- [ ] Exclude `.env`, keystores, passwords, screenshots, and local artifacts from Git.
- [ ] Increment `versionCode` above every previously uploaded bundle.
- [ ] Set an appropriate public `versionName`.
- [ ] Upgrade `compileSdk` and `targetSdk` to API 36 before the August 31, 2026 requirement takes effect.
- [ ] Keep `applicationId = "ai.pinglet.app"`.
- [ ] Confirm release builds use `https://api.pinglet.ai`.
- [ ] Remove development-only logging and endpoints.
- [ ] Ensure production OTPs are never printed to logs.
- [ ] Ensure API keys, JWT secrets, SMTP credentials, and admin credentials are not bundled in Android.
- [ ] Build a signed release Android App Bundle.
- [ ] Protect and back up the upload keystore and credentials.

Target API reference: <https://support.google.com/googleplay/android-developer/answer/11926878>

## 4. Deploy the production backend

```bash
cd ~/pinglet
git pull origin main
./deploy.sh
```

- [ ] Run Prisma migrations before exposing an app version that depends on them.

```bash
docker compose -f docker-compose.prod.yml exec -T backend npx prisma migrate deploy
```

- [ ] Confirm all containers are healthy.

```bash
docker compose -f docker-compose.prod.yml ps
curl -i https://api.pinglet.ai/api/v1/catalogs
```

- [ ] Confirm PostgreSQL is healthy.
- [ ] Confirm Redis is healthy and reachable from backend and worker containers.
- [ ] Confirm the NestJS API is healthy.
- [ ] Confirm the ingestion worker is healthy.
- [ ] Confirm the OpenAI key is valid.
- [ ] Confirm Zoho OTP delivery from `hello@pinglet.ai`.
- [ ] Use strong production JWT and admin secrets.
- [ ] Confirm HTTPS and certificate renewal.
- [ ] Add API and ingestion rate limits.
- [ ] Configure automated PostgreSQL backups and test restoration.
- [ ] Add health and uptime monitoring.
- [ ] Confirm logs do not expose OTPs, tokens, API keys, or private content.

## 5. Test the signed release build

Test the release build installed through Google Play, not only a debug APK.

### Account and entitlement flows

- [ ] Fresh anonymous installation.
- [ ] Anonymous save and AI-import limits.
- [ ] Email OTP request and verification.
- [ ] Invalid, expired, and reused OTP behavior.
- [ ] Seven-day Plus reverse-trial activation.
- [ ] Reverse trial creates no Google Play subscription.
- [ ] Trial expiration returns to Free.
- [ ] Monthly subscription purchase.
- [ ] Annual subscription purchase.
- [ ] Canceled subscription behavior.
- [ ] Expired subscription behavior.
- [ ] Restore purchases after reinstall.
- [ ] Account deletion.

### Capture and ingestion

- [ ] Instagram share-sheet capture.
- [ ] TikTok share-sheet capture.
- [ ] Facebook share-sheet capture.
- [ ] Manual URL submission.
- [ ] Plain personal thought submission.
- [ ] Background processing after leaving the add screen.
- [ ] Queue progress and retry behavior.
- [ ] Unavailable or private source behavior.
- [ ] Failed transcription behavior.
- [ ] Duplicate-link extraction reuse.
- [ ] Quota enforcement.
- [ ] Exact-source text retention.

### Library and discovery

- [ ] Library synchronization.
- [ ] Favorite and unfavorite behavior.
- [ ] Detailed summary, insights, takeaways, OCR, caption, and transcript.
- [ ] Original source links.
- [ ] Explore catalog loading for anonymous and verified accounts.
- [ ] Catalog classification confidence thresholds.
- [ ] Reporting and blocking.
- [ ] Moderation removal propagation.

### Widget

- [ ] Widget appears in supported launchers.
- [ ] Widget addition triggers bootstrap and synchronization.
- [ ] Widget remains populated while the app is closed.
- [ ] Approximate 30-minute local rotation.
- [ ] Offline rotation.
- [ ] Widget resizing.
- [ ] Immediate customization updates.
- [ ] Favorite control reliability.
- [ ] Manual show-another control.
- [ ] Multiple independent widget profiles.
- [ ] Premium feature fallback when entitlement expires.
- [ ] Deep-link to the displayed content item.

### Failure and compatibility testing

- [ ] Backend unavailable.
- [ ] Redis unavailable.
- [ ] Worker unavailable.
- [ ] Slow network.
- [ ] Offline startup.
- [ ] Token expiration and refresh.
- [ ] App upgrade without data loss.
- [ ] Multiple devices on one account.
- [ ] Samsung One UI launcher.
- [ ] Google Pixel launcher.
- [ ] At least one low-memory Android device.

## 6. Add production monitoring

- [ ] Add Android crash reporting.
- [ ] Add Android non-fatal error reporting.
- [ ] Track anonymous authentication failures.
- [ ] Track OTP delivery and verification failures without logging codes.
- [ ] Track trial offer, activation, expiration, and conversion.
- [ ] Track Play purchase start, completion, cancellation, and backend verification failures.
- [ ] Track ingestion success rate by source platform.
- [ ] Track ingestion queue delay and processing time.
- [ ] Track widget bootstrap and rotation failures.
- [ ] Alert on backend, PostgreSQL, Redis, worker, SMTP, and OpenAI failures.
- [ ] Create a support and incident-response process.

## 7. Prepare the Google Play Store listing

Under **Grow users -> Store presence -> Main store listing**:

- [ ] App name: `PingLet`.
- [ ] Accurate short description.
- [ ] Accurate full description.
- [ ] Production 512 x 512 app icon.
- [ ] Production 1024 x 500 feature graphic.
- [ ] Phone screenshots showing Home, widget, share-sheet capture, library, saved details, and Explore if enabled.
- [ ] App category.
- [ ] Support email: `hello@pinglet.ai`.
- [ ] Website: `https://pinglet.ai`.
- [ ] Privacy policy: `https://pinglet.ai/privacy`.
- [ ] Terms: `https://pinglet.ai/terms`.
- [ ] Account deletion: `https://pinglet.ai/delete-account`.
- [ ] Confirm every advertised feature exists in the submitted build.
- [ ] Avoid unsupported claims about exact widget timing or universal lock-screen support.

Store listing guidance: <https://support.google.com/googleplay/android-developer/answer/15191715>

## 8. Complete Play Console declarations

Under **Policy and programs -> App content**:

- [ ] Privacy policy.
- [ ] Ads declaration.
- [ ] App access and reviewer credentials.
- [ ] Target audience and content.
- [ ] Content rating questionnaire.
- [ ] Data Safety form.
- [ ] Account deletion URL.
- [ ] Any news, government, health, financial, or advertising-ID declarations shown by Play Console.

### Data Safety review

Accurately evaluate and disclose:

- [ ] Email addresses.
- [ ] User-created content and personal notes.
- [ ] Submitted social URLs.
- [ ] Extracted captions, transcripts, OCR, summaries, and insights.
- [ ] App interactions and analytics events.
- [ ] Installation or device identifiers.
- [ ] Diagnostics and crash information.
- [ ] Data processed by OpenAI and infrastructure providers.
- [ ] Encryption in transit.
- [ ] Account and associated-data deletion.
- [ ] Data retention and sharing practices.

Data Safety reference: <https://support.google.com/googleplay/android-developer/answer/10787469>

## 9. Provide Google reviewer access

- [ ] Create a dedicated reviewer account such as `play-review@pinglet.ai`.
- [ ] Provide a reusable fixed reviewer OTP restricted to that account, or mailbox credentials that allow retrieval of OTP messages.
- [ ] Ensure access works regardless of reviewer location.
- [ ] Ensure credentials remain valid throughout review.
- [ ] Give clear English instructions for signing in.
- [ ] Explain how to start the free reverse trial.
- [ ] Explain how to add a link and inspect processing.
- [ ] Explain how to access Plus-gated details.
- [ ] Explain how to add and test the widget.

Enter these details under **Policy and programs -> App content -> App access**.

Reviewer access reference: <https://support.google.com/googleplay/android-developer/answer/15748846>

## 10. Satisfy production-access requirements

### New personal developer accounts

- [ ] Complete app setup.
- [ ] Create a closed-testing release.
- [ ] Recruit at least 12 testers.
- [ ] Keep at least 12 testers continuously opted in for 14 consecutive days.
- [ ] Collect meaningful feedback.
- [ ] Fix important issues discovered during testing.
- [ ] Open **Dashboard -> Apply for production**.
- [ ] Complete the closed-test, app-value, and production-readiness questions.
- [ ] Wait for production-access approval.

Testing requirement reference: <https://support.google.com/googleplay/android-developer/answer/14151465>

### Organization developer accounts

- [ ] Confirm organization and developer verification are complete.
- [ ] Complete every Dashboard setup item.
- [ ] Confirm the Production track is enabled.

## 11. Create the production release

Navigate to **Test and release -> Production -> Create new release**.

- [ ] Confirm Google Play App Signing is enabled.
- [ ] Upload the signed `app-release.aab`.
- [ ] Confirm the package name is `ai.pinglet.app`.
- [ ] Confirm the version code is higher than every previously uploaded bundle.
- [ ] Add clear release notes.
- [ ] Resolve every blocking error.
- [ ] Review all warnings.
- [ ] Select distribution countries and regions.
- [ ] Save the release.
- [ ] Preview and confirm.
- [ ] Send changes for review.

For a worldwide launch, select all supported countries. A safer initial release is a smaller set of supported English-speaking countries, followed by expansion after stability is confirmed.

Release reference: <https://support.google.com/googleplay/android-developer/answer/9859348>

## 12. Configure publishing behavior

- [ ] Turn Managed publishing off if approval should publish automatically.
- [ ] Turn Managed publishing on if launch should wait for manual confirmation after approval.
- [ ] Submit the listing, policy declarations, countries, and release together.
- [ ] Monitor review messages and policy notices.
- [ ] Respond quickly to rejected or incomplete declarations.

Publishing reference: <https://support.google.com/googleplay/android-developer/answer/9859751>

## 13. Launch-day operations

- [ ] Confirm the Play Store listing is publicly visible.
- [ ] Install from the public listing on a clean device.
- [ ] Confirm anonymous authentication works.
- [ ] Confirm OTP email delivery works.
- [ ] Complete a real subscription purchase and entitlement verification.
- [ ] Complete a real public-link ingestion.
- [ ] Add and verify the widget.
- [ ] Monitor backend and worker logs.
- [ ] Monitor crashes and ANRs.
- [ ] Monitor reviews and support email.
- [ ] Monitor OpenAI and infrastructure costs.
- [ ] Monitor report/moderation queues.
- [ ] Keep a rollback or emergency-disable plan for ingestion, Explore, trials, and purchases.

## Recommended execution order

1. Complete and test Google Play subscriptions.
2. Implement account deletion in Android, backend, and website.
3. Implement Explore reporting, blocking, terms acceptance, and moderation, or temporarily disable public Explore.
4. Add reusable Google reviewer access.
5. Upgrade Android to API 36.
6. Finish monitoring and production hardening.
7. Commit and push outstanding work.
8. Deploy and migrate the backend.
9. Test the signed release build end to end.
10. Increment the Android version and build the signed AAB.
11. Complete the Play Store listing and policy declarations.
12. Complete closed testing if required.
13. Submit the Production release.
14. Monitor launch health, feedback, retention, and costs.

