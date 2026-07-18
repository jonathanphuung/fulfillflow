package com.fulfillflow.catalog;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        int quantityOnHand,
        int quantityReserved,
        int quantityAvailable,
        Instant updatedAt) {

    static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                inventory.getQuantityOnHand() - inventory.getQuantityReserved(),
                inventory.getUpdatedAt());
    }
}
