// EventProducerImpl.java
package com.lox.productcatalog.kafka;

import com.lox.productcatalog.models.events.Event;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducerImpl implements EventProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;

    public EventProducerImpl(KafkaTemplate<String, Event> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<Void> sendEvent(String topic, Event event) {
        return kafkaTemplate.send(topic, event)
                .thenApply(result -> null);
    }
}
