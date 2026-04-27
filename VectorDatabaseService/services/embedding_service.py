from functools import lru_cache

import numpy as np
from sentence_transformers import SentenceTransformer

from config import TEXT_EMBEDDING_MODEL


class EmbeddingService:
    def __init__(self) -> None:
        self._model = SentenceTransformer(TEXT_EMBEDDING_MODEL)

    def encode_texts(self, texts: list[str]) -> list[list[float]]:
        embeddings = self._model.encode(
            texts,
            normalize_embeddings=True,
            convert_to_numpy=True,
            show_progress_bar=False,
        )
        return embeddings.astype(np.float32).tolist()

    def encode_text_one(self, text: str) -> list[float]:
        return self.encode_texts([text])[0]

    def zero_vector(self) -> list[float]:
        return [0.0] * self._model.get_sentence_embedding_dimension()


@lru_cache(maxsize=1)
def _get_embedding_service() -> EmbeddingService:
    return EmbeddingService()


embedding_service = _get_embedding_service()
