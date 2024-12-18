package com.lox.productcatalog.api.kafka.events;

import java.time.Instant;
import java.util.UUID;

public interface Event {

    String getEventType();

    UUID getProductId();

    Instant getTimestamp();
}
