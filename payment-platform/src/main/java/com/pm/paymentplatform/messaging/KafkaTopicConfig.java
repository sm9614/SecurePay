package com.pm.paymentplatform.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentIntentEventTopic() {
        return TopicBuilder.name("payment-intent-events")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundEventTopic() {
        return TopicBuilder.name("refund-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
