package com.fulfillflow.catalog;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InventoryService {

    private final InventoryRepository inventory;

    InventoryService(InventoryRepository inventory) {
        this.inventory = inventory;
    }

    @Transactional(readOnly = true)
    InventoryResponse get(UUID productId) {
        return InventoryResponse.from(findInventory(productId));
    }

    @Transactional
    InventoryResponse adjust(UUID productId, AdjustInventoryRequest request) {
        var stock = findInventory(productId);
        stock.adjustOnHand(request.quantityChange());
        return InventoryResponse.from(stock);
    }

    private Inventory findInventory(UUID productId) {
        return inventory.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
