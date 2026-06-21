from fastapi import APIRouter, Query
from pydantic import BaseModel

from service.document_management_service import document_management_service

router = APIRouter(prefix="/api/v1/search", tags=["search"])


class SearchRequest(BaseModel):
    query: str
    top_k: int = 100
    threshold: float = 0.30


@router.post("/documents")
def search_documents_post(body: SearchRequest) -> dict:
    results = document_management_service.search_documents(
        query=body.query, top_k=body.top_k, filter_expr=""
    )
    filtered = [
        {"document_id": r["id"], "score": r.get("score", 0.0)}
        for r in results
        if r.get("score", 0.0) >= body.threshold
    ]
    return {"results": filtered}


@router.get("/debug")
def search_debug(query: str, top_k: int = Query(default=10, ge=1, le=50)) -> dict:
    """Dev-only endpoint: returns raw scores with no threshold filtering."""
    results = document_management_service.search_documents(query=query, top_k=top_k, filter_expr="")
    return {
        "query": query,
        "count": len(results),
        "results": [
            {
                "id": r.get("id"),
                "title": r.get("title", ""),
                "score": r.get("score", 0.0),
                "semantic_score": r.get("semantic_score", 0.0),
                "lexical_score": r.get("lexical_score", 0.0),
                "content_preview": (r.get("content") or "")[:200],
            }
            for r in results
        ],
    }


@router.get("/documents")
def search_documents(
    query: str,
    top_k: int = Query(default=10, ge=1, le=50),
    doc_type_id: str | None = None,
    author_id: str | None = None,
    project_id: str | None = None,
    folder_id: str | None = None,
    is_archived: bool | None = None,
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
    results = document_management_service.search_documents(query=query, top_k=top_k, filter_expr=filter_expr)
    return {"count": len(results), "results": results}


@router.get("/documents/iterator")
def search_documents_iterator(
    query: str,
    top_k: int = Query(default=10, ge=1, le=50),
    page_size: int = Query(default=50, ge=5, le=200),
    doc_type_id: str | None = None,
    author_id: str | None = None,
    project_id: str | None = None,
    folder_id: str | None = None,
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
    filter_expr = " && ".join(filters)
    results = document_management_service.search_documents_iterator(
        query=query,
        top_k=top_k,
        page_size=page_size,
        filter_expr=filter_expr,
    )
    return {"count": len(results), "results": results}


@router.get("/chunks/hybrid")
def search_chunks_hybrid(
    query: str,
    top_k: int = Query(default=10, ge=1, le=50),
    document_id: int | None = None,
    source_page: int | None = None,
) -> dict:
    filters = []
    if document_id is not None:
        filters.append(f"document_id == {document_id}")
    if source_page is not None:
        filters.append(f"source_page == {source_page}")
    filter_expr = " && ".join(filters)
    results = document_management_service.search_chunks_hybrid(query=query, top_k=top_k, filter_expr=filter_expr)
    return {"count": len(results), "results": results}
