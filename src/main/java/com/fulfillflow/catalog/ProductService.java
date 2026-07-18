package com.fulfillflow.catalog;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProductService {

    private final ProductRepository products;

    ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    ProductResponse create(CreateProductRequest request) {
        var sku = request.sku().trim().toUpperCase(Locale.ROOT);
        if (products.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateSkuException(sku);
        }

        var product = new Product(
                sku,
                request.name().trim(),
                normalizeDescription(request.description()),
                request.initialQuantity());
        return ProductResponse.from(products.save(product));
    }

    @Transactional(readOnly = true)
    List<ProductResponse> list() {
        return products.findAll(Sort.by("name").ascending()).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    ProductResponse get(UUID id) {
        return products.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
