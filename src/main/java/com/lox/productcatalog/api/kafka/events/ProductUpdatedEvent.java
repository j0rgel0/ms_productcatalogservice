// src/main/java/com/lox/productcatalog/api/kafka/events/ProductUpdatedEvent.java

package com.lox.productcatalog.api.kafka.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lox.productcatalog.api.models.Product;
import com.lox.productcatalog.common.kafka.event.Event;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductUpdatedEvent implements Event {

    private final String eventType;
    private final UUID productId;
    private final String name;
    private final String description;
    private final double price;
    private final String category;
    private final int availableQuantity;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public UUID getProductId() {
        return productId;
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    public Instant getTimestamp() {
        return timestamp;
    }

    // Static method to build the event from a Product
    public static ProductUpdatedEvent fromProduct(Product product) {
        return ProductUpdatedEvent.builder()
                .eventType(EventType.PRODUCT_UPDATED.name())
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().doubleValue())
                .category(product.getCategory())
                .availableQuantity(product.getAvailableQuantity())
                .timestamp(Instant.now())
                .build();
    }
}
