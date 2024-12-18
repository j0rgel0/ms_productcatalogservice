// Product.java
package com.lox.productcatalog.api.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class Product {

    @Id
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer availableQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
