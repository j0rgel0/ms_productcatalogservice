// ProductAddedEvent.java
package com.lox.productcatalog.api.models.events;

import com.lox.productcatalog.api.models.Product;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductAddedEvent implements Event {

    private UUID productId;
    private String name;
    private Instant timestamp;

    public static ProductAddedEvent fromProduct(Product product) {
        return ProductAddedEvent.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .timestamp(Instant.now())
                .build();
    }
}
