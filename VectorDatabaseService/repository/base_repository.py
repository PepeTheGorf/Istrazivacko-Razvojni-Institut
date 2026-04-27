from __future__ import annotations
from typing import Callable
from config import INDEX_NPROBE
from services.milvus_service import milvus_service


class BaseMilvusRepository:
    def __init__(
        self,
        collection_name: str,
        output_fields: list[str],
        schema_factory: Callable,
        index_factory: Callable,
        vector_field: str,
    ) -> None:
        self.client = milvus_service.client
        self.collection_name = collection_name
        self.output_fields = output_fields
        self.schema_factory = schema_factory
        self.index_factory = index_factory
        self.vector_field = vector_field
        self.search_params = {"metric_type": "COSINE", "params": {"nprobe": INDEX_NPROBE}}

    def ensure_collection(self) -> None:
        if not self.client.has_collection(self.collection_name):
            schema = self.schema_factory(self.client)
            indexes = self.index_factory(self.client)
            self.client.create_collection(
                collection_name=self.collection_name,
                schema=schema,
                index_params=indexes,
                consistency_level="Strong",
            )
        self.client.load_collection(self.collection_name)

    def reset(self) -> None:
        if self.client.has_collection(self.collection_name):
            self.client.drop_collection(self.collection_name)
        self.ensure_collection()

    def insert(self, records: list[dict]) -> dict:
        result = self.client.insert(collection_name=self.collection_name, data=records)
        return {"insert_count": result.get("insert_count", 0), "ids": list(result.get("ids", []))}

    def upsert(self, records: dict | list[dict]) -> dict:
        payload = [records] if isinstance(records, dict) else records
        result = self.client.upsert(collection_name=self.collection_name, data=payload)
        return {"upsert_count": result.get("upsert_count", 0), "ids": list(result.get("ids", []))}

    def get_by_id(self, entity_id: int, include_vectors: bool = False) -> dict | None:
        fields = list(self.output_fields)
        if include_vectors:
            fields.append(self.vector_field)
        rows = self.client.get(collection_name=self.collection_name, ids=[entity_id], output_fields=fields)
        return rows[0] if rows else None

    def delete_by_id(self, entity_id: int) -> dict:
        result = self.client.delete(collection_name=self.collection_name, ids=[entity_id])
        return {"delete_count": result.get("delete_count", 0)}

    def list(self, filter_expr: str = "", limit: int = 20, offset: int = 0, include_vectors: bool = False) -> list[dict]:
        fields = list(self.output_fields)
        if include_vectors:
            fields.append(self.vector_field)
        return self.client.query(
            collection_name=self.collection_name,
            filter=filter_expr or "",
            output_fields=fields,
            limit=limit,
            offset=offset,
        )

    def count(self, filter_expr: str = "") -> int:
        rows = self.client.query(
            collection_name=self.collection_name,
            filter=filter_expr or "",
            output_fields=["id"],
            limit=10_000,
        )
        return len(rows)

    def search(self, query_vectors: list[list[float]], top_k: int, filter_expr: str = "", include_vectors: bool = False) -> list[list[dict]]:
        fields = list(self.output_fields)
        if include_vectors:
            fields.append(self.vector_field)
        raw = self.client.search(
            collection_name=self.collection_name,
            data=query_vectors,
            anns_field=self.vector_field,
            search_params=self.search_params,
            limit=top_k,
            filter=filter_expr or "",
            output_fields=fields,
            consistency_level="Strong",
        )
        results: list[list[dict]] = []
        for hits in raw:
            batch = []
            for hit in hits:
                if isinstance(hit, dict):
                    hit_id = hit.get("id")
                    distance = hit.get("distance", 0.0)
                    entity = hit.get("entity", {}) or {}
                else:
                    hit_id = getattr(hit, "id", None)
                    distance = getattr(hit, "distance", 0.0)
                    entity = getattr(hit, "entity", {}) or {}
                row = {"id": hit_id, "score": round(float(distance), 4)}
                if hasattr(entity, "items"):
                    entity = dict(entity)
                row.update(entity)
                batch.append(row)
            results.append(batch)
        return results
