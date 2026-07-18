package com.fulfillflow.order;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<FulfillmentOrder, UUID> {
    @Override
    @EntityGraph(attributePaths = "items")
    Optional<FulfillmentOrder> findById(UUID id);
}
