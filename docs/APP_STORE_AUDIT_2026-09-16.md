# PingLet AI — App Store and source audit

Follow-up: local implementation is documented in [App Store fixes](APP_STORE_FIXES_2026-09-16.md). The findings below describe the original audit state, before those changes.

Reviewed September 16, 2026 against the supplied rejection for version 1.0 (13), submission `34c8f7f0-4fbb-4d7f-9ffa-0b63d943e781`.

Scope: visual inspection of `reported-issue.png` and premium screenshots 01–06; inspection of screenshot generators, native iOS app/extensions, and relevant backend authentication, extraction, subscription, deletion, and website policy code. This is a static audit, not a device test or penetration test. App Store Connect configuration, production configuration/data, and the uploaded archive were not available. No application code or submission assets were changed. Dependencies are not installed locally; builds and automated tests were not run.

## 1. Confirmed rejection: pricing in screenshot 07 — immediate action

Apple’s attachment matches `assets/app-store/premium/07-plus-1290x2796.png`. It contains “Free for 7 days” in the headline and payment/trial language inside the captured app: “free for 7 days,” “No payment required,” and “TRY PLUS FREE.” The surrounding copy also promotes “No card. No automatic charge.” Changing only the headline leaves the underlying issue visible.

The generator reproduces this at `assets/app-store/premium/generate-premium-assets.sh:104`. The 6.5-inch counterpart is `assets/app-store/premium/6.5-inch/07-plus-1284x2778.png`. The premium README currently instructs uploading all seven images.

Recommended immediate correction: remove screenshot 07 from every uploaded size and localization, including custom product pages or experiments if used. A seventh screenshot is unnecessary. Alternatively, replace it with an authentic feature screen without pricing/trial promotions. Do not merely erase text from the captured app UI.

The older asset set also has “7 DAYS FREE” in `assets/app-store/source/04-insights.svg:25` and its generator at `assets/app-store/generate-assets.sh:124`; avoid uploading its rendered insights PNG unchanged. Update the generators and asset instructions when implementing the fix so future regeneration cannot reintroduce the issue.

Premium screenshots 01–06 have no visible pricing promotion for PingLet in the inspected images. Screenshot 06 contains “work for free” within a content quotation; that is not a claim about PingLet pricing. Screenshot 02 has a separate accuracy issue described below.

Apple’s message concerns listing metadata. It does not instruct removal of truthful prices or trial disclosures from the running app. Trial/price information can be described in the app description as Apple’s message suggests. This metadata correction alone can use the same build; code changes for the additional findings require a new build.

Sources: [Accurate metadata rules](https://developer.apple.com/app-store/review/guidelines/#accurate-metadata), [Resubmitting after a metadata rejection](https://developer.apple.com/help/app-store-connect/manage-submissions-to-app-review/reply-to-app-review-messages).

## 2. High, conditional: extra input text can contaminate shared public-post analysis

Product clarification: analysis is generalized analysis of the public post, not personal analysis. Reusing that generalized analysis is intentional and is not itself a privacy issue. This finding concerns an implementation path that accepts additional text despite that intended behavior; it does not establish that users actually submit private context or that a production leak has occurred.

Evidence:

- `ios/PingLet/Features/AddPingLetView.swift:23` sends text remaining beside the URL as `contextText`; this can be ordinary share-sheet text or text entered into the editor.
- `backend/src/ingestion/ingestion.processor.ts:61` inserts user-supplied context into the source document, and line 96 derives analysis from that combined document.
- `backend/src/ingestion/ingestion.service.ts:28` finds reusable extractions across all users by URL.
- Lines 79–81 copy the original source document, takeaways, and analysis into another user’s ingestion.
- `backend/src/ingestion/ingestion.service.ts:156` and `backend/src/content/content.service.ts:73` return the copied derived content to that user.

If user A adds personal context to a public URL and the analysis includes it, user B importing the same URL can receive that context in the derived results. The raw source document is copied internally; it is not directly returned by the ingestion presentation method. The same combined document also enters catalog classification, so public excerpts need examination.

Fix aligned with the intended product: derive reusable analysis solely from fetched public source material. Exclude unverified text accompanying the URL from the AI source document and public catalog derivation, enforcing this on the backend as well as the client. This preserves cross-user reuse without introducing a personal-analysis feature. Review existing cached records only where extra input text was included.

Verification needed: controlled two-account regression test with a distinctive private marker in A’s context; no marker or derived private facts may reach B’s response or Explore. Do not test with real private data.

## 3. High: installation identifier can authenticate a verified account

`backend/src/auth/auth.controller.ts:56` exposes anonymous bootstrap without authentication. `backend/src/auth/auth.service.ts:24` looks up an existing user by the supplied installation ID and issues a session without checking whether that user is anonymous. Email verification in `backend/src/auth/email-otp.service.ts:83` upgrades the same user without clearing that installation ID.

Someone who obtains a verified account’s installation ID can therefore request fresh credentials without its email OTP. The ID is random, so this is not a claim that arbitrary accounts can be guessed; the problem is treating an identifier as sufficient proof of account ownership. The identifier is stored in shared UserDefaults on iOS.

Fix: never mint a verified-account session from installation ID alone. Require valid refresh credentials or email verification, and deliberately handle the device/user association when bootstrapping a new anonymous session.

Verification needed: after upgrading a test account, anonymous bootstrap with its old installation ID must not return that account’s credentials.

## 4. High review risk: third-party AI consent is not explicit

`ios/PingLet/Features/AddPingLetView.swift:105` presents a Terms/Explore agreement but does not identify OpenAI or clearly explain transmission to a third-party AI provider. The share extension uses the same view. The backend sends source text including any extra input text for moderation/analysis and media for AI processing. The website privacy policy names OpenAI, but the import confirmation does not.

Before first AI import, disclose the provider, transmitted data, and purpose; obtain affirmative permission before transmission. Include accessible privacy/terms links and cancellation. Record a versioned consent and require it again for users whose previous agreement lacked this disclosure. Keep ordinary text saves usable without AI consent.

This is an additional risk, not an issue named in Apple’s rejection. [Apple’s data use rules](https://developer.apple.com/app-store/review/guidelines/#data-use-and-sharing) require explicit permission for sharing personal data with third-party AI.

## 5. Medium: YouTube is advertised but unsupported

`ios/PingLet/Features/AddPingLetView.swift:76` advertises YouTube in its placeholder, also visible in premium screenshot 02. The backend rejects it: `backend/src/ingestion/ingestion.service.ts:181` permits Instagram, TikTok, and Facebook HTTPS links only; the media runner has the same restriction.

Remove YouTube from the UI and recapture the screenshot, or implement and verify support before advertising it. For a metadata-only resubmission, omit screenshot 02 while this mismatch remains. Do not claim YouTube support elsewhere in the listing.

## 6. Medium: privacy manifest missing from this checkout

No `PrivacyInfo.xcprivacy` file or project reference was found. `ios/PingLet/Core/SharedStore.swift:5` uses UserDefaults and is compiled into the app, widget, and share extension.

Add the appropriate required-reason declarations to the relevant bundles and verify their presence in the archived product. Evaluate the app-group UserDefaults reason and the standard-defaults fallback against actual use. Because build 13 reached review, inspect that archive before assuming it has exactly the same omission as this checkout.

Source: [Apple TN3183](https://developer.apple.com/documentation/technotes/tn3183-adding-required-reason-api-entries-to-your-privacy-manifest).

## 7. Medium: release configuration can regress on regeneration

`ios/project.yml:12` specifies build 9, while the generated project sets the main app to build 13 and retains project-level build 9. The generated project, Info.plists, and entitlements are untracked in the current working tree. Regeneration can lose manual release settings or create version mismatches.

Reconcile the generation source with the intended release configuration and check app/extension versions in the next archive. Do not increment the binary solely to remove a listing screenshot.

## Additional verification before release

- Account deletion is implemented in native settings and the authenticated backend, including email-code verification. The older public-launch checklist is stale; its unchecked boxes are not proof these features are absent. Exercise deletion and local widget-cache clearing on a test account.
- StoreKit product loading, restore, renewal disclosure, privacy/terms links, server-side Apple signature verification, and transaction ownership checks are present. Verify purchases, restore, expiration, refunds, and server notifications in the actual sandbox configuration. Presence of code does not establish that products or production feature flags are configured.
- Explore reporting, source hiding, automated moderation, and administrative report handling exist. Verify removal propagation and operational handling of reports.
- The media runner downloads third-party public media using yt-dlp. Verify platform permissions and retain evidence of rights for extracted/re-published content. Public accessibility alone does not establish authorization. This is an unresolved documentation/permissions question, not a proven infringement. See [Apple’s intellectual property rules](https://developer.apple.com/app-store/review/guidelines/#intellectual-property).
- Test the submitted build on iPad compatibility mode, including share sheet, keyboard, purchasing, and deletion. The project targets iPhone; Apple testing it on an iPad is not itself a targeting error.
- The first widget screenshot includes a large unrelated Screen Time widget with empty data. A cleaner authentic capture would communicate PingLet better. This is a presentation improvement, not the reported rejection.
- App Store privacy labels, age rating, reviewer access, listing text, live legal URLs, and every uploaded localization still need comparison against production behavior.

## Resubmission steps and reply

1. Remove or replace screenshot 07 in every affected listing variant; avoid the older insights asset with its free-trial CTA.
2. Omit or replace screenshot 02 because of the unsupported YouTube claim. Premium 01, 03, 04, 05, and 06 are candidates for the remaining set, subject to matching the submitted build and content rights.
3. Inspect the final uploaded images, including text inside device frames. Check listing name, subtitle, keywords, and previews for pricing references too.
4. Resolve the high-priority privacy/security findings before public release. These were not assessed by Apple in the supplied message.
5. Resubmit the corrected metadata with the same binary if no binary changes are made; submit a new build if implementing native fixes.

Suggested reply, only after the uploaded metadata is actually corrected:

> Hello App Review team, thank you for the feedback. We removed the promotional screenshot containing free-trial and payment references and reviewed the remaining screenshots and metadata for pricing references. The updated screenshots focus on PingLet’s features and functionality. Please review the updated submission for Guideline 2.3.7. Thank you.

This reply has not been sent. No changes have been made in App Store Connect.
