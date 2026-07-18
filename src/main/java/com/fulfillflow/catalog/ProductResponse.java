package com.fulfillflow.catalog;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        boolean active,
        int quantityOnHand,
        int quantityReserved,
        Instant createdAt) {

    static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.isActive(),
                product.getInventory().getQuantityOnHand(),
                product.getInventory().getQuantityReserved(),
                product.getCreatedAt());
    }
}
