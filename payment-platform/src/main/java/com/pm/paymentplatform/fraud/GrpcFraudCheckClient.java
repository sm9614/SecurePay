package com.pm.paymentplatform.fraud;

import com.pm.fraud.grpc.FraudCheckRequest;
import com.pm.fraud.grpc.FraudCheckResponse;
import com.pm.fraud.grpc.FraudCheckServiceGrpc;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class GrpcFraudCheckClient implements FraudCheckClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcFraudCheckClient.class);
    private static final long DEADLINE_MS = 100;

    private final ManagedChannel channel;
    private final FraudCheckServiceGrpc.FraudCheckServiceBlockingStub stub;
    private final CircuitBreaker circuitBreaker;
    private final FraudCheckMapper fraudCheckMapper;

    public GrpcFraudCheckClient(FraudCheckMapper fraudCheckMapper,
                                @Value("${fraud.grpc.host}") String host,
                                @Value("${fraud.grpc.port}") int port,
                                @Value("${fraud.grpc.cert-path}") String certPath) {
        this.fraudCheckMapper = fraudCheckMapper;

        try {
            SslContext sslContext = GrpcSslContexts.forClient()
                    .sslProvider(SslProvider.JDK)
                    .trustManager(new File(certPath))
                    .build();
            this.channel = NettyChannelBuilder
                    .forAddress(host, port)
                    .sslContext(sslContext)
                    .build();
        } catch (SSLException e) {
            throw new IllegalStateException("Failed to load fraud service TLS certificate from " + certPath, e);
        }

        this.stub = FraudCheckServiceGrpc.newBlockingStub(channel);

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build();

        this.circuitBreaker= CircuitBreakerRegistry.of(config)
                .circuitBreaker("fraudCheck");
    }

    @Override
    public FraudDecision checkTransaction(FraudCheckContext context) {
        Supplier<FraudDecision> call = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                () -> callFraudService(context)
        );

        try {
            return call.get();
        } catch (CallNotPermittedException e) {
            log.warn("Fraud check circuit open, failing open for transaction {}", context.transactionId());
            return failOpen();
        } catch (StatusRuntimeException e) {
            log.error("Fraud check gRPC call failed for transaction {}: {}", context.transactionId(), e.getStatus(), e);
            return failOpen();
        }
    }

    private FraudDecision callFraudService(FraudCheckContext context) {
        FraudCheckRequest request = fraudCheckMapper.toRequest(context);

        FraudCheckResponse response = stub
                .withDeadline(Deadline.after(DEADLINE_MS, TimeUnit.MILLISECONDS))
                .checkTransaction(request);

        return fraudCheckMapper.toFraudDecision(response);
    }

    private FraudDecision failOpen() {
        return new FraudDecision(
                Decision.ALLOW,
                0.0f,
                List.of("FRAUD_SERVICE_UNAVAILABLE")
        );
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdown();
    }
}
