package com.pm.paymentplatform.idempotency;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyService {
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyService(IdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public IdempotencyResult checkIdempotency(IdempotencyKeyRequestDTO request) {
        Optional<IdempotencyKey> idempotencyKey = idempotencyKeyRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (idempotencyKey.isPresent() && idempotencyKey.get().getStatus() == IdempotencyStatus.PENDING) {
            return new IdempotencyResult(IdempotencyOutcome.DUPLICATE_PENDING, IdempotencyKeyMapper.toResponseDTO(idempotencyKey.get()));
        } else if (idempotencyKey.isPresent() && idempotencyKey.get().getStatus() == IdempotencyStatus.COMPLETE) {
            return new IdempotencyResult(IdempotencyOutcome.DUPLICATE_COMPLETE, IdempotencyKeyMapper.toResponseDTO(idempotencyKey.get()));
        } else {
            try {
                IdempotencyKey newIdempotencyKey = new IdempotencyKey();
                newIdempotencyKey.setStatus(IdempotencyStatus.PENDING);
                newIdempotencyKey.setOperationType(request.getOperationType());
                newIdempotencyKey.setIdempotencyKey(request.getIdempotencyKey());
                idempotencyKeyRepository.saveAndFlush(newIdempotencyKey);
                return new IdempotencyResult(IdempotencyOutcome.CREATED, IdempotencyKeyMapper.toResponseDTO(newIdempotencyKey));

            } catch (DataIntegrityViolationException e) {
                IdempotencyKey existing = idempotencyKeyRepository
                        .findByIdempotencyKey(request.getIdempotencyKey())
                        .orElseThrow();

                IdempotencyOutcome outcome = existing.getStatus() == IdempotencyStatus.PENDING
                        ? IdempotencyOutcome.DUPLICATE_PENDING
                        : IdempotencyOutcome.DUPLICATE_COMPLETE;
                return new IdempotencyResult(outcome, IdempotencyKeyMapper.toResponseDTO(existing));
            }
        }
    }
}
