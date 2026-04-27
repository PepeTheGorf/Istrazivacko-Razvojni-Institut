import os


APP_HOST = os.getenv("APP_HOST", "0.0.0.0")
APP_PORT = int(os.getenv("APP_PORT", "8000"))
APP_NAME = os.getenv("APP_NAME", "doucument-management-service")

EUREKA_SERVER = os.getenv(
    "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
    os.getenv("EUREKA_URL", "http://eureka-server:8761/eureka"),
)

MILVUS_HOST = os.getenv("MILVUS_HOST", "standalone")
MILVUS_PORT = int(os.getenv("MILVUS_PORT", "19530"))
MILVUS_URI = os.getenv("MILVUS_URI", f"http://{MILVUS_HOST}:{MILVUS_PORT}")

TEXT_EMBEDDING_MODEL = os.getenv(
    "TEXT_EMBEDDING_MODEL",
    "sentence-transformers/all-MiniLM-L6-v2",
)
EMBEDDING_DIM = int(os.getenv("EMBEDDING_DIM", "384"))

DOCUMENT_COLLECTION = os.getenv("DOCUMENT_COLLECTION", "documents")
CHUNK_COLLECTION = os.getenv("CHUNK_COLLECTION", "document_chunks")

DEFAULT_TOP_K = int(os.getenv("DEFAULT_TOP_K", "10"))
DEFAULT_BATCH_SIZE = int(os.getenv("DEFAULT_BATCH_SIZE", "64"))
INDEX_NLIST = int(os.getenv("INDEX_NLIST", "64"))
INDEX_NPROBE = int(os.getenv("INDEX_NPROBE", "16"))

AUTO_SEED_ON_STARTUP = os.getenv("AUTO_SEED_ON_STARTUP", "true").lower() == "true"
SEED_DOCUMENT_COUNT = int(os.getenv("SEED_DOCUMENT_COUNT", "250"))
SEED_CHUNK_COUNT = int(os.getenv("SEED_CHUNK_COUNT", "500"))
