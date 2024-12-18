package com.lox.productcatalog.api.kafka.events;

import com.lox.productcatalog.api.kafka.topics.KafkaTopics;
import lombok.Getter;

@Getter
public enum EventType {
    PRODUCT_ADDED(KafkaTopics.PRODUCT_EVENTS_TOPIC),
    PRODUCT_UPDATED(KafkaTopics.PRODUCT_EVENTS_TOPIC),
    PRODUCT_REMOVED(KafkaTopics.PRODUCT_EVENTS_TOPIC);

    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }
}
