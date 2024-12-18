// ProductService.java
package com.lox.productcatalog.api.services;

import com.lox.productcatalog.api.models.Product;
import com.lox.productcatalog.api.models.page.ProductPage;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(Product product);

    Mono<Product> getProductById(UUID productId);

    Mono<Product> updateProduct(UUID productId, Product product);

    Mono<Void> deleteProduct(UUID productId);

    Mono<ProductPage> listProducts(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable);
}
