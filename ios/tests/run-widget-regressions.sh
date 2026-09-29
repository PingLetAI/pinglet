#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BUILD_DIR="$(mktemp -d "${TMPDIR:-/tmp}/pinglet-widget-tests.XXXXXX")"
swiftc -parse-as-library -swift-version 5 -module-cache-path "$BUILD_DIR/modules" \
  "$ROOT/ios/PingLet/Core/Models.swift" \
  "$ROOT/ios/PingLet/Core/WidgetSelection.swift" \
  "$ROOT/ios/tests/WidgetSelectionRegression.swift" \
  -o "$BUILD_DIR/widget-regressions"
"$BUILD_DIR/widget-regressions"
