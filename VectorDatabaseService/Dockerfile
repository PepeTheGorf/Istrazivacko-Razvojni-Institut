FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*
RUN pip install --upgrade pip setuptools wheel

# Install CPU PyTorch wheel from the official PyTorch index
RUN pip install --no-cache-dir torch==2.3.1 --extra-index-url https://download.pytorch.org/whl/cpu

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')"

COPY app.py ./
COPY config.py ./
COPY controller/ ./controller/
COPY ingest/ ./ingest/
COPY model/ ./model/
COPY repository/ ./repository/
COPY schema/ ./schema/
COPY service/ ./service/
COPY services/ ./services/

COPY entrypoint.sh ./entrypoint.sh
RUN sed -i 's/\r$//' ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

EXPOSE 8000

ENTRYPOINT ["./entrypoint.sh"]
