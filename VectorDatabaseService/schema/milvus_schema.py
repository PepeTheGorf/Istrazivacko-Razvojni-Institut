from pymilvus import DataType, MilvusClient

from config import EMBEDDING_DIM, INDEX_NLIST

DOCUMENT_SCALAR_FIELDS = [
    "id",
    "title",
    "author",
    "doc_type",
    "project_id",
    "content",
    "created_at",
    "updated_at",
    "is_archived",
]

CHUNK_SCALAR_FIELDS = [
    "id",
    "document_id",
    "chunk_index",
    "section_title",
    "chunk_text",
    "source_page",
    "created_at",
    "updated_at",
]


def document_schema(client: MilvusClient):
    schema = client.create_schema(auto_id=True, enable_dynamic_fields=False)
    schema.add_field("id", DataType.INT64, is_primary=True)
    schema.add_field("title", DataType.VARCHAR, max_length=512)
    schema.add_field("author", DataType.VARCHAR, max_length=256)
    schema.add_field("doc_type", DataType.VARCHAR, max_length=128)
    schema.add_field("project_id", DataType.INT64)
    schema.add_field("content", DataType.VARCHAR, max_length=65535)
    schema.add_field("created_at", DataType.VARCHAR, max_length=32)
    schema.add_field("updated_at", DataType.VARCHAR, max_length=32)
    schema.add_field("is_archived", DataType.BOOL)
    schema.add_field("content_embedding", DataType.FLOAT_VECTOR, dim=EMBEDDING_DIM)
    return schema


def chunk_schema(client: MilvusClient):
    schema = client.create_schema(auto_id=True, enable_dynamic_fields=False)
    schema.add_field("id", DataType.INT64, is_primary=True)
    schema.add_field("document_id", DataType.INT64)
    schema.add_field("chunk_index", DataType.INT32)
    schema.add_field("section_title", DataType.VARCHAR, max_length=256)
    schema.add_field("chunk_text", DataType.VARCHAR, max_length=65535)
    schema.add_field("source_page", DataType.INT32)
    schema.add_field("created_at", DataType.VARCHAR, max_length=32)
    schema.add_field("updated_at", DataType.VARCHAR, max_length=32)
    schema.add_field("chunk_embedding", DataType.FLOAT_VECTOR, dim=EMBEDDING_DIM)
    return schema


def document_index_params(client: MilvusClient):
    index_params = client.prepare_index_params()
    index_params.add_index("author", index_type="INVERTED")
    index_params.add_index("doc_type", index_type="INVERTED")
    index_params.add_index("project_id", index_type="INVERTED")
    index_params.add_index(
        field_name="content_embedding",
        index_type="IVF_FLAT",
        metric_type="COSINE",
        index_name="content_embedding_index",
        params={"nlist": INDEX_NLIST},
    )
    return index_params


def chunk_index_params(client: MilvusClient):
    index_params = client.prepare_index_params()
    index_params.add_index("document_id", index_type="INVERTED")
    index_params.add_index("chunk_index", index_type="INVERTED")
    index_params.add_index("source_page", index_type="INVERTED")
    index_params.add_index(
        field_name="chunk_embedding",
        index_type="IVF_FLAT",
        metric_type="COSINE",
        index_name="chunk_embedding_index",
        params={"nlist": INDEX_NLIST},
    )
    return index_params
