from fastapi import APIRouter, HTTPException, Query

from model.document_models import DocumentCreate, DocumentUpdate
from service.document_management_service import document_management_service

router = APIRouter(prefix="/api/v1/documents", tags=["documents"])


@router.post("")
def create_document(payload: DocumentCreate) -> dict:
    return document_management_service.create_document(payload)


@router.get("/{document_id}")
def get_document(document_id: int) -> dict:
    row = document_management_service.get_document(document_id)
    if row is None:
        raise HTTPException(status_code=404, detail="Document not found")
    return row


@router.get("")
def list_documents(
    doc_type_id: str | None = None,
    author_id: str | None = None,
    project_id: str | None = None,
    folder_id: str | None = None,
    is_archived: bool | None = None,
    limit: int = Query(default=20, ge=1, le=200),
    offset: int = Query(default=0, ge=0),
) -> dict:
    filters = []
    if doc_type_id:
        filters.append(f'doc_type_id == "{doc_type_id}"')
    if author_id:
        filters.append(f'author_id == "{author_id}"')
    if project_id:
        filters.append(f'project_id == "{project_id}"')
    if folder_id:
        filters.append(f'folder_id == "{folder_id}"')
    if is_archived is not None:
        filters.append(f"is_archived == {'true' if is_archived else 'false'}")
    filter_expr = " && ".join(filters)
    rows = document_management_service.list_documents(filter_expr=filter_expr, limit=limit, offset=offset)
    return {"count": len(rows), "results": rows}


@router.put("/{document_id}")
def update_document(document_id: int, payload: DocumentUpdate) -> dict:
    try:
        return document_management_service.update_document(document_id, payload)
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.delete("/{document_id}")
def delete_document(document_id: int) -> dict:
    return document_management_service.delete_document(document_id)


@router.get("/{document_id}/preview")
def preview_document(document_id: int) -> dict:
    row = document_management_service.get_document(document_id)
    if row is None:
        raise HTTPException(status_code=404, detail="Document not found")
    return {
        "id": row["id"],
        "title": row.get("title"),
        "author_id": row.get("author_id"),
        "doc_type_id": row.get("doc_type_id"),
        "project_id": row.get("project_id"),
        "folder_id": row.get("folder_id"),
        "tags": row.get("tags"),
        "metadata": row.get("metadata"),
        "content_preview": (row.get("content", "") or "")[:300],
    }


@router.get("/stats")
def document_stats() -> dict:
    return {"row_count": document_management_service.count_documents()}
