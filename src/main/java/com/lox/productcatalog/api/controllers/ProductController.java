package com.lox.productcatalog.api.controllers;

import com.lox.productcatalog.api.models.Product;
import com.lox.productcatalog.api.models.page.ProductPage;
import com.lox.productcatalog.api.services.ProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return productService.createProduct(product)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable("id") UUID id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable("id") UUID id,
            @RequestBody Product product) {
        return productService.updateProduct(id, product)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable("id") UUID id) {
        return productService.deleteProduct(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping
    public Mono<ResponseEntity<ProductPage>> listProducts(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {

        // Parameter validations
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (page < 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        if (size <= 0) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        // Build Pageable
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                pageable = PageRequest.of(page, size,
                        Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));
            } else {
                pageable = PageRequest.of(page, size);
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        return productService.listProducts(categories, minPrice, maxPrice, pageable)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
    }
}
