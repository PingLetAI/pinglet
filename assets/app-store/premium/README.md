# PingLet Premium App Store Media

Upload **only the five PNGs per size in `resubmission/`**. Use `resubmission/6.9-inch/` for 1290 × 2796 and `resubmission/6.5-inch/` for 1284 × 2778.

The reviewed selection is widget (01), Home (03), insights (04), Library (05), and Explore (06). Compare these unchanged captures against the final submitted build before uploading.

Older PNGs and sources outside `resubmission/` are historical artwork, not an upload set. Screenshot 07 contains free-trial promotions both outside and inside the phone image. Screenshot 02 advertises unsupported YouTube imports. Both are excluded. Updating the app does not change those old pixels.

Prepare the upload set without changing or re-rendering images:

```bash
python3 assets/app-store/premium/prepare-resubmission.py
```

This validates dimensions and RGB opacity, copies the approved filenames, and writes `resubmission/manifest.json` checksums. It refuses unexpected PNGs in the destination.

`generate-premium-assets.sh` now renders only the same five compositions into `resubmission/`. It requires Chrome, GNU base64, and ffmpeg. The copy command above works on macOS without those dependencies.

Remove rejected assets from every App Store Connect size, localization, and custom listing where they were uploaded. Local changes do not change App Store Connect.
