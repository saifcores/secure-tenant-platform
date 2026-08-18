package com.example.securetenant.outbox.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.kafka.enabled", havingValue = "true")
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    @KafkaListener(
            topics = "${app.messaging.kafka.topic:payments.events}",
            groupId = "${spring.kafka.consumer.group-id:securetenant-api}"
    )
    public void onPaymentEvent(String payload) {
        log.info("Consumed payment event: {}", payload);
    }
}
