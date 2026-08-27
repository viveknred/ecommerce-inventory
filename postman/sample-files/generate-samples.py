#!/usr/bin/env python3
"""
Generates the sample files used by folder 10 of the Postman collection,
"10. Phase 4 - Media Upload".

Run it once from anywhere; the files are always written next to this script.

    python postman/sample-files/generate-samples.py

Produced files
--------------
sample-product.png       A small, genuinely valid PNG. The happy path.
sample-product.jpg       The same image as a baseline JPEG, for the second
                         accepted content type.
not-an-image.exe         Rejected on content type: Postman sends an .exe as
                         application/octet-stream.
not-an-image.pdf         Rejected on content type: application/pdf.
renamed-executable.png   The interesting one. It carries a .png name, so Postman
                         sends image/png and both the extension check and the
                         content-type check pass. It is still rejected, because
                         MediaStorageService tries to decode the bytes and the
                         decode fails.
oversized-image.png      A valid PNG of random noise larger than the 2 MB limit.
                         Noise is chosen deliberately: it does not compress, so
                         the file on disk really is over the limit. Not committed
                         to the repository, which is why this script exists.

Only the standard library is used, so no pip install is needed.
"""

import os
import struct
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))

# Two megabytes, matching app.media.max-file-size-bytes.
APP_LIMIT_BYTES = 2 * 1024 * 1024


def png_chunk(chunk_type: bytes, payload: bytes) -> bytes:
    """One PNG chunk: length, type, payload, CRC over type+payload."""
    return (
        struct.pack(">I", len(payload))
        + chunk_type
        + payload
        + struct.pack(">I", zlib.crc32(chunk_type + payload) & 0xFFFFFFFF)
    )


def write_png(path: str, width: int, height: int, rows: list) -> int:
    """
    Writes a truecolour 8-bit PNG.

    rows is a list of bytes objects, one per scanline, each width * 3 bytes of
    RGB. Every scanline is prefixed with filter type 0 (None), which keeps the
    encoder trivial at the cost of a slightly larger file.
    """
    raw = b"".join(b"\x00" + row for row in rows)

    header = struct.pack(
        ">IIBBBBB",
        width,
        height,
        8,  # bit depth
        2,  # colour type 2 = truecolour RGB
        0,  # deflate
        0,  # adaptive filtering
        0,  # no interlace
    )

    data = (
        b"\x89PNG\r\n\x1a\n"
        + png_chunk(b"IHDR", header)
        + png_chunk(b"IDAT", zlib.compress(raw, 6))
        + png_chunk(b"IEND", b"")
    )

    with open(path, "wb") as handle:
        handle.write(data)

    return len(data)


def gradient_rows(width: int, height: int) -> list:
    """A diagonal gradient with a darker border, so the image is visibly an image."""
    rows = []

    for y in range(height):
        row = bytearray()

        for x in range(width):
            on_border = x < 3 or y < 3 or x >= width - 3 or y >= height - 3

            if on_border:
                row += bytes((32, 38, 48))
            else:
                red = int(255 * x / max(width - 1, 1))
                green = int(255 * y / max(height - 1, 1))
                blue = 180 - int(120 * ((x + y) / max(width + height - 2, 1)))
                row += bytes((red, green, max(blue, 0)))

        rows.append(bytes(row))

    return rows


def noise_rows(width: int, height: int) -> list:
    """Random RGB noise. Incompressible, which is the whole point."""
    return [os.urandom(width * 3) for _ in range(height)]


def write_jpeg_from_png(png_path: str, jpeg_path: str) -> int:
    """
    Converts the sample PNG to JPEG if Pillow happens to be installed.

    Pillow is not a project dependency, so this is best-effort. Without it the
    collection still exercises both accepted content types, because the PNG path
    is the one the requests use by default.
    """
    try:
        from PIL import Image
    except ImportError:
        return 0

    with Image.open(png_path) as image:
        image.convert("RGB").save(jpeg_path, "JPEG", quality=85)

    return os.path.getsize(jpeg_path)


def human(size: int) -> str:
    if size < 1024:
        return f"{size} B"

    if size < 1024 * 1024:
        return f"{size / 1024:.1f} KB"

    return f"{size / (1024 * 1024):.2f} MB"


def main() -> None:
    created = []

    # 1. The happy path: a small valid PNG, comfortably under the limit.
    sample_png = os.path.join(HERE, "sample-product.png")
    size = write_png(sample_png, 96, 96, gradient_rows(96, 96))
    created.append(("sample-product.png", size, "valid PNG, expect 201"))

    jpeg_size = write_jpeg_from_png(
        sample_png,
        os.path.join(HERE, "sample-product.jpg"),
    )

    if jpeg_size:
        created.append(("sample-product.jpg", jpeg_size, "valid JPEG, expect 201"))

    # 2. A fake Windows executable. The MZ magic number makes it look the part.
    executable_bytes = (
        b"MZ\x90\x00\x03\x00\x00\x00\x04\x00\x00\x00\xff\xff\x00\x00"
        b"This file is not an image. It exists so the Postman collection can "
        b"prove that POST /api/v1/media/upload rejects executables with 400 "
        b"rather than storing them.\n"
    ) + b"\x00" * 512

    exe_path = os.path.join(HERE, "not-an-image.exe")

    with open(exe_path, "wb") as handle:
        handle.write(executable_bytes)

    created.append(("not-an-image.exe", len(executable_bytes), "expect 400, content type"))

    # 3. The same payload wearing a .png name, to defeat the naive checks and
    #    get caught by the decode attempt.
    renamed_path = os.path.join(HERE, "renamed-executable.png")

    with open(renamed_path, "wb") as handle:
        handle.write(executable_bytes)

    created.append(
        ("renamed-executable.png", len(executable_bytes), "expect 400, failed decode")
    )

    # 4. A minimal but structurally real PDF.
    pdf_bytes = (
        b"%PDF-1.4\n"
        b"1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
        b"2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
        b"3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >> endobj\n"
        b"trailer << /Root 1 0 R >>\n"
        b"%%EOF\n"
    )

    pdf_path = os.path.join(HERE, "not-an-image.pdf")

    with open(pdf_path, "wb") as handle:
        handle.write(pdf_bytes)

    created.append(("not-an-image.pdf", len(pdf_bytes), "expect 400, content type"))

    # 5. A valid PNG that is too big. Grow it until it clears the limit, since
    #    the exact compressed size of random data is not perfectly predictable.
    oversized_path = os.path.join(HERE, "oversized-image.png")
    side = 900
    size = 0

    while size <= APP_LIMIT_BYTES:
        size = write_png(oversized_path, side, side, noise_rows(side, side))

        if size <= APP_LIMIT_BYTES:
            side = int(side * 1.2)

    created.append(("oversized-image.png", size, "expect 400, over 2 MB"))

    width = max(len(name) for name, _, _ in created)

    print(f"Sample files written to {HERE}\n")

    for name, size, note in created:
        print(f"  {name.ljust(width)}  {human(size).rjust(9)}   {note}")

    print(
        "\nPoint Postman's working directory at the postman/ folder "
        "(Settings > General > Working directory)\nso the relative file paths in "
        "folder 10 resolve."
    )


if __name__ == "__main__":
    main()
