package com.lox.productcatalog.common.kafka.event;

import java.time.Instant;
import java.util.UUID;

public interface Event {

    String getEventType();

    UUID getProductId();

    Instant getTimestamp();
}
