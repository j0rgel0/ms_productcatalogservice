package com.lox.productcatalog.models.page;

import com.lox.productcatalog.models.Product;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPage {
    private List<Product> products;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
