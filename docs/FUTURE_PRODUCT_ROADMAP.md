# PingLet Future Product Roadmap

## Document purpose

This document captures possible future directions for PingLet. It is a product
vision and prioritization guide, not a statement that every feature has been
implemented or committed to a release.

Each release should preserve the core promise:

> PingLet is not merely where people save things. It is where things they do not
> want to forget come back to them.

The Android ambient widget, reliable capture, source preservation, and useful
resurfacing remain the foundation. New features should strengthen that loop
rather than turn PingLet into a generic bookmark manager or attention-driven
social feed.

## Product vocabulary

| Term | Meaning |
| --- | --- |
| Pinglet | A saved idea, quote, takeaway, note, reminder, story, or source worth remembering |
| My Pinglets | The user's private personal library |
| Pinglet Pals | Trusted people who exchange things worth remembering |
| Pal Picks | Pinglets intentionally shared by Pals |
| Pinglet Album | A curated or AI-generated collection presented as a lasting artifact |
| Session | A short, intentional sequence of resurfaced Pinglets |
| Discover | Broader recommendations and clearly labeled promoted content |
| Re-ping | Deliberately schedule a Pinglet to return later |

## Product principles

1. Personal content comes first.
2. The original source and attribution remain attached whenever available.
3. AI should transform content without inventing what the source said.
4. Full transcripts, captions, and OCR should remain accessible behind concise takeaways.
5. Resurfacing should be calm, useful, and finite rather than an endless feed.
6. Social features should optimize for trust and meaning, not follower counts.
7. Sponsored content must always be clearly labeled and separately controlled.
8. Users should be able to export and delete their data.
9. The backend should remain platform-independent for Android, iOS, and web clients.
10. Ongoing AI and media-processing costs must be reflected in product limits.

## Strategic product layers

PingLet can grow as five connected products:

| Layer | User value |
| --- | --- |
| Capture | Save useful ideas from social media, the web, voice, images, and personal notes |
| Memory | Resurface the right saved idea at a useful time |
| Understanding | Search, summarize, connect, and question the personal library |
| Reflection | Turn saved material into albums, recaps, patterns, and actions |
| Exchange | Privately share useful ideas with Pals and discover high-quality creators |

## Horizon 1: Complete the personal memory loop

### Universal capture

- Android share-sheet capture from Instagram, TikTok, Facebook, YouTube, LinkedIn, X, browsers, podcasts, and other apps.
- Paste a public URL directly into PingLet.
- Save manually entered quotes, reminders, affirmations, messages, notes, and stories.
- Capture optional personal context such as "why I saved this."
- Add tags or a collection at save time without requiring organization.
- Queue multiple links while earlier items continue processing in the background.
- Retry failed ingestion jobs without creating duplicate library items.
- Detect previously processed URLs and reuse permitted extraction results.
- Preserve a separate personal save record when multiple users save the same source.
- Detect canonical URLs, reposts, and tracking-parameter variants.
- Display processing states: queued, acquiring, transcribing, analyzing, moderating, ready, and failed.
- Provide actionable failure reasons and retry controls.

### Multimodal ingestion

- Extract post captions and visible text while excluding likes, follower counts, and unrelated engagement metadata.
- Run OCR across images and carousels.
- Extract audio and transcribe speech from videos.
- Sample video frames and run OCR against meaningful frame changes.
- Retain full transcripts with timestamps.
- Retain OCR text with image or frame references.
- Identify speakers when technically reliable and useful.
- Record extraction confidence and let users correct text.
- Generate concise takeaways grounded in the source document.
- Offer an "exact wording" mode for quotations and a separate summarized-takeaway mode.
- Preserve platform, creator attribution, original URL, thumbnail, publication date, and ingestion date.
- Moderate source content and generated takeaways independently.
- Respect platform permissions, access controls, copyright, and content-removal requests.

### Reliable ambient widget

- Compact, standard, and expanded responsive layouts.
- User-controlled text size, opacity, contrast, corner radius, and content density.
- Personal-content priority with system-catalog fallback.
- Local rotation independent from network availability.
- Configurable approximate rotation intervals within Android scheduling constraints.
- Favorite and unfavorite directly from the widget.
- Open the displayed item from any non-heart area.
- Preserve source and favorite state through every rotation.
- Refresh every installed widget after relevant changes.
- Bootstrap immediately when a widget is first added.
- Optional widget themes that adapt to wallpaper and system colors.
- Multiple widgets with independent collections or schedules.
- Widget filters such as Fitness, Work, Family, Faith, or Favorites.
- A "Re-ping later" widget action for deliberate resurfacing.
- Accessibility support for text scaling, contrast, and screen readers.

### Personal library foundation

- Ready, processing, and failed views.
- All Pinglets and Favorites tabs.
- Search by exact text, source, creator, platform, and date.
- Edit extracted text, title, author, notes, and collection.
- Open the original source outside PingLet.
- Archive, delete, restore, and bulk-select.
- Sort by newest, oldest, most resurfaced, most opened, and recently forgotten.
- Offline access to cached personal content.
- Account migration from anonymous use to email or federated sign-in without losing local saves.
- Transparent save and processing limits.

## Horizon 2: Intelligent resurfacing

### Adaptive scheduling

- Replace purely random rotation with relevance-aware selection.
- Use spaced-repetition principles without making the product feel like homework.
- Resurface neglected items that have not appeared recently.
- Reinforce items that users favorite, reopen, or deliberately re-ping.
- Reduce frequency for items repeatedly skipped or dismissed.
- Prevent the same creator, topic, or message from appearing too often.
- Balance personal notes, social saves, favorites, and optional system catalogs.
- Let users choose calm, balanced, or intensive resurfacing modes.

### Contextual resurfacing

- Morning, workday, evening, and weekend schedules.
- Topic schedules such as productivity on weekdays or reflection at night.
- Location-aware suggestions only through explicit opt-in and coarse privacy-preserving rules.
- Calendar-aware resurfacing for relevant preparation or reflection.
- Activity-aware suggestions such as fitness Pinglets before a workout, with explicit permission.
- Seasonal and anniversary resurfacing.
- "You saved this one year ago" rediscovery.
- Temporary focus modes for an exam, business launch, trip, or personal goal.

### User feedback controls

- Show more like this.
- Show less like this.
- Not useful right now.
- Never show on widget.
- Re-ping tomorrow, next week, or on a chosen date.
- Explain why an item resurfaced.
- Reset or tune personalization.

### From inspiration to action

- Detect actionable advice in saved content.
- Offer to convert an idea into a reminder, habit, checklist, or goal.
- Ask before creating schedules or notifications.
- Link every generated action back to the originating Pinglet.
- Track whether a user acted without turning PingLet into a productivity dashboard.
- Generate a weekly "ideas worth acting on" review.

## Horizon 3: Personal knowledge and AI

### Automatic organization

- Suggest dynamic topics such as Business, Money, Fitness, Relationships, Faith, Travel, Recipes, Books, and Life Advice.
- Create collections automatically without moving or hiding the original item.
- Allow one Pinglet to belong to several collections.
- Generate temporary smart collections from natural-language requests.
- Detect entities such as people, books, places, products, and techniques.
- Suggest collection cleanup and merges.
- Preserve user-created organization over AI suggestions.

### Ask My Pinglets

- Answer questions using only the user's authorized library by default.
- Cite every claim back to specific Pinglets and original sources.
- Open the relevant transcript segment, image, or source timestamp.
- Clearly distinguish direct source text from AI synthesis.
- Support questions such as "What have I saved about finding first customers?"
- Compare advice from different creators.
- Produce summaries scoped by topic, collection, creator, or date range.
- Refuse to imply evidence that is not present in the saved library.
- Let users include or exclude Pal Picks and Discover content.
- Provide conversation deletion and retention controls.

### Connections and personal patterns

- "You have heard this idea before" links.
- Similar and related Pinglets.
- Contradicting viewpoints.
- Repeated principles saved from different creators.
- Topic evolution over time.
- Frequently saved creators and source platforms.
- Emerging personal interests.
- Belief maps that remain private by default.
- A review queue for AI-suggested relationships before permanent organization.

### AI-generated sessions and playlists

- "I need motivation today" sessions.
- Five-minute or ten-item topic sessions.
- Exam review, morning focus, workout, reflection, or travel sessions.
- Sessions generated only from the user's own saves.
- Optional inclusion of Pal Picks or system catalogs.
- Text, audio, and hands-free presentation modes.
- Save a generated session for reuse.
- Share a session privately without exposing the full library.

## Horizon 4: Albums, recaps, and lasting artifacts

### Pinglet Albums

- Generate an album from a topic, collection, creator, event, or time period.
- Create a clean cover, introduction, themes, chapters, quotes, and source index.
- Include source thumbnails, creator attribution, personal notes, and QR links.
- Offer editable layouts before export.
- Export to PDF.
- Export image cards for private sharing or social media.
- Publish a private web album with revocable access.
- Collaboratively create albums with Pals.
- Generate print-ready files.
- Explore optional physical printing and fulfillment partnerships.
- Preserve provenance and avoid republishing unlicensed source media.

Example album:

> My Discipline Collection - Summer 2026

- Twelve recurring ideas.
- Eight exact quotes worth remembering.
- Five frequently saved creators.
- Three themes that shaped the period.
- Favorite personal notes and original-source links.

### Monthly and yearly recaps

- "My Month in Ideas."
- "My Year in Pinglets."
- Number of saves, sources, revisits, and actions.
- Dominant topics and changing interests.
- Most frequently saved creators.
- Most revisited Pinglet.
- Valuable items not seen recently.
- A private reflection prompt generated from the period.
- User-approved, shareable recap cards.
- Controls to exclude sensitive collections, topics, or sources.
- No public recap by default.

### Personal storytelling

- Build a narrative from personal notes and selected Pinglets.
- Create life chapters, travel journals, learning journals, and family keepsakes.
- Add photos, voice notes, and reflections.
- Generate a private timeline of ideas and moments.
- Export a personal story without exposing unrelated library content.

## Horizon 5: More ways to create Pinglets

### Voice Pinglets

- Record a thought from the app, widget shortcut, wearable, or assistant action.
- Transcribe the recording while retaining the original audio.
- Derive a concise Pinglet without replacing the exact transcript.
- Detect tasks or reminders and ask whether to schedule them.
- Support reflective voice journals and rapid idea capture.

### Camera Pinglets

- Photograph book pages, handwritten notes, signs, slides, menus, and whiteboards.
- Run OCR and preserve the original image.
- Crop, correct, and confirm extracted text.
- Identify book or document metadata when reliable.
- Create source-aware highlights rather than unattributed quotes.
- Support multi-page scanning.

### Browser and desktop capture

- Browser extensions for major desktop browsers.
- Share extension for iOS.
- Desktop quick-capture application or menu-bar utility.
- Save selected text with page title, author, URL, and context.
- Import from browser bookmarks and read-later services.
- Import from supported exports of other knowledge tools.
- Email-to-PingLet capture for newsletters and forwarded notes.

### Audio and podcast capture

- Save a podcast episode with a timestamp.
- Transcribe only the relevant permitted segment when possible.
- Extract quotations and takeaways with timestamp citations.
- Return users to the original podcast application or web source.

## Horizon 6: Pinglet Pals and private collaboration

### Trusted connections

- Invite Pals by private link, contact, username, or QR code.
- Require mutual acceptance by default.
- Share an individual Pinglet with selected Pals.
- Add a personal note explaining why it was shared.
- Let recipients save a Pal Pick into their own library.
- Preserve attribution to the source and sharing Pal.
- Mute, remove, block, and report controls.
- Granular notification and widget-inclusion preferences.
- No public follower counts.

### Pal Picks

- A finite inbox of things trusted Pals intentionally shared.
- Optional, limited appearance on the ambient widget.
- Clear "From your Pal" labeling.
- Accept, dismiss, save, or open source.
- Quality controls that prevent repeated spam.
- User-controlled limits on Pal content frequency.

### Shared collections

- Collections for couples, families, friends, teams, and communities.
- Example collections: startup ideas, parenting, recipes, books, trips, and places to visit.
- Shared notes and lightweight discussion around a Pinglet.
- Roles for owners, contributors, and viewers.
- Change history and member removal.
- Shared collection search and AI summaries.
- Pal Albums and monthly shared recaps.
- Export when a group ends or a member leaves.

### Privacy model

- My Pinglets remain private unless explicitly shared.
- Sharing one Pinglet never exposes its collection or surrounding library.
- Shared copies retain clear ownership and source provenance.
- Users can revoke private links.
- Sensitive topics can be excluded from all social suggestions.
- Social recommendations must not reveal private inferred interests.

## Horizon 7: Creator ecosystem

### Creator profiles

- Verified creator identity and linked source-platform profiles.
- Public collection of eligible creator Pinglets.
- Original-source links rather than rehosting complete content.
- Topic and language information.
- Creator-controlled corrections and removal requests.
- Clear separation between organic saves and paid distribution.

### High-intent creator analytics

- Number of users who saved eligible creator content.
- Resurfacing frequency.
- Return visits to original sources.
- Saves after resurfacing.
- Repeat creator saves.
- Aggregate topic interest.
- Privacy thresholds that prevent identifying individual users.
- No access to private notes, collections, questions, or widget history.

### Promoted Pinglets

- Require a real eligible source post or video.
- Run promoted material through the normal extraction and moderation pipeline.
- Prevent creators from submitting unsupported ad copy as an extracted insight.
- Label every paid placement as Promoted or Sponsored.
- Keep promoted content separate from My Pinglets unless the user saves it.
- Let users disable promoted content where legally or commercially appropriate.
- Frequency caps and topic controls.
- Campaign budgets, schedules, audiences, and objectives.
- Impression, source-click, and save objectives.
- Quality thresholds based on usefulness, safety, and user feedback.
- Advertiser verification and payment-risk controls.
- Public advertising and content policies.
- Appeals and human review for rejected campaigns.

### Creator business products

- Creator Plus subscription for advanced analytics.
- Campaign management dashboard.
- Curated creator albums.
- Shareable "Most Pingleted" insights.
- Deep links that attribute outbound traffic without exposing personal identity.
- Optional creator APIs and publishing integrations.

## Horizon 8: Platform expansion

### iOS

- Native iOS share extension.
- Home Screen and Lock Screen widgets within Apple's supported capabilities.
- Background processing designed around iOS scheduling constraints.
- Shared backend feed, account, entitlements, and library.
- Apple in-app purchases with platform-specific receipt verification.

### Web

- Full library, processing queue, collections, albums, and search.
- Ask My Pinglets with source citations.
- Creator dashboards and campaign management.
- Secure private album links.
- Account, subscription, export, and deletion controls.
- Responsive progressive web experience without pretending to replace native widgets.

### Wearables and ambient surfaces

- Wear OS complication or tile.
- Watch favorites and re-ping actions.
- Short audio sessions.
- Android tablet and foldable layouts.
- Car and voice surfaces only where distraction and platform rules permit.
- Smart display exploration with strict privacy controls.

### Developer platform

- Import and export APIs.
- User-authorized integrations.
- Webhooks for processing completion.
- A source connector framework with explicit permissions.
- Organization features only after the personal product demonstrates retention.
- Rate limits, scoped tokens, audit logs, and developer policies.

## Monetization opportunities

### Free

- A useful but bounded number of personal saves.
- Core ambient widget.
- Basic manual notes and system-catalog fallback.
- Limited monthly AI processing.
- Basic search and collections.
- Account prompt after meaningful activation rather than before first value.

### PingLet Plus

- Higher or unlimited save count subject to fair-use processing limits.
- Larger transcription and media-processing allowance.
- Advanced widget schedules and multiple widget configurations.
- Ask My Pinglets.
- Smart collections and semantic connections.
- AI-generated sessions.
- Monthly and yearly recaps.
- Album generation and premium exports.
- Advanced resurfacing controls.
- Priority processing where operationally sustainable.

Candidate launch pricing currently under consideration:

| Plan | Candidate price |
| --- | ---: |
| Monthly | USD 1.99 |
| Annual | USD 14.99 |

Pricing should be validated against retention, inference cost, transcription
minutes, storage, app-store fees, regional purchasing power, refunds, and taxes.

### Lifetime purchase

A lifetime option creates risk because transcription, vision, storage, and AI
inference have ongoing costs. If introduced, it should include a clearly defined
fair-use allowance or purchased processing credits rather than an unbounded
promise. It should be implemented as a non-consumable platform purchase and
restored across devices tied to the user's PingLet account.

### Creator revenue

- Creator analytics subscription.
- Clearly labeled promoted Pinglets.
- Campaign spend based on qualified impressions, saves, or outbound clicks.
- Premium creator profile and album tools.
- Optional verification or business tooling where policy permits.

### Physical and export revenue

- Premium album templates.
- High-resolution or print-ready exports.
- Optional printed books or keepsake albums through a fulfillment partner.
- Giftable private collections.

## Trust, safety, legal, and quality capabilities

### Content safety

- Moderation for source documents, generated text, user uploads, and promoted content.
- Policies covering violence, sexual content, hate, harassment, self-harm, misinformation, illegal activity, and exploitation.
- Age-appropriate defaults and controls.
- Reporting, blocking, appeals, and human review.
- Stronger review for paid promotion than private personal saves.

### Source rights and provenance

- Process only content the system is permitted to access.
- Preserve original links and attribution.
- Do not imply ownership of third-party content.
- Minimize rehosting and redistribution of source media.
- Support source removal, creator requests, and broken-link handling.
- Mark exact quotes separately from paraphrased takeaways.
- Preserve timestamps and extraction confidence.
- Track extraction model and prompt versions for auditability.

### Privacy and security

- Private-by-default library.
- Encryption in transit and at rest.
- Secure token storage on devices.
- Account-session management and remote sign-out.
- Data export and complete account deletion.
- Configurable transcript and media retention.
- Separation of reusable source extraction from private user notes and behavior.
- No use of private library content for advertising targeting without explicit consent.
- Least-privilege service access and auditable administrative actions.
- Abuse detection for ingestion, sharing, promotions, and account creation.

### AI quality

- Ground takeaways in transcript, OCR, caption, and user-provided text.
- Prefer exact transcription over creative rewriting.
- Show uncertainty instead of fabricating missing content.
- Allow corrections and report extraction problems.
- Evaluate transcription accuracy across accents, languages, noise, and music.
- Evaluate quote faithfulness and source citation correctness.
- Maintain model fallbacks and cost controls.
- Avoid silently changing user-edited content during reprocessing.

## Technical platform capabilities required

### Ingestion platform

- Redis-backed asynchronous job orchestration.
- Independent API and media-worker scaling.
- Configurable concurrency by workload type.
- Idempotent jobs and safe retries.
- URL canonicalization and source-level deduplication.
- Content hashes for media and extraction reuse.
- FFmpeg media processing.
- OCR, transcription, and vision adapters.
- Model-provider abstraction and per-stage cost accounting.
- Dead-letter queues and operator replay tools.
- Processing-status events for Android, iOS, and web.

### Knowledge platform

- Source documents separated from user-specific save records.
- Structured provenance for caption, transcript, OCR, frames, and takeaways.
- Semantic embeddings and scoped vector search.
- Collection and relationship graphs.
- Citation retrieval down to transcript timestamps and frames.
- User-edited text protected from destructive model updates.
- Data retention and deletion propagation across indexes and caches.

### Resurfacing platform

- Local queue generation for offline widget rotation.
- Server-side ranking that remains optional for basic rotation.
- Selection rules for freshness, diversity, priority, and fatigue.
- Event collection for impressions, favorites, opens, dismissals, and re-pings.
- Privacy-preserving personalization.
- Explainable selection reasons.
- Experiment controls that do not compromise the core personal-content promise.

### Commerce platform

- Google Play purchase-token verification.
- Apple receipt verification for future iOS support.
- Platform-independent internal entitlement records.
- Purchase acknowledgment and reconciliation.
- Real-time developer notifications and subscription lifecycle handling.
- Grace period, account hold, cancellation, refund, and expiration logic.
- Creator billing, campaign budgets, invoices, and fraud controls when introduced.

## Suggested release sequence

| Phase | Scope | Advancement gate |
| --- | --- | --- |
| Foundation | Reliable capture, processing, library, source links, widget rotation | Users repeatedly save and see successfully processed content |
| Memory | Adaptive resurfacing, feedback, smart schedules, actions | Resurfaced items produce meaningful opens, favorites, and re-pings |
| Intelligence | Semantic search, Ask My Pinglets, automatic collections, connections | Answers remain grounded and users return to query their library |
| Reflection | Albums, sessions, monthly and yearly recaps | Users generate, revisit, export, or share private artifacts |
| Pals | Trusted sharing, Pal Picks, shared collections | Exchanges are useful without spam or feed-driven behavior |
| Creators | Profiles, privacy-safe analytics, promoted Pinglets | Personal trust remains strong and creator demand is demonstrated |
| Platform | iOS, richer web, wearables, APIs | Core economics and retention support multi-platform investment |

## Metrics that should guide expansion

### Activation

- Percentage of users who successfully save their first source.
- Time from share to ready Pinglet.
- Percentage who add the widget.
- Percentage who see a meaningful first rotation.

### Retention

- Saves per active user after 7, 30, and 90 days.
- Weekly active savers.
- Widget retention and active widget count.
- Percentage of resurfaced items opened, favorited, or re-pinged.
- Return rate to the library without a notification prompt.

### Quality

- Ingestion success by platform and media type.
- Transcript and OCR correction rate.
- Takeaway rejection or edit rate.
- Duplicate processing avoided.
- Source-link availability.
- Moderation false-positive and appeal rates.

### Economics

- AI and media cost per processed save.
- Storage and delivery cost per active user.
- Free-to-Plus conversion.
- Monthly and annual retention.
- Refund and involuntary-churn rates.
- Creator acquisition cost and qualified outbound value when applicable.

### Social health

- Pal Picks saved versus dismissed.
- Blocks, mutes, and reports per shared item.
- Shared collection retention.
- Sponsored-content hide and complaint rates.
- Whether social content displaces personal content in resurfacing.

## Features to avoid or delay

- An endless general-purpose social feed.
- Public follower counts as a primary status mechanic.
- Unlabeled sponsored content.
- AI answers without source citations.
- Unlimited lifetime AI processing at a fixed one-time price.
- Aggressive notifications that compete with the calm widget experience.
- Rehosting third-party media without clear permission.
- Building every source-platform scraper before proving retention on a smaller supported set.
- Enterprise complexity before the personal product works reliably.
- Features that make saving easier but remembering no better.

## Long-term positioning

PingLet can evolve from a persistent message widget into a personal memory layer
for ideas encountered across social media, the web, the physical world, and the
user's own thoughts.

The strongest long-term narrative is:

> Discover something meaningful. Pinglet it. Let it return when it matters.

Albums, recaps, personal AI, actions, Pals, and creator products should all be
extensions of that narrative. The widget is not a disposable first feature; it
is the ambient surface that makes the broader memory system distinctive.
