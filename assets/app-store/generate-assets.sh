#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="$ROOT/assets/app-store"
SOURCE="$OUT/source"
CHROME="${CHROME:-/usr/bin/google-chrome}"

mkdir -p "$SOURCE"
cp "$ROOT/ios/PingLet/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png" "$OUT/icon-1024.png"

render_svg() {
  local svg="$1" png="$2"
  local html="$SOURCE/render.html"
  cat > "$html" <<EOF
<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;width:1290px;height:2796px;overflow:hidden;background:#f7f1e7}svg{display:block}
</style></head><body>$(cat "$svg")</body></html>
EOF
  "$CHROME" --headless=new --disable-gpu --no-sandbox --disable-dev-shm-usage \
    --user-data-dir=/tmp/pinglet-app-store-chrome --hide-scrollbars \
    --force-device-scale-factor=1 --window-size=1290,2796 \
    --screenshot="$png" "file://$html" >/dev/null 2>&1
}

write_start() {
  local file="$1" line1="$2" line2="$3"
  cat > "$file" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1290" height="2796" viewBox="0 0 1290 2796">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#FBF8F1"/><stop offset="1" stop-color="#EDE4D7"/></linearGradient>
  <linearGradient id="phone" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#30332C"/><stop offset="1" stop-color="#0F110E"/></linearGradient>
  <linearGradient id="screen" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#FFFDF7"/><stop offset="1" stop-color="#F2EBDD"/></linearGradient>
  <filter id="shadow" x="-30%" y="-20%" width="160%" height="160%"><feDropShadow dx="0" dy="36" stdDeviation="34" flood-color="#2E271C" flood-opacity=".22"/></filter>
  <clipPath id="clip"><rect x="179" y="614" width="932" height="2078" rx="52"/></clipPath>
</defs>
<rect width="1290" height="2796" fill="url(#bg)"/>
<circle cx="1190" cy="92" r="320" fill="#C7E6D7" opacity=".72"/><circle cx="0" cy="2570" r="285" fill="#E7C0B2" opacity=".38"/>
<circle cx="88" cy="86" r="42" fill="#171914"/><text x="88" y="103" text-anchor="middle" fill="#DDAE3D" font-family="Georgia,serif" font-size="49" font-weight="700">P</text>
<text x="150" y="103" fill="#171914" font-family="Arial,sans-serif" font-size="38" font-weight="700">PingLet</text>
<text x="74" y="256" fill="#171914" font-family="Georgia,serif" font-size="78"><tspan x="74">$line1</tspan><tspan x="74" dy="91">$line2</tspan></text>
<rect x="74" y="463" width="104" height="8" rx="4" fill="#DDAE3D"/>
<rect x="149" y="574" width="992" height="2158" rx="82" fill="url(#phone)" filter="url(#shadow)"/>
<rect x="169" y="594" width="952" height="2118" rx="65" fill="url(#screen)"/>
<g clip-path="url(#clip)">
<rect x="179" y="614" width="932" height="2078" fill="url(#screen)"/>
EOF
}

write_end() {
  local file="$1"
  cat >> "$file" <<'EOF'
</g>
<rect x="486" y="592" width="318" height="39" rx="20" fill="#171914"/>
</svg>
EOF
}

nav() {
  cat <<'EOF'
<rect x="179" y="2520" width="932" height="172" fill="#FFFDF7"/><line x1="179" y1="2520" x2="1111" y2="2520" stroke="#171914" stroke-opacity=".09"/>
<text x="272" y="2585" text-anchor="middle" font-family="Arial" font-size="31">⌂</text><text x="272" y="2642" text-anchor="middle" font-family="Arial" font-size="20">Home</text>
<text x="442" y="2585" text-anchor="middle" font-family="Arial" font-size="31">▮</text><text x="442" y="2642" text-anchor="middle" font-family="Arial" font-size="20">Library</text>
<circle cx="645" cy="2580" r="56" fill="#DDAE3D"/><text x="645" y="2594" text-anchor="middle" font-family="Arial" font-size="47">+</text>
<text x="848" y="2585" text-anchor="middle" font-family="Arial" font-size="31">◈</text><text x="848" y="2642" text-anchor="middle" font-family="Arial" font-size="20">Explore</text>
<text x="1018" y="2585" text-anchor="middle" font-family="Arial" font-size="31">⚙</text><text x="1018" y="2642" text-anchor="middle" font-family="Arial" font-size="20">Settings</text>
EOF
}

f="$SOURCE/01-share-extract.svg"; write_start "$f" "Share what matters." "PingLet finds the signal."
cat >> "$f" <<'EOF'
<text x="236" y="720" fill="#B54B35" font-family="Arial" font-size="25" font-weight="700" letter-spacing="3">NEW PINGLET</text>
<text x="236" y="805" fill="#171914" font-family="Georgia" font-size="61">Keep what found you.</text>
<text x="236" y="865" fill="#4A4D45" font-family="Arial" font-size="27">Write your own words or share a public post.</text>
<rect x="222" y="930" width="846" height="720" rx="42" fill="#FFFFFF" fill-opacity=".78" stroke="#171914" stroke-opacity=".11"/>
<text x="260" y="1000" fill="#B54B35" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">YOUR WORDS OR A DISCOVERED POST</text>
<text x="260" y="1090" fill="#171914" font-family="Georgia" font-size="34"><tspan x="260">This idea about building confidence</tspan><tspan x="260" dy="49">is worth seeing again.</tspan></text>
<text x="260" y="1235" fill="#4A4D45" font-family="Arial" font-size="25"><tspan x="260">https://www.instagram.com/reel/</tspan><tspan x="260" dy="39">meaningful-public-post</tspan></text>
<line x1="260" y1="1440" x2="1030" y2="1440" stroke="#171914" stroke-opacity=".11"/>
<text x="260" y="1510" fill="#77786F" font-family="Arial" font-size="25">Author or creator (optional)</text>
<rect x="222" y="1705" width="846" height="100" rx="30" fill="#171914"/><text x="645" y="1768" text-anchor="middle" fill="#FAF7F0" font-family="Arial" font-size="25" font-weight="700" letter-spacing="2">EXTRACT AND SAVE</text>
<text x="645" y="1870" text-anchor="middle" fill="#67695F" font-family="Arial" font-size="22">Words, images, and speech are analyzed quietly.</text>
EOF
write_end "$f"; render_svg "$f" "$OUT/01-share-extract-1290x2796.png"

f="$SOURCE/02-home-widget.svg"; write_start "$f" "The right thought." "Right when you need it."
cat >> "$f" <<'EOF'
<text x="236" y="714" fill="#B54B35" font-family="Arial" font-size="24" font-weight="700" letter-spacing="3">TODAY</text>
<text x="236" y="795" fill="#171914" font-family="Georgia" font-size="58">One good thought, kept close.</text>
<rect x="222" y="865" width="846" height="610" rx="46" fill="#151712"/>
<text x="266" y="940" fill="#DDAE3D" font-family="Arial" font-size="22" font-weight="700" letter-spacing="2">ON YOUR WIDGET</text>
<text x="266" y="1045" fill="#FAF7F0" font-family="Georgia" font-size="45"><tspan x="266">Confidence grows when your</tspan><tspan x="266" dy="62">actions become evidence for</tspan><tspan x="266" dy="62">the person you are becoming.</tspan></text>
<rect x="266" y="1340" width="68" height="6" rx="3" fill="#DDAE3D"/><text x="266" y="1400" fill="#A7A99F" font-family="Arial" font-size="22">A THOUGHT WORTH KEEPING</text>
<text x="236" y="1555" fill="#171914" font-family="Georgia" font-size="42">Coming up</text><text x="1030" y="1550" text-anchor="end" fill="#77786F" font-family="Arial" font-size="20">READY OFFLINE</text>
<rect x="222" y="1600" width="846" height="560" rx="40" fill="#FFFFFF" fill-opacity=".72" stroke="#171914" stroke-opacity=".11"/>
<text x="266" y="1690" fill="#B54B35" font-family="Arial" font-size="22">01</text><text x="330" y="1690" fill="#171914" font-family="Arial" font-size="27">Build evidence, not just confidence.</text>
<line x1="266" y1="1750" x2="1025" y2="1750" stroke="#171914" stroke-opacity=".1"/>
<text x="266" y="1840" fill="#B54B35" font-family="Arial" font-size="22">02</text><text x="330" y="1840" fill="#171914" font-family="Arial" font-size="27">Protect the work that matters most.</text>
<line x1="266" y1="1900" x2="1025" y2="1900" stroke="#171914" stroke-opacity=".1"/>
<text x="266" y="1990" fill="#B54B35" font-family="Arial" font-size="22">03</text><text x="330" y="1990" fill="#171914" font-family="Arial" font-size="27">Small ideas compound when they return.</text>
EOF
nav >> "$f"; write_end "$f"; render_svg "$f" "$OUT/02-home-widget-1290x2796.png"

f="$SOURCE/03-library.svg"; write_start "$f" "Everything you kept." "Ready to return."
cat >> "$f" <<'EOF'
<text x="236" y="714" fill="#B54B35" font-family="Arial" font-size="24" font-weight="700" letter-spacing="3">LIBRARY</text>
<text x="236" y="795" fill="#171914" font-family="Georgia" font-size="60">Everything you kept.</text>
<rect x="222" y="865" width="846" height="82" rx="28" fill="#E8E5DE"/><rect x="229" y="872" width="414" height="68" rx="23" fill="#C7E6D7"/><text x="438" y="921" text-anchor="middle" font-family="Arial" font-size="24" font-weight="700">ALL SAVES</text><text x="856" y="921" text-anchor="middle" font-family="Arial" font-size="24">FAVORITES</text>
<rect x="222" y="980" width="846" height="78" rx="24" fill="#FFFFFF" fill-opacity=".72" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1030" fill="#77786F" font-family="Arial" font-size="24">Search your PingLets</text>
<rect x="222" y="1100" width="846" height="320" rx="40" fill="#FFFFFF" fill-opacity=".75" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1160" fill="#B54B35" font-family="Arial" font-size="20" font-weight="700">QUOTE</text><text x="1015" y="1163" text-anchor="end" fill="#DDAE3D" font-size="30">♥</text><text x="260" y="1240" fill="#171914" font-family="Georgia" font-size="34"><tspan x="260">Confidence grows when your actions</tspan><tspan x="260" dy="47">become evidence.</tspan></text><text x="260" y="1360" fill="#77786F" font-family="Arial" font-size="22">Saved from Instagram</text>
<rect x="222" y="1450" width="846" height="320" rx="40" fill="#FFFFFF" fill-opacity=".75" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1510" fill="#B54B35" font-family="Arial" font-size="20" font-weight="700">NOTE</text><text x="260" y="1590" fill="#171914" font-family="Georgia" font-size="34"><tspan x="260">What you repeat becomes easier</tspan><tspan x="260" dy="47">to remember and live.</tspan></text><text x="260" y="1710" fill="#77786F" font-family="Arial" font-size="22">Written by you</text>
<rect x="222" y="1800" width="846" height="320" rx="40" fill="#FFFFFF" fill-opacity=".75" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1860" fill="#B54B35" font-family="Arial" font-size="20" font-weight="700">PASSAGE</text><text x="260" y="1940" fill="#171914" font-family="Georgia" font-size="34"><tspan x="260">The best ideas deserve more than</tspan><tspan x="260" dy="47">a forgotten bookmark.</tspan></text><text x="260" y="2060" fill="#77786F" font-family="Arial" font-size="22">Saved from a public link</text>
EOF
nav >> "$f"; write_end "$f"; render_svg "$f" "$OUT/03-library-1290x2796.png"

f="$SOURCE/04-insights.svg"; write_start "$f" "More than a save." "Understand what matters."
cat >> "$f" <<'EOF'
<text x="236" y="710" fill="#B54B35" font-family="Arial" font-size="23" font-weight="700" letter-spacing="3">SAVED BY YOU</text>
<text x="236" y="800" fill="#171914" font-family="Georgia" font-size="48"><tspan x="236">Build evidence for the person</tspan><tspan x="236" dy="61">you want to become.</tspan></text>
<rect x="222" y="960" width="846" height="84" rx="27" fill="#171914"/><text x="645" y="1014" text-anchor="middle" fill="#FAF7F0" font-family="Arial" font-size="23" font-weight="700">OPEN ORIGINAL SOURCE</text>
<text x="236" y="1140" fill="#171914" font-family="Georgia" font-size="39">Overview</text><text x="236" y="1200" fill="#4A4D45" font-family="Arial" font-size="26"><tspan x="236">Confidence is strengthened by repeated action,</tspan><tspan x="236" dy="39">not by waiting to feel completely ready.</tspan></text>
<text x="236" y="1340" fill="#171914" font-family="Georgia" font-size="39">Key insights</text>
<rect x="222" y="1390" width="846" height="370" rx="40" fill="#FFFFFF" fill-opacity=".75" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1460" fill="#171914" font-family="Arial" font-size="28" font-weight="700">Action creates self-trust</text><text x="260" y="1530" fill="#4A4D45" font-family="Arial" font-size="25"><tspan x="260">Small completed promises give your mind</tspan><tspan x="260" dy="39">evidence that you can rely on yourself.</tspan></text><text x="260" y="1680" fill="#77786F" font-family="Georgia" font-size="23">“Build evidence before confidence.”</text>
<rect x="222" y="1800" width="846" height="410" rx="40" fill="#151712"/><text x="260" y="1870" fill="#DDAE3D" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">PINGLET PLUS</text><text x="260" y="1940" fill="#FAF7F0" font-family="Georgia" font-size="38">There is more in this PingLet</text><text x="260" y="2010" fill="#C8CAC2" font-family="Arial" font-size="24"><tspan x="260">Full summary, every insight, practical</tspan><tspan x="260" dy="37">takeaways, transcript, and visible text.</tspan></text><rect x="260" y="2110" width="770" height="78" rx="25" fill="#DDAE3D"/><text x="645" y="2161" text-anchor="middle" fill="#171914" font-family="Arial" font-size="22" font-weight="700">TRY PINGLET PLUS · 7 DAYS FREE</text>
EOF
write_end "$f"; render_svg "$f" "$OUT/04-insights-1290x2796.png"

f="$SOURCE/05-explore.svg"; write_start "$f" "Discover what resonates." "Bring it into your day."
cat >> "$f" <<'EOF'
<text x="236" y="714" fill="#B54B35" font-family="Arial" font-size="24" font-weight="700" letter-spacing="3">EXPLORE</text>
<text x="236" y="795" fill="#171914" font-family="Georgia" font-size="60">Ideas beyond your saves.</text>
<text x="236" y="855" fill="#4A4D45" font-family="Arial" font-size="25">Curated public PingLets that can join your rotation.</text>
<rect x="222" y="930" width="846" height="380" rx="42" fill="#C7E6D7"/><text x="260" y="1000" fill="#234136" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">COLLECTION</text><text x="260" y="1080" fill="#171914" font-family="Georgia" font-size="43">Confidence & self-trust</text><text x="260" y="1150" fill="#365A4D" font-family="Arial" font-size="24"><tspan x="260">Practical reminders for moving before</tspan><tspan x="260" dy="38">you feel completely ready.</tspan></text><text x="260" y="1260" fill="#234136" font-family="Arial" font-size="22" font-weight="700">24 PINGLETS  →</text>
<rect x="222" y="1345" width="846" height="380" rx="42" fill="#F1D9CE"/><text x="260" y="1415" fill="#8A3A2C" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">COLLECTION</text><text x="260" y="1495" fill="#171914" font-family="Georgia" font-size="43">Focus & meaningful work</text><text x="260" y="1565" fill="#6C493F" font-family="Arial" font-size="24"><tspan x="260">Ideas for protecting attention and doing</tspan><tspan x="260" dy="38">the work that deserves your time.</tspan></text><text x="260" y="1675" fill="#8A3A2C" font-family="Arial" font-size="22" font-weight="700">31 PINGLETS  →</text>
<rect x="222" y="1760" width="846" height="380" rx="42" fill="#F4E6B7"/><text x="260" y="1830" fill="#806014" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">COLLECTION</text><text x="260" y="1910" fill="#171914" font-family="Georgia" font-size="43">Reflection & perspective</text><text x="260" y="1980" fill="#685B35" font-family="Arial" font-size="24"><tspan x="260">Quiet thoughts that help the day make</tspan><tspan x="260" dy="38">more sense when they return.</tspan></text><text x="260" y="2090" fill="#806014" font-family="Arial" font-size="22" font-weight="700">18 PINGLETS  →</text>
EOF
nav >> "$f"; write_end "$f"; render_svg "$f" "$OUT/05-explore-1290x2796.png"

f="$SOURCE/06-settings-plus.svg"; write_start "$f" "Your rhythm, your way." "More room with Plus."
cat >> "$f" <<'EOF'
<text x="236" y="714" fill="#B54B35" font-family="Arial" font-size="24" font-weight="700" letter-spacing="3">SETTINGS</text>
<text x="236" y="795" fill="#171914" font-family="Georgia" font-size="60">Your PingLet, your rhythm.</text>
<rect x="222" y="875" width="846" height="540" rx="44" fill="#151712"/><circle cx="304" cy="970" r="48" fill="#C7E6D7"/><text x="304" y="984" text-anchor="middle" font-size="35">●</text><text x="380" y="955" fill="#FAF7F0" font-family="Georgia" font-size="35">PingLet Plus trial</text><text x="380" y="1000" fill="#AEB0A8" font-family="Arial" font-size="22">you@pinglet.ai</text><rect x="900" y="930" width="120" height="50" rx="25" fill="#DDAE3D"/><text x="960" y="962" text-anchor="middle" fill="#171914" font-family="Arial" font-size="18" font-weight="700">TRIAL</text>
<line x1="260" y1="1060" x2="1030" y2="1060" stroke="#FFFFFF" stroke-opacity=".12"/><text x="260" y="1120" fill="#AEB0A8" font-family="Arial" font-size="19">SAVES</text><text x="260" y="1165" fill="#FAF7F0" font-family="Arial" font-size="29" font-weight="700">Unlimited</text><text x="660" y="1120" fill="#AEB0A8" font-family="Arial" font-size="19">AI IMPORTS</text><text x="660" y="1165" fill="#FAF7F0" font-family="Arial" font-size="29" font-weight="700">4 of 50</text><text x="260" y="1270" fill="#C7E6D7" font-family="Arial" font-size="22">6 days remaining · no automatic charge</text>
<rect x="222" y="1450" width="846" height="650" rx="42" fill="#FFFFFF" fill-opacity=".74" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1520" fill="#B54B35" font-family="Arial" font-size="21" font-weight="700" letter-spacing="2">EXPERIENCE</text><text x="260" y="1600" fill="#171914" font-family="Arial" font-size="28" font-weight="700">Content balance</text><rect x="260" y="1650" width="770" height="76" rx="25" fill="#E8E5DE"/><rect x="514" y="1657" width="260" height="62" rx="20" fill="#C7E6D7"/><text x="387" y="1700" text-anchor="middle" font-family="Arial" font-size="21">MINE</text><text x="644" y="1700" text-anchor="middle" font-family="Arial" font-size="21" font-weight="700">BALANCED</text><text x="900" y="1700" text-anchor="middle" font-family="Arial" font-size="21">DISCOVER</text><line x1="260" y1="1800" x2="1030" y2="1800" stroke="#171914" stroke-opacity=".1"/><text x="260" y="1875" fill="#171914" font-family="Arial" font-size="28" font-weight="700">Widget appearance</text><text x="260" y="1915" fill="#77786F" font-family="Arial" font-size="21">Themes, typography, schedules, controls</text><text x="1015" y="1895" text-anchor="end" font-size="31">›</text><line x1="260" y1="1970" x2="1030" y2="1970" stroke="#171914" stroke-opacity=".1"/><text x="260" y="2045" fill="#171914" font-family="Arial" font-size="28" font-weight="700">Processing queue</text><text x="1015" y="2050" text-anchor="end" font-size="31">›</text>
EOF
nav >> "$f"; write_end "$f"; render_svg "$f" "$OUT/06-settings-plus-1290x2796.png"

rm -f "$SOURCE/render.html"
printf 'App Store assets created in %s\n' "$OUT"
