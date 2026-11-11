FROM python:3.12.1-slim
ENV PYTHONUNBUFFERED=1
WORKDIR /app

COPY fraud-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY proto/ ./proto/
RUN python -m grpc_tools.protoc \
    -I./proto \
    --python_out=. \
    --grpc_python_out=. \
    --pyi_out=. \
    ./proto/fraud_check.proto

COPY fraud-service/server.py .

EXPOSE 50051
CMD ["python", "server.py"]