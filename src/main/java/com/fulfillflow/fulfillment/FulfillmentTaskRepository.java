package com.fulfillflow.fulfillment;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface FulfillmentTaskRepository extends JpaRepository<FulfillmentTask, UUID> {
    boolean existsByOrderId(UUID orderId);
    Optional<FulfillmentTask> findByOrderId(UUID orderId);
    List<FulfillmentTask> findAllByOrderByCreatedAtAsc();
}
