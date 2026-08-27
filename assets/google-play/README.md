# PingLet Google Play assets

Production-ready listing assets for the PingLet Android app.

## Upload files

- `icon-512.png`: Play Store app icon, 512 x 512.
- `feature-graphic-1024x500.png`: feature graphic, 1024 x 500.
- `01-widget-1080x1920.png`: persistent Home Screen widget.
- `02-home-1080x1920.png`: resurfacing queue and Home experience.
- `03-capture-1080x1920.png`: personal text and public-link capture.
- `04-library-1080x1920.png`: personal saved library.
- `05-explore-1080x1920.png`: catalog-based discovery.
- `06-settings-1080x1920.png`: rotation and personalization settings.

Upload the screenshots in numeric order. The first three communicate the core product loop without requiring the viewer to expand the gallery.

## Editable sources

Editable SVG compositions are generated in `source/`. To rebuild the PNG files from the repository root:

```bash
./assets/google-play/generate-assets.sh
```

The generator requires Google Chrome, FFmpeg, the approved brand profile image, and the referenced app screenshots.

## Store-copy alignment

The assets intentionally avoid pricing, subscription promises, exact processing quotas, and unsupported lock-screen claims. They focus on the durable product value: capture, personal resurfacing, a persistent Home Screen widget, library, discovery, and customization.
