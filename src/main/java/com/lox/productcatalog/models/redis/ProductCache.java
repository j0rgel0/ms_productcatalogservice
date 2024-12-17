// ProductCache.java
package com.lox.productcatalog.models.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public class ProductCache {

    private String productId;
    private String name;
    private String description;
    private String category;
    private String price;
    private Integer availableQuantity;
}
