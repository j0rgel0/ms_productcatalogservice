package com.lox.productcatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "com.lox.productcatalog.api.repositories.r2dbc")
public class ProductcatalogserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductcatalogserviceApplication.class, args);
    }
}
