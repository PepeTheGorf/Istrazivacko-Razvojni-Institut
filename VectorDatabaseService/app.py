import logging
import os
import socket

import uvicorn
from fastapi import FastAPI
from fastapi.responses import RedirectResponse
from fastapi.middleware.cors import CORSMiddleware

from config import APP_HOST, APP_NAME, APP_PORT, EUREKA_SERVER
from controller.chunk_controller import router as chunk_router
from controller.document_controller import router as document_router
from controller.health_controller import router as health_router
from controller.search_controller import router as search_router
from service.document_management_service import document_management_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Doucument Management Service",
    description="Milvus-backed vector service for semantic document search and tagging support.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router)
app.include_router(document_router)
app.include_router(chunk_router)
app.include_router(search_router)


@app.get("/", include_in_schema=False)
def root_redirect():
    return RedirectResponse(url="/docs")


def _get_ip() -> str:
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sock.connect(("10.254.254.254", 1))
        return sock.getsockname()[0]
    except Exception:
        return "127.0.0.1"
    finally:
        sock.close()


def _register_eureka() -> None:
    try:
        from py_eureka_client import eureka_client

        instance_host = os.getenv("INSTANCE_HOST", _get_ip())
        eureka_client.init(
            eureka_server=EUREKA_SERVER,
            app_name=APP_NAME,
            instance_host=instance_host,
            instance_port=APP_PORT,
            health_check_url=f"http://{instance_host}:{APP_PORT}/health",
        )
        logger.info("Registered with Eureka at %s", EUREKA_SERVER)
    except Exception as exc:
        logger.warning("Eureka registration failed (non-fatal): %s", exc)


@app.on_event("startup")
async def startup() -> None:
    _register_eureka()
    document_management_service.bootstrap()
    logger.info("Doucument Management Service started on %s:%s", APP_HOST, APP_PORT)


if __name__ == "__main__":
    uvicorn.run("app:app", host=APP_HOST, port=APP_PORT, reload=False)
