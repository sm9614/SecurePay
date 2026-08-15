package com.pm.paymentplatform.outbox;

import com.pm.paymentplatform.messaging.KafkaTopicMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${relay.batch-size}")
    private int batchSize;

    @Value("${relay.max-retries}")
    private int maxRetries;

    public OutboxRelay(OutboxEventRepository outboxEventRepository,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${relay.delay-ms}")
    @Transactional
    public void relayPendingEvents() {
        Pageable pageable = PageRequest.of(0, batchSize);
        List<OutboxEvent> batch = outboxEventRepository.findByStatusForUpdate(OutboxStatus.PENDING, pageable);
        batch.forEach(outboxEvent -> {
            String topic = KafkaTopicMapper.map(outboxEvent.getAggregateType());
            String key = outboxEvent.getAggregateId().toString();
            String value = outboxEvent.getPayload();
            try {
                kafkaTemplate.send(topic, key, value).get();
                outboxEvent.setStatus(OutboxEventStateMachine.transition(
                        outboxEvent.getStatus(),
                        OutboxStatus.PUBLISHED));
                outboxEvent.setPublishedAt(Instant.now());
                outboxEventRepository.save(outboxEvent);

            } catch (Exception e) {
                outboxEvent.setStatus(OutboxEventStateMachine.transition(
                        outboxEvent.getStatus(),
                        OutboxStatus.FAILED ));
                outboxEvent.setRetryCount(outboxEvent.getRetryCount() + 1);
                outboxEventRepository.save(outboxEvent);
            }
        });
    }

    @Scheduled(fixedDelayString = "${relay.delay-ms}")
    @Transactional
    public void retryFailedEvents() {
        Pageable pageable = PageRequest.of(0, batchSize);
        List<OutboxEvent> batch = outboxEventRepository.findByStatusForUpdate(OutboxStatus.FAILED, pageable);
        batch.forEach(outboxEvent -> {
           if (outboxEvent.getRetryCount() > maxRetries) {
               outboxEvent.setStatus(OutboxEventStateMachine.transition(
                       outboxEvent.getStatus(),
                       OutboxStatus.DEAD_LETTER
               ));
           } else {
               outboxEvent.setStatus(OutboxEventStateMachine.transition(
                       outboxEvent.getStatus(),
                       OutboxStatus.PENDING
               ));
           }
           outboxEventRepository.save(outboxEvent);
        });
    }
}
