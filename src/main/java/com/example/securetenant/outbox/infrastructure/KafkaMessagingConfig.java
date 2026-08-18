package com.example.securetenant.outbox.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.messaging.kafka.enabled", havingValue = "true")
public class KafkaMessagingConfig {
}
