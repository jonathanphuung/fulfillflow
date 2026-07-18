package com.fulfillflow.catalog;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface InventoryRepository extends JpaRepository<Inventory, UUID> {
}
