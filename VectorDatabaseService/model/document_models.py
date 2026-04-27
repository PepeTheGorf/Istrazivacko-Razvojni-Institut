from typing import Optional

from pydantic import BaseModel


class DocumentCreate(BaseModel):
    title: str
    author: str
    doc_type: str
    project_id: int
    content: str
    created_at: Optional[str] = None
    updated_at: Optional[str] = None
    is_archived: bool = False


class DocumentUpdate(BaseModel):
    title: Optional[str] = None
    author: Optional[str] = None
    doc_type: Optional[str] = None
    project_id: Optional[int] = None
    content: Optional[str] = None
    updated_at: Optional[str] = None
    is_archived: Optional[bool] = None


class ChunkCreate(BaseModel):
    document_id: int
    chunk_index: int
    section_title: str = ""
    chunk_text: str
    source_page: int = 1
    created_at: Optional[str] = None
    updated_at: Optional[str] = None


class ChunkUpdate(BaseModel):
    document_id: Optional[int] = None
    chunk_index: Optional[int] = None
    section_title: Optional[str] = None
    chunk_text: Optional[str] = None
    source_page: Optional[int] = None
    updated_at: Optional[str] = None
