package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.paymentintent.PaymentIntent;
import com.pm.paymentplatform.paymentintent.PaymentIntentNotFoundException;
import com.pm.paymentplatform.paymentintent.PaymentIntentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RefundService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final RefundRepository refundRepository;

    public RefundService(RefundRepository refundRepository, PaymentIntentRepository paymentIntentRepository) {
        this.refundRepository = refundRepository;
        this.paymentIntentRepository = paymentIntentRepository;
    }

    @Transactional
    public RefundResponseDTO createRefund(UUID paymentIntentId, RefundRequestDTO request) {
        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentByIdWithLock(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        Long remainingBalance = paymentIntent.getAmountMinorUnits() - refundRepository.sumReservedAmountByPaymentIntentId(paymentIntentId);

        if (request.getAmountMinorUnits() > remainingBalance) {
            throw new RefundExceedsAvailableBalanceException(remainingBalance, request.getAmountMinorUnits());
        }

        Refund refund = new Refund();
        refund.setAmountMinorUnits(request.getAmountMinorUnits());
        refund.setStatus(RefundStatus.CREATED);
        refund.setPaymentIntent(paymentIntent);

        refundRepository.save(refund);

        return RefundMapper.toResponseDTO(refund);
    }
}
