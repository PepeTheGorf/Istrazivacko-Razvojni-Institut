from typing import Dict, List, Optional

from pydantic import BaseModel


class DocumentCreate(BaseModel):
    title: str
    author_id: str
    doc_type_id: str
    project_id: str
    folder_id: Optional[str] = None
    content: str
    tags: Optional[List[str]] = []
    metadata: Optional[Dict[str, str]] = {}
    is_archived: bool = False
    created_at: Optional[str] = None
    updated_at: Optional[str] = None


class DocumentUpdate(BaseModel):
    title: Optional[str] = None
    author_id: Optional[str] = None
    doc_type_id: Optional[str] = None
    project_id: Optional[str] = None
    folder_id: Optional[str] = None
    content: Optional[str] = None
    tags: Optional[List[str]] = None
    metadata: Optional[Dict[str, str]] = None
    is_archived: Optional[bool] = None
    updated_at: Optional[str] = None


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
