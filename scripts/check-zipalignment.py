#!/usr/bin/env python3
"""Verify Gradle's unsigned APK already has 4-byte-aligned stored ZIP entries.

This uses only Python's standard library and is independent of native libc++.
Run BEFORE apksigner: signing preserves local ZIP payload offsets.
"""
from __future__ import annotations
import struct
import sys
from pathlib import Path
from zipfile import ZipFile, ZIP_STORED


def check(path: Path) -> int:
    with path.open("rb") as f, ZipFile(f) as archive:
        if archive.testzip() is not None:
            raise ValueError("APK ZIP integrity check failed")
        stored = 0
        for entry in archive.infolist():
            if entry.compress_type != ZIP_STORED:
                continue
            stored += 1
            f.seek(entry.header_offset)
            local = f.read(30)
            if len(local) != 30 or local[:4] != b"PK\x03\x04":
                raise ValueError("Invalid local ZIP header")
            name_len, extra_len = struct.unpack_from("<HH", local, 26)
            offset = entry.header_offset + 30 + name_len + extra_len
            if offset % 4:
                raise ValueError(f"Unaligned stored entry {entry.filename} at {offset}")
        if stored == 0:
            raise ValueError("Unexpected APK: no uncompressed entries")
    return stored


if __name__ == "__main__":
    num = check(Path(sys.argv[1]))
    print(f"ZIP alignment verified: {num} stored entries, all 4-byte aligned")
