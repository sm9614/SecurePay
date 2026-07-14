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
    public IdempotencyKeyResponseDTO checkIdempotency(IdempotencyKeyRequestDTO request) {
        Optional<IdempotencyKey> idempotencyKey = idempotencyKeyRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (idempotencyKey.isPresent()) {
            return IdempotencyKeyMapper.toResponseDTO(idempotencyKey.get());
        }

        try {
            IdempotencyKey newIdempotencyKey = new IdempotencyKey();
            newIdempotencyKey.setStatus(IdempotencyStatus.PENDING);
            newIdempotencyKey.setOperationType(request.getOperationType());
            newIdempotencyKey.setIdempotencyKey(request.getIdempotencyKey());
            idempotencyKeyRepository.saveAndFlush(newIdempotencyKey);
            return IdempotencyKeyMapper.toResponseDTO(newIdempotencyKey);

        } catch (DataIntegrityViolationException e) {
            Optional<IdempotencyKey> newIdempotencyKey = idempotencyKeyRepository.findByIdempotencyKey(request.getIdempotencyKey());
            return IdempotencyKeyMapper.toResponseDTO(newIdempotencyKey.orElseThrow());
        }
    }
}
