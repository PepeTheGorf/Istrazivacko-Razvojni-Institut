from fastapi import APIRouter

from service.document_management_service import document_management_service

router = APIRouter(tags=["health"])


@router.get("/health")
def health() -> dict:
    return {"status": "ok"}


@router.get("/ready")
def ready() -> dict:
    return {
        "status": "ready",
        "documents": document_management_service.count_documents(),
        "chunks": document_management_service.count_chunks(),
    }
