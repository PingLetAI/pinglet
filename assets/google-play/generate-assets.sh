#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="$ROOT/assets/google-play"
SOURCE="$OUT/source"
CHROME="${CHROME:-/usr/bin/google-chrome}"

mkdir -p "$SOURCE"

MARK_PATH='<path fill="#DDAE3D" fill-rule="evenodd" d="M240 812V212H470C646 212 740 307 740 439C740 579 646 663 470 663H354V812ZM354 327V548H470C573 548 625 508 625 439C625 369 573 327 470 327Z"/><circle cx="491" cy="439" r="49" fill="#F8F6F0"/><path d="M707 213C820 275 871 357 871 449C871 584 799 696 675 758" fill="none" stroke="#F8F6F0" stroke-width="51" stroke-linecap="round"/>'

data_uri() {
  local file="$1"
  local mime="image/png"
  case "$file" in
    *.jpg|*.jpeg) mime="image/jpeg" ;;
  esac
  printf 'data:%s;base64,%s' "$mime" "$(base64 -w0 "$file")"
}

render_svg() {
  local svg="$1"
  local png="$2"
  local width="$3"
  local height="$4"
  local html="$SOURCE/render.html"

  cat > "$html" <<EOF
<!doctype html>
<html><head><meta charset="utf-8"><style>
html,body{margin:0;width:${width}px;height:${height}px;overflow:hidden;background:#f4efe6}
svg{display:block;width:${width}px;height:${height}px}
</style></head><body>$(cat "$svg")</body></html>
EOF

  "$CHROME" --headless=new --disable-gpu --no-sandbox --disable-dev-shm-usage \
    --user-data-dir=/tmp/pinglet-google-play-chrome --hide-scrollbars \
    --force-device-scale-factor=1 --window-size="${width},${height}" \
    --screenshot="$png" "file://$html" >/dev/null 2>&1
}

create_screenshot() {
  local number="$1"
  local slug="$2"
  local line1="$3"
  local line2="$4"
  local input="$5"
  local image_data
  image_data="$(data_uri "$input")"
  local svg="$SOURCE/${number}-${slug}.svg"

  cat > "$svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#FAF7F0"/>
      <stop offset="1" stop-color="#EFE7DB"/>
    </linearGradient>
    <linearGradient id="phone" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#2B2E27"/>
      <stop offset="1" stop-color="#11130F"/>
    </linearGradient>
    <filter id="shadow" x="-30%" y="-20%" width="160%" height="160%">
      <feDropShadow dx="0" dy="28" stdDeviation="30" flood-color="#2E271C" flood-opacity=".20"/>
    </filter>
    <clipPath id="screen"><rect x="220" y="422" width="640" height="1351" rx="34"/></clipPath>
  </defs>
  <rect width="1080" height="1920" fill="url(#bg)"/>
  <circle cx="1005" cy="84" r="245" fill="#C7E6D7" opacity=".72"/>
  <circle cx="4" cy="1740" r="230" fill="#E7C0B2" opacity=".38"/>
  <path d="M0 327C176 282 249 371 382 324C505 281 559 193 721 202C871 210 953 282 1080 244V0H0Z" fill="#FFFFFF" opacity=".24"/>

  <g transform="translate(66 54) scale(.064)">$MARK_PATH</g>
  <text x="137" y="105" fill="#171914" font-family="Arial, sans-serif" font-size="30" font-weight="700" letter-spacing=".2">PingLet</text>
  <text x="67" y="217" fill="#171914" font-family="Georgia, serif" font-size="62" font-weight="400">
    <tspan x="67" dy="0">$line1</tspan>
    <tspan x="67" dy="75">$line2</tspan>
  </text>
  <rect x="67" y="354" width="86" height="7" rx="4" fill="#DDAE3D"/>

  <rect x="194" y="396" width="692" height="1403" rx="62" fill="url(#phone)" filter="url(#shadow)"/>
  <rect x="210" y="412" width="660" height="1371" rx="46" fill="#F8F6F0"/>
  <image href="$image_data" x="220" y="422" width="640" height="1351" preserveAspectRatio="xMidYMid meet" clip-path="url(#screen)"/>
  <rect x="430" y="409" width="220" height="21" rx="11" fill="#171914" opacity=".9"/>
</svg>
EOF

  render_svg "$svg" "$OUT/${number}-${slug}-1080x1920.png" 1080 1920
}

cat > "$SOURCE/feature-graphic-1024x500.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#F8F5ED"/>
      <stop offset="1" stop-color="#E9E2D7"/>
    </linearGradient>
    <linearGradient id="card" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#252820"/>
      <stop offset="1" stop-color="#11130F"/>
    </linearGradient>
    <filter id="shadow" x="-30%" y="-40%" width="170%" height="190%">
      <feDropShadow dx="0" dy="18" stdDeviation="20" flood-color="#2E271C" flood-opacity=".18"/>
    </filter>
  </defs>
  <rect width="1024" height="500" fill="url(#bg)"/>
  <circle cx="945" cy="55" r="206" fill="#C7E6D7" opacity=".8"/>
  <circle cx="58" cy="486" r="156" fill="#E9C4B7" opacity=".5"/>
  <g transform="translate(66 46) scale(.064)">$MARK_PATH</g>
  <text x="137" y="97" fill="#171914" font-family="Arial, sans-serif" font-size="30" font-weight="700">PingLet</text>
  <text x="66" y="197" fill="#171914" font-family="Georgia, serif" font-size="54">
    <tspan x="66">Keep what</tspan><tspan x="66" dy="62">resonates.</tspan>
  </text>
  <text x="68" y="345" fill="#4E504A" font-family="Arial, sans-serif" font-size="22">Save discoveries. See them again.</text>
  <rect x="584" y="105" width="365" height="292" rx="38" fill="url(#card)" filter="url(#shadow)"/>
  <text x="623" y="178" fill="#DDAE3D" font-family="Arial, sans-serif" font-size="16" font-weight="700" letter-spacing="2">TODAY'S PINGLET</text>
  <text x="623" y="238" fill="#F8F6F0" font-family="Georgia, serif" font-size="31">
    <tspan x="623">“Small ideas become</tspan>
    <tspan x="623" dy="43">part of you when they</tspan>
    <tspan x="623" dy="43">return at the right time.”</tspan>
  </text>
  <rect x="623" y="351" width="62" height="5" rx="3" fill="#DDAE3D"/>
</svg>
EOF

render_svg "$SOURCE/feature-graphic-1024x500.svg" "$OUT/feature-graphic-1024x500.png" 1024 500

ffmpeg -loglevel error -y -i "$ROOT/brand/social/pinglet-profile-1024.png" \
  -vf "scale=512:512:flags=lanczos" -frames:v 1 "$OUT/icon-512.png"

create_screenshot "01" "widget" "A thought that stays" "with you." "/tmp/pinglet-current.png"
create_screenshot "02" "home" "Meaningful ideas return" "throughout your day." "/home/tinkerpal/Downloads/WhatsApp Image 2026-08-25 at 9.20.02 PM.jpeg"
create_screenshot "03" "capture" "Save your words or share" "a public post." "/tmp/pinglet-add-final.png"
create_screenshot "04" "library" "Everything you keep," "in one place." "/tmp/pinglet-library-populated.png"
create_screenshot "05" "explore" "Discover ideas beyond" "your saves." "/tmp/pinglet-explore-final.png"
create_screenshot "06" "settings" "Make every rotation" "feel like yours." "/tmp/pinglet-settings-populated.png"

rm -f "$SOURCE/render.html"

printf 'Google Play assets created in %s\n' "$OUT"
