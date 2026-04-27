from fastapi import APIRouter, HTTPException, Query

from model.document_models import ChunkCreate, ChunkUpdate
from service.document_management_service import document_management_service

router = APIRouter(prefix="/api/v1/chunks", tags=["chunks"])


@router.post("")
def create_chunk(payload: ChunkCreate) -> dict:
    return document_management_service.create_chunk(payload)


@router.get("/{chunk_id}")
def get_chunk(chunk_id: int) -> dict:
    row = document_management_service.get_chunk(chunk_id)
    if row is None:
        raise HTTPException(status_code=404, detail="Chunk not found")
    return row


@router.get("")
def list_chunks(
    document_id: int | None = None,
    section_title: str | None = None,
    source_page: int | None = None,
    limit: int = Query(default=20, ge=1, le=200),
    offset: int = Query(default=0, ge=0),
) -> dict:
    filters = []
    if document_id is not None:
        filters.append(f"document_id == {document_id}")
    if section_title:
        filters.append(f'section_title == "{section_title}"')
    if source_page is not None:
        filters.append(f"source_page == {source_page}")
    filter_expr = " && ".join(filters)
    rows = document_management_service.list_chunks(filter_expr=filter_expr, limit=limit, offset=offset)
    return {"count": len(rows), "results": rows}


@router.put("/{chunk_id}")
def update_chunk(chunk_id: int, payload: ChunkUpdate) -> dict:
    try:
        return document_management_service.update_chunk(chunk_id, payload)
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.delete("/{chunk_id}")
def delete_chunk(chunk_id: int) -> dict:
    return document_management_service.delete_chunk(chunk_id)


@router.get("/stats")
def chunk_stats() -> dict:
    return {"row_count": document_management_service.count_chunks()}
