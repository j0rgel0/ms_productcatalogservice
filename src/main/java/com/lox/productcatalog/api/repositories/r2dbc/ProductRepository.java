package com.lox.productcatalog.api.repositories.r2dbc;

import com.lox.productcatalog.api.models.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, UUID>,
        ProductRepositoryCustom {

    Mono<BigDecimal> findMaxPrice();

    Mono<Long> countProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice);

    Flux<Product> findProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable);
}