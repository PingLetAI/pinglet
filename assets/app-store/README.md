# PingLet App Store assets

This folder contains the first-version iOS App Store visual package.

## Included

- `icon-1024.png`: opaque App Store icon reference. The submitted icon is read from the app binary.
- `01-share-extract-1290x2796.png`: Share Extension and AI extraction.
- `02-home-widget-1290x2796.png`: Home and widget resurfacing.
- `03-library-1290x2796.png`: saved PingLets.
- `04-insights-1290x2796.png`: AI overview, insight, and Plus value.
- `05-explore-1290x2796.png`: Explore collections.
- `06-settings-plus-1290x2796.png`: account, trial, usage, and customization.
- `source/*.svg`: editable source artwork.
- `generate-assets.sh`: deterministic PNG generator.

The screenshots use Apple's accepted 6.9-inch portrait size of 1290 x 2796 pixels and contain no transparency.

## Regenerate

```bash
assets/app-store/generate-assets.sh
```

Set `CHROME` if Google Chrome is installed elsewhere.

## Before public release

These assets accurately represent the current PingLet product flows, but final App Store submission screenshots should be compared against the final TestFlight build. Update any screen whose shipped UI or pricing changes before submission.
