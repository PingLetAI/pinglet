#!/usr/bin/env python3
"""Copy reviewed, unmodified screenshots into an explicit upload set."""
from pathlib import Path
import hashlib
import json
import shutil
import struct

ROOT = Path(__file__).resolve().parent
KEYS = ("01-widget", "03-home", "04-insights", "05-library", "06-explore")
SIZES = {"6.9-inch": (1290, 2796), "6.5-inch": (1284, 2778)}


def main():
    manifest = []
    for slot, (width, height) in SIZES.items():
        source = ROOT if slot == "6.9-inch" else ROOT / slot
        destination = ROOT / "resubmission" / slot
        names = {f"{key}-{width}x{height}.png" for key in KEYS}
        if destination.exists():
            unexpected = {p.name for p in destination.iterdir() if p.suffix.lower() == ".png"} - names
            if unexpected:
                raise SystemExit(f"Remove unreviewed images from {destination}: {sorted(unexpected)}")
        destination.mkdir(parents=True, exist_ok=True)
        for name in sorted(names):
            image = source / name
            data = image.read_bytes()
            if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
                raise SystemExit(f"Not a PNG: {image}")
            if struct.unpack(">II", data[16:24]) != (width, height) or data[25] != 2:
                raise SystemExit(f"Expected RGB PNG at {width}x{height}: {image}")
            offset = 8
            while offset < len(data):
                length = struct.unpack(">I", data[offset:offset + 4])[0]
                if data[offset + 4:offset + 8] == b"tRNS":
                    raise SystemExit(f"PNG has transparency: {image}")
                offset += length + 12
            shutil.copyfile(image, destination / name)
            manifest.append({"file": f"{slot}/{name}", "sha256": hashlib.sha256(data).hexdigest()})
    (ROOT / "resubmission" / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    print("Prepared 5 screenshots per size in assets/app-store/premium/resubmission/")


if __name__ == "__main__":
    main()
