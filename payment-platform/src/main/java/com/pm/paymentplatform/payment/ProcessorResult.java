package com.pm.paymentplatform.payment;

public sealed interface ProcessorResult
    permits ProcessorResult.Success, ProcessorResult.Declined, ProcessorResult.ProcessorError  {

    record Success(String processorReference) implements ProcessorResult {}

    record Declined(String reasonCode, String message) implements ProcessorResult {}

    record ProcessorError(String message) implements ProcessorResult {}
}
