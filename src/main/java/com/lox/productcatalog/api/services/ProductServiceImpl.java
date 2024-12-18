// src/main/java/com/lox/productcatalog/api/services/ProductServiceImpl.java

package com.lox.productcatalog.api.services;

import com.lox.productcatalog.api.exceptions.ProductNotFoundException;
import com.lox.productcatalog.api.kafka.events.EventType;
import com.lox.productcatalog.api.kafka.events.ProductAddedEvent;
import com.lox.productcatalog.api.kafka.events.ProductRemovedEvent;
import com.lox.productcatalog.api.kafka.events.ProductUpdatedEvent;
import com.lox.productcatalog.api.models.Product;
import com.lox.productcatalog.api.models.page.ProductPage;
import com.lox.productcatalog.api.repositories.r2dbc.ProductRepository;
import com.lox.productcatalog.common.kafka.event.EventProducer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EventProducer eventProducer;
    private final ReactiveRedisTemplate<String, Product> reactiveRedisTemplate;
    private final R2dbcEntityTemplate r2dbcEntityTemplate; // Inject R2dbcEntityTemplate
    private static final String HASH_KEY = "productCache";

    private ReactiveHashOperations<String, String, Product> hashOps;

    @PostConstruct
    public void init() {
        this.hashOps = reactiveRedisTemplate.opsForHash();
    }

    @PostConstruct
    public void clearProductCacheOnStartup() {
        log.info("Flushing Redis cache for key: {}", HASH_KEY);
        reactiveRedisTemplate.delete(HASH_KEY)
                .doOnSuccess(success -> log.info("Redis cache flushed successfully on startup."))
                .doOnError(
                        error -> log.error("Failed to flush Redis cache: {}", error.getMessage()))
                .subscribe();
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackCreateProduct")
    @Retry(name = "productServiceRetry", fallbackMethod = "fallbackCreateProduct")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "fallbackCreateProduct")
    public Mono<Product> createProduct(Product product) {
        product.setProductId(UUID.randomUUID());
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        log.info("Creating product: {}", product.getName());

        return r2dbcEntityTemplate.insert(Product.class)
                .using(product)
                .flatMap(savedProduct ->
                        eventProducer.publishEvent(EventType.PRODUCT_ADDED.getTopic(),
                                        ProductAddedEvent.fromProduct(savedProduct))
                                .then(Mono.just(savedProduct))
                )
                .flatMap(savedProduct ->
                        hashOps.put(HASH_KEY, savedProduct.getProductId().toString(), savedProduct)
                                .then(Mono.just(savedProduct))
                )
                .doOnSuccess(
                        p -> log.info("Product created and cached with ID: {}", p.getProductId()))
                .doOnError(e -> log.error("Error creating product: {}", e.getMessage()));
    }

    public Mono<Product> fallbackCreateProduct(Product product, Throwable throwable) {
        log.error("Fallback triggered for createProduct due to: {}", throwable.getMessage());
        return Mono.error(new RuntimeException(
                "Product creation is currently unavailable. Please try again later."));
    }

    @Override
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackGetProductById")
    @Retry(name = "productServiceRetry", fallbackMethod = "fallbackGetProductById")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "fallbackGetProductById")
    public Mono<Product> getProductById(UUID productId) {
        log.info("Fetching product with ID: {}", productId);
        String key = productId.toString();

        return hashOps.get(HASH_KEY, key)
                .flatMap(cachedProduct -> {
                    if (cachedProduct != null) {
                        log.info("Product retrieved from cache: {}", cachedProduct.getName());
                        return Mono.just(cachedProduct);
                    } else {
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(
                        productRepository.findById(productId)
                                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                                        "Product not found with ID: " + productId)))
                                .flatMap(product ->
                                        hashOps.put(HASH_KEY, product.getProductId().toString(),
                                                        product)
                                                .thenReturn(product)
                                )
                )
                .doOnNext(product -> log.info("Product retrieved: {}", product.getName()));
    }

    public Mono<Product> fallbackGetProductById(UUID productId, Throwable throwable) {
        log.error("Fallback triggered for getProductById due to: {}", throwable.getMessage());
        return Mono.error(new RuntimeException(
                "Product retrieval is currently unavailable. Please try again later."));
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackUpdateProduct")
    @Retry(name = "productServiceRetry", fallbackMethod = "fallbackUpdateProduct")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "fallbackUpdateProduct")
    public Mono<Product> updateProduct(UUID productId, Product product) {
        log.info("Updating product with ID: {}", productId);
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(
                        new ProductNotFoundException("Product not found with ID: " + productId)))
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setCategory(product.getCategory());
                    existingProduct.setAvailableQuantity(product.getAvailableQuantity());
                    existingProduct.setUpdatedAt(Instant.now());
                    return r2dbcEntityTemplate.update(Product.class)
                            .matching(org.springframework.data.relational.core.query.Query.query(
                                    org.springframework.data.relational.core.query.Criteria.where(
                                            "product_id").is(productId)))
                            .apply(org.springframework.data.relational.core.query.Update.update(
                                            "name", existingProduct.getName())
                                    .set("description", existingProduct.getDescription())
                                    .set("price", existingProduct.getPrice())
                                    .set("category", existingProduct.getCategory())
                                    .set("available_quantity",
                                            existingProduct.getAvailableQuantity())
                                    .set("updated_at", existingProduct.getUpdatedAt()))
                            .thenReturn(existingProduct);
                })
                .flatMap(updatedProduct ->
                        eventProducer.publishEvent(EventType.PRODUCT_UPDATED.getTopic(),
                                        ProductUpdatedEvent.fromProduct(updatedProduct))
                                .then(Mono.just(updatedProduct))
                )
                .flatMap(updatedProduct ->
                        hashOps.put(HASH_KEY, updatedProduct.getProductId().toString(),
                                        updatedProduct)
                                .then(Mono.just(updatedProduct))
                )
                .doOnSuccess(p -> log.info("Product updated and cache refreshed with ID: {}",
                        p.getProductId()))
                .doOnError(e -> log.error("Error updating product: {}", e.getMessage()));
    }

    public Mono<Product> fallbackUpdateProduct(UUID productId, Product product,
            Throwable throwable) {
        log.error("Fallback triggered for updateProduct due to: {}", throwable.getMessage());
        return Mono.error(new RuntimeException(
                "Product update is currently unavailable. Please try again later."));
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackDeleteProduct")
    @Retry(name = "productServiceRetry", fallbackMethod = "fallbackDeleteProduct")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "fallbackDeleteProduct")
    public Mono<Void> deleteProduct(UUID productId) {
        log.info("Deleting product with ID: {}", productId);
        String key = productId.toString();

        return productRepository.findById(productId)
                .switchIfEmpty(
                        Mono.error(new ProductNotFoundException(
                                "Product not found with ID: " + productId)))
                .flatMap(existingProduct ->
                        r2dbcEntityTemplate.delete(Product.class)
                                .matching(
                                        org.springframework.data.relational.core.query.Query.query(
                                                org.springframework.data.relational.core.query.Criteria.where(
                                                        "product_id").is(productId)))
                                .all()
                                .then(eventProducer.publishEvent(
                                        EventType.PRODUCT_REMOVED.getTopic(),
                                        ProductRemovedEvent.fromProductId(productId)))
                                .then(hashOps.remove(HASH_KEY, key))
                )
                .then()
                .doOnSuccess(
                        v -> log.info("Product deleted and cache removed with ID: {}", productId))
                .doOnError(e -> log.error("Error deleting product: {}", e.getMessage()));
    }

    public Mono<Void> fallbackDeleteProduct(UUID productId, Throwable throwable) {
        log.error("Fallback triggered for deleteProduct due to: {}", throwable.getMessage());
        return Mono.error(new RuntimeException(
                "Product deletion is currently unavailable. Please try again later."));
    }

    @Override
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackListProducts")
    @Retry(name = "productServiceRetry", fallbackMethod = "fallbackListProducts")
    @RateLimiter(name = "productServiceRateLimiter", fallbackMethod = "fallbackListProducts")
    public Mono<ProductPage> listProducts(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        log.info(
                "Listing products with filters - Categories: {}, MinPrice: {}, MaxPrice: {}, Page: {}, Size: {}, Sort: {}",
                categories, minPrice, maxPrice, pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort());

        // Assign minPrice to 0 if null
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
            log.info("minPrice is null. Setting minPrice to 0.");
        }

        Mono<BigDecimal> resolvedMaxPriceMono;

        if (maxPrice == null) {
            // Get the maximum price existing in the database
            resolvedMaxPriceMono = productRepository.findMaxPrice()
                    .doOnNext(max -> log.info("maxPrice is null. Retrieved maxPrice: {}", max))
                    .defaultIfEmpty(BigDecimal.ZERO);
        } else {
            resolvedMaxPriceMono = Mono.just(maxPrice)
                    .doOnNext(max -> log.info("maxPrice provided: {}", max));
        }

        BigDecimal finalMinPrice = minPrice;
        return resolvedMaxPriceMono.flatMap(resolvedMaxPrice -> {
            // Verify that minPrice <= resolvedMaxPrice
            if (finalMinPrice.compareTo(resolvedMaxPrice) > 0) {
                return Mono.error(
                        new IllegalArgumentException("minPrice cannot be greater than maxPrice"));
            }

            Mono<Long> totalElementsMono = productRepository.countProductsByFilters(categories,
                    finalMinPrice, resolvedMaxPrice);
            Mono<List<Product>> productsMono = productRepository.findProductsByFilters(categories,
                            finalMinPrice, resolvedMaxPrice, pageable)
                    .collectList();

            return Mono.zip(totalElementsMono, productsMono)
                    .map(tuple -> {
                        long totalElements = tuple.getT1();
                        List<Product> products = tuple.getT2();
                        int totalPages = (int) Math.ceil(
                                (double) totalElements / pageable.getPageSize());
                        int currentPage = pageable.getPageNumber();
                        int pageSize = pageable.getPageSize();

                        return new ProductPage(products, totalElements, totalPages, currentPage,
                                pageSize);
                    })
                    .doOnSuccess(productPage -> log.info("Retrieved {} products out of {}",
                            productPage.getProducts().size(), productPage.getTotalElements()))
                    .doOnError(e -> log.error("Error retrieving product page: {}", e.getMessage()));
        });
    }

    public Mono<ProductPage> fallbackListProducts(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable, Throwable throwable) {
        log.error("Fallback triggered for listProducts due to: {}", throwable.getMessage());
        return Mono.error(new RuntimeException(
                "Product listing is currently unavailable. Please try again later."));
    }
}
