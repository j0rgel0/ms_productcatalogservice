// ProductRepositoryCustom.java
package com.lox.productcatalog.api.repositories.r2dbc;

import com.lox.productcatalog.api.models.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepositoryCustom {

    Flux<Product> findProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable);

    Mono<Long> countProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice);

    Mono<BigDecimal> findMaxPrice();
}
