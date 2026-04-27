from datetime import datetime, timezone

from config import SEED_CHUNK_COUNT, SEED_DOCUMENT_COUNT
from model.document_models import ChunkCreate, DocumentCreate
from repository.chunk_repository import chunk_repository
from repository.document_repository import document_repository
from services.embedding_service import embedding_service


TOPICS = [
    ("Project charter", "governance", "planning"),
    ("Architecture decision", "systems", "design"),
    ("Research note", "analysis", "study"),
    ("Meeting minutes", "coordination", "discussion"),
    ("Tagging guideline", "metadata", "classification"),
    ("Compliance memo", "policy", "review"),
    ("Knowledge brief", "insights", "summary"),
    ("Experiment log", "testing", "results"),
]


def _now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def _document_payload(index: int) -> DocumentCreate:
    topic, area, focus = TOPICS[index % len(TOPICS)]
    return DocumentCreate(
        title=f"{topic} {index:03d}",
        author=f"Author {index % 12 + 1}",
        doc_type=area,
        project_id=index % 8 + 1,
        content=(
            f"This document discusses {focus} in the context of {area}. "
            f"It contains semantically related descriptions, responsibilities, and follow-up actions. "
            f"Record number {index} is intentionally phrased with natural language variety for semantic search."
        ),
        created_at=_now(),
        updated_at=_now(),
        is_archived=False,
    )


def seed_demo_data() -> None:
    if document_repository.count() >= SEED_DOCUMENT_COUNT and chunk_repository.count() >= SEED_CHUNK_COUNT:
        return

    document_repository.reset()
    chunk_repository.reset()

    document_records = []
    chunk_records = []

    for index in range(SEED_DOCUMENT_COUNT):
        payload = _document_payload(index)
        embedding = embedding_service.encode_text_one(f"{payload.title}\n{payload.content}")
        document_records.append({**payload.model_dump(), "content_embedding": embedding})

    inserted_documents = document_repository.insert(document_records)
    document_ids = inserted_documents["ids"]

    for index, document_id in enumerate(document_ids):
        payload = _document_payload(index)
        sections = [
            (0, "Overview", f"{payload.title} overview for semantic retrieval and fuzzy keyword matching."),
            (1, "Details", f"Detailed explanation about {payload.doc_type}, {payload.author}, and {payload.project_id}."),
        ]
        for chunk_index, section_title, chunk_text in sections:
            chunk_records.append(
                {
                    **ChunkCreate(
                        document_id=document_id,
                        chunk_index=chunk_index,
                        section_title=section_title,
                        chunk_text=chunk_text,
                        source_page=index % 5 + 1,
                        created_at=_now(),
                        updated_at=_now(),
                    ).model_dump(),
                    "chunk_embedding": embedding_service.encode_text_one(f"{section_title}\n{chunk_text}"),
                }
            )

    while len(chunk_records) < SEED_CHUNK_COUNT:
        index = len(chunk_records)
        document_id = document_ids[index % len(document_ids)]
        chunk_text = f"Supplementary chunk {index} about document search, tagging, and metadata-driven retrieval."
        chunk_records.append(
            {
                **ChunkCreate(
                    document_id=document_id,
                    chunk_index=index % 4,
                    section_title="Supplementary",
                    chunk_text=chunk_text,
                    source_page=index % 7 + 1,
                    created_at=_now(),
                    updated_at=_now(),
                ).model_dump(),
                "chunk_embedding": embedding_service.encode_text_one(f"Supplementary\n{chunk_text}"),
            }
        )

    chunk_repository.insert(chunk_records)
