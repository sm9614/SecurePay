package com.pm.paymentplatform.webhook;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class WebhookDeliveryRelay {

    private static final int MAX_ATTEMPTS = 10;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final RestClient restClient;
    private final WebhookSignatureService webhookSignatureService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public WebhookDeliveryRelay(WebhookDeliveryRepository webhookDeliveryRepository,
                                WebhookSignatureService webhookSignatureService) {
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.webhookSignatureService = webhookSignatureService;
        this.restClient = RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .build();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(config);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(5))
                .withReadTimeout(Duration.ofSeconds(10));
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }

    @Scheduled(fixedDelay = 5000)
    public void pollAndDeliver() {
        List<WebhookDelivery> deliveries = webhookDeliveryRepository.
                findByStatusAndNextAttemptAtBefore(WebhookDeliveryStatus.PENDING, Instant.now());

        for (WebhookDelivery delivery : deliveries) {
            attemptDelivery(delivery);
        }
    }

    private void attemptDelivery(WebhookDelivery delivery) {
        if (delivery.getAttemptCount() >= MAX_ATTEMPTS) {
            delivery.setStatus(WebhookDeliveryStatus.DEAD_LETTER);
            webhookDeliveryRepository.save(delivery);
            return;
        }

        try {

            String secret = delivery.getWebhookEndpoint().getSigningSecret();
            String timeStamp = Instant.now().toString();
            String payload = delivery.getPayload();
            String signature = webhookSignatureService.sign(secret, timeStamp, payload);

            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(
                    delivery.getWebhookEndpoint().getId().toString());

            ResponseEntity<String> response = circuitBreaker.executeSupplier(() ->
                    restClient.post()
                    .uri(delivery.getWebhookEndpoint().getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Timestamp", timeStamp)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class)
            );

            delivery.setStatus(WebhookDeliveryStatus.DELIVERED);
            delivery.setLastResponseCode(String.valueOf(response.getStatusCode().value()));
            delivery.setLastResponseBody(response.getBody());

        } catch (RestClientResponseException e) {
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setNextAttemptAt(computeNextAttempt(delivery.getAttemptCount()));
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setLastResponseCode(String.valueOf(e.getStatusCode().value()));
            delivery.setLastResponseBody(e.getResponseBodyAsString());

        } catch (ResourceAccessException e) {
            delivery.setStatus(WebhookDeliveryStatus.FAILED);
            delivery.setNextAttemptAt(computeNextAttempt(delivery.getAttemptCount()));
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setLastResponseBody(e.getMessage());
        } catch (CallNotPermittedException e) {
            delivery.setNextAttemptAt(Instant.now().plus(Duration.ofSeconds(30)));
        }
        webhookDeliveryRepository.save(delivery);
    }

    public Instant computeNextAttempt(int attemptCount) {
        long delay = 2 * (1L << attemptCount);
        long jitterBound = Math.max(1, delay/5);
        long jitter = ThreadLocalRandom.current().nextLong(jitterBound);
        Duration duration = Duration.ofSeconds(jitter + delay);
        return Instant.now().plus(duration);
    }
}
