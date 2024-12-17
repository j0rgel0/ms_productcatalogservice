// ProductUpdatedEvent.java
package com.lox.productcatalog.models.events;

import com.lox.productcatalog.models.Product;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProductUpdatedEvent implements Event {
    private UUID productId;
    private String name;
    private Instant timestamp;

    public static ProductUpdatedEvent fromProduct(Product product) {
        return ProductUpdatedEvent.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .timestamp(Instant.now())
                .build();
    }
}
