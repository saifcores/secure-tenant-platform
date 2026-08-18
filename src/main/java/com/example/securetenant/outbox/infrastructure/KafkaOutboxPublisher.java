package com.example.securetenant.outbox.infrastructure;

import com.example.securetenant.outbox.application.OutboxEventRepository;
import com.example.securetenant.outbox.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnProperty(name = "app.payments.outbox.enabled", havingValue = "true")
public class KafkaOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final String topic;
    private final int batchSize;

    public KafkaOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            @Value("${app.messaging.kafka.topic:payments.events}") String topic,
            @Value("${app.payments.outbox.batch-size:50}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.payments.outbox.poll-interval:5s}")
    public void publishUnpublished() {
        outboxEventRepository.findUnpublished(batchSize).forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(topic, event.aggregateId(), jsonMapper.writeValueAsString(toMessage(event)))
                    .get();
            outboxEventRepository.markPublished(event.id());
        } catch (Exception ex) {
            log.warn("Failed to publish outbox event id={}: {}", event.id(), ex.getMessage());
        }
    }

    private Map<String, Object> toMessage(OutboxEvent event) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("id", event.id().toString());
        message.put("tenantId", event.tenantId());
        message.put("aggregateType", event.aggregateType());
        message.put("aggregateId", event.aggregateId());
        message.put("eventType", event.eventType());
        message.put("payload", event.payload());
        message.put("createdAt", event.createdAt() == null ? null : event.createdAt().toString());
        return message;
    }
}
