from fastapi import APIRouter, Query

from service.document_management_service import document_management_service

router = APIRouter(prefix="/api/v1/search", tags=["search"])


@router.get("/documents")
def search_documents(
    query: str,
    top_k: int = Query(default=10, ge=1, le=50),
    doc_type: str | None = None,
    author: str | None = None,
    project_id: int | None = None,
    is_archived: bool | None = None,
) -> dict:
    filters = []
    if doc_type:
        filters.append(f'doc_type == "{doc_type}"')
    if author:
        filters.append(f'author == "{author}"')
    if project_id is not None:
        filters.append(f"project_id == {project_id}")
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
    doc_type: str | None = None,
    author: str | None = None,
    project_id: int | None = None,
) -> dict:
    filters = []
    if doc_type:
        filters.append(f'doc_type == "{doc_type}"')
    if author:
        filters.append(f'author == "{author}"')
    if project_id is not None:
        filters.append(f"project_id == {project_id}")
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
