from pymilvus import MilvusClient

from config import MILVUS_URI


class MilvusService:
    def __init__(self) -> None:
        self.client = MilvusClient(uri=MILVUS_URI)


milvus_service = MilvusService()
