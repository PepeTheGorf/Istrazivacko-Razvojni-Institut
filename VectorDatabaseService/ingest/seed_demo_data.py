import os
import sys
import time
from typing import List

import requests
import json

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8000/api/v1")

PAYLOAD_PATH = os.path.join(os.path.dirname(__file__), "seed_payload.json")


def _load_documents() -> List[dict]:
    if os.path.exists(PAYLOAD_PATH):
        try:
            with open(PAYLOAD_PATH, "r", encoding="utf-8") as f:
                data = json.load(f)
                if isinstance(data, list):
                    print(f"Loaded {len(data)} documents from seed_payload.json")
                    return data
        except Exception as exc:
            print(f"Failed to load {PAYLOAD_PATH}: {exc}")

    # fallback to `documents` defined earlier in-file (if present)
    try:
        return documents  # type: ignore[name-defined]
    except NameError:
        return []


def _chunkify(items: List[dict], size: int):
    for i in range(0, len(items), size):
        yield items[i : i + size]


def _post_documents(docs: List[dict]) -> bool:
    """Post documents to the API. Returns True if any errors occurred."""
    any_errors = False
    total = len(docs)
    for i, doc in enumerate(docs, start=1):
        try:
            resp = requests.post(f"{BASE_URL}/documents", json=doc, timeout=15)
        except requests.RequestException as exc:
            print(f"{i}/{total} - request failed: {exc}")
            any_errors = True
            continue

        try:
            body = resp.json()
        except ValueError:
            body = resp.text

        print(f"{i}/{total} - {resp.status_code} -> {body}")
        if resp.status_code >= 400:
            any_errors = True

    return any_errors


def _wait_until_available(retries: int = 5, delay: float = 2.0) -> None:
    for attempt in range(1, retries + 1):
        try:
            r = requests.get(BASE_URL, timeout=5)
            print(f"server reachable: {r.status_code}")
            return
        except requests.RequestException as exc:
            print(f"server not reachable (attempt {attempt}/{retries}): {exc}")
            if attempt < retries:
                time.sleep(delay)
    print("Proceeding — server still not reachable. Posts will likely fail.")


def main() -> int:
    print(f"Using BASE_URL={BASE_URL}")
    _wait_until_available()

    docs = _load_documents()
    if not docs:
        print("No documents to post. Exiting.")
        return 0

    try:
        batch_size = int(os.environ.get("BATCH_SIZE", "16"))
    except ValueError:
        batch_size = 16

    total = len(docs)
    print(f"Posting {total} documents in batches of {batch_size}...")

    any_errors = False
    posted = 0
    for chunk in _chunkify(docs, batch_size):
        print(f"Posting batch: {posted + 1} - {posted + len(chunk)}")
        errs = _post_documents(chunk)
        if errs:
            any_errors = True
        posted += len(chunk)

    print(f"Completed: attempted={total}, posted={posted}, errors={'yes' if any_errors else 'no'}")
    return 1 if any_errors else 0


def seed_demo_data() -> int:
    return main()


if __name__ == "__main__":
    raise SystemExit(main())