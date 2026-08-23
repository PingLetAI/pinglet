import { useEffect } from "react";
import type { ReactNode, SVGProps } from "react";
import { QRCodeSVG } from "qrcode.react";

const nav = [
  ["How it works", "#how-it-works"],
  ["Features", "#features"],
  ["FAQ", "#faq"],
];

const PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=ai.pinglet.app";
const APP_STORE_URL = import.meta.env.VITE_APP_STORE_URL as string | undefined;
const PUBLIC_SITE_URL = (import.meta.env.VITE_PUBLIC_SITE_URL as string | undefined) || "https://pinglet.ai";
const DOWNLOAD_URL = `${PUBLIC_SITE_URL.replace(/\/$/, "")}/download`;

const steps = [
  { number: "01", title: "Share what resonates", body: "Send a public TikTok, Instagram, or Facebook post to PingLet, or write your own thought." },
  { number: "02", title: "We keep the meaning", body: "PingLet extracts the caption, visible text, and spoken words while preserving the original source." },
  { number: "03", title: "It finds you again", body: "A quiet Home Screen widget keeps one thought nearby, then rotates when the moment is right." },
];

const features = [
  { icon: "quote", title: "Meaning, not metrics", body: "Captions, speech, and visible words are retained. Likes, shares, and engagement noise are left behind.", tone: "bg-mint" },
  { icon: "heart", title: "Personal first", body: "Your own saves always lead the rotation. Curated ideas only fill the gaps.", tone: "bg-blush" },
  { icon: "link", title: "Source preserved", body: "Every imported thought keeps a path back to the original creator and post.", tone: "bg-[#eee4cb]" },
  { icon: "shield", title: "Quietly reliable", body: "Processing happens in the background. Rotation continues from your local cache, even offline.", tone: "bg-[#e6e9df]" },
];

const faqs = [
  ["Does PingLet repost social content?", "No. PingLet creates a private personal memory from links you choose to save and always preserves the original source."],
  ["Will the widget change exactly every 30 minutes?", "Android optimizes background work for battery, so rotation is approximately every 30 minutes rather than guaranteed to the exact second."],
  ["What happens when I am offline?", "Your widget continues rotating from a local cache. New imports and account sync resume when a connection returns."],
  ["Which platforms are supported?", "PingLet is designed for public links shared from Instagram, TikTok, Facebook, and the wider web. Availability can depend on source permissions."],
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
  return <a href="#top" className="flex items-center gap-3" aria-label="PingLet home">
    <img src="/favicon.svg" alt="" className="h-9 w-9" />
    <span className={`text-xl font-bold tracking-[-.04em] ${light ? "text-paper" : "text-ink"}`}>PingLet</span>
  </a>;
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
  if (window.location.pathname.replace(/\/$/, "") === "/download") return <DownloadPage />;
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
            <div className="reveal mb-7 inline-flex items-center gap-2 rounded-full border border-ink/10 bg-white/55 px-4 py-2 text-xs font-semibold"><Icon name="spark" className="h-4 w-4 text-clay" />AI memory, made ambient</div>
            <h1 className="reveal reveal-delay-1 balance max-w-3xl text-[3.5rem] font-semibold leading-[.94] tracking-[-.065em] sm:text-[5.2rem] lg:text-[6.2rem]">Save it once.<br /><span className="editorial font-normal italic text-clay">Meet it again.</span></h1>
            <p className="reveal reveal-delay-2 balance mt-8 max-w-xl text-lg leading-relaxed text-ink/65 sm:text-xl">PingLet turns the posts, words, and ideas you care about into a quiet personal memory on your Home Screen.</p>
            <div className="reveal reveal-delay-3 mt-9 flex flex-wrap gap-3">
              <Button href="/download">Download app <Icon name="arrow" className="h-4 w-4" /></Button>
              <Button href="#how-it-works" secondary>See how it works</Button>
            </div>
            <div className="mt-10 flex flex-wrap items-center gap-x-6 gap-y-3 text-xs font-semibold text-ink/50"><span>ANDROID FIRST</span><span className="h-1 w-1 rounded-full bg-gold" /><span>PRIVATE BY DEFAULT</span><span className="h-1 w-1 rounded-full bg-gold" /><span>SOURCE PRESERVED</span></div>
          </div>
          <div className="reveal reveal-delay-2 py-10 lg:py-0"><WidgetPreview /></div>
        </div>
      </section>

      <section className="border-y border-ink/10 bg-ink py-7 text-paper">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-5 px-5 text-center sm:px-8 md:flex-row md:text-left">
          <p className="editorial text-xl text-paper/80">From the endless scroll to something that stays.</p>
          <div className="flex items-center gap-7 text-xs font-bold tracking-[.15em] text-paper/45"><span>INSTAGRAM</span><span>TIKTOK</span><span>FACEBOOK</span><span>WEB</span></div>
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

    <footer className="bg-ink py-10 text-paper"><div className="mx-auto flex max-w-7xl flex-col gap-8 px-5 sm:px-8 md:flex-row md:items-center"><Logo light /><p className="text-sm text-paper/45 md:ml-4">Save what sticks.</p><div className="flex flex-wrap gap-6 text-sm text-paper/55 md:ml-auto"><a href="mailto:hello@pinglet.ai" className="hover:text-paper">Contact</a><a href="#" className="hover:text-paper">Privacy</a><a href="#" className="hover:text-paper">Terms</a></div><p className="text-xs text-paper/35">© {new Date().getFullYear()} PingLet</p></div></footer>
  </div>;
}

export default App;
