import { useEffect } from "react";
import type { ReactNode, SVGProps } from "react";
import { QRCodeSVG } from "qrcode.react";

const nav = [
  ["How it works", "#how-it-works"],
  ["Features", "#features"],
  ["AI details", "#ai-details"],
  ["FAQ", "#faq"],
];

const PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ai.pinglet.app";
const APP_STORE_URL = import.meta.env.VITE_APP_STORE_URL as string | undefined;
const PUBLIC_SITE_URL = (import.meta.env.VITE_PUBLIC_SITE_URL as string | undefined) || "https://pinglet.ai";
const DOWNLOAD_URL = `${PUBLIC_SITE_URL.replace(/\/$/, "")}/download`;
const X_URL = "https://x.com/pingletAI";
const INSTAGRAM_URL = "https://www.instagram.com/PingLet.AI";

const steps = [
  { number: "01", title: "Share it to PingLet", body: "From Instagram, TikTok, or Facebook, choose PingLet in the share sheet. You can also paste a public link or write your own thought." },
  { number: "02", title: "AI reads the whole post", body: "Speech is transcribed, visible text is read from images and video frames, and the caption is combined into one faithful source document." },
  { number: "03", title: "Keep it in view", body: "Processing finishes in the background. The result joins your library and quietly returns through your Home Screen widget." },
];

const features = [
  { icon: "quote", title: "The whole post, understood", body: "PingLet combines captions, verbatim speech, image text, and sampled video frames instead of saving engagement metrics.", tone: "bg-mint" },
  { icon: "heart", title: "Personal always comes first", body: "Things you intentionally save lead your library and widget. Discovery content only fills the spaces you allow.", tone: "bg-blush" },
  { icon: "link", title: "Every source stays attached", body: "Open the original creator and post from a PingLet, its detailed analysis, or an Explore collection.", tone: "bg-[#eee4cb]" },
  { icon: "shield", title: "Process without waiting", body: "Queue multiple links and leave the app. Media processing continues in the background with visible progress and status.", tone: "bg-[#e6e9df]" },
  { icon: "spark", title: "From transcript to insight", body: "See a clear overview, faithful takeaways, themes, evidence-backed insights, and practical actions grounded in the source.", tone: "bg-[#e4eadf]" },
  { icon: "quote", title: "A widget that feels yours", body: "Resize it, favorite from the Home Screen, and shape independent profiles with typography, themes, schedules, and content mixes.", tone: "bg-[#f1e6d7]" },
];

const faqs = [
  ["Can I try PingLet Plus without subscribing?", "Yes. Verified accounts can try every Plus feature free for 7 days with no card and no automatic subscription. When the trial ends, the account returns to Free unless you explicitly choose a paid Google Play plan."],
  ["Does PingLet repost social content?", "PingLet does not rehost the original social video or image. It creates a source-linked personal memory. Moderated, strongly matched insights from eligible public links may also appear anonymously in an Explore catalog; personal notes and the identity of the person who saved a link are not published."],
  ["What does the AI actually extract?", "For supported public posts, PingLet can combine the caption, speech transcript, visible text from images, OCR from sampled video frames, and necessary visual context. It removes likes, shares, follower counts, and platform chrome."],
  ["How does Explore choose content?", "Explore is built from approved public-link extractions rather than filler quotes. AI adds an item only when its extraction quality and semantic match to an active catalog both clear strong confidence thresholds."],
  ["Will the widget change exactly every 30 minutes?", "Android optimizes background work for battery, so rotation is approximately every 30 minutes rather than guaranteed to the exact second."],
  ["What happens when I am offline?", "Your widget continues rotating from a local cache. New imports and account sync resume when a connection returns."],
  ["Which platforms are supported?", "PingLet currently supports eligible public links from Instagram, TikTok, and Facebook. Availability still depends on the source platform permitting access to that specific post."],
];

function Icon({ name, className = "h-6 w-6" }: { name: string; className?: string }) {
  const props: SVGProps<SVGSVGElement> = { className, viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: 1.8, strokeLinecap: "round", strokeLinejoin: "round", "aria-hidden": true };
  if (name === "heart") return <svg {...props}><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z" /></svg>;
  if (name === "link") return <svg {...props}><path d="M10 13a5 5 0 0 0 7.1.1l2-2a5 5 0 0 0-7.1-7.1l-1.1 1.1" /><path d="M14 11a5 5 0 0 0-7.1-.1l-2 2A5 5 0 0 0 12 20l1.1-1.1" /></svg>;
  if (name === "shield") return <svg {...props}><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z" /><path d="m9 12 2 2 4-4" /></svg>;
  if (name === "quote") return <svg {...props}><path d="M3 21c3 0 7-1 7-8V5H3v8h4c0 4-1 5-4 5v3Z" /><path d="M14 21c3 0 7-1 7-8V5h-7v8h4c0 4-1 5-4 5v3Z" /></svg>;
  if (name === "arrow") return <svg {...props}><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></svg>;
  return <svg {...props}><path d="m12 3 1.8 4.4L18 9l-4.2 1.6L12 15l-1.8-4.4L6 9l4.2-1.6L12 3Z" /><path d="m5 16 .8 2.2L8 19l-2.2.8L5 22l-.8-2.2L2 19l2.2-.8L5 16Z" /></svg>;
}

function Logo({ light = false }: { light?: boolean }) {
  return <a href="/" className="flex items-center gap-3" aria-label="PingLet home">
    <img src="/favicon.svg" alt="" className="h-9 w-9" />
    <span className={`text-xl font-bold tracking-[-.04em] ${light ? "text-paper" : "text-ink"}`}>PingLet</span>
  </a>;
}

function SiteFooter() {
  return <footer className="bg-ink py-10 text-paper"><div className="mx-auto flex max-w-7xl flex-col gap-8 px-5 sm:px-8 md:flex-row md:items-center">
    <Logo light />
    <p className="text-sm text-paper/45 md:ml-4">Save what sticks.</p>
    <div className="flex flex-wrap gap-x-6 gap-y-3 text-sm text-paper/55 md:ml-auto">
      <a href="mailto:hello@pinglet.ai" className="hover:text-paper">Contact</a>
      <a href="/privacy" className="hover:text-paper">Privacy</a>
      <a href="/terms" className="hover:text-paper">Terms</a>
      <a href={X_URL} target="_blank" rel="noreferrer" className="hover:text-paper">X</a>
      <a href={INSTAGRAM_URL} target="_blank" rel="noreferrer" className="hover:text-paper">Instagram</a>
    </div>
    <p className="text-xs text-paper/35">© {new Date().getFullYear()} PingLet</p>
  </div></footer>;
}

function LegalSection({ title, children }: { title: string; children: ReactNode }) {
  return <section className="border-t border-ink/12 py-8 sm:grid sm:grid-cols-[14rem_1fr] sm:gap-10">
    <h2 className="text-xl font-semibold tracking-[-.025em]">{title}</h2>
    <div className="mt-4 space-y-4 leading-relaxed text-ink/68 sm:mt-0">{children}</div>
  </section>;
}

function LegalPage({ type }: { type: "privacy" | "terms" }) {
  const privacy = type === "privacy";
  useEffect(() => {
    document.title = `${privacy ? "Privacy Policy" : "Terms of Service"} · PingLet`;
  }, [privacy]);

  return <div className="min-h-screen bg-paper">
    <header className="border-b border-ink/10 bg-paper/95"><div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-5 sm:px-8"><Logo /><a href="/" className="text-sm font-semibold text-ink/55 hover:text-ink">Back to PingLet</a></div></header>
    <main className="page-atmosphere px-5 py-16 sm:px-8 sm:py-24">
      <article className="mx-auto max-w-5xl rounded-[2.5rem] border border-ink/10 bg-paper/90 p-6 shadow-soft backdrop-blur sm:p-10 lg:p-14">
        <p className="eyebrow text-clay">Legal</p>
        <h1 className="mt-4 text-4xl font-semibold tracking-[-.05em] sm:text-6xl">{privacy ? "Privacy Policy" : "Terms of Service"}</h1>
        <p className="mt-5 text-sm text-ink/48">Last updated August 26, 2026</p>
        <p className="mt-8 max-w-3xl text-lg leading-relaxed text-ink/68">{privacy
          ? "PingLet is designed to keep what matters to you while collecting only the information needed to capture, process, sync, and resurface it."
          : "These terms govern your use of the PingLet applications, website, widgets, and related services operated by TinkerPal LLC."}</p>

        <div className="mt-12">
          {privacy ? <>
            <LegalSection title="Information we collect">
              <p><strong>Account information.</strong> We collect your email address, verification status, account identifiers, plan, and subscription entitlement. You may initially use PingLet with an anonymous installation-based account.</p>
              <p><strong>Content you choose to save.</strong> This includes private notes, quotes, reminders, public social-post URLs, optional context, source information, favorites, catalog preferences, and widget profiles.</p>
              <p><strong>Processed media information.</strong> When you submit a supported public link, we may process its available caption, audio transcript, visible text, sampled video frames, OCR results, summaries, themes, and derived takeaways.</p>
              <p><strong>Device and usage information.</strong> We may collect installation ID, device platform, app version, locale, timezone, sync timestamps, content interactions, processing status, and diagnostic information. Google Play provides purchase status and entitlement information; PingLet does not receive your full payment-card details.</p>
            </LegalSection>
            <LegalSection title="How we use information">
              <p>We use information to authenticate accounts, process saves, operate background ingestion, generate source-grounded analysis, personalize and rotate widget content, preserve source links, synchronize devices, provide support, enforce limits, verify subscriptions, prevent abuse, improve reliability, and comply with law.</p>
              <p>AI-generated summaries and insights are produced from material you intentionally submit. Private text you write in PingLet is not used to populate Explore catalogs or advertising profiles.</p>
            </LegalSection>
            <LegalSection title="Explore catalogs">
              <p>Eligible content extracted from a public social-post link may be evaluated for an Explore catalog after automated moderation. PingLet requires strong extraction quality and a high-confidence semantic match before adding an item.</p>
              <p>An Explore item may include a concise excerpt or derived takeaway, creator attribution when available, and the original public source link. We do not display the identity or account information of the PingLet user who submitted the link. Private notes, reminders, and personal stories entered directly in PingLet are not eligible.</p>
              <p>Creators or rights holders may request review or removal by contacting <a className="font-semibold underline" href="mailto:hello@pinglet.ai">hello@pinglet.ai</a>.</p>
            </LegalSection>
            <LegalSection title="Service providers">
              <p>We use vendors that help operate PingLet, including cloud hosting and database providers, OpenAI for transcription, moderation, vision, and structured analysis, Zoho for account email, and Google Play for Android distribution and billing.</p>
              <p>Submitted source links may be requested from the original social platform. Those platforms process requests under their own privacy policies. Vendors receive only information reasonably necessary to provide their services.</p>
            </LegalSection>
            <LegalSection title="How information is shared">
              <p>We do not sell your personal information. We may share information with service providers acting for us, when you direct us to share it, to protect users or the service, to comply with legal obligations, or as part of a merger, financing, acquisition, or sale of assets subject to appropriate safeguards.</p>
            </LegalSection>
            <LegalSection title="Storage and retention">
              <p>Widget content is cached locally so rotation can continue offline. Account and saved-content records are retained while your account is active and as reasonably needed to provide the service. Processing workspaces are temporary; derived records such as transcripts and OCR may remain associated with your saved item.</p>
              <p>You may request account and personal-content deletion by emailing <a className="font-semibold underline" href="mailto:hello@pinglet.ai">hello@pinglet.ai</a>. We may retain limited records where required for security, fraud prevention, billing, dispute resolution, or law.</p>
            </LegalSection>
            <LegalSection title="Security and choices">
              <p>We use technical and organizational safeguards intended to protect information, but no network or storage system can be guaranteed completely secure. Keep account and device access protected.</p>
              <p>You can manage favorites, catalog preferences, widget profiles, subscriptions, and saved items in the app. Depending on where you live, you may have rights to access, correct, delete, or receive a copy of personal information.</p>
            </LegalSection>
            <LegalSection title="Children and international use">
              <p>PingLet is not directed to children under 13, and we do not knowingly collect personal information from children under 13. If local law requires a higher minimum age, that requirement applies.</p>
              <p>Information may be processed in countries other than where you live. We use appropriate measures for international processing where required.</p>
            </LegalSection>
            <LegalSection title="Website and policy changes">
              <p>The current website does not use advertising cookies. Hosting providers may process standard request logs needed for security and delivery. If analytics or advertising technologies are introduced, this policy will be updated as required.</p>
              <p>We may update this policy as PingLet evolves. Material changes will be communicated through the service or website, and the date above will be revised.</p>
            </LegalSection>
          </> : <>
            <LegalSection title="Using PingLet">
              <p>You must be at least 13 and legally able to agree to these terms. If local law requires parental consent or a higher age, you must satisfy those requirements. You are responsible for activity associated with your account and device.</p>
              <p>PingLet lets you save private personal text and submit supported public links for extraction, organization, analysis, discovery, and resurfacing. Source availability, background timing, and platform access are not guaranteed.</p>
            </LegalSection>
            <LegalSection title="Your content and permissions">
              <p>You retain ownership of content you create. You grant TinkerPal LLC a limited, non-exclusive license to host, process, reproduce, and transform submitted material as necessary to operate, secure, and improve the service.</p>
              <p>Eligible excerpts or derived takeaways from public-link submissions may be displayed in source-linked Explore catalogs after moderation and high-confidence classification. Directly entered personal text is not eligible unless you separately choose to publish it through a future sharing feature.</p>
              <p>You must have the right to submit material and must not use PingLet to infringe copyright, privacy, publicity, contractual, or other rights. Saving a link does not transfer ownership of the original post to you or PingLet. Creators and rights holders may request review or removal.</p>
            </LegalSection>
            <LegalSection title="Acceptable use">
              <p>You may not use PingLet to access private or restricted media without permission, bypass platform protections, distribute malware, harass others, exploit children, automate abusive requests, reverse engineer protected parts of the service, interfere with infrastructure, or submit unlawful content.</p>
              <p>We may reject, remove, or stop processing material that violates these terms, our safety requirements, source-platform restrictions, or applicable law.</p>
            </LegalSection>
            <LegalSection title="AI and source services">
              <p>Transcripts, OCR, summaries, catalog classifications, and takeaways can contain errors. Review important material against the original source. PingLet does not provide medical, legal, financial, or other professional advice.</p>
              <p>Third-party services such as Instagram, TikTok, Facebook, Google Play, and linked websites are governed by their own terms. We are not responsible for their content, availability, policy changes, or actions.</p>
            </LegalSection>
            <LegalSection title="Subscriptions and billing">
              <p>PingLet may offer a one-time seven-day reverse trial to eligible verified accounts. This trial requires no payment method, does not create a Google Play subscription, and ends automatically without a charge. Access returns to the Free plan unless you explicitly purchase Plus.</p>
              <p>Paid Android subscriptions are processed by Google Play and renew automatically unless canceled through your Play account before renewal. Prices, taxes, trial terms, and billing periods are shown by Google Play before purchase.</p>
              <p>Refunds and billing disputes are handled under Google Play policies and applicable law. Canceling stops future renewal but does not normally end access before the current paid period expires.</p>
            </LegalSection>
            <LegalSection title="Service changes and termination">
              <p>We may modify, suspend, or discontinue features, limits, integrations, or supported sources. Android controls background execution, so widget rotation times are approximate.</p>
              <p>You may stop using PingLet at any time. We may restrict or terminate access for material violations, abuse, security risk, legal requirements, or nonpayment. Provisions that logically survive termination will remain effective.</p>
            </LegalSection>
            <LegalSection title="Disclaimers and liability">
              <p>To the fullest extent permitted by law, PingLet is provided “as is” and “as available” without warranties of uninterrupted operation, accuracy, fitness for a particular purpose, or non-infringement.</p>
              <p>To the fullest extent permitted by law, TinkerPal LLC will not be liable for indirect, incidental, special, consequential, exemplary, or lost-profit damages. Our aggregate liability relating to the service will not exceed the greater of amounts you paid to PingLet during the preceding 12 months or US $50. Some jurisdictions do not allow certain limitations, so they may not apply to you.</p>
            </LegalSection>
            <LegalSection title="Governing terms">
              <p>These terms are governed by the laws of the State of Ohio, excluding conflict-of-law principles, except where mandatory consumer law provides otherwise. Before filing a claim, contact us and allow 30 days to attempt an informal resolution.</p>
              <p>If any provision is unenforceable, the remaining provisions remain effective. These terms and referenced policies form the agreement between you and TinkerPal LLC regarding PingLet.</p>
            </LegalSection>
          </>}
          <LegalSection title="Contact">
            <p>TinkerPal LLC · Cleveland, Ohio, United States</p>
            <p>Questions, privacy requests, or legal notices: <a className="font-semibold underline" href="mailto:hello@pinglet.ai">hello@pinglet.ai</a>.</p>
            <div className="flex gap-5 pt-2"><a href={X_URL} target="_blank" rel="noreferrer" className="font-semibold underline">X</a><a href={INSTAGRAM_URL} target="_blank" rel="noreferrer" className="font-semibold underline">Instagram</a></div>
          </LegalSection>
        </div>
      </article>
    </main>
    <SiteFooter />
  </div>;
}

function Button({ children, href, secondary = false, light = false }: { children: ReactNode; href: string; secondary?: boolean; light?: boolean }) {
  const style = secondary
    ? light ? "border-paper/25 text-paper hover:bg-paper/10" : "border-ink/15 text-ink hover:bg-white/60"
    : "border-ink bg-ink text-paper hover:bg-sage";
  const external = href.startsWith("http");
  return <a href={href} target={external ? "_blank" : undefined} rel={external ? "noreferrer" : undefined} className={`inline-flex h-12 items-center justify-center gap-2 rounded-full border px-6 text-sm font-semibold transition duration-300 ${style}`}>{children}</a>;
}

function StoreButton({ platform, href, unavailable = false }: { platform: "android" | "ios"; href?: string; unavailable?: boolean }) {
  const content = <>
    <svg viewBox="0 0 24 24" className="h-7 w-7 shrink-0 fill-current" aria-hidden="true">
      {platform === "android"
        ? <path d="m3.3 2.6 10.9 9.3L3.3 21.4c-.5-.4-.8-1-.8-1.7V4.3c0-.7.3-1.3.8-1.7Zm12 10.2 2.8 2.4-11.8 6.6 9-9Zm3.9-3.4c.8.4 1.3 1 1.3 1.6s-.5 1.2-1.3 1.6l-2.4 1.3-2.4-2 2.5-2.1 2.3-.4ZM6.3 2.2l11.8 6.6-2.8 2.4-9-9Z" />
        : <path d="M16.7 13.2c0-2.7 2.2-4 2.3-4.1a5 5 0 0 0-3.9-2.1c-1.7-.2-3.2 1-4.1 1-.9 0-2.2-1-3.6-1-1.8 0-3.5 1.1-4.5 2.7-1.9 3.3-.5 8.2 1.4 10.9.9 1.3 2 2.8 3.5 2.7 1.4-.1 1.9-.9 3.6-.9s2.2.9 3.7.9c1.5 0 2.5-1.4 3.4-2.7a12 12 0 0 0 1.5-3.1 4.8 4.8 0 0 1-3.3-4.3ZM14 5.2A4.8 4.8 0 0 0 15.1 1a4.9 4.9 0 0 0-3.3 1.6 4.5 4.5 0 0 0-1.2 3.3A4.1 4.1 0 0 0 14 5.2Z" />}
    </svg>
    <span className="text-left leading-none"><span className="block text-[.62rem] font-medium uppercase tracking-[.12em] text-current/60">{unavailable ? "Coming soon to" : platform === "android" ? "Get it on" : "Download on the"}</span><span className="mt-1 block text-base font-semibold">{platform === "android" ? "Google Play" : "App Store"}</span></span>
  </>;
  const styles = "flex min-h-16 w-full items-center justify-center gap-3 rounded-2xl border px-5 transition sm:w-auto sm:min-w-48";
  if (unavailable || !href) return <div className={`${styles} cursor-not-allowed border-ink/10 bg-ink/5 text-ink/40`} aria-label="PingLet for iOS is coming soon">{content}</div>;
  return <a href={href} target="_blank" rel="noreferrer" className={`${styles} border-ink bg-ink text-paper hover:-translate-y-0.5 hover:bg-sage`}>{content}</a>;
}

function DownloadChoices({ compact = false }: { compact?: boolean }) {
  return <div className={`grid items-center gap-8 ${compact ? "lg:grid-cols-[auto_1fr]" : "md:grid-cols-[auto_1fr]"}`}>
    <div className="relative mx-auto rounded-[1.7rem] bg-white p-4 shadow-soft">
      <QRCodeSVG value={DOWNLOAD_URL} size={compact ? 164 : 196} level="H" bgColor="#ffffff" fgColor="#1b1b17" marginSize={1} />
      <div className="absolute inset-0 grid place-items-center pointer-events-none"><img src="/favicon.svg" alt="" className="h-11 w-11 rounded-full border-4 border-white" /></div>
    </div>
    <div className={compact ? "text-center lg:text-left" : "text-center md:text-left"}>
      <p className="eyebrow text-sage">Scan to download</p>
      <h3 className="mt-3 text-2xl font-semibold tracking-[-.035em] sm:text-3xl">One code. The right store.</h3>
      <p className="mt-3 max-w-md leading-relaxed text-ink/58">Scan with your phone and PingLet will route you to the right app for your device.</p>
      <div className={`mt-6 flex flex-col gap-3 sm:flex-row ${compact ? "lg:justify-start" : "md:justify-start"} justify-center`}>
        <StoreButton platform="android" href={PLAY_STORE_URL} />
        <StoreButton platform="ios" href={APP_STORE_URL} unavailable={!APP_STORE_URL} />
      </div>
    </div>
  </div>;
}

function DownloadPage() {
  useEffect(() => {
    if (new URLSearchParams(window.location.search).has("preview")) return;
    const agent = navigator.userAgent.toLowerCase();
    if (/android/.test(agent)) window.location.replace(PLAY_STORE_URL);
    if (/iphone|ipad|ipod/.test(agent) && APP_STORE_URL) window.location.replace(APP_STORE_URL);
  }, []);

  return <main className="page-atmosphere min-h-screen px-5 py-8 sm:px-8">
    <div className="mx-auto flex max-w-6xl items-center justify-between"><Logo /><a href="/" className="text-sm font-semibold text-ink/55 transition hover:text-ink">Back to website</a></div>
    <div className="mx-auto grid min-h-[calc(100vh-7rem)] max-w-5xl place-items-center py-14">
      <section className="w-full rounded-[2.5rem] border border-ink/10 bg-paper/85 p-6 shadow-soft backdrop-blur sm:p-10 lg:p-14">
        <div className="mx-auto mb-10 max-w-2xl text-center"><p className="eyebrow text-clay">Get PingLet</p><h1 className="balance mt-4 text-4xl font-semibold tracking-[-.05em] sm:text-6xl">Take what matters with you.</h1><p className="mt-5 text-lg leading-relaxed text-ink/60">Download PingLet and turn what you discover into a personal memory that returns.</p></div>
        <DownloadChoices />
        <p className="mt-9 text-center text-xs text-ink/42">The QR code contains only pinglet.ai/download. Device detection happens securely in your browser.</p>
      </section>
    </div>
  </main>;
}

function WidgetPreview() {
  return <div className="relative mx-auto w-full max-w-[470px]">
    <div className="pulse-ring absolute inset-8 rounded-[3rem] border border-gold/40" />
    <div className="float-slow relative overflow-hidden rounded-[2.5rem] border border-white/10 bg-ink p-5 shadow-soft sm:p-7">
      <div className="flex items-center">
        <div className="flex items-center gap-2"><span className="h-2 w-2 rounded-full bg-gold" /><span className="eyebrow text-paper">PingLet</span></div>
        <button className="ml-auto grid h-11 w-11 place-items-center rounded-full bg-white/5 text-gold" aria-label="Favorite this thought"><Icon name="heart" className="h-5 w-5" /></button>
      </div>
      <div className="flex min-h-64 items-center py-8 sm:min-h-72">
        <blockquote className="editorial balance text-[2rem] leading-[1.08] text-paper sm:text-[2.7rem]">“The ideas that shape you deserve more than a place in your saved folder.”</blockquote>
      </div>
      <div className="h-0.5 w-9 rounded bg-gold" />
      <div className="mt-4 flex items-center justify-between gap-4 text-[.68rem] font-bold tracking-[.15em] text-paper/55">
        <span>A THOUGHT WORTH KEEPING</span><span className="text-gold">SOURCE SAVED</span>
      </div>
    </div>
    <div className="absolute -bottom-6 -left-4 rounded-2xl border border-ink/10 bg-paper px-4 py-3 text-xs font-semibold shadow-xl sm:-left-12"><span className="mr-2 inline-block h-2 w-2 rounded-full bg-sage" />Rotates quietly, even offline</div>
  </div>;
}

function App() {
  const path = window.location.pathname.replace(/\/$/, "") || "/";
  if (path === "/download") return <DownloadPage />;
  if (path === "/privacy") return <LegalPage type="privacy" />;
  if (path === "/terms") return <LegalPage type="terms" />;
  return <div id="top" className="overflow-hidden">
    <header className="glass fixed inset-x-0 top-0 z-50 border-b border-ink/8">
      <div className="mx-auto flex h-18 max-w-7xl items-center px-5 sm:px-8">
        <Logo />
        <nav className="mx-auto hidden items-center gap-8 md:flex" aria-label="Primary navigation">
          {nav.map(([label, href]) => <a key={href} href={href} className="text-sm font-medium text-ink/65 transition hover:text-ink">{label}</a>)}
        </nav>
        <a href="/download" className="ml-auto rounded-full bg-ink px-5 py-2.5 text-sm font-semibold text-paper transition hover:bg-sage">Download app</a>
      </div>
    </header>

    <main>
      <section className="page-atmosphere relative min-h-screen pt-32 sm:pt-40">
        <div className="mx-auto grid max-w-7xl items-center gap-16 px-5 pb-24 sm:px-8 lg:grid-cols-[1.08fr_.92fr] lg:pb-32">
          <div>
            <div className="reveal mb-7 inline-flex items-center gap-2 rounded-full border border-ink/10 bg-white/55 px-4 py-2 text-xs font-semibold"><Icon name="spark" className="h-4 w-4 text-clay" />AI memory for what you discover</div>
            <h1 className="reveal reveal-delay-1 balance max-w-3xl text-[3.5rem] font-semibold leading-[.94] tracking-[-.065em] sm:text-[5.2rem] lg:text-[6.2rem]">Save it once.<br /><span className="editorial font-normal italic text-clay">Meet it again.</span></h1>
            <p className="reveal reveal-delay-2 balance mt-8 max-w-xl text-lg leading-relaxed text-ink/65 sm:text-xl">PingLet reads the posts you care about, keeps their source and meaning, and brings the best parts back through a quiet Home Screen memory.</p>
            <div className="reveal reveal-delay-3 mt-9 flex flex-wrap gap-3">
              <Button href="/download">Download app <Icon name="arrow" className="h-4 w-4" /></Button>
              <Button href="#how-it-works" secondary>See how it works</Button>
            </div>
            <div className="mt-10 flex flex-wrap items-center gap-x-6 gap-y-3 text-xs font-semibold text-ink/50"><span>ANDROID FIRST</span><span className="h-1 w-1 rounded-full bg-gold" /><span>PERSONAL SAVES LEAD</span><span className="h-1 w-1 rounded-full bg-gold" /><span>SOURCE PRESERVED</span></div>
          </div>
          <div className="reveal reveal-delay-2 py-10 lg:py-0"><WidgetPreview /></div>
        </div>
      </section>

      <section className="border-y border-ink/10 bg-ink py-7 text-paper">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-5 px-5 text-center sm:px-8 md:flex-row md:text-left">
          <p className="editorial text-xl text-paper/80">From the endless scroll to something that stays.</p>
          <div className="flex items-center gap-7 text-xs font-bold tracking-[.15em] text-paper/45"><span>INSTAGRAM</span><span>TIKTOK</span><span>FACEBOOK</span></div>
        </div>
      </section>

      <section id="how-it-works" className="bg-paper py-24 sm:py-32">
        <div className="mx-auto max-w-7xl px-5 sm:px-8">
          <div className="max-w-2xl"><p className="eyebrow text-clay">A better save button</p><h2 className="balance mt-4 text-4xl font-semibold tracking-[-.045em] sm:text-6xl">Three steps from discovery to memory.</h2></div>
          <div className="mt-16 grid border-y border-ink/12 md:grid-cols-3">
            {steps.map((step, index) => <article key={step.number} className={`relative py-9 md:px-9 md:py-12 ${index > 0 ? "border-t border-ink/12 md:border-l md:border-t-0" : ""}`}>
              <span className="editorial text-5xl text-gold">{step.number}</span><h3 className="mt-10 text-xl font-semibold">{step.title}</h3><p className="mt-3 leading-relaxed text-ink/60">{step.body}</p>
            </article>)}
          </div>
        </div>
      </section>

      <section id="features" className="bg-[#efece3] py-24 sm:py-32">
        <div className="mx-auto max-w-7xl px-5 sm:px-8">
          <div className="flex flex-col justify-between gap-6 md:flex-row md:items-end"><div><p className="eyebrow text-sage">Built for remembering</p><h2 className="balance mt-4 max-w-3xl text-4xl font-semibold tracking-[-.045em] sm:text-6xl">Less noise. More of what mattered.</h2></div><p className="max-w-sm leading-relaxed text-ink/60">A calm layer between what you discover and what you actually carry forward.</p></div>
          <div className="mt-14 grid gap-4 md:grid-cols-2">
            {features.map((feature, index) => <article key={feature.title} className={`${feature.tone} rounded-[2rem] border border-ink/8 p-7 sm:p-10 ${index === 0 || index === 3 ? "md:min-h-80" : "md:min-h-64"}`}>
              <div className="grid h-12 w-12 place-items-center rounded-full bg-paper/80"><Icon name={feature.icon} /></div><h3 className="mt-12 text-2xl font-semibold tracking-[-.025em]">{feature.title}</h3><p className="mt-3 max-w-lg leading-relaxed text-ink/62">{feature.body}</p>
            </article>)}
          </div>
        </div>
      </section>

      <section id="ai-details" className="bg-paper py-24 sm:py-32">
        <div className="mx-auto grid max-w-7xl gap-14 px-5 sm:px-8 lg:grid-cols-[.88fr_1.12fr] lg:items-center">
          <div>
            <p className="eyebrow text-clay">Inside a PingLet</p>
            <h2 className="balance mt-4 text-4xl font-semibold tracking-[-.05em] sm:text-6xl">Not just a summary. A faithful way back in.</h2>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-ink/62">PingLet keeps the original words familiar on your widget, then gives you deeper context when you open the saved post. Everything remains grounded in the extracted source.</p>
            <div className="mt-8 inline-flex items-center gap-2 rounded-full bg-ink px-4 py-2 text-xs font-bold tracking-[.12em] text-paper"><Icon name="spark" className="h-4 w-4 text-gold" />COMPLETE DETAILS WITH PINGLET PLUS</div>
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            {[
              ["Full transcript", "Speech retained as closely as possible to the original wording."],
              ["Visible text", "OCR from images, carousels, and sampled video frames."],
              ["Complete summary", "A concise overview plus a comprehensive account of the whole post."],
              ["Insights & evidence", "Key ideas explained with evidence grounded in the source."],
              ["Takeaways & themes", "Memorable points and recurring subjects, without engagement noise."],
              ["Practical actions", "Useful next steps supported by what the post actually says."],
            ].map(([title, body], index) => <article key={title} className={`rounded-[1.7rem] border border-ink/8 p-6 ${index === 0 || index === 5 ? "bg-mint" : index === 2 ? "bg-blush" : "bg-[#f0ede4]"}`}>
              <span className="editorial text-3xl text-gold">0{index + 1}</span>
              <h3 className="mt-7 text-lg font-semibold">{title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-ink/60">{body}</p>
            </article>)}
          </div>
        </div>
      </section>

      <section className="bg-sage py-24 text-paper sm:py-32">
        <div className="mx-auto grid max-w-7xl gap-14 px-5 sm:px-8 lg:grid-cols-[1.05fr_.95fr] lg:items-center">
          <div>
            <p className="eyebrow text-mint">Explore what people found worth keeping</p>
            <h2 className="balance mt-4 text-4xl font-semibold tracking-[-.05em] sm:text-6xl">Collections shaped by real discoveries.</h2>
            <p className="mt-6 max-w-2xl text-lg leading-relaxed text-paper/68">Explore is not a bank of filler quotes. Eligible public-link insights enter a collection only after moderation, strong extraction quality, and a high-confidence semantic match. Every item keeps its original source.</p>
            <div className="mt-8 flex flex-wrap gap-2 text-sm font-semibold">
              {["Discipline", "Business mindset", "Confidence", "Morning focus", "Fitness", "Drive"].map((catalog) => <span key={catalog} className="rounded-full border border-paper/18 bg-paper/8 px-4 py-2">{catalog}</span>)}
            </div>
          </div>
          <div className="rounded-[2.4rem] border border-paper/15 bg-paper/8 p-5 shadow-soft backdrop-blur sm:p-8">
            <div className="rounded-[1.7rem] bg-paper p-6 text-ink sm:p-8">
              <div className="flex items-center justify-between"><span className="eyebrow text-clay">Business mindset</span><span className="rounded-full bg-mint px-3 py-1 text-xs font-bold text-sage">SOURCE LINKED</span></div>
              <p className="editorial mt-10 text-3xl leading-tight sm:text-4xl">“Build around a problem people already feel, not a solution you still have to explain.”</p>
              <div className="mt-10 border-t border-ink/10 pt-5"><p className="text-sm font-semibold">Why it belongs here</p><p className="mt-2 text-sm leading-relaxed text-ink/56">Strongly matched to entrepreneurship and customer-problem discovery from an approved public source.</p></div>
            </div>
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden bg-ink py-24 text-paper sm:py-32">
        <div className="absolute -right-40 top-1/2 h-[40rem] w-[40rem] -translate-y-1/2 rounded-full border border-gold/15" /><div className="absolute -right-16 top-1/2 h-[26rem] w-[26rem] -translate-y-1/2 rounded-full border border-paper/10" />
        <div className="relative mx-auto grid max-w-7xl gap-16 px-5 sm:px-8 lg:grid-cols-2 lg:items-center">
          <div><p className="eyebrow text-gold">Always within a glance</p><h2 className="editorial balance mt-5 text-5xl leading-[1.02] sm:text-7xl">A thought can stay without asking for your attention.</h2><p className="mt-7 max-w-xl text-lg leading-relaxed text-paper/60">No endless quote carousel. One saved idea lives quietly on your Home Screen until the next one takes its place.</p></div>
          <div className="rounded-[2.5rem] border border-paper/12 bg-paper/5 p-5 backdrop-blur sm:p-8"><div className="rounded-[1.8rem] bg-[#20211e] p-7"><div className="flex items-center gap-2"><span className="h-2 w-2 rounded-full bg-gold" /><span className="eyebrow">PingLet</span><Icon name="heart" className="ml-auto h-5 w-5 text-gold" /></div><p className="editorial my-16 text-4xl leading-tight">Small ideas become part of us by returning, not by being filed away.</p><div className="h-0.5 w-10 bg-gold" /><p className="mt-4 text-xs font-bold tracking-[.14em] text-paper/45">YOUR PERSONAL MEMORY</p></div></div>
        </div>
      </section>

      <section id="faq" className="bg-mint/60 py-24 sm:py-32"><div className="mx-auto grid max-w-7xl gap-14 px-5 sm:px-8 lg:grid-cols-[.75fr_1.25fr]"><div><p className="eyebrow text-sage">Good to know</p><h2 className="mt-4 text-4xl font-semibold tracking-[-.045em] sm:text-6xl">Questions, answered quietly.</h2></div><div className="border-t border-ink/15">{faqs.map(([question, answer]) => <details key={question} className="group border-b border-ink/15 py-1"><summary className="flex cursor-pointer list-none items-center justify-between gap-6 py-6 text-lg font-semibold"><span>{question}</span><span className="grid h-8 w-8 shrink-0 place-items-center rounded-full border border-ink/15 transition group-open:rotate-45">+</span></summary><p className="max-w-2xl pb-7 pr-10 leading-relaxed text-ink/60">{answer}</p></details>)}</div></div></section>

      <section id="download" className="bg-gold px-5 py-20 sm:px-8 sm:py-28"><div className="mx-auto max-w-6xl"><div className="grid gap-12 lg:grid-cols-[.8fr_1.2fr] lg:items-center"><div className="text-center lg:text-left"><img src="/favicon.svg" alt="" className="mx-auto h-16 w-16 lg:mx-0" /><h2 className="balance mt-7 text-4xl font-semibold tracking-[-.05em] sm:text-5xl">Keep the things that move you moving.</h2><p className="mt-5 text-lg text-ink/65">Download PingLet and build a calmer memory for what you discover.</p></div><div className="rounded-[2rem] bg-paper/92 p-6 shadow-soft sm:p-8"><DownloadChoices compact /></div></div></div></section>
    </main>

    <SiteFooter />
  </div>;
}

export default App;
