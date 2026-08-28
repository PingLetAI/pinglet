# PingLet Premium App Store Media

Use the seven numbered PNG files in this directory for the iPhone 6.9-inch screenshot set in App Store Connect. They are `1290 x 2796`, RGB PNG files built from the real iOS screenshots in `reference/`.

For the App Store Connect 6.5-inch upload slot, use the seven files in `6.5-inch/`. They are the accepted `1284 x 2778` portrait size.

The first three tell the core PingLet story in installation-sheet order:

1. A saved idea returning through the Home Screen widget.
2. Sharing a public post into PingLet for extraction.
3. Seeing the current and upcoming widget rotation.

The remaining screens cover insights, Library, Explore, and the no-card Plus trial.

Regenerate from the repository root:

```bash
assets/app-store/premium/generate-premium-assets.sh
```

Editable compositions are written to `source/`. The app UI inside each device frame is the unaltered reference screenshot.
