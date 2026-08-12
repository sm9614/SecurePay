package com.pm.paymentplatform.refund;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/refunds")
public class RefundActionController {

    private final RefundService refundService;

    public RefundActionController(RefundService refundService) {
        this.refundService = refundService;
    }


    @PostMapping("/{id}/process")
    public ResponseEntity<RefundResponseDTO> processRefund(@PathVariable String id) {
        UUID merchantId = (UUID) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        RefundResponseDTO response = refundService.processRefund(UUID.fromString(id), merchantId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
