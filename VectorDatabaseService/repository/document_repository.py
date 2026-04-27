from config import DOCUMENT_COLLECTION
from repository.base_repository import BaseMilvusRepository
from schema.milvus_schema import DOCUMENT_SCALAR_FIELDS, document_index_params, document_schema


class DocumentRepository(BaseMilvusRepository):
    def __init__(self) -> None:
        super().__init__(
            collection_name=DOCUMENT_COLLECTION,
            output_fields=DOCUMENT_SCALAR_FIELDS,
            schema_factory=document_schema,
            index_factory=document_index_params,
            vector_field="content_embedding",
        )


document_repository = DocumentRepository()
