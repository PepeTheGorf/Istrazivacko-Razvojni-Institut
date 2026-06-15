from __future__ import annotations

import hashlib
import json
from functools import lru_cache
from typing import Any

import redis
from redis.exceptions import ConnectionError as RedisConnectionError

from config import REDIS_DB, REDIS_HOST, REDIS_PORT, SEARCH_CACHE_TTL_SECONDS


@lru_cache(maxsize=1)
def _redis_client() -> redis.Redis:
    return redis.Redis(host=REDIS_HOST, port=REDIS_PORT, db=REDIS_DB, decode_responses=True)


def _version_key(namespace: str) -> str:
    return f"{namespace}:version"


def _cache_key(namespace: str, digest: str, version: int) -> str:
    return f"{namespace}:v{version}:{digest}"


def namespace_version(namespace: str) -> int:
    try:
        value = _redis_client().get(_version_key(namespace))
    except RedisConnectionError:
        return 0
    if value is None:
        try:
            _redis_client().set(_version_key(namespace), 1, ex=SEARCH_CACHE_TTL_SECONDS)
        except RedisConnectionError:
            return 0
        return 1
    try:
        return int(value)
    except Exception:
        return 0


def bump_namespace(namespace: str) -> int:
    try:
        version = int(_redis_client().incr(_version_key(namespace)))
        _redis_client().expire(_version_key(namespace), SEARCH_CACHE_TTL_SECONDS)
        return version
    except RedisConnectionError:
        return 0


def make_digest(*parts: Any) -> str:
    raw = json.dumps(parts, sort_keys=True, default=str, ensure_ascii=True, separators=(",", ":"))
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()


def get_json(namespace: str, key_parts: tuple[Any, ...]) -> Any | None:
    try:
        version = namespace_version(namespace)
        if version == 0:
            return None
        digest = make_digest(*key_parts)
        payload = _redis_client().get(_cache_key(namespace, digest, version))
        if payload is None:
            return None
        return json.loads(payload)
    except RedisConnectionError:
        return None
    except Exception:
        return None


def set_json(namespace: str, key_parts: tuple[Any, ...], value: Any) -> None:
    try:
        version = namespace_version(namespace)
        if version == 0:
            return
        digest = make_digest(*key_parts)
        _redis_client().setex(_cache_key(namespace, digest, version), SEARCH_CACHE_TTL_SECONDS, json.dumps(value, ensure_ascii=True))
    except RedisConnectionError:
        return
    except Exception:
        return


def invalidate(namespace: str) -> None:
    try:
        bump_namespace(namespace)
    except Exception:
        return