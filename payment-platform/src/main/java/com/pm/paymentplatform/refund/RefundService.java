package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.outbox.AggregateType;
import com.pm.paymentplatform.outbox.OutboxEventService;
import com.pm.paymentplatform.paymentintent.*;
import com.pm.paymentplatform.statemachine.InvalidStateTransitionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RefundService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final RefundRepository refundRepository;
    private final MerchantRepository merchantRepository;
    private final OutboxEventService outboxEventService;

    public RefundService(RefundRepository refundRepository,
                         PaymentIntentRepository paymentIntentRepository,
                         MerchantRepository merchantRepository,
                         OutboxEventService outboxEventService) {
        this.refundRepository = refundRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public RefundResponseDTO createRefund(UUID paymentIntentId,
                                          RefundRequestDTO request,
                                          UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        PaymentIntent paymentIntent = paymentIntentRepository.getPaymentIntentByIdWithLock(paymentIntentId)
                .orElseThrow(() -> new PaymentIntentNotFoundException(paymentIntentId));

        if (!paymentIntent.getMerchant().getId().equals(merchant.getId())) {
            throw new PaymentIntentNotFoundException(paymentIntentId);
        }

        if (paymentIntent.getStatus() != PaymentIntentStatus.SUCCEEDED) {
            throw new PaymentIntentNotRefundableException(paymentIntent.getStatus());
        }

        Long remainingBalance = paymentIntent.getAmountMinorUnits() - refundRepository.sumReservedAmountByPaymentIntentId(paymentIntentId);

        if (request.getAmountMinorUnits() > remainingBalance) {
            throw new RefundExceedsAvailableBalanceException(remainingBalance, request.getAmountMinorUnits());
        }

        Refund refund = new Refund();
        refund.setAmountMinorUnits(request.getAmountMinorUnits());
        refund.setStatus(RefundStatus.CREATED);
        refund.setPaymentIntent(paymentIntent);
        refund.setMerchant(merchant);

        refundRepository.save(refund);

        return RefundMapper.toResponseDTO(refund);
    }

    @Transactional
    public RefundResponseDTO processRefund(UUID refundId,
                                           UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        Refund refund = refundRepository.getRefundByIdWithLock(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        refund.setStatus(RefundStateMachine.transition(
                refund.getStatus(),
                RefundStatus.PROCESSING));

        if (!refund.getMerchant().getId().equals(merchant.getId())) {
            throw new RefundNotFoundException(refundId);
        }

        if (simulateProcessor()) {
            refund.setStatus(RefundStateMachine.transition(refund.getStatus(), RefundStatus.SUCCEEDED));
        } else {
            refund.setStatus(RefundStateMachine.transition(refund.getStatus(), RefundStatus.FAILED));
            RefundFailedEvent refundFailedEvent = new RefundFailedEvent(
                    refundId,
                    refund.getPaymentIntent().getId(),
                    refund.getAmountMinorUnits()
            );
            outboxEventService.recordEvent(AggregateType.REFUND, refundId, "REFUND_FAILED", refundFailedEvent);

        }
        refundRepository.save(refund);
        return RefundMapper.toResponseDTO(refund);

    }

    //todo replace this with real payment processing
    private boolean simulateProcessor() {
        return false;
    }
}
