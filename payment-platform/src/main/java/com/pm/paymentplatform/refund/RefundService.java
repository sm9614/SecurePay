package com.pm.paymentplatform.refund;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import com.pm.paymentplatform.payment.ProcessorResult;
import com.pm.paymentplatform.paymentintent.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RefundService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final RefundRepository refundRepository;
    private final MerchantRepository merchantRepository;
    private final RefundProcessingService refundProcessingService;

    public RefundService(RefundRepository refundRepository,
                         PaymentIntentRepository paymentIntentRepository,
                         MerchantRepository merchantRepository,
                         RefundProcessingService refundProcessingService) {
        this.refundRepository = refundRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.merchantRepository = merchantRepository;
        this.refundProcessingService = refundProcessingService;
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

    public RefundResponseDTO processRefund(UUID refundId, UUID merchantId) {
        RefundProcessingContext context = refundProcessingService.beginProcessing(refundId, merchantId);
        ProcessorResult result = refundProcessingService.executeRefund(context);
        refundProcessingService.completeProcessing(refundId, result);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundNotFoundException(refundId));

        return RefundMapper.toResponseDTO(refund);
    }
}
