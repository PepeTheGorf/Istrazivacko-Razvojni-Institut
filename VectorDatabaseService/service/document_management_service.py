from __future__ import annotations

from datetime import datetime, timezone

import numpy as np

from config import AUTO_SEED_ON_STARTUP, SEED_CHUNK_COUNT, SEED_DOCUMENT_COUNT
from model.document_models import ChunkCreate, ChunkUpdate, DocumentCreate, DocumentUpdate
from repository.chunk_repository import chunk_repository
from repository.document_repository import document_repository
from services.redis_cache import get_json, invalidate, set_json
from services.embedding_service import embedding_service


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def _merge_optional(existing: dict, update: dict) -> dict:
    merged = dict(existing)
    for key, value in update.items():
        if value is not None:
            merged[key] = value
    return merged


def _normalize_optional_id(value: str | None) -> str | None:
    if value is None:
        return None
    normalized = value.strip()
    return normalized if normalized else None


def _tokenize(text: str) -> set[str]:
    cleaned = "".join(ch.lower() if ch.isalnum() else " " for ch in text)
    return {token for token in cleaned.split() if token}


def _normalize_text(text: str) -> str:
    replacements = {
        "č": "c",
        "ć": "c",
        "đ": "dj",
        "š": "s",
        "ž": "z",
        "Č": "c",
        "Ć": "c",
        "Đ": "dj",
        "Š": "s",
        "Ž": "z",
    }
    return "".join(replacements.get(ch, ch.lower()) for ch in text)


def _document_search_tokens(row: dict) -> set[str]:
    parts: list[str] = [
        row.get("title", ""),
        row.get("content", ""),
        " ".join(row.get("tags") or []),
    ]
    metadata = row.get("metadata") or {}
    parts.extend(f"{key} {value}" for key, value in metadata.items())
    return _tokenize(_normalize_text(" ".join(parts)))


def _cosine(left: list[float], right: list[float]) -> float:
    left_vec = np.asarray(left, dtype=np.float32)
    right_vec = np.asarray(right, dtype=np.float32)
    denom = float(np.linalg.norm(left_vec) * np.linalg.norm(right_vec))
    if denom == 0.0:
        return 0.0
    return float(np.dot(left_vec, right_vec) / denom)


class _DocumentRowIterator:
    def __init__(self, filter_expr: str, page_size: int) -> None:
        self.filter_expr = filter_expr
        self.page_size = page_size
        self.offset = 0
        self._current_page: list[dict] = []
        self._page_index = 0

    def __iter__(self) -> "_DocumentRowIterator":
        return self

    def __next__(self) -> dict:
        while self._page_index >= len(self._current_page):
            self._current_page = document_repository.list(
                filter_expr=self.filter_expr,
                limit=self.page_size,
                offset=self.offset,
                include_vectors=True,
            )
            self._page_index = 0
            self.offset += self.page_size
            if not self._current_page:
                raise StopIteration

        row = self._current_page[self._page_index]
        self._page_index += 1
        return row


class DocumentManagementService:
    SEARCH_NAMESPACE = "vector-search"

    def bootstrap(self) -> None:
        document_repository.ensure_collection()
        chunk_repository.ensure_collection()

        if not AUTO_SEED_ON_STARTUP:
            return

        if document_repository.count() < SEED_DOCUMENT_COUNT or chunk_repository.count() < SEED_CHUNK_COUNT:
            from ingest.seed_demo_data import seed_demo_data

            seed_demo_data()

    def create_document(self, payload: DocumentCreate) -> dict:
        timestamp = payload.created_at or _now()
        updated_at = payload.updated_at or timestamp
        folder_id = _normalize_optional_id(payload.folder_id)
        tags_text = " ".join(payload.tags or [])
        metadata_text = " ".join(f"{k}: {v}" for k, v in (payload.metadata or {}).items())
        embedding_text = f"{payload.title}\n{payload.content}\n{tags_text}\n{metadata_text}"
        embedding = embedding_service.encode_text_one(embedding_text)
        record = payload.model_dump()
        record.update({
            "folder_id": folder_id,
            "created_at": timestamp,
            "updated_at": updated_at,
            "content_embedding": embedding,
        })
        result = document_repository.insert([record])
        invalidate(self.SEARCH_NAMESPACE)
        return {"inserted_ids": result["ids"], "insert_count": result["insert_count"]}

    def create_chunk(self, payload: ChunkCreate) -> dict:
        timestamp = payload.created_at or _now()
        updated_at = payload.updated_at or timestamp
        embedding = embedding_service.encode_text_one(f"{payload.section_title}\n{payload.chunk_text}")
        record = payload.model_dump()
        record.update({"created_at": timestamp, "updated_at": updated_at, "chunk_embedding": embedding})
        result = chunk_repository.insert([record])
        invalidate(self.SEARCH_NAMESPACE)
        return {"inserted_ids": result["ids"], "insert_count": result["insert_count"]}

    def get_document(self, document_id: int, include_vectors: bool = False) -> dict | None:
        return document_repository.get_by_id(document_id, include_vectors=include_vectors)

    def get_chunk(self, chunk_id: int, include_vectors: bool = False) -> dict | None:
        return chunk_repository.get_by_id(chunk_id, include_vectors=include_vectors)

    def update_document(self, document_id: int, payload: DocumentUpdate) -> dict:
        current = document_repository.get_by_id(document_id, include_vectors=True)
        if current is None:
            raise KeyError(f"Document {document_id} not found")
        update_data = payload.model_dump(exclude_none=True)

        # Avoid overwriting optional IDs with blank strings coming from UI forms.
        for optional_id_key in ("folder_id", "project_id", "doc_type_id", "author_id"):
            if optional_id_key in update_data and isinstance(update_data[optional_id_key], str):
                normalized = _normalize_optional_id(update_data[optional_id_key])
                if normalized is None:
                    update_data.pop(optional_id_key, None)
                else:
                    update_data[optional_id_key] = normalized

        merged = _merge_optional(current, update_data)
        if any(field in update_data for field in ("title", "content", "tags", "metadata")):
            tags_text = " ".join(merged.get("tags") or [])
            metadata_text = " ".join(f"{k}: {v}" for k, v in (merged.get("metadata") or {}).items())
            embedding_text = f"{merged.get('title', '')}\n{merged.get('content', '')}\n{tags_text}\n{metadata_text}"
            merged["content_embedding"] = embedding_service.encode_text_one(embedding_text)
        merged["updated_at"] = payload.updated_at or _now()
        result = document_repository.upsert(merged)
        invalidate(self.SEARCH_NAMESPACE)
        ids = result.get("ids") or []
        return {
            "upserted_count": result["upsert_count"],
            "ids": ids,
            "id": ids[0] if ids else None,
            "previous_id": document_id,
        }

    def update_chunk(self, chunk_id: int, payload: ChunkUpdate) -> dict:
        current = chunk_repository.get_by_id(chunk_id, include_vectors=True)
        if current is None:
            raise KeyError(f"Chunk {chunk_id} not found")
        merged = _merge_optional(current, payload.model_dump(exclude_none=True))
        if any(field in payload.model_dump(exclude_none=True) for field in ("section_title", "chunk_text")):
            merged["chunk_embedding"] = embedding_service.encode_text_one(f"{merged.get('section_title', '')}\n{merged.get('chunk_text', '')}")
        merged["updated_at"] = payload.updated_at or _now()
        result = chunk_repository.upsert(merged)
        invalidate(self.SEARCH_NAMESPACE)
        return {"upserted_count": result["upsert_count"]}

    def delete_document(self, document_id: int) -> dict:
        result = document_repository.delete_by_id(document_id)
        invalidate(self.SEARCH_NAMESPACE)
        return result

    def delete_chunk(self, chunk_id: int) -> dict:
        result = chunk_repository.delete_by_id(chunk_id)
        invalidate(self.SEARCH_NAMESPACE)
        return result

    def list_documents(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return document_repository.list(filter_expr=filter_expr, limit=limit, offset=offset)

    def list_chunks(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return chunk_repository.list(filter_expr=filter_expr, limit=limit, offset=offset)

    def count_documents(self, filter_expr: str = "") -> int:
        return document_repository.count(filter_expr=filter_expr)

    def count_chunks(self, filter_expr: str = "") -> int:
        return chunk_repository.count(filter_expr=filter_expr)

    def search_documents(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
        cache_key = ("search_documents", query, top_k, filter_expr)
        cached = get_json(self.SEARCH_NAMESPACE, cache_key)
        if cached is not None:
            return cached

        # Use Milvus ANN index for accurate semantic search, then enrich with lexical score
        query_vector = embedding_service.encode_text_one(query)
        query_tokens = _tokenize(_normalize_text(query))
        ann_hits = document_repository.search([query_vector], top_k=top_k, filter_expr=filter_expr)[0]
        results = []
        for hit in ann_hits:
            semantic_score = float(hit.get("score", 0.0))
            lexical_tokens = _document_search_tokens(hit)
            overlap = len(query_tokens & lexical_tokens)
            lexical_score = overlap / max(len(query_tokens), 1)
            score = (0.75 * semantic_score) + (0.25 * lexical_score)
            enriched = {k: v for k, v in hit.items() if k != "content_embedding"}
            enriched["semantic_score"] = round(semantic_score, 4)
            enriched["lexical_score"] = round(lexical_score, 4)
            enriched["score"] = round(score, 4)
            results.append(enriched)
        results.sort(key=lambda item: item.get("score", 0.0), reverse=True)
        set_json(self.SEARCH_NAMESPACE, cache_key, results)
        return results

    def search_chunks(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
        vector = embedding_service.encode_text_one(query)
        return chunk_repository.search([vector], top_k=top_k, filter_expr=filter_expr)[0]

    def search_documents_iterator(self, query: str, top_k: int = 10, page_size: int = 50, filter_expr: str = "") -> list[dict]:
        cache_key = ("search_documents_iterator", query, top_k, page_size, filter_expr)
        cached = get_json(self.SEARCH_NAMESPACE, cache_key)
        if cached is not None:
            return cached

        query_vector = embedding_service.encode_text_one(query)
        query_tokens = _tokenize(_normalize_text(query))
        all_scored: list[dict] = []
        for row in _DocumentRowIterator(filter_expr=filter_expr, page_size=page_size):
            semantic_score = _cosine(query_vector, row.get("content_embedding", embedding_service.zero_vector()))
            lexical_tokens = _document_search_tokens(row)
            overlap = len(query_tokens & lexical_tokens)
            lexical_score = overlap / max(len(query_tokens), 1)
            score = (0.75 * semantic_score) + (0.25 * lexical_score)
            enriched = {k: v for k, v in row.items() if k != "content_embedding"}
            enriched["semantic_score"] = round(semantic_score, 4)
            enriched["lexical_score"] = round(lexical_score, 4)
            enriched["score"] = round(score, 4)
            all_scored.append(enriched)
        all_scored.sort(key=lambda item: item.get("score", 0.0), reverse=True)
        cached_results = all_scored[:top_k]
        set_json(self.SEARCH_NAMESPACE, cache_key, cached_results)
        return cached_results

    def search_chunks_hybrid(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
        cache_key = ("search_chunks_hybrid", query, top_k, filter_expr)
        cached = get_json(self.SEARCH_NAMESPACE, cache_key)
        if cached is not None:
            return cached

        dense_hits = self.search_chunks(query=query, top_k=max(top_k * 3, 30), filter_expr=filter_expr)
        query_tokens = _tokenize(query)
        ranked: list[dict] = []
        for hit in dense_hits:
            chunk_text = f"{hit.get('section_title', '')} {hit.get('chunk_text', '')}"
            lexical_tokens = _tokenize(chunk_text)
            overlap = len(query_tokens & lexical_tokens)
            lexical_ratio = overlap / max(len(query_tokens), 1)
            semantic_score = float(hit.get("score", 0.0))
            fused = (0.75 * semantic_score) + (0.25 * lexical_ratio)
            enriched = dict(hit)
            enriched["lexical_score"] = round(lexical_ratio, 4)
            enriched["fused_score"] = round(fused, 4)
            ranked.append(enriched)
        ranked.sort(key=lambda item: item.get("fused_score", 0.0), reverse=True)
        results = ranked[:top_k]
        set_json(self.SEARCH_NAMESPACE, cache_key, results)
        return results


document_management_service = DocumentManagementService()
