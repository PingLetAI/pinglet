#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT="$ROOT/assets/social"
SOURCE="$OUT/source"
CHROME="${CHROME:-/usr/bin/google-chrome}"

mkdir -p "$SOURCE"

MARK_PATH='<path fill="#DDAE3D" fill-rule="evenodd" d="M240 812V212H470C646 212 740 307 740 439C740 579 646 663 470 663H354V812ZM354 327V548H470C573 548 625 508 625 439C625 369 573 327 470 327Z"/><circle cx="491" cy="439" r="49" fill="#F8F6F0"/><path d="M707 213C820 275 871 357 871 449C871 584 799 696 675 758" fill="none" stroke="#F8F6F0" stroke-width="51" stroke-linecap="round"/>'

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
    --user-data-dir=/tmp/pinglet-social-chrome --hide-scrollbars \
    --force-device-scale-factor=1 --window-size="${width},${height}" \
    --screenshot="$png" "file://$html" >/dev/null 2>&1
}

cat > "$SOURCE/x-header-1500x500.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1500" height="500" viewBox="0 0 1500 500">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#F8F5ED"/><stop offset="1" stop-color="#E9E2D7"/></linearGradient>
    <linearGradient id="card" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#252820"/><stop offset="1" stop-color="#11130F"/></linearGradient>
    <filter id="shadow" x="-30%" y="-40%" width="170%" height="190%"><feDropShadow dx="0" dy="18" stdDeviation="20" flood-color="#2E271C" flood-opacity=".18"/></filter>
  </defs>
  <rect width="1500" height="500" fill="url(#bg)"/>
  <circle cx="1452" cy="22" r="254" fill="#C7E6D7" opacity=".82"/>
  <circle cx="114" cy="503" r="196" fill="#E9C4B7" opacity=".52"/>
  <path d="M0 0H360C287 105 299 205 206 267C139 312 70 312 0 288Z" fill="#FFFFFF" opacity=".2"/>

  <g transform="translate(500 43) scale(.064)">$MARK_PATH</g>
  <text x="571" y="94" fill="#171914" font-family="Arial, sans-serif" font-size="30" font-weight="700">PingLet</text>
  <text x="500" y="191" fill="#171914" font-family="Georgia, serif" font-size="57"><tspan x="500">Keep what</tspan><tspan x="500" dy="66">resonates.</tspan></text>
  <text x="502" y="341" fill="#4E504A" font-family="Arial, sans-serif" font-size="23">Save discoveries. See them again.</text>
  <rect x="994" y="92" width="414" height="316" rx="40" fill="url(#card)" filter="url(#shadow)"/>
  <text x="1036" y="165" fill="#DDAE3D" font-family="Arial, sans-serif" font-size="17" font-weight="700" letter-spacing="2">TODAY'S PINGLET</text>
  <text x="1036" y="229" fill="#F8F6F0" font-family="Georgia, serif" font-size="32"><tspan x="1036">“Small ideas become</tspan><tspan x="1036" dy="45">part of you when they</tspan><tspan x="1036" dy="45">return at the right time.”</tspan></text>
  <rect x="1036" y="360" width="66" height="5" rx="3" fill="#DDAE3D"/>
</svg>
EOF

cat > "$SOURCE/facebook-cover-1640x624.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="1640" height="624" viewBox="0 0 1640 624">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#F8F5ED"/><stop offset="1" stop-color="#E9E2D7"/></linearGradient>
    <linearGradient id="card" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#252820"/><stop offset="1" stop-color="#11130F"/></linearGradient>
    <filter id="shadow" x="-30%" y="-40%" width="170%" height="190%"><feDropShadow dx="0" dy="22" stdDeviation="24" flood-color="#2E271C" flood-opacity=".18"/></filter>
  </defs>
  <rect width="1640" height="624" fill="url(#bg)"/>
  <circle cx="1575" cy="28" r="302" fill="#C7E6D7" opacity=".82"/>
  <circle cx="130" cy="629" r="238" fill="#E9C4B7" opacity=".52"/>
  <path d="M0 0H430C343 128 359 252 248 328C163 386 82 385 0 353Z" fill="#FFFFFF" opacity=".2"/>

  <g transform="translate(546 70) scale(.072)">$MARK_PATH</g>
  <text x="626" y="127" fill="#171914" font-family="Arial, sans-serif" font-size="35" font-weight="700">PingLet</text>
  <text x="546" y="248" fill="#171914" font-family="Georgia, serif" font-size="66"><tspan x="546">Keep what</tspan><tspan x="546" dy="76">resonates.</tspan></text>
  <text x="548" y="425" fill="#4E504A" font-family="Arial, sans-serif" font-size="27">Save discoveries. See them again.</text>
  <rect x="1086" y="119" width="444" height="350" rx="44" fill="url(#card)" filter="url(#shadow)"/>
  <text x="1132" y="200" fill="#DDAE3D" font-family="Arial, sans-serif" font-size="19" font-weight="700" letter-spacing="2">TODAY'S PINGLET</text>
  <text x="1132" y="271" fill="#F8F6F0" font-family="Georgia, serif" font-size="35"><tspan x="1132">“Small ideas become</tspan><tspan x="1132" dy="49">part of you when they</tspan><tspan x="1132" dy="49">return at the right time.”</tspan></text>
  <rect x="1132" y="418" width="72" height="6" rx="3" fill="#DDAE3D"/>
</svg>
EOF

render_svg "$SOURCE/x-header-1500x500.svg" "$OUT/pinglet-x-header-1500x500.png" 1500 500
render_svg "$SOURCE/facebook-cover-1640x624.svg" "$OUT/pinglet-facebook-cover-1640x624.png" 1640 624

ffmpeg -loglevel error -y -i "$ROOT/brand/social/pinglet-profile-1024.png" \
  -vf "scale=1024:1024:flags=lanczos" -frames:v 1 "$OUT/pinglet-social-profile-1024.png"
ffmpeg -loglevel error -y -i "$ROOT/brand/social/pinglet-profile-1024.png" \
  -vf "scale=400:400:flags=lanczos" -frames:v 1 "$OUT/pinglet-x-profile-400.png"
ffmpeg -loglevel error -y -i "$ROOT/brand/social/pinglet-profile-1024.png" \
  -vf "scale=320:320:flags=lanczos" -frames:v 1 "$OUT/pinglet-facebook-profile-320.png"
rm -f "$SOURCE/render.html"

printf 'Social banners created in %s\n' "$OUT"
