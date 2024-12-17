// ProductRemovedEvent.java
package com.lox.productcatalog.models.events;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProductRemovedEvent implements Event {
    private UUID productId;
    private Instant timestamp;

    public static ProductRemovedEvent fromProductId(UUID productId) {
        return ProductRemovedEvent.builder()
                .productId(productId)
                .timestamp(Instant.now())
                .build();
    }
}
