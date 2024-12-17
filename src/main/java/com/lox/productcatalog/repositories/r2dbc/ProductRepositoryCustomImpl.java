// ProductRepositoryCustomImpl.java
package com.lox.productcatalog.repositories.r2dbc;

import com.lox.productcatalog.models.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final DatabaseClient databaseClient;

    @Autowired
    public ProductRepositoryCustomImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<Product> findProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice, Pageable pageable) {
        // Asignar valores predeterminados si son null
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }

        // Construcción dinámica de la consulta SQL
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM products WHERE 1=1");

        if (categories != null && !categories.isEmpty()) {
            queryBuilder.append(" AND category IN (:categories)");
        }

        if (minPrice != null) {
            queryBuilder.append(" AND price >= :minPrice");
        }

        if (maxPrice != null) {
            queryBuilder.append(" AND price <= :maxPrice");
        }

        // Manejar el ordenamiento
        if (pageable.getSort().isSorted()) {
            queryBuilder.append(" ORDER BY ");
            StringBuilder orderByBuilder = new StringBuilder();
            pageable.getSort().forEach(order -> {
                orderByBuilder.append(order.getProperty())
                        .append(" ")
                        .append(order.getDirection().toString())
                        .append(", ");
            });
            // Eliminar la última coma y espacio
            String orderBy = orderByBuilder.substring(0, orderByBuilder.length() - 2);
            queryBuilder.append(orderBy);
        }

        // Manejar la paginación
        queryBuilder.append(" LIMIT :limit OFFSET :offset");

        // Construir la consulta
        return databaseClient.sql(queryBuilder.toString())
                .bind("categories", categories)
                .bind("minPrice", minPrice)
                .bind("maxPrice", maxPrice)
                .bind("limit", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, metadata) -> {
                    Product product = new Product();
                    product.setProductId(row.get("product_id", UUID.class));
                    product.setName(row.get("name", String.class));
                    product.setDescription(row.get("description", String.class));
                    product.setPrice(row.get("price", BigDecimal.class));
                    product.setCategory(row.get("category", String.class));
                    product.setAvailableQuantity(row.get("available_quantity", Integer.class));
                    product.setCreatedAt(row.get("created_at", Instant.class));
                    product.setUpdatedAt(row.get("updated_at", Instant.class));
                    return product;
                })
                .all();
    }

    @Override
    public Mono<Long> countProductsByFilters(List<String> categories, BigDecimal minPrice,
            BigDecimal maxPrice) {
        // Asignar valores predeterminados si son null
        if (minPrice == null) {
            minPrice = BigDecimal.ZERO;
        }

        // Construcción dinámica de la consulta SQL para el conteo
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1");

        if (categories != null && !categories.isEmpty()) {
            queryBuilder.append(" AND category IN (:categories)");
        }

        if (minPrice != null) {
            queryBuilder.append(" AND price >= :minPrice");
        }

        if (maxPrice != null) {
            queryBuilder.append(" AND price <= :maxPrice");
        }

        return databaseClient.sql(queryBuilder.toString())
                .bind("categories", categories)
                .bind("minPrice", minPrice)
                .bind("maxPrice", maxPrice)
                .map(row -> row.get(0, Long.class))
                .one();
    }

    @Override
    public Mono<BigDecimal> findMaxPrice() {
        String query = "SELECT MAX(price) FROM products";
        return databaseClient.sql(query)
                .map(row -> row.get(0, BigDecimal.class))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }
}
