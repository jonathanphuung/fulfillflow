package com.fulfillflow.catalog;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryAllocator {

    private final InventoryRepository inventory;

    InventoryAllocator(InventoryRepository inventory) {
        this.inventory = inventory;
    }

    @Transactional
    public void reserve(UUID productId, int quantity) {
        findForUpdate(productId).reserve(quantity);
    }

    @Transactional
    public void release(UUID productId, int quantity) {
        findForUpdate(productId).release(quantity);
    }

    private Inventory findForUpdate(UUID productId) {
        return inventory.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
