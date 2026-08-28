#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
OUT="$ROOT/assets/app-store/premium"
SOURCE="$OUT/source"
REFERENCES="$ROOT/reference"
CHROME="${CHROME:-/usr/bin/google-chrome}"

mkdir -p "$SOURCE"

render() {
  local key="$1" image="$2" background="$3" ink="$4" accent="$5"
  local eyebrow="$6" line_one="$7" line_two="$8" body_one="$9" body_two="${10}"
  local data svg html png
  data="$(base64 -w0 "$REFERENCES/$image")"
  svg="$SOURCE/$key.svg"
  html="/tmp/pinglet-premium-$key.html"
  png="$OUT/$key.png"

  cat > "$svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1290" height="2796" viewBox="0 0 1290 2796">
  <defs>
    <clipPath id="screen"><rect x="202" y="812" width="886" height="1920" rx="54"/></clipPath>
    <filter id="shadow" x="-30%" y="-20%" width="160%" height="160%">
      <feDropShadow dx="0" dy="34" stdDeviation="42" flood-color="#000000" flood-opacity="0.22"/>
    </filter>
    <linearGradient id="wash" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="$accent" stop-opacity="0.28"/>
      <stop offset="1" stop-color="$accent" stop-opacity="0"/>
    </linearGradient>
  </defs>

  <rect width="1290" height="2796" fill="$background"/>
  <circle cx="1160" cy="120" r="330" fill="url(#wash)"/>
  <circle cx="80" cy="830" r="230" fill="$accent" opacity="0.10"/>

  <g transform="translate(92 86)">
    <rect width="62" height="62" rx="20" fill="$accent"/>
    <path d="M19 16h16c10 0 17 6 17 15s-7 15-17 15h-5v9H19V16zm11 10v10h5c4 0 6-2 6-5s-2-5-6-5h-5z" fill="$background"/>
    <text x="82" y="45" fill="$ink" font-family="Arial, Helvetica, sans-serif" font-size="38" font-weight="700" letter-spacing="-1">PingLet</text>
  </g>

  <text x="94" y="258" fill="$accent" font-family="Arial, Helvetica, sans-serif" font-size="23" font-weight="700" letter-spacing="4">$eyebrow</text>
  <text x="90" y="395" fill="$ink" font-family="Georgia, 'Times New Roman', serif" font-size="104" letter-spacing="-3">
    <tspan x="90" dy="0">$line_one</tspan>
    <tspan x="90" dy="112">$line_two</tspan>
  </text>
  <text x="94" y="650" fill="$ink" opacity="0.72" font-family="Arial, Helvetica, sans-serif" font-size="32">
    <tspan x="94" dy="0">$body_one</tspan>
    <tspan x="94" dy="46">$body_two</tspan>
  </text>

  <g filter="url(#shadow)">
    <rect x="180" y="790" width="930" height="1964" rx="76" fill="#0E100D"/>
    <image x="202" y="812" width="886" height="1920" preserveAspectRatio="xMidYMid slice"
      clip-path="url(#screen)" href="data:image/jpeg;base64,$data"/>
  </g>
  <rect x="535" y="812" width="220" height="28" rx="14" fill="#0E100D" opacity="0.92"/>
</svg>
EOF

  cat > "$html" <<EOF
<!doctype html><html><head><meta charset="utf-8"><style>
html,body{margin:0;width:1290px;height:2796px;overflow:hidden;background:$background}svg{display:block}
</style></head><body>$(cat "$svg")</body></html>
EOF

  "$CHROME" --headless=new --disable-gpu --no-sandbox --disable-dev-shm-usage \
    --user-data-dir="/tmp/pinglet-premium-chrome-$key" --hide-scrollbars \
    --force-device-scale-factor=1 --window-size=1290,2796 \
    --screenshot="$png" "file://$html" >/dev/null 2>&1
  rm -f "$html"
}

render "01-widget-1290x2796" "IMG_1179.png" "#151712" "#F8F2E7" "#E2B13B" \
  "THE IDEA THAT RETURNS" "Keep what matters" "within reach." \
  "Your saved ideas resurface on your Home Screen," "quietly, throughout the day."

render "02-share-extract-1290x2796" "IMG_1175.png" "#F5EFE5" "#171914" "#B94C35" \
  "SHARE TO PINGLET" "Save it once." "Remember it later." \
  "Send a public post to PingLet. AI extracts the idea" "worth keeping and brings it back to you."

render "03-home-1290x2796" "IMG_1170.png" "#D7EADF" "#172019" "#B94C35" \
  "A QUIETER DAILY RITUAL" "Your best ideas," "back at the right time." \
  "See what is on your widget now and what is" "coming next, ready even when you are offline."

render "04-insights-1290x2796" "IMG_1171.png" "#EBCFC1" "#2A1813" "#B94C35" \
  "MORE THAN A BOOKMARK" "Understand what" "made it resonate." \
  "Return to the original source, then explore the" "overview, insights, takeaways, and full context."

render "05-library-1290x2796" "IMG_1172.png" "#F5EFE5" "#171914" "#D9A72F" \
  "YOUR PERSONAL LIBRARY" "Everything you kept." "Nothing gets lost." \
  "Search every saved PingLet, revisit favorites," "and decide what should return to your widget."

render "06-explore-1290x2796" "IMG_1173.png" "#D7EADF" "#172019" "#B94C35" \
  "CURATED FOR DISCOVERY" "Find ideas beyond" "your own saves." \
  "Explore thoughtful public collections and bring" "the ones that resonate into your daily rotation."

render "07-plus-1290x2796" "IMG_1178.png" "#151712" "#F8F2E7" "#E2B13B" \
  "TRY PINGLET PLUS" "The whole experience." "Free for 7 days." \
  "Full summaries, insights, premium widgets, and" "more AI imports. No card. No automatic charge."

cp "$ROOT/ios/PingLet/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png" "$OUT/icon-1024.png"
printf 'Premium App Store assets created in %s\n' "$OUT"
