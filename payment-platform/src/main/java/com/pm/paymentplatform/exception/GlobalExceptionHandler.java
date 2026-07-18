package com.pm.paymentplatform.exception;

import com.pm.paymentplatform.idempotency.DuplicateIdempotencyKeyPendingException;
import com.pm.paymentplatform.idempotency.IdempotencyKeyNotFoundException;
import com.pm.paymentplatform.paymentintent.InvalidPaymentStateTransitionException;
import com.pm.paymentplatform.paymentintent.PaymentIntentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyNotFound(IdempotencyKeyNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(),null, Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidPaymentStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStateTransition(InvalidPaymentStateTransitionException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), null, Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DuplicateIdempotencyKeyPendingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIdempotencyKeyPending(DuplicateIdempotencyKeyPendingException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), null,Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), null, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaymentIntentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentIntentNotFound(PaymentIntentNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), null, Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse("Unexpected error occurred", null, Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
