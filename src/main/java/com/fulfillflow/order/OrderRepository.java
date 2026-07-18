package com.fulfillflow.order;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

interface OrderRepository extends JpaRepository<FulfillmentOrder, UUID> {
    boolean existsByCustomerName(String customerName);

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<FulfillmentOrder> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "items")
    Optional<FulfillmentOrder> findForUpdateById(UUID id);
}
