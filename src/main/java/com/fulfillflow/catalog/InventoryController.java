package com.fulfillflow.catalog;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
class InventoryController {

    private final InventoryService inventory;

    InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/{productId}")
    InventoryResponse get(@PathVariable UUID productId) {
        return inventory.get(productId);
    }

    @PatchMapping("/{productId}")
    InventoryResponse adjust(
            @PathVariable UUID productId,
            @Valid @RequestBody AdjustInventoryRequest request) {
        return inventory.adjust(productId, request);
    }
}
