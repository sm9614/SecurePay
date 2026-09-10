package com.pm.paymentplatform.messaging;

import java.util.UUID;

@FunctionalInterface
public interface EventDispatcher {
    UUID dispatch(EventType eventType, String message) throws Exception;
}