import logging
import os
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import grpc

import fraud_check_pb2
import fraud_check_pb2_grpc

BASE_DIR = Path(__file__).resolve().parent.parent
PORT = int(os.getenv("FRAUD_SERVICE_PORT", "50051"))
KEY_PATH = Path(os.getenv("FRAUD_SERVICE_KEY_PATH", str(BASE_DIR / "certs" / "server-key.pem")))
CERT_PATH = Path(os.getenv("FRAUD_SERVICE_CERT_PATH", str(BASE_DIR / "certs" / "server-cert.pem")))

class FraudService(fraud_check_pb2_grpc.FraudCheckServiceServicer):

    def CheckTransaction(self, request, context):
        logging.info(f"Received transaction: {request.transaction_id}")
        return fraud_check_pb2.FraudCheckResponse(
            decision=fraud_check_pb2.FraudCheckResponse.ALLOW,
            risk_score=0.0,
            reason_code=["LOW_RISK"],
            model_version="v1"
        )

def serve():
    private_key_bytes = KEY_PATH.read_bytes()
    cert_bytes = CERT_PATH.read_bytes()

    server = grpc.server(ThreadPoolExecutor(max_workers=10))
    fraud_check_pb2_grpc.add_FraudCheckServiceServicer_to_server(FraudService(), server)

    credentials = grpc.ssl_server_credentials([(private_key_bytes, cert_bytes)])
    server.add_secure_port(f"[::]:{PORT}", credentials)
    server.start()
    logging.info(f"Fraud service started on port {PORT}")
    server.wait_for_termination()

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    serve()