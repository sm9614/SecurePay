package com.pm.paymentplatform.paymentintent;

import com.pm.paymentplatform.idempotency.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/payment-intents")
public class PaymentIntentController {

    private final IdempotencyKeyService idempotencyKeyService;
    private final PaymentIntentService paymentIntentService;
    private final ObjectMapper objectMapper;

    public PaymentIntentController(IdempotencyKeyService idempotencyKeyService,
                                   PaymentIntentService paymentIntentService,
                                   ObjectMapper objectMapper) {
        this.idempotencyKeyService = idempotencyKeyService;
        this.paymentIntentService = paymentIntentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<PaymentIntentResponseDTO> createPaymentIntent(@Valid @RequestBody PaymentIntentRequestDTO request,
                                                                        @RequestHeader("Idempotency-Key") String idempotencyKey) {

        IdempotencyKeyRequestDTO idempotencyKeyRequest = new IdempotencyKeyRequestDTO();
        idempotencyKeyRequest.setIdempotencyKey(idempotencyKey);
        idempotencyKeyRequest.setOperationType(OperationType.CREATE_PAYMENT_INTENT);
        IdempotencyResult idempotencyResult = idempotencyKeyService.checkIdempotency(idempotencyKeyRequest);

        if (idempotencyResult.outcome() == IdempotencyOutcome.DUPLICATE_COMPLETE) {
            String idempotencyResponse = idempotencyResult.body().getResponseBody();
            PaymentIntentResponseDTO response = objectMapper.readValue(idempotencyResponse, PaymentIntentResponseDTO.class);
            Integer status = idempotencyResult.body().getResponseStatus();
            return ResponseEntity.status(status).body(response);

        } else if (idempotencyResult.outcome() == IdempotencyOutcome.DUPLICATE_PENDING) {
            throw new DuplicateIdempotencyKeyPendingException("A request with this idempotency key is already in progress");

        }else {

            PaymentIntentResponseDTO paymentIntentResponse = paymentIntentService.createPaymentIntent(request.getAmountMinorUnits(), request.getCurrency(), idempotencyResult.entity());
            String body = objectMapper.writeValueAsString(paymentIntentResponse);
            idempotencyKeyService.completeIdempotencyKey(idempotencyResult.entity(), HttpStatus.ACCEPTED.value(), body);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentIntentResponse);
        }
    }
}
