package com.pm.paymentplatform.idempotency;

public record IdempotencyResult(IdempotencyOutcome outcome, IdempotencyKeyResponseDTO body) {

}
