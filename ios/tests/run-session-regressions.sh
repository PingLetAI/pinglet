#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BUILD_DIR="$(mktemp -d "${TMPDIR:-/tmp}/pinglet-session-tests.XXXXXX")"
swiftc -parse-as-library -swift-version 5 -module-cache-path "$BUILD_DIR/modules" \
  "$ROOT/ios/PingLet/Core/Models.swift" \
  "$ROOT/ios/PingLet/Core/APIClient.swift" \
  "$ROOT/ios/PingLet/Core/SessionManager.swift" \
  "$ROOT/ios/tests/SessionManagerRegression.swift" \
  -o "$BUILD_DIR/session-regressions"
"$BUILD_DIR/session-regressions"
