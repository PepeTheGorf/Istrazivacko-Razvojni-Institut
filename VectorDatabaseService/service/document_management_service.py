from __future__ import annotations

from datetime import datetime, timezone

import numpy as np

from config import AUTO_SEED_ON_STARTUP, SEED_CHUNK_COUNT, SEED_DOCUMENT_COUNT
from model.document_models import ChunkCreate, ChunkUpdate, DocumentCreate, DocumentUpdate
from repository.chunk_repository import chunk_repository
from repository.document_repository import document_repository
from services.embedding_service import embedding_service


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def _merge_optional(existing: dict, update: dict) -> dict:
    merged = dict(existing)
    for key, value in update.items():
        if value is not None:
            merged[key] = value
    return merged


def _tokenize(text: str) -> set[str]:
    cleaned = "".join(ch.lower() if ch.isalnum() else " " for ch in text)
    return {token for token in cleaned.split() if token}


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
        embedding = embedding_service.encode_text_one(f"{payload.title}\n{payload.content}")
        record = payload.model_dump()
        record.update({"created_at": timestamp, "updated_at": updated_at, "content_embedding": embedding})
        result = document_repository.insert([record])
        return {"inserted_ids": result["ids"], "insert_count": result["insert_count"]}

    def create_chunk(self, payload: ChunkCreate) -> dict:
        timestamp = payload.created_at or _now()
        updated_at = payload.updated_at or timestamp
        embedding = embedding_service.encode_text_one(f"{payload.section_title}\n{payload.chunk_text}")
        record = payload.model_dump()
        record.update({"created_at": timestamp, "updated_at": updated_at, "chunk_embedding": embedding})
        result = chunk_repository.insert([record])
        return {"inserted_ids": result["ids"], "insert_count": result["insert_count"]}

    def get_document(self, document_id: int, include_vectors: bool = False) -> dict | None:
        return document_repository.get_by_id(document_id, include_vectors=include_vectors)

    def get_chunk(self, chunk_id: int, include_vectors: bool = False) -> dict | None:
        return chunk_repository.get_by_id(chunk_id, include_vectors=include_vectors)

    def update_document(self, document_id: int, payload: DocumentUpdate) -> dict:
        current = document_repository.get_by_id(document_id, include_vectors=True)
        if current is None:
            raise KeyError(f"Document {document_id} not found")
        merged = _merge_optional(current, payload.model_dump(exclude_none=True))
        if any(field in payload.model_dump(exclude_none=True) for field in ("title", "content")):
            merged["content_embedding"] = embedding_service.encode_text_one(f"{merged.get('title', '')}\n{merged.get('content', '')}")
        merged["updated_at"] = payload.updated_at or _now()
        result = document_repository.upsert(merged)
        return {"upserted_count": result["upsert_count"]}

    def update_chunk(self, chunk_id: int, payload: ChunkUpdate) -> dict:
        current = chunk_repository.get_by_id(chunk_id, include_vectors=True)
        if current is None:
            raise KeyError(f"Chunk {chunk_id} not found")
        merged = _merge_optional(current, payload.model_dump(exclude_none=True))
        if any(field in payload.model_dump(exclude_none=True) for field in ("section_title", "chunk_text")):
            merged["chunk_embedding"] = embedding_service.encode_text_one(f"{merged.get('section_title', '')}\n{merged.get('chunk_text', '')}")
        merged["updated_at"] = payload.updated_at or _now()
        result = chunk_repository.upsert(merged)
        return {"upserted_count": result["upsert_count"]}

    def delete_document(self, document_id: int) -> dict:
        return document_repository.delete_by_id(document_id)

    def delete_chunk(self, chunk_id: int) -> dict:
        return chunk_repository.delete_by_id(chunk_id)

    def list_documents(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return document_repository.list(filter_expr=filter_expr, limit=limit, offset=offset)

    def list_chunks(self, filter_expr: str = "", limit: int = 20, offset: int = 0) -> list[dict]:
        return chunk_repository.list(filter_expr=filter_expr, limit=limit, offset=offset)

    def count_documents(self, filter_expr: str = "") -> int:
        return document_repository.count(filter_expr=filter_expr)

    def count_chunks(self, filter_expr: str = "") -> int:
        return chunk_repository.count(filter_expr=filter_expr)

    def search_documents(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
        vector = embedding_service.encode_text_one(query)
        return document_repository.search([vector], top_k=top_k, filter_expr=filter_expr)[0]

    def search_chunks(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
        vector = embedding_service.encode_text_one(query)
        return chunk_repository.search([vector], top_k=top_k, filter_expr=filter_expr)[0]

    def search_documents_iterator(self, query: str, top_k: int = 10, page_size: int = 50, filter_expr: str = "") -> list[dict]:
        query_vector = embedding_service.encode_text_one(query)
        results: list[dict] = []
        for row in _DocumentRowIterator(filter_expr=filter_expr, page_size=page_size):
            score = _cosine(query_vector, row.get("content_embedding", embedding_service.zero_vector()))
            enriched = dict(row)
            enriched["score"] = round(score, 4)
            results.append(enriched)
            if len(results) >= top_k:
                break
        results.sort(key=lambda item: item.get("score", 0.0), reverse=True)
        return results[:top_k]

    def search_chunks_hybrid(self, query: str, top_k: int = 10, filter_expr: str = "") -> list[dict]:
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
        return ranked[:top_k]


document_management_service = DocumentManagementService()
