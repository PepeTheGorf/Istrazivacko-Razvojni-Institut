from config import CHUNK_COLLECTION
from repository.base_repository import BaseMilvusRepository
from schema.milvus_schema import CHUNK_SCALAR_FIELDS, chunk_index_params, chunk_schema


class ChunkRepository(BaseMilvusRepository):
    def __init__(self) -> None:
        super().__init__(
            collection_name=CHUNK_COLLECTION,
            output_fields=CHUNK_SCALAR_FIELDS,
            schema_factory=chunk_schema,
            index_factory=chunk_index_params,
            vector_field="chunk_embedding",
        )


chunk_repository = ChunkRepository()
