import grpc

import fraud_check_pb2
import fraud_check_pb2_grpc


def main():
    # Trust the server's self-signed certificate
    with open("../certs/server-cert.pem", "rb") as cert_file:
        cert_bytes = cert_file.read()

    credentials = grpc.ssl_channel_credentials(
        root_certificates=cert_bytes
    )

    # Connect to the local TLS server
    channel = grpc.secure_channel(
        "localhost:50051",
        credentials,
        options=[
            ("grpc.ssl_target_name_override", "localhost")
        ],
    )

    try:
        stub = fraud_check_pb2_grpc.FraudCheckServiceStub(channel)

        request = fraud_check_pb2.FraudCheckRequest(
            transaction_id="test-1",
            merchant_id="merchant-1",
            amount_minor_units=2500,
            currency="USD",
            billing_country="US",
            ip_country="US",
            is_first_time_card=False,
            merchant_transaction_count_today=5,
            merchant_average_amount_minor_units=3000,
            velocity_count_5min=1,
            velocity_count_1hr=2,
            prior_decline_count_24h=0,
            prior_refund_rate=0.01,
            is_new_merchant=False,
        )

        response = stub.CheckTransaction(request, timeout=5)

        print("Server response:")
        print(response)

    except grpc.RpcError as error:
        print(f"gRPC request failed: {error.code()}")
        print(f"Details: {error.details()}")

    finally:
        channel.close()


if __name__ == "__main__":
    main()
