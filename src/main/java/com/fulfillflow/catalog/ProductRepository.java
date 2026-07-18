package com.fulfillflow.catalog;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySkuIgnoreCase(String sku);
}
