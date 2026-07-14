package com.pm.paymentplatform.idempotency;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-intents")
public class IdempotencyKeyController {

    private final IdempotencyService idempotencyService;

    public IdempotencyKeyController(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<IdempotencyKeyResponseDTO> createIdempotencyKey(@Valid @RequestBody IdempotencyKeyRequestDTO request) {
        IdempotencyResult idempotencyKeyResult = idempotencyService.checkIdempotency(request);
        IdempotencyKeyResponseDTO response = idempotencyKeyResult.body();
        if (idempotencyKeyResult.outcome() == IdempotencyOutcome.DUPLICATE_COMPLETE) {
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } else if (idempotencyKeyResult.outcome() == IdempotencyOutcome.DUPLICATE_PENDING) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
    }
}
