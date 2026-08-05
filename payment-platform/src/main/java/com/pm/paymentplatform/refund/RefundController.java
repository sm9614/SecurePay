package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.idempotency.*;
import com.pm.paymentplatform.merchant.Merchant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@RestController
@RequestMapping("/payment-intents/{paymentIntentId}/refunds")
public class RefundController {

    private final IdempotencyKeyService idempotencyKeyService;
    private final RefundService refundService;
    private final ObjectMapper objectMapper;

    public RefundController(RefundService refundService,
                            IdempotencyKeyService idempotencyKeyService,
                            ObjectMapper objectMapper) {
        
        this.refundService = refundService;
        this.idempotencyKeyService = idempotencyKeyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<RefundResponseDTO> createRefund(@PathVariable UUID paymentIntentId,
                                                          @RequestHeader("idempotency-key") String idempotencyKey,
                                                          @Valid @RequestBody RefundRequestDTO request) {

        IdempotencyKeyRequestDTO idempotencyKeyRequest = new IdempotencyKeyRequestDTO();
        idempotencyKeyRequest.setIdempotencyKey(idempotencyKey);
        idempotencyKeyRequest.setOperationType(OperationType.CREATE_REFUND);
        IdempotencyResult idempotencyResult = idempotencyKeyService.checkIdempotency(idempotencyKeyRequest);

        if (idempotencyResult.outcome() == IdempotencyOutcome.DUPLICATE_COMPLETE) {
            String idempotencyResponse = idempotencyResult.body().getResponseBody();
            RefundResponseDTO response = objectMapper.readValue(idempotencyResponse, RefundResponseDTO.class);
            Integer status = idempotencyResult.body().getResponseStatus();
            return ResponseEntity.status(status).body(response);

        } else if (idempotencyResult.outcome() == IdempotencyOutcome.DUPLICATE_PENDING) {
            throw new DuplicateIdempotencyKeyPendingException("A request with this idempotency key is already in progress");
            
        } else {
            UUID merchantId = (UUID) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            RefundResponseDTO response = refundService.createRefund(paymentIntentId, request, merchantId);
            String body = objectMapper.writeValueAsString(response);
            idempotencyKeyService.completeIdempotencyKey(idempotencyResult.entity(), HttpStatus.ACCEPTED.value(), body);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
    }
}
