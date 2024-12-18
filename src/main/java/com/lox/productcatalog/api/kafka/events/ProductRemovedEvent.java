// src/main/java/com/lox/productcatalog/api/kafka/events/ProductRemovedEvent.java

package com.lox.productcatalog.api.kafka.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lox.productcatalog.common.kafka.event.Event;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductRemovedEvent implements Event {

    private final String eventType;
    private final UUID productId;
    private final Instant timestamp;

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public String getEventType() {
        return eventType;
    }

    @Override
    public UUID getProductId() {
        return productId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    // Static method to build the event from a productId
    public static ProductRemovedEvent fromProductId(UUID productId) {
        return ProductRemovedEvent.builder()
                .eventType(EventType.PRODUCT_REMOVED.name())
                .productId(productId)
                .timestamp(Instant.now())
                .build();
    }
}
